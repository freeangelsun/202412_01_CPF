from __future__ import annotations

import importlib.util
import tempfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
NXT3 = ROOT / 'cpf-tools/verification/nxt3'


def load_module(name: str, path: Path):
    spec = importlib.util.spec_from_file_location(name, path)
    module = importlib.util.module_from_spec(spec)
    assert spec.loader is not None
    spec.loader.exec_module(module)
    return module


def test_generated_domain_runtime_caches_do_not_expand_minimal_ia():
    layout = load_module('generated_domain_layout_test', NXT3 / 'generated_domain_layout.py')
    with tempfile.TemporaryDirectory() as td:
        root = Path(td) / 'cpf-member'
        (root / 'online').mkdir(parents=True)
        (root / 'online' / 'build.gradle').write_text('// source\n', encoding='utf-8')
        for generated in ('.gradle', '.pytest_cache', 'build', 'out', 'node_modules'):
            target = root / generated
            target.mkdir(parents=True)
            (target / 'cache.bin').write_bytes(b'generated')
        assert layout.domain_surface_dirs(root) == {'online'}


def test_repository_generated_cache_policy_preserves_cpf_tools_build_product_source():
    garbage = load_module('nxt3_garbage_test', NXT3 / 'verify_nxt3_repository_garbage.py')
    hygiene = load_module('nxt3_hygiene_test', NXT3 / 'verify_nxt3_hygiene.py')
    paths = (
        'build/cpf-local-validation/python-env/Lib/site-packages/x/__pycache__/a.pyc',
        'cpf-member/.gradle/8.0/fileHashes/fileHashes.bin',
        '.pytest_cache/v/cache/nodeids',
        'cpf-member/online/build/classes/A.class',
    )
    for path in paths:
        assert garbage.is_generated_cache_path(path)
        assert hygiene.is_generated_cache_path(path)
    product_source = 'cpf-tools/build/gradle-plugin/src/main/java/com/cpf/Plugin.java'
    assert not garbage.is_generated_cache_path(product_source)
    assert not hygiene.is_generated_cache_path(product_source)
    nested_generated = 'cpf-tools/build/gradle-plugin/build/classes/java/main/Plugin.class'
    assert garbage.is_generated_cache_path(nested_generated)
    assert hygiene.is_generated_cache_path(nested_generated)


def test_full_local_uses_external_scratch_and_external_python_environment():
    source = (ROOT / 'cpf-tools/verification/tools/run-cpf-local-full-validation.ps1').read_text(encoding='utf-8')
    assert '[IO.Path]::GetTempPath()' in source
    assert '$finalResultDir' in source
    assert 'Ensure-CpfResultDirectories' in source
    assert 'cpf-local-stage-' in source
    assert "Join-Path $env:LOCALAPPDATA 'CPF\\validation'" in source
    assert "Join-Path $RepoRoot 'build\\cpf-local-validation\\python-env'" not in source
    assert 'Copy-Item -Path (Join-Path $resultDir' in source
    assert 'Compress-Archive -Path (Join-Path $finalResultDir' in source
