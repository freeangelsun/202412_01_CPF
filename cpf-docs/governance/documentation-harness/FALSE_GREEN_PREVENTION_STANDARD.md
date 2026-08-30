# CPF Documentation False-Green Prevention Standard — Harness 2.15.4

1. 사용자 Finding은 자동/수동 PASS보다 우선한다.
2. 사용자 Finding 발생 시 이전 Acceptance와 Review Evidence를 자동 무효화한다.
3. Rule 문구만 보강하고 Validator/Fixture/Required Gate가 없으면 Harness 보강 완료가 아니다.
4. README Coverage는 Keyword 존재가 아니라 설명 깊이와 독자 행동 완결성으로 판정한다.
5. Visual 품질은 파일 존재·해상도·Contact Sheet로 대체하지 않는다.
6. Manual Evidence는 현재 파일 SHA와 일치해야 한다.
7. Source Identity와 CLI Surface는 실행 시 Source에서 읽으며 Validator 코드에 SHA/Command Set을 하드코딩하지 않는다.
8. Harness가 결함을 놓친 경우 산출물을 탓하지 않고 Harness 자체를 FAIL로 판정한 뒤 먼저 보강한다.
9. README Markdown/Keyword/설명량 PASS는 Rendered Brochure PASS를 대체하지 않는다. 900/1200/1440 현재 SHA 화면에서 첫 Viewport, Section Boundary, Text Wall, 핵심 Visual 역할을 별도 검수한다.
10. 동일 작성자가 만든 산출물이라도 Manual Review Evidence는 관찰 항목과 실제 Screenshot SHA를 기록하며, `PASS` 문자열만 채운 Evidence는 무효다.


## 2.15.4 Large-product README completeness reinforcement

- CPF처럼 기능 범위가 큰 제품의 Root README를 얇은 개요로 축약하지 않는다. 제품 대표 문서에서 실제 Source로 확인되는 주요 Capability Family를 충분히 설명한다.
- 최소 Coverage는 Web/Application/Common, Data/Transaction/Cache/Lock/Session, Integration/Protocol/Resilience, Messaging/Async/Schema, File/Object Storage/Archive/Tabular, Security/Crypto/Secret/Identity, Config/Feature Flag/Health/Observability, AI/Realtime/GraphQL, Batch Runtime, DB3/Generator/Operations를 포함한다.
- 기능명만 나열하면 충족으로 인정하지 않는다. 독자가 무엇을 선택하는지, 실패 또는 운영 경계가 무엇인지, 어떤 결과를 얻는지 설명 흐름 안에서 확인할 수 있어야 한다.
- 정보량을 늘릴 때 장문 Text Wall로 바꾸는 것도 FAIL이다. 900/1200/1440 Rendered Brochure Review에서 Figure, 짧은 설명, 의미 그룹, 충분한 Section Boundary가 유지되어야 한다.
- 사용자 Finding으로 “대표 README가 제품 규모에 비해 내용이 부족하다”가 발생하면 이전 Completeness/Visual PASS는 즉시 무효화하고 현재 README SHA로 전체 Gate를 재실행한다.

## 2.15.4 Render Evidence Integrity Hotfix

- README 900/1200/1440 Preview는 PNG Header/Width 존재만으로 PASS하지 않는다. **전체 Image Decode + CRC/Chunk 검증**을 수행하고 손상·truncated 파일은 즉시 FAIL한다.
- `README_PREVIEW.html`은 임시 `/mnt/data`, 사용자 홈, 절대 `file:///` 경로를 Base로 사용하지 않는다. Repository-relative 경로로 어디서든 재현 가능해야 한다.
- Product Visual 원본도 전체 Decode 검증을 수행한다. 파일 크기·Dimension·SHA가 있어도 전체 Decode 실패면 Visual Gate는 FAIL이다.
- Preview/Visual Integrity 실패는 `FALSE_GREEN_PREVENTION_PASS`와 `README_RENDERED_BROCHURE_REVIEW_PASS`를 재개방한다.

