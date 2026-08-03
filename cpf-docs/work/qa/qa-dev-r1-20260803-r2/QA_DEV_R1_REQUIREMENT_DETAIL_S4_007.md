# QA Requirement Detail — CPF-SELF-DEV-S4-007

## 판정

- QA 결과: `미통과`
- 기준 SHA: `2903de14eb9cd6cfcccf8e4d2a3489ee2e4193ca`
- Requirement: 공통 Network Policy Consumer 및 Durable Audit Gate 강화
- QA 회차: `QA-DEV-R1`

## 실제 확인 파일

1. `cpf-core/src/main/java/com/cpf/core/api/security/network/CpfNetworkEndpointPolicy.java`
2. `cpf-starters/integration/http-client/src/main/java/com/cpf/core/common/http/CpfServiceEndpointRegistry.java`
3. `cpf-gateway/src/main/java/com/cpf/gateway/scg/CpfScgTargetResolver.java`
4. `cpf-batch/host-agent/src/main/java/com/cpf/batch/agent/internal/PinnedArtifactHttpTransport.java`
5. `cpf-tools/scripts/verify-cpf-network-policy-consumers.py`
6. `cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmAuditLogService.java`
7. `cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmAuditDeliveryService.java`
8. `cpf-tools/scripts/verify-cpf-audit-fail-closed.py`
9. Network/Audit Targeted Evidence JSON

## 확인된 구현

- 공통 Network Policy는 CIDR, Port, TLS, Special-use, IPv4/IPv6 검사를 제공한다.
- Gateway는 DNS Resolve 후 Address 검증과 Pinned Address를 Target에 전달한다.
- Batch Host Agent는 DNS/Pinned Address/TLS SNI/Hostname Verification을 구현한다.
- Audit는 Owner 실행 전 REQUIRES_NEW Reservation, UNKNOWN Recovery, FOR UPDATE Relay를 구현한다.

## 치명적 Network 결함

`CpfServiceEndpointRegistry`의 DNS Endpoint 경로는 다음 동작만 수행한다.

1. `validateEndpoint(baseUrl)`
2. Hostname URL 문자열 반환

실제 DNS Resolve, `validateResolvedAddresses`, Pinned Connection이 없다. 그런데 Gate는 이 Consumer에 `validateResolvedAddresses`를 요구하지 않고 전체 결과에 `dnsRebinding=true`를 기록한다.

## 영향

- HTTP Client Consumer에서 DNS Rebinding 또는 검증 후 주소 변경을 차단하지 못할 수 있다.
- Gateway/Host Agent와 동일 정책을 사용한다는 완료 주장이 성립하지 않는다.
- Gate가 실제 보안 속성보다 문자열 Token 존재를 PASS한다.

## Audit 미검증

- Source 계약은 확인됐지만 실제 MariaDB/PostgreSQL/Oracle 실행은 없다.
- 다중 인스턴스 Relay, Process Kill, Reservation 후 Owner Kill, Retry Exhaustion을 실행하지 않았다.
- 이는 외부 DB 환경 검증으로 분리하되 Network 구현 결함이 해결되기 전 Requirement는 통과할 수 없다.

## 재개발 요청

- HTTP Client Transport에서 연결 직전 DNS Resolve·Address 검증·Pinned Connection 구현
- Mixed Private/Public DNS, Rebinding, DNS 결과 변경, Proxy 경로 Negative Test
- Gate가 세 Consumer 모두 Resolve/Validate/Pin 계약을 확인하도록 보강
- Audit 3 Vendor + 2 Instance + Process Kill Runtime Test 추가

## 성공 기대 결과

- 모든 Outbound Consumer가 검증한 Address에 실제 연결
- DNS Rebinding Test 실패 0
- Audit Reservation/UNKNOWN/Retry/Multi-instance 결과가 DB Evidence로 남음
