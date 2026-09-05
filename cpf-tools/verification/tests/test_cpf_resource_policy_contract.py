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
    assert "jvmArgs '-Xshare:off'" in text


def test_adm_frontend_physically_splits_oversized_chunks_without_relaxing_warning_limit():
    text = (ROOT / 'cpf-admin/frontend/vite.config.ts').read_text(encoding='utf-8')
    assert 'codeSplitting:' in text
    assert 'maxSize: 400_000' in text
    assert 'chunkSizeWarningLimit' not in text

def test_generated_runtime_capability_is_the_only_archive_input_and_other_duplicates_fail():
    text = (ROOT / 'cpf-tools/build/cpf-root-conventions.gradle').read_text(encoding='utf-8')
    assert "compatibilityCapabilityFile = file('src/main/resources/META-INF/cpf/runtime-capability.properties').canonicalFile" in text
    assert 'element.file.canonicalFile == compatibilityCapabilityFile' in text
    assert "sourceSets.main.resources.srcDir(generatedCapabilityDir)" in text
    assert text.count('duplicatesStrategy = DuplicatesStrategy.FAIL') >= 2
    assert 'duplicatesStrategy = DuplicatesStrategy.EXCLUDE' not in text

def test_root_generated_and_jvm_failure_artifacts_use_managed_evidence_paths():
    convention = (ROOT / 'cpf-tools/build/cpf-root-conventions.gradle').read_text(encoding='utf-8')
    gradle_properties = (ROOT / 'gradle.properties').read_text(encoding='utf-8')
    wrapper_bat = (ROOT / 'gradlew.bat').read_text(encoding='utf-8')
    wrapper_sh = (ROOT / 'gradlew').read_text(encoding='utf-8')
    ignore = (ROOT / '.gitignore').read_text(encoding='utf-8')

    managed = 'cpf-docs/governance/development-harness/evidence/platform/current/generated'
    assert managed in convention
    assert "rootProject.layout.buildDirectory.set" in convention
    assert 'org.gradle.projectcachedir=' not in gradle_properties
    root_settings = (ROOT / 'settings.gradle').read_text(encoding='utf-8')
    assert "gradle.startParameter.projectCacheDir = new File(cpfManagedGradleRoot, 'project-cache')" in root_settings
    assert '-XX:ErrorFile=' in convention
    assert '-XX:HeapDumpPath=' in convention
    for wrapper in (wrapper_bat, wrapper_sh):
        assert managed.replace('/', '\\') in wrapper or managed in wrapper
        assert '--project-cache-dir' in wrapper
        assert 'cpfManagedGradleRoot' in wrapper
        assert 'JAVA_TOOL_OPTIONS' in wrapper
        assert '-XX:ErrorFile=' in wrapper
        assert '-XX:HeapDumpPath=' in wrapper
    assert 'java-hs_err_pid%%p.log' in wrapper_bat
    assert '-XX:ErrorFile=\\"' not in wrapper_bat
    assert '/cpf-docs/governance/development-harness/evidence/platform/current/generated/' in ignore


def test_root_windows_gradle_wrapper_requires_explicit_cpf_debug_opt_in():
    """상위 Shell의 일반 DEBUG 환경변수는 Gradle wrapper UX를 오염시키지 않는다."""
    wrapper = (ROOT / 'gradlew.bat').read_text(encoding='utf-8')
    assert 'CPF_GRADLE_DEBUG' in wrapper
    assert '%DEBUG%' not in wrapper
    assert '@echo off' in wrapper
