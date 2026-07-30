# CPF QA32 최신 Source 검토 결과

## 1. 기준

- Repository: `freeangelsun/202412_01_CPF`
- 최신 확인 Commit: `3249581e8f01dcb546bc6601c31aee525f564d21` (`20260730_11`)
- 이전 QA31 Source 기준 `9594c8d5d9b1127a4e2694d0ec2f4add9475fc7e`보다 2 Commit 앞섬
- GitHub Combined Status: 패키지 작성 시 조회 결과 없음

## 2. 완료 선언 재개방 근거

최신 Tree의 QA31 결과 문서는 스스로 다음을 기록한다.

- Requirement + Scenario 165건: 완료 0, 부분 구현 73, 미검증 92
- Defect 23건: 완료 0, 부분 구현 20, 재확인 필요 3
- Java 25 전체 Gradle 미실행
- ADM/BZA npm ci·lint·unit·production build 미실행
- 3개 DB lifecycle 미실행
- Gateway/Batch/Multi-instance/Failure Injection/Browser E2E 미실행
- 결과 문서와 Unresolved Register가 이전 SHA 및 `WORKING_TREE_NOT_COMMITTED`를 유지

따라서 `20260730_10`, `20260730_11` Push 이후 모든 완료 상태를 latest SHA에서 재검증해야 한다.

## 3. OSS 전환 미적용 확인

### ADM/BZA Frontend

현재 두 `package.json`의 Runtime Dependency는 Vue만 존재한다. Element Plus, TanStack Table, Vue Router, Pinia, TanStack Query, Zod, Orval Runtime/Generator 구성이 아직 Primary Stack으로 적용되지 않았다.

### Gateway

현재 `cpf-gateway/build.gradle`은 일반 Spring Boot Web MVC이며 `war`, `bootWar`, `providedRuntime Tomcat`을 함께 사용한다. Spring Cloud Gateway Server Web MVC가 실제 Data Plane Primary로 연결된 상태가 아니다.

### Core/Common

현재 `cpf-core` Public API Dependency에 MyBatis Starter, WebFlux, Kafka, AMQP, Springdoc 등이 포함되고 `cpf-common`은 Core, MyBatis 3.0.4, Redis, POI, Jackson 등을 `api`로 전파한다. 기능별 Starter 분리와 MyBatis 4 단일화가 필요하다.

### Artifact/Offline/Provenance

- Settings 단계에서 Plugin Portal과 Maven Central이 비-CPF 의존성에 열려 있다.
- Root Build의 OFFLINE은 CPF Group만 봉인하며 제3자 Dependency는 Maven Central에 열려 있다.
- JAR Manifest의 Git Commit은 Git 실패를 허용한다.
- Artifact Mode가 명시되지 않으면 LOCAL_DEV로 갈 수 있다.

## 4. 최근 Push에서 반드시 재검수할 영역

`20260730_10`은 Gateway, Batch File/Shell, ADM Reference/Log Export, DB Migration, QA Gate를 폭넓게 수정했다. 파일 수와 변경량이 크므로 다음을 독립 재검수한다.

- Gateway HMAC/Nonce/Audit/Path Rewrite/Probe/Streaming/Retry Ledger
- Batch FILE_PROCESS/Shell Signature/Result Ledger/Worker Repository
- ADM Reference Catalog/Approval Boundary/Log Export
- 3 Vendor Migration/Rollback/Checksum/Canonical Schema
- QA31 Static Gate가 실제 Product Runtime을 증명하는지

## 5. 타 AI 감사 검토 결론

타 AI의 240개 사례는 Discovery Evidence로 사용한다. API 이름이나 키워드만으로 취약점을 확정하지 않고 다음 네 등급으로 분류한다.

1. 실제 구현 문맥에서 확정된 결함
2. 공통 Gate로 전수 점검할 위험 패턴
3. 테스트·로컬·의도된 사용이라 조건부 허용
4. 과장 또는 Source와 불일치하여 제외

개별 발견 파일은 수정 범위를 제한하지 않는다. 동일·변형 패턴을 Java/Groovy/Kotlin/Script/Generator/SQL/Config/Artifact 전체에 적용한다.

## 6. 이번 QA32 판정

- Requirement: 62건 전부 `OPEN_REVALIDATION`
- Defect/Gap: 60건 전부 `REOPEN_REVALIDATION` 또는 `OPEN`
- Scenario: 202건 전부 `NOT_EXECUTED`
- OSS Migration: 23건 모두 결과 Matrix와 Legacy Removal Evidence가 필요
- 전체 상태: `DEVELOPMENT_REQUIRED / COMPLETION_NOT_ESTABLISHED`
