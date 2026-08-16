# CPF Build / Deployment Topology Guide

## 1. 개발자 로컬 Build

특정 Module만 확인:

```powershell
.\gradlew.bat :cpf-core:test --no-daemon
```

여러 Module:

```powershell
.\gradlew.bat :cpf-core:assemble :cpf-admin:assemble :cpf-gateway:assemble --no-daemon
```

전체 등록 Module과 Publication Gate:

```powershell
.\gradlew.bat aggregateQualityBuild publicationGate --no-daemon
```

Linux/macOS에서는 동일 task를 `./gradlew`로 실행한다. `settings.gradle`에 등록되지 않은 Module은 Gradle이 fail-fast하며, project dependency가 필요한 선행 Module compile을 결정하므로 임의 classpath 조합을 만들지 않는다. 별도 Module-set wrapper script를 유지하지 않고 Gradle task가 단일 실행 계약이다.

## 2. Jenkins

`cpf-tools/release/ci/Jenkinsfile.cpf`는 Source Gate 후 동일 Gradle task 계약으로 Module 선택 Build를 수행한다.

- `FULL_BUILD=true`: `aggregateQualityBuild publicationGate`
- `MODULES=cpf-core,cpf-admin,cpf-gateway`: 선택 Module task 조합
- `GOAL=test|assemble|build`

배포 Pipeline은 Source Build와 Runtime 배포를 분리한다. DB Migration, Secret, Vendor Pack 경로, 승인 단계는 환경별 Jenkins credential/approval 정책에서 주입하며 Repository에 Secret을 저장하지 않는다.

## 3. 실행 Topology

### Modular Monolith

동일 JVM에서는 typed Local Facade를 우선한다. Remote DTO/HTTP serialization을 강제로 거치지 않는다.

### Separated WAS / MSA

동일 업무 Contract의 Remote Adapter가 Service Registry를 통해 endpoint/instance를 선택한다. 내부 호출은 Gateway를 재경유하지 않는다.

### External Client

외부 Channel/Partner는 `cpf-gateway`의 route/channel/security policy를 거친다.

## 4. Standalone BAT

`cpf-batch`는 독립 Boot application이다. Scheduler/Worker/Center-Cut Runtime은 BAT가 소유한다. Generated Domain Center-Cut endpoint는 BAT가 Local 또는 Remote Handler로 호출한다.

Remote 기본 transport는 HTTP이며 `CpfServiceCallEngine`이 target 선택/health/retry/failover/UNKNOWN 정책을 소유한다. 결과불명은 `UNKNOWN_RESULT`로 유지한다.

## 5. Source Gate

```powershell
.\gradlew.bat qualityGate --no-daemon
```

Build까지:

```powershell
.\gradlew.bat aggregateQualityBuild publicationGate --no-daemon
```

DB/Browser/Multi-instance/DR은 이 Source Gate와 별도다. 실행하지 않은 환경 검증은 Evidence가 생길 때까지 `미검증`으로 유지한다.
