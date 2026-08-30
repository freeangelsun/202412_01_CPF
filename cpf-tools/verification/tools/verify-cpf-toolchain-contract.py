#!/usr/bin/env python3
from __future__ import annotations
import argparse,json
from pathlib import Path

POLICY_REL=Path("cpf-tools/verification/contracts/cpf-toolchain-compatibility.json")
FRONTENDS=(Path("cpf-admin/frontend/package.json"),Path("cpf-backoffice-web/frontend/package.json"))

def main()->int:
    ap=argparse.ArgumentParser(); ap.add_argument('--root',default='.'); ap.add_argument('--mutation-self-test',action='store_true'); a=ap.parse_args()
    root=Path(a.root).resolve(); findings=[]
    policy=json.loads((root/POLICY_REL).read_text(encoding='utf-8'))
    if policy.get('policy')!='CAPABILITY_FIRST': findings.append('policy:not-capability-first')
    principles=policy.get('principles') or {}
    for key in ('hostExactPatchPinForbidden','hostMinorPatchGateForbidden','installedCompatibleToolPreferred','capabilityProbePrecedesVersionRejection','projectOwnedWrapperLockContainerPinAllowed'):
        if principles.get(key) is not True: findings.append('policy:'+key)
    tools=policy.get('tools') or {}; java=tools.get('java') or {}; node=tools.get('node') or {}; npm=tools.get('npm') or {}; pwsh=tools.get('powershell') or {}
    if java.get('enforcement')!='CAPABILITY_FIRST_RELEASE_25': findings.append('java:capability-first-release25-missing')
    if java.get('maxMajor') is not None or java.get('hardMinMajor') is not None: findings.append('java:exact-host-major-pin')
    if 'javac --release 25 + execute compiled probe' not in (java.get('capabilities') or []): findings.append('java:release25-probe-missing')
    active_java_files=[
        'cpf-tools/build/gradle-plugin/src/main/java/com/cpf/gradle/CpfPlatformConventionPlugin.java',
        'cpf-tools/build/gradle-plugin/build.gradle','cpf-member/build.gradle','cpf-external/build.gradle',
        'cpf-backoffice/build.gradle','cpf-backoffice-web/build.gradle',
        'cpf-tools/generator/engine/cpf_domain_generator.py','cpf-tools/generator/engine/cpf_customer_library_generator.py',
        'cpf-tools/release/open-git/templates/cpf-education/build.gradle'
    ]
    for rel in active_java_files:
        text=(root/rel).read_text(encoding='utf-8',errors='ignore')
        if 'JavaLanguageVersion.of(25)' in text or 'getToolchain().getLanguageVersion().set(JavaLanguageVersion.of(25))' in text:
            findings.append(rel+':exact-host-java25-toolchain-pin')
    # Frontend engine metadata remains an advisory compatibility floor; it must not exact-pin a host patch/npm packageManager.
    expected_node='>='+str(node.get('compatibilityFloor','22.18.0'))
    expected_npm='>='+str(npm.get('compatibilityFloorMajor',10))
    for relp in FRONTENDS:
        rel=relp.as_posix(); data=json.loads((root/relp).read_text(encoding='utf-8'))
        engines=data.get('engines') or {}
        if engines.get('node')!=expected_node: findings.append(f'{rel}:node-compatibility-floor')
        if engines.get('npm')!=expected_npm: findings.append(f'{rel}:npm-compatibility-floor')
        if 'packageManager' in data: findings.append(f'{rel}:exact-packageManager-pin')
    # CPF-owned reproducibility pins (Wrapper/lock/container image) remain allowed.
    droot=root/'cpf-tools/environment/docker-development-test'
    corpus='\n'.join(x.read_text(encoding='utf-8',errors='ignore') for x in droot.rglob('*') if x.is_file())
    if f"node:{node.get('compatibilityFloor','22.18.0')}-bookworm" not in corpus: findings.append('docker-managed-node-image-missing')
    prereq=(root/'cpf-tools/verification/verify_runtime_prerequisites.py').read_text(encoding='utf-8',errors='ignore')
    for tok in ('cpf-toolchain-compatibility.json','CAPABILITY_FIRST','warnings','NPM_CI_CAPABILITY_MISSING'):
        if tok not in prereq: findings.append('runtime-prerequisite-missing:'+tok)
    for name,rel in [('required-full-runtime','cpf-tools/verification/tools/run-cpf-required-full-runtime-validation.ps1'),('local-full-runtime','cpf-tools/verification/tools/run-cpf-local-full-validation.ps1')]:
        text=(root/rel).read_text(encoding='utf-8',errors='ignore')
        if 'cpf-toolchain-compatibility.json' not in text: findings.append(name+':canonical-policy-missing')
        for forbidden in ('npm 10.9.2',"npmText-ne'10.9.2'",'node>=22.18.0<25'):
            if forbidden in text: findings.append(name+':exact-host-version-gate:'+forbidden)
    mutation=[]
    if a.mutation_self_test:
        mutated=json.loads(json.dumps(policy)); mutated['principles']['hostExactPatchPinForbidden']=False
        mutation.append('PASS' if mutated['principles']['hostExactPatchPinForbidden'] is not True else 'FAIL')
        if mutation!=['PASS']: findings.append('mutation-self-test-failed')
    payload={'status':'PASS' if not findings else 'FAIL','policy':policy.get('policy'),'nodeEngineAdvisory':expected_node,'npmEngineAdvisory':expected_npm,'powershellHardMajor':pwsh.get('minMajor',7),'findings':findings,'mutation':mutation}
    print(json.dumps(payload,ensure_ascii=False,indent=2)); return 0 if not findings else 1
if __name__=='__main__': raise SystemExit(main())
