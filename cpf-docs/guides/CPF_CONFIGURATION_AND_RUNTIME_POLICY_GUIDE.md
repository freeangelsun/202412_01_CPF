# CPF 설정과 Runtime 정책 배포 가이드

## 1. 목적

CPF 설정은 파일의 Key/Value를 읽는 기능에 그치지 않는다. 안전한 기본값, 환경별 설정, 운영 변경, Version, 승인, 배포, ACK, 구성 불일치와 Rollback을 하나의 생명주기로 관리한다.

## 2. 설정 우선순위

```text
CPF 안전 기본값
→ 고객 설정
→ 환경 Profile
→ 운영 Override
→ 호출별 허용 Override
```

낮은 우선순위의 금지 정책을 높은 우선순위가 임의로 완화하지 못하도록 허용 범위를 정의한다.

## 3. 설정 분류

- Static Startup
- Dynamic Runtime
- Secret Reference
- Feature Flag
- Routing
- Rate/Timeout
- Log Policy
- Security Policy
- Batch Policy
- Retention
- Tenant

## 4. Metadata

각 Key는 다음 Metadata를 가진다.

- key
- type
- default
- required
- min/max
- allowed values
- sensitive
- dynamic
- restartRequired
- scope
- description
- owner
- compatibility
- deprecatedSince

## 5. Scope

- Global
- Environment
- Cell
- Service
- Instance
- Domain
- Tenant
- Route
- Job

Scope 충돌 우선순위를 명확히 한다.

## 6. Secret

Secret 성격 설정은 원문 값을 저장하지 않는다.

```yaml
client-secret-ref: vault://payment/client-secret
```

조회 API는 Reference와 Metadata만 반환한다.

## 7. Runtime 정책 원장

정책 상태:

```text
DRAFT
→ VALIDATED
→ APPROVED
→ PUBLISHED
→ RETIRED
```

정책은 Version과 Checksum을 가진다.

## 8. Metadata Codec

정책 Payload는 Versioned JSON 또는 Typed DTO를 사용한다.

지원:

- 다중 Metadata
- Unicode
- 줄바꿈
- 빈 값
- Escape
- 중첩 객체
- 배열
- Schema Version

구분자 Split 문자열로 저장하지 않는다.

## 9. Publish와 Event

```text
정책 원장 변경
+ 배포 Event
→ Commit
→ Consumer Claim
→ Apply
→ ACK
```

원장과 Durable Event의 원자성을 보장한다.

## 10. Consumer

다중 Consumer 안전성:

- Claim
- Lease
- Fencing
- expectedVersion
- checksum
- retry
- poison
- stale ACK 거부

## 11. Row Mapping

DB Column Label 대소문자에 의존하지 않는 명시적 Mapping을 사용한다. Null/빈 값 의미를 Vendor 간 통일한다.

## 12. Apply

Apply 단계:

1. Event 검증
2. 대상 Scope 확인
3. Version 비교
4. Payload Schema
5. Secret Reference 존재
6. Preview
7. Atomic Swap
8. Health
9. ACK

## 13. Partial Failure

상태:

- PENDING
- APPLYING
- APPLIED
- FAILED
- IGNORED
- STALE
- POISON

각 대상 Instance 결과를 저장한다.

## 14. Retry

Retryable:

- 일시 Network
- Lock
- 대상 Startup 중
- 일시 Store 장애

Non-retryable:

- Schema 오류
- 지원하지 않는 Version
- 권한
- Checksum
- 알 수 없는 Key
- 금지 Scope

## 15. Drift

Expected 정책과 Runtime 실제 Snapshot을 비교한다.

- Version
- Checksum
- Scope
- Effective Value
- Source Layer
- AppliedAt

## 16. Reconcile

- Drift 조회
- 대상 선택
- 원인
- 재적용
- Runtime Restart 필요 여부
- 결과
- 감사

## 17. Rollback

과거 검증 Version으로 되돌린다.

- 호환성
- Secret Reference
- 적용 순서
- Health Gate
- Partial Rollback
- Audit

## 18. Feature Flag

- 기본값
- 대상
- 비율
- 조건
- 시작/종료
- Kill Switch
- Rollback
- Metrics

업무 원장 의미를 Feature Flag만으로 바꾸지 않는다.

## 19. Log Policy

- Logger
- Level
- Scope
- 만료
- Sampling
- Masking
- Trace Boost
- 최대 기간

동적 DEBUG는 자동 만료된다.

## 20. 권한과 승인

고위험 정책:

- 외부 공개
- 인증 완화
- Rate 상한 완화
- Secret 변경
- Retention Purge
- Log 민감도
- Batch Retry
- Download

는 별도 Permission과 승인 정책을 적용한다.

## 21. 운영 API

기능:

- 목록
- 상세
- Effective Value
- Version 비교
- Validation
- Approval
- Publish
- Apply Status
- Drift
- Reconcile
- Rollback
- Audit

## 22. Test

- Codec
- Unicode/줄바꿈
- Null/빈 값
- Oracle/PostgreSQL/MariaDB Mapping
- 중복 Event
- Lease 만료
- Stale ACK
- Poison
- Partial Apply
- Drift
- Rollback
- Secret Masking

## 23. 체크리스트

- [ ] 설정 Metadata가 있다.
- [ ] Secret 원문을 저장하지 않는다.
- [ ] 정책은 Version과 Checksum을 가진다.
- [ ] 원장과 배포 Event가 원자적이다.
- [ ] Claim/Lease/Fencing이 있다.
- [ ] Partial Failure와 Drift를 조회할 수 있다.
- [ ] 위험 정책에 승인과 감사가 있다.
- [ ] Rollback과 Health Gate가 있다.
