# CPF_REQUEST_002: MariaDB 전체 설치 SQL 재실행 / Seed Idempotent / DB 권한 검증 요청

## 0. 상위 아키텍처 전제

이번 요청의 직접 구현 범위는 DB 검증이다.
다만 아래 CPF 아키텍처 전제는 이번 작업 중에도 훼손하지 않는다.

* BAT는 향후 독립 실행 가능한 Spring Boot batch worker 모듈로 분리한다.
* BAT는 단순 library가 아니라 executable jar 형태로 standalone 구동 가능해야 한다.
* 필요 시 BAT는 WAS/컨테이너 환경에도 배포 가능해야 한다.
* PFW는 배치 공통 API, 표준 메타, 상태 코드, 오류 코드, 로그/감사 기준을 제공한다.
* ADM은 배치 실행 로직을 소유하지 않고 실행 상태, 이력, 로그, ghost, lock, worker 상태를 관제한다.
* 이번 작업 중 PFW/ADM에 BAT 실행 책임을 추가하거나 고착화하지 않는다.
* 센터컷 대량 모수/item/result 테이블은 PFW에 고정으로 강제하지 않는다.
* BAT는 향후 기본 센터컷 모수/item/result 테이블과 기본 구현체를 제공한다.
* 개발자는 필요 시 업무 주제영역별 커스텀 모수/item/result 테이블을 만들고 adapter/handler로 BAT와 연동할 수 있어야 한다.
* 업무 모듈은 CenterCutTargetProvider, CenterCutHandler, CenterCutResultProvider 등 표준 adapter로 BAT와 연동할 수 있어야 한다.
* 이번 작업에서 BAT/센터컷을 구현하지 않더라도, 향후 분리를 방해하는 SQL/문서/구조 변경은 하지 않는다.
* 관련 영향이 발견되면 CPF_STABILIZATION_REPORT.html에 “BAT 분리 영향 / 센터컷 확장성 영향”으로 기록한다.

## 1. 이번 작업 범위 고정

이번 작업은 아래 범위만 수행한다.

* MariaDB 전체 설치 SQL 재실행
* specs/sql/00_all_install_and_smoke.sql 실제 실행 검증
* seed 재실행 idempotent 검증
* Flyway migration과 all_install SQL 정합성 검증
* app 계정 DDL 차단 검증
* migration/root 계정 DDL 허용 검증
* FK/index/comment/공통 감사 컬럼 smoke 확인
* README / SQL 가이드 / 기능 구현 매트릭스 / CPF_STABILIZATION_REPORT.html 결과 기록

이번 작업에서 아래 항목은 구현하지 않는다.
필요하면 CPF_STABILIZATION_REPORT.html의 다음 보강 후보로만 기록한다.

* ADM 브라우저 실제 클릭 자동화
* Batch listener heartbeat / 처리율 갱신
* ghost 자동 감지 scheduler / 알림
* BAT 독립 실행 모듈 신설
* 온디맨드 배치
* 센터컷 기본 구현체 / 업무별 커스텀 모수 테이블 연동
* 온라인/배치 로그 정책 + 거래 메타 자동 등록
* BAT EDU / XYZ EDU 대규모 보강
* 배치 개발 가이드 / 트랜잭션 가이드 대규모 정본화

요청 파일은 작업 대상으로 수정하지 않는다.
Git commit, push, branch 생성 지시는 하지 않는다.
별도 수정파일 목록 산출물은 만들지 않는다.
작업 결과는 CPF_STABILIZATION_REPORT.html에만 기록한다.

## 2. 작업 전 주의사항

작업 시작 전 CPF_NEW_REQUEST.md 파일 인코딩을 확인한다.
요청 파일은 UTF-8로 저장되어 있어야 한다.

Windows 터미널에서만 한글이 깨져 보이는 경우에는 파일 자체 문제인지, 터미널 코드페이지 문제인지 구분한다.
터미널 표시 문제만으로 요청 파일 내용을 임의 수정하지 않는다.

이번 작업은 MariaDB 전체 설치 SQL을 실제 실행하는 검증 작업이다.
specs/sql/00_all_install_and_smoke.sql 실행 과정에서 로컬 MariaDB의 CPF 관련 DB/schema/table/seed 데이터가 초기화 또는 재생성될 수 있음을 전제로 한다.

DB 계정명, 권한, 비밀번호는 문서 기억으로 판단하지 말고 실제 SQL과 설정 파일 기준으로 확인한다.
단, 비밀번호와 민감정보는 CPF_STABILIZATION_REPORT.html에 평문으로 기록하지 않는다.

요청서에 기재된 테이블명이 실제 SQL과 다를 경우, 임의로 성공 처리하지 말고 “요청명 / 실제명 불일치”로 리포트에 기록한다.

## 3. 작업 목표

현재 ADM runtime smoke는 기존 설치 DB를 사용했다.
이번 작업에서는 MariaDB 전체 설치 SQL을 실제로 다시 실행해 CPF 전체 SQL 기준이 깨지지 않았는지 검증한다.

완료는 SQL 파일 존재가 아니라 실제 실행 결과로 판단한다.

완료 목표:

* 00_all_install_and_smoke.sql 실제 실행 성공
* seed SQL 재실행 시 중복 오류 없이 성공
* Flyway migration 기준과 all_install 기준 불일치 없음
* app 계정은 DDL 차단
* migration/root 계정은 설치 DDL 가능
* 주요 FK/index/comment/공통 감사 컬럼 확인
* 실패/미검증 항목은 성공으로 기록하지 않음

## 4. 먼저 현재 상태 판정

작업 시작 전 실제 파일 기준으로 아래를 먼저 확인하고 리포트에 기록한다.

* specs/sql/00_all_install_and_smoke.sql 존재 여부
* split SQL 파일 목록
* Flyway migration 파일 목록
* seed SQL 파일 목록
* app/migration/root 계정 기준
* 직전 CPF_STABILIZATION_REPORT.html의 DB 미검증 항목
* README와 SQL 가이드의 검증 명령 일치 여부
* BAT 분리 또는 센터컷 확장성에 영향을 줄 수 있는 SQL 변경 여부

상태값은 아래만 사용한다.

* 완료
* 부분 구현
* 미구현
* 미검증
* 실패
* 재확인 필요

## 5. SQL 실행 기준

아래 SQL을 실제 MariaDB에 실행한다.

```powershell
mysql -u root -p < specs/sql/00_all_install_and_smoke.sql
```

실행 후 아래를 확인한다.

* pfwDB 생성
* admDB 생성
* mbrDB 생성
* cmnDB 생성
* accDB 생성
* bizadmDB 생성
* exsDB 생성
* Spring Batch BATCH_* 테이블 생성
* pfw_batch_* 운영 메타 테이블 생성
* adm_* 운영 테이블 생성
* mbr_* 회원 테이블 생성
* bizadm_* 기본 구현 테이블 생성
* exs_* 대외연계 기본 구현 테이블 생성

실행 실패 시 실패 SQL 위치, 오류 메시지, 원인을 리포트에 기록한다.
실패했는데 성공으로 기록하지 않는다.

## 6. Seed Idempotent 검증

seed SQL은 반복 실행해도 중복 오류가 나지 않아야 한다.

필수 검증:

* 00_all_install_and_smoke.sql 1회 실행
* seed 관련 SQL 또는 all_install 2회 실행
* 중복 key 오류 없음
* 기준 코드/메뉴/권한/운영자 seed가 중복 증가하지 않음
* seed 재실행 후 핵심 row count가 예상 범위 유지

확인 대상 예시:

* pfw_code
* pfw_message
* pfw_response_code
* adm_operator
* adm_role
* adm_menu
* adm_button
* adm_role_menu
* adm_role_button
* pfw_batch_job
* pfw_batch_schedule
* pfw_business_day_calendar

테이블명이 실제 SQL과 다르면 실제명을 기준으로 확인하고, 요청명과 실제명을 리포트에 함께 기록한다.

## 7. Flyway 기준 검증

Flyway migration과 all_install 기준이 서로 충돌하지 않아야 한다.

필수 검증:

* Flyway migration 파일 목록 확인
* V9__batch_worker_ghost_operations.sql 포함 여부 확인
* all_install SQL에 V9 변경분 반영 여부 확인
* pfw_batch_worker 존재 확인
* pfw_batch_ghost_event 존재 확인
* pfw_batch_execution.server_instance_id 존재 확인
* pfw_batch_execution.worker_id 존재 확인
* pfw_batch_execution.transaction_global_id 존재 확인
* pfw_batch_step_execution.spring_batch_step_execution_id 존재 확인
* pfw_batch_step_execution.worker_id 존재 확인

가능하면 아래 방식 중 하나로 검증한다.

```powershell
.\gradlew.bat flywayInfo --offline
.\gradlew.bat flywayMigrate --offline
```

프로젝트에 Flyway Gradle task가 없으면 실행하지 말고, 실제 가능한 대체 검증 방식을 리포트에 기록한다.
Flyway task가 없는데 성공으로 기록하지 않는다.

## 8. DB 권한 검증

CPF는 app 계정과 migration/root 계정의 권한을 분리해야 한다.

필수 검증:

app 계정:

* SELECT 가능
* 설계 기준에 따른 INSERT/UPDATE/DELETE 가능 여부 확인
* CREATE TABLE 차단
* ALTER TABLE 차단
* DROP TABLE 차단

migration/root 계정:

* CREATE TABLE 가능
* ALTER TABLE 가능
* DROP TABLE 가능

검증 예시:

```sql
-- app 계정으로 실패해야 하는 예
CREATE TABLE pfwDB.app_ddl_block_test (
    id BIGINT PRIMARY KEY
);

-- migration/root 계정으로 성공해야 하는 예
CREATE TABLE pfwDB.migration_ddl_allow_test (
    id BIGINT PRIMARY KEY
);

DROP TABLE pfwDB.migration_ddl_allow_test;
```

app 계정에서 DDL이 허용되면 실패로 기록한다.
비밀번호는 리포트에 평문으로 기록하지 않는다.

## 9. FK / Index / Comment / 감사 컬럼 검증

핵심 테이블에 대해 FK, index, comment, 공통 감사 컬럼을 확인한다.

필수 확인 대상:

PFW Batch:

* pfw_batch_job
* pfw_batch_schedule
* pfw_batch_execution
* pfw_batch_step_execution
* pfw_batch_lock
* pfw_batch_worker
* pfw_batch_ghost_event

ADM:

* adm_operator
* adm_role
* adm_menu
* adm_role_menu
* adm_audit_log

MBR:

* mbr_member
* mbr_refresh_token
* mbr_member_login_history 또는 실제 login history 테이블명

BIZADM:

* bizadm_admin_user
* bizadm_refresh_token
* bizadm_login_history 또는 실제 login history 테이블명

EXS:

* exs_institution
* exs_endpoint
* exs_message_log 또는 실제 message/log 테이블명

공통 감사 컬럼은 기존 CPF SQL 표준을 따른다.
프로젝트 표준 컬럼명이 다르면 기존 표준명을 따른다.

누락 시 완료로 기록하지 않고 다음 보완 항목으로 남긴다.

## 10. 문서 정합성 확인

이번 작업에서 SQL 구조를 변경하지 않는 것이 원칙이다.
다만 검증 명령, 실제 결과, 실패/미검증 사유는 문서와 리포트에 반영한다.

필수 확인/갱신:

* README.md의 SQL 검증 명령
* specs/SQL_가이드.html의 all_install/Flyway/권한 검증 기준
* specs/기능_구현_매트릭스.html의 SQL 검증 상태
* CPF_STABILIZATION_REPORT.html의 실제 실행 결과

SQL을 변경하지 않았으면 변경하지 않았다고 리포트에 기록한다.
SQL을 변경했다면 변경 사유와 대상 파일을 리포트에 기록한다.

## 11. 필수 검증 명령

가능한 범위에서 아래를 실행한다.

```powershell
mysql -u root -p < specs/sql/00_all_install_and_smoke.sql

powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-sql-standard.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-html-docs.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-utf8.ps1 -CheckMojibake

.\gradlew.bat test --offline
.\gradlew.bat qualityGate --offline
```

가능하면 ADM runtime smoke도 재실행해 DB 설치 후 앱이 정상 동작하는지 확인한다.

```powershell
.\gradlew.bat :adm:bootJar --offline
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\smoke-adm-runtime.ps1
```

단, 이번 핵심은 DB 전체 설치 재검증이다.
ADM runtime smoke가 실패하면 실패 사유를 기록하되, DB 설치 성공/실패와 분리해서 판정한다.

## 12. CPF_STABILIZATION_REPORT.html 기록 기준

리포트에는 아래 형식으로 기록한다.

[MariaDB 전체 설치 SQL 재실행 / Seed Idempotent / DB 권한 검증]

현재 상태 판정:

* 직전 미검증 항목:
* SQL 파일 상태:
* Flyway 파일 상태:
* seed 파일 상태:
* 요청명/실제 테이블명 불일치:
* BAT 분리 영향:
* 센터컷 확장성 영향:

실행 결과:

* 00_all_install_and_smoke.sql 1회 실행:
* seed 재실행:
* Flyway 기준 검증:
* app 계정 DDL 차단:
* migration/root 계정 DDL 허용:
* FK/index 확인:
* comment 확인:
* 공통 감사 컬럼 확인:

검증 명령:

* 실행 명령:
* 실제 결과:
* 실패 명령:
* 실패 사유:

미검증:

* 미검증 항목:
* 미검증 사유:
* 다음 조치:

최종 판정:

* 완료 / 부분 구현 / 미검증 / 실패 / 재확인 필요

실행하지 않은 검증은 성공으로 기록하지 않는다.
MariaDB를 실제로 실행하지 못했으면 미검증으로 기록한다.
SQL 파일 존재만으로 DB 검증 완료 처리하지 않는다.

## 13. 완료 기준

아래가 모두 충족되어야 이번 요청을 완료로 기록한다.

* 00_all_install_and_smoke.sql 실제 실행 성공
* seed 재실행 idempotent 확인
* Flyway V9 변경분과 all_install SQL 정합성 확인
* app 계정 DDL 차단 확인
* migration/root 계정 DDL 허용 확인
* 핵심 테이블 FK/index/comment/공통 감사 컬럼 확인
* 요청명과 실제 테이블명 불일치가 있으면 리포트에 기록
* README/SQL 가이드/기능 매트릭스/리포트에 실제 결과 반영
* 실패/미검증 항목을 성공으로 기록하지 않음
* 이번 작업 중 BAT 분리와 센터컷 확장성을 방해하는 구조 변경 없음

## 14. 완료 불인정 기준

아래 중 하나라도 해당하면 완료로 기록하지 않는다.

* MariaDB 실제 실행 없이 SQL 검증 완료 처리
* all_install SQL 파일 존재만 확인하고 완료 처리
* seed 재실행 없이 idempotent 성공 기록
* app 계정 DDL 차단 검증 없이 권한 검증 완료 처리
* Flyway V9와 all_install 정합성 미확인
* FK/index/comment/감사 컬럼 확인 누락
* 요청서의 테이블명과 실제 SQL 테이블명이 다른데 성공으로 처리
* 실패한 SQL을 수정하지 않고 성공 처리
* 실행하지 않은 명령을 성공으로 기록
* CPF_STABILIZATION_REPORT.html과 실제 결과 불일치
* 이번 요청 범위를 넘어 BAT/센터컷/로그 정책 구현 착수
* PFW/ADM에 향후 BAT가 가져야 할 실행 책임을 추가하거나 고착화
* PFW에 센터컷 대량 모수/item/result 테이블을 고정 강제하는 방향으로 문서화

## 15. 다음 보강 후보로만 기록할 항목

이번 작업에서 아래 항목은 구현하지 않는다.

* ADM 브라우저 실제 클릭 자동화
* Batch listener heartbeat / 처리율 갱신
* ghost 자동 감지 scheduler / 알림
* BAT 독립 실행 모듈 신설
* 온디맨드 배치
* 센터컷 기본 구현체 + 업무별 커스텀 모수 테이블 연동
* 온라인/배치 로그 정책 + 거래 메타 자동 등록

향후 BAT/센터컷 요청 기준은 아래 방향을 유지한다.

BAT:

* 독립 실행 가능한 Spring Boot batch worker
* executable jar standalone 구동 가능
* 필요 시 WAS/컨테이너 배포 가능
* PFW 공통 Batch API 사용
* ADM은 BAT 상태 관제

센터컷:

* PFW는 대량 모수/item/result 테이블을 직접 소유하지 않음
* PFW는 표준 인터페이스/상태/오류/로그/감사/transactionGlobalId 기준 제공
* BAT는 기본 센터컷 모수/item/result 테이블과 기본 구현체 제공
* 개발자는 필요 시 업무별 커스텀 모수/item/result 테이블 추가 가능
* 업무별 테이블은 CenterCutTargetProvider / CenterCutHandler / ResultProvider adapter로 연동
