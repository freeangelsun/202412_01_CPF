from __future__ import annotations
from pathlib import Path
ROOT=Path(__file__).resolve().parents[4]
ENGINE=ROOT/'cpf-tools/generator/engine/cpf_domain_generator.py'
CLI=ROOT/'cpf-tools/runtime/cli/cpf.py'

def test_lifecycle_uses_one_os_neutral_engine_and_cli_surface():
    engine=ENGINE.read_text(encoding='utf-8')
    cli=CLI.read_text(encoding='utf-8')
    for token in ('def dry_run(','def diff(','def regenerate(','def upgrade(','def restore(','def remove_owned('): assert token in engine
    for command in ("'regenerate'","'upgrade'","'restore'","'remove'"): assert command in cli
    assert 'generator-ownership.json' not in engine
