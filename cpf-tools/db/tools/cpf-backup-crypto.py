#!/usr/bin/env python3
"""CPF backup artifact encryption helper.

Format is a deterministic contract with random nonces per chunk:
  magic(8) + header_length(4, big-endian) + header_json + records...
Each record is record_length(4) + chunk_index(8) + nonce(12) + AES-GCM ciphertext+tag.
Secrets are read only from the named environment variable and are never printed.
"""
from __future__ import annotations

import argparse
import base64
import hashlib
import json
import os
import struct
import sys
import uuid
from pathlib import Path

try:
    from cryptography.hazmat.primitives.ciphers.aead import AESGCM
except Exception as exc:  # pragma: no cover - environment-dependent
    raise SystemExit("cryptography package is required for CPF backup encryption") from exc

MAGIC = b"CPFBAK01"
HEADER_LEN = struct.Struct(">I")
RECORD_LEN = struct.Struct(">I")
INDEX = struct.Struct(">Q")
NONCE_SIZE = 12
DEFAULT_CHUNK_SIZE = 4 * 1024 * 1024


def _sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for block in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(block)
    return digest.hexdigest()


def _load_key(env_name: str) -> bytes:
    if not env_name or any(ord(ch) < 33 or ord(ch) > 126 for ch in env_name):
        raise ValueError("key environment variable name is invalid")
    raw = os.environ.get(env_name, "")
    if not raw:
        raise ValueError(f"required encryption key environment variable is missing: {env_name}")
    try:
        key = base64.b64decode(raw, validate=True)
    except Exception as exc:
        raise ValueError("encryption key must be valid base64") from exc
    if len(key) != 32:
        raise ValueError("encryption key must decode to exactly 32 bytes")
    return key


def _safe_output_path(path: Path, overwrite: bool) -> None:
    if path.exists() and not overwrite:
        raise FileExistsError(f"output already exists: {path}")
    path.parent.mkdir(parents=True, exist_ok=True)


def encrypt(input_path: Path, output_path: Path, key_env: str, chunk_size: int, overwrite: bool) -> dict:
    if not input_path.is_file():
        raise FileNotFoundError(f"input file does not exist: {input_path}")
    if input_path.resolve() == output_path.resolve():
        raise ValueError("input and output paths must differ")
    if chunk_size < 64 * 1024 or chunk_size > 64 * 1024 * 1024:
        raise ValueError("chunk size must be between 64 KiB and 64 MiB")
    _safe_output_path(output_path, overwrite)
    key = _load_key(key_env)
    source_size = input_path.stat().st_size
    source_hash = _sha256(input_path)
    header = {
        "schemaVersion": 1,
        "algorithm": "AES-256-GCM-CHUNKED",
        "chunkSize": chunk_size,
        "sourceSize": source_size,
        "sourceSha256": source_hash,
    }
    header_bytes = json.dumps(header, sort_keys=True, separators=(",", ":")).encode("utf-8")
    aes = AESGCM(key)
    temp_path = output_path.with_name(output_path.name + f".tmp-{os.getpid()}-{uuid.uuid4().hex}")
    try:
        with input_path.open("rb") as source, temp_path.open("xb") as target:
            if os.name == "posix":
                os.chmod(temp_path, 0o600)
            target.write(MAGIC)
            target.write(HEADER_LEN.pack(len(header_bytes)))
            target.write(header_bytes)
            index = 0
            while True:
                plaintext = source.read(chunk_size)
                if not plaintext:
                    break
                index_bytes = INDEX.pack(index)
                nonce = os.urandom(NONCE_SIZE)
                aad = MAGIC + header_bytes + index_bytes
                ciphertext = aes.encrypt(nonce, plaintext, aad)
                record = index_bytes + nonce + ciphertext
                target.write(RECORD_LEN.pack(len(record)))
                target.write(record)
                index += 1
            target.flush()
            os.fsync(target.fileno())
        os.replace(temp_path, output_path)
        if os.name == "posix":
            os.chmod(output_path, 0o600)
    except Exception:
        temp_path.unlink(missing_ok=True)
        raise
    return {
        **header,
        "artifactSha256": _sha256(output_path),
        "artifactSize": output_path.stat().st_size,
        "chunkCount": (source_size + chunk_size - 1) // chunk_size,
    }


def _read_header(stream) -> tuple[dict, bytes]:
    if stream.read(len(MAGIC)) != MAGIC:
        raise ValueError("invalid CPF backup magic")
    raw_len = stream.read(HEADER_LEN.size)
    if len(raw_len) != HEADER_LEN.size:
        raise ValueError("truncated CPF backup header length")
    header_length = HEADER_LEN.unpack(raw_len)[0]
    if header_length <= 0 or header_length > 64 * 1024:
        raise ValueError("invalid CPF backup header length")
    header_bytes = stream.read(header_length)
    if len(header_bytes) != header_length:
        raise ValueError("truncated CPF backup header")
    header = json.loads(header_bytes.decode("utf-8"))
    required = {"schemaVersion", "algorithm", "chunkSize", "sourceSize", "sourceSha256"}
    if not required.issubset(header):
        raise ValueError("CPF backup header is incomplete")
    if header["schemaVersion"] != 1 or header["algorithm"] != "AES-256-GCM-CHUNKED":
        raise ValueError("unsupported CPF backup format")
    return header, header_bytes


def decrypt(input_path: Path, output_path: Path, key_env: str, overwrite: bool) -> dict:
    if not input_path.is_file():
        raise FileNotFoundError(f"input file does not exist: {input_path}")
    if input_path.resolve() == output_path.resolve():
        raise ValueError("input and output paths must differ")
    _safe_output_path(output_path, overwrite)
    key = _load_key(key_env)
    aes = AESGCM(key)
    temp_path = output_path.with_name(output_path.name + f".tmp-{os.getpid()}-{uuid.uuid4().hex}")
    digest = hashlib.sha256()
    written = 0
    chunks = 0
    try:
        with input_path.open("rb") as source:
            header, header_bytes = _read_header(source)
            with temp_path.open("xb") as target:
                if os.name == "posix":
                    os.chmod(temp_path, 0o600)
                expected_index = 0
                while True:
                    raw_len = source.read(RECORD_LEN.size)
                    if not raw_len:
                        break
                    if len(raw_len) != RECORD_LEN.size:
                        raise ValueError("truncated CPF backup record length")
                    record_length = RECORD_LEN.unpack(raw_len)[0]
                    min_length = INDEX.size + NONCE_SIZE + 16
                    max_length = INDEX.size + NONCE_SIZE + int(header["chunkSize"]) + 16
                    if record_length < min_length or record_length > max_length:
                        raise ValueError("invalid CPF backup record length")
                    record = source.read(record_length)
                    if len(record) != record_length:
                        raise ValueError("truncated CPF backup record")
                    index_bytes = record[: INDEX.size]
                    index = INDEX.unpack(index_bytes)[0]
                    if index != expected_index:
                        raise ValueError("CPF backup chunk sequence is invalid")
                    nonce = record[INDEX.size : INDEX.size + NONCE_SIZE]
                    ciphertext = record[INDEX.size + NONCE_SIZE :]
                    aad = MAGIC + header_bytes + index_bytes
                    plaintext = aes.decrypt(nonce, ciphertext, aad)
                    target.write(plaintext)
                    digest.update(plaintext)
                    written += len(plaintext)
                    chunks += 1
                    expected_index += 1
                target.flush()
                os.fsync(target.fileno())
        if written != int(header["sourceSize"]):
            raise ValueError("decrypted CPF backup size mismatch")
        if digest.hexdigest() != str(header["sourceSha256"]).lower():
            raise ValueError("decrypted CPF backup SHA-256 mismatch")
        os.replace(temp_path, output_path)
        if os.name == "posix":
            os.chmod(output_path, 0o600)
    except Exception:
        temp_path.unlink(missing_ok=True)
        raise
    return {
        **header,
        "artifactSha256": _sha256(input_path),
        "artifactSize": input_path.stat().st_size,
        "chunkCount": chunks,
        "decryptedSha256": digest.hexdigest(),
        "decryptedSize": written,
    }


def inspect(input_path: Path) -> dict:
    if not input_path.is_file():
        raise FileNotFoundError(f"input file does not exist: {input_path}")
    with input_path.open("rb") as stream:
        header, _ = _read_header(stream)
    return {
        **header,
        "artifactSha256": _sha256(input_path),
        "artifactSize": input_path.stat().st_size,
    }


def main() -> int:
    parser = argparse.ArgumentParser(description="CPF encrypted backup artifact helper")
    sub = parser.add_subparsers(dest="command", required=True)
    enc = sub.add_parser("encrypt")
    enc.add_argument("--input", required=True)
    enc.add_argument("--output", required=True)
    enc.add_argument("--key-env", required=True)
    enc.add_argument("--chunk-size", type=int, default=DEFAULT_CHUNK_SIZE)
    enc.add_argument("--overwrite", action="store_true")
    dec = sub.add_parser("decrypt")
    dec.add_argument("--input", required=True)
    dec.add_argument("--output", required=True)
    dec.add_argument("--key-env", required=True)
    dec.add_argument("--overwrite", action="store_true")
    ins = sub.add_parser("inspect")
    ins.add_argument("--input", required=True)
    args = parser.parse_args()
    try:
        if args.command == "encrypt":
            result = encrypt(Path(args.input), Path(args.output), args.key_env, args.chunk_size, args.overwrite)
        elif args.command == "decrypt":
            result = decrypt(Path(args.input), Path(args.output), args.key_env, args.overwrite)
        else:
            result = inspect(Path(args.input))
        print(json.dumps(result, sort_keys=True, separators=(",", ":")))
        return 0
    except Exception as exc:
        print(f"CPF_BACKUP_CRYPTO_ERROR: {exc}", file=sys.stderr)
        return 2


if __name__ == "__main__":
    raise SystemExit(main())
