# PROJECT FINALIZATION

다음 세션/작업자는 **CPF 프로젝트 자체를 이번 연속 요청에서 100% 끝낸다**는 전제로 작업한다.
목록의 일부 완료가 아니라 Project Completion이 목표다.

# CPF V9I Handover

## Baseline
- latest master: `3ed676061246c9db3e44f29e254c0393ecca3929`
- R6J QA A/B both reviewed same SHA
- current verdict: **RELEASE_BLOCKED**
- central findings: 56
- direct development rework: 34
- central requirement ledger: 93

## Next Action
1. Developer GPT receives `final-dev-request/CPF_DEVGPT_R6J_FULL_100_PERCENT_EXECUTION_INSTRUCTION.md` and owns the entire 100% scope
2. Developer implements direct 34 + self-discovered defects
3. Developer produces Root Overlay ZIP; GPT does not Push
4. User applies/Pushes
5. Central rechecks new master SHA
6. QA A/B scopes rotate and re-review same IDs + new findings
7. target runtime/Codex evidence must bind to new exact SHA

## Architecture
- ADM is delivered CPF Product.
- EDU is adopter-facing Public API/SPI/Extension/Integration education.
- EDU-ADM: Product 9 / Extension Sample 4 / Merge 4.
- 17/135 numeric preservation is not a completion goal.

## Transaction/Logging
Release-critical:
- same transactionId across nested/remote/async/message/batch
- ADM one-shot full timeline/tree
- DB3 standard identifiers/index/retention
- FileLog durable spool/retry/dedup/loss alert
- masking/raw permission/audit
- failure/process-kill evidence

## Collaboration
- QA/Developer discover issues beyond explicit requirements.
- A/B scopes rotate.
- opinions/disagreements are preserved and centrally adjudicated.
- no prior PASS is inherited without current-SHA evidence.

## Completion Scope
- 34 direct defects are not a split assignment.
- Developer owns all 93 requirements, all 56 findings, canonical GA, runtime, and self-found defects.
- Each QA owns 100% full-product verification.

## 100% Finalization Mandate

다음 개발/QA 세션은 일부를 처리하는 세션이 아니다.
현재 남은 전체를 100% 종료 대상으로 가져간다.

- Developer: current Requirement/Finding/Runtime/self-found 전체
- QA A: full product 100%
- QA B: full product 100%
- Central: stricter merge + remaining 100% next scope

부분 구현/미구현을 정상 종료 상태로 남기지 않는다.
