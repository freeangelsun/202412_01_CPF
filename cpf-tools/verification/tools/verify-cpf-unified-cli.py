#!/usr/bin/env python3
from __future__ import annotations
import argparse, json, re, sys, zipfile
from pathlib import Path

PUBLIC={'bootstrap','domain-new','domain-sync','build','test','run','stop','reset','status','doctor','version','help'}
INTERNAL={'dev','verify','publish','release'}
CLASSIFICATIONS={'CANONICAL_ENGINE','INTERNAL_ENGINE','CLI_CONSUMER','THIN_WRAPPER','MIGRATE_TO_CLI','DUPLICATE','DEAD'}

def main()->int:
    ap=argparse.ArgumentParser(); ap.add_argument('--root',default='.'); args=ap.parse_args(); root=Path(args.root).resolve(); fail=[]
    src=root/'cpf-tools/runtime/cli/java/CpfCli.java'; builder=root/'cpf-tools/runtime/cli/build-cpf-cli.py'; catalog=root/'cpf-tools/runtime/cli/contracts/cpf-command-catalog.json'; inv=root/'cpf-tools/runtime/cli/contracts/cpf-tooling-entrypoint-inventory.json'
    java=[p for p in root.rglob('CpfCli.java') if not any(x in p.parts for x in ('build','cpf-release','.gradle'))]
    if java != [src]: fail.append('EXACTLY_ONE_CLI_SOURCE:'+','.join(p.relative_to(root).as_posix() for p in java))
    if not src.is_file(): fail.append('CLI_SOURCE_MISSING')
    else:
        text=src.read_text(encoding='utf-8')
        for cmd in PUBLIC|INTERNAL:
            if f'"{cmd}"' not in text: fail.append('CLI_COMMAND_MISSING:'+cmd)
        for token in ('StandardCharsets.UTF_8','CPF-CLI-JAVA-VERSION','capabilityProfile','internalEnabled','cpf_local_runtime.py','run-cpf-canonical-verifiers.py','domainState','--json','--strict','EXIT_TIMEOUT = 124','case "domain" -> generator','case "db-render" -> generator','PYTHONUTF8','PYTHONIOENCODING'):
            if token not in text: fail.append('CLI_CONTRACT_MISSING:'+token)
    if not builder.is_file():
        fail.append('CLI_BUILDER_MISSING')
    else:
        build_text=builder.read_text(encoding='utf-8')
        for token in ("'-version'", "'--release','25'", "'-Xlint:all'", "'-Werror'", 'Java 25 javac required'):
            if token not in build_text: fail.append('CLI_BUILDER_JAVA25_CONTRACT_MISSING:'+token)
    if not catalog.is_file(): fail.append('COMMAND_CATALOG_MISSING')
    else:
        data=json.loads(catalog.read_text(encoding='utf-8'))
        if data.get('owner')!='cpf-tools/runtime/cli' or data.get('officialInterface')!='cpf' or data.get('canonicalSource')!='cpf-tools/runtime/cli/java/CpfCli.java': fail.append('COMMAND_CATALOG_OWNER')
        if {x['command'] for x in data.get('publicCommands',[])}!=PUBLIC: fail.append('PUBLIC_COMMAND_CATALOG_DRIFT')
        if {x['namespace'] for x in data.get('internalNamespaces',[])}!=INTERNAL: fail.append('INTERNAL_COMMAND_CATALOG_DRIFT')
        internal_commands={x['namespace']:set(x.get('commands',[])) for x in data.get('internalNamespaces',[])}
        if not {'domain','db-render'} <= internal_commands.get('dev',set()): fail.append('INTERNAL_DOMAIN_LIFECYCLE_CATALOG_DRIFT')
        if 'domain' not in internal_commands.get('verify',set()): fail.append('INTERNAL_DOMAIN_VERIFY_CATALOG_DRIFT')
        if data.get('profiles',{}).get('PUBLIC',{}).get('internalNamespaces') is not False: fail.append('PUBLIC_INTERNAL_CAPABILITY_NOT_BLOCKED')
        expected_exit_codes={'OK':0,'FAILURE':1,'USAGE':2,'PREREQUISITE':69,'TIMEOUT':124}
        if data.get('developerDiscovery',{}).get('stableExitCodes')!=expected_exit_codes: fail.append('STABLE_EXIT_CODE_CATALOG_DRIFT')
    wrappers=['cpf-tools/runtime/cli/cpf','cpf-tools/runtime/cli/cpf.cmd','cpf-tools/runtime/cli/cpf.ps1']
    for rel in wrappers:
        p=root/rel
        if not p.is_file(): fail.append('WRAPPER_MISSING:'+rel); continue
        low=p.read_text(encoding='utf-8-sig').lower()
        if 'cpf-cli.jar' not in low or 'java' not in low: fail.append('WRAPPER_NOT_JAVA:'+rel)
        if any(x in low for x in ('docker compose','gradlew','cpf.py','domain create','domain sync')): fail.append('WRAPPER_LOGIC_DUPLICATED:'+rel)
    for rel in ('cpf-tools/build/tools/cpf-dev.ps1','cpf-tools/build/tools/cpf-dev.sh'):
        low=(root/rel).read_text(encoding='utf-8-sig').lower()
        if 'runtime/cli' not in low.replace('\\','/') or any(x in low for x in ('gradlew','run-cpf-local-full-validation.ps1','start-cpf-local.ps1')): fail.append('LEGACY_DEV_NOT_THIN:'+rel)
    generated_helper=root/'cpf-tools/generator/tools/generated-domain-common.ps1'
    helper_text=generated_helper.read_text(encoding='utf-8-sig') if generated_helper.is_file() else ''
    if 'cpf-tools/runtime/cli/cpf.cmd' not in helper_text.replace('\\','/') or 'cpf-tools/runtime/cli/cpf.py' in helper_text.replace('\\','/'):
        fail.append('GENERATED_DOMAIN_HELPER_BYPASSES_UNIFIED_CLI')
    for token in ("@('dev') + $processArguments", "@('dev', 'db-render')", 'CPF_WORKSPACE'):
        if token not in helper_text: fail.append('GENERATED_DOMAIN_HELPER_MAPPING_MISSING:'+token)
    engine=root/'cpf-tools/runtime/cli/cpf.py'
    if not engine.is_file() or 'INTERNAL Tooling Engine' not in engine.read_text(encoding='utf-8'): fail.append('PY_ENGINE_NOT_INTERNALIZED')
    if not inv.is_file(): fail.append('ENTRYPOINT_INVENTORY_MISSING')
    else:
        data=json.loads(inv.read_text(encoding='utf-8'))
        if set(data.get('classificationValues',[]))!=CLASSIFICATIONS: fail.append('ENTRYPOINT_CLASSIFICATION_VALUES')
        if data.get('duplicateCount')!=0 or data.get('deadCount')!=0: fail.append('ENTRYPOINT_DUPLICATE_OR_DEAD')
        if data.get('entrypointCount',0)<100: fail.append('ENTRYPOINT_INVENTORY_TOO_SMALL')
        ids={x['path']:x['classification'] for x in data.get('entries',[]) if x.get('id','').startswith('FILE:')}
        if ids.get('cpf-tools/runtime/cli/java/CpfCli.java')!='CANONICAL_ENGINE': fail.append('CLI_OWNER_CLASSIFICATION')
        if ids.get('cpf-tools/runtime/cli/cpf.py')!='INTERNAL_ENGINE': fail.append('PY_ENGINE_CLASSIFICATION')
    # Public projections must ship only thin wrappers + jar, never Java CLI source.
    for policy_rel in ('cpf-tools/release/open-git/open-git-surface-policy.json','cpf-tools/release/public/cpf-public-surface-policy.json'):
        p=root/policy_rel
        if not p.is_file(): fail.append('PUBLIC_POLICY_MISSING:'+policy_rel); continue
        pol=json.loads(p.read_text(encoding='utf-8'))
        rules=pol.get('templateRules',[])+pol.get('sourceRules',[])
        targets={str(x.get('target','')).replace('\\','/') for x in rules}
        if any(t.endswith(('CpfCli.java','CpfBootstrap.java','CpfGeneratorLauncher.java')) for t in targets): fail.append('PUBLIC_CLI_SOURCE_LEAK_POLICY:'+policy_rel)
    artifact=root/'cpf-tools/release/open-git/open-git-artifact-policy.json'
    if artifact.is_file():
        a=json.loads(artifact.read_text(encoding='utf-8')); b=a.get('profiles',{}).get('binary',{})
        if b.get('sourcesJar')!='DENY' or b.get('javadocJar')!='DENY' or a.get('defaultProfile')!='binary': fail.append('OPEN_GIT_BINARY_PROFILE_DRIFT')
    final_catalog=root/'cpf-tools/release/cpf-final-artifact-catalog.json'
    if not final_catalog.is_file():
        fail.append('FINAL_ARTIFACT_CATALOG_MISSING')
    else:
        fc=json.loads(final_catalog.read_text(encoding='utf-8'))
        rows=[row for row in fc.get('artifacts',[]) if row.get('artifactId')=='cpf-cli']
        if len(rows)!=1:
            fail.append('CPF_CLI_ARTIFACT_OWNER_COUNT:'+str(len(rows)))
        else:
            row=rows[0]
            if row.get('ownerPath')!='cpf-tools/runtime/cli' or row.get('producer')!='cpf-tools/runtime/cli/build-cpf-cli.py': fail.append('CPF_CLI_ARTIFACT_OWNER_DRIFT')
            if row.get('publishSources') is not False or row.get('publishJavadoc') is not False: fail.append('CPF_CLI_ARTIFACT_SOURCE_LEAK_POLICY')
            if set(row.get('capabilityProfiles',[]))!={'INTERNAL','PUBLIC'}: fail.append('CPF_CLI_ARTIFACT_PROFILE_DRIFT')
            if 'java25-fail-closed' not in row.get('requiredAttestations',[]): fail.append('CPF_CLI_JAVA25_ATTESTATION_MISSING')
    req=(root/'cpf-docs/governance/development-harness/product/CPF_PRODUCT_ARCHITECTURE_AND_REQUIREMENTS.md').read_text(encoding='utf-8')
    for token in ('CPF의 공식 Tooling Interface','cpf-tools','Public/Internal','cpf-cli.jar'):
        if token not in req: fail.append('TOP_REQUIREMENT_MISSING:'+token)
    print(json.dumps({'status':'PASS' if not fail else 'FAIL','failures':fail,'publicCommands':sorted(PUBLIC),'internalNamespaces':sorted(INTERNAL)},ensure_ascii=False))
    return 0 if not fail else 1
if __name__=='__main__': raise SystemExit(main())
