import csv
from pathlib import Path

ROOT = Path(__file__).resolve().parents[4]
MANIFEST = ROOT / 'cpf-docs/governance/development-harness/evidence/devgpt/current/CPF_C_DEV_QA_2_6_DELETE_MANIFEST.csv'
CURRENT_ONLY = ROOT / 'cpf-docs/governance/development-harness/standards/CPF_HARNESS_CURRENT_ONLY_AND_GARBAGE_STANDARD.md'
DELIVERY = ROOT / 'cpf-docs/governance/development-harness/standards/CPF_FINAL_DELIVERY_AND_HANDOVER_STANDARD.md'

EXPECTED = {
    'cpf-tools/build/gradle-plugin/.gradle',
    'cpf-tools/build/gradle-plugin/build',
    'cpf-tools/build/platform-bom/.gradle',
    'cpf-tools/build/tools/__pycache__',
}


def rows():
    with MANIFEST.open(encoding='utf-8-sig', newline='') as f:
        return list(csv.DictReader(f))


def test_delivery_generated_delete_manifest_is_typed_exact_root_allowlist():
    rs = rows()
    assert {r['path'] for r in rs} == EXPECTED
    assert all(r['type'] == 'GENERATED_ROOT' for r in rs)
    assert all(r['delete_mode'] == 'GENERATED_ROOT_ONLY' for r in rs)
    assert not any(r['path'] in {'editorconfig','gitattributes','gitignore'} or r['path'].startswith('github/') for r in rs)


def test_mutable_generated_cleanup_is_not_bound_to_stale_content_sha():
    for r in rows():
        assert 'sha' not in r['delete_mode'].lower()
    corpus = CURRENT_ONLY.read_text(encoding='utf-8') + '\n' + DELIVERY.read_text(encoding='utf-8')
    assert 'GENERATED_ROOT' in corpus
    assert '선행 `.`을 제거' in corpus
    assert '실행마다 byte/SHA가 바뀌' in corpus
