#!/usr/bin/env python3
"""Build CPF development desired-state package metadata from a Local Working Tree ZIP baseline.

This tool is Git-independent. It must run on a fresh desired-state replay where every
PENDING_USER_EXECUTION delete candidate has been removed only in the replay tree. It never
removes files from the developer/user working copy.
"""
from __future__ import annotations

import argparse, csv, hashlib, importlib.util, json, sys
from datetime import datetime, timezone
from pathlib import Path
from zipfile import ZipFile

PACKAGE_REL = "cpf-docs/deliverables/PACKAGE_MANIFEST.json"
CHANGE_REL = "cpf-docs/deliverables/CHANGE_MANIFEST.csv"
SUMS_REL = "cpf-docs/deliverables/SHA256SUMS.txt"
DELETE_REL = "cpf-docs/deliverables/DELETE_MANIFEST.csv"
CLOSURE_REL = "cpf-docs/work/current/CPF_DEVELOPMENT_QA_CLOSURE.csv"
REQUIREMENT_REL = "cpf-docs/work/REQUIREMENT_STATUS.csv"
SOURCE_STATE_TOOL = "cpf-tools/verification/tools/cpf-source-state.py"
PACKAGE_METADATA_EXCLUSIONS = {PACKAGE_REL, CHANGE_REL, SUMS_REL}
PROTECTED_PREFIXES = (
    "cpf-docs/deliverables/", "cpf-docs/guides/", "cpf-docs/environment/docker/",
    "cpf-tools/environment/docker-development-test/",
)


def sha256_file(path: Path) -> str:
    h=hashlib.sha256()
    with path.open('rb') as f:
        for b in iter(lambda:f.read(1024*1024),b''): h.update(b)
    return h.hexdigest()


def sha256_zip_entry(zf: ZipFile, name: str) -> str:
    h=hashlib.sha256()
    with zf.open(name) as f:
        for b in iter(lambda:f.read(1024*1024),b''): h.update(b)
    return h.hexdigest()


def all_files(root: Path) -> list[str]:
    return sorted(p.relative_to(root).as_posix() for p in root.rglob('*') if p.is_file())


def identity(entries: list[tuple[str,str]]) -> tuple[str,str]:
    material=''.join(f'{digest}  {rel}\n' for rel,digest in sorted(entries)).encode()
    return hashlib.sha1(material,usedforsecurity=False).hexdigest(), hashlib.sha256(material).hexdigest()


def baseline_inventory(path: Path) -> tuple[str,dict[str,str]]:
    digest=sha256_file(path); out={}
    with ZipFile(path) as zf:
        for info in zf.infolist():
            if info.is_dir(): continue
            rel=info.filename.replace('\\','/')
            if rel in out: raise RuntimeError(f'duplicate baseline path: {rel}')
            out[rel]=sha256_zip_entry(zf,info.filename)
    return digest,out


def read_csv(path: Path) -> tuple[list[str],list[dict[str,str]]]:
    with path.open(encoding='utf-8-sig',newline='') as f:
        r=csv.DictReader(f); return list(r.fieldnames or []), [{k:(v or '').strip() for k,v in row.items()} for row in r]


def write_csv(path: Path, fields: list[str], rows: list[dict[str,str]]) -> None:
    with path.open('w',encoding='utf-8',newline='') as f:
        w=csv.DictWriter(f,fieldnames=fields); w.writeheader(); w.writerows(rows)


def source_snapshot(root: Path) -> dict:
    tool=root/SOURCE_STATE_TOOL
    spec=importlib.util.spec_from_file_location('cpf_source_state',tool)
    if spec is None or spec.loader is None:
        raise RuntimeError(f'cannot load canonical source identity tool: {tool}')
    mod=importlib.util.module_from_spec(spec)
    previous=sys.dont_write_bytecode
    sys.dont_write_bytecode=True
    try:
        spec.loader.exec_module(mod)
    finally:
        sys.dont_write_bytecode=previous
    return mod.snapshot(root,'source')


def update_closure_identity(root: Path, source_sha256: str) -> tuple[int,int]:
    path=root/CLOSURE_REL; fields,rows=read_csv(path)
    required={'finding_key','finding_id','closure_state','evidence_paths','source_identity_sha256'}
    if required-set(fields): raise RuntimeError(f'closure ledger missing columns: {sorted(required-set(fields))}')
    if len(rows)!=63: raise RuntimeError(f'closure ledger count mismatch: {len(rows)}')
    closed=blocked=0
    for row in rows:
        row['source_identity_sha256']=source_sha256
        if row['closure_state']=='CLOSED': closed+=1
        elif row['closure_state']=='BLOCKED_EXTERNAL': blocked+=1
        else: raise RuntimeError(f"invalid closure state: {row['finding_key']}={row['closure_state']}")
        ev=root/row['evidence_paths']
        if not ev.is_file(): raise RuntimeError(f"finding evidence missing: {row['finding_key']} -> {row['evidence_paths']}")
        text=ev.read_text(encoding='utf-8').replace('PENDING_FINAL_SOURCE_IDENTITY',source_sha256)
        ev.write_text(text,encoding='utf-8')
    write_csv(path,fields,rows)
    return closed,blocked


def delete_rows(root: Path) -> tuple[list[dict[str,str]],dict[str,int]]:
    fields,rows=read_csv(root/DELETE_REL)
    required={'path','approved','precondition','lifecycle','user_execution_required','user_approved'}
    if required-set(fields): raise RuntimeError(f'delete manifest missing columns: {sorted(required-set(fields))}')
    seen=set(); counts={}
    for row in rows:
        rel=row['path'].replace('\\','/')
        if not rel or rel in seen: raise RuntimeError(f'delete manifest missing/duplicate path: {rel!r}')
        seen.add(rel); lifecycle=row['lifecycle']; counts[lifecycle]=counts.get(lifecycle,0)+1
        if lifecycle=='PENDING_USER_EXECUTION':
            if row['approved'].lower()!='true' or row['precondition']!='SATISFIED' or row['user_execution_required'].lower()!='true':
                raise RuntimeError(f'{rel}: pending delete lifecycle contract invalid')
            if row['user_approved'].lower()=='true' and not row.get('user_approval_ref','').strip():
                raise RuntimeError(f'{rel}: user_approved lacks approval reference')
            if rel.startswith(PROTECTED_PREFIXES):
                raise RuntimeError(f'protected path cannot be pending deletion: {rel}')
    return rows,counts


def change_rows(root: Path, baseline: dict[str,str], delete_set:set[str]) -> list[dict[str,str]]:
    current={rel:sha256_file(root/rel) for rel in all_files(root) if rel not in PACKAGE_METADATA_EXCLUSIONS}
    rows=[]
    for rel in sorted((set(baseline)|set(current))-PACKAGE_METADATA_EXCLUSIONS):
        old=baseline.get(rel); new=current.get(rel)
        if old==new: continue
        if old is None:
            t=root/rel; rows.append({'path':rel,'change_type':'ADDED','size_bytes':str(t.stat().st_size),'sha256':new,'baseline_sha256':''})
        elif new is None:
            if rel not in delete_set: raise RuntimeError(f'desired-state deletion missing from DELETE_MANIFEST: {rel}')
            rows.append({'path':rel,'change_type':'DELETED','size_bytes':'0','sha256':'','baseline_sha256':old})
        else:
            t=root/rel; rows.append({'path':rel,'change_type':'MODIFIED','size_bytes':str(t.stat().st_size),'sha256':new,'baseline_sha256':old})
    return rows


def main()->int:
    ap=argparse.ArgumentParser(); ap.add_argument('--root',required=True); ap.add_argument('--baseline-zip',required=True); ap.add_argument('--generated-at')
    args=ap.parse_args(); root=Path(args.root).resolve(); baseline_zip=Path(args.baseline_zip).resolve()
    if not root.is_dir() or not baseline_zip.is_file(): raise SystemExit('root or baseline ZIP missing')
    baseline_sha,baseline=baseline_inventory(baseline_zip)
    drows,dcounts=delete_rows(root); delete_set={r['path'].replace('\\','/') for r in drows}
    pending={r['path'].replace('\\','/') for r in drows if r['lifecycle']=='PENDING_USER_EXECUTION'}
    remaining=sorted(rel for rel in pending if (root/rel).exists())
    if remaining: raise RuntimeError(f'pending delete candidates still exist in desired replay: {remaining[:10]}')
    req_fields,reqs=read_csv(root/REQUIREMENT_REL)
    canonical_doc=root/'cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md'
    import re
    canonical_ids=[m.group(1) for line in canonical_doc.read_text(encoding='utf-8-sig').splitlines() if (m:=re.match(r'^\| `([A-Z0-9-]+)` \|',line))]
    if not canonical_ids or len(canonical_ids)!=len(set(canonical_ids)): raise RuntimeError(f'canonical catalog count/duplicate drift: {len(canonical_ids)}/{len(set(canonical_ids))}')
    req_ids=[(r.get('exact_id') or '').strip() for r in reqs]
    if req_ids!=canonical_ids: raise RuntimeError(f'requirement projection order/set mismatch: ledger={len(req_ids)} canonical={len(canonical_ids)}')

    source=source_snapshot(root); source_sha256=source['contentSha256']; source_sha1=source['contentSha1']
    closed,blocked=update_closure_identity(root,source_sha256)
    source_after=source_snapshot(root)
    if source_after['contentSha256']!=source_sha256: raise RuntimeError('source identity changed while updating work/evidence metadata')

    # PACKAGE/CHANGE/SUMS are written after payload inventory to avoid self-reference.
    paths=[rel for rel in all_files(root) if rel not in PACKAGE_METADATA_EXCLUSIONS]
    files=[]; entries=[]; total=0
    for rel in paths:
        t=root/rel; dg=sha256_file(t); sz=t.stat().st_size
        files.append({'path':rel,'sizeBytes':sz,'sha256':dg}); entries.append((rel,dg)); total+=sz
    payload_sha1,payload_sha256=identity(entries)
    generated=args.generated_at or datetime.now(timezone.utc).isoformat()
    changes=change_rows(root,baseline,delete_set)
    summary={k:sum(r['change_type']==k for r in changes) for k in ('ADDED','MODIFIED','DELETED')}
    manifest={
        'schemaVersion':7,'packageType':'CPF_DEVELOPMENT_DESIRED_STATE','generatedAt':generated,
        'baselineInput':baseline_zip.name,'baselineSourceZipSha256':baseline_sha,'baselineSourceFileCount':len(baseline),
        'gitExactSha':'UNVERIFIED_SOURCE_ZIP_HAS_NO_DOT_GIT',
        'sourceIdentity':{'sha1':source_sha1,'sha256':source_sha256,'fileCount':source['fileCount'],'totalBytes':source['totalBytes'],
                          'algorithm':source['identityPolicy'],'excludedPaths':['cpf-docs/work/**','cpf-docs/deliverables/**','generated build/cache trees']},
        'packageMetadataExcludedPaths':sorted(PACKAGE_METADATA_EXCLUSIONS),
        'packagePayloadIdentity':{'sha1':payload_sha1,'sha256':payload_sha256,'fileCount':len(files),'totalBytes':total},
        'desiredState':{'fullFileCount':len(files)+len(PACKAGE_METADATA_EXCLUSIONS),'runtimeGarbageIncluded':0,'pendingDeleteCandidatesAppliedInReplay':len(pending)},
        'changeSummary':summary,
        'deleteManifest':DELETE_REL,'deleteManifestCount':len(drows),'deleteLifecycleCounts':dcounts,
        'developerFindingClosure':{'ledger':CLOSURE_REL,'total':63,'closed':closed,'blockedExternal':blocked},
        'requirementProjection':{'path':REQUIREMENT_REL,'rows':len(reqs)},
        'developmentCompletion':'IMPLEMENTABLE_SCOPE_COMPLETE' if blocked else 'COMPLETE',
        'overallCompletion':'BLOCKED_EXTERNAL' if blocked else 'COMPLETE',
        'actualUserWorkingTreeDeletionPerformed':False,
        'unverifiedRuntime':[
            'Fresh Public Workspace live mixed-vendor DB provisioning/migration/seed/runtime-health',
            'Java25 root Gradle clean build/test/publication',
            'live Oracle/PostgreSQL/MariaDB install→migration→seed→runtime query→upgrade→rollback',
            'Multi-WAS/same-host multi-process/process-kill/recovery',
            'Browser E2E against running ADM/Backoffice services',
            'Public Binary live repository resolution',
        ],
        'files':files,
    }
    (root/PACKAGE_REL).write_text(json.dumps(manifest,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    # Recompute change rows with PACKAGE_MANIFEST now materialized; class counts must remain stable.
    changes=change_rows(root,baseline,delete_set)
    summary2={k:sum(r['change_type']==k for r in changes) for k in ('ADDED','MODIFIED','DELETED')}
    manifest['changeSummary']=summary2
    (root/PACKAGE_REL).write_text(json.dumps(manifest,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    changes=change_rows(root,baseline,delete_set)
    with (root/CHANGE_REL).open('w',encoding='utf-8',newline='') as f:
        fields=['path','change_type','size_bytes','sha256','baseline_sha256']; w=csv.DictWriter(f,fieldnames=fields); w.writeheader(); w.writerows(changes)
    sums_paths=sorted(rel for rel in all_files(root) if rel!=SUMS_REL)
    (root/SUMS_REL).write_text(''.join(f'{sha256_file(root/rel)}  {rel}\n' for rel in sums_paths),encoding='utf-8')
    print(json.dumps({'status':'PASS','baselineSourceZipSha256':baseline_sha,'baselineFiles':len(baseline),'sourceIdentitySha256':source_sha256,
                      'sourceFiles':source['fileCount'],'desiredFiles':len(all_files(root)),'changeSummary':summary2,'deleteCandidates':len(drows),
                      'findingsClosed':closed,'findingsBlockedExternal':blocked},ensure_ascii=False))
    return 0
if __name__=='__main__': raise SystemExit(main())
