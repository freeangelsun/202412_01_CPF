# Final Target Gap·Patch 결정

`PROPOSED_REQUIREMENT_ADDITIONS.csv`의 24건을 다음과 같이 분류했다.

- `PROMOTE_NEW_CANONICAL` 16건: Cache, Feature Flag, Session/BFF, Crypto Agility, Vulnerability Response, Threat Model, Performance/Soak, Webhook, AsyncAPI/Schema, Data Encryption, Data Quality, Time, Notification, Support Bundle, Supplier Due Diligence, Upgrade Assistant
- `MERGE_STRENGTHEN` 7건: Telemetry Lifecycle, Design System/I18N, OpenAPI Profile, CBOM, Dependency Policy, Config Schema, 기존 Canonical 강화
- `REVIEW_FOR_CANONICAL_SPLIT` 1건: Data Residency
- `DEFER_OUT_OF_SCOPE` 1건: AI Security — AI 제품 Capability 도입 전까지 해당 없음

신규 후보는 자동으로 최상위 정본을 변경하지 않는다. 중복·Owner·Consumer·Acceptance·Migration/Compatibility·Continuity를 검산하고 REQ-GAP 승인 뒤 반영한다.

즉시 필요한 정본 Patch는 `REQUIRED_CANONICAL_PATCHES.csv`에 분리했다. 현재 확정 결함은 Section 21의 Canonical 수 `162` 표기를 실제 Catalog 수 `169`로 정정하는 것이다.
