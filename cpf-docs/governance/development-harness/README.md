# CPF Development Harness

개발/QA 단일 진입점: `CPF_DEVELOPMENT_HARNESS.md`

## 가장 먼저 볼 것

1. `CPF_DEVELOPMENT_HARNESS.md` — 전체 실행 규칙, Current Merge Control State
2. `current/CURRENT_WORK_ITEM_REGISTRY.csv` — **유일한 작업대상/상태 정본**
3. `standards/CPF_RULE_MODEL_AND_IMPACT_SEARCH_STANDARD.md` — 공통 규칙·기능별 규칙·검색/영향도 추적
4. `standards/CPF_WORK_ITEM_SESSION_MERGE_AND_REPORT_STANDARD.md` — sessionKey·개별 Evidence Block·미Merge 자동 탐색·Merge·Final Self Review
5. `product/CPF_PRODUCT_ARCHITECTURE_AND_REQUIREMENTS.md` — Product Contract/Architecture
6. `current/ROLE_EXECUTION_LEDGER.csv`, `current/TEST_EXECUTION_LEDGER.csv`, `evidence/` — 역할/Test/실행근거. **별도 작업 목록이 아님**

## 관리 원칙

- 작업대상 목록을 역할별·세션별로 따로 만들지 않는다. 새 Requirement/Finding/Defect는 `CURRENT_WORK_ITEM_REGISTRY.csv` 한 곳에 Root Cause 단위로 병합·세분화한다.
- 개발/검수 세션은 고유 `sessionKey`와 `evidence/<role>/current/sessions/<sessionKey>/SESSION_REPORT.md`를 남긴다.
- 다음 작업자는 신규 개발 전에 미Merge Session을 전수 검색·검증·Merge한다. 사용자에게 별도 Merge 지시를 기다리지 않는다.
- 여러 Work Item의 `일괄 완료/일괄 PASS/일괄 SKIP`을 금지한다. Work Item마다 독립 Evidence Block이 필요하다.
- 실행 환경 버전은 과거 대화나 사용자 PC 값에 맞추지 않고 **Current Source의 canonical prerequisite를 다시 읽어 required/actual을 비교**한다.
- 최종 완료 전 모든 Mandatory Work Item을 한 건씩 Self Review하고 QA가 Merge completeness와 최종 Acceptance를 확인한다.
- Harness 규칙/구조는 Current Harness에서만 관리하고 과거 버전별 Harness 폴더·backup·checkpoint를 만들지 않는다.

## 구조

- Product Contract: `product/`
- Current Work Registry/Status: `current/`
- Common/Feature/Execution Standards: `standards/`
- Role/Session Evidence: `evidence/`
- Machine Contracts: `contracts/`
- Validators/Negative Fixtures: `validators/`, `tests/`

## 현재 Source에 존재하는 Harness Self Gate

Repository에 존재하지 않는 Wrapper 경로를 문서상 정본으로 가정하지 않는다. Current Harness 자체 검증은 실제 존재하는 다음 진입점을 기준으로 한다.

```powershell
python .\cpf-docs\governance\development-harness\validators\run_all_gates.py
python .\cpf-docs\governance\development-harness\validators\show_status.py
```

Linux에서도 동일 Python entrypoint를 사용한다. 제품 최고강도 Runtime은 Current Work Item과 `current/CPF_REQUIRED_FULL_RUNTIME_REQUEST.md`의 실제 canonical 실행경로를 확인해 수행하며, 환경 미충족 단계는 PASS로 만들지 않는다.
