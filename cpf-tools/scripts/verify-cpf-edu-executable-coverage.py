#!/usr/bin/env python3
"""Validate the CPF executable education catalog and Canonical-162 coverage.

Development mode validates the deterministic contract without pretending that runtime
commands were executed. Release mode additionally requires every Source/Test/Public
contract glob to resolve, every runtime row to be verified, and exact-SHA evidence to
exist for each canonical requirement.
"""
from __future__ import annotations
import argparse,csv,glob,json,re
from pathlib import Path

EXPECTED_FEATURE_IDS=[f'EDU-{i:03d}' for i in range(1,33)]
PLACEHOLDER=re.compile(r'^(?:|미수집|미구현|TODO|TBD|N/?A|NONE|-)$',re.I)
FORBIDDEN_IMPORT=re.compile(r'^\s*import\s+com\.cpf\.[\w.]*\.internal(?:\.|;)',re.M)
REQUIRED_AXES={'normal','error','recovery'}
ALLOWED_STATUS={'완료','부분 구현','미구현','미검증','실패','재확인 필요'}

def rows(path:Path):
 with path.open(encoding='utf-8-sig',newline='') as f:return list(csv.DictReader(f))

def nonempty(value): return isinstance(value,str) and not PLACEHOLDER.match(value.strip())

def resolve(root:Path, patterns:list[str]):
 found=[]
 for pattern in patterns:
  found.extend(Path(x) for x in glob.glob(str(root/pattern),recursive=True) if Path(x).is_file())
 return sorted(set(found))

def validate(root:Path,catalog_path:Path,mapping_path:Path,canonical_path:Path,release=False):
 errors=[]
 try: catalog=json.loads(catalog_path.read_text(encoding='utf-8'))
 except Exception as e:return [f'catalog unreadable: {e}']
 features=catalog.get('features') or []
 ids=[x.get('featureId') for x in features]
 if catalog.get('featureCount')!=32:errors.append('catalog featureCount must be 32')
 if catalog.get('canonicalRequirementCount')!=162:errors.append('catalog canonicalRequirementCount must be 162')
 if ids!=EXPECTED_FEATURE_IDS:errors.append(f'feature IDs must be continuous EDU-001..EDU-032: {ids}')
 if len(set(ids))!=len(ids):errors.append('duplicate EDU feature ID')
 commands=[]
 feature_by_id={x.get('featureId'):x for x in features}
 for x in features:
  fid=x.get('featureId','<missing>')
  for field in ['area','coverageScope','ownerModule']:
   if not nonempty(x.get(field)):errors.append(f'{fid}: {field} missing or placeholder')
  for field in ['referenceSources','tests','publicContracts','admObservationPaths','runtimeCommands']:
   values=x.get(field)
   if not isinstance(values,list) or not values or any(not nonempty(v) for v in values):errors.append(f'{fid}: {field} must contain concrete values')
  axes=x.get('scenarioAxes') or {}
  missing=REQUIRED_AXES-set(axes)
  if missing:errors.append(f'{fid}: required scenario axes missing: {sorted(missing)}')
  if len(set(axes.values()))!=len(axes):errors.append(f'{fid}: duplicate scenario IDs')
  if any(not nonempty(v) for v in axes.values()):errors.append(f'{fid}: scenario ID placeholder')
  for route in x.get('admObservationPaths') or []:
   if not route.startswith('/'):errors.append(f'{fid}: ADM observation path must start with /: {route}')
  for command in x.get('runtimeCommands') or []:
   commands.append((command,fid))
  if x.get('developmentStatus') not in ALLOWED_STATUS:errors.append(f'{fid}: invalid developmentStatus')
  if x.get('verificationStatus') not in ALLOWED_STATUS:errors.append(f'{fid}: invalid verificationStatus')
  if release:
   for field in ['referenceSources','tests','publicContracts']:
    matched=resolve(root,x.get(field) or [])
    if not matched:errors.append(f'{fid}: release {field} did not resolve any tracked file')
    if field=='referenceSources':
     for p in matched:
      if p.suffix=='.java' and 'cpf-reference' in p.as_posix():
       text=p.read_text(encoding='utf-8',errors='replace')
       if FORBIDDEN_IMPORT.search(text):errors.append(f'{fid}: reference source imports Internal package: {p.relative_to(root)}')
   if x.get('developmentStatus')!='완료':errors.append(f'{fid}: release developmentStatus must be 완료')
   if x.get('verificationStatus')!='완료':errors.append(f'{fid}: release verificationStatus must be 완료')
 dup={c for c,_ in commands if sum(1 for x,_ in commands if x==c)>1}
 for c in sorted(dup):errors.append(f'duplicate runtime command across EDU features: {c}')
 try: mapping=rows(mapping_path)
 except Exception as e:return errors+[f'mapping unreadable: {e}']
 try: canonical=rows(canonical_path)
 except Exception as e:return errors+[f'canonical matrix unreadable: {e}']
 canonical_ids=[r.get('requirement_id','').strip() for r in canonical]
 mapping_ids=[r.get('requirement_id','').strip() for r in mapping]
 if len(canonical_ids)!=162 or len(set(canonical_ids))!=162:errors.append('canonical matrix must contain 162 unique requirement IDs')
 if len(mapping_ids)!=162 or len(set(mapping_ids))!=162:errors.append('EDU coverage matrix must contain 162 unique requirement IDs')
 missing=sorted(set(canonical_ids)-set(mapping_ids));extra=sorted(set(mapping_ids)-set(canonical_ids))
 if missing:errors.append(f'canonical requirements missing from EDU coverage: {missing}')
 if extra:errors.append(f'unknown requirements in EDU coverage: {extra}')
 for r in mapping:
  rid=r.get('requirement_id','<missing>')
  fids=[v.strip() for v in (r.get('edu_feature_ids') or '').split('|') if v.strip()]
  if not fids:errors.append(f'{rid}: no EDU feature mapping')
  unknown=sorted(set(fids)-set(feature_by_id))
  if unknown:errors.append(f'{rid}: unknown EDU feature IDs: {unknown}')
  for field in ['section','public_contract','reference_source','normal_scenario','error_fault_recovery_scenario','adm_observation_path','runtime_command','evidence']:
   if not nonempty(r.get(field)):errors.append(f'{rid}: {field} missing or placeholder')
  if r.get('coverage_development_status')!='완료':errors.append(f'{rid}: coverage_development_status must be 완료')
  status=r.get('runtime_verification_status')
  if status not in ALLOWED_STATUS:errors.append(f'{rid}: invalid runtime_verification_status')
  expected_scenarios=set();expected_commands=set()
  for fid in fids:
   x=feature_by_id.get(fid)
   if x:
    expected_scenarios.update(x.get('scenarioAxes',{}).values());expected_commands.update(x.get('runtimeCommands',[]))
  supplied_scenarios=set(v.strip() for field in ['normal_scenario','error_fault_recovery_scenario'] for v in (r.get(field) or '').split('|') if v.strip())
  if expected_scenarios and not supplied_scenarios.issubset(expected_scenarios):errors.append(f'{rid}: scenario references are not owned by mapped EDU features')
  supplied_commands=set(v.strip() for v in (r.get('runtime_command') or '').split('|') if v.strip())
  if expected_commands and not supplied_commands.issubset(expected_commands):errors.append(f'{rid}: runtime command not owned by mapped EDU features')
  if release:
   if status!='완료':errors.append(f'{rid}: release runtime_verification_status must be 완료')
   evidence=(root/(r.get('evidence') or '')).resolve()
   try:evidence.relative_to(root.resolve())
   except ValueError:errors.append(f'{rid}: evidence escapes repository root');continue
   if not evidence.is_file():errors.append(f'{rid}: exact-SHA EDU evidence missing: {r.get("evidence")}')
   else:
    try:data=json.loads(evidence.read_text(encoding='utf-8'))
    except Exception as e:errors.append(f'{rid}: evidence unreadable: {e}');continue
    if data.get('requirementId')!=rid:errors.append(f'{rid}: evidence requirementId mismatch')
    if not re.fullmatch(r'[0-9a-f]{40}',str(data.get('sourceSha',''))):errors.append(f'{rid}: evidence sourceSha must be exact 40-char SHA')
    if data.get('exitCode')!=0:errors.append(f'{rid}: evidence exitCode must be 0')
    if not data.get('sanitized'):errors.append(f'{rid}: evidence must be sanitized')
 return errors

def main():
 p=argparse.ArgumentParser();p.add_argument('--root',type=Path,default=Path.cwd());p.add_argument('--catalog',type=Path);p.add_argument('--mapping',type=Path);p.add_argument('--canonical',type=Path);p.add_argument('--release',action='store_true');a=p.parse_args()
 root=a.root.resolve();catalog=a.catalog or root/'cpf-tools/governance/cpf-edu-executable-catalog.json';mapping=a.mapping or root/'cpf-docs/quality/CPF_20260801_QA36_EDU_EXECUTABLE_COVERAGE_MATRIX.csv';canonical=a.canonical or root/'cpf-docs/quality/CPF_20260801_QA36_CANONICAL_162_REQUIREMENT_MATRIX.csv'
 errors=validate(root,catalog,mapping,canonical,a.release)
 for e in errors:print('[FAIL]',e)
 if errors:return 1
 print(f'[PASS] CPF EDU executable coverage features=32 canonicalRequirements=162 release={a.release}')
 return 0
if __name__=='__main__':raise SystemExit(main())
