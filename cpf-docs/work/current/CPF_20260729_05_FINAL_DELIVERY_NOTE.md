# CPF 20260729_05 최종 Root Overlay 전달서

## 기준

- Repository: `https://github.com/freeangelsun/202412_01_CPF`
- Branch: `master`
- 개발 기준 SHA: `b8941577b99535ff3e64a4fad99b74bafa544227`
- 최상위 정본: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
- Canonical Requirement: 162개
- Enterprise QA Requirement: 816개
- QA Scenario: 387개
- Commit/Push/Branch/Tag/PR: 수행하지 않음

## 최종 Source Closure

- Source Closure Gate: 31 PASS / 0 FAIL
- Generator 멱등 Template Gate: MariaDB/PostgreSQL/Oracle 3종 PASS
- Generator 핵심 Java Template Stub Compile: 33 Source PASS (`--release 21`)
- ADM/BZA Frontend Syntax: 20 files PASS
- Workflow YAML Parse: PASS
- Public Raw Map, Core Internal Import, Direct Client, Secret Literal, Root Hygiene: PASS

## 최종 보강 내용

- Generated Domain CREATE/UPDATE/DELETE의 Request Hash 기반 멱등 원장을 업무 Row와 분리했다.
- 동일 Idempotency Key·동일 요청은 재실행하지 않고, 동일 Key·다른 요청은 충돌로 차단한다.
- MariaDB/PostgreSQL/Oracle Mapper·Install·Migration·Rollback·Verify Template을 함께 갱신했다.
- Local Web/Batch Runtime의 Gradle 논리 이름은 유지하고 물리 Source를 `cpf-tools/runtime/*`로 이관했다.
- Root `deploy`는 제품 배포 자산 정본으로 유지하며 Java Source Module을 넣지 않는다.
- 적용 Script는 Root Local Runtime과 중복 Build Module을 손실 없이 정본 위치로 이관한다.

## 검증 경계

이 실행 환경에는 Java 25, Gradle 9.1 전체 의존성, PowerShell, Oracle/PostgreSQL/MariaDB Runtime, Redis Topology, Browser, 다중 인스턴스 환경이 없어 해당 통합 실행 결과는 `미검증`이다. 실행하지 않은 결과를 PASS로 기록하지 않았다.

Codex는 개발자가 아니라 검수자이며, 적용 후 exact SHA에서 Build/Test/DB/Redis/Browser/Multi-instance/Generator 387개 Scenario를 실행하고 결함만 반환한다. 결함 수정은 ChatGPT 개발 세션이 담당한다.
