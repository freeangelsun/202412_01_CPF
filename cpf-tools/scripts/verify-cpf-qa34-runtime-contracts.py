#!/usr/bin/env python3
import argparse,json,re,sys
from pathlib import Path

def require(root, rel, tokens, failures):
 p=root/rel
 if not p.is_file(): failures.append(f"missing:{rel}"); return
 text=p.read_text(encoding='utf-8',errors='replace')
 for token in tokens:
  if token not in text: failures.append(f"{rel}:missing-token:{token}")

def main():
 ap=argparse.ArgumentParser();ap.add_argument('--root',default='.');ap.add_argument('--json-report');a=ap.parse_args();root=Path(a.root);f=[]
 require(root,'cpf-batch/scheduler/src/main/java/com/cpf/batch/scheduler/SchedulerDispatchService.java',['UNKNOWN','fencing','outbox','reconcile'],f)
 require(root,'cpf-batch/control-server/src/main/java/com/cpf/batch/control/deploy/DeploymentEngine.java',['UNKNOWN_RESULT','ROLLBACK','reconcile','idempotencyKey'],f)
 require(root,'cpf-batch/control-server/src/main/java/com/cpf/batch/control/deploy/DeploymentCellLock.java',['acquire','release'],f)
 require(root,'cpf-gateway/src/main/java/com/cpf/gateway/scg/CpfGatewayLedgerCompletionFilter.java',['UNKNOWN_RESULT','recovery'],f)
 require(root,'cpf-gateway/src/main/java/com/cpf/gateway/scg/CpfGatewayAuditRecoverySpool.java',['spool','replay'],f)
 require(root,'cpf-gateway/src/main/java/com/cpf/gateway/scg/CpfGatewayLedgerRecoverySpool.java',['spool','replay'],f)
 require(root,'cpf-tools/scripts/smoke-bat-two-worker-runtime.ps1',['Stop-Process','two','worker'],f)
 result={'status':'PASS' if not f else 'FAIL','failures':f}
 if a.json_report: Path(a.json_report).write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
 print(json.dumps(result,ensure_ascii=False,indent=2));return 0 if not f else 1
if __name__=='__main__': raise SystemExit(main())
