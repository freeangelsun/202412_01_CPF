from __future__ import annotations
import importlib.util
import json
import shutil
import subprocess
from pathlib import Path

ROOT=Path(__file__).resolve().parents[3]
TOOL=ROOT/'cpf-tools/release/open-git/cpf_open_git.py'
SPEC=importlib.util.spec_from_file_location('cpf_open_git_cli_contract',TOOL)
MODULE=importlib.util.module_from_spec(SPEC); assert SPEC and SPEC.loader; SPEC.loader.exec_module(MODULE)


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
        if len(cmd) >= 2 and str(cmd[0]).endswith("javac") and cmd[1] == "-version":
            return "javac 25.0.3"
        if str(cmd[0]).endswith("javac") and "--release" in cmd:
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


def test_production_cli_build_rejects_non_java25_before_compile(tmp_path:Path, monkeypatch):
    staging=tmp_path/'workspace'
    calls=[]
    def fake_run(cmd, cwd, *, capture=False, env=None):
        calls.append([str(x) for x in cmd])
        if len(cmd) >= 2 and str(cmd[0]).endswith('javac') and cmd[1] == '-version':
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
