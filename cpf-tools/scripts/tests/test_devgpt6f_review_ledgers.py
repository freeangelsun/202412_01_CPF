from __future__ import annotations
import csv, json, subprocess, sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
SCRIPT = ROOT / 'cpf-tools/scripts/build-devgpt6f-review-ledgers.py'
SCOPE = ROOT / 'cpf-docs/work/current/development-session-results/DEV-20260805-R01/DEVGPT-6F/REV-001/scope'

def count(path: Path) -> int:
    with path.open(encoding='utf-8-sig', newline='') as f:
        return sum(1 for _ in csv.DictReader(f))

def test_review_builder_is_fail_closed_and_complete(tmp_path: Path):
    out = tmp_path / 'review'
    p = subprocess.run([sys.executable, str(SCRIPT), '--root', str(ROOT), '--scope-dir', str(SCOPE), '--output-dir', str(out), '--baseline-sha', '09dd686c5ae0826594b9c5e1f871d95d95d3ce1c'], text=True, capture_output=True)
    assert p.returncode == 0, p.stdout + p.stderr
    assert count(out/'WORK_ITEM_DEVELOPMENT_REVIEW.csv') == 224
    assert count(out/'REQUIREMENT_DEVELOPMENT_REVIEW.csv') == 5658
    assert count(out/'SCENARIO_DEVELOPMENT_REVIEW.csv') == 7878
    assert count(out/'ENGINEERING_GATE_RESULT.csv') == 21
    data=json.loads((out/'REVIEW_COVERAGE_VALIDATION.json').read_text(encoding='utf-8'))
    assert data['unreviewedWorkItems']==0
    assert data['unreviewedRequirements']==0
    assert data['unreviewedScenarios']==0
    assert data['missingEvidence']==0
    assert data['missingConsumer']==0
    assert data['finalPassPromotions']==0

def test_wrong_baseline_rejected(tmp_path: Path):
    p=subprocess.run([sys.executable,str(SCRIPT),'--root',str(ROOT),'--scope-dir',str(SCOPE),'--output-dir',str(tmp_path/'o'),'--baseline-sha','0'*40], text=True,capture_output=True)
    assert p.returncode != 0
