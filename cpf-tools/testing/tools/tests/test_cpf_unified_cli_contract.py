from __future__ import annotations
import json, os, re, subprocess, sys, tempfile, zipfile
from pathlib import Path

ROOT=Path(__file__).resolve().parents[4]
CLI_SRC=ROOT/'cpf-tools/runtime/cli/java/CpfCli.java'
CATALOG=ROOT/'cpf-tools/runtime/cli/contracts/cpf-command-catalog.json'

def test_exactly_one_canonical_cli_owner_and_catalog():
    assert CLI_SRC.is_file()
    java=[p for p in ROOT.rglob('CpfCli.java') if '.gradle' not in p.parts and 'build' not in p.parts]
    assert java == [CLI_SRC], java
    data=json.loads(CATALOG.read_text(encoding='utf-8'))
    assert data['owner']=='cpf-tools/runtime/cli'
    assert data['officialInterface']=='cpf'
    assert data['implementation']=='java-jar'
    assert {x['command'] for x in data['publicCommands']} == {'bootstrap','domain-new','domain-sync','build','test','run','stop','reset','status','doctor','help','version'}
    assert {x['namespace'] for x in data['internalNamespaces']} == {'dev','verify','publish','release'}
    assert data['profiles']['PUBLIC']=={'publicCommands':True,'internalNamespaces':False,'sourceWorkspace':False}
    commands={x['command']:x for x in data['publicCommands']}
    assert commands['build']['canonicalGradleTask']=='cpfBuildAll'
    assert commands['test']['canonicalGradleTask']=='cpfTestAll'
    source=CLI_SRC.read_text(encoding='utf-8')
    assert 'gradle(root, "cpfBuildAll", argv)' in source
    assert 'gradle(root, "cpfTestAll", argv)' in source

def test_official_wrappers_are_java_thin_launchers():
    wrappers=[ROOT/'cpf-tools/runtime/cli/cpf',ROOT/'cpf-tools/runtime/cli/cpf.cmd',ROOT/'cpf-tools/runtime/cli/cpf.ps1']
    for p in wrappers:
        text=p.read_text(encoding='utf-8').lower()
        assert 'cpf-cli.jar' in text and 'java' in text
        assert 'cpf.py' not in text
        assert 'cpf-cli-java-version' in text
        for forbidden in ('docker compose','gradlew','domain new','domain sync'):
            assert forbidden not in text

def test_bootstrap_jar_source_identity_is_non_circular_and_managed():
    state=ROOT/'cpf-tools/verification/tools/cpf-source-state.py'
    text=state.read_text(encoding='utf-8')
    assert 'cpf-tools/runtime/cli/lib/cpf-cli.jar' in text
    before=subprocess.check_output([sys.executable,str(state),'--root',str(ROOT),'--scope','source'],text=True,encoding='utf-8')
    source=json.loads(before)['contentSha256']
    jar=ROOT/'cpf-tools/runtime/cli/lib/cpf-cli.jar'
    assert jar.is_file()
    with zipfile.ZipFile(jar) as z:
        props=z.read('cpf-cli.properties').decode('utf-8')
    if f'sourceIdentitySha256={source}' not in props:
        javac=subprocess.run(['javac','-version'],text=True,encoding='utf-8',errors='replace',capture_output=True)
        if not re.search(r'(?:javac\s+)?25(?:[.\s-]|$)', (javac.stdout+javac.stderr).strip()):
            import pytest
            pytest.skip('Java 25 is required to rebuild cpf-cli.jar for the current source identity')
    assert f'sourceIdentitySha256={source}' in props
    assert 'capabilityProfile=INTERNAL' in props

def _cli_command(*args: str) -> list[str]:
    if sys.platform == 'win32':
        return [os.environ.get('COMSPEC', 'cmd.exe'), '/d', '/c', str(ROOT/'cpf-tools/runtime/cli/cpf.cmd'), *args]
    return ['/bin/sh', str(ROOT/'cpf-tools/runtime/cli/cpf'), *args]

def test_internal_cli_cross_platform_version_status_and_java25_fail_closed():
    env={**os.environ,'CPF_WORKSPACE':str(ROOT)}
    java_home=env.get('JAVA_HOME','').strip()
    java_bin=(Path(java_home)/'bin'/('java.exe' if sys.platform=='win32' else 'java')) if java_home else Path('java')
    raw=subprocess.check_output([str(java_bin),'-version'],stderr=subprocess.STDOUT,text=True,encoding='utf-8',errors='replace')
    # JVM launcher는 JAVA_TOOL_OPTIONS/_JAVA_OPTIONS/JDK_JAVA_OPTIONS가 설정되어 있으면
    # "Picked up <VAR>: <값>"을 stderr 첫 줄에 낸다. FullLocal 검증기가 자식 JVM UTF-8 강제를
    # 위해 그 변수를 설정하므로, 첫 줄을 그대로 version 문자열로 보면 판정이 뒤집힌다.
    version_lines=[line for line in raw.splitlines()
                   if not re.match(r'^\s*Picked up (JAVA_TOOL_OPTIONS|_JAVA_OPTIONS|JDK_JAVA_OPTIONS):', line)]
    actual=version_lines[0] if version_lines else ''
    java25=bool(re.search(r'version\s+"25(?:\.|")', actual))
    for command in ('version','status'):
        cp=subprocess.run(_cli_command(command),cwd=ROOT,env=env,text=True,encoding='utf-8',errors='replace',capture_output=True)
        if java25:
            assert cp.returncode==0, cp.stdout+cp.stderr
            assert ('CAPABILITY_PROFILE=INTERNAL' in cp.stdout) if command=='version' else ('CPF_STATUS=' in cp.stdout)
        else:
            assert cp.returncode==69, cp.stdout+cp.stderr
            assert 'CPF-CLI-JAVA-VERSION' in cp.stderr
