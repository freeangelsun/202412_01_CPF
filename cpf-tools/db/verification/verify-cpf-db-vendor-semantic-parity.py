#!/usr/bin/env python3
"""Fail-closed semantic/dialect/lifecycle parity gate for CPF official DB vendors.

The gate validates generated Source and Install DDL against the canonical platform-schema.json,
including tables, ordered columns, PK, UK, indexes and FK references. It also scans every SQL
file in Source/Install/Migration/Rollback/Verify for vendor-incompatible tokens and empty stubs.
"""
from __future__ import annotations
import argparse, hashlib, json, re, subprocess, sys
from pathlib import Path
from typing import Iterable

VENDORS=("mariadb","postgresql","oracle")
LIFECYCLE=("source","install","migration","rollback","verify")
FORBIDDEN={
 "mariadb":(r"\bBYTEA\b",r"\bVARCHAR2\b",r"\bNUMBER\s*\(",r"\bCLOB\b",r"\bGENERATED\s+BY\s+DEFAULT\s+AS\s+IDENTITY\b"),
 "postgresql":(r"\bLONGBLOB\b",r"\bMEDIUMTEXT\b",r"\bVARCHAR2\b",r"\bNUMBER\s*\(",r"\bAUTO_INCREMENT\b",r"\bENGINE\s*=",r"\bREGEXP\b"),
 "oracle":(r"\bLONGBLOB\b",r"\bMEDIUMTEXT\b",r"\bBYTEA\b",r"\bAUTO_INCREMENT\b",r"\bENGINE\s*=",r"\bLIMIT\s+\d+",r"\bSERIAL\b",r"\bREGEXP\b",r"\bRIGHT\s*\("),
}
EXECUTABLE=re.compile(r"(?i)\b(CREATE|ALTER|DROP|INSERT|UPDATE|DELETE|SELECT|BEGIN|DECLARE|COMMENT|GRANT|REVOKE|SET|MERGE|CALL)\b")
CREATE_TABLE_START=re.compile(r"(?is)\bCREATE\s+TABLE\s+(?:IF\s+NOT\s+EXISTS\s+)?((?:[\w\"`$#]+\.)?[\w\"`$#]+)\s*\(")
CREATE_INDEX=re.compile(r"(?is)\bCREATE\s+(UNIQUE\s+)?INDEX\s+([\w\"`$#]+)\s+ON\s+((?:[\w\"`$#]+\.)?[\w\"`$#]+)\s*\((.*?)\)\s*;")
ALTER_CONSTRAINT=re.compile(r"(?is)\bALTER\s+TABLE\s+((?:[\w\"`$#]+\.)?[\w\"`$#]+)\s+ADD\s+(CONSTRAINT\s+[\w\"`$#]+\s+.*?)\s*;")
# Current source schema is derived from canonical platform-schema.json. QA39 was
# absorbed into that canonical schema; including its legacy compatibility DDL here
# double-counts the same tables and creates a false duplicate.
SCHEMA_SOURCE_NAMES={"10_cpf_schema.sql","40_business_modules_schema.sql"}

class GateError(RuntimeError): pass

def sha(p:Path)->str:
 h=hashlib.sha256()
 with p.open('rb') as f:
  for b in iter(lambda:f.read(1024*1024),b''):h.update(b)
 return h.hexdigest()

def ident(value:str)->str:
 value=value.strip().split('.')[-1].strip().strip('"`')
 return value.lower()

def idents(value:str)->list[str]:
 result=[]
 for part in split_top_level(value, ','):
  token=part.strip()
  if not token: continue
  # index order, Oracle quoted names, and prefix lengths are normalized to canonical column names.
  m=re.match(r'(?is)^([\w\"`$#]+)',token)
  if m: result.append(ident(m.group(1)))
 return result

def split_top_level(text:str, delimiter:str=',')->list[str]:
 result=[];start=0;depth=0;i=0;quote=None;line_comment=False;block_comment=False
 while i<len(text):
  c=text[i];n=text[i+1] if i+1<len(text) else ''
  if line_comment:
   if c=='\n': line_comment=False
   i+=1;continue
  if block_comment:
   if c=='*' and n=='/': block_comment=False;i+=2;continue
   i+=1;continue
  if quote:
   if c==quote:
    if n==quote: i+=2;continue
    quote=None
   i+=1;continue
  if c=='-' and n=='-': line_comment=True;i+=2;continue
  if c=='/' and n=='*': block_comment=True;i+=2;continue
  if c in ("'",'"','`'): quote=c;i+=1;continue
  if c=='(': depth+=1
  elif c==')': depth=max(0,depth-1)
  elif c==delimiter and depth==0:
   result.append(text[start:i]);start=i+1
  i+=1
 result.append(text[start:])
 return result

def matching_paren(text:str, open_pos:int)->int:
 depth=0;i=open_pos;quote=None;line_comment=False;block_comment=False
 while i<len(text):
  c=text[i];n=text[i+1] if i+1<len(text) else ''
  if line_comment:
   if c=='\n':line_comment=False
   i+=1;continue
  if block_comment:
   if c=='*' and n=='/':block_comment=False;i+=2;continue
   i+=1;continue
  if quote:
   if c==quote:
    if n==quote:i+=2;continue
    quote=None
   i+=1;continue
  if c=='-' and n=='-':line_comment=True;i+=2;continue
  if c=='/' and n=='*':block_comment=True;i+=2;continue
  if c in ("'",'"','`'):quote=c;i+=1;continue
  if c=='(':depth+=1
  elif c==')':
   depth-=1
   if depth==0:return i
  i+=1
 raise GateError(f"unclosed CREATE TABLE parenthesis at offset {open_pos}")

def blank_table()->dict:
 return {"columns":[],"primaryKey":[],"uniqueKeys":{},"indexes":{},"foreignKeys":{}}

def add_constraint(table:dict,item:str)->bool:
 s=item.strip().rstrip(',')
 cm=re.match(r'(?is)^CONSTRAINT\s+([\w\"`$#]+)\s+(.*)$',s)
 name=None;body=s
 if cm:name=ident(cm.group(1));body=cm.group(2).strip()
 pm=re.match(r'(?is)^PRIMARY\s+KEY\s*\((.*?)\)',body)
 if pm:table['primaryKey']=idents(pm.group(1));return True
 uim=re.match(r'(?is)^UNIQUE\s+(?:INDEX|KEY)\s+([\w\"`$#]+)\s*\((.*)\)\s*$',body)
 if uim:
  table['indexes'][ident(uim.group(1))]={"columns":idents(uim.group(2)),"unique":True};return True
 um=re.match(r'(?is)^UNIQUE(?:\s+KEY)?(?:\s+[\w\"`$#]+)?\s*\((.*?)\)',body)
 if um:
  key=name or f"__unique_{len(table['uniqueKeys'])}"
  table['uniqueKeys'][key]=idents(um.group(1));return True
 fm=re.match(r'(?is)^FOREIGN\s+KEY\s*\((.*?)\)\s+REFERENCES\s+((?:[\w\"`$#]+\.)?[\w\"`$#]+)\s*\((.*?)\)',body)
 if fm:
  key=name or f"__foreign_{len(table['foreignKeys'])}"
  table['foreignKeys'][key]={"columns":idents(fm.group(1)),"refTable":ident(fm.group(2)),"refColumns":idents(fm.group(3))}
  return True
 im=re.match(r'(?is)^(?:INDEX|KEY)\s+([\w\"`$#]+)\s*\((.*)\)\s*$',body)
 if im:table['indexes'][ident(im.group(1))]={"columns":idents(im.group(2)),"unique":False};return True
 return bool(re.match(r'(?is)^(CHECK|EXCLUDE)\b',body))

def parse_schema_text(text:str)->dict[str,dict]:
 tables={};pos=0
 while True:
  m=CREATE_TABLE_START.search(text,pos)
  if not m:break
  table_name=ident(m.group(1));open_pos=m.end()-1;close_pos=matching_paren(text,open_pos)
  if table_name in tables:raise GateError(f"duplicate CREATE TABLE: {table_name}")
  table=blank_table();body=text[open_pos+1:close_pos]
  for raw in split_top_level(body):
   item=raw.strip()
   if not item:continue
   if add_constraint(table,item):continue
   if re.match(r'(?is)^(PRIMARY|UNIQUE|FOREIGN|CHECK|INDEX|KEY|CONSTRAINT)\b',item):
    raise GateError(f"unparsed table constraint table={table_name}: {item[:120]}")
   cm=re.match(r'(?is)^([\w\"`$#]+)\s+',item)
   if cm:table['columns'].append(ident(cm.group(1)))
   else:raise GateError(f"unparsed column table={table_name}: {item[:120]}")
  tables[table_name]=table;pos=close_pos+1
 # External indexes are normalized into the table inventory.
 for m in CREATE_INDEX.finditer(text):
  unique=bool(m.group(1));name=ident(m.group(2));table_name=ident(m.group(3));cols=idents(m.group(4))
  if table_name not in tables:raise GateError(f"index references unknown table: {name}->{table_name}")
  tables[table_name]['indexes'][name]={"columns":cols,"unique":unique}
 # Support vendors that add constraints after CREATE TABLE.
 for m in ALTER_CONSTRAINT.finditer(text):
  table_name=ident(m.group(1))
  if table_name in tables:add_constraint(tables[table_name],m.group(2))
 return tables

def canonical_inventory(path:Path, production_only:bool=False)->dict[str,dict]:
 raw=json.loads(path.read_text(encoding='utf-8-sig'))
 if raw.get('tableCount')!=len(raw.get('tables',[])):raise GateError('canonical tableCount mismatch')
 result={}
 for t in raw['tables']:
  if production_only and not bool(t.get('productionDefault', True)):
   continue
  name=ident(t['name'])
  if name in result:raise GateError(f"duplicate canonical table: {name}")
  result[name]={
   'columns':[ident(c['name']) for c in t.get('columns',[])],
   'primaryKey':[ident(c) for c in t.get('primaryKey',[])],
   'uniqueKeys':{ident(k['name']):[ident(c) for c in k.get('columns',[])] for k in t.get('uniqueKeys',[])},
   'indexes':{ident(k['name']):{"columns":[idents(str(c))[0] if idents(str(c)) else ident(str(c)) for c in k.get('columns',[])],"unique":bool(k.get('unique',False))} for k in t.get('indexes',[])},
   'foreignKeys':{ident(k['name']):{'columns':[ident(c) for c in k.get('columns',[])],'refTable':ident(k['refTable']),'refColumns':[ident(c) for c in k.get('refColumns',[])]} for k in t.get('foreignKeys',[])},
  }
 return result

def sql_files(root:Path,vendor:str,kind:str)->list[Path]:
 d=root/'cpf-tools/db/vendor'/vendor/kind
 if not d.is_dir():raise GateError(f"missing lifecycle directory: {d.relative_to(root)}")
 return sorted(p for p in d.rglob('*.sql') if p.is_file())

def compare_inventory(expected:dict[str,dict],actual:dict[str,dict],vendor:str,label:str)->list[str]:
 findings=[]
 missing=sorted(set(expected)-set(actual));extra=sorted(set(actual)-set(expected))
 if missing:findings.append(f"{vendor}/{label}: missing tables count={len(missing)} sample={missing[:20]}")
 if extra:findings.append(f"{vendor}/{label}: extra tables count={len(extra)} sample={extra[:20]}")
 for name in sorted(set(expected)&set(actual)):
  e=expected[name];a=actual[name]
  for field in ('columns','primaryKey'):
   if e[field]!=a[field]:findings.append(f"{vendor}/{label}/{name}: {field} mismatch expected={e[field]} actual={a[field]}")
  for field in ('uniqueKeys','indexes','foreignKeys'):
   if e[field]!=a[field]:
    em=sorted(set(e[field])-set(a[field]));ex=sorted(set(a[field])-set(e[field]));changed=sorted(k for k in set(e[field])&set(a[field]) if e[field][k]!=a[field][k])
    findings.append(f"{vendor}/{label}/{name}: {field} mismatch missing={em[:10]} extra={ex[:10]} changed={changed[:10]}")
 return findings

def verify_schema_inventory(canonical_path:Path,vendor_source_root:Path)->dict:
 expected=canonical_inventory(canonical_path);findings=[];counts={}
 for vendor in VENDORS:
  source_dir=vendor_source_root/vendor/'source'
  files=sorted(p for p in source_dir.glob('*.sql') if p.name in SCHEMA_SOURCE_NAMES)
  if not files:findings.append(f"{vendor}: generated schema sources missing");continue
  actual=parse_schema_text('\n'.join(p.read_text(encoding='utf-8-sig') for p in files))
  counts[vendor]={"tables":len(actual),"columns":sum(len(t['columns']) for t in actual.values()),"indexes":sum(len(t['indexes']) for t in actual.values()),"uniqueKeys":sum(len(t['uniqueKeys']) for t in actual.values()),"foreignKeys":sum(len(t['foreignKeys']) for t in actual.values())}
  findings.extend(compare_inventory(expected,actual,vendor,'source'))
 return {"status":"PASS" if not findings else "FAIL","canonicalTables":len(expected),"objectCounts":counts,"findings":findings}


def normalize_sql(value:str)->str:
 return re.sub(r"\s+"," ",value.strip()).upper()

def canonical_check(raw:dict,table_name:str,constraint_name:str)->str|None:
 for table in raw.get('tables',[]):
  if ident(str(table.get('name','')))!=ident(table_name):continue
  for check in table.get('checks',[]):
   if ident(str(check.get('name','')))==ident(constraint_name):return str(check.get('expression',''))
 return None

def last_control_status_check(text:str)->str|None:
 matches=re.findall(r"(?is)CHECK\s*\(\s*control_status\s+IN\s*\((.*?)\)\s*\)",text)
 return matches[-1] if matches else None

def verify_batch_abandon_lifecycle(root:Path,canonical_raw:dict)->list[str]:
 """Verify the two-phase ABANDONING state across canonical, fresh install, upgrade and rollback.

 This is deliberately fail-closed because general table inventory parsing ignores CHECK expressions.
 """
 findings=[]
 expression=canonical_check(canonical_raw,'cpf_batch_execution_control','ck_cpf_bat_control_status')
 if expression is None:return findings
 if 'ABANDONING' not in normalize_sql(expression):
  findings.append('canonical cpf_batch_execution_control: ABANDONING missing from ck_cpf_bat_control_status')
 for vendor in VENDORS:
  base=root/'cpf-tools/db/vendor'/vendor
  for kind,path in (('source',base/'source/35_bat_schema.sql'),('install',base/'install/00_empty_install.sql')):
   if not path.is_file():findings.append(f"{vendor}/{kind}: BAT schema missing");continue
   check=last_control_status_check(path.read_text(encoding='utf-8-sig'))
   if check is None:findings.append(f"{path.relative_to(root)}: ck_cpf_bat_control_status missing")
   elif 'ABANDONING' not in normalize_sql(check):findings.append(f"{path.relative_to(root)}: ABANDONING missing")
  migrations=list((base/'migration').rglob('V99__bat_abandon_two_phase_state.sql'))
  if len(migrations)!=1:findings.append(f"{vendor}/migration: expected exactly one V99 BAT abandon migration, found {len(migrations)}")
  else:
   mtext=migrations[0].read_text(encoding='utf-8-sig');mcheck=last_control_status_check(mtext)
   if mcheck is None or 'ABANDONING' not in normalize_sql(mcheck):findings.append(f"{migrations[0].relative_to(root)}: migration does not add ABANDONING")
   if 'DROP CONSTRAINT' not in normalize_sql(mtext):findings.append(f"{migrations[0].relative_to(root)}: prior constraint is not replaced")
  rollback=base/'rollback/R99__bat_abandon_two_phase_state.sql'
  if not rollback.is_file():findings.append(f"{vendor}/rollback: R99 BAT abandon rollback missing")
  else:
   rtext=rollback.read_text(encoding='utf-8-sig');rnorm=normalize_sql(rtext);rcheck=last_control_status_check(rtext)
   if 'ABANDONING' not in rnorm:findings.append(f"{rollback.relative_to(root)}: rollback guard does not inspect ABANDONING")
   if vendor=='postgresql' and 'RAISE EXCEPTION' not in rnorm:findings.append(f"{rollback.relative_to(root)}: PostgreSQL fail-closed guard missing")
   if vendor=='oracle' and 'RAISE_APPLICATION_ERROR' not in rnorm:findings.append(f"{rollback.relative_to(root)}: Oracle fail-closed guard missing")
   if vendor=='mariadb' and 'SIGNAL SQLSTATE' not in rnorm:findings.append(f"{rollback.relative_to(root)}: MariaDB fail-closed guard missing")
   if rcheck is None:findings.append(f"{rollback.relative_to(root)}: rollback check constraint missing")
   elif 'ABANDONING' in normalize_sql(rcheck):findings.append(f"{rollback.relative_to(root)}: rollback constraint still allows ABANDONING")
  verify_sql=base/'verify/V99__bat_abandon_two_phase_state.sql'
  if not verify_sql.is_file():findings.append(f"{vendor}/verify: V99 BAT abandon verification missing")
  else:
   vnorm=normalize_sql(verify_sql.read_text(encoding='utf-8-sig'))
   if 'ABANDONING' not in vnorm:findings.append(f"{verify_sql.relative_to(root)}: verification does not inspect ABANDONING")
   if vendor=='postgresql' and 'RAISE EXCEPTION' not in vnorm:findings.append(f"{verify_sql.relative_to(root)}: PostgreSQL fail-closed verification missing")
   if vendor=='oracle' and 'RAISE_APPLICATION_ERROR' not in vnorm:findings.append(f"{verify_sql.relative_to(root)}: Oracle fail-closed verification missing")
   if vendor=='mariadb' and 'CPF-BAT-V99-VERIFY-FAILED' not in vnorm:findings.append(f"{verify_sql.relative_to(root)}: MariaDB fail-closed verification marker missing")
 return findings


def verify_migration_integrity(root:Path,vendor:str)->list[str]:
 findings=[];migration_root=root/'cpf-tools/db/vendor'/vendor/'migration'
 if not migration_root.is_dir():return [f"{vendor}/migration: directory missing"]
 for directory in sorted({p.parent for p in migration_root.rglob('*.sql')}):
  sqls=sorted(directory.glob('*.sql'))
  versions={}
  for sql in sqls:
   m=re.fullmatch(r'V(\d+(?:[._]\d+)*)__([A-Za-z0-9_\-]+)\.sql',sql.name,re.I)
   if not m:
    findings.append(f"{sql.relative_to(root)}: Flyway versioned filename 형식 오류")
    continue
   version=m.group(1).replace('_','.')
   if version in versions:findings.append(f"{sql.relative_to(root)}: duplicate Flyway version {version} with {versions[version]}")
   versions[version]=sql.name
   sidecar=sql.with_name(sql.name+'.sha256')
   if sidecar.is_file():
    token=sidecar.read_text(encoding='utf-8-sig').strip().split()[0] if sidecar.read_text(encoding='utf-8-sig').strip() else ''
    if token.lower()!=sha(sql):findings.append(f"{sidecar.relative_to(root)}: sidecar checksum mismatch")
  checksum=directory/'checksums.sha256'
  if checksum.is_file():
   entries={}
   for n,line in enumerate(checksum.read_text(encoding='utf-8-sig').splitlines(),1):
    line=line.strip()
    if not line or line.startswith('#'):continue
    parts=line.split()
    if len(parts)<2 or not re.fullmatch(r'[0-9a-fA-F]{64}',parts[0]):findings.append(f"{checksum.relative_to(root)}:{n}: checksum line 형식 오류");continue
    entries[Path(parts[-1].lstrip('*')).name]=parts[0].lower()
   for sql in sqls:
    if sql.name not in entries:findings.append(f"{checksum.relative_to(root)}: missing entry {sql.name}")
    elif entries[sql.name]!=sha(sql):findings.append(f"{checksum.relative_to(root)}: hash mismatch {sql.name}")
 return findings

def verify(root:Path)->dict:
 findings=[];inventory={}
 canonical=root/'cpf-tools/db/canonical/platform-schema.json';canonical_raw={}
 if not canonical.is_file():findings.append('canonical platform-schema.json missing');expected={}
 else:
  try:
   canonical_raw=json.loads(canonical.read_text(encoding='utf-8-sig'))
   expected=canonical_inventory(canonical)
  except (GateError,json.JSONDecodeError) as ex:findings.append(str(ex));expected={};canonical_raw={}

 production_expected=canonical_inventory(canonical, production_only=True) if expected else {}

 # Canonical DB v5 current-state contract: generated/current is the authoritative
 # cross-vendor schema snapshot. Historical source/install trees are migration
 # history and are validated by verify_migration_lifecycle.py instead of being
 # compared byte-for-byte with the current canonical schema.
 manifest_path=root/'cpf-tools/db/vendor-pack-manifest.json'
 if expected and manifest_path.is_file():
  try: manifest=json.loads(manifest_path.read_text(encoding='utf-8-sig'))
  except json.JSONDecodeError: manifest={}
  if manifest.get('currentSnapshotAuthority')=='CANONICAL_JSON_RENDERER':
   for vendor in VENDORS:
    current=root/'cpf-tools/db/generated/current'/vendor
    inventory[vendor]={'generatedCurrent':[]}
    schema_files=[current/'cpf-platform-schema.sql',current/'backoffice-schema.sql',current/'reference-fixture-schema.sql']
    for f in schema_files:
     if not f.is_file(): findings.append(f'{vendor}/generated-current: missing {f.name}'); continue
     text=f.read_text(encoding='utf-8-sig')
     if not text.strip() or not EXECUTABLE.search(text): findings.append(f'{f.relative_to(root)}: empty/non-executable SQL')
     for pattern in FORBIDDEN[vendor]:
      if re.search(pattern,text,re.I): findings.append(f'{f.relative_to(root)}: forbidden vendor token {pattern}')
     inventory[vendor]['generatedCurrent'].append({'path':f.relative_to(root).as_posix(),'bytes':f.stat().st_size,'sha256':sha(f)})
    if all(f.is_file() for f in schema_files):
     try:
      actual=parse_schema_text('\n'.join(f.read_text(encoding='utf-8-sig') for f in schema_files))
      findings.extend(compare_inventory(expected,actual,vendor,'generated-current'))
     except GateError as ex: findings.append(f'{vendor}/generated-current parse: {ex}')
   # Fresh/source/install are executable current lifecycle surfaces, not immutable history.
   # Validate them against the same canonical inventory and reject stale Channel policy seed contracts.
   for vendor in VENDORS:
    base=root/'cpf-tools/db/vendor'/vendor
    source_files=[base/'source'/name for name in SCHEMA_SOURCE_NAMES if (base/'source'/name).is_file()]
    if source_files:
     try:
      actual=parse_schema_text('\n'.join(f.read_text(encoding='utf-8-sig') for f in source_files))
      findings.extend(compare_inventory(production_expected,actual,vendor,'source-current'))
     except GateError as ex: findings.append(f'{vendor}/source-current parse: {ex}')
    install_file=base/'install/00_empty_install.sql'
    if install_file.is_file():
     text=install_file.read_text(encoding='utf-8-sig')
     for pattern in FORBIDDEN[vendor]:
      if re.search(pattern,text,re.I): findings.append(f'{install_file.relative_to(root)}: forbidden vendor token {pattern}')
     try:
      installed=parse_schema_text(text)
      findings.extend(compare_inventory(production_expected,installed,vendor,'install-current'))
     except GateError as ex: findings.append(f'{vendor}/install-current parse: {ex}')
    stale_policy=re.compile(r'(?is)OPS_CHANNEL_EXECUTION_POLICY\s*\([^;]{0,1200}?(standard_execution_id|original_channel_code|caller_channel_code|request_type)')
    for seed in (base/'source/50_framework_seed_data.sql',base/'source/00_product_seed.sql',base/'seed/00_product_seed.sql'):
     if seed.is_file() and stale_policy.search(seed.read_text(encoding='utf-8-sig')):
      findings.append(f'{seed.relative_to(root)}: stale Channel policy seed contract')

   renderer=root/'cpf-tools/db/render_vendor_pack.py'
   if not renderer.is_file(): findings.append('canonical renderer missing')
   else:
    cp=subprocess.run([sys.executable,str(renderer),'--root',str(root),'--check'],text=True,capture_output=True)
    if cp.returncode: findings.append('canonical generated-current drift: '+(cp.stdout+cp.stderr).strip())
   lifecycle=root/'cpf-tools/db/verify_migration_lifecycle.py'
   if not lifecycle.is_file(): findings.append('migration lifecycle verifier missing')
   else:
    cp=subprocess.run([sys.executable,str(lifecycle),'--root',str(root),'--source-sha','a'*40],text=True,capture_output=True)
    if cp.returncode: findings.append('migration lifecycle contract failed: '+(cp.stdout+cp.stderr).strip())
   result={'status':'PASS' if not findings else 'FAIL','officialVendors':list(VENDORS),'canonicalTableCount':len(expected),'lifecycle':inventory,'findings':findings,'currentSnapshotAuthority':'CANONICAL_JSON_RENDERER'}
   if findings: raise GateError(json.dumps(result,ensure_ascii=False,indent=2))
   return result
 for vendor in VENDORS:
  inventory[vendor]={}
  for kind in LIFECYCLE:
   try:files=sql_files(root,vendor,kind)
   except GateError as ex:findings.append(str(ex));files=[]
   inventory[vendor][kind]=[]
   if not files:findings.append(f"{vendor}/{kind}: no SQL files")
   for p in files:
    text=p.read_text(encoding='utf-8-sig')
    if not text.strip() or not EXECUTABLE.search(text):findings.append(f"{p.relative_to(root)}: empty/non-executable SQL")
    for pattern in FORBIDDEN[vendor]:
     if re.search(pattern,text,re.I):findings.append(f"{p.relative_to(root)}: forbidden vendor token {pattern}")
    inventory[vendor][kind].append({"path":p.relative_to(root).as_posix(),"bytes":p.stat().st_size,"sha256":sha(p)})
  findings.extend(verify_migration_integrity(root,vendor))
  if expected:
   schema_files=[p for p in sql_files(root,vendor,'source') if p.name in SCHEMA_SOURCE_NAMES]
   try:actual=parse_schema_text('\n'.join(p.read_text(encoding='utf-8-sig') for p in schema_files));findings.extend(compare_inventory(expected,actual,vendor,'source'))
   except GateError as ex:findings.append(f"{vendor}/source parse: {ex}")
   install_files=sql_files(root,vendor,'install')
   try:
    installed=parse_schema_text('\n'.join(p.read_text(encoding='utf-8-sig') for p in install_files))
    if installed:findings.extend(compare_inventory(expected,installed,vendor,'install'))
    else:findings.append(f"{vendor}/install: no CREATE TABLE inventory")
   except GateError as ex:findings.append(f"{vendor}/install parse: {ex}")
 findings.extend(verify_batch_abandon_lifecycle(root,canonical_raw))
 gen=root/'cpf-tools/db/generator/generate-official-db-vendor-source.ps1'
 if not gen.is_file():findings.append('generator missing')
 else:
  t=gen.read_text(encoding='utf-8-sig')
  for token in ("'^LONGBLOB$','BYTEA'","'^MEDIUMTEXT$','TEXT'","$u -eq 'LONGBLOB'","$u -eq 'MEDIUMTEXT'"):
   if token not in t:findings.append(f"generator Type-For mapping missing: {token}")
 result={"status":"PASS" if not findings else "FAIL","officialVendors":list(VENDORS),"canonicalTableCount":len(expected),"lifecycle":inventory,"findings":findings}
 if findings:raise GateError(json.dumps(result,ensure_ascii=False,indent=2))
 return result

def main()->int:
 ap=argparse.ArgumentParser();ap.add_argument('--root',default='.');ap.add_argument('--json-output');ap.add_argument('--schema-fixture-root');ap.add_argument('--canonical')
 a=ap.parse_args();root=Path(a.root).resolve()
 try:
  if a.schema_fixture_root:
   canonical=Path(a.canonical).resolve() if a.canonical else root/'cpf-tools/db/canonical/platform-schema.json'
   r=verify_schema_inventory(canonical,Path(a.schema_fixture_root).resolve());code=0 if r['status']=='PASS' else 1
  else:r=verify(root);code=0
 except GateError as e:
  try:r=json.loads(str(e))
  except json.JSONDecodeError:r={"status":"FAIL","findings":[str(e)]}
  code=1
 if a.json_output:
  p=Path(a.json_output);p=p if p.is_absolute() else root/p;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(r,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
 print(json.dumps(r,ensure_ascii=False));return code
if __name__=='__main__':raise SystemExit(main())
