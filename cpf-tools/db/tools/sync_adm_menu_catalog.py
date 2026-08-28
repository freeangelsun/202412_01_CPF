#!/usr/bin/env python3
"""Synchronize canonical ADM menu catalog with frontend route registry and seed-model.

The catalog owns DB menu identity/parent/path/order. Frontend route metadata owns route identity.
Every route menuId must resolve to exactly one catalog entry; one menu may intentionally own multiple routes.
"""
from __future__ import annotations
import argparse, ast, json, re
from pathlib import Path

ROUTE_RE = re.compile(
    r'"(?P<key>[^"]+)"\s*:\s*\{\s*routeId:\s*"(?P<route>[^"]+)"\s*,\s*path:\s*"(?P<path>[^"]+)"\s*,\s*menuId:\s*"(?P<menu>[A-Z0-9_]+)"\s*,\s*label:\s*"(?P<label>[^"]+)"\s*,\s*group:\s*"(?P<group>[^"]+)"[\s\S]*?riskLevel:\s*"(?P<risk>[A-Z]+)"'
)

def routes(root: Path):
    out=[]
    for p in sorted((root/'cpf-admin/frontend/src/app/routes').glob('*.ts')):
        if p.name=='types.ts': continue
        for m in ROUTE_RE.finditer(p.read_text(encoding='utf-8')):
            d=m.groupdict(); d['source']=p.relative_to(root).as_posix(); out.append(d)
    return out

def parse_values(source: str):
    rows=[]
    for raw in re.findall(r'\(([^()]*)\)', source):
        parts=[]; cur=''; q=False; i=0
        while i<len(raw):
            c=raw[i]
            if c=="'":
                if q and i+1<len(raw) and raw[i+1]=="'": cur+="''"; i+=2; continue
                q=not q; cur+=c
            elif c==',' and not q: parts.append(cur.strip()); cur=''
            else: cur+=c
            i+=1
        parts.append(cur.strip())
        vals=[]
        for x in parts:
            if x.upper()=='NULL': vals.append(None)
            elif x.startswith("'") and x.endswith("'"): vals.append(x[1:-1].replace("''", "'"))
            else:
                try: vals.append(int(x))
                except ValueError: vals.append(x)
        rows.append(vals)
    return rows

def sql(v):
    if v is None: return 'NULL'
    if isinstance(v,int): return str(v)
    return "'"+str(v).replace("'","''")+"'"

def main():
    ap=argparse.ArgumentParser(); ap.add_argument('--root',default='.'); ap.add_argument('--check',action='store_true')
    args=ap.parse_args(); root=Path(args.root).resolve()
    seed_path=root/'cpf-tools/db/canonical/seed-model.json'; cat_path=root/'cpf-tools/db/canonical/adm-menu-catalog.json'
    seed=json.loads(seed_path.read_text(encoding='utf-8'))
    rr=routes(root)
    if len(rr)!=68: raise SystemExit(f'ADM route count mismatch: expected 68 actual {len(rr)}')
    grouped={}
    for r in rr: grouped.setdefault(r['menu'],[]).append(r)
    if len(grouped)!=64: raise SystemExit(f'ADM unique menu count mismatch: expected 64 actual {len(grouped)}')

    existing={}
    menu_stmt_indexes=[]
    for i,st in enumerate(seed['statements']):
        table=(st.get('currentTable') or st.get('table') or '').upper()
        if table!='ADM_MENU' or st.get('statementKind')!='insert' or st.get('sourceKind')!='values': continue
        menu_stmt_indexes.append(i)
        cols=[c.upper() for c in st['columns']]
        try: idx={name:cols.index(name) for name in ['MENU_ID','PARENT_MENU_ID','MENU_NAME','MENU_PATH','SORT_ORDER','USE_YN']}
        except ValueError: continue
        for row in parse_values(st.get('source','')):
            if len(row)<len(cols): continue
            mid=str(row[idx['MENU_ID']])
            existing[mid]={
                'menuId':mid,'parentMenuId':row[idx['PARENT_MENU_ID']], 'menuName':row[idx['MENU_NAME']],
                'menuPath':row[idx['MENU_PATH']], 'sortOrder':int(row[idx['SORT_ORDER']]), 'useYn':row[idx['USE_YN']]
            }

    next_order=max([x['sortOrder'] for x in existing.values()] or [150])+10
    entries=[]
    for mid, rs in sorted(grouped.items(), key=lambda kv:min(x['path'] for x in kv[1])):
        base=existing.get(mid)
        if base is None:
            primary=rs[0]
            base={'menuId':mid,'parentMenuId':None,'menuName':primary['label'],
                  'menuPath':'/adm#'+primary['path'].lstrip('/'),'sortOrder':next_order,'useYn':'Y'}
            next_order+=10
        base=dict(base)
        base['routes']=[{'routeId':x['route'],'path':x['path'],'group':x['group'],'riskLevel':x['risk'],'label':x['label']} for x in rs]
        entries.append(base)
    entries.sort(key=lambda x:(x['sortOrder'],x['menuId']))
    catalog={'schemaVersion':1,'owner':'cpf-admin','policy':{
        'routeCount':68,'uniqueMenuCount':64,'oneMenuMayOwnMultipleRoutes':True,
        'frontendRegistry':'cpf-admin/frontend/src/app/routes','dbSeedModel':'cpf-tools/db/canonical/seed-model.json'
    },'menus':entries}

    # Canonical ADM_MENU statement. Replace every previous ADM_MENU values statement with one catalog-rendered statement.
    source=',\n    '.join('('+', '.join(sql(x) for x in [e['menuId'],e['parentMenuId'],e['menuName'],e['menuPath'],e['sortOrder'],e['useYn'],'SYSTEM','SYSTEM'])+')' for e in entries)
    stmt={
      'sourceFile':'60_adm_seed_data.sql','logicalDatabase':'cpfDB','statementKind':'insert','table':'ADM_MENU','tableName':'ADM_MENU',
      'columns':['MENU_ID','PARENT_MENU_ID','MENU_NAME','MENU_PATH','SORT_ORDER','USE_YN','created_by','updated_by'],
      'sourceKind':'values','source':source,'conflictKey':'PRIMARY','conflictColumns':['MENU_ID'],
      'updates':[
        {'column':'PARENT_MENU_ID','expression':'VALUES(PARENT_MENU_ID)'},{'column':'MENU_NAME','expression':'VALUES(MENU_NAME)'},
        {'column':'MENU_PATH','expression':'VALUES(MENU_PATH)'},{'column':'SORT_ORDER','expression':'VALUES(SORT_ORDER)'},
        {'column':'USE_YN','expression':'VALUES(USE_YN)'},{'column':'updated_by','expression':'VALUES(updated_by)'},
        {'column':'updated_at','expression':'CURRENT_TIMESTAMP'}],
      'currentLogicalDatabase':'cpfDB','legacyLogicalDatabase':'admDB','productionDefault':True,'currentTable':'ADM_MENU','currentTableName':'ADM_MENU',
      'targetDatabaseRole':'CPF_PLATFORM_DB','logicalOwner':'admin'
    }
    # Keep first statement position for dependency order, remove later ADM_MENU statements.
    indexes=[i for i,st in enumerate(seed['statements']) if (st.get('currentTable') or st.get('table') or '').upper()=='ADM_MENU' and st.get('statementKind')=='insert' and st.get('sourceKind')=='values']
    if not indexes: raise SystemExit('ADM_MENU seed statement missing')
    first=indexes[0]
    new=[]
    for i,st in enumerate(seed['statements']):
        if i==first: new.append(stmt)
        elif i in indexes: continue
        else: new.append(st)
    seed['statements']=new; seed['statementCount']=len(new)

    cat_text=json.dumps(catalog,ensure_ascii=False,indent=2)+'\n'; seed_text=json.dumps(seed,ensure_ascii=False,indent=2)+'\n'
    if args.check:
        problems=[]
        if not cat_path.is_file() or cat_path.read_text(encoding='utf-8')!=cat_text: problems.append('adm-menu-catalog.json drift')
        if seed_path.read_text(encoding='utf-8')!=seed_text: problems.append('seed-model.json drift')
        if problems: raise SystemExit('ADM_MENU_CATALOG=FAIL '+', '.join(problems))
    else:
        cat_path.write_text(cat_text,encoding='utf-8'); seed_path.write_text(seed_text,encoding='utf-8')
    duplicate={m:[r['route'] for r in rs] for m,rs in grouped.items() if len(rs)>1}
    print(f'ADM_MENU_CATALOG=PASS routes={len(rr)} menus={len(entries)} multiRouteMenus={len(duplicate)} missing=0')
    if duplicate: print('ADM_MENU_MULTI_ROUTE='+json.dumps(duplicate,ensure_ascii=False,sort_keys=True))
if __name__=='__main__': main()
