from __future__ import annotations

import hashlib
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
QUERY = ROOT / 'cpf-tools/verification/nxt3/verify_nxt3_query_db3.py'
RUNNER = ROOT / 'cpf-tools/verification/nxt3/run_nxt3_final_all.py'
POLICY = ROOT / 'cpf-tools/db/contracts/query-db3-policy.json'
ANNOTATION = ROOT / 'cpf-docs/work/evidence/current/ANNOTATION_RUNTIME_CONSUMER.json'
REDIS = ROOT / 'cpf-docs/work/evidence/current/REDIS_VALKEY_PROVIDER.json'


def _sha(path: Path) -> str:
    return hashlib.sha256(path.read_bytes()).hexdigest()


def test_query_db3_ignores_transient_module_build_but_keeps_cpf_tools_build(tmp_path: Path) -> None:
    transient = tmp_path / 'some-module/build/generated/bad.sql'
    transient.parent.mkdir(parents=True)
    transient.write_text('SELECT * FROM T LIMIT 10; -- mysql\n', encoding='utf-8')

    cp = subprocess.run(
        [sys.executable, str(QUERY), '--root', str(tmp_path), '--policy', str(POLICY)],
        text=True,
        capture_output=True,
    )
    assert cp.returncode == 0, cp.stdout + cp.stderr

    product = tmp_path / 'cpf-tools/build/product-contract.yml'
    product.parent.mkdir(parents=True)
    product.write_text('database: mysql\n', encoding='utf-8')
    cp = subprocess.run(
        [sys.executable, str(QUERY), '--root', str(tmp_path), '--policy', str(POLICY)],
        text=True,
        capture_output=True,
    )
    assert cp.returncode != 0
    assert 'cpf-tools/build/product-contract.yml' in cp.stdout


def test_nxt3_runner_redirects_child_evidence_and_children_respect_external_output(tmp_path: Path) -> None:
    runner = RUNNER.read_text(encoding='utf-8')
    assert "TemporaryDirectory(prefix='cpf-nxt3-child-evidence-')" in runner
    assert "verify_redis_valkey_provider_currentization.py" in runner and "child_evidence/'REDIS_VALKEY_PROVIDER.json'" in runner
    assert "verify_annotation_runtime_consumer.py" in runner and "child_evidence/'ANNOTATION_RUNTIME_CONSUMER.json'" in runner

    before = {_sha(ANNOTATION), _sha(REDIS)}
    annotation_out = tmp_path / 'annotation.json'
    redis_out = tmp_path / 'redis.json'
    commands = [
        [sys.executable, str(ROOT / 'cpf-tools/verification/nxt3/verify_annotation_runtime_consumer.py'), '--root', str(ROOT), '--evidence', str(annotation_out)],
        [sys.executable, str(ROOT / 'cpf-tools/verification/nxt3/verify_redis_valkey_provider_currentization.py'), '--root', str(ROOT), '--evidence', str(redis_out)],
    ]
    for cmd in commands:
        cp = subprocess.run(cmd, text=True, capture_output=True)
        assert cp.returncode == 0, cp.stdout + cp.stderr
    assert annotation_out.is_file() and redis_out.is_file()
    after = {_sha(ANNOTATION), _sha(REDIS)}
    assert after == before
