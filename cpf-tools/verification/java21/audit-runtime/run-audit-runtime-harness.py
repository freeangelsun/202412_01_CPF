#!/usr/bin/env python3
from __future__ import annotations
import argparse, json, os, shutil, signal, subprocess, sys, time
from pathlib import Path

def run(cmd, **kw):
    return subprocess.run(cmd, text=True, capture_output=True, **kw)

def line_count(path: Path)->int:
    if not path.exists(): return 0
    return sum(1 for x in path.read_text(encoding='utf-8').splitlines() if x.strip())

def main()->int:
    ap=argparse.ArgumentParser(); ap.add_argument('--work-dir',required=True); ap.add_argument('--source-head',required=True); ap.add_argument('--javac',default='javac'); ap.add_argument('--java',default='java'); ns=ap.parse_args()
    here=Path(__file__).resolve().parent; work=Path(ns.work_dir).resolve(); shutil.rmtree(work,ignore_errors=True); (work/'classes').mkdir(parents=True); (work/'store').mkdir()
    sources=[str(p) for p in sorted((here/'src').rglob('*.java'))]
    cp=run([ns.javac,'--release','21','-encoding','UTF-8','-d',str(work/'classes'),*sources])
    if cp.returncode: print(cp.stdout+cp.stderr); return cp.returncode
    classpath=str(work/'classes'); store=str(work/'store');
    def worker(instance,start,end,delay,execution,logname):
        log=open(work/logname,'w',encoding='utf-8'); p=subprocess.Popen([ns.java,'-cp',classpath,'com.cpf.tools.audit.AuditWorker',store,instance,str(start),str(end),str(delay),ns.source_head,execution],stdout=log,stderr=subprocess.STDOUT,text=True); return p,log
    p1,l1=worker('instance-A',1,140,20,'exec-A','instance-A.log'); p2,l2=worker('instance-B',81,220,8,'exec-B','instance-B.log')
    deadline=time.time()+15
    while line_count(work/'store'/'audit.records')<45 and time.time()<deadline: time.sleep(.05)
    before=line_count(work/'store'/'audit.records'); killed_pid=p1.pid; p1.kill(); p1.wait(timeout=5); l1.close()
    progress_deadline=time.time()+10; after=before
    while after<=before and time.time()<progress_deadline:
        time.sleep(.05); after=line_count(work/'store'/'audit.records')
        if p2.poll() is not None and after<=before: break
    if after<=before:
        print(f'other process did not progress before={before} after={after} processBExit={p2.poll()}')
        if p2.poll() is None: p2.kill()
        return 4
    p2.wait(timeout=30); l2.close()
    p1r,l1r=worker('instance-A-restarted',1,140,0,'exec-A-restart','instance-A-restart.log'); p1r.wait(timeout=30); l1r.close()
    verifier=run([ns.java,'-cp',classpath,'com.cpf.tools.audit.AuditVerifier',store,'220',ns.source_head])
    failure=run([ns.java,'-cp',classpath,'com.cpf.tools.audit.AuditFailureProbe',str(work/'failure-probe'),ns.source_head])
    raw=(work/'store'/'audit.records').read_text(encoding='utf-8')
    secret_leaks=[s for s in ('superSecret','rawToken','900101-1234567') if s in raw]
    result={'java_version':run([ns.java,'-version']).stderr.splitlines()[0], 'javac_exit':cp.returncode,'instance_a_pid':killed_pid,'instance_b_pid':p2.pid,'kill_exit':p1.returncode,'count_before_kill':before,'count_after_kill':after,'restart_pid':p1r.pid,'verifier_exit':verifier.returncode,'verifier_output':verifier.stdout.strip(),'failure_probe_exit':failure.returncode,'failure_probe_output':failure.stdout.strip(),'secret_leaks':secret_leaks,'record_count':line_count(work/'store'/'audit.records'),'source_head':ns.source_head}
    (work/'result.json').write_text(json.dumps(result,ensure_ascii=False,indent=2),encoding='utf-8')
    print(json.dumps(result,ensure_ascii=False,indent=2))
    return 0 if verifier.returncode==0 and failure.returncode==0 and not secret_leaks and after>before else 5
if __name__=='__main__': raise SystemExit(main())
