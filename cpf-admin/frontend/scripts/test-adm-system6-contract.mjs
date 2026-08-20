import fs from 'node:fs';
import assert from 'node:assert/strict';
const read=(p)=>fs.readFileSync(new URL(`../${p}`,import.meta.url),'utf8');
const state=read('src/state/createAdmState.ts');
const logs=read('src/features/logs/LogsPage.vue');
const groups=read('src/features/transaction-groups/TransactionGroupsPage.vue');
const timeline=fs.readFileSync(new URL('../../../cpf-starters/platform-operations/observability/src/main/java/com/cpf/platform/operations/observability/internal/logging/segment/CpfTransactionTimelineQueryFacade.java',import.meta.url),'utf8');
for(const key of ['originalSystemCode','systemCode','callerSystemCode','targetSystemCode','targetOperationId']) assert.match(state,new RegExp(`${key}: \\\"\\\"`));
for(const label of ['Original System','Current System','Caller System','Target System','Target Operation']) { assert.match(logs,new RegExp(label)); assert.match(groups,new RegExp(label)); }
for(const col of ['original_system_code','system_code','caller_system_code','target_system_code','target_operation_id']) assert.match(timeline,new RegExp(col));
assert.match(logs,/Channel Context \(Ingress\/Security\)/);
assert.match(groups,/Channel Context \(Ingress\/Security\)/);
assert.doesNotMatch(logs,/<th>Channel 흐름<\/th>/);
console.log('[CPF][ADM][PASS] System6 is the primary transaction/log identity; Channel remains secondary ingress/security context');
