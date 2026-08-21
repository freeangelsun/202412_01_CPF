import importlib.util, json, tempfile
from pathlib import Path

SCRIPT=Path(__file__).parents[3]/'verification'/'tools'/'run-cpf-canonical-verifiers.py'

def test_registry_is_nonempty_and_paths_are_root_relative():
    root=Path(__file__).parents[3]
    data=json.loads((root/'verification'/'contracts'/'cpf-verifier-registry.json').read_text(encoding='utf-8'))
    assert data['verifiers']
    ids=[x['id'] for x in data['verifiers']]
    assert len(ids)==len(set(ids))
    assert all(not Path(x['path']).is_absolute() for x in data['verifiers'])

def test_runner_source_has_single_root_policy():
    text=SCRIPT.read_text(encoding='utf-8')
    assert "'--root',str(root)" in text
    assert 'registry missing or escapes root' in text
