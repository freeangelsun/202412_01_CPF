#!/usr/bin/env python3
"""CPF Generated Domain 상용 lifecycle 회귀 Gate.

제품 Source를 변경하지 않고 ``cpf-docs/work/evidence/generated/domain-generator/verification`` 아래의 격리 출력만 사용한다.
입력 preflight, DB3/Public Starter 경계, idempotency, upgrade, remove/restore, 사용자 수정 보호와
한글/공백 경로를 실제 ``cpf`` CLI로 호출해 검증한다.
"""
from __future__ import annotations

import argparse
import hashlib
import json
import os
import shutil
import subprocess
import sys
from pathlib import Path


def sha256(path: Path) -> str:
    """파일 변경 보호 확인에 사용하는 SHA-256을 반환한다."""
    return hashlib.sha256(path.read_bytes()).hexdigest()


def run(command: list[str], *, expect: int = 0) -> subprocess.CompletedProcess[str]:
    """명령을 실행하고 기대 종료 코드와 다르면 stdout/stderr를 포함해 즉시 실패시킨다."""
    process_env={**os.environ,"PYTHONUTF8":"1","PYTHONIOENCODING":"utf-8"}
    result = subprocess.run(command, text=True, encoding="utf-8", errors="replace", capture_output=True, check=False, env=process_env)
    if result.returncode != expect:
        raise RuntimeError(
            "명령 종료 코드가 예상과 다릅니다.\n"
            f"command={command}\nexpected={expect}\nactual={result.returncode}\n"
            f"stdout={result.stdout}\nstderr={result.stderr}"
        )
    return result


def yaml_text(name: str, code: str, package: str, prefix: str, *, security: str = "resource-server") -> str:
    """Domain-neutral 임의 Domain 검증 입력을 생성한다. Secret 실제 값은 포함하지 않는다."""
    return f"""domain:
  name: {name}
  systemCode: {code}
  packageName: {package}
database:
  role: CUSTOMER_BUSINESS_DB
  tablePrefix: {prefix}
preset: standard-enterprise
modules:
  online: true
  batch: true
generation:
  sampleTransaction: true
features:
  persistence: mybatis
  httpClient: true
  resilience: true
  cache: none
  messaging: none
  objectStorage: none
  securityProfile: {security}
"""


def main() -> int:
    parser = argparse.ArgumentParser(description="CPF Generated Domain lifecycle regression gate")
    parser.add_argument("--root", default=".", help="CPF repository root")
    args = parser.parse_args()
    root = Path(args.root).resolve()
    cli = root / "cpf-tools/runtime/cli/cpf.py"
    if not cli.is_file():
        raise RuntimeError(f"CPF CLI가 없습니다: {cli}")

    # 한글과 공백이 있는 경로에서 동일 Core CLI가 정상 동작하는지도 같이 검증한다.
    work = root / "cpf-docs/work/evidence/generated/domain-generator/verification" / "NXT3 lifecycle 한글 space"
    shutil.rmtree(work, ignore_errors=True)
    work.mkdir(parents=True, exist_ok=True)
    definition = work / "order-cpf-domain.yaml"
    definition.write_text(yaml_text("order", "ORD", "order", "ORD", security="oidc"), encoding="utf-8")
    output = work / "cpf-order"
    base = [sys.executable, str(cli), "--root", str(root)]

    evidence: dict[str, object] = {"status": "FAIL", "work": str(work), "checks": {}}
    try:
        # 1) write-before preflight와 OIDC Public Starter 계약.
        preflight = run(base + ["domain", "validate", "--file", str(definition), "--output", str(output)])
        evidence["checks"]["preflight"] = json.loads(preflight.stdout)
        if output.exists():
            raise RuntimeError("preflight가 Product/Generated Source를 생성했습니다.")

        generated = run(base + ["domain", "generate", "--file", str(definition), "--output", str(output)])
        generated_json = json.loads(generated.stdout)
        online_build = output / "online/build.gradle"
        if "cpf-starter-oidc" not in online_build.read_text(encoding="utf-8"):
            raise RuntimeError("OIDC 선택이 Public cpf-starter-oidc로 조립되지 않았습니다.")
        build_text = online_build.read_text(encoding="utf-8")
        if "cpf-starter-integration-http" not in build_text:
            raise RuntimeError("httpClient=true인데 Public integration-http Provider가 조립되지 않았습니다.")
        if "cpf-starter-integration-resilience" not in build_text:
            raise RuntimeError("resilience=true인데 Public resilience Provider가 조립되지 않았습니다.")
        evidence["checks"]["freshGenerate"] = generated_json

        # 2) 같은 입력 재실행은 byte-stable idempotent여야 한다.
        before = sha256(online_build)
        rerun = run(base + ["domain", "generate", "--file", str(definition), "--output", str(output)])
        if sha256(online_build) != before:
            raise RuntimeError("Idempotent rerun에서 Generated Source hash가 변경됐습니다.")
        evidence["checks"]["idempotent"] = json.loads(rerun.stdout)

        # 3) 사용자 수정 Generated 파일은 upgrade가 단 한 파일도 덮기 전에 차단해야 한다.
        online_build.write_text(online_build.read_text(encoding="utf-8") + "// 사용자 수정 보호 검증\n", encoding="utf-8", newline="\n")
        modified_hash = sha256(online_build)
        failed_upgrade = run(
            base + ["domain", "upgrade", "order", "--file", str(definition), "--output", str(output)],
            expect=2,
        )
        if sha256(online_build) != modified_hash:
            raise RuntimeError("실패한 upgrade가 사용자 수정 파일을 변경했습니다.")
        evidence["checks"]["userOwnedProtection"] = {
            "status": "PASS",
            "stderr": failed_upgrade.stderr.strip(),
        }

        # 보호 검증용 수정만 되돌리고 동일 state에서 remove→restore byte parity를 확인한다.
        online_build.write_text(online_build.read_text(encoding="utf-8").replace("// 사용자 수정 보호 검증\n", ""), encoding="utf-8", newline="\n")
        tree_before = {p.relative_to(output).as_posix(): sha256(p) for p in output.rglob("*") if p.is_file()}
        planned = run(base + ["domain", "remove", "order", "--file", str(definition), "--output", str(output)])
        plan_json = json.loads(planned.stdout)
        if plan_json.get("status") != "PLANNED_DELETE_MANIFEST" or plan_json.get("applied") is not False:
            raise RuntimeError("remove가 사용자 승인 Delete Manifest 계획으로 종료되지 않았습니다.")
        direct_apply = run(base + ["domain", "remove", "order", "--file", str(definition), "--output", str(output), "--apply"], expect=2)
        if not output.exists():
            raise RuntimeError("금지된 direct --apply가 Generated Root를 삭제했습니다.")
        # Temp fixture에서는 사용자 승인 Delete Manifest 적용 결과를 exact candidates로 모사해 restore parity를 검증합니다.
        for rel in plan_json.get("deleteCandidates", []):
            candidate = output / rel
            if candidate.is_file(): candidate.unlink()
        for directory in sorted((x for x in output.rglob("*") if x.is_dir()), key=lambda x: len(x.parts), reverse=True):
            try: directory.rmdir()
            except OSError: pass
        restored = run(base + ["domain", "restore", "--file", str(definition), "--output", str(output)])
        tree_after = {p.relative_to(output).as_posix(): sha256(p) for p in output.rglob("*") if p.is_file()}
        if tree_before != tree_after:
            raise RuntimeError("Delete Manifest 모사 remove→restore Source hash parity가 깨졌습니다.")
        evidence["checks"]["removePlan"] = plan_json
        evidence["checks"]["directApplyRejected"] = {"status":"PASS","stderr":direct_apply.stderr.strip()}
        evidence["checks"]["restore"] = json.loads(restored.stdout)

        # 4) 정의 변경 upgrade는 user-owned extra 파일을 보존하면서 Generated-owned 변경만 반영한다.
        user_file = output / "USER_NOTE.txt"
        user_file.write_text("고객 소유 파일\n", encoding="utf-8")
        changed_definition = yaml_text("order", "ORD", "order", "ORD", security="resource-server")
        definition.write_text(changed_definition, encoding="utf-8")
        upgraded = run(base + ["domain", "upgrade", "order", "--file", str(definition), "--output", str(output)])
        if not user_file.is_file():
            raise RuntimeError("upgrade가 user-owned extra file을 삭제했습니다.")
        if "cpf-starter-oidc" in online_build.read_text(encoding="utf-8"):
            raise RuntimeError("upgrade 후 이전 OIDC dependency가 남았습니다.")
        evidence["checks"]["upgrade"] = json.loads(upgraded.stdout)

        # 5) plaintext credential input은 output 생성 전에 거부해야 한다.
        invalid = work / "invalid-secret.yaml"
        invalid.write_text(
            yaml_text("loan", "LON", "loan", "LON") + "  password: plaintext-forbidden\n",
            encoding="utf-8",
        )
        invalid_output = work / "cpf-loan"
        invalid_result = run(
            base + ["domain", "validate", "--file", str(invalid), "--output", str(invalid_output)],
            expect=2,
        )
        if invalid_output.exists():
            raise RuntimeError("Invalid secret input에서 partial Generated Source가 생겼습니다.")
        evidence["checks"]["plaintextSecretRejection"] = {
            "status": "PASS",
            "stderr": invalid_result.stderr.strip(),
        }

        evidence["status"] = "PASS"
        print(json.dumps(evidence, ensure_ascii=False, indent=2))
        return 0
    finally:
        # 검증 산출물은 build/** transient이며 작업 종료 시 정리한다.
        shutil.rmtree(work, ignore_errors=True)


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except Exception as exc:  # noqa: BLE001 - Gate는 전체 실패 원문을 한 번에 보여준다.
        print(f"CPF_GENERATOR_LIFECYCLE_GATE=FAIL {exc}", file=sys.stderr)
        raise SystemExit(2)
