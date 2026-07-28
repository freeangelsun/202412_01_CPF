# CPF 20260728_02 Final Completion Root Overlay Manifest

## 기준

- Repository: `https://github.com/freeangelsun/202412_01_CPF`
- Branch: `master`
- 적용 기준 Commit: `ecaddd581a88ede22b63116effd61313744b3fbe`
- Package: `CPF_20260728_02_FINAL_COMPLETION_ROOT_OVERLAY.zip`
- Commit / Push / Branch 생성: 수행하지 않음

## 구현 포함 범위

- Runtime Control 상태 계약과 실제 Runtime Consumer 연결
- ADM Runtime Change Center 및 Runtime Group 운영 화면/API
- Gateway replayable request spool, streaming response, Range·conditional·query 전달
- Gateway 인증 Principal 및 exact authority 판정 보정
- Service Call transport retry·timeout·unknown-result 분류
- Batch Scheduler·Worker·Center-Cut·Host-Agent Runtime 정책 Consumer 연결
- External Institution endpoint·layout·timeout Runtime 적용
- Generator 계약 및 사전 검증 보강
- Oracle·PostgreSQL·MariaDB ADM 메뉴·권한 migration/rollback/checksum
- 적용·정적 검증·인수인계·Codex 통합 검증 요청서

## 검증 판정

- 정적 Overlay Gate: `PASS`
- Runtime API 독립 compile/behavior: `PASS`
- Gateway spool/stream 독립 behavior: `PASS`
- Batch Runtime Policy/Consumer 독립 behavior: `PASS`
- External Institution Runtime 독립 behavior: `PASS`
- ADM TypeScript 구문: `PASS`
- 전체 Java 25 / Gradle 9.1 build, 실DB, Browser, 다중 인스턴스: `미검증`

상세 결과는 `VALIDATION_LEDGER.md`와 `evidence/`를 확인한다. 실행하지 않은 통합 검증은 성공으로 기록하지 않았다.
