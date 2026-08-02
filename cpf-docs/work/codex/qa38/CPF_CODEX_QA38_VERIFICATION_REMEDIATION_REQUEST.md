# CPF QA38 Codex 전수검수·보완 개발 요청서

기능 범위는 Final Request와 Final Matrix에 자체 포함돼 있다.

- Stage 순서 준수
- 앞 Stage 실패 시 뒤 고비용 Stage 중단
- Root Cause 수정 전 재실행 금지
- Source/SQL/Test/Config/Generator/Evidence까지 보완
- actual Consumer 없는 Starter 금지
- DB는 Vendor별 Empty 0
- Docker/Browser/Supply-chain은 후순위지만 필수
- History/Continuity를 매 Stage 갱신
- Source 변경 후 targeted+upper test, 사용자 Push 후 exact-SHA final plan
