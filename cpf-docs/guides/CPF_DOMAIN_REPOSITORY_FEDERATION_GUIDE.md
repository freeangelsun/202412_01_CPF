# CPF Domain Repository Federation Guide

## 원칙

업무 Domain은 플랫폼 Root에 영구 하드코딩하는 방식이 아니라 독립 Git Repository로 운영할 수 있다.
본 작업은 신규 고정 업무 Domain을 생성하지 않는다. EXS 역시 고정 Module로 복원하지 않는다.

독립 Domain은 Source Project가 아니라 게시된 계약에 의존한다.

```gradle
implementation platform('com.cpf:cpf-bom:<version>')
implementation 'com.cpf:cpf-core:<version>'
implementation 'com.cpf:cpf-common:<version>'
implementation 'com.cpf.batch:cpf-batch-contract:<version>' // Batch 사용 시
```

금지:
- `com.cpf.core.common.*` 직접 import
- `project(':cpf-core')`, `project(':cpf-common')`, `project(':cpf-batch...')` Root 결합
- Domain별 플랫폼 내부 구현 복사

## 기존 Golden Domain Export

```powershell
pwsh cpf-tools/generator/export-domain-repository.ps1 `
  -DomainModule cpf-member -SystemCode MBR -PlatformVersion <version>
```

산출물은 기본적으로 `build/domain-repositories/cpf-domain-<name>`에 생성한다.
Gradle Wrapper와 MariaDB Domain Template도 함께 복사해 독립 clean build가 가능하도록 한다.

## 신규 Domain

`create-domain-repository.ps1`는 기존 표준 `create-domain.ps1`를 Stage 용도로 사용하고, 생성 직후 독립 Repository로 Export한 다음 Root 임시 Module을 제거한다.
즉 생성 결과가 CPF Root의 신규 고정 Domain으로 남지 않는다.

## Batch Job Pack

업무 Job/Step/Center-Cut Provider/Handler는 Domain Repository에서 `cpf-batch-contract` SPI를 구현한다.
BAT Worker/Runner는 승인된 Job Pack 계약만 소비한다.

## 검증

`verify-domain-federation.ps1`는 독립 Domain에서 내부 core package 및 Root project dependency를 Fail-closed로 차단한다.
최종 검증에서는 `create -> clean build/test -> optional composite -> remove -> regenerate parity`를 실행한다.
