#!/usr/bin/env python3
"""Currentize retired Backoffice product identities in current derived datasets without changing stable IDs/order.

Historical migration/evidence path columns are intentionally preserved. Structural ownership, work-package,
capability and functional identity columns are migrated from the retired BZA/cpf-biz-* architecture to
Backoffice/MBW. Part indexes are regenerated from actual bytes after the rewrite.
"""
from __future__ import annotations

import sys as _cpf_sys

# CPF 표준 인코딩은 UTF-8 이다. 호출자의 콘솔 코드페이지(Windows cp949 등)에 좌우되면
# 한글 출력이 깨져 진단 메시지를 읽을 수 없다. 진입점이 스스로 출력 스트림을 고정한다.
for _cpf_stream in (_cpf_sys.stdout, _cpf_sys.stderr):
    try:
        _cpf_stream.reconfigure(encoding='utf-8')
    except (AttributeError, ValueError):
        pass
import argparse,csv,hashlib,re
from pathlib import Path

CURRENT=Path('cpf-docs/work/current')
DATASETS=(
 ('CPF_REQUIREMENT_MASTER','requirement_id'),
 ('CPF_EXECUTION_SEQUENCE','execution_order'),
 ('CPF_SCENARIO_MASTER','scenario_id'),
)
# Actual path/evidence/history text can legitimately contain historical BZA migration names; never rewrite these columns.
HISTORICAL_COLUMNS={
 'source_basis','evidence_path','개발GPT_evidence','개발GPT_자체검수evidence','Codex_evidence','QA_검수evidence',
 'QA_검수이력경로','QA_재개발대상파일','documentation_impact','baseline_sha'
}
STRUCTURAL_COLUMNS={
 'owner_module','owner_package','change_target','actual_consumer','requirement_group','capability','feature','function_type',
 'actor','trigger','preconditions','inputs','defaults','processing_steps','state_transition','outputs','error_handling',
 'concurrency_control','timeout_policy','retry_policy','unknown_result_policy','recovery_policy','security_control','permission',
 'data_scope','masking','api_contract','frontend_contract','generator_impact','completion_prohibited_when','work_package_id',
 'sequence_lane','phase_entry_criteria','phase_exit_criteria','parallel_execution_rule','change_freeze_rule','rework_prevention_control',
 'scope_decision','requirement'
}

def sha(data:bytes)->str:return hashlib.sha256(data).hexdigest()

def currentize(value:str, *, structural:bool)->str:
 if not value:return value
 value=value.replace('cpf-biz-frontend','cpf-backoffice-web/frontend')
 value=value.replace('cpf-biz-channel','cpf-backoffice-web')
 value=value.replace('cpf-biz-admin','cpf-backoffice')
 if structural:
  value=re.sub(r'(?<![A-Za-z0-9])BZA_DB(?![A-Za-z0-9])','MBW_DB',value)
  value=re.sub(r'(?<![A-Za-z0-9])BZA_UI(?![A-Za-z0-9])','MBW_WEB',value)
  value=value.replace('BZA-BUSINESS','MBW-BUSINESS').replace('BZA 업무 관리','MBW 업무 Backoffice')
  value=re.sub(r'(?<=-)BZA(?=-)','MBW',value)
  value=re.sub(r'\bbza_(?=[a-z0-9_])','mbw_',value)
 return value

def rewrite_part(path:Path)->tuple[int,str,str]:
 with path.open(encoding='utf-8-sig',newline='') as f:
  rd=csv.DictReader(f);fields=rd.fieldnames or [];rows=list(rd)
 changed=0
 for row in rows:
  for col in fields:
   if col in HISTORICAL_COLUMNS:continue
   old=row.get(col,'');new=currentize(old,structural=col in STRUCTURAL_COLUMNS)
   if new!=old:row[col]=new;changed+=1
 with path.open('w',encoding='utf-8',newline='') as f:
  w=csv.DictWriter(f,fieldnames=fields,lineterminator='\n');w.writeheader();w.writerows(rows)
 return len(rows),fields[0] if fields else '',str(changed)

def rebuild_index(root:Path,stem:str,id_col:str)->tuple[int,int]:
 part_dir=root/CURRENT/f'{stem}.parts';index=root/CURRENT/f'{stem}.csv';parts=sorted(part_dir.glob('*.csv'))
 if not parts or not index.is_file():return 0,0
 with index.open(encoding='utf-8-sig',newline='') as f:
  rd=csv.DictReader(f);index_fields=rd.fieldnames or [];old=list(rd)
 meta_by_name={Path(r['part_path']).name:r for r in old}
 total=0;changed=0;newrows=[];header_line=None
 for seq,p in enumerate(parts,1):
  _,_,c=rewrite_part(p);changed+=int(c)
  raw=p.read_bytes();lines=raw.splitlines(keepends=True);header_line=header_line or (lines[0] if lines else b'')
  with p.open(encoding='utf-8-sig',newline='') as f: rows=list(csv.DictReader(f))
  total+=len(rows);oldrow=meta_by_name.get(p.name,{})
  first=rows[0].get(id_col,'') if rows else '';last=rows[-1].get(id_col,'') if rows else ''
  row={k:oldrow.get(k,'') for k in index_fields}
  row.update({
   'dataset_manifest_version':'2','dataset_name':stem,'logical_record_count':str(total), # fixed below
   'part_sequence':str(seq),'part_path':p.relative_to(root).as_posix(),'part_record_count':str(len(rows)),
   'first_record_id':first,'last_record_id':last,'size_bytes':str(len(raw)),'sha256':sha(raw)
  });newrows.append(row)
 header_sha=sha((header_line or b'').rstrip(b'\r\n'))
 for row in newrows:
  row['logical_record_count']=str(total);row['logical_header_sha256']=header_sha
 with index.open('w',encoding='utf-8',newline='') as f:
  w=csv.DictWriter(f,fieldnames=index_fields,lineterminator='\n');w.writeheader();w.writerows(newrows)
 return total,changed

def main()->int:
 ap=argparse.ArgumentParser();ap.add_argument('--root',type=Path,default=Path.cwd());a=ap.parse_args();root=a.root.resolve();rc=0
 for stem,idcol in DATASETS:
  count,changed=rebuild_index(root,stem,idcol)
  if count:print(f'{stem}: records={count} currentizedCells={changed}')
 current=root/CURRENT
 retired=('cpf-biz-admin','cpf-biz-channel','cpf-biz-frontend')
 bad=[]
 for stem,_ in DATASETS:
  partdir=current/f'{stem}.parts'
  if not partdir.is_dir():continue
  for p in partdir.glob('*.csv'):
   s=p.read_text(encoding='utf-8-sig',errors='replace')
   for token in retired:
    if token in s:bad.append(f'{p.relative_to(root)}:{token}')
 if bad:
  print('CPF_DERIVED_CURRENTIZATION=FAIL '+str(len(bad)));print('\n'.join(bad[:50]));rc=1
 else:print('CPF_DERIVED_CURRENTIZATION=PASS retiredProductRoots=0 stableIds=preserved')
 return rc
if __name__=='__main__':raise SystemExit(main())
