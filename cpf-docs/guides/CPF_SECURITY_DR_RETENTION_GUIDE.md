# CPF 보안·재해복구·데이터 보존 가이드

## 1. 목적

이 문서는 인증, 권한, Secret, 인증서, 민감정보, 감사, Backup, Restore, 재해복구와 Retention의 공통 제품 기준을 정의한다.

## 2. 보안 기본값

- 인증되지 않은 요청 거부
- 최소 권한
- 외부 공개 기본 거부
- Secret Reference
- 민감정보 마스킹
- 입력/출력 검증
- 위험 조치 승인
- Audit
- 안전 차단

## 3. 인증

지원 구성:

- Session
- OAuth2/OIDC
- JWT
- mTLS
- API Key Reference
- Agent Certificate
- Service Account

인증 실패와 권한 실패를 구분한다.

## 4. Session

- Access Token 짧은 수명
- Refresh Rotation
- Reuse Detection
- Device Metadata
- Session 목록과 폐기
- Logout
- Password 변경 후 폐기
- 권한 변경 재평가

## 5. 권한

평가 축:

- User
- Role
- Permission
- Action
- Resource
- Environment
- Domain
- Organization
- Tenant
- Data Scope
- Time

Deny 우선순위를 명시한다.

## 6. Secret

계약:

```text
Secret Reference
→ Provider
→ 짧은 Scope Value
→ 사용
→ 메모리 정리
```

Provider:

- ENV
- Vault
- KMS
- HSM
- Cloud Secret Manager

## 7. Secret Rotation

1. 새 Version 생성
2. 대상 호환
3. 배포
4. Instance ACK
5. 연결시험
6. 구 Version 폐기
7. Audit

## 8. 인증서

관리 정보:

- Subject
- Issuer
- Serial
- Fingerprint
- Validity
- Key Usage
- SAN
- Algorithm
- Chain
- Revocation

Private Key는 API/Evidence에 노출하지 않는다.

## 9. Application Security

- SQL Injection
- XSS
- CSRF
- SSRF
- Path Traversal
- Upload
- Deserialization
- Open Redirect
- Header Injection
- Command Injection
- Dependency/CVE

입력 검증과 Output Encoding을 적용한다.

## 10. 파일 보안

- 크기
- 확장자
- MIME
- Magic Number
- 경로
- 악성 검사
- Quarantine
- Download 권한
- Checksum
- Retention

Scanner 미구성 또는 장애 시 안전 차단한다.

## 11. 민감정보

분류:

- PUBLIC
- INTERNAL
- CONFIDENTIAL
- RESTRICTED

분류에 따라 저장, Log, Download, Masking, Retention과 암호화를 적용한다.

## 12. Masking

- API
- UI
- Log
- Audit
- Trace
- Evidence
- Export
- Exception

원문 보기는 별도 권한·사유·감사와 제한 시간을 요구한다.

## 13. 감사

감사 필드:

- actor
- role
- target
- action
- reason
- before/after
- operationId
- approval
- result
- transactionId
- timestamp

감사 자체 실패 정책을 위험도에 따라 결정한다.

## 14. Hash Chain

- canonical payload
- previousHash
- recordHash
- chain head
- row lock
- tamper verify
- tail deletion detection

Hash Chain은 기밀성을 대신하지 않는다.

## 15. 데이터 보존

Retention 정책:

- target
- duration
- action
- cutoff
- legal hold
- archive
- purge
- dry run
- kill switch
- approval
- audit

## 16. Legal Hold

Legal Hold는 Purge보다 우선한다.

- Case ID
- 대상
- 기간
- 사유
- 승인
- 해제
- Audit

## 17. Archive

```text
대상 Preview
→ Archive 저장
→ Checksum/Count
→ 원본 삭제
→ 결과
```

중간 실패 시 정합성을 보장한다.

## 18. Purge

- 기본 비활성
- cutoff 필수
- 최대 건수
- Chunk
- Lock/부하
- Kill Switch
- 중단/재개
- Evidence

## 19. Backup

- 암호화
- 접근 통제
- SHA-256
- Manifest
- Offsite
- Immutable
- Retention
- 복구 Test

Password를 Command Line에 넣지 않는다.

## 20. Restore

1. 격리 대상
2. Manifest
3. Checksum
4. Vendor/DB
5. 복구
6. Schema
7. 데이터
8. Application Smoke
9. 거래 대사
10. Evidence

## 21. DR

DR 범위:

- DB
- Artifact
- Config
- Secret
- Certificate
- Message Offset
- File
- Registry
- Gateway
- Batch Checkpoint
- DNS/LB

## 22. RPO/RTO

고객 운영 정책으로 값을 정한다.

- RPO
- RTO
- Backup 주기
- 복구 순서
- 우선 서비스
- 데이터 대사
- 업무 재처리
- 훈련 주기

## 23. DR 훈련

- 시나리오
- 격리 환경
- 실제 Backup
- 복구
- 측정
- Failover
- 거래 확인
- 원복
- 개선

## 24. Break-glass

- 재인증
- 제한 Scope
- 자동 만료
- Alert
- 사후 승인
- 모든 활동 감사

## 25. Download

- 별도 Permission
- Reason
- Masked Default
- 최대 크기
- Watermark
- Checksum
- 만료
- Audit

## 26. Security Gate

- Secret Pattern
- 취약 URL
- TLS
- Dependency/CVE
- License
- External CDN
- Private Key
- Permission Seed
- Upload
- Raw Log

## 27. Test

- 인증
- 권한
- Deny
- Session Reuse
- Secret Masking
- Rotation
- Certificate 만료
- Scanner Down
- Audit Tamper
- Legal Hold
- Purge Kill Switch
- Backup/Restore
- DR

## 28. 체크리스트

- [ ] Secret 원문이 노출되지 않는다.
- [ ] 권한은 서버가 평가한다.
- [ ] 위험 조치에 승인과 감사가 있다.
- [ ] Scanner/Verifier 장애 시 안전 차단한다.
- [ ] Retention에 Legal Hold와 Kill Switch가 있다.
- [ ] Backup을 실제 Restore 검증한다.
- [ ] DR 훈련으로 RPO/RTO를 측정한다.
