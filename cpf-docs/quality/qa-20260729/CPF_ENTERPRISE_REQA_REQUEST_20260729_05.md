# CPF Enterprise 재 QA·보완 개발 통합 요청서 — 20260729_05
## 1. 기준과 목적
- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- 최신 확인 SHA: `b8941577b99535ff3e64a4fad99b74bafa544227` (`20260728_07`)
- 최상위 정본: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
- 사용자 승인 전 Commit·Push·Branch·Tag·Release 금지

첨부된 20260728_04 패키지의 기존 요구사항과 시나리오를 폐기하지 않고 최신 master에서 재판정한다. 본 요청은 앞 세션 패키지와 이후 확정된 Redis·UI·Tree·버튼 권한·Excel·Runtime 결과·Package·Generator·Hygiene 요구를 병합한 재 QA 요청이다.

## 2. 수량
- 기존 Delta 요구사항: **659개**
- 신규 상세 요구사항: **157개**
- 병합 Delta 요구사항: **816개**
- 기존 실행 시나리오: **240개**
- 신규 실행 시나리오: **59개**
- 병합 실행 시나리오: **299개**
- 기존 원본 1,214 요구사항/201 시나리오도 별도로 폐기하지 않고 병합 추적한다. 최종 숫자는 Root Cause Dedup 후 Manifest에 기록한다.

## 3. 판정 원칙

- Source에서 직접 확인한 결함, 기존 문서가 `미검증`으로 선언한 항목, 새 목표 요구사항을 혼합하지 않는다.
- 허용 상태는 `완료 / 부분 구현 / 미구현 / 미검증 / 실패 / 재확인 필요`만 사용한다.
- Class, Interface, Table, Route, Button, Swagger, 설정 문자열, Mock, 일부 Unit Test, 과거 Evidence만으로 완료 처리하지 않는다.
- 완료는 `Requirement → Owner → Public API/SPI → Consumer → DB/Config → Runtime → ADM/BZA → 권한/승인/감사 → 오류/부분실패 → 다중 Instance → 재기동/복구 → Test → Guide → Evidence → Legacy 제거`가 최신 SHA에서 연결돼야 한다.
- 미설치 외부 제품은 계약·기본 Adapter·Simulator·Fault Test까지 구현하되 실제 제품 연결은 `미검증`으로 분리한다.
- 신규 구조를 추가하고 기존 Consumer·Legacy를 남기면 `부분 구현`이다.

## 4. 신규 Work Package

### WP16 — Redis·분산 Cache (26개)

| ID | 상태 | 심각도 | 제목 |
|---|---|---|---|
| `CPF-QA-D04-REDIS-001` | 재확인 필요 | P0 | Provider-neutral Cache API·SPI |
| `CPF-QA-D04-REDIS-002` | 미구현 | P0 | Redis Adapter Ownership |
| `CPF-QA-D04-REDIS-003` | 미구현 | P0 | Redis 의존성·AutoConfiguration |
| `CPF-QA-D04-REDIS-004` | 미구현 | P0 | Standalone·Sentinel·Cluster 정책 |
| `CPF-QA-D04-REDIS-005` | 미구현 | P0 | TLS·ACL·Secret Reference |
| `CPF-QA-D04-REDIS-006` | 재확인 필요 | P0 | Key Namespace 표준 |
| `CPF-QA-D04-REDIS-007` | 재확인 필요 | P0 | Serialization·Schema Version |
| `CPF-QA-D04-REDIS-008` | 재확인 필요 | P0 | TTL·Jitter·Negative Cache |
| `CPF-QA-D04-REDIS-009` | 재확인 필요 | P0 | Entry Size·Eviction·Memory Guard |
| `CPF-QA-D04-REDIS-010` | 재확인 필요 | P0 | Cache-aside 일관성 |
| `CPF-QA-D04-REDIS-011` | 재확인 필요 | P0 | Stampede·Single-flight |
| `CPF-QA-D04-REDIS-012` | 재확인 필요 | P0 | Distributed Lock·Fencing |
| `CPF-QA-D04-REDIS-013` | 재확인 필요 | P0 | Invalidation Event 계약 |
| `CPF-QA-D04-REDIS-014` | 미구현 | P0 | Durable Invalidation Source |
| `CPF-QA-D04-REDIS-015` | 미구현 | P0 | Fast Invalidation Channel |
| `CPF-QA-D04-REDIS-016` | 재확인 필요 | P0 | Consumer Identity |
| `CPF-QA-D04-REDIS-017` | 재확인 필요 | P0 | Checkpoint CAS |
| `CPF-QA-D04-REDIS-018` | 재확인 필요 | P0 | Backlog Paging·Backpressure |
| `CPF-QA-D04-REDIS-019` | 재확인 필요 | P0 | Retry·Poison·DLQ |
| `CPF-QA-D04-REDIS-020` | 재확인 필요 | P0 | Offline Reconcile |
| `CPF-QA-D04-REDIS-021` | 재확인 필요 | P0 | Desired·Actual·Drift |
| `CPF-QA-D04-REDIS-022` | 재확인 필요 | P0 | Fail-open·Fail-closed 정책 |
| `CPF-QA-D04-REDIS-023` | 미구현 | P0 | ADM Cache 운영 UI |
| `CPF-QA-D04-REDIS-024` | 미구현 | P0 | Cache 위험 조치 |
| `CPF-QA-D04-REDIS-025` | 재확인 필요 | P1 | Health·Metrics·Alert |
| `CPF-QA-D04-REDIS-026` | 미구현 | P1 | Redis Testkit·EDU |

### WP17 — ADM/BZA UI·Menu·Tree (24개)

| ID | 상태 | 심각도 | 제목 |
|---|---|---|---|
| `CPF-QA-D04-UIX-001` | 재확인 필요 | P0 | ADM/BZA 기능 완전성 Matrix |
| `CPF-QA-D04-UIX-002` | 재확인 필요 | P0 | UI Pattern 적정성 |
| `CPF-QA-D04-UIX-003` | 부분 구현 | P0 | ADM Runtime 계층 Tree |
| `CPF-QA-D04-UIX-004` | 부분 구현 | P0 | BZA 조직 재귀 Tree |
| `CPF-QA-D04-UIX-005` | 부분 구현 | P0 | BZA 메뉴·권한 Tree |
| `CPF-QA-D04-UIX-006` | 재확인 필요 | P0 | ADM Service Registry 운영 UI |
| `CPF-QA-D04-UIX-007` | 부분 구현 | P0 | ADM Batch Runtime Tree·Control |
| `CPF-QA-D04-UIX-008` | 부분 구현 | P0 | Raw JSON 기본 화면 제거 |
| `CPF-QA-D04-UIX-009` | 부분 구현 | P0 | 업무 흐름 연속성 |
| `CPF-QA-D04-UIX-010` | 부분 구현 | P0 | 부분 실패 UX |
| `CPF-QA-D04-UIX-011` | 재확인 필요 | P0 | 오류·민감정보 UX |
| `CPF-QA-D04-UIX-012` | 재확인 필요 | P0 | Server Paging·Filter·Sort |
| `CPF-QA-D04-UIX-013` | 재확인 필요 | P0 | Virtual Scroll·Lazy Loading |
| `CPF-QA-D04-UIX-014` | 재확인 필요 | P0 | Detail Drawer·History |
| `CPF-QA-D04-UIX-015` | 재확인 필요 | P0 | Approval UI |
| `CPF-QA-D04-UIX-016` | 부분 구현 | P0 | Desired·Actual Diff Viewer |
| `CPF-QA-D04-UIX-017` | 부분 구현 | P0 | Attempt Timeline |
| `CPF-QA-D04-UIX-018` | 재확인 필요 | P0 | Menu·Route·API parity Gate |
| `CPF-QA-D04-UIX-019` | 재확인 필요 | P1 | 검색·필터 상태 보존 |
| `CPF-QA-D04-UIX-020` | 재확인 필요 | P1 | 접근성 |
| `CPF-QA-D04-UIX-021` | 재확인 필요 | P1 | 반응형·저해상도 |
| `CPF-QA-D04-UIX-022` | 재확인 필요 | P1 | Design System·공통 Component |
| `CPF-QA-D04-UIX-023` | 재확인 필요 | P1 | 외부 CDN·Font·Script 금지 |
| `CPF-QA-D04-UIX-024` | 재확인 필요 | P1 | Browser E2E 권한 Matrix |

### WP18 — 버튼별 Action 권한 (15개)

| ID | 상태 | 심각도 | 제목 |
|---|---|---|---|
| `CPF-QA-D04-ACT-001` | 재확인 필요 | P0 | Button Action Permission Manifest |
| `CPF-QA-D04-ACT-002` | 재확인 필요 | P0 | Generic WRITE 권한 분해 |
| `CPF-QA-D04-ACT-003` | 재확인 필요 | P0 | 조회·상세 권한 분리 |
| `CPF-QA-D04-ACT-004` | 재확인 필요 | P0 | 생성·수정·삭제 권한 |
| `CPF-QA-D04-ACT-005` | 재확인 필요 | P0 | 운영 실행 권한 |
| `CPF-QA-D04-ACT-006` | 재확인 필요 | P0 | 복구 권한 |
| `CPF-QA-D04-ACT-007` | 재확인 필요 | P0 | 승인·반려 권한 |
| `CPF-QA-D04-ACT-008` | 재확인 필요 | P0 | Upload·Download 권한 |
| `CPF-QA-D04-ACT-009` | 재확인 필요 | P0 | 로그·Evidence 권한 |
| `CPF-QA-D04-ACT-010` | 재확인 필요 | P0 | Secret·Certificate 권한 |
| `CPF-QA-D04-ACT-011` | 재확인 필요 | P0 | Backend 403 필수 |
| `CPF-QA-D04-ACT-012` | 재확인 필요 | P0 | Deny 우선·상속 |
| `CPF-QA-D04-ACT-013` | 재확인 필요 | P0 | 권한 변경 전파 |
| `CPF-QA-D04-ACT-014` | 재확인 필요 | P0 | Data Scope·Tenant 격리 |
| `CPF-QA-D04-ACT-015` | 재확인 필요 | P0 | 권한 Audit·Simulation |

### WP19 — Excel·CSV Download (14개)

| ID | 상태 | 심각도 | 제목 |
|---|---|---|---|
| `CPF-QA-D04-XLDN-001` | 부분 구현 | P0 | 검색조건·선택·전체 Download |
| `CPF-QA-D04-XLDN-002` | 부분 구현 | P0 | Server Streaming |
| `CPF-QA-D04-XLDN-003` | 부분 구현 | P0 | 비동기 Download Job |
| `CPF-QA-D04-XLDN-004` | 부분 구현 | P0 | CSV·XLSX 정책 |
| `CPF-QA-D04-XLDN-005` | 재확인 필요 | P0 | Formula Injection 방지 |
| `CPF-QA-D04-XLDN-006` | 재확인 필요 | P0 | Masking·Data Scope |
| `CPF-QA-D04-XLDN-007` | 재확인 필요 | P0 | 민감 Download 승인 |
| `CPF-QA-D04-XLDN-008` | 재확인 필요 | P0 | Download File 보안 |
| `CPF-QA-D04-XLDN-009` | 재확인 필요 | P0 | Temporary File Lifecycle |
| `CPF-QA-D04-XLDN-010` | 재확인 필요 | P0 | 동시성·Quota |
| `CPF-QA-D04-XLDN-011` | 부분 구현 | P0 | Vendor-neutral Query |
| `CPF-QA-D04-XLDN-012` | 재확인 필요 | P0 | Download Audit |
| `CPF-QA-D04-XLDN-013` | 재확인 필요 | P0 | 오류 UX·재시도 |
| `CPF-QA-D04-XLDN-014` | 부분 구현 | P0 | ADM Download Center |

### WP20 — Excel·CSV Upload (16개)

| ID | 상태 | 심각도 | 제목 |
|---|---|---|---|
| `CPF-QA-D04-XLUP-001` | 미구현 | P0 | 업무용 Excel·CSV Upload Framework |
| `CPF-QA-D04-XLUP-002` | 미구현 | P0 | Template Registry·Version |
| `CPF-QA-D04-XLUP-003` | 재확인 필요 | P0 | 확장자·MIME·크기 |
| `CPF-QA-D04-XLUP-004` | 재확인 필요 | P0 | Macro·Formula·위험 콘텐츠 |
| `CPF-QA-D04-XLUP-005` | 재확인 필요 | P0 | Encoding·Locale |
| `CPF-QA-D04-XLUP-006` | 재확인 필요 | P0 | Schema·Header Validation |
| `CPF-QA-D04-XLUP-007` | 재확인 필요 | P0 | Reference·Code Integrity |
| `CPF-QA-D04-XLUP-008` | 재확인 필요 | P0 | 중복 정책 |
| `CPF-QA-D04-XLUP-009` | 미구현 | P0 | Dry-run·Preview |
| `CPF-QA-D04-XLUP-010` | 재확인 필요 | P0 | Atomicity 정책 |
| `CPF-QA-D04-XLUP-011` | 미구현 | P0 | 행별 Result·Error File |
| `CPF-QA-D04-XLUP-012` | 미구현 | P0 | 비동기 Upload Job |
| `CPF-QA-D04-XLUP-013` | 재확인 필요 | P0 | Approval·Permission |
| `CPF-QA-D04-XLUP-014` | 재확인 필요 | P0 | Retry·Reprocess·Rollback |
| `CPF-QA-D04-XLUP-015` | 재확인 필요 | P0 | 원본 파일 Retention |
| `CPF-QA-D04-XLUP-016` | 재확인 필요 | P0 | Upload Audit·Evidence |

### WP21 — Runtime 대상별 결과·복구 (18개)

| ID | 상태 | 심각도 | 제목 |
|---|---|---|---|
| `CPF-QA-D04-RTCRES-001` | 부분 구현 | P0 | 대상별 Runtime Result 계약 |
| `CPF-QA-D04-RTCRES-002` | 부분 구현 | P0 | Failure Stage 표준 |
| `CPF-QA-D04-RTCRES-003` | 부분 구현 | P0 | Error Code·Masked Reason |
| `CPF-QA-D04-RTCRES-004` | 재확인 필요 | P0 | Retryable 판정 |
| `CPF-QA-D04-RTCRES-005` | 부분 구현 | P0 | Attempt History 불변 보존 |
| `CPF-QA-D04-RTCRES-006` | 재확인 필요 | P0 | Target Snapshot 불변성 |
| `CPF-QA-D04-RTCRES-007` | 재확인 필요 | P0 | Approval 실효성 |
| `CPF-QA-D04-RTCRES-008` | 재확인 필요 | P0 | Break-glass 상한 |
| `CPF-QA-D04-RTCRES-009` | 재확인 필요 | P0 | CAS·Request Hash |
| `CPF-QA-D04-RTCRES-010` | 부분 구현 | P0 | Desired·Actual 저장 |
| `CPF-QA-D04-RTCRES-011` | 재확인 필요 | P0 | Offline 복귀 Reconcile |
| `CPF-QA-D04-RTCRES-012` | 재확인 필요 | P0 | Rollback·Forward Recovery |
| `CPF-QA-D04-RTCRES-013` | 재확인 필요 | P0 | Retry/Cancel/Rollback/Reconcile API |
| `CPF-QA-D04-RTCRES-014` | 재확인 필요 | P0 | Restart Required 상태 |
| `CPF-QA-D04-RTCRES-015` | 부분 구현 | P0 | Runtime Control UI |
| `CPF-QA-D04-RTCRES-016` | 부분 구현 | P0 | Audit Hash-chain |
| `CPF-QA-D04-RTCRES-017` | 재확인 필요 | P0 | Log·Trace 연계 |
| `CPF-QA-D04-RTCRES-018` | 재확인 필요 | P0 | Multi-instance·Partial Failure Runtime Test |

### WP22 — Package·Generated Domain·Generator (18개)

| ID | 상태 | 심각도 | 제목 |
|---|---|---|---|
| `CPF-QA-D04-PKG-001` | 재확인 필요 | P0 | 기존 CPF 표준 우선 |
| `CPF-QA-D04-PKG-002` | 재확인 필요 | P0 | Module Ownership Matrix |
| `CPF-QA-D04-PKG-003` | 재확인 필요 | P0 | 업무 기능별 Package 경계 |
| `CPF-QA-D04-PKG-004` | 재확인 필요 | P0 | Public API·SPI·Internal |
| `CPF-QA-D04-PKG-005` | 재확인 필요 | P0 | Generated Domain normalized parity |
| `CPF-QA-D04-PKG-006` | 재확인 필요 | P0 | Reference와 Production Feature 분리 |
| `CPF-QA-D04-PKG-007` | 재확인 필요 | P0 | Typed Contract |
| `CPF-QA-D04-PKG-008` | 재확인 필요 | P0 | Query·Command 책임 |
| `CPF-QA-D04-PKG-009` | 재확인 필요 | P0 | Facade 생성 기준 |
| `CPF-QA-D04-PKG-010` | 재확인 필요 | P0 | Domain Manifest 강화 |
| `CPF-QA-D04-PKG-011` | 재확인 필요 | P0 | Generator 관리영역 |
| `CPF-QA-D04-PKG-012` | 재확인 필요 | P0 | Generator-first 변경 |
| `CPF-QA-D04-PKG-013` | 재확인 필요 | P0 | Local·Remote 동일 Contract |
| `CPF-QA-D04-PKG-014` | 재확인 필요 | P0 | Feature 간 Repository 직접 참조 금지 |
| `CPF-QA-D04-PKG-015` | 재확인 필요 | P0 | common/util/helper/manager 오남용 |
| `CPF-QA-D04-PKG-016` | 재확인 필요 | P1 | Frontend Feature Package |
| `CPF-QA-D04-PKG-017` | 재확인 필요 | P1 | SQL·Test·Guide 구조 parity |
| `CPF-QA-D04-PKG-018` | 재확인 필요 | P1 | Architecture Decision 절차 |

### WP23 — Repository Hygiene·Garbage 제거 (14개)

| ID | 상태 | 심각도 | 제목 |
|---|---|---|---|
| `CPF-QA-D04-HYG-001` | 재확인 필요 | P0 | Semantic Consumer Graph |
| `CPF-QA-D04-HYG-002` | 재확인 필요 | P0 | Dead Class·Interface·SPI |
| `CPF-QA-D04-HYG-003` | 재확인 필요 | P0 | Legacy 이중구조 제거 |
| `CPF-QA-D04-HYG-004` | 재확인 필요 | P1 | 중복 구현 제거 |
| `CPF-QA-D04-HYG-005` | 재확인 필요 | P0 | Frontend 고아 제거 |
| `CPF-QA-D04-HYG-006` | 재확인 필요 | P0 | DB Garbage 제거 |
| `CPF-QA-D04-HYG-007` | 재확인 필요 | P0 | 문서·Evidence 정본화 |
| `CPF-QA-D04-HYG-008` | 재확인 필요 | P1 | Repository Root Allowlist |
| `CPF-QA-D04-HYG-009` | 재확인 필요 | P0 | Cleanup Script |
| `CPF-QA-D04-HYG-010` | 재확인 필요 | P1 | 민감정보 Cleanup |
| `CPF-QA-D04-HYG-011` | 재확인 필요 | P1 | Generated Artifact Drift |
| `CPF-QA-D04-HYG-012` | 재확인 필요 | P1 | 대형 File·God Class |
| `CPF-QA-D04-HYG-013` | 재확인 필요 | P0 | 삭제 안전 절차 |
| `CPF-QA-D04-HYG-014` | 재확인 필요 | P0 | Hygiene CI Gate |

### WP24 — Source-backed QA·Evidence (12개)

| ID | 상태 | 심각도 | 제목 |
|---|---|---|---|
| `CPF-QA-D04-QAE-001` | 재확인 필요 | P0 | Source 확인과 요구사항 분리 |
| `CPF-QA-D04-QAE-002` | 재확인 필요 | P0 | 최신 master exact SHA |
| `CPF-QA-D04-QAE-003` | 재확인 필요 | P0 | Requirement 전수 재판정 |
| `CPF-QA-D04-QAE-004` | 재확인 필요 | P0 | Scenario 전수 실행 |
| `CPF-QA-D04-QAE-005` | 재확인 필요 | P0 | Evidence 강제 Gate |
| `CPF-QA-D04-QAE-006` | 재확인 필요 | P0 | 실행 명령·환경 |
| `CPF-QA-D04-QAE-007` | 재확인 필요 | P0 | 원본 Output·Hash |
| `CPF-QA-D04-QAE-008` | 재확인 필요 | P0 | 미실행 성공 금지 |
| `CPF-QA-D04-QAE-009` | 재확인 필요 | P0 | CI Required Check |
| `CPF-QA-D04-QAE-010` | 재확인 필요 | P0 | Known Gaps 원장 |
| `CPF-QA-D04-QAE-011` | 재확인 필요 | P0 | 완료 보고 진실성 |
| `CPF-QA-D04-QAE-012` | 재확인 필요 | P0 | 사용자 승인 없는 Git 변경 금지 |

## 5. Work Package 공통 수행 방식

1. 최신 master와 정본을 확인한다.
2. Inventory의 각 ID에 실제 Source·Method·API·SQL·UI·Test 경로를 연결한다.
3. Source에서 확인되지 않은 항목은 추측하지 않고 `미검증` 또는 `재확인 필요`로 둔다.
4. 결함은 Root Cause와 Owner·Consumer 기준으로 병합하되 원본 ID 추적을 유지한다.
5. 수정 시 Source·SQL·Frontend·Test·Generator·Guide·Evidence를 하나의 Change Set으로 닫는다.
6. 정적 Gate는 후보 탐지용이다. Browser·DB·Multi-process·Fault Runtime 결과로 완료를 판정한다.
7. 실행하지 않은 검증을 성공으로 기록하지 않는다.

## 6. 우선순위

- P0-1: 최신 SHA·CI·Evidence·기존 전수 원장 재개방
- P0-2: Runtime Control 대상별 결과·승인 실효성·버튼별 Backend 권한
- P0-3: Redis/Cache durable consistency·다중 Instance·ADM 운영
- P0-4: ADM/BZA Tree·Raw JSON 제거·실제 운영 흐름
- P0-5: Excel Upload/Download 보안·대량·복구
- P0-6: Package/Generator typed contract·업무 feature·parity
- P0-7: Semantic Garbage 제거와 회귀

## 7. 현재 Source에서 직접 확인된 Known Gaps
세부 내용은 `CPF_SOURCE_VERIFIED_KNOWN_GAPS_20260729_05.csv`를 사용한다. 이 목록은 전체 Repository 전수 검수 완료를 의미하지 않으며, SourceConfirmed와 RuntimeRequired를 구분한다.
- `CPF-KG-001` **BZA 조직 Tree 2단계 렌더링** — `cpf-biz-admin/frontend/src/features/organizations/OrganizationsPage.vue`: root와 직속 child만 렌더링하며 grandchildren은 하위 수로만 표시한다.
- `CPF-KG-002` **BZA 메뉴 관리 평면 CRUD** — `cpf-biz-admin/frontend/src/features/menus/MenusPage.vue`: parentMenuCode 필드는 있으나 CrudTable 기반 평면 관리다.
- `CPF-KG-003` **권한 Simulation Raw JSON** — `cpf-biz-admin/frontend/src/features/permissions/PermissionsPage.vue`: simulation 결과를 JSON.stringify 기반 pre로 출력한다.
- `CPF-KG-004` **Runtime Control Raw JSON·개발자 입력 중심** — `cpf-admin/frontend/src/features/runtime-control/RuntimeControlPage.vue`: preview/audit/group/status가 pre로 출력되고 payload JSON을 직접 입력한다.
- `CPF-KG-005` **cpf-common Redis Provider 의존성 부재** — `cpf-common/build.gradle`: Spring Cache와 Caffeine 의존성은 있으나 Redis client/adapter 의존성이 확인되지 않는다.
- `CPF-KG-006` **Notification Action generic WRITE** — `cpf-admin/frontend/src/features/notifications/NotificationsPage.vue`: 규칙 저장·비활성·테스트 발송·retry·cancel이 동일 canWrite('NOTIFICATION')를 사용한다.
- `CPF-KG-007` **Generator Feature Package reference 고정** — `cpf-tools/scripts/new-cpf-domain.ps1`: FeaturePackage가 BasePackage.reference로 고정된다.
- `CPF-KG-008` **Generator QueryPort에 Mutation 혼재** — `cpf-tools/scripts/new-cpf-domain.ps1`: QueryPort가 search뿐 아니라 create/update/delete/rollback을 포함한다.
- `CPF-KG-009` **Generator Public 계약 Map 사용** — `cpf-tools/scripts/new-cpf-domain.ps1`: Controller/Facade/Service/Port/Remote/Repository 결과에 Map<String,Object>가 폭넓게 사용된다.
- `CPF-KG-010` **통합 Matrix Gate 후보 탐지 수준** — `cpf-tools/verification/run-full-qa-closure.ps1`: symbol 횟수·정규식·경로 pattern으로 matrix를 만들며 status를 다수 미검증/재확인으로 둔다.

## 8. 완료 금지 조건

- Redis가 Caffeine 또는 설정 문자열만 존재
- Pub/Sub만 있고 durable replay·offline reconcile 없음
- Tree가 루트+직속 하위 또는 평면 들여쓰기
- Raw JSON이 기본 운영 화면
- generic WRITE 하나로 위험 조치 허용
- Frontend Button 숨김만 있고 Backend 403 없음
- Download가 현재 page 또는 byte[] 전체 메모리 생성
- 업무용 Template/Dry-run/행별 Result/복구 Upload 없음
- Runtime 전체 집계만 있고 대상별 failure stage/result 없음
- approvalId 문자열 존재만 확인
- Generator가 `.reference`와 `Map<String,Object>`만 생성
- 새 Package와 Legacy Package가 공존
- Garbage 후보 CSV만 만들고 실제 Consumer 확인·삭제·회귀 없음
- 최신 exact SHA의 Java/DB/Browser/Multi-instance Evidence 없음

## 9. 필수 산출물

- `CPF_ENTERPRISE_REQA_REQUEST_20260729_05.md`
- `CPF_ENTERPRISE_REQA_INVENTORY_20260729_05.csv`
- `CPF_ENTERPRISE_REQA_SCENARIOS_20260729_05.csv`
- `CPF_ADM_REALTIME_CONTROL_MATRIX_20260729_05.csv`
- `CPF_SOURCE_VERIFIED_KNOWN_GAPS_20260729_05.csv`
- `CPF_ENTERPRISE_REQA_TRACKER_20260729_05.xlsx`
- `CPF_ENTERPRISE_REQA_MANIFEST_20260729_05.json`
- `CPF_CODEX_REQA_HANDOFF_20260729_05.md`

## 10. 종료 보고

기준 SHA, 발견/수정/잔여 상태 수, 변경 Source·SQL·Frontend·Generator·Test, 실행 명령, 실제 미실행 항목,
Evidence 경로·Hash, 보호한 성공 기능, 제거한 Legacy·Garbage, 남은 Release Blocker를 보고한다.
`완료` 외 상태가 하나라도 있으면 전체 완료 또는 GA라고 표현하지 않는다.
