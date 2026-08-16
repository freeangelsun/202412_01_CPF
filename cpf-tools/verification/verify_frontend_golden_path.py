#!/usr/bin/env python3
from pathlib import Path
import argparse, hashlib, json, re, sys

ROOT = Path(__file__).resolve().parents[2]
FIXTURE_ROOT = Path(__file__).resolve().parent / "fixtures" / "frontend"


def git_blob_sha(path: Path) -> str:
    data = path.read_bytes()
    header = f"blob {len(data)}\0".encode("utf-8")
    return hashlib.sha1(header + data).hexdigest()


def read_text(path: Path) -> str:
    return path.read_text(encoding="utf-8", errors="strict")


def sha256_file(path: Path) -> str:
    return hashlib.sha256(path.read_bytes()).hexdigest()


def operation_ids_from_openapi(path: Path) -> list[str]:
    spec = json.loads(read_text(path))
    methods = {"get", "post", "put", "patch", "delete", "head", "options", "trace"}
    values: list[str] = []
    for item in (spec.get("paths") or {}).values():
        if not isinstance(item, dict):
            continue
        for method, operation in item.items():
            if str(method).lower() not in methods or not isinstance(operation, dict):
                continue
            operation_id = operation.get("operationId")
            if operation_id:
                values.append(str(operation_id))
    return sorted(values)


def verify_marker(marker: Path, frontend: Path, module: str, fail: list[str]) -> None:
    try:
        doc = json.loads(read_text(marker))
    except Exception as exc:
        fail.append(f"OPENAPI_SOURCE_MARKER_INVALID:{module}:{exc.__class__.__name__}")
        return
    if int(doc.get("schemaVersion", 0)) < 3:
        fail.append(f"OPENAPI_SOURCE_SCHEMA_TOO_OLD:{module}")
    if int(doc.get("openApiOperationCount", 0)) <= 0:
        fail.append(f"OPENAPI_OPERATION_COUNT_EMPTY:{module}")
    generated = doc.get("generatedFiles") or []
    paths = {str(item.get("path", "")) for item in generated if isinstance(item, dict)}
    if not any(p.endswith("cpf-api.ts") for p in paths):
        fail.append(f"OPENAPI_GENERATED_CLIENT_NOT_TRACKED:{module}")
    if doc.get("sanitized") is not True:
        fail.append(f"OPENAPI_SOURCE_NOT_SANITIZED:{module}")

    for path_key, hash_key, label in [
        ("openApiPath", "openApiSha256", "OPENAPI"),
        ("generatorConfigPath", "generatorConfigSha256", "GENERATOR_CONFIG"),
        ("packageLockPath", "packageLockSha256", "PACKAGE_LOCK"),
    ]:
        relative = str(doc.get(path_key, ""))
        target = frontend / relative
        expected = str(doc.get(hash_key, ""))
        if not relative or not target.is_file():
            fail.append(f"{label}_SOURCE_MISSING:{module}:{relative}")
        elif sha256_file(target) != expected:
            fail.append(f"{label}_HASH_DRIFT:{module}:{relative}")

    for item in generated:
        if not isinstance(item, dict):
            fail.append(f"GENERATED_MARKER_ENTRY_INVALID:{module}")
            continue
        relative = str(item.get("path", ""))
        target = frontend / relative
        expected = str(item.get("sha256", ""))
        if not relative or not target.is_file():
            fail.append(f"GENERATED_FILE_MISSING:{module}:{relative}")
            continue
        if sha256_file(target) != expected:
            fail.append(f"GENERATED_FILE_HASH_DRIFT:{module}:{relative}")
        if "size" in item and int(item["size"]) != target.stat().st_size:
            fail.append(f"GENERATED_FILE_SIZE_DRIFT:{module}:{relative}")

    openapi_path = frontend / str(doc.get("openApiPath", ""))
    if openapi_path.is_file():
        operation_ids = operation_ids_from_openapi(openapi_path)
        if len(operation_ids) != int(doc.get("openApiOperationCount", 0)):
            fail.append(f"OPENAPI_OPERATION_COUNT_DRIFT:{module}:{len(operation_ids)}")
        actual_ids_hash = hashlib.sha256("\n".join(operation_ids).encode("utf-8")).hexdigest()
        if actual_ids_hash != str(doc.get("openApiOperationIdsSha256", "")):
            fail.append(f"OPENAPI_OPERATION_IDS_HASH_DRIFT:{module}")
        compatibility = frontend / "src/generated/cpf-api.ts"
        contract = frontend / "src/generated/cpf-operation-contract.ts"
        orval = frontend / "src/generated/orval/cpf-api.ts"
        compatibility_text = read_text(compatibility) if compatibility.is_file() else ""
        contract_text = read_text(contract) if contract.is_file() else ""
        orval_text = read_text(orval) if orval.is_file() else ""
        for operation_id in operation_ids:
            if f"function {operation_id}<" not in compatibility_text:
                fail.append(f"COMPATIBILITY_OPERATION_MISSING:{module}:{operation_id}")
            if f'operationId: "{operation_id}"' not in contract_text:
                fail.append(f"OPERATION_CONTRACT_MISSING:{module}:{operation_id}")
            if not re.search(rf"export\s+const\s+{re.escape(operation_id)}\s*=", orval_text):
                fail.append(f"ORVAL_OPERATION_MISSING:{module}:{operation_id}")


def verify_mutator(mutator: Path, module: str, fail: list[str]) -> None:
    s = read_text(mutator)
    for status in ["401", "403", "404", "409", "422", "429", "503"]:
        if status not in s:
            fail.append(f"HTTP_STATUS_HANDLER_MISSING:{module}:{status}")
    if not re.search(r"(?:status\s*>=\s*500|>=\s*500)", s):
        fail.append(f"HTTP_5XX_HANDLER_MISSING:{module}")


def verify_index(index: Path, module: str, fail: list[str]) -> None:
    s = read_text(index)
    if re.search(r'<(?:script|link)[^>]+(?:src|href)=["\']https?://', s, re.I):
        fail.append(f"EXTERNAL_RUNTIME_CDN:{module}")


def verify_live(root: Path, module: str, fail: list[str]) -> bool:
    src = root / module / "frontend" / "src"
    if not src.is_dir():
        return False
    generated = src / "generated"
    marker = generated / ".cpf-openapi-source.json"
    index = root / module / "frontend" / "index.html"
    mutator = src / "shared" / "orval-mutator.ts"
    if not generated.is_dir():
        fail.append(f"GENERATED_CLIENT_MISSING:{module}")
    if not marker.is_file():
        fail.append(f"OPENAPI_SOURCE_MARKER_MISSING:{module}")
    else:
        verify_marker(marker, root / module / "frontend", module, fail)
    consumers = []
    features = src / "features"
    if features.is_dir():
        for p in features.rglob("*"):
            if not p.is_file() or p.suffix.lower() not in {".ts", ".vue", ".tsx"}:
                continue
            s = read_text(p)
            if re.search(r"from\s+['\"][^'\"]*generated/", s):
                consumers.append(p)
            if "fetch(" in s:
                fail.append(f"FEATURE_RAW_FETCH_FORBIDDEN:{p.relative_to(root).as_posix()}")
    if not consumers:
        fail.append(f"NO_ACTUAL_GENERATED_CONSUMER:{module}")
    if not mutator.is_file():
        fail.append(f"ORVAL_MUTATOR_MISSING:{module}")
    else:
        verify_mutator(mutator, module, fail)
    if not index.is_file():
        fail.append(f"INDEX_MISSING:{module}")
    else:
        verify_index(index, module, fail)
    return True


def verify_fixture(module: str, meta: dict, baseline_sha: str, expected_baseline: str, fail: list[str]) -> None:
    if baseline_sha != expected_baseline:
        fail.append(f"FRONTEND_EVIDENCE_BASELINE_SHA_MISMATCH:{baseline_sha}")
    fields = [
        ("featureFixture", "featureBlobSha"),
        ("mutatorFixture", "mutatorBlobSha"),
        ("indexFixture", "indexBlobSha"),
        ("markerFixture", "markerBlobSha"),
    ]
    resolved = {}
    for fixture_key, sha_key in fields:
        p = FIXTURE_ROOT / str(meta.get(fixture_key, ""))
        resolved[fixture_key] = p
        if not p.is_file():
            fail.append(f"FRONTEND_EVIDENCE_FILE_MISSING:{module}:{fixture_key}")
            continue
        actual = git_blob_sha(p)
        expected = str(meta.get(sha_key, ""))
        if actual != expected:
            fail.append(f"FRONTEND_EVIDENCE_BLOB_MISMATCH:{module}:{fixture_key}:{actual}")
    feature = resolved.get("featureFixture")
    if feature and feature.is_file():
        s = read_text(feature)
        for token in meta.get("requiredFeatureTokens", []):
            if token not in s:
                fail.append(f"FRONTEND_CONSUMER_TOKEN_MISSING:{module}:{token}")
        if "fetch(" in s:
            fail.append(f"FEATURE_RAW_FETCH_FORBIDDEN_EVIDENCE:{module}")
    mutator = resolved.get("mutatorFixture")
    if mutator and mutator.is_file():
        verify_mutator(mutator, module, fail)
    index = resolved.get("indexFixture")
    if index and index.is_file():
        verify_index(index, module, fail)
    marker = resolved.get("markerFixture")
    if marker and marker.is_file():
        verify_marker(marker, FIXTURE_ROOT, module, fail)


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--root", default=str(ROOT))
    ap.add_argument("--baseline-sha", default="b7d3cdacff3adba757d852faacf4cff5ee80cdaf")
    args = ap.parse_args()
    root = Path(args.root).resolve()
    fail: list[str] = []
    evidence_file = FIXTURE_ROOT / "evidence.json"
    if not evidence_file.is_file():
        fail.append("FRONTEND_EVIDENCE_METADATA_MISSING")
        evidence = {"baselineSha": "", "modules": {}}
    else:
        evidence = json.loads(read_text(evidence_file))
    for module in ["cpf-admin", "cpf-biz-admin"]:
        if verify_live(root, module, fail):
            continue
        meta = (evidence.get("modules") or {}).get(module)
        if not isinstance(meta, dict):
            fail.append(f"FRONTEND_MISSING_AND_NO_EVIDENCE:{module}")
            continue
        verify_fixture(module, meta, str(evidence.get("baselineSha", "")), args.baseline_sha, fail)
    fail = sorted(set(fail))
    print("CPF_FRONTEND_GOLDEN_PATH=" + ("PASS" if not fail else "FAIL"))
    print("mode=" + ("LIVE_SOURCE_HASHED" if not fail else "FAILED"))
    print("baselineSha=" + args.baseline_sha)
    print("failures=" + str(len(fail)))
    for item in fail:
        print(item)
    return 0 if not fail else 1


if __name__ == "__main__":
    raise SystemExit(main())
