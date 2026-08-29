#!/usr/bin/env python3
import subprocess,sys
from pathlib import Path
H=Path(__file__).resolve().parents[1]; V=H/'validators'; ROOT=H.parents[2]
def run(s,*a):
 r=subprocess.run([sys.executable,str(V/s),*map(str,a)],cwd=ROOT);
 if r.returncode: raise SystemExit(r.returncode)
def main():
 if len(sys.argv)<2: print('ALL_GATES=FAIL final acceptance manifest path required'); return 2
 run('validate_harness.py'); run('validate_source_alignment.py'); run('validate_source_currentization.py'); run('validate_quality_fixtures.py'); run('validate_readme.py',ROOT/'README.md'); run('validate_docx_artifacts.py'); run('validate_reader_task_coverage.py'); run('validate_readability_actionability.py'); run('validate_visual_assets.py'); run('validate_rendered_page_composition.py'); run('validate_visual_comfort.py'); run('validate_final_acceptance.py',Path(sys.argv[1])); print('ALL_GATES=PASS'); return 0
if __name__=='__main__': raise SystemExit(main())
