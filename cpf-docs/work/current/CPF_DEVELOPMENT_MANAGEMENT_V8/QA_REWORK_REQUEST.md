# QA Rework Request

현재 이 패키지는 QA 결과를 선점하지 않는다. QA가 결함을 발견하면 `templates/QA_REOPEN_FEED_TEMPLATE.csv`에 다음을 기록한다.

- 회차와 기준 SHA
- 대상 Entity/Requirement/Scenario
- `REDEVELOP`, `REREVIEW`, `INVALIDATE_IMPACT`, `REOPEN_OWNER`, `EXTERNAL_BLOCK`
- 결함 근거·영향 경로·수정 내용
- 재실행 명령·기대 결과·실패 기준
- 요구 Evidence·미조치 위험

`apply-qa-reopen-feed.ps1` 실행 후 다음 개발 요청에서 대상이 자동 재포함된다.
