# QA Test and Evidence

## 검수 기준

- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- HEAD: `2903de14eb9cd6cfcccf8e4d2a3489ee2e4193ca`
- Parent: `d2adc89f344fa1f93a2f9291f6576ce69be05239`
- QA 회차: `QA-DEV-R1`
- QA가 Commit/Push/삭제한 항목: 없음

## Requirement별 1차 판정

| Requirement | QA 결과 | 핵심 근거 |
|---|---|---|
| S4-001 | 미통과 | Split Gate exact SHA·Execution continuity·현행 상태 Schema 누락 |
| S4-002 | 미통과 | `head=UNAVAILABLE`, Result Matrix 0행 PASS |
| S4-003 | 미통과 | 전체 Build/Package/Public/SPI/Internal/순환/Consumer Gate 아님 |
| S4-004 | 미통과 | 문자열 Fixture만 검사, 실제 Context·Consumer·DB 실패 미실행 |
| S4-005 | 미통과 | Annotation 0건 PASS, 미부착 Endpoint Header 검증 우회 |
| S4-006 | 미통과 | Targeted Stub Evidence, BZA 호환·Raw Body·전체 Actor 경로 결함 |
| S4-007 | 미통과 | HTTP Client DNS Resolve/Pin 미구현, Audit Runtime 미검증 |
| S4-008 | 미통과 | PostgreSQL/Oracle SQL에 LONGBLOB·MEDIUMTEXT 포함 |
| S4-009 | 미통과 | Catalog PackageBase/Source Drift, stale baseline, Publication 미검증 |

## 실제 Source에서 확인한 중대 결함

### 1. Transaction Annotation 0건 PASS

- Evidence: `executionAnnotationCount=0`
- Interceptor는 거래 Annotation이 없으면 업무 Header 검증을 생략한다.
- Gate는 업무 Controller Coverage를 강제하지 않는다.

### 2. HTTP Client DNS Rebinding 방어 미적용

- Gateway와 Host Agent는 Resolve·Address Validation·Pin을 수행한다.
- HTTP Client Registry는 `validateEndpoint` 후 Hostname URL만 반환한다.
- Gate는 이 차이를 검사하지 않고 `dnsRebinding=true`를 기록한다.

### 3. DB Vendor Fresh Install SQL 문법 결함

- PostgreSQL/Oracle Install SQL에 MariaDB 타입 `LONGBLOB`, `MEDIUMTEXT`가 포함돼 있다.
- Path-only Gate는 이 SQL을 읽지 않고 PASS한다.

### 4. Starter Catalog Ownership Drift

- Catalog Persistence packageBase와 실제 `com.cpf.common.config` Source가 불일치한다.
- Catalog baseline SHA와 Evidence SHA가 최신 HEAD가 아니다.

## 인정 가능한 환경 제약과 실제 결함 분리

### Source 수정이 먼저 필요한 항목

- PostgreSQL/Oracle 잘못된 타입
- Transaction Annotation Coverage 0
- HTTP Client DNS Resolve/Pin
- Catalog/Source Package Ownership
- Gate의 False-positive 구조

### 구현 수정 후 외부 환경 검증이 필요한 항목

- Java 25 Toolchain·Bytecode·Publication·SBOM
- MariaDB/PostgreSQL/Oracle 실제 Install/Migration/Rollback
- ADM/BZA 전체 Application Playwright
- Audit 2 Instance·Process Kill·Recovery
- Registry/Signing/Release

Java 21 Compile/Test/Harness는 개발 검증으로 인정하되 Java 25 전용 항목은 별도 `미검증`으로 유지한다.
