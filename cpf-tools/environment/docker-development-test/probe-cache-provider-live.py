#!/usr/bin/env python3
"""Dependency-free live Redis/Valkey protocol probe used by the Docker QA fixture.

The probe deliberately asserts the native server identity.  A Valkey PASS is not
reported as a Redis PASS (or vice versa), even though both expose the RESP
operations used by CPF's cache provider abstraction.
"""

from __future__ import annotations

import sys as _cpf_sys

# CPF 표준 인코딩은 UTF-8 이다. 호출자의 콘솔 코드페이지(Windows cp949 등)에 좌우되면
# 한글 출력이 깨져 진단 메시지를 읽을 수 없다. 진입점이 스스로 출력 스트림을 고정한다.
for _cpf_stream in (_cpf_sys.stdout, _cpf_sys.stderr):
    try:
        _cpf_stream.reconfigure(encoding='utf-8')
    except (AttributeError, ValueError):
        pass

import argparse
import concurrent.futures
import hashlib
import json
import socket
import sys
import time
import uuid
from dataclasses import dataclass
from datetime import datetime, timezone
from pathlib import Path
from typing import Any


class RespError(RuntimeError):
    pass


def utc_now() -> str:
    return datetime.now(timezone.utc).isoformat().replace("+00:00", "Z")


def encode_command(*parts: str | bytes | int) -> bytes:
    encoded = [p if isinstance(p, bytes) else str(p).encode("utf-8") for p in parts]
    chunks = [f"*{len(encoded)}\r\n".encode("ascii")]
    for part in encoded:
        chunks.extend((f"${len(part)}\r\n".encode("ascii"), part, b"\r\n"))
    return b"".join(chunks)


@dataclass
class RespClient:
    host: str
    port: int
    password: str
    timeout_seconds: float = 3.0

    def __post_init__(self) -> None:
        self.sock: socket.socket | None = None
        self.reader: Any = None

    def connect(self) -> "RespClient":
        self.close()
        self.sock = socket.create_connection(
            (self.host, self.port), timeout=self.timeout_seconds
        )
        self.sock.settimeout(self.timeout_seconds)
        self.reader = self.sock.makefile("rb")
        if self.password:
            reply = self.execute("AUTH", self.password)
            if reply != "OK":
                raise RespError("authentication was not acknowledged")
        return self

    def close(self) -> None:
        if self.reader is not None:
            self.reader.close()
        if self.sock is not None:
            self.sock.close()
        self.reader = None
        self.sock = None

    def __enter__(self) -> "RespClient":
        return self.connect()

    def __exit__(self, *_: object) -> None:
        self.close()

    def execute(self, *parts: str | bytes | int) -> Any:
        if self.sock is None or self.reader is None:
            raise RespError("client is not connected")
        self.sock.sendall(encode_command(*parts))
        return self._read_reply()

    def _read_line(self) -> bytes:
        line = self.reader.readline()
        if not line.endswith(b"\r\n"):
            raise RespError("truncated RESP reply")
        return line[:-2]

    def _read_reply(self) -> Any:
        marker = self.reader.read(1)
        if marker == b"+":
            return self._read_line().decode("utf-8", errors="strict")
        if marker == b"-":
            raise RespError(self._read_line().decode("utf-8", errors="replace"))
        if marker == b":":
            return int(self._read_line())
        if marker == b"$":
            length = int(self._read_line())
            if length == -1:
                return None
            payload = self.reader.read(length)
            if len(payload) != length or self.reader.read(2) != b"\r\n":
                raise RespError("truncated RESP bulk reply")
            return payload
        if marker == b"*":
            count = int(self._read_line())
            return None if count == -1 else [self._read_reply() for _ in range(count)]
        raise RespError(f"unsupported RESP reply marker: {marker!r}")


def decode_text(value: Any) -> str:
    if isinstance(value, bytes):
        return value.decode("utf-8", errors="strict")
    if isinstance(value, str):
        return value
    raise RespError(f"expected text reply, received {type(value).__name__}")


def detect_provider(info_server: str) -> str:
    fields: dict[str, str] = {}
    for line in info_server.splitlines():
        if ":" in line and not line.startswith("#"):
            key, value = line.split(":", 1)
            fields[key.strip().lower()] = value.strip().lower()
    if "valkey_version" in fields or fields.get("server_name") == "valkey":
        return "valkey"
    if "redis_version" in fields:
        return "redis"
    return "unknown"


def assertion(checks: list[dict[str, Any]], name: str, passed: bool, detail: str) -> None:
    checks.append({"name": name, "passed": bool(passed), "detail": detail})
    if not passed:
        raise AssertionError(f"{name}: {detail}")


def check_identity(client: RespClient, provider: str, checks: list[dict[str, Any]]) -> str:
    info = decode_text(client.execute("INFO", "SERVER"))
    detected = detect_provider(info)
    assertion(
        checks,
        "native-provider-identity",
        detected == provider,
        f"expected={provider}; detected={detected}",
    )
    return detected


def run_full(
    host: str, port: int, password: str, provider: str, namespace: str
) -> tuple[list[dict[str, Any]], dict[str, Any]]:
    checks: list[dict[str, Any]] = []
    details: dict[str, Any] = {}
    prefix = f"{namespace}:{provider}"
    with RespClient(host, port, password) as first, RespClient(
        host, port, password
    ) as second:
        detected = check_identity(first, provider, checks)
        details["detectedProvider"] = detected
        assertion(checks, "ping", first.execute("PING") == "PONG", "PONG received")

        put_key = f"{prefix}:put"
        assertion(
            checks,
            "put",
            first.execute("SET", put_key, "cpf-live-value", "EX", 30) == "OK",
            "SET acknowledged",
        )
        assertion(
            checks,
            "get",
            first.execute("GET", put_key) == b"cpf-live-value",
            "exact bytes returned",
        )

        load_key = f"{prefix}:get-or-load"
        first.execute("DEL", load_key)
        loader_calls = 0
        loaded = first.execute("GET", load_key)
        if loaded is None:
            loader_calls += 1
            first.execute("SET", load_key, "loaded-once", "NX", "EX", 30)
            loaded = first.execute("GET", load_key)
        assertion(
            checks,
            "get-or-load",
            loaded == b"loaded-once" and loader_calls == 1,
            f"loaderCalls={loader_calls}",
        )

        assertion(
            checks,
            "evict",
            first.execute("DEL", put_key) == 1 and first.execute("GET", put_key) is None,
            "DEL removed the exact key",
        )

        ttl_key = f"{prefix}:ttl"
        first.execute("SET", ttl_key, "short-lived", "PX", 900)
        pttl = first.execute("PTTL", ttl_key)
        assertion(
            checks,
            "ttl-active",
            isinstance(pttl, int) and 0 < pttl <= 900,
            f"pttlMs={pttl}",
        )
        time.sleep(1.1)
        assertion(
            checks,
            "ttl-stale-rejected",
            first.execute("GET", ttl_key) is None and first.execute("PTTL", ttl_key) == -2,
            "expired value is not served stale",
        )

        shared_key = f"{prefix}:multi-instance"
        first.execute("SET", shared_key, "version-1", "EX", 30)
        observed_v1 = second.execute("GET", shared_key)
        first.execute("SET", shared_key, "version-2", "EX", 30)
        observed_v2 = second.execute("GET", shared_key)
        assertion(
            checks,
            "multi-instance-refresh",
            observed_v1 == b"version-1" and observed_v2 == b"version-2",
            "second connection observed both committed versions",
        )
        first.execute("DEL", shared_key)
        assertion(
            checks,
            "invalidation",
            second.execute("GET", shared_key) is None,
            "second connection observed exact-key invalidation",
        )

        serialization_key = f"{prefix}:serialization"
        document = {
            "version": 7,
            "contentType": "application/json",
            "payload": "CPF-캐시-✓",
        }
        serialized = json.dumps(
            document, ensure_ascii=False, separators=(",", ":"), sort_keys=True
        ).encode("utf-8")
        first.execute("SET", serialization_key, serialized, "EX", 30)
        returned = first.execute("GET", serialization_key)
        assertion(
            checks,
            "serialization",
            returned == serialized and json.loads(returned.decode("utf-8")) == document,
            f"sha256={hashlib.sha256(serialized).hexdigest()}",
        )

    stampede_key = f"{prefix}:stampede-lock"

    def contender(number: int) -> tuple[int, bool]:
        with RespClient(host, port, password) as client:
            result = client.execute("SET", stampede_key, f"owner-{number}", "NX", "PX", 5000)
            return number, result == "OK"

    with concurrent.futures.ThreadPoolExecutor(max_workers=8) as pool:
        winners = [number for number, won in pool.map(contender, range(8)) if won]
    assertion(
        checks,
        "stampede-single-loader",
        len(winners) == 1,
        f"winnerCount={len(winners)}",
    )

    reconnect_key = f"{prefix}:reconnect"
    with RespClient(host, port, password) as client:
        client.execute("DEL", stampede_key)
        client.execute("SET", reconnect_key, "survives-service-restart")
        for suffix in ("get-or-load", "serialization"):
            client.execute("DEL", f"{prefix}:{suffix}")
    details["reconnectKey"] = reconnect_key
    return checks, details


def run_recovery(
    host: str, port: int, password: str, provider: str, namespace: str
) -> tuple[list[dict[str, Any]], dict[str, Any]]:
    checks: list[dict[str, Any]] = []
    key = f"{namespace}:{provider}:reconnect"
    with RespClient(host, port, password) as client:
        detected = check_identity(client, provider, checks)
        pre_interruption_value = client.execute("GET", key)
        client.execute("SET", key, "reloaded-after-restart", "EX", 30)
        assertion(
            checks,
            "reconnect-and-recovery",
            client.execute("PING") == "PONG"
            and pre_interruption_value is None
            and client.execute("GET", key) == b"reloaded-after-restart",
            "new connection recovered, rejected stale in-memory state, and reloaded the value",
        )
        client.execute("DEL", key)
    return checks, {
        "detectedProvider": detected,
        "reconnectKey": key,
        "coldRestartLostEphemeralValue": pre_interruption_value is None,
    }


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser()
    parser.add_argument("--provider", choices=("redis", "valkey"), required=True)
    parser.add_argument("--host", default="127.0.0.1")
    parser.add_argument("--port", type=int, required=True)
    parser.add_argument("--password-file", type=Path, required=True)
    parser.add_argument("--namespace", required=True)
    parser.add_argument("--mode", choices=("full", "identity", "recovery"), default="full")
    parser.add_argument("--evidence-output", type=Path, required=True)
    return parser


def main(argv: list[str] | None = None) -> int:
    args = build_parser().parse_args(argv)
    started_at = utc_now()
    checks: list[dict[str, Any]] = []
    details: dict[str, Any] = {}
    status = "FAIL"
    error_type = ""
    error_detail = ""
    try:
        password = args.password_file.read_text(encoding="utf-8").strip()
        if not password:
            raise ValueError("password file is empty")
        if args.mode == "full":
            checks, details = run_full(
                args.host, args.port, password, args.provider, args.namespace
            )
        elif args.mode == "recovery":
            checks, details = run_recovery(
                args.host, args.port, password, args.provider, args.namespace
            )
        else:
            with RespClient(args.host, args.port, password) as client:
                detected = check_identity(client, args.provider, checks)
                details["detectedProvider"] = detected
        status = "PASS"
    except Exception as exc:  # evidence must survive expected negative probes
        error_type = type(exc).__name__
        error_detail = str(exc)[:1000]
    result = {
        "schemaVersion": 1,
        "provider": args.provider,
        "mode": args.mode,
        "status": status,
        "startedAt": started_at,
        "endedAt": utc_now(),
        "endpoint": {"host": args.host, "port": args.port},
        "namespace": args.namespace,
        "checks": checks,
        "details": details,
        "error": {"type": error_type, "detail": error_detail} if error_type else None,
    }
    args.evidence_output.parent.mkdir(parents=True, exist_ok=True)
    args.evidence_output.write_text(
        json.dumps(result, ensure_ascii=False, indent=2) + "\n", encoding="utf-8"
    )
    print(
        f"CPF_CACHE_PROVIDER_PROBE={status} provider={args.provider} mode={args.mode} "
        f"checks={len(checks)}"
    )
    if error_type:
        print(f"ERROR_TYPE={error_type}", file=sys.stderr)
        print(f"ERROR_DETAIL={error_detail}", file=sys.stderr)
    return 0 if status == "PASS" else 1


if __name__ == "__main__":
    raise SystemExit(main())
