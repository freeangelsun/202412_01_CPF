# CPF QA36 프로젝트 전체 작업 전 독립 리뷰

## 1. 기준

- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- latest exact SHA: `95e592c05fc457301efdb13ee50e0d7453325806`
- Commit: `20260801_02`
- Canonical Target: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
- Canonical Requirement: **162**
- Active legacy detailed QA: **1,873**
- Active legacy execution scenarios: **441**
- QA36 active gap requirements: **85**
- Canonical 17-axis mandatory scenario rows: **2,754**
- Current overall judgment: **실패 / Release·GA 승인 금지**

## 2. 이번 리뷰가 이전 QA35와 다른 점

이전 QA35 V2는 ADM·EDU·Frontend의 중대한 누락을 보강했지만 프로젝트 전체 QA를 대신할 수 없었다.
QA36는 최상위 162 Requirement 전체, 공식 Module·Starter·Batch submodule·Build·DB·Release·Docs를
하나의 Master QA 구조로 재개방한다.

기존 1,873개 상세 QA와 441개 시나리오는 폐기하거나 85개 Gap으로 대체하지 않는다.
원본 ID를 import하여 Canonical 162와 `maps-to`, `split-into`, `superseded-by`, `duplicate-of` 관계를 기록해야 한다.

## 3. 최신 Git 직접 판정

1. latest master는 docs-only commit이다.
2. latest exact SHA에 CI Status와 Workflow Run이 없다.
3. Current Work Request는 실제와 달리 독립 검증 한 건만 남았다고 선언한다.
4. ADM OpenAPI는 과거 SHA와 인증 2개 operation만 포함한다.
5. ADM marker는 schema 2지만 verifier는 schema 3과 추가 generated files를 요구한다.
6. 따라서 Frontend deterministic build contract는 Source 수준에서 실패다.
7. cpf-reference, local web, local batch 모듈은 존재하지만 전체 실행·fault·evidence는 미검증이다.
8. Guide는 latest보다 한 Commit 이전 SHA를 기준으로 대규모 기능 절차를 설명한다.

## 4. 전체 QA 범위

- Architecture/Public Boundary
- Core Contract/Runtime
- Common/Data/3DB
- Gateway/External/Event/Saga
- Batch/Scheduler/Worker/Center-Cut/Agent
- ADM/BZA/Frontend/BFF
- Security/Privacy/Audit
- Observability/SLO/Incident/DR
- Generator/Generated Domain
- EDU/Reference Runtime
- API/OpenAPI/Generated Client
- Build/Artifact/Deploy/Supply Chain
- Test/Runtime/Browser/Broker/Fault/Evidence
- Documentation/Governance/Product Packaging
- Repository Hygiene

## 5. 상태 원칙

162 Canonical Requirement 중 `완료`로 승인한 항목은 없다.
모든 Runtime 검증은 latest SHA에서 실행되지 않았으므로 `verification_status=미검증`이다.
Source 모순이 확인된 항목은 `실패`, 일부 구조가 존재하는 항목은 `부분 구현`,
충분한 Source 확인이 불가능한 제품정책·DB 기능은 `재확인 필요`다.
