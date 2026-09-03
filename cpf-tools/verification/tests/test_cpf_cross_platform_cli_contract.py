from __future__ import annotations
import importlib.util
import json
import pytest
import shutil
import subprocess
import sys
from pathlib import Path
from types import SimpleNamespace

ROOT=Path(__file__).resolve().parents[3]
TOOL=ROOT/'cpf-tools/release/open-git/cpf_open_git.py'
SPEC=importlib.util.spec_from_file_location('cpf_open_git_cli_contract',TOOL)
MODULE=importlib.util.module_from_spec(SPEC); assert SPEC and SPEC.loader; SPEC.loader.exec_module(MODULE)
BUILD_TOOL=ROOT/'cpf-tools/runtime/cli/build-cpf-cli.py'
BUILD_SPEC=importlib.util.spec_from_file_location('cpf_cli_builder_contract',BUILD_TOOL)
BUILD_MODULE=importlib.util.module_from_spec(BUILD_SPEC); assert BUILD_SPEC and BUILD_SPEC.loader; BUILD_SPEC.loader.exec_module(BUILD_MODULE)


def _is_command(cmd:list[object], executable:str)->bool:
    """Match Windows .exe and POSIX executable names through one contract helper."""
    actual=str(cmd[0]).replace('\\','/').rsplit('/',1)[-1].casefold()
    return actual.removesuffix('.exe')==executable.casefold()


def test_java_cli_is_single_implementation_and_wrappers_are_thin(tmp_path:Path, monkeypatch):
    source=ROOT/'cpf-tools/runtime/cli/java/CpfCli.java'
    bootstrap=ROOT/'cpf-tools/runtime/bootstrap/CpfBootstrap.java'
    assert source.is_file() and bootstrap.is_file()
    assert 'cpf-cli.jar' in bootstrap.read_text(encoding='utf-8')
    assert 'CpfGeneratorLauncher.java' not in bootstrap.read_text(encoding='utf-8')
    staging=tmp_path/'workspace'
    for rel in ('bin/cpf','bin/cpf.cmd','bin/cpf.ps1'):
        src=ROOT/'cpf-tools/release/open-git/templates'/rel
        dst=staging/rel; dst.parent.mkdir(parents=True,exist_ok=True); shutil.copy2(src,dst)
    original_run=MODULE.run
    def fake_java25_probe(cmd, cwd, *, capture=False, env=None):
        if len(cmd) >= 2 and _is_command(cmd,"javac") and cmd[1] == "-version":
            return "javac 25.0.3"
        if _is_command(cmd,"javac") and "--release" in cmd:
            adjusted=list(cmd); adjusted[adjusted.index("--release")+1]="21"
            return original_run(adjusted, cwd, capture=capture, env=env)
        return original_run(cmd, cwd, capture=capture, env=env)
    monkeypatch.setattr(MODULE, "run", fake_java25_probe)
    built=MODULE.build_cross_platform_cli(ROOT,staging,'b'*64,'1.0.0-test')
    assert built['status']=='PASS'
    verified=MODULE.verify_cross_platform_cli(staging,'b'*64)
    assert verified['status']=='PASS'


def test_binary_profile_never_projects_cli_java_source_or_sources_jar():
    policy=json.loads((ROOT/'cpf-tools/release/open-git/open-git-surface-policy.json').read_text(encoding='utf-8'))
    targets={row['target'] for row in policy['templateRules']}
    assert 'bin/CpfBootstrap.java' not in targets
    assert 'bin/CpfGeneratorLauncher.java' not in targets
    assert {'bin/cpf','bin/cpf.cmd','bin/cpf.ps1'} <= targets
    artifact=json.loads((ROOT/'cpf-tools/release/open-git/open-git-artifact-policy.json').read_text(encoding='utf-8'))
    assert artifact['profiles']['binary']['sourcesJar']=='DENY'
    assert artifact['profiles']['binary']['javadocJar']=='DENY'


def test_cli_command_surface_contains_required_lifecycle_and_utf8_contract():
    text=(ROOT/'cpf-tools/runtime/cli/java/CpfCli.java').read_text(encoding='utf-8')
    for command in ('bootstrap','domain-new','domain-sync','build','test','run','stop','reset'):
        assert f'case "{command}"' in text
    for token in ('-Dfile.encoding=UTF-8','-Dstdout.encoding=UTF-8','-Dstderr.encoding=UTF-8'):
        assert token in text
    assert 'ProcessBuilder' in text
    assert 'StandardCharsets.UTF_8' in text
    assert 'EXIT_TIMEOUT = 124' in text
    assert 'private static final int TIMEOUT = 124' not in text
    catalog=json.loads((ROOT/'cpf-tools/runtime/cli/contracts/cpf-command-catalog.json').read_text(encoding='utf-8'))
    assert catalog['developerDiscovery']['stableExitCodes']=={
        'OK':0,'FAILURE':1,'USAGE':2,'PREREQUISITE':69,'TIMEOUT':124
    }
    namespaces={row['namespace']:set(row['commands']) for row in catalog['internalNamespaces']}
    assert {'domain','db-render'} <= namespaces['dev']
    assert 'domain' in namespaces['verify']
    assert 'case "domain" -> generator' in text
    assert 'case "db-render" -> generator' in text
    assert 'builder.environment().put("PYTHONUTF8", "1")' in text
    assert 'builder.environment().put("PYTHONIOENCODING", "utf-8")' in text


def test_production_cli_build_rejects_non_java25_before_compile(tmp_path:Path, monkeypatch):
    staging=tmp_path/'workspace'
    calls=[]
    def fake_run(cmd, cwd, *, capture=False, env=None):
        calls.append([str(x) for x in cmd])
        if len(cmd) >= 2 and _is_command(cmd,'javac') and cmd[1] == '-version':
            return 'javac 21.0.11'
        raise AssertionError(f'unexpected command after Java version rejection: {cmd}')
    monkeypatch.setattr(MODULE, 'run', fake_run)
    try:
        MODULE.build_cross_platform_cli(ROOT, staging, 'c'*64, '1.0.0-test')
    except MODULE.OpenGitReleaseError as exc:
        assert 'Java 25 javac is required' in str(exc)
    else:
        raise AssertionError('Java 21 must be rejected by the production CPF CLI build')
    assert len(calls) == 1 and calls[0][1] == '-version'


def test_internal_cli_builder_rejects_non_java25_before_source_or_compile(tmp_path:Path, monkeypatch):
    calls=[]
    monkeypatch.setattr(BUILD_MODULE.shutil,'which',lambda name:f'/fake/{name}')
    def fake_run(cmd,cwd):
        calls.append([str(value) for value in cmd])
        if cmd[1]=='-version':
            return SimpleNamespace(stdout='javac 21.0.11',stderr='',returncode=0)
        raise AssertionError(f'unexpected command after Java version rejection: {cmd}')
    monkeypatch.setattr(BUILD_MODULE,'run',fake_run)
    monkeypatch.setattr(sys,'argv',['build-cpf-cli.py','--root',str(tmp_path)])
    with pytest.raises(SystemExit,match='Java 25 javac required'):
        BUILD_MODULE.main()
    assert calls == [['/fake/javac','-version']]


def test_internal_cli_builder_owns_strict_java25_compile_contract():
    text=BUILD_TOOL.read_text(encoding='utf-8')
    for token in ("'-version'","'--release','25'","'-Xlint:all'","'-Werror'"):
        assert token in text


def test_generated_domain_powershell_consumers_use_unified_java_cli():
    text=(ROOT/'cpf-tools/generator/tools/generated-domain-common.ps1').read_text(encoding='utf-8-sig')
    normalized=text.replace('\\','/')
    assert 'cpf-tools/runtime/cli/lib/cpf-cli.jar' in normalized
    assert 'cpf-tools/runtime/cli/cpf.py' not in normalized
    assert '[Diagnostics.ProcessStartInfo]::new()' in text
    assert 'StandardOutputEncoding' in text and 'StandardErrorEncoding' in text
    assert 'RedirectStandardOutput = $true' in text and 'RedirectStandardError = $true' in text
    assert 'UseShellExecute = $false' in text
    assert "@('dev') + $processArguments" in text
    assert "@('dev', 'db-render')" in text
    assert "Environment['CPF_WORKSPACE']" in text


def test_generated_domain_inventory_resolver_accepts_the_posix_python3_entrypoint():
    text=(ROOT/'cpf-tools/generator/tools/generated-domain-common.ps1').read_text(encoding='utf-8-sig')
    assert '$python = Get-Command python -ErrorAction SilentlyContinue' in text
    assert '$python3 = Get-Command python3 -ErrorAction SilentlyContinue' in text
    assert 'if ($null -ne $python3) { return @($python3.Source) }' in text
    assert text.index('Get-Command python3') < text.index('Get-Command py -ErrorAction')
