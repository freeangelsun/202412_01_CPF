from pathlib import Path
import re, sys
R=Path(sys.argv[1]).resolve() if len(sys.argv)>1 else Path(__file__).resolve().parents[3]
errors=[]
vendors=('mariadb','postgresql','oracle')

def strip_comments(s:str)->str:
    s=re.sub(r'/\*.*?\*/','',s,flags=re.S)
    return re.sub(r'--.*?$','',s,flags=re.M)

def split_top(s:str):
    out=[]; buf=[]; depth=0; quote=None
    for ch in s:
        if quote:
            buf.append(ch)
            if ch==quote: quote=None
            continue
        if ch in "'\"`": quote=ch; buf.append(ch); continue
        if ch=='(': depth+=1
        elif ch==')': depth-=1
        if ch==',' and depth==0:
            out.append(''.join(buf).strip()); buf=[]
        else: buf.append(ch)
    if ''.join(buf).strip(): out.append(''.join(buf).strip())
    return out

def ident(x:str)->str:
    return x.strip().strip('`"[]').lower()

def schema(sql:str):
    sql=strip_comments(sql)
    tables={}
    pattern=re.compile(r'CREATE\s+TABLE\s+(?:IF\s+NOT\s+EXISTS\s+)?([\w.`"\[\]]+)\s*\(',re.I)
    for m in pattern.finditer(sql):
        start=m.end(); depth=1; quote=None; i=start
        while i<len(sql) and depth:
            ch=sql[i]
            if quote:
                if ch==quote: quote=None
            elif ch in "'\"`": quote=ch
            elif ch=='(': depth+=1
            elif ch==')': depth-=1
            i+=1
        if depth: raise ValueError('unclosed CREATE TABLE '+m.group(1))
        body=sql[start:i-1]
        columns=[]
        for part in split_top(body):
            token=part.strip().split(None,1)[0] if part.strip() else ''
            if not token: continue
            head=ident(token)
            if head in {'constraint','primary','unique','foreign','check','key','index'}: continue
            columns.append(head)
        tables[ident(m.group(1).split('.')[-1])]=tuple(columns)
    drops=set(ident(x.split('.')[-1]) for x in re.findall(r'DROP\s+TABLE\s+(?:IF\s+EXISTS\s+)?([\w.`"\[\]]+)',sql,re.I))
    indexes=set(ident(x) for x in re.findall(r'CREATE\s+(?:UNIQUE\s+)?INDEX\s+([\w`"\[\]]+)',sql,re.I))
    return tables,drops,indexes

for module in sorted((R/'cpf-starters').iterdir()):
    db=module/'src/main/resources/db'
    if not db.exists(): continue
    relsets={}
    for v in vendors:
        relsets[v]={p.relative_to(db/v).as_posix():p for p in (db/v).rglob('*.sql')}
    for rel in sorted(relsets['mariadb']):
        parsed={}
        for v in vendors:
            try: parsed[v]=schema(relsets[v][rel].read_text(encoding='utf-8'))
            except Exception as e: errors.append(f'{module.name}/{v}/{rel}: {e}'); continue
        if len(parsed)!=3: continue
        base_tables=parsed['mariadb'][0]
        for v in ('postgresql','oracle'):
            if set(parsed[v][0])!=set(base_tables):
                errors.append(f'{module.name}/{rel}: table mismatch mariadb={sorted(base_tables)} {v}={sorted(parsed[v][0])}')
                continue
            for table,columns in base_tables.items():
                if tuple(parsed[v][0][table])!=tuple(columns):
                    errors.append(f'{module.name}/{rel}/{table}: column mismatch mariadb={columns} {v}={parsed[v][0][table]}')
            if parsed[v][1]!=parsed['mariadb'][1]:
                errors.append(f'{module.name}/{rel}: rollback drop mismatch mariadb={sorted(parsed["mariadb"][1])} {v}={sorted(parsed[v][1])}')
        # Every rollback file must drop at least one table when migration creates tables.
        if '/rollback/' in '/'+rel and not parsed['mariadb'][1]:
            errors.append(f'{module.name}/{rel}: rollback has no DROP TABLE')

print('DB_MODULES',sum(1 for p in (R/'cpf-starters').iterdir() if (p/'src/main/resources/db').exists()))
if errors:
    for e in errors: print('ERROR',e)
    sys.exit(1)
print('QA38_SQL_SEMANTIC_PARITY_PASS')
