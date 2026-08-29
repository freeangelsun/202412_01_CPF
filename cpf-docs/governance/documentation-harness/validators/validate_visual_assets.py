#!/usr/bin/env python3
import json,math,sys,hashlib
from pathlib import Path
ROOT=Path(__file__).resolve().parents[4]
M=ROOT/'cpf-docs/assets/product-docs/visual-geometry.json'
def fail(m): print('VISUAL_GEOMETRY=FAIL '+m); raise SystemExit(1)
def rect(o): return float(o['x']),float(o['y']),float(o['w']),float(o['h'])
def point_inside(x,y,r,strict=True):
    rx,ry,rw,rh=r
    return (rx < x < rx+rw and ry < y < ry+rh) if strict else (rx <= x <= rx+rw and ry <= y <= ry+rh)
def dist_to_boundary(x,y,r):
    rx,ry,rw,rh=r
    if not point_inside(x,y,r,False):
        dx=max(rx-x,0,x-(rx+rw)); dy=max(ry-y,0,y-(ry+rh)); return math.hypot(dx,dy)
    return min(abs(x-rx),abs(x-(rx+rw)),abs(y-ry),abs(y-(ry+rh)))
if not M.is_file(): fail('manifest missing')
d=json.loads(M.read_text(encoding='utf-8'))
if d.get('harnessVersion')!='2.15.1': fail('manifest harnessVersion; current assets must be regenerated/re-manifested for Harness 2.15.1')
if d.get('schemaVersion')!='2.0': fail('manifest schemaVersion 2.0 required')
assets=d.get('assets',[])
if not assets: fail('assets empty')
for a in assets:
    name=a.get('asset','')
    cw=float(a['canvas']['width']); ch=float(a['canvas']['height']); safe=float(a['canvas']['safeMargin'])
    if cw<=0 or ch<=0 or safe<64: fail('canvas '+name)
    ap=ROOT/name
    if not ap.is_file(): fail('asset missing '+name)
    try:
        from PIL import Image
        with Image.open(ap) as im:
            if im.size!=(int(cw),int(ch)): fail('canvas/image dimension mismatch '+name)
    except Exception as e: fail('image decode '+name+' '+str(e))
    if a.get('sha256') and hashlib.sha256(ap.read_bytes()).hexdigest().upper()!=str(a['sha256']).upper(): fail('asset sha256 mismatch '+name)
    objs={o['id']:o for o in a.get('objects',[])}
    meaningful=[o for o in objs.values() if o.get('kind') in ('node','text','annotation','junction')]
    if len(meaningful)<3: fail('coarse geometry manifest '+name)
    for o in objs.values():
        x,y,w,h=rect(o)
        if w<=0 or h<=0: fail('invalid box '+name+' '+o['id'])
        if x<0 or y<0 or x+w>cw or y+h>ch: fail('outside canvas '+name+' '+o['id'])
        if o.get('kind')=='text' and o.get('parent'):
            p=objs.get(o['parent'])
            if not p: fail('parent missing '+name+' '+o['id'])
            pad=28.0; px,py,pw,ph=rect(p)
            if x<px+pad or y<py+pad or x+w>px+pw-pad or y+h>py+ph-pad: fail('text outside parent '+name+' '+o['id'])
    conns=a.get('connectors')
    if conns is None: fail('connectors missing '+name)
    for c in conns:
        if not c.get('from') or not c.get('to') or c.get('from') not in objs or c.get('to') not in objs: fail('connector refs '+name+' '+str(c.get('id')))
        pts=c.get('points',[])
        if len(pts)<2: fail('connector route missing '+name+' '+str(c.get('id')))
        tx,ty=map(float,c.get('targetBoundaryPoint',pts[-1]))
        tr=rect(objs[c['to']])
        if dist_to_boundary(tx,ty,tr)>2.0: fail('connector endpoint not on target boundary '+name+' '+str(c.get('id')))
        if point_inside(tx,ty,tr,True): fail('connector target intrusion '+name+' '+str(c.get('id')))
        if float(c.get('targetInteriorPenetrationPx',0))>0: fail('connector target intrusion metric '+name+' '+str(c.get('id')))
        if float(c.get('sourceInteriorPenetrationPx',0))>0: fail('connector source intrusion metric '+name+' '+str(c.get('id')))
        if float(c.get('targetBoundaryDistancePx',0))>2.0: fail('connector endpoint boundary distance '+name+' '+str(c.get('id')))
        if float(c.get('arrowheadBodyInsideTargetPx',0))>0: fail('arrowhead inside target '+name+' '+str(c.get('id')))
        if c.get('crossesUnrelatedNodeInterior',False): fail('connector crosses node '+name+' '+str(c.get('id')))
        if c.get('crossesTextOrLabel',False): fail('connector crosses text '+name+' '+str(c.get('id')))
        if c.get('endsInUnlabeledEmptySpace',False): fail('connector empty end '+name+' '+str(c.get('id')))
    er=a.get('embeddedRenders')
    if not er: fail('embedded render metrics missing '+name)
    for r in er:
        if float(r.get('effectiveMinTextPt',0))<10.5: fail('embedded text too small '+name+' '+str(r.get('surface')))
        if int(r.get('crop',1))!=0 or int(r.get('boundaryIntrusion',1))!=0: fail('embedded crop/boundary '+name+' '+str(r.get('surface')))
        if r.get('contrastPass') is not True: fail('embedded contrast '+name+' '+str(r.get('surface')))
print('VISUAL_GEOMETRY=PASS ASSETS='+str(len(assets)))
