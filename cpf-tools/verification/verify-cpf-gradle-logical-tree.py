#!/usr/bin/env python3
"""Fail-closed CPF Gradle logical project tree contract.

Checks IDE-facing logical paths without changing physical source directories or Maven coordinates.
No Git access is performed.
"""
from __future__ import annotations
import argparse, json, re, sys
from pathlib import Path

ROOT_GROUPS = {"apps", "runtime", "framework", "starters", "internal"}
EXPLICIT_LEAVES = {
    ":framework:core": "cpf-core",
    ":apps:admin": "cpf-admin",
    ":apps:backoffice": "cpf-backoffice/online",
    ":runtime:gateway": "cpf-gateway",
    ":apps:education": "cpf-education",
    ":internal:testing:testkit": "cpf-tools/testing/cpf-testkit",
    ":runtime:local": "cpf-tools/runtime/cpf-local-runtime",
    ":runtime:local-batch": "cpf-tools/runtime/cpf-local-batch-runtime",
    ":internal:verification:core-only-consumer": "cpf-tools/verification/core-only-consumer",
    ":runtime:batch:api": "cpf-batch/api",
    ":runtime:batch:runtime-support": "cpf-batch/runtime-support",
    ":runtime:batch:runtime": "cpf-batch/runtime",
    ":runtime:batch:control-plane": "cpf-batch/control-plane",
    ":runtime:batch:scheduler": "cpf-batch/scheduler",
    ":runtime:batch:worker": "cpf-batch/worker",
    ":runtime:batch:center-cut": "cpf-batch/center-cut",
    ":runtime:batch:agent": "cpf-batch/agent",
    ":runtime:batch:testkit": "cpf-batch/testkit",
}
# 통합 Runtime 만 고정 진입점이다. 개별 App/Domain 실행은 발견 결과로 투영되므로
# 이름을 고정하지 않고 논리 project path 를 쓰는지만 계약으로 강제한다.
RUN_ALIASES = {
    "cpfRunAllLocal": ":runtime:local:bootRun",
    "cpfRunAllBatch": ":runtime:local-batch:bootRun",
}
PROJECTED_RUN_TARGETS = (
    'dependsOn "${a.path}:bootRun"',
    'dependsOn "${d.mountedPath}:bootRun"',
)

def fail(msgs: list[str], msg: str) -> None:
    msgs.append(msg)

def main() -> int:
    ap=argparse.ArgumentParser()
    ap.add_argument('--root', default='.')
    ap.add_argument('--json-output')
    ns=ap.parse_args(); root=Path(ns.root).resolve(); errors=[]
    settings=(root/'settings.gradle').read_text(encoding='utf-8')
    convention=(root/'cpf-tools/build/cpf-root-conventions.gradle').read_text(encoding='utf-8')
    catalog=json.loads((root/'cpf-tools/generator/contracts/cpf-starter-catalog.json').read_text(encoding='utf-8'))
    modules=catalog.get('modules',[])
    paths=[str(m.get('projectPath','')) for m in modules]
    if len(paths) != len(set(paths)): fail(errors, 'duplicate canonical module projectPath')
    if len(paths) != 64: fail(errors, f'canonical starter module count must be 64, actual={len(paths)}')
    for m in modules:
        path=str(m.get('projectPath','')); owner=str(m.get('ownerPath',''))
        parts=path.split(':')[1:]
        if len(parts)<2 or parts[0] not in {'starters','internal','framework'}:
            fail(errors,f'invalid catalog logical path: {path}')
        if path.startswith(':cpf-'):
            fail(errors,f'legacy flat catalog path: {path}')
        if not (root/owner/'build.gradle').is_file():
            fail(errors,f'catalog owner build missing: {owner}')
    # Catalog-driven include/projectDir is required so Maven artifact coordinates stay independent.
    for token in ('include projectPath','project(projectPath).projectDir = file(ownerPath)'):
        if token not in settings: fail(errors,f'settings missing catalog-driven logical binding: {token}')
    for path, rel in EXPLICIT_LEAVES.items():
        if not re.search(r"include[^\n]*['\"]"+re.escape(path)+r"['\"]", settings):
            fail(errors,f'explicit logical leaf not included: {path}')
        binding=f"project('{path}').projectDir = file('{rel}')"
        if binding not in settings:
            fail(errors,f'physical owner binding mismatch: expected {binding}')
    batch_binding="project(':runtime:batch').projectDir = file('cpf-batch')"
    if batch_binding not in settings:
        fail(errors,'Batch aggregate must own physical cpf-batch build.gradle')
    for group in ROOT_GROUPS:
        if f"project(':{group}').projectDir" not in settings:
            fail(errors,f'logical root parent missing projectDir: :{group}')
    for task,target in RUN_ALIASES.items():
        if f"registerCpfRunAlias('{task}', '{target}'" not in convention:
            fail(errors,f'run alias drift: {task} -> {target}')
    for marker in PROJECTED_RUN_TARGETS:
        if marker not in convention:
            fail(errors,f'projected run target drift: {marker}')
    if 'Gradle Projects는 apps / runtime / framework / starters / internal 계층' not in convention:
        fail(errors,'cpfHelp does not describe canonical IDE hierarchy')
    # Only concrete Gradle project/task references are forbidden from using retired top-level paths.
    retired=re.compile(r"['\"]:(cpf-core|cpf-admin|cpf-backoffice|cpf-gateway|cpf-education|cpf-local-runtime|cpf-local-batch-runtime|cpf-batch(?::(?:api|runtime-support|runtime|control-plane|scheduler|worker|center-cut|agent|testkit))?)(?::[^'\"]*)?['\"]")
    scan_ext={'.gradle','.kts','.ps1','.sh','.bat','.cmd'}
    for p in root.rglob('*'):
        if not p.is_file() or p.suffix.lower() not in scan_ext or 'cpf-docs' in p.parts: continue
        try: text=p.read_text(encoding='utf-8')
        except UnicodeDecodeError: continue
        for match in retired.finditer(text):
            fail(errors,f'retired Gradle project/task reference: {p.relative_to(root).as_posix()} {match.group(0)}')
    result={
        'gate':'CPF_GRADLE_LOGICAL_TREE', 'status':'PASS' if not errors else 'FAIL',
        'catalogModuleCount':len(paths), 'logicalRoots':sorted(ROOT_GROUPS),
        'explicitLeafCount':len(EXPLICIT_LEAVES), 'failures':errors,
    }
    payload=json.dumps(result,ensure_ascii=False,indent=2)+'\n'
    if ns.json_output:
        out=Path(ns.json_output); out=out if out.is_absolute() else root/out; out.parent.mkdir(parents=True,exist_ok=True); out.write_text(payload,encoding='utf-8')
    print(payload,end='')
    return 1 if errors else 0

if __name__=='__main__': raise SystemExit(main())
