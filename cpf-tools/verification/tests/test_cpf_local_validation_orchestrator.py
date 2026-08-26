from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
SCRIPT = ROOT / 'cpf-tools/verification/tools/run-cpf-local-full-validation.ps1'


def text():
    return SCRIPT.read_text(encoding='utf-8')


def test_orchestrator_is_laptop_safe_and_collects_all_failures():
    source = text()
    for token in (
        '$HOME', 'Downloads', "ValidateSet('local','dev','test','stg','prod')",
        'PYTEST_COLLECT_ALL', 'GRADLE_FULL_BUILD_QUALITY', '${name}_NPM_CI',
        '${name}_FRONTEND_VERIFY', 'GRADLE_ASSEMBLE_AFTER_FRONTEND',
        'DB3_RUNTIME_MATRIX', 'LOCAL_ONE_WAS_START', 'LOCAL_ONE_WAS_STOP',
        'MANAGED_STATE_AFTER', 'Compress-Archive', 'CPF_LOCAL_VALIDATION_ZIP',
        '[switch] $FullLocal', 'WINDOWS_PATH_COMPATIBILITY', 'PUBLICATION_STARTER_CLOSURE'
    ):
        assert token in source, token
    assert '-PcpfSkipFrontendBuild=true' in source
    assert '--no-parallel' in source
    assert '$strictExitEffective=[bool]$StrictExit -or [bool]$FullLocal' in source
    assert 'if($strictExitEffective -and ($fail -gt 0 -or $skip -gt 0 -or $notExecuted -gt 0)){exit 1}' in source
    assert 'IncludeDistributedRuntime' not in source
    assert 'git status' not in source
    assert 'git diff' not in source
    assert 'git rev-parse' not in source
    assert 'Get-CpfTreeState' in source
    assert "'managed' (Join-Path $evidenceDir 'managed-state-before.json')" in source


def test_orchestrator_manages_docker_selectively_instead_of_starting_everything():
    source = text()
    for token in (
        '[switch] $SkipDocker', '[switch] $KeepDockerStarted',
        'Test-CpfDockerReady', 'Test-CpfContainerRunning', 'Start-CpfDockerTarget',
        'Stop-CpfDockerTargetIfOwned', 'STATE=ALREADY_RUNNING',
        "foreach($vendor in @('mariadb','postgresql','oracle'))",
        'DB3_RUNTIME_', 'CACHE_PROVIDER_LIVE', 'QA39_RUNTIME_FAULT_SMOKE',
        'MESSAGING_KAFKA_RELIABILITY', 'BATCH_TWO_WORKER_CRASH_UNKNOWN',
        'dockerAllContainersPrestartRequired=$false', 'DOCKER_ALL_PRESTART_REQUIRED=false'
    ):
        assert token in source, token
    assert "'-Action','up','-Target',$Target" in source
    assert "'-Action','stop','-Target',$Target" in source
    assert "-Target','all'" not in source
    assert 'KeepDockerStarted' in source


def test_existing_containers_are_preserved_and_only_owned_containers_are_stopped():
    source = text()
    assert "$alreadyRunning=Test-CpfContainerRunning $container" in source
    assert "if($alreadyRunning)" in source
    assert "$startedByValidation=-not $alreadyRunning" in source
    assert "Wait-CpfDockerFunctionalReadiness" in source
    assert "started=$false" in source
    assert "if($null -eq $State -or -not $State.started -or $KeepDockerStarted){return}" in source


def test_python_bootstrap_does_not_pollute_function_return_pipeline():
    source = text()
    assert "$pipOutput=@(& $venvPython -m pip install" in source
    assert "$pipRc=$LASTEXITCODE" in source
    assert "PYTHON_BOOTSTRAP.log" in source
    assert "return [string]$venvPython" in source
    # External bootstrap command output must be captured before being written to Host/log.
    assert "& $venvPython -m pip install --disable-pip-version-check --no-input -r $requirements\n" not in source


def test_batch_two_worker_stage_is_kafka_free():
    source = text()
    start = source.index("$batchDbEnv=Import-CpfEnvFile $DockerSecretFile")
    end = source.index("# 7. 기본 로컬 Runtime", start)
    batch = source[start:end]
    assert "Start-CpfDockerTarget 'mariadb'" in batch
    assert "Start-CpfDockerTarget 'kafka'" not in batch
    assert "Stop-CpfDockerTargetIfOwned 'kafka'" not in batch
    assert 'batchKafkaState' not in batch
    assert 'BATCH_TWO_WORKER_CRASH_UNKNOWN' in batch
    assert 'GATEWAY_BATCH_RUNTIME' in batch
