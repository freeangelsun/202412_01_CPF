#!/usr/bin/env python3
"""Canonical Python Generator와 DB3 멱등 Template의 정합성을 검증한다."""
from __future__ import annotations

import importlib.util
import re
import sys
import tempfile
import xml.etree.ElementTree as ET
from pathlib import Path


root = Path(sys.argv[1] if len(sys.argv) > 1 else ".").resolve()
engine_path = root / "cpf-tools/generator/engine/cpf_domain_generator.py"
errors: list[str] = []


def load_engine():
    sys.path.insert(0, str(engine_path.parent))
    spec = importlib.util.spec_from_file_location("cpf_idempotency_generator", engine_path)
    if spec is None or spec.loader is None:
        raise RuntimeError(f"canonical generator를 불러올 수 없습니다: {engine_path}")
    module = importlib.util.module_from_spec(spec)
    sys.modules[spec.name] = module
    spec.loader.exec_module(module)
    return module


engine = load_engine()
with tempfile.TemporaryDirectory(prefix="cpf-generator-idempotency-") as directory:
    definition = Path(directory) / "cpf-domain.yaml"
    definition.write_text(
        """domain:
  name: idempotency
  systemCode: IDM
  packageName: idempotency
database:
  role: CUSTOMER_BUSINESS_DB
  tablePrefix: IDM
preset: standard-enterprise
modules:
  online: true
features:
  persistence: mybatis
  httpClient: true
  resilience: true
  cache: none
  messaging: none
generation:
  sampleTransaction: true
""",
        encoding="utf-8",
        newline="\n",
    )
    domain = engine.validate_definition(engine.load_yaml_subset(definition))
    generated, _ = engine.render_files(root, domain, engine.load_catalog(root))

generated_text = "\n".join(generated.values())
required_generator_tokens = (
    "record SampleIdempotencyRecord",
    "findIdempotency",
    "insertIdempotency",
    'requestHash("DELETE"',
    'replay(idem,"DELETE",hash,tx)',
    "logicalDeleteWithVersion",
    "같은 idempotencyKey가 다른 요청에 사용되었습니다.",
    'MessageDigest.getInstance("SHA-256")',
)
for token in required_generator_tokens:
    if token not in generated_text:
        errors.append(f"generated source missing: {token}")

for legacy in ("findByIdempotencyKey", "UUID.randomUUID"):
    if legacy in generated_text:
        errors.append(f"legacy generated idempotency contract remains: {legacy}")

for vendor in engine.SUPPORTED_VENDORS:
    data_mapper = (
        root
        / "cpf-starters/data/persistence/src/main/resources/cpf-generated-domain-dialect"
        / vendor
        / "mybatis/__MAPPER__.xml.template"
    )
    if not data_mapper.is_file():
        errors.append(f"{vendor} mapper template missing")
        continue
    xml_text = data_mapper.read_text(encoding="utf-8-sig")
    if "@CPF_SCHEMA_NAME@" in xml_text:
        errors.append(f"{vendor} mapper still requires generated-project schema metadata")
    xml_without_doctype = re.sub(r"<!DOCTYPE[^>]+>", "", xml_text, count=1)
    try:
        tree = ET.fromstring(xml_without_doctype)
    except ET.ParseError as exc:
        errors.append(f"{vendor} mapper XML: {exc}")
        continue
    ids = {node.attrib.get("id") for node in tree if node.attrib.get("id")}
    for statement in (
        "findById", "findIdempotency", "insertIdempotency", "insert",
        "updateWithVersion", "logicalDeleteWithVersion", "search", "count", "cursorSlice",
    ):
        if statement not in ids:
            errors.append(f"{vendor} mapper statement missing: {statement}")
    for token in ("@CPF_IDEMPOTENCY_RESULT_TYPE@", "statusCode", "request_hash"):
        if token not in xml_text:
            errors.append(f"{vendor} mapper token missing: {token}")

    resources = {
        "install": engine._ddl(root, domain, vendor),
        "migration": engine._migration(root, domain, vendor),
        "seed": engine._seed(root, domain, vendor),
        "rollback": engine._rollback(root, domain, vendor),
        "verify": engine._verify_sql(root, domain, vendor),
    }
    for role, content in resources.items():
        if "IDM_sample_item_idem" not in content and role != "seed":
            errors.append(f"{vendor} {role} does not manage idempotency ledger")
        if role in {"install", "migration", "verify"} and "request_hash" not in content:
            errors.append(f"{vendor} {role} request_hash missing")

if errors:
    for error in errors:
        print(f"[FAIL] {error}")
    print(f"GENERATOR_IDEMPOTENCY_TEMPLATE=FAIL errors={len(errors)}")
    raise SystemExit(1)

print("[PASS] Canonical Generator CRUD/idempotency replay/conflict contract")
print("[PASS] Data-owned selected-Vendor Mapper and canonical DB3 lifecycle parity")
print("GENERATOR_IDEMPOTENCY_TEMPLATE=PASS vendors=3 metadata=NONE")
