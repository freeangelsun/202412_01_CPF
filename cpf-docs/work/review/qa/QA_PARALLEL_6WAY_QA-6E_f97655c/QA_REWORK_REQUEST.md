# QA-6E 재개발·재검수 요청

- 기준 SHA: `f97655c1299936a1101bc3ec10239265ec3b502e`
- 미통과 Requirement: **2,524**
- 환경차이 미검증 Requirement: **1,509**
- 상세: `QA_REWORK_DETAIL_QA-6E.csv`

미통과 행은 Owner·Consumer·호출 경로·오류·부분실패·UNKNOWN·멱등성·CAS·감사·DB·Frontend·Test를 함께 보완한다. 환경차이 행은 실패로 변환하지 말고 Java25, Oracle/PostgreSQL/MariaDB, Broker, multi-instance/process-kill, Chromium/Firefox/WebKit 환경의 exit code·log·artifact hash를 제출한다.

QA 직접보완 Source/Test는 개발GPT·Codex가 독립 검토하고 QA가 새 exact SHA에서 재검수한다.
