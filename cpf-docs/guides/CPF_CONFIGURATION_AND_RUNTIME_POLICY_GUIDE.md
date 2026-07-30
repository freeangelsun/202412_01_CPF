# CPF 설정과 실행 환경 정책 배포 가이드

[← 문서 홈](README.md) · [제품 소개](../../README.md) · [구조와 배포](CPF_ARCHITECTURE_AND_TOPOLOGY_GUIDE.md) · [용어와 계약](CPF_TERMINOLOGY_AND_CONTRACT_REFERENCE.md)

> **대상**: 설정 관리자, 운영자, 실행 환경 개발자
> **목적**: 버전 설정을 검증·승인·게시하고 인스턴스 적용·정본 불일치·되돌리기를 관리한다.
> **관련 문서**: [플랫폼 운영자](CPF_ADMIN_OPERATOR_GUIDE.md) · [보안·재해복구·데이터 보존](CPF_SECURITY_DR_RETENTION_GUIDE.md)

---


## 0. 문서 계약

| 항목 | 기준 |
|---|---|
| 기준 Source | `master` / `b7c6146e952c10b885952fa2bc6b6786f4611d86` |
| Owner | 기술 계약은 `cpf-core`; 정책 Owner와 적용 Adapter는 각 실행 환경 |
| 이 문서로 완료하는 일 | 정적 설정과 동적 정책을 버전·승인·적용 ACK·Drift·Rollback으로 관리하고 Secret 원문을 분리한다. |
| 적용 범위 | Profile, Environment, Secret Reference, Runtime Policy, Cache, Log Level, Gateway/Batch 정책 |
| 주요 독자 | Platform Engineer, 운영자, 보안 관리자, Runtime Owner |
| 완료 판정 | Source·API·SQL·Config·Test·Runtime·Evidence 중 해당 범위가 실제로 연결되고 검증돼야 한다. |

### 0.1 읽는 순서

1. 책임 경계와 상태 모델을 먼저 확인한다.
2. 정상 절차를 수행하기 전에 권한·설정·데이터베이스·다중 인스턴스 영향을 확인한다.
3. 오류·부분 실패·복구 절차와 완료 점검을 같은 작업 범위로 수행한다.
4. 직접 실행하지 않은 검증은 `완료`로 기록하지 않는다.

---


## 1. 목적

CPF 설정은 파일의 Key/Value를 읽는 기능에 그치지 않는다. 안전한 기본값, 환경별 설정, 운영 변경, 버전, 승인, 배포, ACK, 구성 불일치와 되돌리기를 하나의 생명주기로 관리한다.

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
- Dynamic 실행 환경
- 비밀값 참조
- Feature Flag
- 경로 선택
- Rate/시간 제한
- 로그 정책
- 보안 정책
- 배치 정책
- 보존
- Tenant

## 4. 메타데이터

각 Key는 다음 메타데이터를 가진다.

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

## 5. 범위

- Global
- Environment
- 셀
- 서비스
- 인스턴스
- 업무영역
- Tenant
- 경로
- 작업

범위 충돌 우선순위를 명확히 한다.

## 6. 비밀값

비밀값 성격 설정은 원문 값을 저장하지 않는다.

```yaml
client-secret-ref: vault://payment/client-secret
```

조회 API는 참조와 메타데이터만 반환한다.

## 7. 실행 환경 정책 원장

정책 상태:

```text
DRAFT
→ VALIDATED
→ APPROVED
→ PUBLISHED
→ RETIRED
```

정책은 버전과 체크섬을 가진다.

## 8. 메타데이터 Codec

게이트웨이와 같은 실행 제품은 정책 메타데이터를 임의 문자열 연결로 전달하지 않는다. Codec은 필드명, 자료형, 기본값, 버전과 알 수 없는 필드 처리 규칙을 고정하며, 직렬화 뒤 다시 읽었을 때 같은 의미가 유지되는지 검증한다. 손상된 메타데이터나 지원하지 않는 버전은 적용하지 않는다.


정책 본문은 버전이 부여된 JSON 또는 Typed DTO를 사용한다.

지원:

- 다중 메타데이터
- Unicode
- 줄바꿈
- 빈 값
- Escape
- 중첩 객체
- 배열
- 스키마 버전

구분자 Split 문자열로 저장하지 않는다.

## 9. 게시와 사건

```text
정책 원장 변경
+ 배포 Event
→ Commit
→ Consumer Claim
→ Apply
→ ACK
```

원장과 Durable 사건의 원자성을 보장한다.

## 10. 소비자

다중 소비자 안전성:

- 점유
- 임대
- Fencing
- expectedVersion
- checksum
- retry
- poison
- stale ACK 거부

## 11. Row 매핑

DB Column Label 대소문자에 의존하지 않는 명시적 매핑을 사용한다. Null/빈 값 의미를 공급자 간 통일한다.

## 12. 적용

적용 단계:

1. 사건 검증
2. 대상 범위 확인
3. 버전 비교
4. 본문 스키마
5. 비밀값 참조 존재
6. Preview
7. Atomic Swap
8. 상태 점검
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

각 대상 인스턴스 결과를 저장한다.

## 14. 재시도

Retryable:

- 일시 Network
- 잠금
- 대상 Startup 중
- 일시 Store 장애

Non-retryable:

- 스키마 오류
- 지원하지 않는 버전
- 권한
- 체크섬
- 알 수 없는 Key
- 금지 범위

## 15. 정본 불일치

Expected 정책과 실행 환경 실제 스냅샷을 비교한다.

- 버전
- 체크섬
- 범위
- Effective Value
- 소스 Layer
- AppliedAt

## 16. 상태 대사

- 정본 불일치 조회
- 대상 선택
- 원인
- 재적용
- 실행 환경 Restart 필요 여부
- 결과
- 감사

## 17. 되돌리기

과거 검증 버전으로 되돌린다.

- 호환성
- 비밀값 참조
- 적용 순서
- 상태 점검 Gate
- Partial 되돌리기
- 감사

## 18. Feature Flag

- 기본값
- 대상
- 비율
- 조건
- 시작/종료
- Kill Switch
- 되돌리기
- 지표

업무 원장 의미를 Feature Flag만으로 바꾸지 않는다.

## 19. 로그 정책

- Logger
- Level
- 범위
- 만료
- Sampling
- 마스킹
- 추적 Boost
- 최대 기간

동적 DEBUG는 자동 만료된다.

## 20. 권한과 승인

고위험 정책:

- 외부 공개
- 인증 완화
- Rate 상한 완화
- 비밀값 변경
- 보존 Purge
- 로그 민감도
- 배치 재시도
- 내려받기

는 별도 권한과 승인 정책을 적용한다.

## 21. 운영 API

### 참조 Catalog

동적 매개변수 중 서비스, 서버 그룹, 비밀값 참조, 작업정의처럼 관리 대상 식별자를 가리키는 값은 참조 Catalog Port로 조회한다. Catalog 응답은 식별자, 표시명, 사용 가능 여부, 비활성 사유, 상위 식별자와 부가 정보를 포함한다. 상위 매개변수가 바뀌면 종속 선택값을 지우고 다시 검증한다.


기능:

- 목록
- 상세
- Effective Value
- 버전 비교
- 검증
- 승인
- 게시
- 적용 상태
- 정본 불일치
- 상태 대사
- 되돌리기
- 감사

## 22. 테스트

- Codec
- Unicode/줄바꿈
- Null/빈 값
- Oracle/PostgreSQL/MariaDB 매핑
- 중복 사건
- 임대 만료
- 오래된 확인 응답
- Poison
- 일부 적용
- 정본 불일치
- 되돌리기
- 비밀값 마스킹

## 23. 체크리스트

- [ ] 설정 메타데이터가 있다.
- [ ] 비밀값 원문을 저장하지 않는다.
- [ ] 정책은 버전과 체크섬을 가진다.
- [ ] 원장과 배포 사건이 원자적이다.
- [ ] 점유/임대/Fencing이 있다.
- [ ] Partial Failure와 정본 불일치를 조회할 수 있다.
- [ ] 위험 정책에 승인과 감사가 있다.
- [ ] 되돌리기와 상태 점검 Gate가 있다.

## 부록 A. 설정 명세 예

```yaml
key: cpf.remote.payment.timeout
valueType: duration
scope: service
required: true
defaultValue: 1500ms
minimum: 100ms
maximum: 10000ms
secret: false
restartRequired: false
validation:
  - lessThan: cpf.remote.payment.totalBudget
```

## 부록 B. 단계 적용

1. 시험 인스턴스 또는 소규모 대상군에 게시한다.
2. 적용 확인 응답과 실제 체크섬을 확인한다.
3. 오류율·지연·준비 상태를 관찰한다.
4. 확대 조건을 만족하면 다음 대상군에 적용한다.
5. 중단 조건을 만족하면 확대를 멈추고 되돌린다.

## 부록 C. 비밀값 교체

새 버전 등록 → 이중 유효 기간 → 소비자 단계 적용 → 새 값 사용 확인 → 구 값 폐기 → 실패 소비자 격리·재적용 → 감사

## 부록 D. 정본 불일치

인스턴스가 보고한 버전·체크섬이 기대값과 다르면 자동으로 성공 처리하지 않는다. 마지막 적용 시각, 실패 단계, 재시작 필요 여부와 로컬 덮어쓰기를 확인하고 재적용 또는 되돌리기를 수행한다.

## 29. 설정 우선순위

권장 우선순위는 낮은 값에서 높은 값 순으로 다음과 같다.

```text
제품 기본값
→ Environment Profile
→ 배포 Manifest 설정
→ Secret Reference 해석
→ 승인된 Runtime Policy
→ 시간 제한 Emergency Override
```

상위 계층이 하위 값을 덮어쓸 때 최종 Effective Value와 출처를 운영자가 확인할 수 있어야 한다. Secret Value는 Effective Config 조회에서도 원문을 반환하지 않는다.

## 30. Runtime Policy 게시 절차

```text
Draft
→ Schema·범위·상한 검증
→ 영향 Preview
→ 작성/승인 분리
→ Publish Version 생성
→ Runtime Instance Claim/Fetch
→ 로컬 검증
→ Atomic Apply
→ ACK
→ Expected/Applied 비교
```

정책 적용 실패가 원 업무 Transaction을 Rollback시키지 않도록 별도 Transaction과 재처리 Queue를 사용한다. 다만 보안 필수 정책이 없으면 해당 기능은 안전 차단한다.

## 31. Override 안전 규칙

- 대상 System·Module·Instance·Transaction 범위를 최소화한다.
- 시작·만료 시각과 자동 회수 조건을 둔다.
- 최대 Log Level·Capture Byte·Timeout·Rate 상한을 넘지 못하게 한다.
- Base Policy와 Override Diff를 보여준다.
- 같은 대상의 중복 Override 우선순위를 명시한다.
- Override 생성·변경·중지와 실제 적용 ACK를 감사합니다.

## 32. Drift 대응

| Drift | 원인 예 | 조치 |
|---|---|---|
| Expected Version > Applied | Instance Offline·Fetch 실패 | 연결 복구 후 재적용 |
| Version 같고 Checksum 다름 | 로컬 변경·Codec 차이 | Instance 격리·정본 재적용 |
| 일부 Instance만 과거 Version | Rolling 중단·ACK 유실 | 확대 중단·상태 대사 |
| Expired Override 유지 | Timer/Clock 오류 | 즉시 회수·Clock 확인 |
| Secret Reference 해석 실패 | 권한·Provider 장애 | 안전 차단 또는 제한 동작 |

Drift를 “다음 재시작 때 해결”로 방치하지 않는다. 영향과 위험도에 따라 Reconcile, Drain, Rollback 또는 Incident를 선택한다.

## 부록 Z. 구현 추적 시작점

문서의 설명을 완료 근거로 사용하지 않는다. 아래 경로에서 실제 Consumer·구현·설정·SQL·Test 연결을 확인한다. 경로가 이동했다면 `git ls-files`와 `git grep -n`으로 최신 Owner를 다시 찾는다.

| 추적 대상 | 대표 경로 또는 명령 | 확인 목적 |
|---|---|---|
| 공통 계약 | `git ls-files cpf-core | Select-String "policy|config|secret"` | Policy·Secret Reference 계약 |
| Gateway 적용 | `CpfRuntimePolicyMetadataCodec.java`, `GatewayRuntimePolicyDistributionAdapter.java` | 정책 Metadata와 배포 |
| Runtime Config | 각 Module `src/main/resources/application.yml` | 정적 기본값과 Profile |
| Drift 검증 | `git grep -n "expected.*version\|applied.*version\|checksum" cpf-*` | 기대·적용 상태 비교 |

### Z.1 공통 확인 명령

```powershell
git status --short
git diff --check
git grep -n "TODO\|UnsupportedOperationException\|return null" -- ':!cpf-docs/archive/**'
pwsh -File .\cpf-tools\scripts\check-architecture-ownership.ps1
pwsh -File .\cpf-tools\scripts\check-document-links.ps1
pwsh -File .\cpf-tools\scripts\check-repository-hygiene.ps1
```

명령이 현재 Repository에 존재하지 않거나 Parameter가 달라졌다면 해당 Tool Source와 [도구 상세 참조](CPF_TOOL_REFERENCE.md)를 먼저 갱신한다.

### Z.2 완료 상태 사용

- **완료**: 구현·Consumer·운영 경로·검증·Evidence가 현재 Commit에서 확인됨
- **부분 구현**: 일부 계층 또는 실패·복구·운영 경로가 빠짐
- **미구현**: 제품 동작이 없음
- **미검증**: 구현은 있으나 요구된 실행 검증을 수행하지 않음
- **실패**: 검증을 수행했으나 기대 결과를 충족하지 못함
- **재확인 필요**: Source·문서·Evidence 또는 환경이 서로 달라 현재 상태를 확정할 수 없음
