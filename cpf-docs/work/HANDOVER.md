# CPF 최종 개발 패키지 인수인계

작성 시각: `2026-08-18 10:42:05 +0900`

## 1. 기준
- 작업 시작 Source: `CPF_FULL_SOURCE_FOR_NEXT_QA_20260817_205301.zip`
- Baseline Source ZIP SHA-256: `fb0cb9cc190f79db066e2872fa30b6002eac3140331c2a33f4716f86ba30ed41`
- Git exact SHA: ZIP에 `.git`이 없어 검증 불가. 과거 SHA를 현재 exact SHA로 사용하지 않는다.
- 제품 공식 Java: Java 25. GPT 환경은 Java21 대체 compile/정적 검증만 사용.
- 사용자 승인 없는 Git commit/push/branch/reset/restore/clean/history 변경 없음.

## 2. 최신 Architecture 핵심
- Business Transaction Identity는 Channel vocabulary 사용.
- 외부 직접 CPF 호출 필수 Header: Transaction Id, Original/Caller/Target Channel, Target Operation Id = 5개.
- Current Channel은 Receiver Generated Domain canonical `systemCode`로 Framework가 자동 확정.
- `systemCode`를 다른 Channel 값으로 Mapping하지 않고 동일 값을 사용.
- Channel Policy: `operationId + callerChannel`.
- Runtime System/Domain/Instance/Host는 별도 trusted metadata이며 Channel Header를 SystemCode로 재해석하지 않음.
- transactionId 34자리 포맷을 유지하고 내부 3자리 값은 issuer metadata이며 Original Channel과 동일시하지 않음.
- operationId / transactionId / executionId 및 current/target operation을 분리.

## 3. 개발 완료 영역
Operation DB3/Bootstrap/Discovery/ADM, Channel enforcement, Transaction hooks, REST/Boundary Result, durable Async, File Context, Cache DX, Testkit 중복정리, Generator/Generated Domain, EDU 20+15 전면 재개발, Frontend automatic Bootstrap contract, DB3 append migrations V121~V127, stale verifier/tool currentization.

## 4. 실행 검증 요약
자세한 수치는 `TEST_AND_EVIDENCE.md` 참조. 핵심 정적/독립 Gate는 PASS. Testing Tools는 전체 파일을 분할 실행하여 378 PASS / 22 환경 Skip, Runtime Tool 65 PASS / 2 Skip, Generator Test 27 PASS / 10 Skip.

## 5. 완료로 승계하면 안 되는 항목
Java25 Root Gradle, live Oracle/PostgreSQL/MariaDB, Docker/Redis/Valkey, Multi-WAS/Process-Kill, actual Browser E2E는 이 환경에서 미검증. 반드시 사용자 로컬 FinalLocal 결과로 판정.

## 6. 다음 실행
1. Overlay 적용
2. Delete Manifest 한 줄 적용
3. 사용자 Java25 `run-cpf-final-local-validation.ps1` 실행
4. Downloads의 최신 `CPF_LOCAL_VALIDATION_*` 결과 ZIP을 다음 QA/개발 세션에 전달
5. FAIL이 있으면 같은 Requirement를 공통 Root Cause로 다시 보정하고 QA 통과까지 반복

## 7. 보호/삭제
- Delete Manifest 파일 항목만 삭제하며 Directory delete 금지.
- 보호경로 삭제 0건을 NXT3 Gate로 확인.
- `.pytest_cache`, `__pycache__`, `*.pyc`, root `/build/` 등 ignored 생성물은 최종 Source/Overlay에서 제외.

## 8. Codex
`cpf-docs/work/current/CODEX_REVALIDATION_REQUEST.md`를 사용한다.
