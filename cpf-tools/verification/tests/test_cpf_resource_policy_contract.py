from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[3]
PROFILES = ('common','local','dev','test','stg','prod')
MEMORY_KEYS = ('gradle.jvm.xms','gradle.jvm.xmx','test.xms','test.xmx','runtime.web.xms','runtime.web.xmx','runtime.batch.xms','runtime.batch.xmx')

def props(path: Path):
    out = {}
    for raw in path.read_text(encoding='utf-8').splitlines():
        line = raw.strip()
        if not line or line.startswith('#'):
            continue
        k, v = line.split('=', 1)
        assert k not in out
        out[k] = v
    return out

def mb(value: str) -> int:
    m = re.fullmatch(r'(\d+)([mMgG])', value)
    assert m, value
    n = int(m.group(1))
    return n * 1024 if m.group(2).lower() == 'g' else n

def test_profiles_exist_and_use_canonical_memory_steps():
    base = props(ROOT / 'gradle/cpf-runtime/common.properties')
    assert base['heap.step.mb'] == '250'
    assert base['runtime.memory.ceiling.mb'] == '1000'
    for profile in PROFILES:
        path = ROOT / f'gradle/cpf-runtime/{profile}.properties'
        assert path.is_file(), path
        merged = dict(base)
        merged.update(props(path))
        for key in MEMORY_KEYS:
            value = mb(merged[key])
            assert 250 <= value <= 1000, (profile, key, merged[key])
            assert value % 250 == 0, (profile, key, merged[key])

def test_module_override_is_optional_and_has_highest_file_priority():
    script = (ROOT / 'cpf-tools/runtime/tools/cpf-resource-policy.ps1').read_text(encoding='utf-8')
    assert "Join-Path $ModuleDir 'cpf-resource.properties'" in script
    assert script.index('foreach ($entry in $Explicit.GetEnumerator())') < script.index("$moduleFile = Join-Path $ModuleDir 'cpf-resource.properties'")
    assert 'must use ${step}MB increments' in script

def test_gradle_convention_applies_central_policy_to_all_tests_and_boot_runs():
    text = (ROOT / 'cpf-tools/build/cpf-root-conventions.gradle').read_text(encoding='utf-8')
    assert "gradle/cpf-runtime/${cpfResourceProfile}.properties" in text
    assert "target.file('cpf-resource.properties')" in text
    assert "tasks.withType(Test).configureEach" in text
    assert "tasks.matching { it.name == 'bootRun' }.configureEach" in text
    assert "tasks.named('bootRun')" not in text
    assert "tasks.register('cpfResourcePolicy')" in text
    assert "tasks.register('cpfModules')" in text
