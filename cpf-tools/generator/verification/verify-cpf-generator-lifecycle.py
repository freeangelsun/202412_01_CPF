#!/usr/bin/env python3
"""Canonical stateless Generated Domain lifecycle contract verifier."""
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
import importlib.util
import json
import shutil
import sys
import tempfile
from pathlib import Path

VENDORS = ["mariadb", "postgresql", "oracle"]
OPERATIONS = ["preflight", "dry-run", "generate", "diff", "regenerate", "upgrade", "remove", "restore", "verify"]

class ContractError(RuntimeError):
    pass

def load_json(path: Path) -> dict:
    if not path.is_file():
        raise ContractError(f"missing JSON: {path}")
    try:
        value=json.loads(path.read_text(encoding="utf-8-sig"))
    except Exception as exc:
        raise ContractError(f"invalid JSON {path}: {exc}") from exc
    if not isinstance(value,dict): raise ContractError(f"JSON root must be object: {path}")
    return value

def require_file(root: Path, rel: str) -> Path:
    path=root/rel
    if not path.is_file(): raise ContractError(f"required file missing: {rel}")
    return path

def _require_tokens(path: Path, tokens: list[str]) -> None:
    text=path.read_text(encoding="utf-8-sig",errors="ignore")
    missing=[token for token in tokens if token not in text]
    if missing: raise ContractError(f"{path}: required lifecycle tokens missing={missing}")

def validate_contract(root: Path, contract: dict) -> None:
    if contract.get("schemaVersion") != 5: raise ContractError("schemaVersion must be 5")
    if contract.get("supportedVendors") != VENDORS: raise ContractError(f"supportedVendors must be exactly {VENDORS}")
    if contract.get("operations") != OPERATIONS: raise ContractError(f"operations must be exactly {OPERATIONS}")
    engine=require_file(root,str(contract.get("canonicalEngine","")))
    cli=require_file(root,str(contract.get("canonicalCli","")))
    schema=require_file(root,str(contract.get("transientInputSchema","")))
    load_json(schema)
    if contract.get("developerContract") != "cpf-<domain>/gradle.properties":
        raise ContractError("developerContract must be root gradle.properties")
    _require_tokens(engine,["def preflight(","def dry_run(","def generate(","def diff(","def regenerate(","def upgrade(","def restore(","def remove_owned(","def verify_generated(","_write_transient_state","generation-state.json","generatorMetadata':'ABSENT'"])
    _require_tokens(cli,["domain","generate","dry-run","diff","regenerate","upgrade","restore","remove","verify"])
    state=contract.get("transientState")
    if not isinstance(state,dict) or state.get("customerProjectMetadata") != "NONE" or "cpf-docs/governance/development-harness/evidence/platform/current/generated/domain-generator/verification" not in str(state.get("directory","")):
        raise ContractError("transientState must keep lifecycle state outside the generated customer project")
    lock=contract.get("workspaceOwnershipLock")
    if (not isinstance(lock,dict) or lock.get("sourceControlled") is not False or lock.get("status") != "FORBIDDEN"
            or lock.get("freshCloneRecovery") != "DETERMINISTIC_CONTRACT_PARITY_TO_TRANSIENT_STATE"
            or lock.get("generatedProjectMetadata") is not False):
        raise ContractError("workspaceOwnershipLock must be forbidden and fresh-clone recovery must be deterministic")
    forbidden=contract.get("forbiddenPermanentProjectEntries")
    if not isinstance(forbidden,list) or len(forbidden)!=len(set(forbidden)) or not {".cpf","manifest","cpf-domain.yaml","cpf-generator.lock.json"}.issubset(set(forbidden)):
        raise ContractError("forbiddenPermanentProjectEntries must block all generator-only metadata")
    protection=contract.get("userProtection")
    required={
      "generatedFileDriftBlocksRegenerate":True,"generatedFileDriftBlocksUpgrade":True,
      "generatedFileDriftBlocksRemove":True,"unmanagedFilesAreNeverRemoved":True,
      "databaseObjectsNeverAutoDropped":True,"restoreRequiresMatchingDefinitionAndExpectedSeed":True,
      "frameworkIntegrationPointsRemovedWithDomain":True,
    }
    if protection != required: raise ContractError("userProtection must remain fail-closed")
    db_assets=contract.get("generatedDatabaseAssets")
    if (not isinstance(db_assets,dict) or db_assets.get("sourceControlled") is not False
            or db_assets.get("generatedDomainRoot") is not False or db_assets.get("vendors") != VENDORS
            or "cpf-docs/governance/development-harness/evidence/platform/current/generated/domain-generator/verification" not in str(db_assets.get("root", ""))):
        raise ContractError("generatedDatabaseAssets must be externally rendered DB3 lifecycle assets outside Generated Domain source root")

def _load_engine(root: Path, rel: str):
    path=root/rel
    sys.path.insert(0,str(path.parent))
    spec=importlib.util.spec_from_file_location("cpf_generator_lifecycle_engine",path)
    if spec is None or spec.loader is None: raise ContractError(f"cannot load engine: {path}")
    module=importlib.util.module_from_spec(spec); sys.modules[spec.name]=module; spec.loader.exec_module(module); return module

def validate_lifecycle_runtime(root: Path, contract: dict) -> None:
    engine=_load_engine(root,contract["canonicalEngine"])
    with tempfile.TemporaryDirectory(prefix="cpf-generator-lifecycle-") as td:
        stage=Path(td); repo=stage/"repo"; repo.mkdir()
        for rel in [contract["transientInputSchema"],"cpf-tools/generator/contracts/cpf-starter-catalog.json","gradle/cpf-stack.properties"]:
            src=root/rel; dst=repo/rel; dst.parent.mkdir(parents=True,exist_ok=True); shutil.copy2(src,dst)
        for rel in ["cpf-tools/db/generated/domain-template",
                    "cpf-starters/data/persistence/src/main/resources/cpf-generated-domain-dialect"]:
            src=root/rel; dst=repo/rel; dst.parent.mkdir(parents=True,exist_ok=True); shutil.copytree(src,dst)
        lifecycle=repo/"cpf-docs/governance/development-harness/evidence/platform/current/generated/domain-generator/lifecycle-ledger"
        definition=lifecycle/"definition/cpf-domain.yaml"
        definition.parent.mkdir(parents=True,exist_ok=True)
        definition.write_text("""domain:
  name: ledger
  systemCode: LDG
  packageName: ledger
database:
  role: CUSTOMER_BUSINESS_DB
  tablePrefix: LDG
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
""",encoding="utf-8")
        output=lifecycle/"cpf-ledger"
        dry=engine.dry_run(repo,definition,output)
        if dry.get("status")!="DRY_RUN_PASS": raise ContractError("dry-run did not pass")
        transient=repo/"cpf-docs/governance/development-harness/evidence/platform/current/generated/domain-generator/verification/cpf-ledger"
        if transient.exists(): raise ContractError("dry-run mutated persistent transient state/evidence")
        gen=engine.generate(repo,definition,output)
        if gen.get("status")!="GENERATED": raise ContractError("generate did not materialize project")
        d=engine.validate_definition(engine.load_yaml_subset(definition))
        vr=engine.verify_generated(repo,definition,output,d)
        if vr.get("status")!="PASS" or vr.get("generatorMetadata")!="ABSENT": raise ContractError("generated project verification failed")
        if not (output / "gradle.properties").is_file(): raise ContractError("Developer Domain contract missing from Generated Root")
        if any((output/name).exists() for name in contract["forbiddenPermanentProjectEntries"]): raise ContractError("generator-only metadata leaked into customer project")
        state=repo/"cpf-docs/governance/development-harness/evidence/platform/current/generated/domain-generator/verification/cpf-ledger/generation-state.json"
        if not state.is_file(): raise ContractError("transient generation-state missing")
        target=next(output.rglob("SampleTransactionController.java")); original=target.read_text(encoding="utf-8")
        target.write_text(original+"\n// 사용자 변경\n",encoding="utf-8",newline="\n")
        for action,name in ((engine.regenerate,"regenerate"),(engine.upgrade,"upgrade")):
            try: action(repo,definition,output)
            except Exception: pass
            else: raise ContractError(f"{name} accepted user-modified generated file")
        try: engine.remove_owned(repo,definition,output,apply=True)
        except Exception: pass
        else: raise ContractError("remove accepted user-modified generated file")
        target.write_text(original,encoding="utf-8",newline="\n")
        up=engine.upgrade(repo,definition,output)
        if up.get("status")!="UPGRADED": raise ContractError("upgrade did not pass on unchanged seed")
        rem=engine.remove_owned(repo,definition,output,apply=False)
        if rem.get("status")!="PLANNED_DELETE_MANIFEST" or rem.get("applied") is not False:
            raise ContractError("safe remove must produce a user-executed Delete Manifest plan")
        candidates=rem.get("deleteCandidates") or []
        if not candidates:
            raise ContractError("safe remove produced no delete candidates")
        try: engine.remove_owned(repo,definition,output,apply=True)
        except Exception: pass
        else: raise ContractError("general direct --apply removed disposable Generated Source without explicit approval")
        user_file=output/"USER_NOTE.txt"; user_file.write_text("customer owned\n",encoding="utf-8")
        try: engine.remove_owned(repo,definition,output,apply=True,approved_disposable_lifecycle=True)
        except Exception: pass
        else: raise ContractError("approved disposable remove accepted a user-owned extra file")
        if not user_file.is_file() or any(not (output/rel).is_file() for rel in candidates):
            raise ContractError("failed approved disposable preflight partially deleted the Generated Root")
        user_file.unlink()
        for artifact in (output/"build/classes/sample.class",output/"online/build/test-results/result.xml"):
            artifact.parent.mkdir(parents=True,exist_ok=True); artifact.write_text("disposable\n",encoding="utf-8")
        removed=engine.remove_owned(repo,definition,output,apply=True,approved_disposable_lifecycle=True)
        if removed.get("status")!="REMOVED" or removed.get("applied") is not True:
            raise ContractError("approved disposable lifecycle did not apply its exact Delete Manifest")
        if sorted(removed.get("discardedBuildArtifacts") or []) != ["build","online/build"]:
            raise ContractError("approved disposable lifecycle did not report exact Gradle artifact roots")
        remaining=[rel for rel in candidates if (output/rel).exists()]
        if remaining: raise ContractError(f"approved disposable remove left Generated Source: {remaining}")
        restored=engine.restore(repo,definition,output)
        if restored.get("status")!="RESTORED": raise ContractError("restore did not restore matching seed")
        if not engine.diff(repo,definition,output).get("clean"): raise ContractError("restore parity is not clean")

        # Public Workspace fresh clone: Developer Contract + exact current Source parity로 transient state를 복구해야 한다.
        public_repo=stage/"public-repo"; public_repo.mkdir()
        for rel in [contract["transientInputSchema"],"cpf-tools/generator/contracts/cpf-starter-catalog.json","gradle/cpf-stack.properties"]:
            src=root/rel; dst=public_repo/rel; dst.parent.mkdir(parents=True,exist_ok=True); shutil.copy2(src,dst)
        src=root/"cpf-tools/db/generated/domain-template"; dst=public_repo/"cpf-tools/db/generated/domain-template"; dst.parent.mkdir(parents=True,exist_ok=True); shutil.copytree(src,dst)
        public_output=public_repo/"cpf-ledger"
        public_def=stage/"public-ledger-input.yaml"
        public_def.write_text(definition.read_text(encoding="utf-8"),encoding="utf-8")
        public_gen=engine.generate(public_repo,public_def,public_output)
        if public_gen.get("status")!="GENERATED": raise ContractError("public workspace generate did not pass")
        if any((public_output/name).exists() for name in ("cpf-domain.yaml","cpf-generator.lock.json",".cpf")):
            raise ContractError("Generator metadata leaked into public workspace output")
        transient_public=public_repo/"cpf-docs/governance/development-harness/evidence/platform/current/generated/domain-generator/verification/cpf-ledger"
        if transient_public.exists(): shutil.rmtree(transient_public)
        recovered=engine.regenerate(public_repo,public_output/"gradle.properties",public_output)
        if recovered.get("status")!="REGENERATED": raise ContractError("fresh clone deterministic state recovery failed")
        updated=public_def.read_text(encoding="utf-8").replace("  online: true\n", "  online: true\n  batch: true\n")
        public_def.write_text(updated,encoding="utf-8")
        synced=engine.upgrade(public_repo,public_def,public_output)
        if synced.get("status")!="UPGRADED" or not (public_output/"batch/build.gradle").is_file():
            raise ContractError("fresh clone deterministic state recovery did not enable safe sync/upgrade")

def main() -> int:
    parser=argparse.ArgumentParser(); parser.add_argument("--root",type=Path,default=Path.cwd()); parser.add_argument("--contract",default="cpf-tools/generator/contracts/generator-lifecycle-contract.json"); parser.add_argument("--static-only",action="store_true")
    args=parser.parse_args(); root=args.root.resolve(); contract=load_json(root/args.contract); validate_contract(root,contract)
    if not args.static_only: validate_lifecycle_runtime(root,contract)
    print(f"[PASS] CPF generator lifecycle schema=5 vendors={len(VENDORS)} operations={len(OPERATIONS)} runtime={not args.static_only}")
    return 0

if __name__=="__main__":
    try: raise SystemExit(main())
    except ContractError as exc:
        print(f"[FAIL] {exc}",file=sys.stderr); raise SystemExit(1)
