# CPF Security Guide

## 1. Security Principles

- Zero Trust 경계
- 최소 권한
- Defense in Depth
- Secure by Default
- Secret Separation
- 개인정보 최소화
- 모든 중요 조치 Audit
- 운영과 개발 권한 분리

## 2. Authentication

지원 방식:

- OAuth2/OIDC
- JWT
- API Key
- mTLS
- 관리자 Session
- 내부 Workload Identity

Token은 issuer, audience, signature, expiry, nonce와 scope를 검증합니다.

## 3. Authorization

- API 권한
- 메뉴 권한
- 버튼 권한
- 데이터 범위
- 다운로드 권한
- 운영 조치 권한
- 승인 권한
- Masking 해제 권한

관리자 Role 하나에 모든 권한을 부여하지 않습니다.

## 4. Channel and Caller Trust

외부에서 전달된 Channel, User, SystemCode와 권한 Header를 그대로 신뢰하지 않습니다.

Gateway에서 인증된 정보만 내부 표준 Header로 생성합니다.

공유 호출 유형은 내부 신뢰 구간에서만 허용하고 외부 진입을 차단합니다.

## 5. Transport Security

- TLS 1.2 이상
- 강한 Cipher
- hostname verification
- mTLS
- 인증서 체인
- revocation 정책
- certificate rotation
- expiry alert

인증서 Private Key는 Repository와 Evidence에 저장하지 않습니다.

## 6. Secret Management

- Environment 또는 Secret Manager
- encryption at rest
- least privilege
- rotation
- version
- audit
- emergency revoke

YAML, Source, Test fixture, Log와 Screenshot에 원문 Secret을 포함하지 않습니다.

## 7. Personal Data

### Classification

- Public
- Internal
- Confidential
- Personal
- Sensitive Personal
- Credential

### Controls

- 수집 최소화
- field-level masking
- encryption
- 접근 권한
- 조회 Audit
- 다운로드 승인
- watermark
- retention
- deletion
- non-production anonymization

## 8. Logging Security

금지:

- Password
- access token
- refresh token
- private key
- full account number
- resident registration number
- card data
- unmasked personal data

Masking 전 원문을 다른 Logger에서 출력하지 않는지 검증합니다.

## 9. Admin Security

- MFA
- session timeout
- concurrent session policy
- IP/network restriction
- dual control
- re-authentication
- high-risk action approval
- immutable audit
- test API prod disable

## 10. File Security

- extension allowlist
- MIME validation
- magic number
- size limit
- antivirus
- archive bomb protection
- path traversal protection
- quarantine
- encryption
- retention
- secure delete

## 11. API Security

- input validation
- rate limit
- replay protection
- idempotency
- CORS allowlist
- CSRF
- SSRF prevention
- output encoding
- injection prevention
- error information minimization

## 12. Supply Chain

- dependency lock
- vulnerability scan
- SBOM
- license scan
- artifact checksum
- provenance
- signed release
- build isolation
- secret scan

## 13. Security Events

- authentication failure
- authorization denial
- privilege change
- masking bypass
- bulk download
- secret change
- certificate change
- admin control
- suspicious replay
- integrity failure

보안 이벤트는 일반 거래 로그와 분리하여 보존합니다.

## 14. Security Review Checklist

```text
[ ] Trust Boundary가 정의됐다.
[ ] 인증과 권한이 실제 API에 연결됐다.
[ ] 운영 화면과 API 권한이 일치한다.
[ ] 개인정보 분류와 마스킹이 적용됐다.
[ ] Secret이 외부화됐다.
[ ] mTLS와 인증서 생명주기가 정의됐다.
[ ] Audit가 변경 전후 값을 기록한다.
[ ] 실패·우회·권한상승 Test가 있다.
[ ] Dependency·SBOM·License·Secret Scan을 통과했다.
```
