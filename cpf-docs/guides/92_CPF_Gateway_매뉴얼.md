# CPF Gateway 매뉴얼


## 문서 기준과 판정

- Repository: `https://github.com/freeangelsun/202412_01_CPF`
- Branch: `master`
- Source 기준 Commit: `e134c1f275c306c0e9ab4a044d9140ac4b3ca620`
- 최상위 목표 정본: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
- 문서 표준 정본: `cpf-docs/specification/CPF_DOCUMENTATION_STANDARD.md`
- 사용자 지시 적용: 요구사항에 정의되고 Source에 연결된 기능은 사용 가능한 제품 기능으로 설명한다.
- 이 문서 작업에서 직접 수행한 Runtime·DB·Browser·다중 인스턴스 검증: `미검증`
- 문서와 Source의 경로·식별자 정합성 검토: `완료`

> Runtime 미검증은 기능 절차를 생략하는 이유가 아니다. 다만 실행 결과를 직접 확인하지 않은 항목은 배포 승인 시 해당 환경의 Evidence로 다시 확인한다.


## 1. 선택 기준

외부 Client와 내부 API 사이에 인증, Route, Header/Body 검증, 제한, Attempt Ledger, 운영 Publish/Rollback이 필요할 때 `cpf-gateway`를 사용한다. 내부 동일 JVM 호출을 Gateway로 우회시키지 않는다.

## 2. 설치

Gateway Artifact Hash/SBOM을 검증하고 `secure-api` 및 필요한 Integration/Security Capability를 구성한다. DB가 필요한 Attempt Ledger와 Runtime Control Migration을 적용한다.

## 3. Route 계약

Route ID, Predicate, Filter, Rewrite, Target, Timeout, Retry, Authentication, Permission, Version, Checksum을 정의한다. Route 변경은 Draft → Validate → Approve → Publish → ACK/NACK → Observe 순서다.

## 4. Predicate·Filter·Rewrite

- Predicate: Host, Path, Method, Header, Tenant, Client
- Filter: 인증, 권한, Header 표준화, Rate Limit, Audit
- Rewrite: 외부/내부 Path와 Header Mapping

사용자 입력으로 임의 Target URL을 만들지 않는다.

## 5. Discovery·Load Balancing

Service Registry의 승인된 Instance만 Target으로 사용한다. Health, Weight, Zone, Version, Drain 상태를 반영한다. Stale Instance와 비정상 Endpoint를 자동 선택하지 않는다.

## 6. Authentication·Authorization

JWT Issuer/Audience/Expiry/Signature를 확인하고 Service Identity를 내부 호출에 전달한다. Permission과 Data Scope를 Route/Resource 기준으로 검증한다. 인증 실패와 권한 실패를 구분한다.

## 7. HMAC·Body Hash·Nonce

Canonical Method/Path/Query/Header/Body Hash를 서명한다. Timestamp Window와 Nonce 저장소로 Replay를 차단한다. Body를 변경하는 Filter는 서명 검증 순서를 명확히 한다.

## 8. SSRF·TLS

Target Scheme/Host/Port/CIDR Allowlist를 적용하고 Loopback, Link-local, Metadata Endpoint를 차단한다. TLS 인증서 Chain/SAN/Expiry를 검증하며 Trust-all을 허용하지 않는다.

## 9. Timeout·Retry·Circuit·Bulkhead

전체 Timeout Budget을 DNS/Connect/TLS/Write/Read에 배분한다. Retry는 멱등 Operation에만 적용한다. Circuit과 Bulkhead 정책은 ADM `/resilience-policies`와 Audit를 통해 변경한다.

## 10. Idempotency·Attempt Ledger

각 Attempt에 Operation ID, Idempotency Key, Target, Start/End, Request Hash, Result, Error, Provider Tracking을 기록한다. Write 후 Response 유실은 UNKNOWN으로 저장하고 Reconcile 전 재호출하지 않는다.

## 11. Validation·Version·Checksum

Publish 전 Route Syntax, Target Allowlist, Permission, Secret Reference, Certificate, Dependency와 Config Checksum을 검증한다. Expected Version이 다르면 Conflict로 중단한다.

## 12. Publish·ACK/NACK

1. Draft를 Validate한다.
2. 영향 Preview와 Target Snapshot을 확인한다.
3. Reason과 Approval을 기록한다.
4. Publish Version과 Checksum을 배포한다.
5. Instance별 ACK/NACK를 수집한다.
6. Observed Route/Hash와 Desired를 비교한다.

## 13. Partial Apply·LKG·Rollback

일부 Instance가 NACK이면 신규 Traffic을 해당 Instance에서 제외한다. 실패 원인을 수정해 재배포하거나 Last Known Good(LKG) Version으로 Rollback한다. 성공 Instance를 중복 적용하지 않는다.

## 14. Scale-out·Drift·Reconciliation

신규 Instance는 현재 승인 Version과 Checksum을 수신한 뒤 Readiness를 연다. Desired/Applied/Observed 차이를 Drift로 표시하고 자동 성공 처리하지 않는다.

## 15. Probe·Health

Liveness, Readiness, Target Dependency, Route Catalog Version, Certificate, Attempt Ledger 상태를 분리한다. ADM `/topology`, `/runtimeControl`, `/incidents`에서 확인한다.

## 16. 장애 Runbook

- Target Down: Circuit Open, Fail-fast, Incident, 복구 후 Probe
- Response Loss: UNKNOWN, Target 거래 조회, Reconcile
- 인증서 만료: 신규 Traffic 차단, 인증서 교체, Handshake 검증
- Route Partial Apply: 실패 Instance 격리, LKG/Rollback
- Retry 폭주: Retry 비활성/Backoff, Bulkhead, Queue·DB 부하 확인

## 17. EDU

### Route Publish

Draft Route를 생성하고 Validation 오류, Approval, Publish, Instance ACK를 확인한다.

### SSRF Negative

Loopback/Metadata/CIDR 밖 Target을 입력해 Validation이 거부하는지 확인한다.

### Response Loss

Toxiproxy로 응답을 차단해 Attempt가 UNKNOWN으로 남고 Reconcile 후 확정되는지 확인한다.
