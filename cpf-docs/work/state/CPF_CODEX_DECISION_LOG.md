# CPF Codex Decision Log

진행 로그가 아니라 다른 PC와 새 세션에서도 유지해야 하는 Architecture/Ownership/Migration 결정만 기록한다.

## DEC-CODEX-001 — DB Vendor Pack 중앙 소유

- 결정: MariaDB, PostgreSQL, Oracle의 Provision/Install/Seed/Migration/Rollback/Verify SQL은 `cpf-tools/db` 중앙 Pack이 소유한다.
- 이유: Vendor 선택으로 Java 업무 Source가 달라지거나 각 Module에 Vendor SQL이 반복 복제되는 것을 방지한다.
- 결과: 동일 Java Source/Artifact를 유지하고 외부 resource overlay와 DB lifecycle 설정으로 Vendor를 선택한다.

## DEC-CODEX-002 — Generated Domain은 Metadata 기반 무제한 확장

- 결정: DomainName/SystemCode/Package/TablePrefix를 canonical metadata에 적용하며 MBR/EXS/PAY 등 고정 목록이나 switch를 사용하지 않는다.
- 이유: 신규 Domain 추가 때 중앙 Tool Java/Python Source 수정 또는 3-Vendor SQL 수작업 복제가 필요 없어야 한다.
- 결과: MBR/EXS는 제품 고정 업무가 아니라 동일 Generator 회귀 Domain이다.

## DEC-CODEX-003 — Generated Domain DB Ownership

- 결정: Generated Domain은 별도 물리 DB와 principal을 생성하지 않고 외부에서 Provision된 `CUSTOMER_BUSINESS_DB`에 table prefix로 논리 소유권을 둔다.
- 이유: 고객 업무 DB의 원자성, 권한 소유권, 무제한 Domain 확장을 유지하고 CPF Platform DB와 분리한다.
- 결과: Generator DB lifecycle은 Install→Seed→Migration/Verify/Rollback만 수행하며 Provision/principal은 Customer DB lifecycle 소유다.

## DEC-CODEX-004 — Platform과 Generated Domain 분리

- 결정: CPF/CMN/ADM/BZA/BAT/EDU Platform Seed·Schema와 Generated Domain Sample Template을 별도 canonical owner로 관리한다.
- 이유: 삭제·재생성 가능한 Domain에 ADM/Platform이 종속되거나 Generated Domain이 실제 회원·계좌 같은 임의 업무 모델로 고정되는 것을 방지한다.
- 결과: Platform Seed의 과거 REF sample 메시지는 EDU 소유 `MEDU010001/EEDU010001`로 currentize했다.

## DEC-CODEX-005 — Historical Migration 불변

- 결정: 이미 배포·checksum 고정된 migration 본문은 수정하지 않는다.
- 이유: Upgrade 재현성과 감사 가능성을 보존한다.
- 결과: 데이터/구조 보정은 canonical metadata와 새 forward migration/rollback으로 생성하며 기존 checksum은 append-only로 관리한다.

## DEC-CODEX-006 — Generated Runtime Query Overlay

- 결정: Generated Project 내부의 module-local Mapper XML을 소유하지 않고 Data Starter의 selected-Vendor generated-resource overlay를 사용한다.
- 이유: Domain마다 Vendor SQL을 복제하지 않고 동일 Source를 유지한다.
- 결과: `cpf-member`와 `cpf-external`의 과거 Mapper XML은 승인된 stale deletion이며 복구하지 않는다.

## DEC-CODEX-007 — 검증 Evidence 승계 조건

- 결정: HEAD, command hash, 관련 Source/SQL/Config/Profile, Vendor/Docker 환경, exit code, log hash, 필요한 artifact hash가 모두 같은 PASS만 재사용한다.
- 이유: 다른 SHA·환경·명령의 과거 PASS가 현재 결함을 숨기지 않게 한다.
- 결과: 최종 Commit SHA에서 Runtime/DB/Root high-cost stages를 다시 실행한다.

