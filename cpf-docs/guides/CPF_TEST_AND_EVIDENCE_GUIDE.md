# CPF Test와 Evidence 가이드

## 1. 목적

CPF는 Test 코드 존재가 아니라 실행 결과와 Source Commit의 일치를 검증한다. 이 문서는 Test 계층, 환경, Fault, Evidence와 완료 판정 기준을 정의한다.

## 2. Test Pyramid

```text
Unit
→ Contract
→ Integration
→ Runtime
→ Multi-instance/Fault
→ Browser
→ Release/DR
```

각 계층은 다른 실패를 검출한다.

## 3. Unit Test

- Domain Policy
- Validation
- State Transition
- Retry Classification
- Masking
- Codec
- Mapper
- Utility
- Permission

## 4. Contract Test

- Public API
- Error
- Header
- Schema
- Local/Remote
- Message Version
- File/전문
- SPI Adapter

## 5. Integration Test

- DB
- Transaction
- Outbox/Inbox
- Migration
- Security
- Audit
- Cache
- Registry
- Controller

## 6. Runtime Test

실제 Process를 시작한다.

- Health
- API
- DB
- Messaging
- File
- Gateway
- Batch
- Frontend
- Log/Trace

## 7. Multi-instance

- Scheduler 2개
- Worker 다수
- Gateway 다수
- Policy Consumer 다수
- Audit Writer 다수
- Lease/Fencing
- Takeover
- Stale Result

## 8. Fault

- DB Down
- Broker Down
- Target Down
- Network Delay
- Timeout
- ACK Loss
- Disk Full
- Process Kill
- Partial Apply
- Scanner Down
- Signature 실패
- Clock Skew

## 9. Browser

- Login/Logout
- 401/403
- Search/Paging
- Detail
- Form
- 409
- 위험 Action
- Approval
- Audit
- Accessibility
- Console Error

## 10. DB Lifecycle

Vendor별:

```text
Fresh Install
→ Verify
→ Upgrade
→ Verify
→ Rollback
→ Verify
→ Reapply
→ Verify
```

## 11. Generator

- 두 임의 Domain
- Dry Run
- Apply
- Build
- DB Artifact
- Conflict
- User Modification
- Remove
- Recreate

## 12. Artifact

- Version
- BOM
- POM
- Hash
- Signature
- SBOM
- bootJar/War
- Offline Bundle
- Remote Publish

## 13. 정상·오류·경계

최소 세트:

- 정상
- Null/빈 값
- 최대/최소
- Duplicate
- Version 충돌
- 권한
- Timeout
- Retry
- Unknown
- Partial Failure
- Recovery

## 14. Test Data

- Product Seed와 분리
- 격리
- 자동 정리
- 개인정보 없음
- 반복 가능
- Clock 고정
- Random Seed 기록

## 15. Skip

Skip에는 사유와 조건이 있어야 한다.

- 환경 없음
- 외부 Dependency
- 운영 전용
- 장시간

최종 `RequireAll`에서는 Skip을 성공으로 인정하지 않는다.

## 16. Evidence 구조

```json
{
  "sourceCommit": "...",
  "command": "...",
  "environment": "local",
  "profile": "postgresql",
  "startedAt": "...",
  "finishedAt": "...",
  "exitCode": 0,
  "status": "PASS",
  "requirements": ["DB-INSTALL"],
  "artifacts": [],
  "sanitized": true
}
```

## 17. 필수 필드

- Evidence ID
- Source Commit
- Dirty 여부
- Tool Version
- Command
- Parameter
- Environment/Profile
- 시작/종료
- Exit Code
- Status
- Requirement
- Expected
- Actual
- Raw Artifact
- Sanitizing

## 18. Source Commit

문서 Commit과 실제 검증 Source Commit을 구분한다. Evidence는 실행한 Source Tree의 정확한 Commit과 Dirty Fingerprint를 기록한다.

## 19. Raw Artifact

- Log
- JUnit
- Query Result
- Browser Report
- Screenshot
- Video
- Trace
- Migration Plan
- Hash

Screenshot만으로 완료 처리하지 않는다.

## 20. 민감정보 제거

- Password
- Token
- Secret
- Private Key
- 개인정보
- 내부 Host 정책상 민감값
- DB Dump

원본은 제한 저장소에 두고 Evidence에는 안전한 Reference를 남긴다.

## 21. Requirement 추적

```text
Requirement
→ Source
→ Test
→ Execution
→ Evidence
```

구현에서 역방향으로 Owner와 Requirement도 확인한다.

## 22. 상태

제품 추적 상태:

- 완료
- 부분 구현
- 미구현
- 미검증
- 실패
- 재확인 필요

제품 README/Guide는 완성 제품 계약을 설명하고, 상태는 Work/Review/Evidence에서 관리한다.

## 23. False Green 방지

금지:

- Test 삭제
- Assertion 약화
- 오류를 Skip
- Source 자동 수정 Gate
- 과거 Evidence 승계
- 한 Vendor 결과를 3개로 복사
- 문자열 언급만으로 완료
- Dirty Tree 결과를 Clean Release로 기록

## 24. Runner

```powershell
pwsh -File .\cpf-tools\scripts\verify-full-product.ps1 `
  -WithDatabase `
  -WithGeneratorLifecycle `
  -WithBrowser `
  -RequireAll `
  -Profile local
```

## 25. 결과 보고

- PASS/FAIL 요약
- 실행하지 못한 항목
- Source 결함
- 환경 결함
- 재실행 명령
- Evidence 경로
- 민감정보 확인

## 26. 보존

Evidence Retention:

- Release
- Security
- Audit
- DB Migration
- DR
- 일반 CI

등급별 기간을 정한다.

## 27. 체크리스트

- [ ] Source Commit이 정확하다.
- [ ] Command와 환경이 있다.
- [ ] 정상·오류·경계 Test가 있다.
- [ ] 다중 인스턴스와 Fault를 검증한다.
- [ ] 3개 Vendor 결과를 분리한다.
- [ ] Raw Artifact가 있다.
- [ ] 민감정보를 제거했다.
- [ ] 과거 Evidence를 현재 성공으로 사용하지 않는다.
