# CPF 공식 문서 작업 상세 인수인계

## 1. 이번 세션의 기준

- Source 기준 ZIP: `CPF_FULL_SOURCE_FOR_NEXT_QA_20260818_112401(1).zip`
- Source ZIP SHA-256: `a62e1abfa134d3124f2ab6743821610fa225ed5cc3e8c21e201e7a20785a25f4`
- 문서 작성 해석: 사용자가 전달한 최신 개발정보/Steering/추가 개발요건은 **산출물에서는 최종 구현 완료된 Current Contract**로 간주한다.
- Git write: 수행하지 않음.
- 파일 삭제: 수행하지 않음.
- 공식 사용자 Publication Surface: README 포함 7종.
- 별도 설계 산출물: 5종.

## 2. 가장 중요한 영구 원칙

사용자가 개발 진행정보·QA 역할·추가 개발요건을 문서 작업에 제공하는 목적은 개발 상태표를 사용자 문서에 옮기기 위해서가 아니다.

**QA = Source Truth를 깊게 확인하여 개발자·운영자가 산출물만 보고 CPF를 이해하고 올바르게 사용할 수 있도록 만드는 과정**으로 해석한다.

따라서 다음을 항상 수행한다.

1. 실제 Public API/SPI/Annotation/Base Class/Method/Config/Command를 Source에서 확인한다.
2. Generated Domain의 실제 Consumer와 Golden Path를 확인한다.
3. Starter/Profile/Provider 선택 기준과 Public/Internal 경계를 설명한다.
4. 정상뿐 아니라 오류·UNKNOWN·Retry·Reconcile·Recovery를 사용자의 업무 흐름으로 설명한다.
5. ADM/BZA/Gateway/Batch에서 실제 운영자가 무엇을 조회·제어하는지 설명한다.
6. 새 Requirement를 문서 맨 아래나 License 아래에 append하지 않는다. 적정 Owner 장/하위절로 흡수한다.
7. 기존 문서의 유효한 상세 내용을 줄여서 currentize하지 않는다.
8. 공통 Class/API/Header가 바뀌면 README → Owner Guide → Specification → 설계 산출물 → 예제/표/도식 → Handover를 같은 Cycle에서 전수 대조한다.

## 3. 공식 문서 구조

### 사용자 공식 문서 7종

1. `README.md`
2. `cpf-docs/guides/02_프레임워크_개발자_가이드.docx/.pdf`
3. `cpf-docs/guides/03_배치_개발자_가이드.docx/.pdf`
4. `cpf-docs/guides/04_운영자_매뉴얼.docx/.pdf`
5. `cpf-docs/guides/05_배치_운영_가이드.docx/.pdf`
6. `cpf-docs/guides/06_Gateway_개발_사용_가이드.docx/.pdf`
7. `cpf-docs/guides/07_Specification_기술_명세.docx/.pdf`

### 별도 설계 산출물 5종

- `아키텍처설계서`
- `기술사양서`
- `기술표준서`
- `데이터베이스표준서`
- `산출물목록`

과거 19종 사용자 Guide 체계는 Current Publication Surface가 아니다. 필요한 내용은 위 Owner 문서에 흡수한다.

## 4. 최신 기능 정본

### Channel / Context

- 내부 Canonical Context: Transaction / Original / Current / Caller / Target / Target Operation 6개.
- 외부 Business Domain 직접 호출 필수 Wire Header: Current를 제외한 5개.
- `currentChannel`: Receiver Generated Domain의 canonical `systemCode` 값을 Framework가 그대로 자동 사용.
- SystemCode→Channel Mapping/Resolver 및 별도 Channel config 없음.
- Channel Identity: 1~16자.
- `targetChannel == currentChannel` Controller-before 검증.
- Channel Policy: `operationId + callerChannel`.
- Same JVM/Remote 동일 의미.
- transactionId의 기존 issuer metadata와 Original Channel은 별개.

### Optional Subject Tracking / ADM Customer Timeline

- Subject Identifier 값 Optional / Collector Pipeline Mandatory.
- Canonical Type: CUSTOMER_NO / CUSTOMER_ID / MEMBER_NO / LOGIN_ID.
- 기본 서비스 이용 이력은 ACTOR Role 중심.
- Principal/Session/Trusted Gateway/Optional Metadata/Generated Contract/Late Enrichment를 신뢰 정책에 따라 수집.
- Canonical 조회 관계: `Subject → transactionId → 기존 Log/Trace/Audit/Execution Timeline`.
- Late Enrichment 후에도 transaction 시작 시점부터 전체 Timeline 조회.
- Raw PII 반복 저장 대신 protected deterministic search key + key version + masked display.
- Same JVM/Remote/Async/Messaging/Recovery는 transactionId/executionId correlation으로 연결.
- 업무 Golden Path의 수동 Customer Tracking Setter와 일반 로그 평문 고객번호는 0.
- ADM Subject Search 자체도 RBAC/Masking/Audit/Retention 대상.

### Central Managed Server Registry

- exactly-one `Managed Server + Runtime Instance + Capability` 운영 Identity 체계.
- Server는 한 번 등록/발견하고 Dashboard/Gateway/Batch/Logging/Configuration/Health/Audit가 동일 Identity를 재사용.
- Server ≠ Application ≠ Runtime Instance.
- Agent/Gateway/File Logging 등은 별도 Server Master가 아니라 Runtime Capability.
- `systemCode` ≠ `managedServerId` ≠ `instanceId` ≠ `runtimeInstanceId`.
- Feature 화면은 Capability Filtered View/Selector이며 자체 Server CRUD를 만들지 않는다.
- Transaction/Subject Timeline의 `instanceId`에서 Central Server Detail로 Drill-down 가능.
- Runtime registration/heartbeat는 Framework Management Capability가 담당하며 업무 개발자가 직접 구현하지 않는다.

## 5. Public API / Class / Command 현행화 검수

최종 문서에서 참조하는 `Cpf*` 타입 54종을 Source 선언과 대조했고 **미존재 타입 0건**을 확인했다.

주요 현재 Golden Path 예:

- `@CpfRestController`
- `@CpfOnlineTransaction`
- `@CpfService`
- `@CpfTransactional`
- `@CpfRepository`
- `CpfBaseController`
- `CpfBaseService`
- `CpfBaseRepository`
- `CpfBaseDao`
- `@CpfClient`
- `@CpfRetry`
- `@CpfTimeLimiter`
- `@CpfPreAuthorize`
- `@CpfApprovalRequired`
- `@CpfAudit`
- `CpfContexts`
- `CpfDomainClient`
- `CpfResult`
- `CpfSqlSession`
- `CpfNamedParameterJdbcOperations`

다음 구 Surface는 최종 문서 잔존 0건:

- `X-Original-System-Code`
- `X-System-Code`
- `X-Caller-System-Code`
- `X-Target-System-Code`
- `@CpfController`
- `@CpfTx`
- `@CpfTimeout`
- `@CpfPermission`
- `CpfDataOperations`
- `CpfJdbcOperations`

현재 Source에서 확인한 `cpf-dev.ps1` 주요 명령:

- `run-local`
- `status`
- `verify-fast`

다음 세션에서도 API/Class/명령어가 변경되면 문서의 예제/표/Reference를 Source와 다시 전수 대조한다.

## 6. 페이지 편집 표준 — 절대 승계

**빈 공간 최소화는 문단 간격 제거가 아니다.**

- 본문은 1.15 수준의 자연스러운 행간과 식별 가능한 문단 간격을 유지한다.
- 중간 절은 앞뒤 약 1~2줄 수준의 시각적 여백으로 구분한다.
- 실제 대메뉴/대장급 장은 새 페이지 시작을 기본으로 하되 모든 Heading 1/2에 기계적으로 Page Break를 걸지 않는다.
- 표의 마지막 1행, bullet 1개, 제목 하나가 다음 페이지에 고립되지 않게 한다.
- 표/그림 때문에 빈 공간이 생기면 배치·행 분할·그림 크기·앞뒤 설명 흐름을 조정한다.
- 문단 간격 0, 지나친 글꼴 축소, 정보 압축으로 빈 공간을 해결하지 않는다.
- 표지/목차를 제외한 본문은 페이지 전체의 정보 밀도와 제목/본문/표/그림 비율을 기준으로 판단한다.
- `CPF_DOCUMENTATION_STANDARD.md`의 `페이지 구성·여백·밀도 편집 표준`을 다음 세션에서도 우선 적용한다.

## 7. 이번 최종 페이지 수

| 문서 | 페이지 |
|---|---:|
| 02 프레임워크 개발자 가이드 | 16 |
| 03 배치 개발자 가이드 | 14 |
| 04 운영자 매뉴얼 | 16 |
| 05 배치 운영 가이드 | 15 |
| 06 Gateway 개발·사용 가이드 | 12 |
| 07 Specification / 기술 명세 | 17 |
| 기술사양서 | 7 |
| 기술표준서 | 5 |
| 데이터베이스표준서 | 5 |
| 산출물목록 | 2 |
| 아키텍처설계서 | 7 |
| **합계** | **116** |

불필요한 강제 Page Break를 제거하여 반쪽/꼬리 페이지를 줄였으며, 문단 간격을 없애서 페이지 수를 줄인 것이 아니다.

## 8. 최종 문서 QA 결과

- DOCX: 11종.
- PDF: 11종.
- 총 페이지: 116.
- DOCX 접근성 Audit: **11/11, high=0 / medium=0 / low=0**.
- PDF Preflight: **11/11 PASS**, Openable, 비암호화.
- PDF TOC 내부 `GoTo` 링크: **11/11 존재, invalid destination 0**.
- README local link: **22개 / missing 0**.
- `Cpf*` Source Fact Check: **54종 / missing 0**.
- 구 System Header/API/Annotation 잔존: **0**.
- 전 페이지 PNG/Contact Sheet 시각검수 완료.
- 글자/표/도식 잘림·겹침·한글 glyph 오류: 0.
- 이유 없는 저밀도/표 한 행 꼬리/목차 항목 고립을 수정 후 재렌더 확인.
- Comment/Tracked Change를 최종 사용자 문서에 남기지 않는다.
- Render/contact sheet 같은 QA 중간 산출물은 전달 ZIP에 포함하지 않는다.

## 9. README License 규칙

`License` 아래에는 License 내용만 둔다.

- Community & Evaluation License.
- 학습/테스트/평가/비상업적 이용.
- 상용 또는 업무 목적 사용 및 별도 조건 협의가 필요하면 **GitHub Repository Owner `@freeangelsun`에게 문의**.
- Central Registry, Channel, Subject Tracking 등의 기능 설명을 License 아래에 두지 않는다.
- 공식 연락 이메일이 Source에서 확인되지 않은 상태에서는 임의 이메일을 만들지 않는다.

## 10. 다음 세션 시작 순서

1. 사용자가 새 Source ZIP/Steering을 주었는지 확인.
2. 새 정보는 산출물에서 최종 구현 완료 Target Contract로 해석.
3. Source의 Public API/Class/Annotation/Config/Command와 실제 Consumer 재Inventory.
4. 영향 Owner 문서 및 README/Specification/설계 산출물을 함께 currentize.
5. 문서 마지막에 Requirement 블록을 append하지 말고 적정 장에 재배치.
6. DOCX render → 전 페이지 시각 QA.
7. 접근성 Audit.
8. PDF 변환 → TOC 링크/Preflight.
9. README local links/API stale term check.
10. Manifest/Hash/Unicode ZIP Entry 검증.
11. Root-relative Overlay ZIP + 적용/검증/Git Status 명령 제공.

## 11. Git / 삭제 안전

- Commit/Push/Branch/Tag/Reset/Restore/Stash/Clean은 사용자 승인 없이 하지 않는다.
- 이번 문서 Overlay의 Delete Manifest는 비어 있다. 실제 삭제 수행 없음.
- 보호 경로(`cpf-docs/deliverables/**`, `cpf-docs/guides/**` 등) 삭제는 별도 승인 없이 수행하지 않는다.
- Overlay는 Repository Root 상대경로로 적용한다.

## 12. 다음 세션이 반복하면 안 되는 실패

- License 아래에 새 기능 설명 append.
- 새 Requirement를 문서 마지막에 통째로 붙이기.
- 빈 공간을 줄인다고 문단 사이 여백을 없애기.
- 모든 Heading 1/2를 새 페이지로 강제하기.
- 한 행/한 bullet만 남은 페이지를 “렌더 성공”으로 PASS.
- Source에 없는 과거 API/Class를 예제로 유지.
- TOC가 보인다는 이유만으로 실제 내부 링크를 검증하지 않기.
- 7종 공식 문서와 과거 19종 체계를 혼용하기.
- QA Finding을 사용자 문서에 개발 미완료 문구로 옮기기.
