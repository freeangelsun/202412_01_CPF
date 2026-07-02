# CPF_M0_M3_3_M4_REPAIR_REQUEST — 최종 목표 기준서 편입 / 표준·확장 헤더 / Center-Cut 소유권 보정 요청

## 0. CPF 최종 목표

CPF(CoreFlow Platform Framework)는 금융권을 포함한 범용 업무 시스템에서 실제 프로젝트에 재사용 가능한 운영형 공통 프레임워크다.

CPF의 최종 목표는 샘플 프로젝트나 문서 모음이 아니라, 실제 업무 시스템을 시작할 때 반복적으로 필요한 개발 표준, 운영 표준, 보안 표준, 연계 표준, 배치 표준, DB 표준, 검증 표준, 문서 표준을 제공하는 것이다.

최종 상태는 아래를 만족해야 한다.

```text
1. 개발자가 신규 업무 기능을 빠르게 만들 수 있는 표준 구조를 제공한다.
2. 운영자가 ADM에서 거래, 오류, 감사, 권한, 배치, 캐시, 정책, 보안 상태를 조회하고 조치할 수 있다.
3. 표준 헤더 수신, 검증, 거래 context 생성, 로그 저장, ADM 조회, outbound 전파가 E2E로 연결된다.
4. PFW, CMN, ADM, BIZADM, MBR, EXS, BAT, EDU/XYZ 모듈 역할이 명확히 분리된다.
5. BAT는 독립 실행 가능한 worker/application으로 job, step, lock, heartbeat, ghost 감지, 재실행, 중지, 운영자 조치, 감사 로그를 지원한다.
6. center-cut은 PFW 표준 계약, BAT 기본 구현체, 업무 adapter 구조로 분리된다.
7. REST 연계와 고정길이 전문 연계를 모두 표준화할 수 있는 기반을 제공한다.
8. DB, Flyway, split SQL, all_install, smoke SQL은 정합성을 가진다.
9. Gradle test, qualityGate, smoke, runtime API, OpenAPI, browser click, 신규 MariaDB 설치 검증 결과가 기록된다.
10. 실행하지 않은 검증, 미구현, 미검증, 실패 항목은 완료로 포장하지 않는다.
```

---

## 0-1. CPF_FINAL_TARGET_REQUIREMENTS.md 상위 기준 적용

이번 작업부터 `CPF_FINAL_TARGET_REQUIREMENTS.md`를 CPF의 최상위 목표 기준서로 사용한다.

`CPF_FINAL_TARGET_REQUIREMENTS.md`는 이번 작업에서 전체를 구현하라는 범위 문서가 아니다.
이 문서는 CPF가 최종적으로 도달해야 할 목표, 설계 원칙, 상태값 기준, 완료 기준, 완료 불인정 기준을 정의한 상위 기준서다.

Codex는 이번 요청서에 명시된 작업 범위를 우선 수행하되, 구현 과정에서 `CPF_FINAL_TARGET_REQUIREMENTS.md`의 목표와 충돌하지 않도록 설계한다.

특히 모든 기능은 향후 아래 항목이 추가될 수 있다는 전제로 확장성/변경 가능성을 고려한다.

```text
신규 업무
신규 채널
신규 기관
신규 배치
신규 전문
신규 정책
신규 테이블
신규 외부 연계
신규 ADM 조회/조치 기능
```

이번 범위 밖의 기능을 무리하게 구현하지는 않는다.
다만 현재 작업 결과가 향후 최종 목표 달성을 어렵게 만드는 구조라면, 요청 범위 안에서 최소 보정하거나 `CPF_STABILIZATION_REPORT.md`에 `부분 구현`, `미검증`, `재확인 필요`로 기록한다.

요청서에 직접 명시되지 않았더라도 이번 작업과 강하게 연결된 소스, 테스트, SQL, smoke, evidence, 리포트, 기능 매트릭스 보강은 선제적으로 수행할 수 있다.

단, 선제 보강은 현재 마일스톤 목적과 직접 관련된 범위로 제한하며, 수행 이유와 검증 결과를 `CPF_STABILIZATION_REPORT.md`에 기록한다.

---

## 1. 이번 작업 목적

이번 작업은 신규 대형 기능 개발이 아니라, 앞으로 CPF 개발이 흔들리지 않도록 최상위 기준과 공통 기반을 정리하는 작업이다.

이번 작업 목적은 아래 네 가지다.

```text
1. CPF_FINAL_TARGET_REQUIREMENTS.md를 repo 최상위 기준서로 편입한다.
2. README와 리포트에서 CPF_FINAL_TARGET_REQUIREMENTS.md를 상위 기준서로 참조하도록 정리한다.
3. PFW 표준 헤더 / 확장 헤더 구조를 source-level로 보강한다.
4. M4에서 구현된 center-cut의 PFW/BAT/업무 모듈 소유권을 재검토하고 보정한다.
```

이번 작업은 아래를 목표로 한다.

```text
1. 앞으로 요청서 작성 시 CPF_FINAL_TARGET_REQUIREMENTS.md를 기준으로 삼을 수 있게 한다.
2. 표준 헤더가 고정 목록만 처리하는 구조가 아니라 X-Cpf-Ext-* 확장 헤더를 처리할 수 있게 한다.
3. center-cut item/result 저장 구조가 PFW에 고정되어 향후 업무 확장을 막지 않게 한다.
4. 문서/리포트/기능 매트릭스의 상태값 과장을 줄인다.
```

---

## 2. 필수 제한

아래는 반드시 지킨다.

```text
Git commit 금지
Git push 금지
branch 생성 금지
민감정보 원문 기록 금지
실행하지 않은 검증을 완료로 기록 금지
별도 변경파일 목록 산출물 생성 금지
문서 포맷 작업만으로 기능 완료 처리 금지
HTML 문서 신규 작성 금지
HTML 문서 직접 수정 지양
현재 마일스톤 목적과 무관한 대규모 리팩터링 금지
```

`CPF_NEW_REQUEST.md`는 사용자 요청 원본으로 보고 기능 검수 대상에서 제외한다.
기존 작업 지시 내용 보존이 필요한 경우 수정하지 않는다.

`CPF_STABILIZATION_REPORT.md`는 내부 검수용 Markdown 리포트로 유지한다.

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

기록할 내용:

```text
작업 시작 branch
작업 시작 HEAD SHA
작업 시작 origin/master SHA
작업 시작 status --short
작업 시작 최근 commit
사용자 요청 원본 파일 여부
이번 작업에서 의도적으로 수정한 파일
기존 수정 상태로 남아 있던 파일
```

---

## 4. CPF_FINAL_TARGET_REQUIREMENTS.md 편입

### 4.1 파일 추가

로컬에 작성된 `CPF_FINAL_TARGET_REQUIREMENTS.md`를 repo 루트에 추가한다.

파일 위치:

```text
./CPF_FINAL_TARGET_REQUIREMENTS.md
```

이 파일은 CPF의 최상위 목표 기준서다.

주의:

```text
1. 이 파일은 전체 목표 기준서이며 이번 작업의 전체 구현 범위가 아니다.
2. 모든 항목을 이번 작업에서 한 번에 구현하라는 의미가 아니다.
3. 실제 구현은 CPF_NEW_REQUEST.md 또는 각 마일스톤 요청서의 범위에 따라 순차 진행한다.
4. 단, 이번 작업 결과가 이 기준서의 설계 원칙과 충돌하지 않아야 한다.
```

### 4.2 README 반영

`README.md`에 `CPF_FINAL_TARGET_REQUIREMENTS.md`를 최상위 목표 기준서로 안내한다.

README에 반영할 내용:

```text
1. CPF_FINAL_TARGET_REQUIREMENTS.md는 CPF의 최종 목표와 설계 원칙을 정의한 최상위 기준서다.
2. 개별 작업 요청서는 이 기준서 아래에서 마일스톤 단위로 작성된다.
3. 중간 문서는 Markdown 원본을 우선한다.
4. 기존 HTML 문서는 최종 정본화 전까지 남아 있을 수 있으나, 신규/수정 문서는 md를 우선한다.
5. 실행하지 않은 검증은 완료로 기록하지 않는다.
```

기존 README에 “HTML 가이드에서 관리”, “HTML 가이드 현행화” 같은 표현이 남아 있으면 최소 범위에서 보정한다.

단, README 전체 재작성은 하지 않는다.

### 4.3 CPF_STABILIZATION_REPORT.md 반영

`CPF_STABILIZATION_REPORT.md`에 이번 작업부터 `CPF_FINAL_TARGET_REQUIREMENTS.md`를 상위 목표 기준으로 삼는다고 기록한다.

기록할 내용:

```text
1. CPF_FINAL_TARGET_REQUIREMENTS.md 신규 편입 여부
2. 이 파일은 최종 목표 기준서이며 단일 작업 범위가 아님
3. 앞으로 요청서/검수/리포트는 이 기준서의 상태값과 완료 불인정 기준을 따른다는 점
4. 이번 작업에서 이 기준서와 충돌하지 않도록 반영한 내용
```

### 4.4 기능 매트릭스 / evidence 영향

기능 매트릭스나 evidence check가 최상위 문서 목록을 검증한다면 `CPF_FINAL_TARGET_REQUIREMENTS.md`를 반영한다.

주의:

```text
1. 기능 매트릭스가 HTML이고 check script와 강하게 연결되어 있으면 무리하게 md로 전환하지 않는다.
2. 기존 HTML을 수정해야 한다면 최소 범위로 수정한다.
3. 중간 문서 원본은 Markdown 우선이라는 원칙을 리포트에 기록한다.
4. 문서 포맷 수정만으로 기능 완료 처리하지 않는다.
```

---

## 5. 표준 헤더 / 확장 헤더 구조 보강

### 5.1 기준

CPF 표준 헤더는 고정된 목록만 처리하는 구조가 아니라, 개발팀이 CPF 표준 naming rule에 맞춰 확장 헤더를 추가하고 사용할 수 있어야 한다.

확장 헤더는 ADM에서 운영자가 등록/수정하는 관리 기능이 아니다.
개발팀이 설계 검토 후 코드/설정/테스트/배포 절차로 추가하는 개발 표준이다.

### 5.2 naming rule

확장 헤더 naming rule:

```text
X-Cpf-Ext-*
```

기본 예약형:

```text
X-Cpf-Ext-1
X-Cpf-Ext-2
X-Cpf-Ext-3
X-Cpf-Ext-4
X-Cpf-Ext-5
```

이름 기반 확장형:

```text
X-Cpf-Ext-{Key}
```

예:

```text
X-Cpf-Ext-Campaign-Id
X-Cpf-Ext-Partner-Code
X-Cpf-Ext-Legacy-Trace
X-Cpf-Ext-Experiment-Group
```

### 5.3 요구사항

아래를 source-level로 반영한다.

```text
1. 사전 등록된 이름만 허용하는 방식으로 제한하지 않는다.
2. naming rule에 맞으면 CPF 확장 헤더로 인식한다.
3. 모든 확장 헤더는 기본 OPTIONAL이다.
4. 확장 헤더를 무조건 마스킹하지 않는다.
5. Authorization, token, API key, 인증값류를 확장 헤더로 우회 저장/전파하는 것은 금지한다.
6. HeaderExtractor가 확장 헤더를 추출할 수 있어야 한다.
7. HeaderValidator가 확장 헤더 naming rule을 검증할 수 있어야 한다.
8. HeaderContext 또는 TransactionContext가 확장 헤더를 보관/조회할 수 있어야 한다.
9. HeaderMutator가 허용된 확장 헤더를 추가/수정/삭제/보정할 수 있어야 한다.
10. HeaderSnapshot이 확장 헤더를 포함할 수 있어야 한다.
11. HeaderPropagator가 outbound 전파 시 확장 헤더를 처리할 수 있어야 한다.
12. CpfWebClient / CpfRestClient outbound 전파 구조와 충돌하지 않아야 한다.
13. ADM은 확장 헤더 등록/수정 관리가 아니라 거래 로그/header snapshot 조회에서 확장 헤더 값을 표시하면 된다.
14. EDU/XYZ 또는 표준 헤더 샘플에 확장 헤더 사용 예제를 추가한다.
```

### 5.4 테스트 요구사항

가능한 범위에서 아래 테스트를 추가한다.

```text
1. X-Cpf-Ext-1 ~ X-Cpf-Ext-5가 확장 헤더로 인식되는지
2. X-Cpf-Ext-Campaign-Id가 확장 헤더로 인식되는지
3. X-Cpf-Ext-Partner-Code가 확장 헤더로 인식되는지
4. X-Random-Test가 CPF 확장 헤더가 아닌지
5. 확장 헤더가 HeaderContext에 저장/조회되는지
6. 확장 헤더가 HeaderSnapshot에 포함되는지
7. 확장 헤더가 outbound 전파 대상에 포함 가능한지
8. 확장 헤더가 기본 REQUIRED로 처리되지 않는지
9. 확장 헤더가 무조건 마스킹되지 않는지
10. Authorization/token/API key류가 확장 헤더 우회로 원문 저장되지 않는지
```

이번 작업은 표준 헤더 Runtime E2E가 아니다.
Runtime E2E는 기능 묶음 안정 후 별도 통합 검증에서 수행한다.

---

## 6. Center-Cut 소유권 보정

### 6.1 기준

center-cut 역할은 아래처럼 분리한다.

```text
PFW:
- center-cut 표준 인터페이스
- 상태 코드
- parent/child transactionGlobalId 기준
- 로그/감사/오류 표준
- 공통 서비스 계약

BAT:
- 기본 center-cut 모수
- item
- result
- 기본 repository/provider/handler
- standalone worker 실행
- heartbeat/job/step/lock 연계

업무 모듈:
- 필요 시 업무별 target/item/result 테이블
- CenterCutTargetProvider / Handler adapter로 연동

ADM:
- center-cut 실행 상태와 결과 조회
- 운영자 조치 조회/요청
```

### 6.2 현재 재확인 대상

M4 작업에서 아래 구조가 PFW 쪽에 들어간 상태라면 재검토한다.

```text
pfw_center_cut_job
pfw_center_cut_parameter
pfw_center_cut_item
pfw_center_cut_result
```

이 구조가 `CPF_FINAL_TARGET_REQUIREMENTS.md`의 모듈 책임, 확장성 원칙, PFW 책임 범위에 맞는지 확인한다.

### 6.3 보정 방향

권장 방향은 아래다.

```text
1. PFW에는 표준 계약과 공통 상태/로그/감사/오류 기준만 둔다.
2. 기본 center-cut 실행 메타와 item/result 저장소는 BAT 소유로 두는 방향을 우선 검토한다.
3. 업무 대상 item/result는 PFW가 직접 소유하지 않는다.
4. 필요하면 bat_center_cut_job / bat_center_cut_parameter / bat_center_cut_item / bat_center_cut_result 형태로 이동 또는 재정의한다.
5. 기존 pfw_center_cut_*를 그대로 유지해야 한다면, 왜 PFW 소유가 맞는지 리포트에 근거를 명확히 남긴다.
6. 단, 업무 item/result 저장소가 PFW에 고정되는 구조는 피한다.
7. 향후 업무 모듈별 center-cut adapter가 추가될 수 있도록 interface/adapter 구조를 고려한다.
```

### 6.4 SQL/Flyway/all_install 보정

DB 변경이 필요하면 아래를 함께 정리한다.

```text
specs/sql/10_pfw_schema.sql
specs/sql/30_bat_schema.sql 또는 BAT 관련 SQL 위치
specs/sql/50_framework_seed_data.sql
specs/sql/00_all_install.sql
specs/sql/00_all_install_and_smoke.sql
specs/sql/migration/flyway/V16__batch_center_cut_standard.sql
specs/sql/99_smoke_check.sql
```

주의:

```text
1. 한쪽 SQL에만 있고 다른 설치 경로에 없는 상태 금지
2. 기존 V16을 무조건 삭제하거나 깨뜨리지 말고 migration 이력과 현재 개발 단계 기준으로 판단
3. 신규 빈 MariaDB full install은 이번 작업에서 실행하지 않으면 미검증 유지
4. SQL 위치 보정 후 check-sql-standard를 실행
```

### 6.5 기능 보강 최소 범위

이번 작업에서 center-cut을 크게 확장하지 않는다.
다만 소유권 보정에 따라 필요한 최소 기능은 보강한다.

후보:

```text
1. BAT DB 기반 CenterCutTargetProvider 초안
2. BAT item/result repository 초안
3. 기존 sample provider가 메모리 기반이면 주석과 리포트에 명확히 기록
4. smoke는 기존 sample 유지 가능
5. 업무 DB adapter와 EDU 샘플은 다음 단계로 넘겨도 됨
```

완료 기준:

```text
center-cut 테이블/모듈 책임이 앞으로 확장 가능한 방향으로 정리되어야 한다.
```

---

## 7. 상태값 보정

기능 매트릭스와 `CPF_STABILIZATION_REPORT.md`의 상태값을 실제 검증 기준으로 보정한다.

특히 아래 항목을 확인한다.

```text
standard-header Source-Level
standard-header Runtime E2E
ADM permission write runtime
V15 apply
smoke-openapi 단독 실행
ADM runtime smoke 내부 OpenAPI
MariaDB full install
browser click
redis-kafka-mq-broker
BAT runtime smoke
center-cut sample runtime smoke
```

중요 기준:

```text
1. :pfw:test 통과는 표준 헤더 Source-Level 완료이지 Runtime E2E 완료가 아니다.
2. 표준 헤더 Runtime E2E를 실행하지 않았다면 미검증으로 기록한다.
3. smoke-openapi.ps1 단독 실행이 서버 미기동으로 실패하면 실패로 기록한다.
4. ADM runtime smoke 내부 OpenAPI 성공과 단독 smoke-openapi 실패를 구분한다.
5. MariaDB full install을 실행하지 않으면 미검증으로 유지한다.
6. browser click을 실행하지 않으면 미검증으로 유지한다.
7. Redis/Kafka/MQ 실 broker를 사용하지 않았으면 미검증으로 유지한다.
```

---

## 8. Markdown 문서 기준 반영

중간 개발/검수 단계에서는 HTML 문서를 신규 작성하거나 직접 수정하지 않는다.
모든 중간 리포트와 신규 가이드는 Markdown(.md)을 원본으로 작성한다.

이번 작업에서 보정할 문구:

```text
HTML 가이드에서 관리
HTML 가이드 현행화
```

새 기준:

```text
중간 문서는 Markdown 원본으로 관리한다.
기존 HTML 문서는 최종 정본화 전까지 남아 있을 수 있으나, 신규/수정 문서는 md를 우선한다.
최종 정본화 단계에서 md 원본 기준으로 PDF/HTML/DOCX를 생성한다.
```

기존 HTML 처리 기준:

```text
1. 기능 매트릭스처럼 check script/evidence와 강하게 연결된 HTML은 함부로 삭제하지 않는다.
2. 수정이 필요한 기존 HTML은 동일 목적의 md 전환을 우선 검토한다.
3. 전환 시 check script/evidence/link가 깨지지 않게 함께 수정한다.
4. 문서 포맷 변환만으로 기능 완료 처리하지 않는다.
```

---

## 9. 테스트 / 검증 요구사항

가능한 범위에서 아래 명령을 실행한다.

```powershell
.\gradlew.bat :pfw:test --offline --no-daemon --console=plain
.\gradlew.bat :bat:test --offline --no-daemon --console=plain
.\gradlew.bat :adm:test --offline --no-daemon --console=plain
.\gradlew.bat :xyz:test --offline --no-daemon --console=plain
.\gradlew.bat test --offline --no-daemon --console=plain
.\gradlew.bat :bat:bootJar --offline --no-daemon --console=plain
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\smoke-bat-runtime.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\smoke-adm-runtime.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-sql-standard.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-feature-evidence.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-utf8.ps1 -CheckMojibake
.\gradlew.bat qualityGate --offline --no-daemon --console=plain
```

주의:

```text
1. 실행하지 못한 검증은 미검증으로 기록한다.
2. 실패한 검증은 실패로 기록한다.
3. 일부 smoke가 환경 문제로 실패하면 실패 사유와 재실행 조건을 기록한다.
4. 신규 MariaDB full install을 실제 실행하지 않았다면 완료로 기록하지 않는다.
5. browser click을 실제 실행하지 않았다면 완료로 기록하지 않는다.
6. 실 broker 검증을 실제 Redis/Kafka/MQ로 수행하지 않았다면 완료로 기록하지 않는다.
```

---

## 10. 리포트 / 매트릭스 반영

`CPF_STABILIZATION_REPORT.md`와 기능 매트릭스에 아래를 반영한다.

```text
1. CPF_FINAL_TARGET_REQUIREMENTS.md 신규 편입 결과
2. README 최상위 기준서 참조 반영 결과
3. Markdown 문서 원본 기준 반영 결과
4. 표준 헤더 확장 구조 구현 내용
5. 확장 헤더 테스트 결과
6. center-cut 소유권 보정 결과
7. PFW/BAT/업무 모듈 역할 분리 판단
8. SQL/Flyway/all_install 반영 여부
9. standard-header Source-Level과 Runtime E2E 상태 분리
10. 실행한 검증 결과
11. 실행하지 않은 검증의 미검증 사유
12. 실패한 검증의 실패 사유
13. CPF_FINAL_TARGET_REQUIREMENTS.md와 충돌 가능성이 있는 항목
```

상태값은 아래 6개만 사용한다.

```text
완료
부분 구현
미구현
미검증
실패
재확인 필요
```

---

## 11. 완료 기준

아래가 충족되어야 이번 작업 완료로 본다.

```text
1. CPF_FINAL_TARGET_REQUIREMENTS.md가 repo 루트에 추가됨
2. 이 파일이 최상위 목표 기준서이며 단일 작업 범위가 아님을 README 또는 리포트에 명시함
3. README에서 CPF_FINAL_TARGET_REQUIREMENTS.md를 참조함
4. 중간 문서 Markdown 원본 기준이 README/리포트에 반영됨
5. 표준 헤더 X-Cpf-Ext-* naming rule이 source-level로 반영됨
6. X-Cpf-Ext-1 ~ X-Cpf-Ext-5 기본 예약형이 반영됨
7. X-Cpf-Ext-{Key} 이름 기반 확장 헤더가 인식 가능함
8. 확장 헤더가 기본 REQUIRED로 처리되지 않음
9. 확장 헤더가 무조건 마스킹되지 않음
10. Authorization/token/API key류를 확장 헤더로 우회 저장/전파하지 않도록 기준 또는 테스트가 반영됨
11. HeaderExtractor/Validator/Context/Mutator/Snapshot/Propagator 중 해당 구조가 확장 헤더를 처리할 수 있도록 보강됨
12. CpfWebClient/CpfRestClient outbound 전파 구조와 충돌 없음
13. EDU/XYZ 또는 표준 헤더 샘플에 확장 헤더 예제가 추가됨
14. center-cut PFW/BAT/업무 모듈 소유권이 실제 소스/SQL 기준으로 정리됨
15. pfw_center_cut_* 유지 또는 이동 여부에 대한 명확한 근거가 리포트에 기록됨
16. 업무 item/result 저장소가 PFW에 고정되는 구조를 피함
17. SQL 변경이 있으면 split SQL/Flyway/all_install/smoke SQL 정합성이 맞음
18. standard-header Source-Level과 Runtime E2E 상태가 분리됨
19. 필요한 테스트/qualityGate/smoke 결과가 기록됨
20. 실행하지 않은 검증은 미검증으로 남김
21. CPF_FINAL_TARGET_REQUIREMENTS.md와 충돌하는 설계가 없음
22. 확장성/변경 가능성을 고려한 interface/adapter/spec 구조가 반영되거나, 아직 미구현이면 부분 구현/재확인 필요로 기록됨
23. Git commit / push / branch 생성 없음
```

---

## 12. 완료 불인정 기준

아래 중 하나라도 해당하면 완료로 인정하지 않는다.

```text
1. CPF_FINAL_TARGET_REQUIREMENTS.md를 추가하지 않음
2. CPF_FINAL_TARGET_REQUIREMENTS.md를 이번 작업 전체 구현 범위로 오해해 무관한 대규모 리팩터링 수행
3. README나 리포트에 최상위 기준서 참조가 없음
4. 표준 헤더 Runtime E2E를 실행하지 않았는데 완료 기록
5. X-Cpf-Ext-* naming rule 없이 5개 상수만 추가
6. 확장 헤더를 사전 등록된 이름만 허용하도록 제한
7. 확장 헤더를 무조건 REQUIRED로 처리
8. 확장 헤더를 무조건 마스킹
9. Authorization/token/API key류가 확장 헤더 우회로 원문 저장/전파될 가능성이 있음
10. 공통 API 없이 업무 코드에서 request.getHeader/header.set 직접 사용
11. center-cut item/result가 PFW에 고정되는 구조를 근거 없이 유지
12. 업무 DB adapter 고려 없이 PFW 테이블만 확장
13. 현재 샘플 job/API에만 맞춘 하드코딩 구조
14. 향후 업무 모듈 확장을 막는 강결합 구조
15. HTML 문서만 수정하고 md 기준을 반영하지 않음
16. 실행하지 않은 검증 완료 기록
17. 실패한 검증을 숨김
18. 민감정보 원문 기록
19. Git commit / push / branch 생성
20. 별도 변경파일 목록 산출물 생성
```

---

## 13. 완료 보고 양식

작업 완료 보고는 아래 양식을 따른다.

```text
1. 기준 정보
- 작업 시작 branch:
- 작업 시작 HEAD SHA:
- 작업 시작 origin/master SHA:
- 작업 종료 HEAD SHA:
- 작업 종료 status --short:
- 사용자 요청 원본 파일:

2. CPF_FINAL_TARGET_REQUIREMENTS.md 반영
- 파일 추가 여부:
- README 참조 반영:
- 리포트 반영:
- 기능 매트릭스/evidence 영향:
- 이 파일이 단일 작업 범위가 아님을 명시했는지:

3. 상위 목표 기준 반영
- CPF_FINAL_TARGET_REQUIREMENTS.md 확인 여부:
- 이번 작업에서 반영한 최종 목표 원칙:
- 확장성/변경 가능성 고려 내용:
- 이번 범위 밖이라 미구현/미검증으로 남긴 항목:
- 최종 목표와 충돌 가능성이 있어 재확인 필요로 기록한 항목:

4. 표준 헤더 확장 구조
- naming rule:
- 기본 예약형:
- key 기반 확장:
- 공통 API:
- outbound 전파:
- EDU/XYZ 예제:
- 테스트:

5. center-cut 소유권 보정
- PFW:
- BAT:
- 업무 adapter:
- SQL/Flyway/all_install:
- 유지/이동 판단 근거:
- 확장성 고려 내용:

6. 문서 기준 보정
- Markdown 원본 기준 반영:
- HTML 직접 수정 여부:
- README/문서 인덱스 보정:
- check script/evidence 영향:

7. 상태값 보정
- standard-header Source-Level:
- standard-header Runtime E2E:
- ADM permission runtime:
- BAT runtime smoke:
- center-cut sample runtime:
- MariaDB full install:
- browser click:
- broker:

8. 실행 명령 결과
- :pfw:test:
- :bat:test:
- :adm:test:
- :xyz:test:
- 전체 test:
- :bat:bootJar:
- smoke-bat-runtime:
- smoke-adm-runtime:
- check-sql-standard:
- check-feature-evidence:
- check-utf8:
- qualityGate:

9. 추가 선제 보강
- 요청서에 없었지만 수행한 작업:
- 수행 이유:
- 검증 결과:

10. 남은 작업
- 표준 헤더 Runtime E2E:
- center-cut 업무 adapter:
- ADM center-cut 관제:
- DB full install:
- ADM browser click:
- EDU/XYZ:
- CMN/EXS/전문 처리:
```

---

## 14. 다음 마일스톤 분기

이번 작업이 닫히면 다음은 아래 중 하나로 간다.

```text
1. M3-4 — PFW 공통 응답 / 오류 / validation / paging 보강
2. M4-2 — 업무 DB 기반 center-cut adapter + XYZ/EDU center-cut 샘플
3. M5 — EDU/XYZ 실전 샘플 완성
4. M6-1 — CMN 고정길이 전문 처리 공통 모듈
```

단, 아래가 정리되지 않으면 다음 단계로 넘어가지 않는다.

```text
1. CPF_FINAL_TARGET_REQUIREMENTS.md가 repo 최상위 기준서로 반영되지 않음
2. standard-header Source-Level과 Runtime E2E 상태가 분리되지 않음
3. center-cut 소유권이 PFW/BAT/업무 모듈 기준으로 정리되지 않음
4. 확장 헤더 구조가 단순 상수 추가 수준에 머무름
```
