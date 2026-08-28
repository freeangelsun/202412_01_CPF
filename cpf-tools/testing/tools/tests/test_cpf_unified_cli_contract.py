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
    assert {x['command'] for x in data['publicCommands']} == {'bootstrap','domain-new','domain-sync','build','test','run','stop','reset','status'}
    assert {x['namespace'] for x in data['internalNamespaces']} == {'dev','verify','publish','release'}
    assert data['profiles']['PUBLIC']=={'publicCommands':True,'internalNamespaces':False,'sourceWorkspace':False}

def test_official_wrappers_are_java_thin_launchers():
    wrappers=[ROOT/'cpf-tools/runtime/cli/cpf',ROOT/'cpf-tools/runtime/cli/cpf.cmd',ROOT/'cpf-tools/runtime/cli/cpf.ps1']
    for p in wrappers:
        text=p.read_text(encoding='utf-8').lower()
        assert 'cpf-cli.jar' in text and 'java' in text
        assert 'cpf.py' not in text
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
    assert f'sourceIdentitySha256={source}' in props
    assert 'capabilityProfile=INTERNAL' in props

def test_internal_cli_linux_version_status_and_java25_fail_closed():
    cli=ROOT/'cpf-tools/runtime/cli/cpf'
    env={**os.environ,'CPF_WORKSPACE':str(ROOT)}
    version=subprocess.run([str(cli),'version'],cwd=ROOT,env=env,text=True,encoding='utf-8',capture_output=True)
    assert version.returncode==0, version.stdout+version.stderr
    assert 'CAPABILITY_PROFILE=INTERNAL' in version.stdout
    status=subprocess.run([str(cli),'status'],cwd=ROOT,env=env,text=True,encoding='utf-8',capture_output=True)
    assert status.returncode==0 and 'CPF_STATUS=' in status.stdout
    if sys.platform != 'win32':
        actual=subprocess.check_output(['java','-version'],stderr=subprocess.STDOUT,text=True).splitlines()[0]
        if '25' not in actual:
            build=subprocess.run([str(cli),'build'],cwd=ROOT,env=env,text=True,encoding='utf-8',capture_output=True)
            assert build.returncode==69
            assert 'CPF-CLI-JAVA-VERSION' in build.stderr
