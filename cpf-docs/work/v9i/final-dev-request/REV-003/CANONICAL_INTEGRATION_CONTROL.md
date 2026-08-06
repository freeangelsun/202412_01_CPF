# Canonical Integration Control

## 1. 정본 계층

이번 마지막 개발 Campaign은 기존 통합 검증 정본을 대체하지 않는다.

정본 우선순위는 다음과 같다.

1. `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
2. `cpf-docs/work/v9i/REVIEW_INDEX.md`
3. `cpf-docs/work/v9i/DATASET_MAP.md`
4. `cpf-docs/work/v9i/results/REQUIREMENT_STATUS_INDEX.csv`
5. `cpf-docs/work/v9i/results/status/REQUIREMENT_STATUS_PART_001.csv` ~ `PART_004.csv`
6. `cpf-docs/work/v9i/results/INTEGRATION_REQUEST_CLOSURE.csv`
7. `cpf-docs/work/v9i/results/INTEGRATION_REQUEST_UNION.csv`
8. `cpf-docs/work/v9i/results/PROVENANCE_INDEX.csv`
9. `cpf-docs/work/v9i/results/EVIDENCE_CATALOG_INDEX.csv`
10. `cpf-docs/work/v9i/results/TEST_EXECUTION_LEDGER.csv`
11. `cpf-docs/work/v9i/evidence/FINAL_INTEGRITY.json`
12. `cpf-docs/work/v9i/evidence/FINAL_MANAGEMENT_STATE.json`

`final-dev-request/REV-003`은 위 정본을 갱신하기 위한 개발 요청 Package이며 별도 정본이 아니다.

## 2. 고정 보존 기준

현재 통합 기준은 다음을 보존한다.

- exact ID: 47,745
- Source Session: 6
- Source Result 파일: 82
- Integration Request: 32
- 개발GPT 종결 Request: 30
- 외부 Runtime·승인 대기: 2
- duplicate primary: 0
- orphan evidence/execution/request: 0
- hash mismatch: 0

변경으로 ID나 Request가 추가되는 경우에는 append/upsert하고 증가 근거를 남긴다.
기존 ID·request_id를 재번호화하거나 삭제하지 않는다.

## 3. 매 변경 전 필수 확인

각 Requirement 작업 전에 다음을 수행한다.

1. `git fetch origin`
2. 최신 `origin/master` exact SHA 기록
3. Working Tree 확인
4. 통합 원장 Index와 Part의 행 수·크기·SHA 검증
5. orphan·duplicate·hash mismatch 검증
6. 해당 Requirement의 기존 exact ID, request_id, Source, Consumer, Evidence 조회
7. 다른 Requirement와 공유 계약·호출 경로 영향 확인

사전 통합 검증이 실패하면 제품 변경부터 시작하지 않고 원장 또는 기준선 결함을 먼저 기록한다.

## 4. 매 변경 후 필수 확인

각 Requirement 구현·검증 후 다음을 수행한다.

1. 변경 Source·SQL·API·Test·Config·Frontend·Script를 Change Manifest에 기록
2. 실제 Consumer와 호출 경로 갱신
3. 관련 exact ID와 request_id를 append/upsert
4. Requirement 상태·실행 결과·Evidence 경로 갱신
5. 회귀 영향 Requirement 재검증
6. Index/Part 행 수·크기·SHA 재생성
7. duplicate·orphan·hash mismatch 재검증
8. 기준 Commit과 Evidence SHA 일치 확인
9. `INTEGRATION_DELTA.md`에 변경 전후 차이 기록

## 5. 환경 의존 항목

Java25, DB3, Browser, Multi-process 실제 Runtime이 없더라도 통합 검증은 생략하지 않는다.

개발GPT는 다음을 통합 정본에 반영한다.

- 구현 변경
- 대체검증 결과
- 미실행 Target Runtime
- Codex/QA 이관 Owner
- 정확한 실행 명령
- 성공·실패 기준
- Evidence 예정 경로

실제 Runtime 미실행은 `verification_status=미검증`으로 유지한다.
환경 부족 때문에 같은 실행을 반복하지 않지만, 통합 원장 갱신과 무결성 검증은 매 변경마다 수행한다.

## 6. 종료 조건

25건 개발 요청의 완료 보고 전 다음이 모두 필요하다.

- 모든 Requirement의 사전·사후 통합 검증 기록
- 기존 47,745 exact ID와 32 request_id 보존 또는 증분 근거
- duplicate/orphan/hash mismatch 0
- 변경 파일과 Evidence의 최신 SHA 일치
- Development 상태와 Runtime Verification 상태 분리
- Codex/QA 이관 항목 명확화
- QA 통과 전 전체 완료 주장 금지

## FDEV-025 Append Rule

FDEV-025는 Campaign Requirement 원장에만 append하며, 47,745 exact ID Part와 32 integration request ID는 그대로 보존한다.
