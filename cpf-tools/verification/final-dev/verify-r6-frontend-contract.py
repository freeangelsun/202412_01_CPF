#!/usr/bin/env python3
from __future__ import annotations
import re,sys
from pathlib import Path
R=Path(sys.argv[1]).resolve() if len(sys.argv)>1 else Path(__file__).resolve().parents[3]
errors=[]
def read(rel):
 p=R/rel
 if not p.is_file(): errors.append('missing '+rel); return ''
 return p.read_text(encoding='utf-8',errors='replace')

routes=read('cpf-admin/frontend/src/app/routes.ts')
entries=re.findall(r'^\s*"([^"]+)":\s*\{\s*routeId:\s*"([^"]+)".*?expectedOperationIds:\s*\[([^\]]*)\]',routes,re.M|re.S)
if len(entries)!=63: errors.append(f'ADM canonical route count={len(entries)} expected=63')
route_ops={key:re.findall(r'["\']([^"\']+)["\']',block) for key,rid,block in entries if key==rid}
if len(route_ops)!=63: errors.append('ADM routeId/key mismatch or duplicate')
contract=read('cpf-admin/frontend/src/generated/adm-route-operation-contract.ts')
generated={key:re.findall(r'["\']([^"\']+)["\']',block) for key,block in re.findall(r'^\s*"([^"]+)":\s*\[([^\]]*)\]',contract,re.M)}
if set(route_ops)!=set(generated): errors.append(f'route contract keys drift missing={sorted(set(route_ops)-set(generated))} extra={sorted(set(generated)-set(route_ops))}')
for key in sorted(set(route_ops)&set(generated)):
 if sorted(route_ops[key])!=sorted(generated[key]): errors.append('route operation drift '+key)
state=read('cpf-admin/frontend/src/state/createAdmState.ts')
for token in ['import { admCapabilityRegistry } from "../app/routes"','menus: Object.values(admCapabilityRegistry).map(route => ({']:
 if token not in state: errors.append('ADM state is not projected from canonical route registry: '+token)

for rel,invoke in [
 ('cpf-admin/frontend/src/components/RouteOperationWorkbench.vue','admInvokeOperation'),
 ('cpf-biz-admin/frontend/src/components/RouteOperationWorkbench.vue','bzaInvokeOperation')]:
 body=read(rel)
 for token in ['item.method === "GET"','descriptor.method !== "GET"','전용 화면']:
  if token not in body: errors.append(f'{rel}: GET-only guard missing {token}')
 for token in ['bodyText','attachDangerousReason','confirmOpen']:
  if token in body: errors.append(f'{rel}: generic mutation path remains {token}')
 if invoke not in body: errors.append(f'{rel}: generated read operation resolver missing')

approvals=read('cpf-admin/frontend/src/features/approvals/ApprovalsPage.vue')
api=read('cpf-admin/frontend/src/generated/cpf-api.ts')
required=['admApprovalPolicies','admApprovalPolicyDetail','admApprovalPolicySave','admApprovalRequest','admApprovalRequestDetail','admApprovalDecision','admApprovalExecute','admApprovalReconcile']
if 'from "../../generated/cpf-api"' not in approvals: errors.append('approval page does not import generated client')
for name in required:
 if not re.search(rf'\b{name}(?:<[^\n(]+>)?\s*\(',approvals): errors.append('approval page missing direct consumer '+name)
 if not re.search(rf'\b(?:export\s+)?(?:async\s+)?function\s+{name}\b|\bexport\s+const\s+{name}\b',api): errors.append('generated client missing '+name)
for token in ['admInvokeOperation(','admMutation(','fetch(','axios']:
 if token in approvals: errors.append('approval page generic/direct HTTP bypass '+token)
for token in ['requestKey:crypto.randomUUID()','idempotencyKey:crypto.randomUUID()','window.confirm','parseStrictJsonObject']:
 if token not in approvals: errors.append('approval UX safety missing '+token)

bza_manifest=read('cpf-biz-admin/frontend/src/shared/bzaPermissionManifest.ts')
for token in ['bza-permission-manifest.json','manifest.actionRules','manifest.apiResourceGroups','return null']:
 if token not in bza_manifest: errors.append('BZA canonical permission manifest contract missing '+token)

consumer_policies = {
 'cpf-admin/frontend/scripts/verify-operation-consumer.mjs': [
  'const generatedConsumed=new Set()', 'const workbenchGetOnly=',
  'const highRiskOperationIds=new Set()', 'riskLevel:',
  'high-risk mutation must call generated API directly', 'operation.method==="GET"',
  'expired consumer waiver:'
 ],
 'cpf-biz-admin/frontend/scripts/verify-operation-consumer.mjs': [
  'const generatedConsumed=new Set()', 'const typedGeneratedConsumed=new Set()',
  'const workbenchGetOnly=', 'const typedHighRiskActions=new Set(["PII_RAW","SIMULATE","DECIDE"])',
  'canonical BZA permission manifest missing', 'high-risk mutation must call Orval typed generated API directly',
  'operation.method==="GET"', 'expired consumer waiver:'
 ]
}
for rel,tokens in consumer_policies.items():
 consumer_gate=read(rel)
 for token in tokens:
  if token not in consumer_gate: errors.append(f'{rel}: real consumer gate missing {token}')
 route_block=consumer_gate.split('const routeRegistry=',1)[1].split('const waiversPath=',1)[0] if 'const routeRegistry=' in consumer_gate and 'const waiversPath=' in consumer_gate else ''
 if 'else consumed.add(operationId)' in route_block:
  errors.append(f'{rel}: registry-only consumer false green remains')


# Browser release wiring must use the real BZA URL and current Integration Closure labels/permission annotations.
bza_playwright=read('cpf-biz-admin/frontend/playwright.config.ts')
if 'process.env.CPF_BZA_FRONTEND_URL || process.env.CPF_FRONTEND_URL' not in bza_playwright:
 errors.append('BZA Playwright does not prefer CPF_BZA_FRONTEND_URL')
if "CPF_BZA_FRONTEND_URL is required for release browser validation" not in bza_playwright:
 errors.append('BZA Playwright release URL is not fail-closed')
integration_e2e=read('cpf-admin/frontend/e2e/integration-closure-r6.spec.ts')
for token in ['승인 검증 후 단회 실행','toHaveAttribute("title", /권한 없음:','CPF_E2E_AUTH_STATE_READONLY','CPF_E2E_AUTH_STATE_OPERATOR']:
 if token not in integration_e2e: errors.append('Integration Closure real-session role matrix missing '+token)
if 'name: "보정 실행"' in integration_e2e: errors.append('Integration Closure E2E stale execute selector remains')

if errors:
 for e in errors: print('[CPF][R6I][FRONTEND][FAIL]',e)
 raise SystemExit(1)
print(f'[CPF][R6I][FRONTEND][PASS] admRoutes={len(route_ops)} routeBindings={sum(map(len,route_ops.values()))} uniqueOps={len({x for v in route_ops.values() for x in v})} approvalGeneratedConsumers={len(required)} workbenches=GET_ONLY')
