#!/usr/bin/env python3
"""Deterministic CPF migration apply/partial-failure/rollback/reapply state simulator."""
from __future__ import annotations
import argparse,hashlib,json,sys
from pathlib import Path
VALID=('PENDING','COMPLETED','UNKNOWN','ROLLED_BACK')
def plan_hash(ops:list[str])->str:return hashlib.sha256('\n'.join(ops).encode()).hexdigest()
def simulate(ops:list[str],fail_at:int|None=None)->dict:
 if not ops or len(set(ops))!=len(ops):raise ValueError('operations must be non-empty and unique')
 states={op:'PENDING' for op in ops};events=[]
 for i,op in enumerate(ops):
  if fail_at is not None and i==fail_at:
   states[op]='UNKNOWN';events.append({'operation':op,'action':'APPLY','status':'UNKNOWN'});break
  states[op]='COMPLETED';events.append({'operation':op,'action':'APPLY','status':'COMPLETED'})
 return {'schemaVersion':1,'planSha256':plan_hash(ops),'operations':ops,'states':states,'events':events,'reconcileRequired':'UNKNOWN' in states.values()}
def rollback(state:dict)->dict:
 for op in reversed(state['operations']):
  if state['states'][op]=='COMPLETED':state['states'][op]='ROLLED_BACK';state['events'].append({'operation':op,'action':'ROLLBACK','status':'COMPLETED'})
  elif state['states'][op]=='UNKNOWN':state['events'].append({'operation':op,'action':'ROLLBACK','status':'SKIPPED_UNKNOWN'})
 state['reconcileRequired']='UNKNOWN' in state['states'].values();return state
def reapply(state:dict)->dict:
 for op in state['operations']:
  if state['states'][op] in ('PENDING','UNKNOWN','ROLLED_BACK'):
   state['states'][op]='COMPLETED';state['events'].append({'operation':op,'action':'REAPPLY','status':'COMPLETED'})
 state['reconcileRequired']=False;return state
def main()->int:
 ap=argparse.ArgumentParser();ap.add_argument('--plan',type=Path,required=True);ap.add_argument('--fail-at',type=int);ap.add_argument('--rollback',action='store_true');ap.add_argument('--reapply',action='store_true');ap.add_argument('--output',type=Path);a=ap.parse_args()
 raw=json.loads(a.plan.read_text());ops=raw['operations'] if isinstance(raw,dict) else raw
 state=simulate(ops,a.fail_at)
 if a.rollback:state=rollback(state)
 if a.reapply:state=reapply(state)
 text=json.dumps(state,ensure_ascii=False,indent=2,sort_keys=True)+'\n'
 if a.output:a.output.parent.mkdir(parents=True,exist_ok=True);a.output.write_text(text)
 print(text,end='');return 0
if __name__=='__main__':
 try:raise SystemExit(main())
 except (OSError,ValueError,KeyError,json.JSONDecodeError) as e:print(json.dumps({'status':'FAIL','error':type(e).__name__}),file=sys.stderr);raise SystemExit(1)
