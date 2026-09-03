#!/usr/bin/env python3
"""Current Source toolchain policy와 실제 host capability를 fail-closed로 비교합니다."""

import sys as _cpf_sys

# CPF 표준 인코딩은 UTF-8 이다. 호출자의 콘솔 코드페이지(Windows cp949 등)에 좌우되면
# 한글 출력이 깨져 진단 메시지를 읽을 수 없다. 진입점이 스스로 출력 스트림을 고정한다.
for _cpf_stream in (_cpf_sys.stdout, _cpf_sys.stderr):
    try:
        _cpf_stream.reconfigure(encoding='utf-8')
    except (AttributeError, ValueError):
        pass
from pathlib import Path
import argparse,json,re,shutil,subprocess,tempfile
ROOT=Path(__file__).resolve().parents[2]
POLICY_REL=Path('cpf-tools/verification/contracts/cpf-toolchain-compatibility.json')
def version_tuple(s):
    m=re.search(r'(\d+)(?:\.(\d+))?(?:\.(\d+))?',s); return tuple(int(x or 0) for x in m.groups()) if m else None
def run(cmd):
    cp=subprocess.run(cmd,text=True,encoding='utf-8',errors='replace',capture_output=True,check=False); return cp.returncode,((cp.stdout or '')+(cp.stderr or '')).strip()
def main()->int:
    ap=argparse.ArgumentParser(); ap.add_argument('--root',default=str(ROOT)); ap.add_argument('--json',action='store_true'); a=ap.parse_args()
    root=Path(a.root).resolve(); failures=[]; warnings=[]; actual={}; required={}
    policy=json.loads((root/POLICY_REL).read_text(encoding='utf-8')); tools=policy['tools']
    wrapper=(root/'gradlew.bat' if (root/'gradlew.bat').is_file() else root/'gradlew')
    if not wrapper.is_file(): failures.append('GRADLE_WRAPPER_MISSING')
    for rel in ['cpf-admin/frontend/package.json','cpf-admin/frontend/package-lock.json','cpf-backoffice-web/frontend/package.json','cpf-backoffice-web/frontend/package-lock.json']:
        if not (root/rel).is_file(): failures.append('FULL_FRONTEND_FILE_MISSING:'+rel)
    java=shutil.which('java'); javac=shutil.which('javac'); node=shutil.which('node'); npm=shutil.which('npm'); python=shutil.which('python'); pwsh=shutil.which('pwsh')
    required['java']='installed JDK capable of javac --release 25 and executing the compiled Java 25 class; exact host JDK version is not pinned'
    if not java or not javac:
        failures.append('JAVA_JDK_MISSING')
    else:
        rc,jv=run([java,'--version']); actual['java']=jv.splitlines()[0] if jv else ''
        rc2,jcv=run([javac,'--version']); actual['javac']=jcv.splitlines()[0] if jcv else ''
        if rc or rc2:
            failures.append('JAVA_JDK_VERSION_UNREADABLE')
        else:
            with tempfile.TemporaryDirectory(prefix='cpf-java25-capability-') as td:
                probe=Path(td)/'CpfJava25CapabilityProbe.java'
                probe.write_text('public final class CpfJava25CapabilityProbe { public static void main(String[] args) { System.out.print("CPF_JAVA25_CAPABILITY=PASS"); } }\n',encoding='utf-8')
                cp=subprocess.run([javac,'--release','25',str(probe)],cwd=td,text=True,encoding='utf-8',errors='replace',capture_output=True,check=False)
                if cp.returncode:
                    failures.append('JAVA_RELEASE_25_COMPILE_CAPABILITY_MISSING')
                else:
                    rp=subprocess.run([java,'-cp',td,'CpfJava25CapabilityProbe'],cwd=td,text=True,encoding='utf-8',errors='replace',capture_output=True,check=False)
                    if rp.returncode or 'CPF_JAVA25_CAPABILITY=PASS' not in (rp.stdout or ''):
                        failures.append('JAVA_RELEASE_25_RUNTIME_CAPABILITY_MISSING')
    required['node']='installed + ESM/fetch + frontend npm lifecycle; engine floor '+str(tools['node'].get('compatibilityFloor'))+' is advisory'
    required['npm']='installed + npm ci/npm run; major '+str(tools['npm'].get('compatibilityFloorMajor'))+' is advisory'
    required['python']='>='+str(tools['python']['minVersion'])+' (verifier language features)'
    required['powershell']='major >='+str(tools['powershell']['minMajor'])+' (script runtime language)'
    if not node: failures.append('NODE_MISSING')
    else:
        rc,nv=run([node,'--version']); actual['node']=nv; v=version_tuple(nv)
        if rc or v is None: failures.append('NODE_VERSION_UNREADABLE:'+nv)
        else:
            floor=version_tuple(str(tools['node'].get('compatibilityFloor','0')))
            if floor and v<floor: warnings.append('NODE_BELOW_ADVISORY_ENGINE_FLOOR:'+nv+':advisory='+str(tools['node'].get('compatibilityFloor')))
            cap=subprocess.run([node,'--input-type=module','-e','if(typeof fetch!=="function")process.exit(3); await Promise.resolve();'],text=True,encoding='utf-8',errors='replace',capture_output=True,check=False)
            if cap.returncode: failures.append('NODE_ESM_FETCH_CAPABILITY_MISSING')
    if not npm: failures.append('NPM_MISSING')
    else:
        rc,nv=run([npm,'--version']); actual['npm']=nv; v=version_tuple(nv)
        if rc or v is None: failures.append('NPM_VERSION_UNREADABLE:'+nv)
        else:
            floor=int(tools['npm'].get('compatibilityFloorMajor',0));
            if v[0]<floor: warnings.append('NPM_BELOW_ADVISORY_FLOOR:'+nv+':advisoryMajor='+str(floor))
            cap=subprocess.run([npm,'ci','--help'],text=True,encoding='utf-8',errors='replace',capture_output=True,check=False)
            if cap.returncode: failures.append('NPM_CI_CAPABILITY_MISSING')
    if python:
        rc,pv=run([python,'--version']); actual['python']=pv; v=version_tuple(pv); minimum=version_tuple(tools['python']['minVersion'])
        if rc or v is None or v<minimum: failures.append('PYTHON_LANGUAGE_CAPABILITY_UNSUPPORTED:'+pv+':required='+required['python'])
    else: failures.append('PYTHON_MISSING')
    if pwsh:
        rc,pv=run([pwsh,'--version']); actual['powershell']=pv; v=version_tuple(pv)
        if rc or v is None or v[0]<int(tools['powershell']['minMajor']): failures.append('POWERSHELL_LANGUAGE_RUNTIME_UNSUPPORTED:'+pv+':required='+required['powershell'])
    else: failures.append('POWERSHELL_MISSING')
    payload={'status':'PASS' if not failures else 'FAIL','policy':'CAPABILITY_FIRST','prerequisiteSource':POLICY_REL.as_posix(),'required':required,'actual':actual,'warnings':warnings,'failures':failures}
    if a.json: print(json.dumps(payload,ensure_ascii=False))
    else:
        print('CPF_FULL_RUNTIME_PREREQUISITES='+payload['status']); print('policy=CAPABILITY_FIRST'); print('prerequisiteSource='+POLICY_REL.as_posix()); print('warnings='+str(len(warnings))); print('failures='+str(len(failures)))
        for x in warnings: print('WARN '+x)
        for x in failures: print('FAIL '+x)
    return 0 if not failures else 1
if __name__=='__main__': raise SystemExit(main())
