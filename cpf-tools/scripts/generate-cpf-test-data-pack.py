#!/usr/bin/env python3
from __future__ import annotations
import argparse,hashlib,json,re,sys
from pathlib import Path

OFFICIAL=("mariadb","postgresql","oracle")
class PolicyError(RuntimeError): pass

def read_json(path:Path): return json.loads(path.read_text(encoding='utf-8-sig'))
def quote(v:str,identifier:str)->str:
    if v=='mariadb': return f'`{identifier.replace("`","``")}`'
    return f'"{identifier.replace(chr(34),chr(34)*2)}"'
def text_length(type_name:str)->int:
    m=re.search(r'\((\d+)\)',type_name); return int(m.group(1)) if m else 4000

def deterministic_expr(vendor:str, column:str, seed:str, klass:str, length:int)->str:
    q=quote(vendor,column); seed_sql=seed.replace("'","''")
    if klass=='email':
        prefix=deterministic_expr(vendor,column,seed,'hash',max(8,min(40,length-16)))
        return f"CONCAT(SUBSTRING({prefix},1,{max(1,length-16)}),'@example.invalid')" if vendor=='mariadb' else (f"SUBSTR({prefix},1,{max(1,length-16)}) || '@example.invalid'")
    if klass=='phone': return "'0000000000'"
    if klass=='ip': return "'192.0.2.1'"
    if klass in ('payload','message'): return "'{}'" if ('JSON' in column.upper() or 'PAYLOAD' in column.upper() or 'BODY' in column.upper()) else "'[MASKED]'"
    if klass=='file_name': return "'masked-file.bin'"
    if vendor=='mariadb': base=f"LOWER(SHA2(CONCAT(COALESCE(CAST({q} AS CHAR),''),'|','{seed_sql}'),256))"; return f"LEFT({base},{length})"
    if vendor=='postgresql': base=f"LOWER(MD5(COALESCE({q}::text,'') || '|' || '{seed_sql}'))"; return f"SUBSTR({base},1,{length})"
    base=f"LOWER(STANDARD_HASH(COALESCE(TO_CHAR({q}),'') || '|' || '{seed_sql}','SHA256'))"; return f"SUBSTR({base},1,{length})"

def classify(policy:dict,name:str)->str|None:
    for item in policy['masking']['classes']:
        if re.search(item['columnPattern'],name,re.I): return item['name']
    return None

def formal_key_columns(schema:dict)->set[tuple[str,str]]:
    out=set()
    for t in schema['tables']:
        for c in t.get('primaryKey',[]): out.add((t['name'].lower(),c.lower()))
        for fk in t.get('foreignKeys',[]):
            for c in fk.get('columns',[]): out.add((t['name'].lower(),c.lower()))
            for c in fk.get('refColumns',[]): out.add((fk['refTable'].lower(),c.lower()))
    return out

def generate_mask(schema:dict,policy:dict,vendor:str,seed:str):
    keys=formal_key_columns(schema); statements=[]; inventory=[]
    for table in schema['tables']:
        assignments=[]
        for col in table.get('columns',[]):
            klass=classify(policy,col['name'])
            if not klass: continue
            if (table['name'].lower(),col['name'].lower()) in keys: continue
            typ=str(col['type']).upper()
            if not any(token in typ for token in ('CHAR','TEXT','CLOB','BLOB','BINARY')): continue
            length=max(8,min(text_length(typ),4000))
            expr=deterministic_expr(vendor,col['name'],seed,klass,length)
            assignments.append(f"{quote(vendor,col['name'])} = {expr}")
            inventory.append({'table':table['name'],'column':col['name'],'class':klass,'type':col['type'],'logicalDatabase':table.get('logicalDatabase'),'formalKey':False})
        if assignments:
            statements.append(f"UPDATE {quote(vendor,table['name'])} SET\n  "+",\n  ".join(assignments)+";")
    header=["-- CPF_TEST_DATA_MASKING_V1","-- Generated from canonical platform-schema.json and cpf-test-data-policy.json","-- Production target execution is forbidden."]
    if vendor=='oracle': header += ["WHENEVER SQLERROR EXIT SQL.SQLCODE","SET DEFINE OFF"]
    return '\n'.join(header+['']+statements+['','COMMIT;','']),inventory

def generate_synthetic(vendor:str):
    rows=[(f'CPF-SYNTH-{i:04d}',f'Synthetic Item {i:04d}','SYNTHETIC','ACTIVE',f'CPF synthetic deterministic row {i:04d}','CPF_SYNTHETIC_DATA_V1',i,'CPF_SYNTHETIC_DATA_V1') for i in range(1,11)]
    t=quote(vendor,'cmn_sample_item'); cols=['sample_key','item_name','category_code','status_code','searchable_text','owner_reference','sort_order','created_by','updated_by']; qcols=', '.join(quote(vendor,c) for c in cols)
    statements=[]
    for row in rows:
        vals=[]
        for value in row:
            vals.append(str(value) if isinstance(value,int) else "'"+str(value).replace("'","''")+"'")
        if vendor=='mariadb':
            statements.append(f"INSERT INTO {t} ({qcols}) VALUES ({', '.join(vals)}) ON DUPLICATE KEY UPDATE {quote(vendor,'item_name')}=VALUES({quote(vendor,'item_name')}), {quote(vendor,'updated_by')}='CPF_SYNTHETIC_DATA_V1';")
        elif vendor=='postgresql':
            statements.append(f"INSERT INTO {t} ({qcols}) VALUES ({', '.join(vals)}) ON CONFLICT ({quote(vendor,'sample_key')}) DO UPDATE SET {quote(vendor,'item_name')}=EXCLUDED.{quote(vendor,'item_name')}, {quote(vendor,'updated_by')}='CPF_SYNTHETIC_DATA_V1';")
        else:
            key=row[0].replace("'","''")
            statements.append(f"MERGE INTO {t} d USING (SELECT '{key}' {quote(vendor,'sample_key')} FROM dual) s ON (d.{quote(vendor,'sample_key')}=s.{quote(vendor,'sample_key')}) WHEN MATCHED THEN UPDATE SET d.{quote(vendor,'item_name')}='{row[1]}' WHEN NOT MATCHED THEN INSERT ({qcols}) VALUES ({', '.join(vals)});")
    header=['-- CPF_SYNTHETIC_DATA_V1','-- Contains no production-derived values.']
    if vendor=='oracle': header += ['WHENEVER SQLERROR EXIT SQL.SQLCODE','SET DEFINE OFF']
    return '\n'.join(header+['']+statements+['','COMMIT;',''])

def main()->int:
    ap=argparse.ArgumentParser();ap.add_argument('--root',type=Path,default=Path.cwd());ap.add_argument('--vendor',choices=OFFICIAL,required=True);ap.add_argument('--mode',choices=('mask','synthetic'),required=True);ap.add_argument('--seed',default='CPF_TEST_DATA_V1');ap.add_argument('--output',type=Path,required=True);ap.add_argument('--inventory',type=Path);args=ap.parse_args()
    root=args.root.resolve();schema=read_json(root/'cpf-tools/db/canonical/platform-schema.json');policy=read_json(root/'cpf-tools/db/cpf-test-data-policy.json')
    if tuple(policy['officialVendors'])!=OFFICIAL: raise PolicyError('official vendor policy drift')
    if not args.seed or len(args.seed)>128 or re.search(r'[\x00-\x1f\x7f]',args.seed): raise PolicyError('invalid deterministic seed')
    if args.mode=='mask': sql,inventory=generate_mask(schema,policy,args.vendor,args.seed)
    else: sql=generate_synthetic(args.vendor);inventory=[]
    args.output.parent.mkdir(parents=True,exist_ok=True);args.output.write_text(sql,encoding='utf-8',newline='\n')
    result={'schemaVersion':1,'vendor':args.vendor,'mode':args.mode,'sqlFile':str(args.output),'sha256':hashlib.sha256(args.output.read_bytes()).hexdigest(),'statementCount':sql.count(';'),'maskedColumnCount':len(inventory),'synthetic':args.mode=='synthetic','productionDerived':False,'sanitized':True}
    if args.inventory:
        args.inventory.parent.mkdir(parents=True,exist_ok=True);args.inventory.write_text(json.dumps({'summary':result,'columns':inventory},ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    print(json.dumps(result,sort_keys=True,separators=(',',':')));return 0
if __name__=='__main__':
 try: raise SystemExit(main())
 except PolicyError as e: print(f'[FAIL] {e}',file=sys.stderr);raise SystemExit(1)
