# CPF 기술 Stack 지원 상태와 Migration 결정

## 1. 현재 상태

현재 Source 기준:

- Java: 25
- Gradle Wrapper: 9.1.0
- Spring Boot: 3.4.13
- Spring Dependency Management Plugin: 1.1.7

현재 조합은 CPF 상용 Release 기준으로 `TRANSITION`이다.
Spring Boot 3.4.13의 공식 System Requirements는 Java 24까지, Gradle 7.6.4+/8.4+를 명시하므로 Java 25 + Gradle 9.1.0 조합을 공식 지원 조합으로 볼 수 없다.

## 1.1 공식 기준 확인 — 2026-07-27

| 구성 | 공식 확인 |
|---|---|
| Spring Boot 3.4.13 | Java 17~24, Gradle 7.6.4+ 또는 8.4+, Servlet 6.0 계열 |
| Gradle 9.1.0 | Java 25 실행/Toolchain 지원 |
| Spring Boot 4.1.0 | Java 17~26, Gradle 8.14+ 및 9.x, Servlet 6.1 계열 |

따라서 Java 25 + Gradle 9.1.0을 유지하려면 Spring Boot 3.4.13을 GA 기준으로 고정할 수 없다.
Boot 3.5.x는 Java 25 자체는 지원하지만 공식 System Requirements가 Gradle 9를 명시하지 않으므로 현재 Java25/Gradle9 목표의 최종 해법으로 확정하지 않는다.

공식 Source는 Spring Boot System Requirements/Gradle Plugin 문서와 Gradle Java Compatibility Matrix를 사용한다.
버전 채택 시점에는 다시 최신 공식 문서를 확인한다.

## 2. 결정

CPF는 Java 25와 Gradle 9 계열 목표를 유지한다.
Spring Boot는 4.x 공식 지원 Line으로 별도 Migration Change Set에서 이관한다.

현재 검증 Candidate는 `4.1.0`으로 기록한다. Candidate 숫자만 보고 즉시 전환하지 않고 다음 compatibility가 모두 닫힌 뒤 실제 Target을 확정한다.

- Spring Framework 7
- Spring Security
- Spring Batch
- MyBatis / MyBatis Spring Boot Starter
- Flyway
- Tomcat/외부 WAS
- Servlet 6.1
- Jakarta API
- Actuator
- Testcontainers
- Byte Buddy
- springdoc/OpenAPI
- CPF Gradle Convention Plugin
- Generated Domain
- bootJar / bootWar / Exploded WAR

## 3. Release 정책

`gradle/cpf-stack.properties`의 `stackState=TRANSITION` 동안:

- 일반 개발/검증은 가능하다.
- 현재 Stack을 "공식 지원 완료"로 문서화하지 않는다.
- `commercialReleaseGate`는 실패해야 한다.
- Build가 우연히 성공한다는 이유만으로 GA 지원 상태로 바꾸지 않는다.

Migration과 Runtime/External WAS Evidence가 완료된 후에만 `SUPPORTED_GA`로 전환한다.

## 4. Version Single Source

다음 값은 `gradle/cpf-stack.properties`를 정본으로 사용한다.

- Java Version
- Gradle Version
- Spring Boot Version
- Spring Dependency Management Plugin Version
- Migration Candidate Boot Version
- Stack State

Root/Module/Generator/Standalone Export에서 별도 Version literal을 만들지 않는다.

## 5. Migration 완료조건

1. Java 25/Gradle 9에서 전체 configuration/compile/test
2. Spring Boot Plugin/BOM 정상 동작
3. BAT Runtime/Spring Batch 회귀
4. ADM/BZA/Gateway/Generated Domain boot
5. bootJar/bootWar
6. 외부 WAS 실제 배포
7. DB/Flyway/MyBatis 회귀
8. Frontend/API/OpenAPI 영향 없음
9. Dependency Lock 갱신
10. CVE/License 확인
11. 기존 주요 Evidence 중 영향권 항목 재검증

실행하지 않은 항목은 `미검증`이다.
