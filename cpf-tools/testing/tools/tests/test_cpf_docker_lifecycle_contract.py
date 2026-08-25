from pathlib import Path
ROOT=Path(__file__).resolve().parents[4]
SCRIPT=ROOT/'cpf-tools/verification/tools/run-cpf-local-full-validation.ps1'

def test_full_validation_owns_docker_start_readiness_and_cleanup():
    text=SCRIPT.read_text(encoding='utf-8')
    required=(
        'Start-CpfDockerTarget','Wait-CpfContainerReady','Wait-CpfDockerFunctionalReadiness',
        'Test-CpfDockerFunctionalReadiness','DOCKER_${Target}_READINESS','Stop-CpfDockerTargetIfOwned',
        "mariadb=@('exec',$Container,'healthcheck.sh','--connect','--innodb_initialized')",
        "postgresql=@('exec',$Container,'pg_isready')",
        "redis=@('exec',$Container,'redis-cli','ping')",
        "kafka=@('exec',$Container,'/opt/kafka/bin/kafka-topics.sh','--bootstrap-server','localhost:9092','--list')",
        "finally{Stop-CpfDockerTargetIfOwned $vendor $dockerState}",
        "finally{Stop-CpfDockerTargetIfOwned 'kafka' $kafkaState}",
    )
    for token in required:
        assert token in text, token

def test_existing_container_is_preserved_but_still_readiness_checked():
    text=SCRIPT.read_text(encoding='utf-8')
    assert '$alreadyRunning=Test-CpfContainerRunning $container' in text
    assert '$startedByValidation=-not $alreadyRunning' in text
    assert 'existing container is preserved' in text
