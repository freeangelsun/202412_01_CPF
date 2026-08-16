#!/usr/bin/env python3
from __future__ import annotations

import argparse
import asyncio
import socket
import struct
import sys
from dataclasses import dataclass

HOST = "0.0.0.0"
PORTS = {
    "length": 9010,
    "crlf": 9011,
    "stxetx": 9012,
    "fixed": 9013,
}
MAX_FRAME = 1024 * 1024
FIXED_LENGTH = 64
STX = 0x02
ETX = 0x03
DLE = 0x10


@dataclass(frozen=True)
class Protocol:
    name: str
    port: int


async def read_length(reader: asyncio.StreamReader) -> bytes:
    header = await reader.readexactly(4)
    size = struct.unpack(">I", header)[0]
    if size < 0 or size > MAX_FRAME:
        raise ValueError(f"invalid length {size}")
    return await reader.readexactly(size)


def encode_length(payload: bytes) -> bytes:
    return struct.pack(">I", len(payload)) + payload


async def read_crlf(reader: asyncio.StreamReader) -> bytes:
    data = await reader.readuntil(b"\r\n")
    if len(data) > MAX_FRAME + 2:
        raise ValueError("CRLF frame too large")
    return data[:-2]


def encode_crlf(payload: bytes) -> bytes:
    return payload + b"\r\n"


async def read_fixed(reader: asyncio.StreamReader) -> bytes:
    return await reader.readexactly(FIXED_LENGTH)


def encode_fixed(payload: bytes) -> bytes:
    return payload[:FIXED_LENGTH].ljust(FIXED_LENGTH, b" ")


async def read_stxetx(reader: asyncio.StreamReader) -> bytes:
    first = await reader.readexactly(1)
    if first[0] != STX:
        raise ValueError("missing STX")
    out = bytearray()
    escaped = False
    while True:
        value = (await reader.readexactly(1))[0]
        if escaped:
            out.append(value ^ 0x20)
            escaped = False
        elif value == DLE:
            escaped = True
        elif value == ETX:
            return bytes(out)
        else:
            out.append(value)
        if len(out) > MAX_FRAME:
            raise ValueError("STX/ETX frame too large")


def encode_stxetx(payload: bytes) -> bytes:
    out = bytearray([STX])
    for value in payload:
        if value in (STX, ETX, DLE):
            out.extend((DLE, value ^ 0x20))
        else:
            out.append(value)
    out.append(ETX)
    return bytes(out)


async def transform(payload: bytes, writer: asyncio.StreamWriter) -> bytes | None:
    text = payload.rstrip(b" \x00").decode("utf-8", errors="replace")
    if text == "PING":
        return b"PONG"
    if text == "CLOSE":
        writer.close()
        await writer.wait_closed()
        return None
    if text == "DROP":
        await asyncio.sleep(3600)
        return None
    if text.startswith("DELAY:"):
        _, millis, body = text.split(":", 2)
        await asyncio.sleep(max(0, min(int(millis), 60000)) / 1000)
        return body.encode("utf-8")
    if text.startswith("PARTIAL:"):
        _, count, body = text.split(":", 2)
        raw = body.encode("utf-8")
        writer.write(raw[: max(0, int(count))])
        await writer.drain()
        writer.close()
        await writer.wait_closed()
        return None
    if text == "MALFORMED":
        return b"\x00\xffCPF-MALFORMED"
    return payload


async def handle(reader: asyncio.StreamReader, writer: asyncio.StreamWriter, protocol: str) -> None:
    peer = writer.get_extra_info("peername")
    try:
        while True:
            if protocol == "length":
                payload = await read_length(reader)
                encoder = encode_length
            elif protocol == "crlf":
                payload = await read_crlf(reader)
                encoder = encode_crlf
            elif protocol == "stxetx":
                payload = await read_stxetx(reader)
                encoder = encode_stxetx
            elif protocol == "fixed":
                payload = await read_fixed(reader)
                encoder = encode_fixed
            else:
                raise ValueError(protocol)
            response = await transform(payload, writer)
            if response is None or writer.is_closing():
                return
            writer.write(encoder(response))
            await writer.drain()
    except (asyncio.IncompleteReadError, ConnectionResetError, BrokenPipeError):
        pass
    except Exception as exc:
        print(f"[{protocol}] peer={peer} error={exc!r}", file=sys.stderr, flush=True)
    finally:
        if not writer.is_closing():
            writer.close()
            await writer.wait_closed()


async def serve() -> None:
    servers = []
    for name, port in PORTS.items():
        server = await asyncio.start_server(
            lambda r, w, p=name: handle(r, w, p),
            HOST,
            port,
            limit=MAX_FRAME + 16,
        )
        servers.append(server)
        print(f"CPF QA39 TCP simulator protocol={name} port={port}", flush=True)
    await asyncio.gather(*(server.serve_forever() for server in servers))


def roundtrip(name: str, payload: bytes = b"PING") -> bytes:
    port = PORTS[name]
    with socket.create_connection(("127.0.0.1", port), timeout=2) as sock:
        sock.settimeout(2)
        if name == "length":
            sock.sendall(encode_length(payload))
            size = struct.unpack(">I", sock.recv(4))[0]
            data = b""
            while len(data) < size:
                data += sock.recv(size - len(data))
            return data
        if name == "crlf":
            sock.sendall(encode_crlf(payload))
            data = b""
            while not data.endswith(b"\r\n"):
                data += sock.recv(4096)
            return data[:-2]
        if name == "stxetx":
            sock.sendall(encode_stxetx(payload))
            data = bytearray()
            while True:
                chunk = sock.recv(1)
                if not chunk:
                    break
                data.extend(chunk)
                if chunk[0] == ETX:
                    break
            raw = bytes(data)
            if not raw or raw[0] != STX or raw[-1] != ETX:
                raise RuntimeError("invalid STX/ETX response")
            return raw[1:-1]
        if name == "fixed":
            sock.sendall(encode_fixed(payload))
            data = b""
            while len(data) < FIXED_LENGTH:
                data += sock.recv(FIXED_LENGTH - len(data))
            return data.rstrip(b" ")
        raise ValueError(name)


def self_test() -> int:
    for name in PORTS:
        result = roundtrip(name)
        if result != b"PONG":
            print(f"{name}: expected PONG, got {result!r}", file=sys.stderr)
            return 1
    print("CPF QA39 TCP simulator self-test PASS")
    return 0


def main() -> int:
    parser = argparse.ArgumentParser()
    mode = parser.add_mutually_exclusive_group(required=True)
    mode.add_argument("--serve", action="store_true")
    mode.add_argument("--self-test", action="store_true")
    args = parser.parse_args()
    if args.self_test:
        return self_test()
    asyncio.run(serve())
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
