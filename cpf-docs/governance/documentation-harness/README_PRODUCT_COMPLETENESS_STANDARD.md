# CPF README Product Completeness Standard — Harness 2.15.4

## 목적
Root README는 짧은 마케팅 카드나 기능 목차가 아니라 **CPF 전체 제품을 처음 보는 사람이 구조·기능·개발·운영 흐름까지 이해하는 대표 문서**다. 자동 검사에 몇 개 키워드가 존재한다는 이유만으로 PASS하지 않는다.

## Hard Gate
- 전체 visible 설명량과 주요 Section별 최소 설명 깊이를 `readme-product-completeness.json`으로 강제한다. 이는 상한이 아니라 **최소 충분성**이다.
- `Channel/Entry → Gateway 선택 구조 → Business Domain → Public Starter/Capability → cpf-core/common → Batch Runtime → ADM/Backoffice → DB Owner → Runtime Identity/Trace`가 하나의 전체 Architecture로 이해되어야 한다. Module 이름 목록이나 일부 Zone 그림만으로 대체할 수 없다.
- Architecture Visual은 README 초중반에 등장하고, 그림 바로 아래 설명이 호출·소유·선택 경계를 충분히 풀어야 한다.
- 현재 `CpfCli.java`에서 Public command를 실행 시 추출한다. README는 명령을 나열하는 데 그치지 않고 일반 개발자가 언제 어떤 순서로 사용하는지 설명한다.
- 장점은 별도 홍보 Heading을 만들지 않되, 구조·동작 설명에서 개발 편의, 운영 추적, 실패 복구, DB3 이식성, 보안·감사, 생성 일관성이 충분히 드러나야 한다. Keyword만 존재하면 FAIL이다.
- 전체 길이의 상한은 없다. 필요한 설명을 삭제해 짧게 만드는 것은 FAIL이며, 과밀하면 구조·여백·시각화를 개선한다.

## User Finding 우선
사용자가 README가 전체 프로젝트를 설명하지 못하거나 얇다고 판단하면 자동 PASS는 즉시 무효다. 같은 Finding을 이 Standard, Validator, Negative Fixture, Final Required Gate에 반영하기 전 다시 완료 처리하지 않는다.


## 2.15.4 Large-product README completeness reinforcement

- CPF처럼 기능 범위가 큰 제품의 Root README를 얇은 개요로 축약하지 않는다. 제품 대표 문서에서 실제 Source로 확인되는 주요 Capability Family를 충분히 설명한다.
- 최소 Coverage는 Web/Application/Common, Data/Transaction/Cache/Lock/Session, Integration/Protocol/Resilience, Messaging/Async/Schema, File/Object Storage/Archive/Tabular, Security/Crypto/Secret/Identity, Config/Feature Flag/Health/Observability, AI/Realtime/GraphQL, Batch Runtime, DB3/Generator/Operations를 포함한다.
- 기능명만 나열하면 충족으로 인정하지 않는다. 독자가 무엇을 선택하는지, 실패 또는 운영 경계가 무엇인지, 어떤 결과를 얻는지 설명 흐름 안에서 확인할 수 있어야 한다.
- 정보량을 늘릴 때 장문 Text Wall로 바꾸는 것도 FAIL이다. 900/1200/1440 Rendered Brochure Review에서 Figure, 짧은 설명, 의미 그룹, 충분한 Section Boundary가 유지되어야 한다.
- 사용자 Finding으로 “대표 README가 제품 규모에 비해 내용이 부족하다”가 발생하면 이전 Completeness/Visual PASS는 즉시 무효화하고 현재 README SHA로 전체 Gate를 재실행한다.
