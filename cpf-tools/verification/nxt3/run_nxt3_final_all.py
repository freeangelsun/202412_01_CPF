#!/usr/bin/env python3
"""CPF NXT3 최종 누적 검증 실행기.

한 Gate가 실패해도 나머지 Gate를 계속 실행하고 모든 오류를 Evidence로 남긴다.
제품 삭제/Commit/Push는 수행하지 않는다.
"""
from __future__ import annotations
import argparse, json, os, shutil, subprocess, sys, tempfile, time
from pathlib import Path

# The runner itself is Python; setting the environment only for child processes is too late
# to prevent this interpreter from emitting __pycache__ while importing helpers. Keep the
# repository validation read-only without weakening the garbage gate.
sys.dont_write_bytecode = True
os.environ["PYTHONDONTWRITEBYTECODE"] = "1"

# 개별 Gate 실패와 무관하게 전체 검증을 계속 수행하고 최종 결과에서 실패를 일괄 집계한다.

def run_one(name: str, cmd: list[str], root: Path, timeout: int) -> dict:
    started=time.time()
    try:
        env=os.environ.copy(); env['PYTHONDONTWRITEBYTECODE']='1'
        cp=subprocess.run(cmd,cwd=root,text=True,capture_output=True,timeout=timeout,env=env)
        return {"name":name,"status":"PASS" if cp.returncode==0 else "FAIL","rc":cp.returncode,
                "seconds":round(time.time()-started,3),"cmd":cmd,"stdout":cp.stdout[-30000:],"stderr":cp.stderr[-30000:]}
    except subprocess.TimeoutExpired as exc:
        out=exc.stdout.decode(errors='replace') if isinstance(exc.stdout,bytes) else (exc.stdout or '')
        err=exc.stderr.decode(errors='replace') if isinstance(exc.stderr,bytes) else (exc.stderr or '')
        return {"name":name,"status":"FAIL","rc":124,"seconds":round(time.time()-started,3),"cmd":cmd,
                "stdout":out[-30000:],"stderr":err[-30000:]+f"\nTIMEOUT={timeout}s"}
    except Exception as exc:
        return {"name":name,"status":"FAIL","rc":125,"seconds":round(time.time()-started,3),"cmd":cmd,"stdout":"","stderr":repr(exc)}


def main() -> int:
    ap=argparse.ArgumentParser()
    ap.add_argument('--root',default='.')
    ap.add_argument('--evidence',default='cpf-docs/work/evidence/current/STATIC_GATE_SWEEP.json')
    ap.add_argument('--log',default='cpf-docs/work/evidence/current/STATIC_GATE_SWEEP.log')
    ap.add_argument('--gate-timeout',type=int,default=180)
    ap.add_argument('--include-gradle',action='store_true')
    ap.add_argument('--gradle-timeout',type=int,default=1800)
    ns=ap.parse_args(); root=Path(ns.root).resolve(); py=sys.executable
    gate=root/'cpf-tools/verification/nxt3'
    # Verification mode must be read-only for tracked repository Evidence. Child gates that
    # can emit evidence are redirected to an external temporary directory so a clean checkout
    # remains byte-for-byte unchanged after NXT3 execution.
    child_evidence_tmp=tempfile.TemporaryDirectory(prefix='cpf-nxt3-child-evidence-')
    child_evidence=Path(child_evidence_tmp.name)
    entries=[
      ('root-generated-domain-prefix',[py,'-B',str(gate/'verify_root_generated_domain_prefix.py'),'--root',str(root)]),
      ('generated-domain-minimal-ia',[py,'-B',str(gate/'verify_generated_customer_domain_minimal_ia.py'),'--root',str(root)]),
      ('generator-public-boundary',[py,'-B',str(gate/'verify_generated_public_boundary.py'),'--root',str(root)]),
      ('generator-presets',[py,'-B',str(gate/'verify_generator_presets.py'),'--root',str(root)]),
      ('starter-provider-catalog',[py,'-B',str(gate/'verify_starter_provider_slot_catalog.py'),'--root',str(root)]),
      ('redis-valkey-current',[py,'-B',str(gate/'verify_redis_valkey_current.py'),'--root',str(root)]),
      ('redis-valkey-provider',[py,'-B',str(gate/'verify_redis_valkey_provider_currentization.py'),'--root',str(root),'--evidence',str(child_evidence/'REDIS_VALKEY_PROVIDER.json')]),
      ('annotation-runtime-consumer',[py,'-B',str(gate/'verify_annotation_runtime_consumer.py'),'--root',str(root),'--evidence',str(child_evidence/'ANNOTATION_RUNTIME_CONSUMER.json')]),
      ('adm-backoffice-framework',[py,'-B',str(gate/'verify_nxt3_adm_backoffice_framework.py'),'--root',str(root)]),
      ('adm-backoffice-frontend-gateway',[py,'-B',str(gate/'verify_nxt3_adm_backoffice_frontend_gateway.py'),'--root',str(root)]),
      ('common-management-propagation',[py,'-B',str(gate/'verify_nxt3_common_management_propagation.py'),'--root',str(root)]),
      ('config-contract',[py,'-B',str(gate/'verify_nxt3_config_contract.py'),'--root',str(root)]),
      ('query-db3-self-test',[py,'-B',str(gate/'verify_nxt3_query_db3.py'),'--root',str(root),'--self-test']),
      ('query-db3',[py,'-B',str(gate/'verify_nxt3_query_db3.py'),'--root',str(root)]),
      ('korean-comment-self-test',[py,'-B',str(gate/'verify_nxt3_korean_comment.py'),'--root',str(root),'--self-test']),
      ('korean-comment',[py,'-B',str(gate/'verify_nxt3_korean_comment.py'),'--root',str(root)]),
      ('adm-incident-canonical-db',[py,'-B',str(gate/'verify_adm_incident_canonical_db.py'),'--root',str(root)]),
      ('central-registry-retention-closure',[py,'-B',str(gate/'verify-cpf-central-registry-retention-closure.py'),'--root',str(root)]),
      ('db-source-plan-derivation',[py,'-B',str(gate/'verify_db_source_plan_derivation.py'),'--root',str(root)]),
      ('layout',[py,'-B',str(gate/'cpf_nxt3_layout_gate.py'),'--root',str(root)]),
      ('garbage-sweep',[py,'-B',str(gate/'verify_nxt3_repository_garbage.py'),'--root',str(root)]),
      ('hygiene',[py,'-B',str(gate/'verify_nxt3_hygiene.py'),'--root',str(root)]),
      ('cpf-verify-all',([str(root/'cpf-tools/runtime/cli/cpf.cmd')] if os.name=='nt' else [str(root/'cpf-tools/runtime/cli/cpf')])+['verify','all']),
    ]
    results=[]
    for name,cmd in entries:
        # Python gate는 script 실물을, shell/native command는 PATH 실행 가능 여부와 script 인수를 분리해 검증한다.
        # 'sh' 같은 PATH command를 Repository 상대경로로 오인하면 Linux에서 False FAIL이 발생한다.
        if cmd[0] == py:
            script_index = 2 if len(cmd) > 2 and cmd[1] == '-B' else 1
            runnable = len(cmd) > script_index and Path(cmd[script_index]).is_file()
        else:
            runnable = bool(shutil.which(cmd[0]) or Path(cmd[0]).is_file())
            if runnable and len(cmd) > 1 and cmd[0] in {'sh', 'bash'}:
                runnable = Path(cmd[1]).is_file()
        if not runnable:
            results.append({"name":name,"status":"FAIL","rc":127,"seconds":0,"cmd":cmd,"stdout":"","stderr":"required executable/script missing"})
            continue
        r=run_one(name,cmd,root,ns.gate_timeout); results.append(r)
        print(f"[{r['status']}] {name} rc={r['rc']} {r['seconds']}s",flush=True)

    if ns.include_gradle:
        gradlew=root/('gradlew.bat' if os.name=='nt' else 'gradlew')
        if gradlew.is_file():
            gradle_cmd=[str(gradlew),'--no-daemon','qualityGate','aggregateQualityBuild','publicationGate','qa34IntegrationTest']
            r=run_one('gradle-quality-publication-integration',gradle_cmd,root,ns.gradle_timeout); results.append(r)
            print(f"[{r['status']}] gradle-quality-publication-integration rc={r['rc']} {r['seconds']}s",flush=True)
        else:
            results.append({"name":"gradle-quality-publication-integration","status":"UNVERIFIED","rc":None,"seconds":0,"cmd":[],"stdout":"","stderr":"gradlew not found"})

    failed=[x for x in results if x['status']=='FAIL']; unverified=[x for x in results if x['status']=='UNVERIFIED']
    sha=os.environ.get('CPF_SOURCE_SHA','').strip()
    if not (len(sha)==40 and all(c in '0123456789abcdefABCDEF' for c in sha)):
        sha=''
    # User policy: validation must not query Git unless explicitly requested.
    if not sha:
        base=root/'cpf-docs/work/BASE_SHA.txt'
        if base.is_file():
            candidate=base.read_text(encoding='utf-8',errors='ignore').strip()
            if len(candidate)==40 and all(c in '0123456789abcdefABCDEF' for c in candidate): sha=candidate
    if not sha: sha='UNKNOWN'
    summary={"gate":"CPF_NXT_FINAL_ALL","executionSourceSha":sha,"status":"FAIL" if failed else ('UNVERIFIED' if unverified else 'PASS'),
             "failedCount":len(failed),"unverifiedCount":len(unverified),"checkCount":len(results),"results":results}
    ep=Path(ns.evidence); ep=ep if ep.is_absolute() else root/ep; ep.parent.mkdir(parents=True,exist_ok=True); ep.write_text(json.dumps(summary,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    lp=Path(ns.log); lp=lp if lp.is_absolute() else root/lp; lp.parent.mkdir(parents=True,exist_ok=True)
    with lp.open('w',encoding='utf-8',newline='\n') as f:
        f.write(f"CPF_NXT_FINAL_ALL={summary['status']} failed={len(failed)} unverified={len(unverified)} checks={len(results)}\n")
        for r in results:
            f.write(f"\n===== {r['name']} status={r['status']} rc={r['rc']} seconds={r['seconds']} =====\n")
            if r.get('stdout'): f.write(r['stdout']+'\n')
            if r.get('stderr'): f.write('STDERR:\n'+r['stderr']+'\n')
    print(f"CPF_NXT_FINAL_ALL={summary['status']} failed={len(failed)} unverified={len(unverified)} checks={len(results)}")
    child_evidence_tmp.cleanup()
    return 1 if failed else 0

if __name__=='__main__':
    raise SystemExit(main())
