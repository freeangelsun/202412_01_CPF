# CPF Gateway 매뉴얼

## 문서 기준

| 항목 | 기준 |
|---|---|
| Repository | `https://github.com/freeangelsun/202412_01_CPF` |
| Branch | `master` |
| Source 기준 Commit | `61dcbbe7d81e44a4ba3534ecd0f91f7adfa4e9c5` (`04_09`) |
| 최상위 목표 정본 | `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md` |
| 문서 표준 정본 | `cpf-docs/specification/CPF_DOCUMENTATION_STANDARD.md` |
| 주 독자 | API 개발자·보안담당자·Gateway 운영자 |
| 문서 사용 결과 | 외부 API Route를 등록·검증·승인·게시하고 보안·ACK/NACK·Drift·Rollback을 운영한다. |
| 구현 상태 | `완료` — 사용자가 요청한 산출물 작성 전제 |
| 이 작성 세션의 Runtime 재실행 | 수행하지 않음 |
| 문서 현행화 범위 | Source·Catalog·Route·공식 문서 구조와 절차 정합성 |

> 이 문서는 구현 기능을 사용할 수 있는 상태로 설명한다. 이 작성 세션에서 Runtime을 다시 실행하지 않았다는 사실은 기능 절차를 축소하는 근거가 아니며, 고객 환경 배포 승인 시에는 해당 환경의 실행 기록을 별도로 보존한다.
## 1. 선택 기준

외부 Client와 내부 API 사이에 인증, Route, Header/Body 검증, Rate/Concurrency 제한, Attempt Ledger, 승인된 Publish와 Rollback이 필요할 때 `cpf-gateway`를 사용한다. 동일 JVM 내부 호출은 Gateway를 경유하지 않는다.

## 2. 설치

1. Gateway Artifact Hash·SBOM을 검증한다.
2. `secure-api`, Data JDBC, Integration Resilience와 필요한 Security Capability를 구성한다.
3. Attempt Ledger·Route Catalog·Runtime Control DB Migration을 적용한다.
4. Service Identity, JWT/HMAC Secret, Trust Store를 Repository 밖에서 공급한다.
5. Liveness·Readiness·DB·Target Probe를 확인한다.
6. ADM Gateway 메뉴와 Operation 연결을 확인한다.

## 3. Server Group과 Target

Server Group에는 Group ID, Service, Environment, Zone, Discovery/Static Target, Health, Weight, Drain, Version을 등록한다.

승인된 Scheme·Host·Port·CIDR만 허용한다. Loopback, Link-local, Metadata Endpoint와 사용자 입력 임의 URL을 차단한다.

## 4. Route 계약

필수 값:

- Route ID·Version·Status
- Host·Path·Method Predicate
- Header·Query 조건
- Rewrite 규칙
- Target Group
- Authentication·Permission
- Timeout·Retry·Circuit·Bulkhead
- Idempotency·Attempt Ledger
- Config Checksum·Expected Version
- Owner·Reason·Approval

상태 흐름은 Draft → Validated → Approved → Published → Applied/Partial/Failed → Retired다.

## 5. Predicate·Filter·Rewrite

Predicate는 Host, Path, Method, Header, Tenant, Client를 사용한다. Filter는 인증·권한·Header 표준화·Rate Limit·Audit를 수행한다. Rewrite는 외부/내부 Path와 Header Mapping을 명시한다.

Body를 변경하는 Filter는 HMAC Body Hash 검증 전후 순서를 문서화한다.

## 6. Authentication·Authorization

JWT는 Issuer, Audience, Signature, Expiry, Scope를 검증한다. 내부 Target에는 Service Identity와 최소 권한을 전달한다. 인증 실패와 권한 실패를 다른 Result Code로 반환한다.

## 7. HMAC·Nonce·Replay

Canonical Method/Path/Query/Header/Body Hash를 서명한다. Timestamp Window와 Nonce 저장소로 Replay를 차단한다. Clock Skew 허용 범위와 Nonce 보존 시간을 설정한다.

## 8. SSRF·TLS

- Scheme·Host·Port·CIDR Allowlist
- DNS Rebinding과 Redirect 재검증
- Loopback·Link-local·Metadata 차단
- TLS Chain·SAN·Expiry·Key Usage 검증
- Trust-all·Hostname 검증 해제 금지
- Certificate Rotate 시 동시 신뢰와 Rollback 준비

## 9. Timeout·Retry·Circuit·Bulkhead

전체 Timeout Budget을 DNS, Connect, TLS, Write, Read와 Owner 처리에 배분한다. Retry는 멱등 Operation에만 적용한다. 비멱등 요청은 Attempt Ledger와 Reconcile 없이 Retry하지 않는다.

Circuit·Bulkhead 변경 후 Error·Latency·UNKNOWN·Queue·Connection Pool을 확인한다.

## 10. Idempotency·Attempt Ledger

각 Attempt에 Operation ID, Idempotency Key, Route/Target, Start/End, Request Hash, Provider Tracking, Result와 Error를 기록한다.

Write 후 Response 유실은 UNKNOWN으로 저장한다. Target 거래 조회나 Owner Operation 조회로 확정하기 전 재호출하지 않는다.

## 11. 연결시험

1. Target DNS·Route·TLS를 확인한다.
2. Authentication·Service Identity를 확인한다.
3. 최소 권한 Test Request를 전송한다.
4. Timeout·Status·Response Schema를 검증한다.
5. Test Operation ID와 결과를 기록한다.
6. 취소 가능한 상태에서만 Cancel한다.
7. Config 변경 후 Revalidate한다.

## 12. Validate·Approve·Publish

1. Draft Route Syntax와 Target Allowlist를 Validate한다.
2. Permission·Secret·Certificate·Dependency를 확인한다.
3. Preview에서 영향 Route·Instance·Client와 Checksum을 확인한다.
4. Reason과 Approval을 기록한다.
5. Publish Version을 배포한다.
6. Instance별 ACK/NACK·Applied Checksum을 수집한다.
7. Observed Route와 Desired Version을 비교한다.

## 13. Partial Apply·LKG·Rollback

NACK Instance는 신규 Traffic에서 제외한다. 성공 Instance를 다시 적용하지 않는다. 실패 원인을 수정해 실패 Target만 재배포하거나 Last Known Good Version으로 Rollback한다.

Rollback 후 Desired·Applied·Observed와 Route Checksum이 일치해야 한다.

## 14. Scale-out·Drift

신규 Instance는 현재 승인 Version과 Checksum을 수신한 뒤 Readiness를 연다. Desired/Applied/Observed 차이를 Drift로 표시한다. Drift가 있는 Instance를 성공으로 집계하지 않는다.

## 15. ADM 운영 메뉴

- `/gateway-dashboard`: Snapshot·Event·Stream
- `/gateway-servers`, `/gateway-groups`: Server Group·Member
- `/gateway-routes`: Binding·Route 상태
- `/gateway-security`: 인증·보안·제한
- `/gateway-health`: 연결시험과 재검증
- `/gateway-transactions`: Attempt·Trace
- `/gateway-log-policies`: 로그 정책
- `/gateway-apply-status`: ACK/NACK·적용 이력

## 16. 장애 Runbook

| 장애 | 즉시 조치 | 정상화 |
|---|---|---|
| Target Down | Circuit Open·Fail-fast·Incident | Probe 성공 후 점진 Traffic |
| Response Loss | UNKNOWN 보존 | Target 거래 조회·Reconcile |
| Certificate 만료 | 신규 Traffic 차단 | Rotate·Handshake·구 Version 제거 |
| Partial Apply | NACK Instance 격리 | 재배포 또는 LKG |
| Retry 폭주 | Retry 제한·Bulkhead | Dependency 복구·Queue/DB 확인 |
| Route Drift | Readiness 제한 | 승인 Version 재적용 |
| SSRF 탐지 | Route 차단·Incident | Allowlist·DNS·Redirect 검토 |

## 17. Backup·Upgrade

Route Catalog, Server Group, Secret/Certificate Reference, Attempt Ledger, Config Version과 Artifact를 Manifest로 보존한다. Upgrade 시 기존 Route·Filter·OpenAPI·DB 호환을 확인하고 Canary 후 확장한다.

## 18. EDU

### Route Publish

Draft 생성 → Validation 오류 수정 → 연결시험 → Approval → Publish → Instance ACK → 거래 Test → Audit 확인.

### SSRF Negative

Loopback, Metadata IP, Allowlist 밖 Host, Redirect Target을 입력해 Validation이 거부하는지 확인한다.

### Response Loss

Toxiproxy로 응답을 차단해 Attempt가 UNKNOWN으로 남는지 확인하고 Target 거래 조회 후 Reconcile한다.
