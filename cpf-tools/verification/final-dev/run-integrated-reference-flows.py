#!/usr/bin/env python3
from __future__ import annotations
import argparse, shutil, subprocess, sys, tempfile
from pathlib import Path

ap=argparse.ArgumentParser(); ap.add_argument("--root", type=Path, default=Path(".")); args=ap.parse_args()
ROOT = args.root.resolve()
ONLINE = ROOT/'cpf-reference/src/main/java/com/cpf/reference/online/integrated/OnlineAbcdReferenceFlow.java'
BATCH = ROOT/'cpf-reference/src/main/java/com/cpf/reference/batch/integrated/BatchAbcReferenceFlow.java'

HARNESS = r'''
import com.cpf.reference.online.integrated.OnlineAbcdReferenceFlow;
import com.cpf.reference.batch.integrated.BatchAbcReferenceFlow;
import java.util.List;

public class CpfIntegratedReferenceHarness {
  private static void check(boolean v, String m) { if (!v) throw new AssertionError(m); }
  public static void main(String[] args) {
    online(); batch();
    System.out.println("PASS integrated reference runtime harness");
  }
  private static void online() {
    var repo=new OnlineAbcdReferenceFlow.InMemoryRepository(); var remote=new OnlineAbcdReferenceFlow.ScenarioRemote();
    var c=new OnlineAbcdReferenceFlow.DomainC(remote); var b=new OnlineAbcdReferenceFlow.DomainB(c,repo); var a=new OnlineAbcdReferenceFlow.DomainA(b);
    var ctl=new OnlineAbcdReferenceFlow.Controller(a); var d=new OnlineAbcdReferenceFlow.DomainD(repo,remote);
    var ok=ctl.execute(new OnlineAbcdReferenceFlow.Request("TX-O","K1","P",1));
    check(ok.outcome()==OnlineAbcdReferenceFlow.Outcome.SUCCESS,"online success");
    check(ok.events().stream().allMatch(e->e.transactionId().equals("TX-O")),"online transactionId lineage");
    var dup=ctl.execute(new OnlineAbcdReferenceFlow.Request("TX-O","K1","P",2));
    check(dup.outcome()==OnlineAbcdReferenceFlow.Outcome.SUCCESS && remote.sideEffects.get()==1,"online duplicate side-effect");
    remote.timeout=true; var t=ctl.execute(new OnlineAbcdReferenceFlow.Request("TX-R","K2","P",1));
    check(t.outcome()==OnlineAbcdReferenceFlow.Outcome.FAILED,"online timeout"); remote.timeout=false;
    repo.failSave=true; var u=ctl.execute(new OnlineAbcdReferenceFlow.Request("TX-U","K3","P",1));
    check(u.outcome()==OnlineAbcdReferenceFlow.Outcome.UNKNOWN,"online remote-success/db-fail unknown");
    check(d.reconcile(u).outcome()==OnlineAbcdReferenceFlow.Outcome.RECONCILED,"online reconcile");
  }
  private static void batch() {
    var store=new BatchAbcReferenceFlow.Store(); var remote=new BatchAbcReferenceFlow.Remote(); var lease=new BatchAbcReferenceFlow.Lease();
    var c=new BatchAbcReferenceFlow.DomainC(remote); var b=new BatchAbcReferenceFlow.DomainB(store); var a=new BatchAbcReferenceFlow.DomainA(b,c);
    var step=new BatchAbcReferenceFlow.Step(store,a,2,1); var op=new BatchAbcReferenceFlow.SchedulerOperator(new BatchAbcReferenceFlow.Job(step,lease));
    var items=List.of(new BatchAbcReferenceFlow.Item("K1","1"),new BatchAbcReferenceFlow.Item("K2","2"),new BatchAbcReferenceFlow.Item("K3","3"));
    step.killAfter=1;
    var p=op.launch(new BatchAbcReferenceFlow.Identity("TX-B","JOB","EX1","STEP",1,0),items,"N1");
    check(p.state()==BatchAbcReferenceFlow.State.UNKNOWN && p.checkpoint()==2,"batch process kill/checkpoint");
    step.killAfter=-1;
    var r=op.launch(new BatchAbcReferenceFlow.Identity("TX-B","JOB","EX2","STEP",2,0),items,"N2");
    check(r.state()==BatchAbcReferenceFlow.State.SUCCESS && r.committed()==3,"batch restart");
    check(r.identity().transactionId().equals("TX-B") && r.identity().attempt()==2,"batch structured identity");
    var again=op.launch(new BatchAbcReferenceFlow.Identity("TX-B","JOB","EX3","STEP",3,0),items,"N3");
    check(again.committed()==3 && remote.effectKeys().size()==3,"batch duplicate prevention");
  }
}
'''

def main() -> int:
    if not ONLINE.is_file() or not BATCH.is_file():
        print('FAIL missing integrated reference source', file=sys.stderr); return 1
    javac=shutil.which('javac'); java=shutil.which('java')
    if not javac or not java:
        print('UNVERIFIED Java runtime unavailable; javac/java required', file=sys.stderr); return 2
    with tempfile.TemporaryDirectory(prefix='cpf-ref-flow-') as td:
        t=Path(td); h=t/'CpfIntegratedReferenceHarness.java'; h.write_text(HARNESS,encoding='utf-8')
        cp=subprocess.run([javac,'-d',str(t),str(ONLINE),str(BATCH),str(h)],capture_output=True,text=True)
        if cp.returncode:
            print(cp.stdout+cp.stderr,file=sys.stderr); return cp.returncode
        run=subprocess.run([java,'-ea','-cp',str(t),'CpfIntegratedReferenceHarness'],capture_output=True,text=True)
        sys.stdout.write(run.stdout); sys.stderr.write(run.stderr); return run.returncode
if __name__=='__main__': raise SystemExit(main())
