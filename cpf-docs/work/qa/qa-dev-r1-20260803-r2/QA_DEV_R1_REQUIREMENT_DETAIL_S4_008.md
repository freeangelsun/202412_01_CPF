# QA Requirement Detail — CPF-SELF-DEV-S4-008

## 판정

- QA 결과: `미통과`
- 기준 SHA: `2903de14eb9cd6cfcccf8e4d2a3489ee2e4193ca`
- Requirement: 공식 DB Vendor exactly-three Manifest·물리 Lifecycle 정합성
- QA 회차: `QA-DEV-R1`

## 실제 확인 파일

1. `cpf-tools/db/vendor-pack-manifest.json`
2. `cpf-tools/db/vendor/mariadb/pack.json`
3. `cpf-tools/db/vendor/postgresql/pack.json`
4. `cpf-tools/db/vendor/oracle/pack.json`
5. 각 Vendor `install/00_empty_install.sql`
6. `cpf-tools/scripts/verify-cpf-db-vendor-manifest.py`
7. `cpf-tools/scripts/tests/test_verify_cpf_db_vendor_manifest.py`
8. `cpf-docs/work/evidence/20260803/session4/P04_DB_VENDOR_R2_TARGETED.json`

## Gate 확인 범위

- 공식 Vendor가 MariaDB/PostgreSQL/Oracle 정확히 3개인지 확인
- Lifecycle Key와 36개 경로/디렉터리 존재 확인
- Path Escape와 Fixed Domain List 정책 확인

## 치명적 SQL 결함

PostgreSQL과 Oracle Fresh Install SQL에 MariaDB 전용 타입이 포함돼 있다.

- PostgreSQL:
  - `payload LONGBLOB`
  - `header_json MEDIUMTEXT`
  - `attribute_json MEDIUMTEXT`
- Oracle:
  - `payload LONGBLOB`
  - `header_json MEDIUMTEXT`
  - `attribute_json MEDIUMTEXT`

이 SQL은 각 Vendor에서 Fresh Install Syntax 오류를 일으킨다.

## 미통과 근거

1. Gate는 SQL 내용을 읽거나 Vendor 문법을 검사하지 않는다.
2. Canonical Schema → Vendor DDL Type Mapping을 대조하지 않는다.
3. DDL/DML/Index/FK/Metadata parity를 검사하지 않는다.
4. Migration 순서, Checksum, Drift, Rollback 역연산을 검사하지 않는다.
5. 실제 SQL 결함이 있는데도 Path 36개 존재로 PASS했다.
6. 실제 DB Runtime 이전에 Source/Generator 결함을 먼저 수정해야 한다.

## 재개발 요청

- Canonical Type Mapping의 BLOB/CLOB/Text Vendor 변환 수정
- PostgreSQL/Oracle Lifecycle Pack 전체 재생성
- Vendor별 SQL Parser 또는 Container Fresh Install Gate 추가
- Install → Seed → Verify → Migration → Rollback → Reinstall 실행
- Schema Object/Column/Index/FK Metadata parity 자동 비교
- Generator Source와 산출물 Hash 정합성 검증

## 성공 기대 결과

- PostgreSQL/Oracle에서 MariaDB 전용 Token 0건
- 3 Vendor Fresh Install/Upgrade/Rollback 성공
- Rollback 후 Object Drift 0
- Runtime Query와 DDL Metadata 일치
