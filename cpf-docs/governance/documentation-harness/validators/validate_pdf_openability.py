#!/usr/bin/env python3
import shutil,subprocess,sys,tempfile
from pathlib import Path
H=Path(__file__).resolve().parents[1]
ROOT=H.parents[2]

def fail(msg):
    print('PDF_OPENABILITY=FAIL '+msg)
    raise SystemExit(1)

def pdfs():
    return sorted(list((ROOT/'cpf-docs/guides').glob('*.pdf'))+list((ROOT/'cpf-docs/deliverables').glob('*.pdf')))

def main():
    files=pdfs()
    if len(files)!=11: fail(f'expected 11 official PDFs, got {len(files)}')
    total=0
    try:
        import fitz
        from pypdf import PdfReader
    except Exception as e: fail('parser dependency unavailable: '+str(e))
    try:
        import pypdfium2 as pdfium
    except Exception as e: fail('PDFium dependency unavailable: '+str(e))
    poppler=shutil.which('pdftoppm')
    pdfinfo=shutil.which('pdfinfo')
    for p in files:
        b=p.read_bytes()
        if len(b)<1024: fail(f'{p.name}: too small')
        if b.startswith(b'version https://git-lfs.github.com/spec'): fail(f'{p.name}: Git LFS pointer')
        if not b.startswith(b'%PDF-'): fail(f'{p.name}: missing PDF header')
        if b'%%EOF' not in b[-8192:]: fail(f'{p.name}: missing EOF marker')
        try:
            r=PdfReader(str(p))
            if r.is_encrypted: fail(f'{p.name}: encrypted')
            n1=len(r.pages)
        except Exception as e: fail(f'{p.name}: pypdf open failed: {e}')
        try:
            d=fitz.open(str(p)); n2=d.page_count
            if n2:
                for idx in sorted(set([0,n2-1])):
                    page=d.load_page(idx); pix=page.get_pixmap(matrix=fitz.Matrix(0.35,0.35),alpha=False)
                    if pix.width<=0 or pix.height<=0: fail(f'{p.name}: PyMuPDF render produced empty pixmap')
            d.close()
        except Exception as e: fail(f'{p.name}: PyMuPDF open/render failed: {e}')
        try:
            d=pdfium.PdfDocument(str(p)); n4=len(d)
            if n4:
                for idx in sorted(set([0,n4-1])):
                    bmp=d[idx].render(scale=0.35)
                    pil=bmp.to_pil()
                    if pil.width<=0 or pil.height<=0: fail(f'{p.name}: PDFium render produced empty image')
            d.close()
        except Exception as e: fail(f'{p.name}: PDFium open/render failed: {e}')
        if n1<=0 or n1!=n2 or n1!=n4: fail(f'{p.name}: parser page count mismatch pypdf={n1} pymupdf={n2} pdfium={n4}')
        if pdfinfo:
            q=subprocess.run([pdfinfo,str(p)],capture_output=True,text=True)
            if q.returncode!=0: fail(f'{p.name}: pdfinfo failed')
            page_line=next((x for x in q.stdout.splitlines() if x.startswith('Pages:')),None)
            if page_line:
                try:n3=int(page_line.split(':',1)[1].strip())
                except Exception:n3=-1
                if n3!=n1: fail(f'{p.name}: pdfinfo page count mismatch {n3}!={n1}')
        if poppler:
            with tempfile.TemporaryDirectory(prefix='cpf-pdf-open-') as td:
                for pg in sorted(set([1,n1])):
                    out=Path(td)/f'p{pg}'
                    q=subprocess.run([poppler,'-f',str(pg),'-singlefile','-png','-r','54',str(p),str(out)],capture_output=True,text=True)
                    png=Path(str(out)+'.png')
                    if q.returncode!=0 or not png.is_file() or png.stat().st_size==0:
                        fail(f'{p.name}: Poppler render failed page {pg}')
        total+=n1
    engines=['pypdf','PyMuPDF','PDFium']
    if pdfinfo: engines.append('pdfinfo')
    if poppler: engines.append('Poppler')
    print(f'PDF_OPENABILITY=PASS PDF={len(files)} PAGES={total}')
    print('PARSERS_RENDERERS='+','.join(engines))
    print('NOTE=VS Code Text Editor may display valid PDF bytes as %PDF binary text; that is an editor association issue, not PDF corruption.')
    return 0
if __name__=='__main__': raise SystemExit(main())
