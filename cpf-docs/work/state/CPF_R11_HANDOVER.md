# CPF R11 Handover

## 기준

- Source 기준 SHA: `b6db56f5ee745558a59ce511ad681216004b9672`
- 사용자 적용 방식: Overlay ZIP을 Repository Root에 덮어쓴 뒤 cleanup/source gate/통합검증 후 직접 commit/push
- GitHub Connector write test: `403 Resource not accessible by integration`; ChatGPT가 push하지 않음

## 이번 변경에서 보호해야 할 Architecture

1. Generated Domain은 `com.cpf.core.api.*` / `com.cpf.core.spi.*`만 사용한다.
2. `com.cpf.core.common.*`은 Core Runtime 내부 구현이며 Generator/REF/BAT가 직접 소비하지 않는다.
3. Batch/Center-Cut Runtime Owner는 `cpf-batch`다. Domain은 업무 Handler/Target adapter만 제공한다.
4. 결과불명은 `UNKNOWN_RESULT`로 보존하고 성공/실패로 추정하지 않는다.
5. ADM/BZA 메뉴/위험조치는 fail-closed 권한 정책을 유지한다.
6. `cpf-common.utils`는 zero-consumer 확인 없이 삭제하지 않는다.
7. Fixed-Length 로그는 layoutId/version 없이 임의 분해하지 않는다.
8. 전체 Runtime/DB/Browser 검증 전에는 실행 성공 Evidence로 승격하지 않는다.

## 적용 직후 순서

1. `cleanup-r11-obsolete.ps1 -WhatIf`
2. `cleanup-r11-obsolete.ps1`
3. `verify-r11-source-product.ps1`
4. 필요 시 `verify-r11-source-product.ps1 -RunBuild`
5. 통합 DB/Runtime/Browser/다중 인스턴스 검증 계획을 실행
6. `git status`, `git diff`, secret scan 확인 후 사용자가 commit/push

이번 Handover에는 새로운 기능 개발 backlog를 만들지 않는다. 남은 것은 실제 환경이 필요한 통합 **검증**이다. 검증에서 실패가 발견되면 실패 Requirement를 다시 Source Gap으로 승격한다.

## Owner DB Boundary 보정 (R11 후속 Gate 발견)

Public Boundary Gate 통과 후 Common Capability Gate가 ADM의 기존 BAT/MBR/REF direct DataSource 구성을 발견했다. 이후 Architecture를 다음과 같이 보정했다.

1. ADM은 ADM 소유 DB와 CPF 플랫폼 read-model만 직접 접근한다.
2. 업무 Owner DB의 CRUD/transaction은 Owner Module에 남긴다.
3. ADM ↔ Owner는 `CpfOwnerAdminOperationsPort` 같은 topology-independent 계약을 사용한다.
4. 동일 JVM에서는 Owner Port Bean, 분리 WAS에서는 ServiceCall 기반 Remote Adapter를 사용한다.
5. Batch Scheduler/JobRepository/ExecutionTarget Runtime Owner는 `cpf-batch`이며 ADM 중복 Runtime은 삭제한다.
6. 향후 Generated Domain의 운영 기능도 ADM에 DataSource를 추가하지 않고 동일 Owner Port 패턴으로 확장한다.

이 보정의 Full Gradle/Spring/DB/Remote E2E는 아직 수행하지 않았으므로 `미검증`이다. 사용자 Repository에서 `post-apply-r11.ps1` Gate를 다시 통과한 뒤 통합 검증 단계로 승격한다.
