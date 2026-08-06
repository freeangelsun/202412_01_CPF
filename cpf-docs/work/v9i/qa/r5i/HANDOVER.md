# QA R5I Handover

1. 최신 master 기준 검수 SHA는 `e7cc9ada86c871214a20862779f2433bc46fea1b`다.
2. 자체 QA 16건과 동료 QA R5 24건을 병합해 29건으로 확정했다.
3. FDEV-001~FDEV-025는 모두 미통과다.
4. P0 12건을 먼저 수정한다.
5. 개발GPT 자체검수 후 Codex 독립 검수·보완을 수행한다.
6. Java25/Gradle9.1, Node, DB3, Browser, Broker, multi-process Runtime을 exact result SHA에서 실행한다.
7. QA R6는 과거 Evidence를 승계하지 않고 exact result SHA에서 재검수한다.
8. 사용자 승인 없는 삭제·Git 쓰기는 금지한다.
