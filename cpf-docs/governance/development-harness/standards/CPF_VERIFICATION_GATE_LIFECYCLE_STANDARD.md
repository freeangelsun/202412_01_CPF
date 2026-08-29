> Development Harness 내부 통합 표준. 이 파일은 독립 정본이 아니며 `CPF_DEVELOPMENT_HARNESS.md`의 통제를 받는다.

# CPF Verification Gate Lifecycle Policy

> 역할: 검증 진입점과 Evidence 생명주기를 정의한다. 제품 Acceptance Criteria는 `../product/CPF_PRODUCT_ARCHITECTURE_AND_REQUIREMENTS.md`가 정본이다.

## 1. Stable Entrypoint

검증 Script/Verifier는 기능 Owner와 안정된 경로를 사용한다. 날짜/QA 회차/세션 이름으로 verifier를 계속 복제하지 않는다. 기존 Gate를 현행화한다.

## 2. False Green 금지

- 대상 파일/operation/route/test가 0건이면 기대 Count가 0이라고 명시된 경우 외에는 FAIL한다.
- stale path/retired module/old header policy를 검사해 PASS하지 않는다.
- Product Consumer 없이 sample/interface 존재만 확인하는 Gate를 완료 증거로 사용하지 않는다.
- Git checkout mode와 ZIP/fallback mode가 모두 있는 verifier는 두 실행경로를 모두 검증한다.

## 3. Failure Aggregation

가능한 Gate를 끝까지 실행해 실패를 Root Cause별로 집계하고 동일 패턴을 전수검색한 뒤 일괄 보정한다.

## 4. Evidence

Current Evidence에는 source identity, command, environment, exit code, start/end time, result/report hash를 남긴다. 과거 실패/결정은 Current Requirement에 필요한 의미만 흡수하고 Current Evidence와 경쟁시키지 않는다.

## 5. Runtime

미실행 Runtime은 `미검증`. 환경장애는 실제 오류와 재실행 조건을 기록한다. Static PASS로 Java25/live DB/Multi-WAS/Browser E2E를 대체하지 않는다.
