#!/usr/bin/env python3
import argparse,json,re,sys
from pathlib import Path
H=Path(__file__).resolve().parents[1]; DEFAULT_ROOT=H.parents[2]
def plain(md):
    s=re.sub(r'```.*?```',' ',md,flags=re.S); s=re.sub(r'<!--.*?-->',' ',s,flags=re.S)
    s=re.sub(r'!\[[^\]]*\]\([^)]*\)',' ',s); s=re.sub(r'<img\b[^>]*>',' ',s,flags=re.I)
    s=re.sub(r'\[([^\]]+)\]\([^)]+\)',r'\1',s); s=re.sub(r'[#>*_`|~-]+',' ',s)
    return re.sub(r'\s+',' ',s).strip()
def parse_set(text,name):
    m=re.search(rf'private static final Set<String> {name} = Set\.of\((.*?)\);',text,re.S)
    return re.findall(r'"([^"]+)"',m.group(1)) if m else []
def main():
    ap=argparse.ArgumentParser(); ap.add_argument('readme',nargs='?',default=None); ap.add_argument('--source-root',default=None); a=ap.parse_args()
    root=Path(a.source_root).resolve() if a.source_root else DEFAULT_ROOT
    p=Path(a.readme).resolve() if a.readme else root/'README.md'; cfg=json.loads((H/'readme-product-completeness.json').read_text(encoding='utf-8'))
    text=p.read_text(encoding='utf-8'); vis=plain(text); errs=[]; depth=cfg['minimumDepth']
    def e(x): errs.append(x)
    if len(vis)<depth['visibleCharactersTotal']: e(f'visible explanation too thin {len(vis)}<{depth["visibleCharactersTotal"]}')
    h2=list(re.finditer(r'(?m)^##\s+(.+)$',text));
    if len(h2)<depth['h2Sections']: e(f'H2 coverage too small {len(h2)}<{depth["h2Sections"]}')
    # Sections with visible text.
    sections=[]
    for i,m in enumerate(h2):
        end=h2[i+1].start() if i+1<len(h2) else len(text); sections.append((m.group(1).strip(),plain(text[m.end():end]),m.start()))
    substantive=sum(1 for _,s,_ in sections for para in re.split(r'(?<=[.!?다요])\s+',s) if len(para.strip())>=90)
    if substantive<depth['substantiveParagraphs']: e(f'substantive explanation blocks {substantive}<{depth["substantiveParagraphs"]}')
    # Coverage groups.
    for g in cfg['requiredCoverageGroups']:
        if g.get('sourceDerived'): continue
        hits=sum(1 for tok in g.get('any',[]) if tok.lower() in text.lower())
        if hits<int(g.get('minHits',1)): e(f'coverage group {g["id"]} {hits}<{g.get("minHits",1)}')
    # Whole architecture section and figure placement.
    arch=[x for x in sections if re.search(r'(아키텍처|전체\s*구조|architecture|구성)',x[0],re.I)]
    if not arch: e('whole architecture section missing')
    else:
        archtext=max(arch,key=lambda x:len(x[1]))[1]
        if len(archtext)<depth['architectureSectionCharacters']: e(f'architecture explanation too thin {len(archtext)}<{depth["architectureSectionCharacters"]}')
    imgpos=[m.start() for m in re.finditer(r'(?i)(!\[[^\]]*\]\([^)]*architecture[^)]*\)|<img\b[^>]*architecture[^>]*>)',text)]
    if not imgpos: e('architecture visual reference missing')
    elif min(imgpos)/max(1,len(text))*100>cfg['architecture']['mustAppearBeforePercent']: e('architecture visual appears too late')
    # Developer journey section depth.
    dev=[x for x in sections if re.search(r'(개발|시작|사용|CLI|흐름)',x[0],re.I)]
    if not dev or max(len(x[1]) for x in dev)<depth['developerJourneyCharacters']: e('developer journey explanation too thin')
    ops=[x for x in sections if re.search(r'(운영|실패|복구|거래|추적|상태)',x[0],re.I)]
    if not ops or max(len(x[1]) for x in ops)<depth['operationsFailureRecoveryCharacters']: e('operations/failure/recovery explanation too thin')
    nav=[x for x in sections if re.search(r'(문서|가이드|더\s*알아|참고|Reference)',x[0],re.I)]
    if not nav or max(len(x[1]) for x in nav)<depth['documentationNavigationCharacters']: e('documentation navigation explanation too thin')
    # Current CLI public surface; every command mentioned, 10 substantively explained. No hardcoded command list in this validator.
    candidates=list(root.rglob('CpfCli.java'))
    clip=candidates[0] if len(candidates)==1 else None
    if clip is None:
        e('current public CLI authority could not be uniquely discovered')
    else:
        cli=clip.read_text(encoding='utf-8'); public=parse_set(cli,'PUBLIC'); internal=parse_set(cli,'INTERNAL_NAMESPACES')
        missing=[]; explained=0
        for cmd in public:
            ms=list(re.finditer(rf'(?<![\w-])(?:cpf\s+)?{re.escape(cmd)}(?![\w-])',text,re.I))
            if not ms: missing.append(cmd); continue
            ok=False
            for m in ms:
                ctx=plain(text[max(0,m.start()-240):min(len(text),m.end()+240)])
                if len(re.findall(r'[가-힣]',ctx))>=35 and len(ctx)>=120: ok=True; break
            if ok: explained+=1
        if missing: e('current public CLI missing from README: '+','.join(missing))
        if explained<int(cfg['developerGoldenPath'].get('minimumPublicCommandsExplained',10)): e(f'CLI explanation count too low {explained}')
        # Public/Internal boundary must be explicit when internal namespaces exist.
        if internal and not (re.search(r'(Public|일반\s*개발자)',text,re.I) and re.search(r'(Internal|내부\s*개발|프레임워크\s*개발)',text,re.I)): e('Public/Internal CLI boundary explanation missing')
    # Benefit/value depth: require substantive contexts, not tokens only.
    theme_hits=[]; section_ids=set()
    for theme in cfg['valueExplanation'].get('themeEvidence',[]):
        found=False
        for si,(hd,sec,_) in enumerate(sections):
            toks=[tok for tok in theme['any'] if tok.lower() in sec.lower()]
            if toks and len(sec)>=cfg['valueExplanation']['minimumContextCharsAroundEvidence']:
                found=True; section_ids.add(si)
        if found: theme_hits.append(theme['id'])
    if len(theme_hits)<cfg['valueExplanation']['requiredValueThemesMin']: e(f'substantive value themes {len(theme_hits)}<{cfg["valueExplanation"]["requiredValueThemesMin"]}')
    if len(section_ids)<cfg['valueExplanation']['distinctSectionsMin']: e(f'value explanations spread only {len(section_ids)} sections')
    if errs:
        print('README_PRODUCT_COMPLETENESS=FAIL COUNT='+str(len(errs))); [print('-',x) for x in errs]; return 1
    print('README_PRODUCT_COMPLETENESS=PASS'); print('README_ARCHITECTURE_COMPLETENESS_PASS=PASS'); print('README_EXPLANATION_DEPTH_PASS=PASS'); print('README_DEVELOPER_GOLDEN_PATH_PASS=PASS'); print('VISIBLE_CHARS='+str(len(vis))+' H2='+str(len(h2))+' SUBSTANTIVE='+str(substantive)); return 0
if __name__=='__main__': raise SystemExit(main())
