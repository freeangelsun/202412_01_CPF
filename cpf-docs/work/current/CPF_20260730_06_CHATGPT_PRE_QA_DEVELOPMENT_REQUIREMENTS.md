# CPF 20260730_06 ChatGPT 사전 QA 자체검수 개발 요건

## 1. 기준

- Repository: `https://github.com/freeangelsun/202412_01_CPF`
- Branch: `master`
- 검수 기준 SHA: `693cc77bde4c830b78ca1408dec7e34ef84cd11d`
- Commit Message: `20260730_06`
- 검수 목적: QA 추가 목록 수신 전에 실제 Source·Frontend·DB·Gate에서 선제 결함과 추가 개발 요건을 추출한다.
- 제외 범위: README와 일반 Guide 개선. 개발·검증 정본에 필요한 최소 문서만 관리한다.
- 주의: 이 문서는 완료 보고가 아니다. 실행하지 않은 Build·DB·Runtime·Browser 검증을 PASS로 기록하지 않는다.

## 2. 현재 판정

`20260730_06`은 55,331줄 규모의 변경으로 Gateway·Batch·ADM·Core API·DB Canonical을 동시에 변경했다. 기존 문서의 `신규 개발 잔여 없음`과 Review Only 지시는 실제 Source와 일치하지 않는다.

사전 등록 항목은 총 **48건**이다.

- Priority: P0 31건, P1 16건, P2 1건
- Status: Consumer 단절 1건, 미검증 5건, 미구현 3건, 부분 구현 32건, 실패 2건, 재확인 필요 5건

## 3. 최우선 P0 묶음

### 3.1 Gateway

1. Control API nonce를 JVM 로컬 저장에서 분산 원자 claim으로 변경한다.
2. Control signature에 canonical Body Hash와 key rotation을 포함한다.
3. Route timeout/retry 값을 실제 HTTP/ServiceCall 실행에 연결한다.
4. Streaming 응답 완료 후에만 ledger SUCCESS를 확정하고 partial delivery를 UNKNOWN/FAILED로 기록한다.
5. retry/failover 각 시도를 개별 attempt ledger로 남긴다.
6. `CpfPayloadProtectionPort` 제품 구현 또는 ADM 사전 차단을 제공한다.

### 3.2 Batch

1. Control Service 직접 projection과 Scheduler Outbox projection의 이중 Owner를 제거한다.
2. Publish 직전에 전체 Definition·Capability·Dependency를 재검증한다.
3. CALENDAR/BUSINESS_DAY를 Cron으로 처리하는 구현을 전용 Calendar 계산으로 교체한다.
4. `unknownResultPolicy`와 `compensationReference`를 실제 복구 Runtime에 연결한다.
5. Spring Batch terminal 상태·lease heartbeat·worker crash recovery를 완성한다.
6. Protocol Adapter의 직접 URI 호출을 CPF 외부연계 보안 경계로 이관한다.
7. Map payload의 비정상 JSON 직렬화와 비멱등 retry를 수정한다.

### 3.3 ADM/UI

1. Secret/Path/File Reference의 interface-only 상태를 제거한다.
2. Log Policy에서 body identity fallback과 즉시 승인 override를 제거한다.
3. Log Export를 다중 인스턴스·민감정보·at-rest 안전 구조로 바꾼다.
4. Gateway UI에 Reference Picker, 승인·활성·차단·Retire, 권한, paging을 구현한다.
5. Batch APPROVAL 상태를 불변으로 처리하고 승인·Runtime·복구 drill-down을 연결한다.

### 3.4 DB/Gate/Test

1. 문자열 anchor 존재만 확인하는 False-green Gate를 행동 검증으로 교체한다.
2. Current Request를 최신 SHA와 실제 개발 잔여 목록으로 교체한다.
3. Oracle·PostgreSQL·MariaDB 실제 Lifecycle과 exact-SHA Evidence를 생성한다.
4. Gateway·Batch multi-instance/failure injection과 ADM Browser E2E를 수행한다.
5. Matrix의 완료 상태를 QA 병합 후 실제 구현과 Evidence 기준으로 다시 판정한다.

## 4. 상세 요건

| ID | 우선순위 | 영역 | 상태 | 요건 |
|---|---:|---|---|---|
| `PREQA-GW-CTRL-001` | P0 | Gateway Control Security | 부분 구현 | 다중 인스턴스 Replay 방지 |
| `PREQA-GW-CTRL-002` | P0 | Gateway Control Security | 부분 구현 | 제어 명령 Body 무결성 |
| `PREQA-GW-CTRL-003` | P1 | Gateway Control Security | 부분 구현 | Key rotation과 nonce 저장소 안전성 |
| `PREQA-GW-RUN-001` | P0 | Gateway Runtime | 부분 구현 | Route timeout/retry Consumer 연결 |
| `PREQA-GW-RUN-002` | P0 | Gateway Runtime | 부분 구현 | Streaming 완료 판정 시점 |
| `PREQA-GW-RUN-003` | P0 | Gateway Runtime | 부분 구현 | Retry/Failover 시도별 원장 |
| `PREQA-GW-RUN-004` | P1 | Gateway Runtime | 부분 구현 | 응답 byte/overflow 추적 |
| `PREQA-GW-LOG-001` | P0 | Gateway Logging | 미구현 | Payload 보호 기본 구현 또는 사전 차단 |
| `PREQA-GW-LOG-002` | P1 | Gateway Logging | 부분 구현 | UTF-8 request preview 절단 |
| `PREQA-GW-UI-001` | P1 | ADM Gateway UI | 부분 구현 | Registry Reference Picker 실제 적용 |
| `PREQA-GW-UI-002` | P0 | ADM Gateway UI | 부분 구현 | Binding 승인·활성·차단·Retire 운영 흐름 |
| `PREQA-GW-UI-003` | P1 | ADM Gateway UI | 부분 구현 | Paging·상세·Drift 복구 |
| `PREQA-GW-UI-004` | P0 | ADM Gateway UI | 재확인 필요 | 권한별 버튼 통제 |
| `PREQA-GW-UI-005` | P2 | ADM Gateway UI | 부분 구현 | SSE reconnect lifecycle |
| `PREQA-BAT-PUB-001` | P0 | Batch Ownership | 부분 구현 | Published projection 단일 Owner |
| `PREQA-BAT-PUB-002` | P0 | Batch Definition | 부분 구현 | Publish 시 전체 재검증 |
| `PREQA-BAT-SCH-001` | P0 | Batch Scheduler | 부분 구현 | CALENDAR/BUSINESS_DAY Trigger 의미 구현 |
| `PREQA-BAT-SCH-002` | P1 | Batch Scheduler | 부분 구현 | Projection Outbox 운영·DLQ |
| `PREQA-BAT-WRK-001` | P0 | Batch Worker | Consumer 단절 | unknownResultPolicy 실제 실행 |
| `PREQA-BAT-WRK-002` | P0 | Batch Worker | 미구현 | Compensation/Reconciliation Runtime |
| `PREQA-BAT-WRK-003` | P0 | Batch Worker | 부분 구현 | Spring Batch 비동기 완료·Lease heartbeat |
| `PREQA-BAT-WRK-004` | P0 | Batch Worker Security | 부분 구현 | Protocol Adapter SSRF·TLS·Credential 통제 |
| `PREQA-BAT-WRK-005` | P0 | Batch Worker | 부분 구현 | JSON payload 직렬화 |
| `PREQA-BAT-WRK-006` | P1 | Batch Worker | 부분 구현 | 외부 payload/response 상한 |
| `PREQA-BAT-WRK-007` | P0 | Batch Worker | 부분 구현 | 비멱등 SERVICE_CALL retry 통제 |
| `PREQA-BAT-WRK-008` | P1 | Batch Worker | 부분 구현 | Executor capability Publish 사전 검증 |
| `PREQA-BAT-UI-001` | P1 | ADM Batch UI | 부분 구현 | APPROVAL 상태 불변성 |
| `PREQA-BAT-UI-002` | P1 | ADM Batch UI | 재확인 필요 | 권한·승인 UX |
| `PREQA-BAT-UI-003` | P1 | ADM Batch UI | 재확인 필요 | Runtime·Recovery 운영 화면 연결 |
| `PREQA-ADM-PAR-001` | P0 | ADM Parameter Reference | 미구현 | Secret/Path/File Catalog 실제 Provider |
| `PREQA-ADM-PAR-002` | P1 | ADM Parameter Reference | 부분 구현 | Paging·선택값 Resolve |
| `PREQA-ADM-SEC-001` | P0 | ADM Security | 부분 구현 | Client body identity fallback 제거 |
| `PREQA-ADM-LOG-001` | P0 | ADM Log Policy | 부분 구현 | Override 실제 Maker-Checker 승인 |
| `PREQA-ADM-LOG-002` | P1 | ADM Log Policy | 부분 구현 | CAS·Capability 사전 검증 |
| `PREQA-ADM-EXPORT-001` | P0 | ADM Log Export | 부분 구현 | 다중 인스턴스 Artifact 저장 |
| `PREQA-ADM-EXPORT-002` | P0 | ADM Log Export Security | 부분 구현 | 자유 문자열 Payload 마스킹 |
| `PREQA-ADM-EXPORT-003` | P1 | ADM Log Export Security | 부분 구현 | At-rest·restart cleanup·감사 원자성 |
| `PREQA-GATE-001` | P0 | Verification Gate | 부분 구현 | Anchor 기반 False-green 제거 |
| `PREQA-GATE-002` | P0 | Work Context | 실패 | Current Request 최신 SHA/작업 내용 Drift |
| `PREQA-TEST-001` | P0 | Test Coverage | 부분 구현 | 대규모 변경 대비 행동 Test 부족 |
| `PREQA-DB-001` | P0 | DB Lifecycle | 미검증 | 3 Vendor 실제 Lifecycle |
| `PREQA-DB-002` | P1 | DB Lifecycle | 미검증 | Migration/Rollback 데이터 보존과 Drift |
| `PREQA-RUNTIME-001` | P0 | Gateway Runtime | 미검증 | Gateway multi-instance/failure injection |
| `PREQA-RUNTIME-002` | P0 | Batch Runtime | 미검증 | Definition→Approval→Publish→Scheduler→Worker→Recovery E2E |
| `PREQA-FRONT-001` | P0 | Browser E2E | 미검증 | ADM/BZA 권한·승인·SSE Browser 검증 |
| `PREQA-CORE-001` | P0 | API/SPI Ownership | 재확인 필요 | 신규 Port 기본 구현·Consumer 전수 매핑 |
| `PREQA-GENERATOR-001` | P1 | Generator | 재확인 필요 | Core 계약 변경 Generator 동기화 |
| `PREQA-MATRIX-001` | P0 | Matrix/Evidence | 실패 | 완료 상태 재판정 |


상세 Finding, Evidence Path, 완료 조건은 함께 제공한 CSV를 정본으로 사용한다.

## 5. QA 목록 병합 규칙

1. QA 항목이 동일 원인이라면 새 번호를 만들지 않고 `PREQA-*` 항목에 QA ID를 병기한다.
2. 화면·API·Service·DB·Runtime이 같은 기능 흐름이면 하나의 완료 단위로 묶는다.
3. Interface 추가만으로 완료하지 않는다. 기본 구현 또는 명시적 Optional 정책과 실제 Consumer가 있어야 한다.
4. Static PASS는 Runtime PASS로 승격하지 않는다.
5. Source 수정 시 SQL·Migration·Rollback·Frontend·Test·Generator 영향을 함께 확인한다.
6. QA 목록 수신 후 Priority는 사용자 영향, 보안, 데이터 정합성, 복구 불능 위험 순으로 재조정한다.

## 6. 완료 처리 금지

- `20260730_06` 변경 파일이나 Class 존재만으로 완료 처리
- 문자열 anchor가 있다는 이유로 Consumer 연결 완료 처리
- Gateway stream 전송 전 ledger SUCCESS 기록을 정상 처리
- Batch 결과 불명 정책을 저장만 하고 Runtime 미연결 상태로 완료 처리
- Secret/Path/File Provider가 항상 unavailable인데 Reference 기능 완료 처리
- 3개 DB 중 일부만 실행하고 DB Lifecycle 완료 처리
- Browser·multi-instance·failure injection 미실행 상태를 PASS 처리
- exact-SHA가 아닌 과거 Evidence로 현재 완료 처리

## 7. 다음 단계

QA 결함·추가 개발 요건 목록을 받으면 이 목록과 원인 기준으로 병합한다. 중복을 제거한 뒤 P0부터 Source·SQL·Frontend·Test·Runtime을 함께 보완하며, README와 일반 Guide 작업은 별도 산출물 세션에 맡긴다.
