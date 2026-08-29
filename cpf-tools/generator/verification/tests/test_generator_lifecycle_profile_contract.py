from __future__ import annotations
from pathlib import Path
ROOT=Path(__file__).resolve().parents[4]
ENGINE=ROOT/'cpf-tools/generator/engine/cpf_domain_generator.py'
CLI=ROOT/'cpf-tools/runtime/cli/cpf.py'
REMOVE_WRAPPER=ROOT/'cpf-tools/generator/tools/remove-domain.ps1'
LIFECYCLE_SMOKE=ROOT/'cpf-tools/generator/verification/smoke-generated-domain-lifecycle.ps1'

def test_lifecycle_uses_one_os_neutral_engine_and_cli_surface():
    engine=ENGINE.read_text(encoding='utf-8')
    cli=CLI.read_text(encoding='utf-8')
    for token in ('def dry_run(','def diff(','def regenerate(','def upgrade(','def restore(','def remove_owned('): assert token in engine
    for command in ("'regenerate'","'upgrade'","'restore'","'remove'"): assert command in cli
    assert 'generator-ownership.json' not in engine

def test_disposable_remove_approval_is_hidden_and_scoped_to_physical_lifecycle_smoke():
    engine=ENGINE.read_text(encoding='utf-8')
    cli=CLI.read_text(encoding='utf-8')
    wrapper=REMOVE_WRAPPER.read_text(encoding='utf-8')
    smoke=LIFECYCLE_SMOKE.read_text(encoding='utf-8')
    assert "approved_disposable_lifecycle: bool=False" in engine
    assert "cpf-docs/governance/development-harness/evidence/platform/current/generated/domain-generator" in engine
    assert "expected_definition=(lifecycle/'definition/cpf-domain.yaml').resolve()" in engine
    assert "--approved-disposable-lifecycle" in cli and "help=argparse.SUPPRESS" in cli
    assert "ApprovedDisposableLifecycle" in wrapper
    assert "ConfirmGeneratedSourceRemoval" in smoke and "-ApprovedDisposableLifecycle" in smoke
