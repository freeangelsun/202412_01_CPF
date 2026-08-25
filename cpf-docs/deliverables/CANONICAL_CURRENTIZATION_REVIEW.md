# CPF 개발 정본 Currentization 리뷰 — 2026-08-25

## 기준 Source

- Baseline ZIP SHA-256: `d2e89aba1841a4387a473610db905415f8565fcf09d06a56a8afa3a1b33a3a48`
- Current Product Source SHA-256: `c79be31a71c15c02665d56e29c0f51244c91ab3894183775ce311cde3dbf40df`
- Canonical Requirement: `208`
- 전체 상태: Source/Static/Contract/Substitute 개발 범위 완료, 필수 live Runtime `미검증`

## 이번 정본 추가·현행화

- `GEN-CUSTOM-LIB`: 고객사 공통 JAR 작업공간 Generator. `cpf-common`을 수정하지 않고 `customer-libraries/<name>`을 생성한다. 모든 Domain에 전역 자동주입하지 않으며 `attach`로 선택한 Domain에만 dependency를 연결한다. 생성 설명/CLI 도움말은 한글을 기본으로 한다.
- `DEVEX-WINDOWS-PATH`: 프로젝트 Root 상대경로+파일명 전체 `<=200`. 200자 초과는 warning이 아니라 FAIL이며 장문 Evidence는 안정적 alias와 `PATH_ALIAS_MAP.csv`를 사용한다.
- `DEVEX-DOCKER-LIFECYCLE`: 필요한 Runtime 컨테이너가 내려가 있으면 Harness가 자동 기동하고 health+functional readiness를 확인한다. Harness가 시작한 컨테이너만 종료 시 정리한다.

## 정본 운영

작업 중 Finding을 열 때부터 `CPF_CANONICAL_DEVELOPMENT_CLOSURE_INVENTORY.csv`, `REQUIREMENT_STATUS.csv`, Developer Review/Evidence/Handover를 함께 현행화한다. QA/Codex 소유 판정은 Developer GPT가 임의 변경하지 않는다.

## Overlay 정책

결과 ZIP에는 Baseline과 바이트가 다른 `MODIFY`와 Baseline에 없던 `ADD`만 포함한다. 변경되지 않은 파일은 포함하지 않는다. 삭제는 payload로 표현하지 않고 사용자 승인 `DELETE_MANIFEST.csv`로만 전달한다.

## 검증 판정

현재 Source에서 실행 가능한 정적/계약/Substitute 검증은 관측 FAIL `0`이다. 그러나 Java25 Root Gradle, Docker DB3 3사, 2-worker 장애/복구, One-WAS, Browser, Performance, Fresh Replay는 Assistant 환경 제약으로 미검증이다. 따라서 QA-ready/전체 완료로 승격하지 않는다.

Generated at: `2026-08-25T17:53:08+09:00`
