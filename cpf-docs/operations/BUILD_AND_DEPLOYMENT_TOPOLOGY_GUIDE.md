# CPF Build / Deployment Topology Guide

## 1. 개발자 로컬 Build

특정 Domain만 확인:

```powershell
pwsh .\cpf-tools\scripts\build-module-set.ps1 -Modules cpf-member -Goal test -NoDaemon
```

여러 Module:

```powershell
pwsh .\cpf-tools\scripts\build-module-set.ps1 -Modules cpf-core,cpf-common,cpf-member,cpf-account -Goal assemble -NoDaemon
```

전체 등록 Module:

```powershell
pwsh .\cpf-tools\scripts\build-module-set.ps1 -Full -Goal build -NoDaemon
```

Script는 `settings.gradle`에 등록되지 않은 Module을 거부한다. Gradle project dependency가 필요한 선행 Module compile을 결정하므로 임의 classpath 조합을 만들지 않는다.

## 2. Jenkins

`deploy/ci/Jenkinsfile.cpf`는 Source Gate 후 Module Set Build를 수행한다.

- `FULL_BUILD=true`: settings.gradle 등록 전체
- `MODULES=cpf-core,cpf-common,cpf-member`: 선택 묶음
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
pwsh .\cpf-tools\scripts\verify-r11-source-product.ps1
```

Build까지:

```powershell
pwsh .\cpf-tools\scripts\verify-r11-source-product.ps1 -RunBuild
```

DB/Browser/Multi-instance/DR은 이 Source Gate와 별도다. 실행하지 않은 환경 검증은 Evidence가 생길 때까지 `미검증`으로 유지한다.
