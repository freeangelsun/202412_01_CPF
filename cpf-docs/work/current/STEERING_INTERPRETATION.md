# CPF Current Steering Interpretation / Design Decisions

> Current local-source ZIP SHA-256: `b5573c0ab545597563846d0fd31e8669e5b7fec6df73393fed70f17b5f0b6850` (8,440 files, `.git` absent)
> Revision: 2026-08-19

## 원칙

Steering의 목적은 보존하되 문구를 기계적으로 구현해 Architecture를 악화시키지 않는다. 최신 사용자 Steering이 과거 Canonical과 충돌하면 최신 Steering을 우선하고 Source/Owner/Security/Runtime/Evidence를 함께 currentize한다. 미실행 Runtime은 PASS로 기록하지 않는다.

## 이번 주요 설계 판단

1. **Canonical 거래 Identity는 System 기반 6종**: `X-Transaction-Id`, `X-Original-System-Code`, `X-System-Code`, `X-Caller-System-Code`, `X-Target-System-Code`, `X-Target-Operation-Id`. Channel은 별도 optional policy/context다.
2. **Receiver-owned System Header**: `X-System-Code`는 현재 Receiver가 trusted runtime metadata로 확정한다. 외부 Channel이 이를 신뢰값으로 생성하지 않는다.
3. **Header는 trust source가 아님**: Caller/Target/Operation은 trusted registry/security/runtime metadata와 검증한다. Canonical protected field는 일반 custom Header API로 변경할 수 없다.
4. **`cpf-biz-admin`은 Optional Prebuilt Business Administration Domain**: Generator 대상은 아니지만 generated business domain과 동일한 Public Starter/API, transaction/context, domain invocation, security, audit/trace, DB3/Test 계약을 사용한다.
5. **BZA data ownership 제한**: BZA approval state, BZA 업무권한, Backoffice 설정 등 BZA-owned data만 소유한다. Member/Customer/Account 원장은 해당 Business Domain이 exactly-one Owner다.
6. **외부 BZA Channel은 DB-less Pure Spring Boot**: `cpf-biz-channel`은 CPF BOM/Starter/Java/Internal dependency 0, HTTP/HTTPS only다. Session을 이유로 업무/권한 원장 DB를 붙이지 않는다.
7. **BZA Frontend는 외부 Reference UI**: `cpf-biz-frontend`는 Channel만 호출하고 대표 4개 Reference 흐름을 제공한다. 과거 embedded `cpf-biz-admin/frontend` Full UI는 active target이 아니다.
8. **Direct HTTP는 보안 우회가 아님**: Gateway/Direct는 endpoint 차이일 뿐 동일 authN/authZ/channel policy/audit/canonical header 계약을 지킨다. 자동 Gateway→Direct fallback은 금지한다.
9. **선택형 공통 Optionality**: optional module/application/capability는 물리적으로 없어도 Root settings/build/test/publication/installer/verifier가 정상이어야 하며, 존재하면 aggregate regression에 참여한다. Optional DB migration/deploy asset도 선택 시에만 적용한다.
10. **`instanceId` 단일 의미**: WAS/runtime instance ID. 명시값 또는 실제 Runtime Hostname으로 1회 확정하며 `local`, Domain명, localhost synthetic fallback을 사용하지 않는다.
11. **operationId와 executionId 분리**: operationId는 안정적인 Handler/OpenAPI/Domain Client/ADM 계약, executionId는 실행 건 식별자다.
12. **Source 구조 자체가 품질**: 기능 동작뿐 아니라 Owner/패키지/네이밍/의존 방향/개발자 탐색성/운영·변경 비용을 검수한다. 파일 크기만으로 기계 분할하지 않고 feature-first + 필요한 역할 경계를 사용한다. ADM/BZA Frontend도 기능별 page/component/api/model/composable 구조를 따른다.
13. **Education 20+15는 구조까지 검증**: 숫자 충족이 아니라 feature-first role package, 실제 Consumer/Golden Path/Test, nested dummy/static-inner 제거를 검증한다.
14. **Public Distribution은 Default-Deny**: 빈 staging에 명시적 Public classification/allowlist만 생성하고 Private/Internal/Secret leakage를 0으로 검증한다. 모든 Gate와 clean consumer가 PASS한 경우에만 사용자 승인 `push` 단계에 도달한다.
15. **과거 Evidence 비승계**: 현재 local source는 `.git`이 없으므로 exact Git SHA를 invent하지 않는다. 현재 Source에서 직접 실행한 Gate만 PASS로 기록하고 Gradle/Node/DB3/Multi-WAS/Browser 등 미실행 Runtime은 미검증으로 둔다.
