#!/usr/bin/env python3
from pathlib import Path
import json,re,csv,sys,hashlib,unicodedata
ROOT=Path(__file__).resolve().parents[4]; H=ROOT/'cpf-docs/governance/development-harness'
c=json.loads((H/'contracts/contract-registry.json').read_text(encoding='utf-8'))
s=json.loads((H/'contracts/source-surface-registry.json').read_text(encoding='utf-8'))
sid=json.loads((H/'current/SOURCE_IDENTITY.json').read_text(encoding='utf-8'))
findings=[]
def add(cat,scope,msg): findings.append({'category':cat,'scope':scope,'message':msg})
# Profile completeness based entirely on registry values; one Root-Cause Work Item per Runtime surface.
for rel,d in s['runtimeResourceDirs'].items():
    missing=[prof for prof in c['environmentProfiles'] if not d['profileFiles'].get(prof)]
    if missing: add('CONFIG_PROFILE_SET',rel,'필수 profile 누락: '+','.join(missing))
# paired scripts where a stem exists
for d in s['standaloneBinDirs']:
    names=set(d['files'])
    stems={n.rsplit('.',1)[0] for n in names if n.endswith(('.ps1','.sh'))}
    for stem in stems:
        pair={stem+'.ps1',stem+'.sh'}
        if names & pair and not pair.issubset(names): add('STANDALONE_OS_PARITY',d['binDir'],'missing pair for '+stem)
# UTF-8 across source-controlled textual surfaces (ignore build/cache/generated binaries)
exts={'.java','.kt','.kts','.gradle','.properties','.xml','.yml','.yaml','.json','.csv','.md','.txt','.py','.ps1','.sh','.sql','.js','.ts','.tsx','.jsx','.html','.css'}
for p in ROOT.rglob('*'):
    if not p.is_file() or any(x in p.parts for x in ('build','.gradle','node_modules','.git')) or p.is_relative_to(H): continue
    if p.suffix.lower() not in exts and p.name not in {'.editorconfig','.gitattributes','.gitignore'}: continue
    try: txt=p.read_text(encoding='utf-8')
    except Exception as e: add('UTF8',p.relative_to(ROOT).as_posix(),str(e)); continue
    if '\x00' in txt or '\x08' in txt: add('CONTROL_CHAR',p.relative_to(ROOT).as_posix(),'NUL/BACKSPACE 제어문자 검출')
# Report without pretending product closure
def work_id(x): return 'WP-HARN-'+hashlib.sha256((x['category']+'|'+x['scope']+'|'+x['message']).encode('utf-8')).hexdigest()[:12].upper()
with (H/'current/CURRENT_WORK_ITEM_REGISTRY.csv').open(encoding='utf-8-sig',newline='') as f: tracked={r['work_item_id'] for r in csv.DictReader(f)}
untracked=[work_id(x) for x in findings if work_id(x) not in tracked]
out={'sourceZipSha256':sid['inputZipSha256'],'baselineProductContentSha256':sid['baselineProductContentSha256'],'findingCount':len(findings),'untrackedCount':len(untracked),'findings':findings,'status':'PASS' if not findings else ('UNTRACKED_GAPS' if untracked else 'TRACKED_GAPS')}
(H/'PRODUCT_CONFORMANCE_REPORT.json').write_text(json.dumps(out,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print('PRODUCT_CONFORMANCE='+out['status']+' FINDINGS='+str(len(findings))+' UNTRACKED='+str(len(untracked)))
for x in findings[:100]: print('GAP',x['category'],x['scope'],x['message'])
for x in untracked[:100]: print('UNTRACKED',x)
raise SystemExit(1 if untracked else 0)
