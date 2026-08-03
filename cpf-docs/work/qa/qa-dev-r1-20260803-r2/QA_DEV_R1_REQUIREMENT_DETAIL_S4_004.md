# QA Requirement Detail — CPF-SELF-DEV-S4-004

## 판정

- QA 결과: `미통과`
- 기준 SHA: `2903de14eb9cd6cfcccf8e4d2a3489ee2e4193ca`
- Requirement: ADM/CMN DB-less·Product Persistence 소유권과 안전 기본값
- QA 회차: `QA-DEV-R1`

## 실제 확인 파일

1. `cpf-admin/src/main/java/com/cpf/admin/config/AdmPersistencePolicy.java`
2. `cpf-starters/data/persistence-jdbc/src/main/java/com/cpf/common/config/CmnDataSourceConfig.java`
3. `cpf-starters/data/persistence-jdbc/src/main/java/com/cpf/common/config/CmnSampleDataSourceConfig.java`
4. `cpf-starters/data/persistence-mybatis/src/main/java/com/cpf/common/config/CmnMyBatisConfig.java`
5. `cpf-core/src/main/java/com/cpf/core/api/database/CpfDataSources.java`
6. `cpf-tools/scripts/verify-cpf-db-less-fail-closed.py`
7. `cpf-tools/scripts/tests/test_verify_cpf_db_less_fail_closed.py`
8. `cpf-docs/work/evidence/20260803/session4/P03_DB_LESS_FAIL_CLOSED_R2_TARGETED.json`
9. `cpf-docs/work/development/p00-p05-session4/EXECUTION_LEDGER.csv`

## 소스상 확인된 구현

- ADM 기본 Persistence Mode는 `DATABASE`.
- `MEMORY`는 `edu`, `test` Active Profile에서만 허용된다.
- CMN Product DataSource는 `spring.datasource.cmn`을 사용한다.
- Product MyBatis는 `cmnDataSource`를 필수 주입한다.
- Sample DB는 `edu/test + cpf.cmn.sample-db.enabled=true`에서 별도 `cmn-sample` Binding을 사용한다.

## 미통과 근거

1. Gate는 위 4개 Java Source의 문자열·정규식만 검사한다.
2. Test도 실제 Spring Context가 아니라 축약 문자열 Fixture를 생성해 Gate를 호출한다.
3. `CpfDataSources` 아래 실제 Resolver의 URL/JNDI 분기와 오류 전파를 실행하지 않는다.
4. Product Profile에서 DataSource/JNDI 누락·접속 실패 시 Context가 실패하는지 검증하지 않는다.
5. EDU/Test 이외 Profile에서 Memory Bean 또는 In-memory Repository가 생성되지 않는지 검증하지 않는다.
6. ADM Service/Repository의 실제 Consumer가 Policy를 우회하지 않는지 전수검사하지 않는다.
7. Evidence는 Push 전 SHA와 Targeted Fixture만 기록한다.
8. 실행 원장은 실제 Script 대신 Placeholder 명령을 사용한다.

## 재개발 요청

- Product/EDU/Test/Profile 조합별 Spring Context Test 추가
- URL/JNDI 누락·잘못된 Driver·접속 실패 Negative Test 추가
- ADM 실제 Repository/Service Consumer와 Memory Fallback 전수검사
- Java 21 대체 Harness 또는 Gradle Test에서 Bean Graph·Exception 결과 기록
- 최신 exact SHA에서 재실행

## 성공 기대 결과

- Product Profile DB 구성 누락/장애 시 Application Context 또는 요청이 fail-closed
- EDU/Test 외 Memory Persistence Bean 0개
- Product Consumer의 Memory Fallback 0개
- Source·Consumer·Test·Evidence가 같은 SHA
