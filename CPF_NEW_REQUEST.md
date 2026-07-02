# CPF_M4_2_CENTER_CUT_ADAPTER_EDU_REQUEST — 업무 DB 기반 Center-Cut Adapter / EDU·XYZ 샘플 보강 요청

## 0. CPF 최종 목표

이번 작업은 `CPF_FINAL_TARGET_REQUIREMENTS.md`를 최상위 목표 기준서로 따른다.

`CPF_FINAL_TARGET_REQUIREMENTS.md`는 전체 목표 기준서이며, 이번 작업에서 모든 항목을 구현하라는 범위 문서가 아니다.
Codex는 이번 요청서 범위를 우선 수행하되, 구현 과정에서 CPF 최종 목표와 충돌하지 않도록 설계한다.

이번 작업에서 특히 지킬 기준은 아래와 같다.

```text
1. PFW는 center-cut 표준 인터페이스, 상태, 로그, 감사, 오류 기준을 담당한다.
2. BAT는 기본 center-cut 모수/item/result 구현체를 담당한다.
3. 업무 모듈은 업무 DB 대상 테이블을 adapter로 연결한다.
4. 업무 item/result 저장소를 PFW에 고정하지 않는다.
5. 실행하지 않은 검증은 완료로 기록하지 않는다.
6. 문서 정본화보다 기능 구현과 최소 실행 검증을 우선한다.
```

---

## 1. 이번 작업 목적

이번 작업 목적은 center-cut을 샘플 수준에서 한 단계 올려, 실제 업무 DB 기반 adapter와 EDU/XYZ 예제로 확장하는 것이다.

이번 작업에서 닫을 범위는 아래다.

```text
1. 업무 DB 기반 center-cut target/item/result adapter 구조 추가
2. XYZ 또는 EDU 업무 테이블을 대상으로 center-cut 샘플 구현
3. BAT center-cut 기본 구현체와 업무 adapter 연결
4. item별 성공/실패/result 기록 확인
5. parent/child transactionGlobalId 기준 유지
6. 최소 smoke 또는 test 추가
7. 리포트/기능 매트릭스 최소 갱신
8. smoke-standard-header-e2e.ps1 DB password process argument 보안 보정
```

이번 작업은 ADM browser click, Redis/Kafka/MQ 실 broker, 최종 문서 정본화 작업이 아니다.

---

## 2. 필수 제한

아래는 반드시 지킨다.

```text
Git commit 금지
Git push 금지
branch 생성 금지
민감정보 원문 기록 금지
실행하지 않은 검증 완료 기록 금지
별도 변경파일 목록 산출물 생성 금지
문서 포맷 작업만으로 기능 완료 처리 금지
HTML 문서 신규 작성 금지
HTML 문서 직접 수정 지양
현재 마일스톤 목적과 무관한 대규모 리팩터링 금지
```

`CPF_NEW_REQUEST.md`는 사용자 요청 원본으로 보고 기능 검수 대상에서 제외한다.

---

## 3. 작업 시작 전 기준 기록

작업 시작 전에 아래를 확인하고 `CPF_STABILIZATION_REPORT.md`에 기록한다.

```powershell
git branch --show-current
git remote -v
git rev-parse HEAD
git rev-parse origin/master
git status --short
git log -1 --oneline
```

safe.directory 문제로 실패하면 기존 방식처럼 `git -c safe.directory=...`로 보조 확인하고 사유를 기록한다.

---

## 4. Center-Cut 현재 기준 확인

먼저 현재 center-cut 구조를 실제 파일 기준으로 확인한다.

확인 대상 후보:

```text
pfw center-cut 표준 인터페이스
BAT center-cut provider/handler/repository/tasklet/job/step
specs/sql/35_bat_schema.sql
specs/sql/00_all_install.sql
specs/sql/00_all_install_and_smoke.sql
specs/sql/99_smoke_check.sql
specs/sql/migration/flyway/V17__batch_center_cut_ownership_repair.sql
CPF_STABILIZATION_REPORT.md
specs/기능_구현_매트릭스.html
```

확인할 기준:

```text
1. PFW가 업무 item/result 저장소를 직접 소유하지 않는지
2. BAT가 기본 center-cut 실행 메타를 소유하는지
3. 업무 모듈이 adapter로 target/item/result를 연결할 수 있는지
4. 현재 sample이 메모리 기반인지 DB 기반인지
5. 업무 DB 기반 예제가 없는 경우 이번 작업에서 최소 예제를 추가할 위치
```

---

## 5. 업무 DB 기반 Adapter 구조 추가

### 5.1 기본 방향

업무 DB 기반 center-cut은 아래 구조를 따른다.

```text
업무 대상 테이블
→ CenterCutTargetProvider
→ BAT center-cut item 생성
→ CenterCutHandler
→ item 처리
→ ItemStateRepository / ResultRepository
→ BAT result 기록
→ ADM 또는 smoke 조회
```

PFW는 공통 계약만 제공하고, 업무 데이터 저장소를 직접 소유하지 않는다.

### 5.2 구현 후보

가능하면 XYZ/EDU에 center-cut 대상 업무 테이블을 둔다.

후보 이름:

```text
xyz_center_cut_sample_target
xyz_center_cut_sample_result
```

또는 기존 EDU/XYZ fixture 테이블을 재사용할 수 있으면 재사용한다.

단, 억지로 새 테이블을 많이 만들지 말고, 실제 center-cut 흐름을 설명할 수 있는 최소 업무 테이블만 둔다.

### 5.3 Adapter 후보

아래 역할을 명확히 나눈다.

```text
XyzCenterCutTargetProvider
XyzCenterCutHandler
XyzCenterCutResultAdapter 또는 Repository
XyzCenterCutSampleService
XyzCenterCutSampleController 또는 smoke-only runner
```

이름은 기존 패키지 구조에 맞춰 조정한다.

### 5.4 처리 시나리오

최소 시나리오:

```text
1. 업무 대상 데이터 3~5건 seed
2. center-cut job 실행
3. 대상 item 조회
4. item별 handler 처리
5. 성공 item result 기록
6. 실패 item 1건 발생 가능하면 실패 result 기록
7. parent transactionGlobalId와 child transactionGlobalId 연결
8. smoke SQL 또는 runtime smoke로 결과 확인
```

---

## 6. SQL / Flyway / all_install 반영

DB 변경이 있으면 아래를 함께 반영한다.

```text
specs/sql/40_xyz_schema.sql 또는 기존 XYZ SQL 위치
specs/sql/50_framework_seed_data.sql 또는 업무 seed 위치
specs/sql/00_all_install.sql
specs/sql/00_all_install_and_smoke.sql
specs/sql/99_smoke_check.sql
specs/sql/migration/flyway/V18__xyz_center_cut_sample.sql 후보
```

주의:

```text
1. split SQL에만 있고 all_install에 없으면 완료 아님
2. Flyway에만 있고 all_install에 없으면 완료 아님
3. seed만 있고 smoke 확인이 없으면 완료 아님
4. 신규 MariaDB full install을 다시 실행하지 않으면 DB full install은 미검증으로 기록
5. SQL 변경이 있으면 check-sql-standard는 반드시 실행
```

---

## 7. Smoke / Test 추가

가능하면 아래 중 하나 이상을 추가한다.

```text
1. unit/service test
2. mapper DB slice test
3. BAT center-cut adapter test
4. runtime smoke script
5. smoke SQL 확인
```

후보 스크립트:

```text
scripts/smoke-center-cut-adapter.ps1
```

최소 검증 항목:

```text
1. 업무 target seed 존재
2. provider가 target 조회
3. handler가 item 처리
4. result가 DB에 기록
5. 성공/실패 count 확인
6. parent/child transactionGlobalId 연결 확인
7. 민감정보 원문 기록 없음
```

---

## 8. 표준 헤더 E2E smoke 보안 보정

`smoke-standard-header-e2e.ps1`의 MariaDB query 실행부에서 DB password를 process argument로 넘기지 않도록 보정한다.

현재 보정 대상:

```text
--password=$DbPassword
```

요구사항:

```text
1. password는 환경변수 `MYSQL_PWD` 또는 `MARIADB_PWD`로만 주입한다.
2. process argument, result JSON, log, report에 password 원문이 남지 않게 한다.
3. 기존 smoke-standard-header-e2e.ps1 -RequireRuntime 동작은 유지한다.
4. 민감값 마스킹 회귀 테스트 또는 스크립트 self-check를 추가한다.
```

이 보정은 이번 center-cut 기능 범위와 직접 충돌하지 않는 작은 보안 보정으로 처리한다.

---

## 9. 실행 검증 요구사항

가능한 범위에서 아래를 실행한다.

```powershell
.\gradlew.bat :pfw:test --offline --no-daemon --console=plain
.\gradlew.bat :bat:test --offline --no-daemon --console=plain
.\gradlew.bat :xyz:test --offline --no-daemon --console=plain
.\gradlew.bat test --offline --no-daemon --console=plain

powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-sql-standard.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-feature-evidence.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-html-docs.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-utf8.ps1 -CheckMojibake
```

DB 또는 runtime 변경이 있으면 가능하면 아래도 실행한다.

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\smoke-mariadb-full-install.ps1 -RequireRun
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\smoke-center-cut-adapter.ps1
.\gradlew.bat qualityGate --offline --no-daemon --console=plain
```

실행하지 못한 검증은 완료로 기록하지 않는다.

---

## 10. 리포트 / 기능 매트릭스 반영

`CPF_STABILIZATION_REPORT.md`와 `specs/기능_구현_매트릭스.html`에 아래를 최소 반영한다.

```text
1. center-cut 업무 DB adapter 구현 여부
2. XYZ/EDU center-cut 샘플 구현 여부
3. SQL/Flyway/all_install/smoke 반영 여부
4. 실행한 test/smoke 결과
5. 실행하지 않은 검증의 미검증 사유
6. standard-header-e2e password process argument 보정 여부
7. 남은 ADM center-cut 관제/browser click 미검증 여부
```

문서 정본화, HTML 디자인, 가이드 장문 보강은 이번 범위가 아니다.

---

## 11. 완료 기준

아래가 충족되어야 이번 작업 완료로 본다.

```text
1. 업무 DB 기반 center-cut adapter 구조가 실제 소스로 추가됨
2. XYZ 또는 EDU 샘플에서 center-cut 대상 업무 데이터를 조회할 수 있음
3. BAT center-cut 기본 구현체와 업무 adapter가 연결됨
4. item별 처리 결과가 DB에 기록됨
5. 성공/실패 count 또는 result 조회 가능
6. parent/child transactionGlobalId 기준이 유지됨
7. 필요한 SQL이 split/Flyway/all_install/smoke 경로에 반영됨
8. 관련 test 또는 smoke가 실행됨
9. 실행하지 않은 검증은 미검증으로 기록됨
10. `smoke-standard-header-e2e.ps1`의 DB password process argument 노출 가능성이 제거됨
11. 리포트와 기능 매트릭스 상태값이 실제 파일과 일치함
12. Git commit / push / branch 생성 없음
```

---

## 12. 완료 불인정 기준

아래 중 하나라도 해당하면 완료로 인정하지 않는다.

```text
1. PFW가 업무 item/result 저장소를 직접 소유하는 구조로 회귀
2. 메모리 sample만 있고 업무 DB 기반 adapter가 없음
3. SQL이 한 경로에만 반영되고 all_install/Flyway/smoke와 불일치
4. result DB 기록 없이 로그만 남김
5. parent/child transactionGlobalId 기준 없음
6. test/smoke 미실행인데 완료 기록
7. 민감정보 원문 기록
8. DB password를 process argument, log, result JSON, report에 원문 노출
9. 리포트와 기능 매트릭스 상태 불일치
10. Git commit / push / branch 생성
11. 별도 변경파일 목록 산출물 생성
```

---

## 13. 완료 보고 양식

```text
1. 기준 정보
- 작업 시작 branch:
- 작업 시작 HEAD SHA:
- 작업 시작 origin/master SHA:
- 작업 종료 HEAD SHA:
- 작업 종료 status --short:
- 사용자 요청 원본 파일:

2. center-cut adapter
- 업무 모듈:
- target table:
- provider:
- handler:
- result repository:
- parent/child transactionGlobalId:
- 성공/실패 처리:

3. SQL 반영
- split SQL:
- Flyway:
- all_install:
- all_install_and_smoke:
- 99_smoke_check:
- seed:

4. 실행 검증
- :pfw:test:
- :bat:test:
- :xyz:test:
- 전체 test:
- smoke-center-cut-adapter:
- smoke-mariadb-full-install:
- check-sql-standard:
- check-feature-evidence:
- check-html-docs:
- check-utf8:
- qualityGate:

5. 표준 헤더 E2E 보안 보정
- password process argument 제거 여부:
- 환경변수 주입 방식:
- result/log/report 민감정보 원문 미기록 확인:

6. 상태값 반영
- center-cut adapter:
- EDU/XYZ center-cut sample:
- ADM center-cut 관제:
- MariaDB full install:
- standard-header-e2e:
- ADM browser click:

7. 남은 작업
- ADM center-cut 관제:
- ADM runtime/OpenAPI/browser click:
- Redis/Kafka/MQ:
- EDU/XYZ 실전 샘플:
- CMN 고정길이 전문:
```

---

## 14. 다음 마일스톤 분기

이번 작업이 완료되면 다음은 아래 중 하나로 간다.

```text
1. ADM center-cut 관제 + ADM runtime/OpenAPI smoke
2. EDU/XYZ 실전 샘플 CRUD/paging/validation/facade/outbound 완성
3. CMN 고정길이 전문 처리
4. EXS 전문 송수신
```

분기 기준:

```text
1. center-cut adapter가 실패하면 center-cut 보정 먼저 진행
2. center-cut adapter가 완료되면 ADM 관제 또는 EDU/XYZ 실전 샘플로 진행
3. ADM runtime 환경이 계속 어려우면 OpenAPI/API smoke까지만 닫고 browser click은 미검증 유지
```
