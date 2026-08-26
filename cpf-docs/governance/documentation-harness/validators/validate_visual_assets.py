#!/usr/bin/env python3
import json
from pathlib import Path
ROOT=Path(__file__).resolve().parents[4]
M=ROOT/'cpf-docs/assets/product-docs/visual-geometry.json'
def fail(m): print('VISUAL_GEOMETRY=FAIL '+m); raise SystemExit(1)
if not M.is_file(): fail('manifest missing')
d=json.loads(M.read_text(encoding='utf-8'))
if d.get('harnessVersion')!='2.3.0': fail('manifest harnessVersion')
for a in d.get('assets',[]):
    cw=float(a['canvas']['width']); ch=float(a['canvas']['height']); safe=float(a['canvas']['safeMargin'])
    if cw<=0 or ch<=0 or safe<64: fail('canvas '+a.get('asset',''))
    ap=ROOT/a['asset']
    if not ap.is_file(): fail('asset missing '+a['asset'])
    objs={o['id']:o for o in a.get('objects',[])}
    for o in objs.values():
        x,y,w,h=map(float,(o['x'],o['y'],o['w'],o['h']))
        if w<=0 or h<=0: fail('invalid box '+a['asset']+' '+o['id'])
        if x<safe or y<safe or x+w>cw-safe or y+h>ch-safe:
            if str(o.get('allowSafeMargin','')).lower()!='true': fail('safe-area '+a['asset']+' '+o['id'])
        if o.get('kind')=='text' and o.get('parent') is not None:
            p=objs.get(o['parent'])
            if not p: fail('parent missing '+a['asset']+' '+o['id'])
            pad=28.0
            if x<float(p['x'])+pad or y<float(p['y'])+pad or x+w>float(p['x'])+float(p['w'])-pad or y+h>float(p['y'])+float(p['h'])-pad:
                fail('text outside parent '+a['asset']+' '+o['id'])
    for pair in a.get('noOverlap',[]):
        one,two=objs.get(pair[0]),objs.get(pair[1])
        if not one or not two: fail('overlap ref missing '+a['asset'])
        over=(float(one['x'])<float(two['x'])+float(two['w']) and float(one['x'])+float(one['w'])>float(two['x']) and float(one['y'])<float(two['y'])+float(two['h']) and float(one['y'])+float(one['h'])>float(two['y']))
        if over: fail('overlap '+a['asset']+' '+pair[0]+' '+pair[1])
    for g in a.get('minVerticalGaps',[]):
        one,two=objs.get(g['from']),objs.get(g['to'])
        if not one or not two: fail('gap ref missing '+a['asset'])
        gap=float(two['y'])-(float(one['y'])+float(one['h']))
        if gap<float(g['min']): fail(f"vertical gap {a['asset']} {g['from']} {g['to']} actual={gap}")
print('VISUAL_GEOMETRY=PASS ASSETS='+str(len(d.get('assets',[]))))
