from pathlib import Path
import importlib.util

ROOT=Path(__file__).resolve().parents[3]
SCRIPT=ROOT/'cpf-tools/verification/verify-cpf-gradle-logical-tree.py'

def test_gate_script_is_git_independent_and_fail_closed():
    text=SCRIPT.read_text(encoding='utf-8')
    assert 'subprocess' not in text.lower()
    assert 'rev-parse' not in text.lower()
    assert 'retired Gradle project/task reference' in text
    assert "catalogModuleCount" in text
    assert "project(':runtime:batch').projectDir = file('cpf-batch')" in text

def test_current_tree_uses_five_logical_roots_and_no_flat_run_aliases():
    settings=(ROOT/'settings.gradle').read_text(encoding='utf-8')
    convention=(ROOT/'cpf-tools/build/cpf-root-conventions.gradle').read_text(encoding='utf-8')
    for root_group in ('apps','runtime','framework','starters','internal'):
        assert f"project(':{root_group}').projectDir" in settings
    for legacy in (':cpf-local-runtime:bootRun',':cpf-admin:bootRun',':cpf-backoffice:bootRun',':cpf-gateway:bootRun',':cpf-local-batch-runtime:bootRun',':cpf-education:bootRun'):
        assert legacy not in convention
