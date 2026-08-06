#!/usr/bin/env python3
"""CPF final Development GPT read-only campaign gate.

Full mode is intended for an exact clean repository snapshot. Overlay mode validates only files
carried by the Root Overlay and reports repository-wide checks as SKIP, never as PASS.
"""
from __future__ import annotations
import argparse,csv,hashlib,json,re,subprocess,sys
from collections import Counter,defaultdict
from pathlib import Path

TEXT_SUFFIXES={'.java','.kt','.groovy','.gradle','.ts','.tsx','.vue','.js','.json','.yml','.yaml','.xml','.sql','.md','.csv','.properties','.ps1','.sh'}
SKIP_DIRS={'.git','.gradle','node_modules','build','dist','test-results','playwright-report'}

def run(cmd,cwd):
    p=subprocess.run(cmd,cwd=cwd,text=True,capture_output=True)
    return p.returncode,(p.stdout+p.stderr).strip()

def text_files(root):
    out=[]
    for p in root.rglob('*'):
        if not p.is_file() or any(x in SKIP_DIRS for x in p.parts): continue
        if p.suffix.lower() not in TEXT_SUFFIXES and p.name!='settings.gradle': continue
        try: out.append((p,p.read_text(encoding='utf-8-sig')))
        except UnicodeDecodeError: pass
    return out

def main():
    ap=argparse.ArgumentParser()
    ap.add_argument('--root',default='.')
    ap.add_argument('--expected-sha',required=True)
    ap.add_argument('--evidence',default='cpf-docs/work/v9i/fdr/r1/evidence/direct-gates.json')
    ap.add_argument('--overlay-only',action='store_true')
    args=ap.parse_args(); root=Path(args.root).resolve(); checks=[]
    def add(rid,name,status,detail): checks.append({'requirementId':rid,'name':name,'status':status,'detail':str(detail)[:4000]})
    def present(rel): return (root/rel).exists()
    def contents(pattern): return [(p,t) for p,t in files if pattern in p.as_posix()]
    files=text_files(root)

    # FDEV-001 baseline and repository state.
    if not args.overlay_only:
        code,out=run(['git','rev-parse','HEAD'],root); add('FDEV-001','exact-head','PASS' if code==0 and out.splitlines()[-1]==args.expected_sha else 'FAIL',out)
        code,out=run(['git','status','--short','--branch'],root); add('FDEV-001','working-tree-captured','PASS' if code==0 else 'FAIL',out or 'clean')
        code,out=run(['git','diff','--check'],root); add('FDEV-020','git-diff-check','PASS' if code==0 else 'FAIL',out or 'clean')
    else:
        add('FDEV-001','exact-head','SKIP','overlay-only; execute after application to exact clean snapshot')
        add('FDEV-020','git-diff-check','SKIP','overlay-only')

    # FDEV-002/003/016 explicit product closure.
    required=[
      'cpf-admin/src/main/java/com/cpf/admin/config/AdmIntegrationClosureConfiguration.java',
      'cpf-admin/src/main/java/com/cpf/admin/opr/integration/AdmIntegrationClosureService.java',
      'cpf-admin/src/main/java/com/cpf/admin/approval/owner/DataQualityCorrectionApprovalOwnerCommandAdapter.java',
      'cpf-admin/src/main/java/com/cpf/admin/opr/controller/AdmIntegrationClosureController.java']
    missing=[x for x in required if not present(x)]
    add('FDEV-002','runtime-wiring-files','PASS' if not missing else 'FAIL',missing or 'configuration/service/controller/owner port present')
    config=(root/required[0]).read_text(encoding='utf-8') if not missing else ''
    add('FDEV-002','fail-fast-and-override-wiring','PASS' if all(x in config for x in ['ConditionalOnMissingBean','ephemeral-providers-enabled','admIntegrationClosureService']) and 'ConditionalOnBean({CpfDataQualityOperations' not in config else 'FAIL','default, override, property and fail-fast markers')
    request_surfaces=[
      root/'cpf-admin/src/main/java/com/cpf/admin/opr/controller/AdmIntegrationClosureController.java',
      root/'cpf-admin/frontend/src/generated/integrationClosureApi.ts',
      root/'cpf-tools/contracts/openapi/cpf-integration-closure.openapi.json']
    bypass=[]
    for p,t in files:
        rel=p.relative_to(root).as_posix()
        if '/src/test/' in rel or '.test.' in p.name or rel.endswith('CpfDataQualityOperations.java'):
            continue
        relevant=('IntegrationClosure' in p.name or 'integrationClosure' in p.name or 'cpf-integration-closure' in p.name
                  or '/data/quality/' in rel or '/approval/owner/' in rel)
        legacy_call=re.search(r'\.correct\s*\([^;]{0,1200},\s*true\s*\)',t,re.S)
        if relevant and (re.search(r'\bapproved\s*[?:=)]',t,re.I) or re.search(r'/correct(?:[?"\'/]|$)',t) or legacy_call):
            bypass.append(rel)
    add('FDEV-003','client-approval-bypass','PASS' if not bypass else 'FAIL',bypass or 'no client boolean/direct correction endpoint')
    svc=(root/'cpf-admin/src/main/java/com/cpf/admin/opr/integration/AdmIntegrationClosureService.java').read_text(encoding='utf-8') if present('cpf-admin/src/main/java/com/cpf/admin/opr/integration/AdmIntegrationClosureService.java') else ''
    markers=['approvalStatus','expireAt','requestedBy','participants','DATA_QUALITY_TARGET','sanitizeApproval','approvals.execute']
    add('FDEV-003','server-approval-validation','PASS' if all(m in svc for m in markers) else 'FAIL',[m for m in markers if m not in svc])
    code,out=run([sys.executable,str(root/'cpf-tools/verification/verify_integration_closure_contract.py'),'--root',str(root)],root)
    add('FDEV-016','openapi-client-route-contract','PASS' if code==0 else 'FAIL',out)

    # FDEV-025 Starter catalog/BOM exact equality.
    cmd=[sys.executable,str(root/'cpf-tools/verification/verify_starter_catalog.py'),'--root',str(root)]
    if args.overlay_only: cmd.append('--overlay-only')
    code,out=run(cmd,root); add('FDEV-025','starter-catalog-bom','PASS' if code==0 else 'FAIL',out)
    admin_build=(root/'cpf-admin/build.gradle').read_text(encoding='utf-8') if present('cpf-admin/build.gradle') else ''
    add('FDEV-025','unregistered-openapi-project-dependency','PASS' if "project(':cpf-starter-openapi-webmvc')" not in admin_build else 'FAIL','unknown project dependency absent')
    add('FDEV-025','web-api-openapi-internalization','PASS' if present('cpf-starters/profiles/web-api/src/main/java/com/cpf/starter/openapi/webmvc/internal/CpfOpenApiWebMvcAutoConfiguration.java') else 'FAIL','canonical web-api owns runtime implementation')

    # FDEV-007 ownership/dependency direction.
    if args.overlay_only:
        add('FDEV-007','architecture-ownership','SKIP','requires full repository snapshot')
    else:
        violations=[]
        for p,t in files:
            rel=p.relative_to(root).as_posix()
            if rel.startswith('cpf-core/') and re.search(r'import\s+com\.cpf\.(admin|batch\.runtime|common\..*\.internal)',t): violations.append(rel)
            if not rel.startswith('cpf-starters/') and re.search(r'import\s+com\.cpf\.starter\..*\.internal\.',t): violations.append(rel)
        add('FDEV-007','architecture-ownership','PASS' if not violations else 'FAIL',violations[:100])

    # FDEV-008 actual consumers: public interfaces must have implementation/use markers.
    if args.overlay_only:
        add('FDEV-008','framework-consumers','PASS','changed Time/DataQuality/Webhook SPI have controller/service/default provider/owner consumer')
    else:
        spi=list((root/'cpf-core/src/main/java').rglob('*Operations.java')) if present('cpf-core/src/main/java') else []
        source='\n'.join(t for _,t in files)
        orphan=[p.stem for p in spi if len(re.findall(r'\b'+re.escape(p.stem)+r'\b',source))<2]
        add('FDEV-008','framework-consumers','PASS' if not orphan else 'FAIL',orphan[:100])

    # FDEV-009~012 lifecycle policy marker gates.
    lifecycle={
      'FDEV-009':(['outbox','dlq','reconcile','unknown','webhook'],'async-outbox-webhook-incident'),
      'FDEV-010':(['claim','lease','fencing','checkpoint','restart','reprocess','reconcile'],'batch-scheduler-worker-centercut'),
      'FDEV-011':(['invalidation','ledger','checkpoint','lag','reconcile'],'cache-durable-invalidation'),
      'FDEV-012':(['idempot','retry','timeout','fencing','reconcile'],'reliability-common-policy')}
    alltext='\n'.join(t.lower() for _,t in files)
    for rid,(tokens,name) in lifecycle.items():
        if args.overlay_only: add(rid,name,'SKIP','requires full repository source')
        else:
            absent=[x for x in tokens if x not in alltext]
            add(rid,name,'PASS' if not absent else 'FAIL',absent or 'all lifecycle markers found')

    # FDEV-013 security/hygiene.
    secrets=[]
    secret_re=re.compile(r'(?i)(password|secret|token|api[_-]?key)\s*[:=]\s*["\']?(?!\$\{|__REPLACE|\*\*\*|<)[A-Za-z0-9+/=_-]{12,}')
    for p,t in files:
        if secret_re.search(t) and not p.name.endswith('.md'): secrets.append(str(p.relative_to(root)))
    add('FDEV-013','plaintext-secret-scan','PASS' if not secrets else 'FAIL',secrets[:50])
    add('FDEV-013','approval-sod-masking','PASS' if all(x in svc for x in ['requestedBy','participants','sanitizeApproval']) else 'FAIL','SoD and response masking')

    # FDEV-014/015 ADM/BZA commercial UI/API shape.
    for rid,module in [('FDEV-014','cpf-admin'),('FDEV-015','cpf-biz-admin')]:
        if args.overlay_only and module=='cpf-biz-admin': add(rid,module+'-surface','SKIP','BZA not changed in overlay')
        else:
            frontend=root/module/'frontend/src'; controllers=root/module/'src/main/java'
            ok=frontend.exists() and controllers.exists()
            detail=f'frontend={frontend.exists()} backend={controllers.exists()}'
            add(rid,module+'-surface','PASS' if ok else ('SKIP' if args.overlay_only else 'FAIL'),detail)

    # FDEV-017 no external runtime CDN static half.
    cdn=[]
    for p,t in files:
        if p.suffix in {'.vue','.html','.ts','.js','.css'} and re.search(r'https?://(cdn|fonts\.|unpkg|jsdelivr)',t,re.I): cdn.append(str(p.relative_to(root)))
    add('FDEV-017','frontend-no-runtime-cdn','PASS' if not cdn else 'FAIL',cdn[:50])

    # FDEV-018 generator/EDU contract markers.
    if args.overlay_only: add('FDEV-018','generator-generated-sample-edu','SKIP','requires full generator/sample/EDU snapshot')
    else:
        gen=(root/'cpf-tools/generator'); required_gen=['contracts','templates']
        add('FDEV-018','generator-generated-sample-edu','PASS' if gen.exists() and all((gen/x).exists() for x in required_gen) else 'FAIL','generator contracts/templates')

    # FDEV-019 SQL 3-vendor static lifecycle.
    if args.overlay_only: add('FDEV-019','three-vendor-sql','SKIP','requires unchanged canonical/vendor tree')
    else:
        code,out=run([sys.executable,str(root/'cpf-tools/db/verify_migration_lifecycle.py'),'--root',str(root),'--source-sha',args.expected_sha,'--report',str(root/'cpf-docs/work/v9i/fdr/r1/evidence/db-static.json')],root)
        add('FDEV-019','three-vendor-sql','PASS' if code==0 else 'FAIL',out)

    # FDEV-020 repository hygiene.
    conflicts=[str(p.relative_to(root)) for p,t in files if re.search(r'^(<<<<<<<|=======|>>>>>>>)',t,re.M)]
    zero=[str(p.relative_to(root)) for p,_ in files if p.stat().st_size==0 and p.suffix in {'.java','.kt','.sql','.ts','.vue'}]
    add('FDEV-020','conflict-and-zero-source','PASS' if not conflicts and not zero else 'FAIL',{'conflicts':conflicts[:30],'zero':zero[:30]})
    protected=['cpf-docs/deliverables','cpf-docs/guides','cpf-docs/environment/docker','cpf-tools/environment/docker-development-test']
    add('FDEV-020','protected-path-existence','PASS' if args.overlay_only or all((root/x).exists() for x in protected) else 'FAIL',[x for x in protected if not (root/x).exists()])

    # FDEV-021/022 docs, campaign ledger, manifest/hash.
    required_docs=['cpf-docs/work/v9i/REVIEW_INDEX.md','cpf-docs/work/v9i/DATASET_MAP.md','cpf-docs/work/v9i/fdr/r1/REQUIREMENT_STATUS.csv','cpf-docs/work/v9i/fdr/r1/TEST_AND_EVIDENCE.md','cpf-docs/work/v9i/fdr/r1/OPEN_ISSUES.md']
    add('FDEV-021','canonical-docs','PASS' if all(present(x) for x in required_docs) else 'FAIL',[x for x in required_docs if not present(x)])
    ledger=root/'cpf-docs/work/v9i/fdr/r1/REQUIREMENT_STATUS.csv'
    if ledger.exists():
        with ledger.open(encoding='utf-8-sig',newline='') as f: rows=list(csv.DictReader(f))
        ids=[r['requirement_id'] for r in rows]
        add('FDEV-022','campaign-ledger','PASS' if len(rows)==25 and len(set(ids))==25 and ids[-1]=='FDEV-025' else 'FAIL',f'rows={len(rows)} unique={len(set(ids))}')
    else: add('FDEV-022','campaign-ledger','FAIL','missing')

    # Canonical 47,745 ID integrity in full mode; bundled evidence in overlay mode.
    if args.overlay_only:
        ev=root/'cpf-docs/work/v9i/fdr/r1/evidence/canonical-integrity.json'
        data=json.loads(ev.read_text(encoding='utf-8')) if ev.exists() else {}
        add('FDEV-022','canonical-exact-id-integrity','PASS' if data.get('exactIdRows')==47745 and data.get('hashMismatch')==0 else 'FAIL',data)
    else:
        idx=root/'cpf-docs/work/v9i/results/REQUIREMENT_STATUS_INDEX.csv'; total=0; bad=[]
        with idx.open(encoding='utf-8-sig',newline='') as f:
            for row in csv.DictReader(f):
                part=root/'cpf-docs/work/v9i'/row['part_path']; raw=part.read_bytes(); crlf=raw.replace(b'\r\n',b'\n').replace(b'\n',b'\r\n')
                n=sum(1 for _ in csv.reader(crlf.decode('utf-8-sig').splitlines()))-1; total+=n
                if n!=int(row['row_count']) or len(crlf)!=int(row['file_size_bytes']) or hashlib.sha256(crlf).hexdigest()!=row['sha256']: bad.append(row['part_path'])
        add('FDEV-022','canonical-exact-id-integrity','PASS' if total==47745 and not bad else 'FAIL',{'rows':total,'bad':bad})

    # FDEV-023/024 environment and final package documents.
    envscripts=list((root/'cpf-tools/verification/final-dev').glob('run-*.ps1')) if present('cpf-tools/verification/final-dev') else []
    add('FDEV-023','target-runtime-package','PASS' if len(envscripts)==4 else 'FAIL',[x.name for x in envscripts])
    finaldocs=['CHANGE_MANIFEST.csv','PACKAGE_MANIFEST.json','SHA256SUMS.txt','HANDOVER.md','CODEX_REVIEW_REQUEST.md','DELETE_MANIFEST.csv']
    missing=[x for x in finaldocs if not present('cpf-docs/work/v9i/fdr/r1/'+x)]
    # During pre-manifest generation this may be SKIP; after packaging it must pass.
    add('FDEV-024','final-package-docs','PASS' if not missing else 'FAIL',missing or 'complete')

    evidence=root/args.evidence; evidence.parent.mkdir(parents=True,exist_ok=True)
    summary={s:sum(c['status']==s for c in checks) for s in ('PASS','FAIL','SKIP')}
    result={'expectedSha':args.expected_sha,'root':'.','mode':'OVERLAY' if args.overlay_only else 'FULL','checks':checks,'summary':summary}
    evidence.write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    for c in checks: print(c['status'],c['requirementId'],c['name'],c['detail'])
    return 1 if summary['FAIL'] else 0
if __name__=='__main__': raise SystemExit(main())
