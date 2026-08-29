#!/usr/bin/env python3
import json,re,sys,statistics,argparse
from pathlib import Path
import fitz
H=Path(__file__).resolve().parents[1]; ROOT=H.parents[2]; C=json.loads((H/'rendered-page-composition.json').read_text(encoding='utf-8'))
def analyze(pg):
 h=pg.rect.height; w=pg.rect.width; top=h*C['contentBand']['topIgnoreRatio']; bot=h*C['contentBand']['bottomIgnoreRatio']; rects=[]; lines=[]; total=small=0; minfont=99
 for b in pg.get_text('dict').get('blocks',[]):
  if b.get('type')==0:
   for ln in b.get('lines',[]):
    spans=[s for s in ln.get('spans',[]) if str(s.get('text','')).strip()]
    if not spans: continue
    y0=min(s['bbox'][1] for s in spans); y1=max(s['bbox'][3] for s in spans); cy=(y0+y1)/2
    if cy<top or cy>bot: continue
    txt=''.join(str(s.get('text','')) for s in spans).strip(); fs=max(float(s.get('size',0)) for s in spans); minfont=min(minfont,min(float(s.get('size',99)) for s in spans)); chars=len(re.sub(r'\s+','',txt)); total+=chars; small+=sum(len(re.sub(r'\s+','',str(s.get('text','')))) for s in spans if float(s.get('size',99))<C['smallText']['thresholdPt']); lines.append((y0,y1,fs,txt)); rects.append((y0,y1))
  elif b.get('type')==1:
   y0,y1=b['bbox'][1],b['bbox'][3]; cy=(y0+y1)/2
   if top<=cy<=bot: rects.append((y0,y1))
 try:
  for dr in pg.get_drawings():
   r=dr.get('rect')
   if r and not r.is_empty and (r.width>w*.03 or r.height>h*.02):
    cy=(r.y0+r.y1)/2
    if top<=cy<=bot: rects.append((r.y0,r.y1))
 except: pass
 if rects: t=min(x[0] for x in rects)/h; b=max(x[1] for x in rects)/h
 else:t=b=0
 dots=sum(1 for _,_,_,x in lines if re.search(r'\.{12,}',x)); return {'top':t,'bottom':b,'span':b-t,'blocks':len(rects),'lines':len(lines),'chars':total,'minFont':minfont if minfont<99 else None,'smallShare':small/total if total else 0,'lineData':lines,'isToc':dots>=C['tocComposition']['dotLeaderLinesMin'],'height':h}
def main():
 ap=argparse.ArgumentParser(); ap.add_argument('--json-out'); a=ap.parse_args(); errs=[]; reviews=[]; rep={'harnessVersion':C['harnessVersion'],'documents':[]}
 for s in C['officialPdfs']:
  p=ROOT/s['path'];
  if not p.is_file(): errs.append(s['id']+': missing'); continue
  d=fitz.open(p); ms=[analyze(x) for x in d]; med=statistics.median([m['bottom'] for m in ms[1:-1] if m['bottom']]) if len(ms)>2 else .7
  for i,m in enumerate(ms):
   n=i+1
   if i==len(ms)-1:
    lp=C['lastPage'];
    if m['bottom']<lp['minContentBottomRatioHardFail'] and m['chars']<lp['tailRuleMaxChars']: errs.append(f"{s['id']}: page {n}/{len(ms)} sparse last page bottom={m['bottom']:.3f}")
    elif m['bottom']<lp['minContentBottomRatioReview']: reviews.append(f"{s['id']}: page {n}/{len(ms)} last-page review bottom={m['bottom']:.3f}")
   elif i>0 and not m['isToc']:
    ip=C['interiorPage'];
    if m['bottom']<ip['minContentBottomRatioHardFail'] and m['span']<ip['minVerticalSpanRatioHardFail']: errs.append(f"{s['id']}: page {n}/{len(ms)} interior dead-space")
    elif m['bottom']<ip['minContentBottomRatioReview']: reviews.append(f"{s['id']}: page {n}/{len(ms)} interior balance review bottom={m['bottom']:.3f}")
   if m['smallShare']>C['smallText']['maxCharShareHardFail']: errs.append(f"{s['id']}: page {n} small-text share={m['smallShare']:.1%}")
   if m['lines']>=C['densePage']['textLinesHardFail'] and m['bottom']>=C['densePage']['contentBottomRatioAtHardFail']: errs.append(f"{s['id']}: page {n} dense page lines={m['lines']}")
   elif m['lines']>=C['densePage']['textLinesReview']: reviews.append(f"{s['id']}: page {n}/{len(ms)} high-density review lines={m['lines']}")
  rep['documents'].append({'id':s['id'],'pages':len(d),'metrics':[{k:v for k,v in m.items() if k!='lineData'} for m in ms]})
 rep['errors']=errs; rep['reviewSignals']=reviews
 if a.json_out: Path(a.json_out).write_text(json.dumps(rep,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
 if errs: print('RENDERED_PAGE_COMPOSITION=FAIL COUNT='+str(len(errs))); [print('-',e) for e in errs]; return 1
 print('RENDERED_PAGE_COMPOSITION=PASS PDF='+str(len(rep['documents']))); print('REVIEW_SIGNALS='+str(len(reviews))); [print('~',e) for e in reviews]; return 0
if __name__=='__main__': raise SystemExit(main())
