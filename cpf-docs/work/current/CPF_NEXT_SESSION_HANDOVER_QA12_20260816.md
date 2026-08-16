# CPF 다음 세션 Handover — QA12 FullLocal 재검수 연속성

## 기준

- 입력 Source: `CPF_FULL_SOURCE_FOR_NEXT_QA(20260816-061343).zip`
- 입력 FullLocal: `CPF_LOCAL_VALIDATION_20260816_132902.zip`
- 입력 FullLocal 집계: PASS 102 / FAIL 30 / SKIP_ENV 3 / TOTAL 135
- 결과 Content SHA-1: `6ce96c49fbfca3b26ab172187ac06fe279e09040`
- 결과 Content SHA-256: `91a58a0a50abbba56f75c5ba5f4aa5cf84965353a54cbb586bca38177ba09eea`
- 이번 Overlay 삭제 대상: 0
- Git commit/push/history 변경: 수행하지 않음

## 반드시 함께 승계할 3개 관리축

1. 최신 통합 Handover의 실행 순서와 FullLocal Closure 규칙
2. SPECIAL-01~20 품질 Backlog
3. Developer/Adoption REWORK-01~10

과거 PASS를 새 Source 성공으로 자동 승계하지 않는다. 새 전체 Source ZIP이 오면 그 ZIP이 새 기준이다.

## 이번 재개발 결과

FullLocal 30 FAIL을 Build graph, Generator/Local runtime 계약, Evidence/identity, Windows Python, DB lifecycle stale test, Frontend/generated-client, Performance/orchestrator 원인군으로 묶어 Source/Test/Verifier/Generator/Frontend/Evidence를 보정했다.

현재 Clean Source에서 확인한 범위:

- Verification 45/45 PASS
- Testing Tools 80/80 test files PASS / FAIL 0
- Runtime Tools 65 PASS / 2 SKIP / 7 subtests PASS
- Generator 27 PASS / 10 SKIP / 6 subtests PASS
- DB Verification 75/75 PASS
- NXT3 22/22 individual gates PASS
- Starter Catalog 64 / Public 24 / Internal 40 PASS
- Public Function TOP100 / Golden20 PASS
- Batch Developer TOP50 50/50 PASS
- ADM 321 / BZA 96 OpenAPI static closure PASS

## 다음 Windows FullLocal 필수 Closure

`cpf-docs/work/current/CPF_NEXT_LOCAL_DEVELOPMENT_REQUIREMENTS.md`를 따른다. 특히 Java25 build/test/publication, npm full verify, Browser E2E/A11y, Local 1-WAS, FileLog↔DB↔ADM correlation, DB3, Redis/Valkey, Kafka, Batch process kill/UNKNOWN/reconcile, Gateway/Topology, Security/Masking, live Performance를 실제 실행한다.

NXT3는 Windows에서 aggregate 22/22도 다시 확인한다. Docker가 정상인데 Runtime 단계가 SKIP되거나 1-WAS가 정상인데 Integrated Logging이 NOT_EXECUTED/SKIP이면 Orchestrator 결함으로 재개발한다.

## 종료 조건

FAIL 0 + 필수 Runtime PASS + 영향도 재검수 PASS + Fresh Apply/Evidence fail-closed + QA 최종 통과 전에는 전체 완료가 아니다.

## 다음 세션 강제 작업 루틴 — 로컬 검증과 병행

사용자가 `로컬 적용`, `로컬 전달`, `FullLocal 실행 중`이라고 알리면 다음 세션은 대기하지 않는다. 현재 적용 Source를 내부 기준으로 즉시 재구성하여 다음 개발 사이클의 정적·기능·보안·계약·오케스트레이터·Evidence 검수와 다음 로컬 Stage 준비를 병행한다. 로컬 결과가 도착하면 그 결과만 기존 선행검수에 병합해 Root Cause 단위 재개발을 이어간다.

선행 작업은 별도 ZIP으로 중간 전달하지 않는다. 다음 로컬 결과까지 합친 뒤 한 번의 Overlay에 최대 범위를 묶는다. Runtime 전용 항목만 로컬 Evidence로 닫고, 현재 환경에서 확인 가능한 결함은 미리 수정·재검수한다.

