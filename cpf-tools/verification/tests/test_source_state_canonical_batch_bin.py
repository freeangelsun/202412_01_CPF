import importlib.util
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
P=ROOT/'cpf-tools/verification/tools/cpf-source-state.py'
spec=importlib.util.spec_from_file_location('cpf_source_state',P); mod=importlib.util.module_from_spec(spec); spec.loader.exec_module(mod)
def test_batch_standalone_shells_are_product_source():
    snap=mod.snapshot(ROOT,'source'); paths={r['path'] for r in snap['files']}
    for role in ('control-plane','scheduler','worker','agent','center-cut'):
        for name in ('run.ps1','stop.ps1','run.sh','stop.sh'):
            assert f'cpf-batch/{role}/bin/{name}' in paths
def test_noncanonical_compiled_bin_is_still_excluded():
    assert mod._is_generated('cpf-random/bin/com/acme/Foo.class')
