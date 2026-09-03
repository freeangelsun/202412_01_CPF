#!/usr/bin/env python3
"""One-shot loopback proxy that delays a completed upstream HTTP response.

The Batch runtime qualification uses this only to create a real response-loss
window: the Domain receives and completes its normal request, while the
Worker that owns the database claim is terminated before it can observe that
response.  This is deliberately a verifier-side network fault, not a product
handler or a database-row fabrication.
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
import socket
import sys
import time


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--listen-port", type=int, required=True)
    parser.add_argument("--listen-host", default="0.0.0.0")
    parser.add_argument("--upstream-host", default="127.0.0.1")
    parser.add_argument("--upstream-port", type=int, required=True)
    parser.add_argument("--delay-seconds", type=float, required=True)
    parser.add_argument("--accept-timeout-seconds", type=float, default=60.0)
    parser.add_argument("--client-read-timeout-seconds", type=float, default=15.0)
    parser.add_argument("--upstream-connect-timeout-seconds", type=float, default=15.0)
    parser.add_argument("--upstream-read-timeout-seconds", type=float, default=30.0)
    return parser.parse_args()


def receive_http_request(client: socket.socket, timeout_seconds: float) -> bytes:
    """Receive one content-length HTTP request without inspecting its payload."""
    received = bytearray()
    header_end = -1
    expected_length: int | None = None
    client.settimeout(timeout_seconds)
    while True:
        chunk = client.recv(64 * 1024)
        if not chunk:
            raise RuntimeError("client closed before request was complete")
        received.extend(chunk)
        if header_end < 0:
            header_end = received.find(b"\r\n\r\n")
            if header_end >= 0:
                header = bytes(received[:header_end]).decode("iso-8859-1")
                for line in header.split("\r\n")[1:]:
                    if line.lower().startswith("content-length:"):
                        expected_length = int(line.partition(":")[2].strip())
                        break
                if expected_length is None:
                    raise RuntimeError("response-loss proxy requires Content-Length request")
        if header_end >= 0 and len(received) >= header_end + 4 + expected_length:
            return bytes(received)


def forward_delayed_response(args: argparse.Namespace) -> None:
    for option_name in (
            "delay_seconds",
            "accept_timeout_seconds",
            "client_read_timeout_seconds",
            "upstream_connect_timeout_seconds",
            "upstream_read_timeout_seconds"):
        if getattr(args, option_name) <= 0:
            raise ValueError(option_name.replace("_", "-") + " must be positive")
    listener = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    listener.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    # CPF Network Policy 는 loopback 대상 호출을 무조건 차단한다(SSRF 가드).
    # 검증도 비-loopback 주소로 프록시에 도달해야 하므로 이 검증 전용 포트만 모든 인터페이스에
    # 바인딩한다. 정책을 약화시키지 않고 topology 만 정책에 맞춘다.
    listener.bind((args.listen_host, args.listen_port))
    listener.listen(1)
    listener.settimeout(args.accept_timeout_seconds)
    print("READY", flush=True)
    try:
        client, _ = listener.accept()
        with client:
            request = receive_http_request(client, args.client_read_timeout_seconds)
            with socket.create_connection(
                    (args.upstream_host, args.upstream_port),
                    timeout=args.upstream_connect_timeout_seconds) as upstream:
                upstream.settimeout(args.upstream_read_timeout_seconds)
                upstream.sendall(request)
                first_response = upstream.recv(64 * 1024)
                if not first_response:
                    raise RuntimeError("upstream closed without an HTTP response")
                # The first response byte proves the real Domain has completed its side of the
                # call.  Do not log headers or payload: transaction content can be sensitive.
                print("UPSTREAM_RESPONSE_DELAY_STARTED", flush=True)
                time.sleep(args.delay_seconds)
                try:
                    client.sendall(first_response)
                    while True:
                        chunk = upstream.recv(64 * 1024)
                        if not chunk:
                            break
                        client.sendall(chunk)
                except (BrokenPipeError, ConnectionResetError, socket.timeout):
                    # The verifier intentionally kills the Worker during this response-loss window.
                    print("CLIENT_RESPONSE_LOSS_OBSERVED", flush=True)
    finally:
        listener.close()


def main() -> int:
    try:
        forward_delayed_response(parse_args())
        return 0
    except Exception as failure:  # pragma: no cover - surfaced to the harness process log
        print(f"ERROR {type(failure).__name__}: {failure}", file=sys.stderr, flush=True)
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
