#!/usr/bin/env python3
"""Canonical stateless Generated Domain lifecycle contract verifier."""
from __future__ import annotations

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
    if contract.get("schemaVersion") != 4: raise ContractError("schemaVersion must be 4")
    if contract.get("supportedVendors") != VENDORS: raise ContractError(f"supportedVendors must be exactly {VENDORS}")
    if contract.get("operations") != OPERATIONS: raise ContractError(f"operations must be exactly {OPERATIONS}")
    engine=require_file(root,str(contract.get("canonicalEngine","")))
    cli=require_file(root,str(contract.get("canonicalCli","")))
    schema=require_file(root,str(contract.get("canonicalInputSchema","")))
    load_json(schema)
    _require_tokens(engine,["def preflight(","def dry_run(","def generate(","def diff(","def regenerate(","def upgrade(","def restore(","def remove_owned(","def verify_generated(","_write_transient_state","generation-state.json","customerMetadata':'NONE'"])
    _require_tokens(cli,["domain","generate","dry-run","diff","regenerate","upgrade","restore","remove","verify"])
    state=contract.get("transientState")
    if not isinstance(state,dict) or state.get("customerProjectMetadata") != "NONE" or "build/domain-generator/verification" not in str(state.get("directory","")):
        raise ContractError("transientState must keep lifecycle state outside the generated customer project")
    lock=contract.get("workspaceOwnershipLock")
    if not isinstance(lock,dict) or lock.get("sourceControlled") is not True or lock.get("semanticSourceOfTruth") is not False or lock.get("freshCloneRecovery") is not True or lock.get("generatedProjectMetadata") is not False:
        raise ContractError("workspaceOwnershipLock must be source-controlled safety metadata outside Generated Project")
    forbidden=contract.get("forbiddenPermanentProjectEntries")
    if not isinstance(forbidden,list) or len(forbidden)!=len(set(forbidden)) or not {".cpf","manifest"}.issubset(set(forbidden)) or "cpf-domain.yaml" in set(forbidden):
        raise ContractError("forbiddenPermanentProjectEntries must block generator-only metadata while allowing canonical cpf-domain.yaml")
    protection=contract.get("userProtection")
    required={
      "generatedFileDriftBlocksRegenerate":True,"generatedFileDriftBlocksUpgrade":True,
      "generatedFileDriftBlocksRemove":True,"unmanagedFilesAreNeverRemoved":True,
      "databaseObjectsNeverAutoDropped":True,"restoreRequiresMatchingDefinitionAndExpectedSeed":True,
      "purgeDefinitionRequiresExplicitOption":True,"frameworkIntegrationPointsRemovedWithDomain":True,
    }
    if protection != required: raise ContractError("userProtection must remain fail-closed")
    db_assets=contract.get("generatedDatabaseAssets")
    if (not isinstance(db_assets,dict) or db_assets.get("sourceControlled") is not False
            or db_assets.get("generatedDomainRoot") is not False or db_assets.get("vendors") != VENDORS
            or "build/domain-generator/verification" not in str(db_assets.get("root", ""))):
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
        for rel in [contract["canonicalInputSchema"],"cpf-tools/generator/contracts/cpf-starter-catalog.json","gradle/cpf-stack.properties"]:
            src=root/rel; dst=repo/rel; dst.parent.mkdir(parents=True,exist_ok=True); shutil.copy2(src,dst)
        for rel in ["cpf-tools/db/generated/domain-template",
                    "cpf-starters/data/persistence/src/main/resources/cpf-generated-domain-dialect"]:
            src=root/rel; dst=repo/rel; dst.parent.mkdir(parents=True,exist_ok=True); shutil.copytree(src,dst)
        definition=stage/"ledger.yaml"
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
        output=repo/"cpf-ledger"
        dry=engine.dry_run(repo,definition,output)
        if dry.get("status")!="DRY_RUN_PASS": raise ContractError("dry-run did not pass")
        transient=repo/"build/domain-generator/verification/cpf-ledger"
        if transient.exists(): raise ContractError("dry-run mutated persistent transient state/evidence")
        gen=engine.generate(repo,definition,output)
        if gen.get("status")!="GENERATED": raise ContractError("generate did not materialize project")
        d=engine.validate_definition(engine.load_yaml_subset(definition))
        vr=engine.verify_generated(repo,definition,output,d)
        if vr.get("status")!="PASS" or vr.get("customerMetadata")!="NONE": raise ContractError("generated project verification failed")
        if not (output / "cpf-domain.yaml").is_file(): raise ContractError("canonical cpf-domain.yaml missing from Generated Root")
        if any((output/name).exists() for name in contract["forbiddenPermanentProjectEntries"]): raise ContractError("generator-only metadata leaked into customer project")
        state=repo/"build/domain-generator/verification/cpf-ledger/generation-state.json"
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
        # 실제 제품 CLI는 삭제하지 않는다. 여기서는 disposable fixture에서 사용자 Delete Manifest 실행만 시뮬레이션한다.
        for rel in candidates:
            candidate=output/rel
            if candidate.is_file(): candidate.unlink()
        for directory in sorted([p for p in output.rglob('*') if p.is_dir()],key=lambda p:len(p.parts),reverse=True):
            try: directory.rmdir()
            except OSError: pass
        remaining={p.name for p in output.iterdir()} if output.is_dir() else set()
        if not remaining.issubset({"cpf-domain.yaml","cpf-generator.lock.json"}):
            raise ContractError(f"safe remove replay left non-canonical generated content: {sorted(remaining)}")
        restored=engine.restore(repo,definition,output)
        if restored.get("status")!="RESTORED": raise ContractError("restore did not restore matching seed")
        if not engine.diff(repo,definition,output).get("clean"): raise ContractError("restore parity is not clean")

        # Public Workspace fresh clone: source-controlled definition + ownership lock만으로 sync/upgrade가 복구되어야 한다.
        public_repo=stage/"public-repo"; public_repo.mkdir()
        for rel in [contract["canonicalInputSchema"],"cpf-tools/generator/contracts/cpf-starter-catalog.json","gradle/cpf-stack.properties"]:
            src=root/rel; dst=public_repo/rel; dst.parent.mkdir(parents=True,exist_ok=True); shutil.copy2(src,dst)
        src=root/"cpf-tools/db/generated/domain-template"; dst=public_repo/"cpf-tools/db/generated/domain-template"; dst.parent.mkdir(parents=True,exist_ok=True); shutil.copytree(src,dst)
        public_output=public_repo/"cpf-ledger"; public_output.mkdir(parents=True)
        public_def=public_output/"cpf-domain.yaml"
        public_def.write_text(definition.read_text(encoding="utf-8"),encoding="utf-8")
        public_gen=engine.generate(public_repo,public_def,public_output)
        if public_gen.get("status")!="GENERATED": raise ContractError("public workspace generate did not pass")
        lock=public_output/"cpf-generator.lock.json"
        if not lock.is_file(): raise ContractError("source-controlled workspace ownership lock missing")
        transient_public=public_repo/"build/domain-generator/verification/cpf-ledger"
        if transient_public.exists(): shutil.rmtree(transient_public)
        updated=public_def.read_text(encoding="utf-8").replace("  online: true\n", "  online: true\n  batch: true\n")
        public_def.write_text(updated,encoding="utf-8")
        synced=engine.upgrade(public_repo,public_def,public_output)
        if synced.get("status")!="UPGRADED" or not (public_output/"batch/build.gradle").is_file():
            raise ContractError("fresh clone ownership lock did not restore safe sync/upgrade")

def main() -> int:
    parser=argparse.ArgumentParser(); parser.add_argument("--root",type=Path,default=Path.cwd()); parser.add_argument("--contract",default="cpf-tools/generator/contracts/generator-lifecycle-contract.json"); parser.add_argument("--static-only",action="store_true")
    args=parser.parse_args(); root=args.root.resolve(); contract=load_json(root/args.contract); validate_contract(root,contract)
    if not args.static_only: validate_lifecycle_runtime(root,contract)
    print(f"[PASS] CPF generator lifecycle schema=4 vendors={len(VENDORS)} operations={len(OPERATIONS)} runtime={not args.static_only}")
    return 0

if __name__=="__main__":
    try: raise SystemExit(main())
    except ContractError as exc:
        print(f"[FAIL] {exc}",file=sys.stderr); raise SystemExit(1)
