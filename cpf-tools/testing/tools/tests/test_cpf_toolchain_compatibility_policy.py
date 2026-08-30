import json
from pathlib import Path
ROOT=Path(__file__).resolve().parents[4]
POLICY=ROOT/'cpf-tools/verification/contracts/cpf-toolchain-compatibility.json'

def test_host_toolchain_is_capability_first_not_exact_patch_pinned():
    p=json.loads(POLICY.read_text(encoding='utf-8'))
    assert p['policy']=='CAPABILITY_FIRST'
    assert p['principles']['hostExactPatchPinForbidden'] is True
    assert p['principles']['installedCompatibleToolPreferred'] is True
    for spec in p['tools'].values():
        assert spec.get('exactPatchRequired') is not True

def test_frontend_package_metadata_uses_compatible_ranges_without_exact_package_manager_pin():
    for rel in ('cpf-admin/frontend/package.json','cpf-backoffice-web/frontend/package.json'):
        package=json.loads((ROOT/rel).read_text(encoding='utf-8'))
        assert package['engines']['node']=='>=22.18.0'
        assert package['engines']['npm']=='>=10'
        assert 'packageManager' not in package


def test_java_keeps_release_25_but_does_not_exact_pin_host_jdk():
    p=json.loads(POLICY.read_text(encoding='utf-8'))
    java=p['tools']['java']
    assert java['enforcement']=='CAPABILITY_FIRST_RELEASE_25'
    assert java.get('maxMajor') is None
    assert java.get('hardMinMajor') is None
    assert 'javac --release 25 + execute compiled probe' in java['capabilities']
    convention=(ROOT/'cpf-tools/build/gradle-plugin/src/main/java/com/cpf/gradle/CpfPlatformConventionPlugin.java').read_text(encoding='utf-8')
    assert 'getToolchain().getLanguageVersion()' not in convention
    assert 'getRelease().set(25)' in convention


def test_full_local_java_selection_is_capability_first_not_jdk25_name_or_version_pinned():
    text=(ROOT/'cpf-tools/verification/tools/run-cpf-local-full-validation.ps1').read_text(encoding='utf-8')
    assert 'Test-CpfJavaRelease25Home' in text
    assert 'javacExe --release 25' in text
    assert "-Filter 'jdk-25*'" not in text
    assert 'version "25' not in text
    assert 'CPF_JAVA_HOME' in text
