# IR-S05-ADM-BZA — API·Frontend·Browser Consumer 검증

- Parent request: `DEVGPT-V9-S01`
- Integration owner: `DEVGPT-V9-S05`
- Baseline SHA: `fc207ac5560da59f352ee0c5f83199177f2987b4`
- Status: `미완료 / 재확인 필요`

## Required implementation and validation

1. AuthN/AuthZ, masking, audit, logging, trace 운영 API를 OpenAPI Generated Client와 실제 ADM/BZA 화면에 연결한다.
2. server-side permission/data scope를 UI 숨김과 별개로 강제한다.
3. 검색, paging, 상세, 상태, 오류, 위험조치 승인·사유·감사·결과 추적을 검증한다.
4. 401/403/404/409/429/500/503, deep link, keyboard, responsive, accessibility, browser regression을 실행한다.
5. 화면/네트워크 로그에 Secret·PII 원문이 남지 않는지 확인한다.
6. 명령, Exit Code, 실제 결과, screenshot/browser trace, exact SHA를 `impacted_ids.csv`의 각 ID에 연결한다.

S05 적용·Push 후 최신 `origin/master`에서 Backend/OpenAPI/Frontend 원 Consumer 회귀 Evidence가 필요하다.
