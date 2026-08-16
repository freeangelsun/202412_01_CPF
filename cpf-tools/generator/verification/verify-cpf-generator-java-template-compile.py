#!/usr/bin/env python3
"""Compile canonical Generated Domain regression outputs and assert fail-closed preflight."""
from __future__ import annotations

import importlib.util
import json
from pathlib import Path
import subprocess
import sys
import tempfile


CANONICAL_ENGINE = "cpf-tools/generator/engine/cpf_domain_generator.py"
GENERATED_JAVAC_GATE = "cpf-tools/verification/nxt3/verify_generated_javac.py"


def load_engine(engine_path: Path):
    sys.path.insert(0, str(engine_path.parent))
    spec = importlib.util.spec_from_file_location("cpf_java_compile_generator", engine_path)
    if spec is None or spec.loader is None:
        raise RuntimeError(f"canonical generator를 불러올 수 없습니다: {engine_path}")
    module = importlib.util.module_from_spec(spec)
    sys.modules[spec.name] = module
    spec.loader.exec_module(module)
    return module


def verify_unsupported_sample_preflight(root: Path, engine) -> str:
    """Sample Transaction이 non-MyBatis로 통과하는 회귀를 실제 preflight에서 차단한다."""
    with tempfile.TemporaryDirectory(prefix="cpf-generator-unsupported-preflight-") as directory:
        definition = Path(directory) / "cpf-domain.yaml"
        definition.write_text(
            """domain:
  name: unsupported-sample
  systemCode: USP
  packageName: unsupported
database:
  role: CUSTOMER_BUSINESS_DB
  tablePrefix: USP
preset: custom
modules:
  online: true
features:
  persistence: jdbc
  httpClient: false
  resilience: false
generation:
  sampleTransaction: true
""",
            encoding="utf-8",
            newline="\n",
        )
        target = root / "build/cpf-generator-verification/cpf-unsupported-sample"
        try:
            engine.preflight(root, definition, target)
        except engine.DomainError as exc:
            detail = str(exc)
            if "persistence=mybatis" not in detail:
                raise RuntimeError(f"non-MyBatis Sample preflight가 잘못된 사유로 실패했습니다: {detail}") from exc
            return detail
    raise RuntimeError("generation.sampleTransaction=true + persistence=jdbc가 preflight를 통과했습니다.")


def run_generated_javac(root: Path, gate_path: Path) -> tuple[subprocess.CompletedProcess[str], dict]:
    process = subprocess.run(
        [sys.executable, str(gate_path), "--root", str(root)],
        cwd=root,
        capture_output=True,
        text=True,
    )
    try:
        result = json.loads(process.stdout)
    except json.JSONDecodeError as exc:
        raise RuntimeError(f"NXT3 Generated Javac 결과가 JSON이 아닙니다: {process.stdout[-2000:]}") from exc
    return process, result


def main() -> int:
    root = Path(sys.argv[1] if len(sys.argv) > 1 else ".").resolve()
    engine_path = root / CANONICAL_ENGINE
    gate_path = root / GENERATED_JAVAC_GATE
    for label, path in (("canonical generator", engine_path), ("NXT3 Generated Javac gate", gate_path)):
        if not path.is_file():
            print(f"GENERATOR_JAVA_TEMPLATE_COMPILE=FAIL reason={label}_missing path={path}")
            return 1

    try:
        engine = load_engine(engine_path)
        rejected = verify_unsupported_sample_preflight(root, engine)
        process, result = run_generated_javac(root, gate_path)
    except Exception as exc:
        print(f"GENERATOR_JAVA_TEMPLATE_COMPILE=FAIL reason={exc}")
        return 1

    if process.stderr:
        print(process.stderr.rstrip(), file=sys.stderr)
    checks = result.get("checks") if isinstance(result, dict) else None
    if (
        process.returncode != 0
        or result.get("gate") != "NXT3_GENERATED_JAVAC"
        or result.get("status") != "PASS"
        or not isinstance(checks, list)
        or not checks
        or any(check.get("status") != "PASS" for check in checks)
    ):
        print(json.dumps(result, ensure_ascii=False, indent=2))
        print(
            "GENERATOR_JAVA_TEMPLATE_COMPILE=FAIL "
            f"nxt3Rc={process.returncode} nxt3Status={result.get('status')}"
        )
        return 1

    source_count = sum(int(check.get("sourceCount", 0)) for check in checks)
    print(
        "GENERATOR_UNSUPPORTED_SAMPLE_PREFLIGHT=PASS "
        f"persistence=jdbc reason={json.dumps(rejected, ensure_ascii=False)}"
    )
    print(
        "GENERATOR_JAVA_TEMPLATE_COMPILE=PASS "
        f"canonicalEngine={CANONICAL_ENGINE} regressionOutputs={len(checks)} "
        f"sources={source_count} release={result.get('javaRelease')}"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
