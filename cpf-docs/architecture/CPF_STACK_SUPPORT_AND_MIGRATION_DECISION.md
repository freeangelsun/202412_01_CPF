# CPF 기술 Stack 지원 상태와 Migration 결정

> Canonical path: `cpf-docs/architecture/CPF_STACK_SUPPORT_AND_MIGRATION_DECISION.md`  
> Source of truth: `gradle/cpf-stack.properties` + Gradle Wrapper + BOM/Lock  
> Reviewed baseline: `c1f273f1ea4fafac6fd5d23bd837adfc38a04497`  
> Synchronized: `2026-07-31`

## 1. 현재 Source Target

현재 `gradle/cpf-stack.properties` 기준:

| 구성 | Target |
|---|---|
| Java | 25 |
| Gradle | 9.1.0 |
| Spring Boot | 4.1.0 |
| Spring Cloud | 2025.1.2 |
| Spring Batch | 6.0.4 |
| Servlet | 6.1 |
| Stack State | `TARGET` |

과거 Spring Boot 3.4.13 `TRANSITION` 설명은 현재 Source 상태가 아니다.

## 2. `TARGET`과 `SUPPORTED_GA` 구분

`stackState=TARGET`은 Source와 Build 설정이 해당 Stack을 목표로 이관됐다는 뜻이다.

다음을 의미하지 않는다.

- 전체 Repository Build 성공
- 모든 Module Runtime 성공
- 외부 WAS 지원 완료
- 3 DB Vendor 완료
- Kafka·Browser·Multi-instance 완료
- 상용 Release 지원 완료

`SUPPORTED_GA`는 별도 Release Gate와 Evidence를 모두 통과한 뒤에만 선언한다.

## 3. Version Single Source

다음 값은 `gradle/cpf-stack.properties` 또는 명시된 정본만 사용한다.

- Java Version
- Gradle Version
- Spring Boot/Cloud/Batch
- dependency-management plugin
- Servlet
- 주요 Tool/OSS version
- stackState

금지:

- Root/Module/Generator/Script의 별도 version literal
- README/Guide/ADR가 properties와 다른 현재 버전을 주장
- `latest`, dynamic range, SNAPSHOT
- lockfile 미반영
- Generated Domain의 별도 BOM/version

## 4. Build 지원 완료 조건

1. fresh clone
2. clean Gradle cache
3. Wrapper checksum
4. Java 25 toolchain
5. settings/includeBuild/project graph
6. Plugin/BOM resolution
7. 전체 compile/test
8. Published POM/source/javadoc
9. bootJar/bootWar
10. Generated Domain standalone build
11. LOCAL_DEV/REMOTE/OFFLINE
12. dependency lock convergence
13. reproducible artifact
14. final artifact dependency verification
15. no local fallback in REMOTE/OFFLINE

## 5. Framework compatibility

실제 Source와 Runtime에서 검증한다.

- Spring Framework 7
- Spring Security
- Spring Session
- Spring Batch 6
- Spring Cloud Gateway MVC
- MyBatis/Spring integration
- Flyway OSS Core
- Micrometer/OTel
- Tomcat/External WAS
- Jakarta/Servlet 6.1
- Actuator
- Testcontainers
- Byte Buddy/Mock/Test tooling
- springdoc/OpenAPI
- CPF Convention Plugin/BOM
- Generated Domain
- Java Agent/Worker/Process

Compile 성공만으로 compatibility 완료를 선언하지 않는다.

## 6. Artifact별 Runtime 검증

- `cpf-core`/`cpf-common` public artifact consumer
- ADM Backend
- BZA Backend
- Gateway BootJar
- Batch Control/Worker/Agent/Runner
- Generated Domain BootJar
- External WAS BootWar/War
- ADM/BZA static artifact
- Offline bundle

각 Artifact는 startup, health, representative request, failure, shutdown과 package content를 검증한다.

## 7. DB·Broker·Browser 검증

### DB

MariaDB, PostgreSQL, Oracle 각각:

- empty install
- migration
- upgrade
- rollback/forward recovery
- reapply
- schema drift
- runtime query
- Spring Batch repository
- Spring Session
- backup/restore 영향

### Kafka

- publish/consume
- duplicate
- ordering
- rebalance
- retry/DLT
- broker outage
- manager/worker crash
- response loss

### Browser

ADM/BZA:

- clean npm ci
- generated client
- typecheck/unit/build
- Chromium/Firefox/WebKit
- session/CSRF/permission
- deep link
- accessibility
- error/recovery

## 8. Stack State 전이

```text
PROPOSED
→ TARGET
→ VERIFIED_CANDIDATE
→ SUPPORTED_GA
→ DEPRECATED
→ UNSUPPORTED
```

- `TARGET`: Source 목표
- `VERIFIED_CANDIDATE`: Build·핵심 Runtime은 통과했으나 전체 GA Matrix 미완료
- `SUPPORTED_GA`: Final Target Release Gate 전체 통과
- `DEPRECATED`: 지원 종료 일정·Migration 제공
- `UNSUPPORTED`: Build/Runtime/보안 지원 없음

현재 상태는 Source 정본의 `TARGET`이다.

## 9. Release 차단

다음 중 하나라도 존재하면 `SUPPORTED_GA` 금지:

- full Gradle 미실행/실패
- unsupported plugin/dependency
- lock/POM/BOM drift
- 외부 WAS 미검증인데 지원 표기
- 3DB/Kafka/Browser 미검증
- Generated Domain 미검증
- final Artifact SBOM/License/CVE 미검증
- exact-SHA Evidence 없음
- Stack 문서와 properties 불일치

## 10. 관련 Requirement

- `REL-BUILD`
- `REL-DEPLOY`
- `REL-COMPAT`
- `RULE-ARCH`
- `RULE-QUALITY`
- `TEST-RUNTIME`
- `TEST-BROKER`
- `TEST-BROWSER`
- `TEST-EVIDENCE`
- `DEVEX-CODEGEN`
