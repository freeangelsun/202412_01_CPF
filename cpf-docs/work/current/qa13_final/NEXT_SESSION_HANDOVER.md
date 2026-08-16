# CPF QA13 최종 개발 Handover

## 현재 판정

- 개발 GPT Source/Static/independent gate: 완료.
- 전체 QA: `RUNTIME_REVERIFY_REQUIRED`.
- 사용자 승인 없는 Git write/delete/history rewrite: 없음.
- 제품 삭제 대상: 0.

## 기준

- pre-development Content SHA-1: `6ce96c49fbfca3b26ab172187ac06fe279e09040`
- pre-development Content SHA-256: `91a58a0a50abbba56f75c5ba5f4aa5cf84965353a54cbb586bca38177ba09eea`
- result Content SHA-1: `6371c50581487bf4061415e60487e3cf27383f28`
- result Content SHA-256: `56f9edce7dc5f60eb1f38d10d46a14da41812968013f093c5762a0d97c7e6565`

## 세션 지속 루틴

사용자가 "로컬 전달/적용/테스트 실행"이라고 하면 개발 GPT는 기다리지 않는다.

1. 현재 적용 Source를 기준으로 다음 사이클 정적/기능/보안/계약/오케스트레이터/Evidence 검수 시작.
2. SPECIAL 20, Developer/Adoption REWORK, Capability Management 전수 영향도 재개방.
3. 다음 로컬 Runtime 항목/Evidence/실패조건을 미리 준비.
4. 로컬 결과가 도착하면 기존 선행검수에 결과만 병합.
5. FAIL은 증상별이 아니라 Root Cause로 묶어 Source/Config/SQL/API/Frontend/Test/Verifier를 일괄 수정.
6. 중간 선행작업 ZIP은 주지 않고 내부 누적. 로컬 결과 대응 사이클에서 Overlay를 한 번에 제공.
7. 작업량은 가능한 최대 범위로 한 사이클에 처리.

## Capability Management 최종 기준

`ENABLE → USE → OBSERVE → MANAGE → RECOVER → AUDIT`

시스템이 이미 아는 systemCode/domain/application/module/instance/starter/capability/provider/version/transactionId/traceId/executionId/operation은 개발자가 반복 입력하지 않는다. Public API/Annotation/Starter/Context/Runtime registration에서 자동 수집한다.

ADM Top-Level 공통 운영 IA:

- 운영 현황
- 로그·추적
- 장애·복구
- 설정·정책
- 감사·변경이력

고유 Workflow 전용 메뉴:

- Batch
- Gateway
- Security / Approval
- Deployment

신규 Starter는 Canonical Catalog에 추가되면 runtime metadata/ADM common read model에 자동 편입되어야 하며 수기 메뉴 추가를 요구하지 않는다.

## 다음 입력

Overlay 적용 후 생성된 최신 `CPF_LOCAL_VALIDATION_<timestamp>.zip` + 최신 전체 Source ZIP.
