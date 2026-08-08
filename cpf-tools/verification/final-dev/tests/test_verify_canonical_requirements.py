import importlib.util
from pathlib import Path

def load():
    p=Path(__file__).parents[1]/'verify-canonical-requirements.py'
    spec=importlib.util.spec_from_file_location('gate',p)
    m=importlib.util.module_from_spec(spec); spec.loader.exec_module(m); return m

def test_current_canonical_contract_passes():
    m=load(); root=Path(__file__).parents[4]
    assert m.verify(root/'cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md')==[]

def test_mutated_declared_count_fails(tmp_path):
    m=load(); root=Path(__file__).parents[4]
    text=(root/'cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md').read_text()
    mutated=text.replace('현재 Canonical Requirement Count는 **169개**','현재 Canonical Requirement Count는 **168개**')
    p=tmp_path/'mutated.md'; p.write_text(mutated)
    assert any('section21' in e for e in m.verify(p))

def test_alias_cannot_inflate_denominator(tmp_path):
    m=load(); root=Path(__file__).parents[4]
    text=(root/'cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md').read_text()
    text=text.replace('| `FACADE-LOCAL` |', '| `CPF-TXID` |',1)
    p=tmp_path/'mutated.md'; p.write_text(text)
    assert any('inflate canonical denominator' in e for e in m.verify(p))
