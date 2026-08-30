#!/usr/bin/env python3
from pathlib import Path
import csv,json,re,sys
ROOT=Path(__file__).resolve().parents[4];H=ROOT/'cpf-docs/governance/development-harness';C=H/'current'
def rows(p):
 with p.open(encoding='utf-8-sig',newline='') as f:return list(csv.DictReader(f))
def load(p):return json.loads(p.read_text(encoding='utf-8'))
e=[];b=load(H/'contracts/harness-strength-baseline.json');c=load(H/'contracts/contract-registry.json')
canon=rows(C/'CANONICAL_PRODUCT_REQUIREMENTS.csv');work=rows(C/'CURRENT_WORK_ITEM_REGISTRY.csv');ctrl=rows(H/'contracts/harness-control-registry.csv')
tracking=[r for r in work if r.get('item_role','TRACKING')=='TRACKING'];execs=[r for r in work if r.get('item_role')=='ROOT_CAUSE_EXECUTION']
if len(canon)<b['minimumCanonicalRequirementCount']:e.append(f'REQUIREMENT_COVERAGE_REDUCED {len(canon)}<{b["minimumCanonicalRequirementCount"]}')
if len(tracking)<b['minimumTrackingWorkCount']:e.append(f'TRACKING_WORK_REDUCED {len(tracking)}<{b["minimumTrackingWorkCount"]}')
if set(b['requiredExecutionWorkIds'])- {r['work_item_id'] for r in execs}:e.append('ROOT_CAUSE_EXECUTION_WP_MISSING')
if len(ctrl)<b['minimumControlCount'] or set(b['requiredControlIds'])- {r['control_id'] for r in ctrl}:e.append('CONTROL_ENFORCEMENT_REDUCED')
for k in ['requiredRoles','requiredDbVendors','requiredCompletionEvidence','requiredTestEvidence','requiredIndependentReviewerSourceModificationEvidence','mandatoryRuntimePlatforms','forbiddenCompletionSignals']:
 actual={'requiredRoles':sorted(c['roles']),'requiredDbVendors':sorted(c['officialDbVendors']),'requiredCompletionEvidence':sorted(c['requiredCompletionEvidence']),'requiredTestEvidence':sorted(c['requiredTestEvidence']),'requiredIndependentReviewerSourceModificationEvidence':sorted(c['independentReviewerSourceModificationEvidence']),'mandatoryRuntimePlatforms':sorted(c['mandatoryRuntimePlatforms']),'forbiddenCompletionSignals':sorted(c['forbiddenCompletionSignals'])}[k]
 if set(b[k])-set(actual):e.append('HARNESS_STRENGTH_REDUCED '+k)
for fn,key in [('CPF_REQUIREMENT_MASTER.csv','minimumDetailedRequirementCount'),('CPF_SCENARIO_MASTER.csv','minimumScenarioCount'),('CPF_EXECUTION_SEQUENCE.csv','minimumExecutionSequenceCount')]:
 rs=rows(C/fn); n=int(rs[0]['logical_record_count']) if rs else 0
 if n<b[key]:e.append(f'DATASET_SCOPE_REDUCED {fn} {n}<{b[key]}')
# Negative Mutation / False Green protection count is itself a non-regression contract.
neg_path=H/'tests/test_negative_fixtures.py'
if not neg_path.is_file():
 e.append('NEGATIVE_MUTATION_SUITE_MISSING')
else:
 neg_text=neg_path.read_text(encoding='utf-8')
 base_checks=len(re.findall(r"\brecord\(\s*['\"]",neg_text))
 mutation_checks=len(re.findall(r"\brun_(?:mut|auth_mut|strength_mut)\(\s*['\"]",neg_text))
 negative_count=base_checks+mutation_checks
 if negative_count<b.get('minimumNegativeMutationCount',0):
  e.append(f'NEGATIVE_MUTATION_REDUCED {negative_count}<{b.get("minimumNegativeMutationCount",0)}')

# Capability-first host toolchain policy is a Common Rule and may not regress to exact host patch pins.
rule_text=(H/'standards/CPF_RULE_MODEL_AND_IMPACT_SEARCH_STANDARD.md').read_text(encoding='utf-8')
if 'CR-22' not in rule_text or 'Capability-first Toolchain Compatibility' not in rule_text:
 e.append('CAPABILITY_FIRST_COMMON_RULE_MISSING')
policy_path=ROOT/'cpf-tools/verification/contracts/cpf-toolchain-compatibility.json'
if not policy_path.is_file():
 e.append('TOOLCHAIN_COMPATIBILITY_POLICY_MISSING')
else:
 policy=load(policy_path)
 if policy.get('policy')!='CAPABILITY_FIRST' or (policy.get('principles') or {}).get('hostExactPatchPinForbidden') is not True or (policy.get('principles') or {}).get('hostMinorPatchGateForbidden') is not True or (policy.get('principles') or {}).get('capabilityProbePrecedesVersionRejection') is not True:
  e.append('TOOLCHAIN_COMPATIBILITY_POLICY_WEAKENED')
 for name,spec in (policy.get('tools') or {}).items():
  if spec.get('exactPatchRequired') is True:
   e.append('HOST_EXACT_PATCH_PIN_REINTRODUCED '+name)
 java=(policy.get('tools') or {}).get('java') or {}
 if java.get('maxMajor') is not None or java.get('hardMinMajor') is not None or java.get('enforcement')!='CAPABILITY_FIRST_RELEASE_25':
  e.append('HOST_JAVA_EXACT_MAJOR_PIN_REINTRODUCED')

# tracking -> execution exact mechanical linkage; 193 canonical umbrellas retained.
execids={r['work_item_id'] for r in execs}
for r in tracking:
 xs=[x for x in r.get('execution_wp_ids','').split(';') if x]
 if not xs:e.append('TRACKING_WITHOUT_EXECUTION '+r['work_item_id'])
 for x in xs:
  if x not in execids:e.append('TRACKING_EXECUTION_ORPHAN '+r['work_item_id']+' '+x)
umb=[r for r in tracking if r.get('source_type')=='CANONICAL_COVERAGE_UMBRELLA']
if len(umb)<193:e.append(f'CANONICAL_UMBRELLA_REDUCED {len(umb)}<193')
# Handover aliases must resolve to actual tracking/root execution mapping.
text=(C/'CPF_DEVELOPMENT_HANDOVER.md').read_text(encoding='utf-8')
aliases=set(re.findall(r'WP-R(?:01\.21|03\.15|07\.17)',text)); mapped={a for r in work for a in r.get('handover_aliases','').split(';') if a}
if aliases-mapped:e.append('HANDOVER_REGISTRY_ALIAS_ORPHAN '+','.join(sorted(aliases-mapped)))
if e:
 for x in e[:200]:print('FAIL',x)
 print('HARNESS_STRENGTH_REGRESSION=FAIL ERRORS='+str(len(e)));raise SystemExit(1)
print(f'HARNESS_STRENGTH_REGRESSION=PASS CANONICAL={len(canon)} TRACKING={len(tracking)} EXECUTION={len(execs)} CONTROLS={len(ctrl)}')
