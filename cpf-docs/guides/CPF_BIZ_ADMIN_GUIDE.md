# CPF 업무 관리자(BZA) 가이드

[← 문서 홈](README.md) · [제품 소개](../../README.md) · [구조와 배포](CPF_ARCHITECTURE_AND_TOPOLOGY_GUIDE.md) · [용어와 계약](CPF_TERMINOLOGY_AND_CONTRACT_REFERENCE.md)

> **대상**: 업무 관리자, 권한 관리자, 결재 운영자
> **목적**: 조직·사용자·역할·권한·결재·알림·첨부를 감사 가능하게 운영한다.
> **관련 문서**: [ADM·BZA 화면 표준](CPF_ADMIN_BZA_UI_STANDARD_GUIDE.md) · [보안·재해복구·데이터 보존](CPF_SECURITY_DR_RETENTION_GUIDE.md)

---


## 0. 문서 계약

| 항목 | 기준 |
|---|---|
| 기준 Source | `master` / `b7c6146e952c10b885952fa2bc6b6786f4611d86` |
| Owner | `cpf-biz-admin` |
| 이 문서로 완료하는 일 | 사용자·조직·Role·Permission·결재·알림·첨부를 유효기간·상태 전이·승인·감사와 함께 운영한다. |
| 적용 범위 | BZA API·Frontend·Data Ownership·업무 관리자 권한 |
| 주요 독자 | 업무 관리자, 권한 관리자, 조직 관리자, 결재 운영자 |
| 완료 판정 | Source·API·SQL·Config·Test·Runtime·Evidence 중 해당 범위가 실제로 연결되고 검증돼야 한다. |

### 0.1 읽는 순서

1. 책임 경계와 상태 모델을 먼저 확인한다.
2. 정상 절차를 수행하기 전에 권한·설정·데이터베이스·다중 인스턴스 영향을 확인한다.
3. 오류·부분 실패·복구 절차와 완료 점검을 같은 작업 범위로 수행한다.
4. 직접 실행하지 않은 검증은 `완료`로 기록하지 않는다.

---


## 1. 목적

`cpf-biz-admin`은 고객 업무 운영을 위한 관리 체계다. 단순 CRUD가 아니라 조직, 사용자, 권한, 결재, 첨부, 알림과 감사를 장기간 안전하게 관리한다.

## 2. 주요 기능

- 사용자와 계정 상태
- 역할과 권한
- 조직과 직원
- Position, JobTitle과 배정
- 결재 정책과 실행
- 대리결재
- 알림
- 첨부와 검사
- 감사 해시 Chain
- Tenant와 환경 범위

## 3. 사용자

사용자 상태 예:

- ACTIVE
- LOCKED
- SUSPENDED
- PASSWORD_CHANGE_REQUIRED
- EXPIRED
- TERMINATED

관리 원칙:

- ID와 Login ID 구분
- 비밀번호 원문 조회 금지
- 잠금 해제와 Reset 감사
- 계정 상태 변경 사유
- 유효기간
- 세션 폐기
- 동시 수정 버전

## 4. 역할

역할은 코드만 User Row에 저장하는 단순 구조가 아니다.

```text
User
→ UserRole Assignment
→ Role
→ Permission
→ Scope
```

배정 필드:

- userRoleId
- userId
- roleId
- validFrom / validTo
- primary
- environment
- organization
- grantedBy
- reason
- operationId
- version

Grant/Revoke는 이력을 보존한다.

## 5. 권한

권한은 다음 축을 조합한다.

- Resource
- Action
- HTTP Method
- API 형식
- Menu
- 업무영역
- Environment
- 조직
- Data 범위
- Allow/Deny
- Validity

Deny 우선순위를 명확히 한다. 화면 메뉴 숨김은 서버 권한 검사를 대신하지 않는다.

## 6. 조직

조직은 Tree 구조와 유효기간을 가진다.

- organizationId
- organizationCode
- name
- parentId
- type
- validFrom / validTo
- status
- version

Parent 변경 시 Cycle을 차단한다. 과거 조직 관계는 이력으로 보존한다.

## 7. 직원

사용자 계정과 직원은 분리할 수 있다.

- employeeId
- employeeNumber
- name
- employmentStatus
- hireDate / retireDate
- userId
- contact의 마스킹 값

직원은 여러 조직 배정을 가질 수 있다.

## 8. 배정

배정은 다음을 표현한다.

- 소속 조직
- 직위
- 직책
- 업무 역할
- 주 소속
- 겸직
- 파견
- 직무대행
- 유효기간

결재 시점의 조직과 역할은 스냅샷으로 보존한다.

## 9. 결재 정책

정책 구성:

- policyId
- version
- 대상 업무
- 조건
- Step
- Approver Rule
- ALL/ANY/N_OF_M
- 대리 허용
- 반려/회수/재상신
- 시간 제한
- Escalation
- 유효기간

게시된 정책은 직접 수정하지 않고 새 버전을 만든다.

## 10. 결재 실행

```text
업무 요청
→ 적용 정책 선택
→ Policy Version Snapshot
→ 결재선 생성
→ 요청
→ 승인/반려/보류
→ Owner Command
→ 결과
→ 감사
```

Requester는 Body 값이 아니라 인증 사용자와 직원 매핑에서 도출한다.

## 11. 대리결재

검증 항목:

- 대리 기간
- 대상 업무
- 위임자/수임자
- 조직 범위
- 재위임 허용
- 자기 승인 방지
- 이해 상충
- 감사

## 12. 메뉴

Menu는 Tree로 관리한다.

- menuId
- parentId
- route
- componentKey
- permission
- order
- visible
- enabled
- external 여부

경로와 Component 실파일, API 권한을 Gate에서 확인한다.

## 13. Server 페이징

대량 목록은 `/page` API 또는 Cursor를 사용한다.

- Stable Order
- Count 최적화
- Filter 허용 목록
- 반출 별도 처리
- 브라우저 전체 조회 금지

## 14. 첨부

상태:

```text
PENDING
→ SCANNING
→ CLEAN
→ AVAILABLE

PENDING/SCANNING
→ INFECTED / SUSPICIOUS / FAILED / QUARANTINED
```

다운로드 조건:

- CLEAN
- Quarantine 아님
- 권한
- 업무 대상 접근 가능
- 보존 미만료
- 감사

## 15. 알림

알림은 다음을 제공한다.

- 채널
- Template 버전
- Recipient
- 참조
- 발송 상태
- 읽음
- 재시도
- 만료
- Deep Link

알림 링크를 열 때 대상 업무 권한을 다시 확인한다.

## 16. 감사 해시 Chain

각 감사 Row:

- previousHash
- recordHash
- canonicalPayload
- actor
- target
- action
- occurredAt

검증 결과:

- VALID
- PARTIAL_LEGACY
- BROKEN

마지막 Row 삭제도 Persisted Chain Head로 검출한다.

## 17. 동시성

- `expectedVersion`
- 조직 Parent 변경 잠금
- Primary 배정 직렬화
- 역할 Grant/Revoke operationId
- 결재 Action 중복 방지
- Notification Read Idempotency

## 18. Tenant

Tenant 기능 사용 시:

- Tenant Resolver
- User/역할 범위
- DB Predicate 또는 스키마
- 캐시 Key
- 로그/감사
- 파일 Path
- Notification
- 배치

에 Tenant가 일관되게 반영돼야 한다.

## 19. 프런트엔드 사용성

- 기능별 경로와 Directory
- 검색과 페이징
- 상세 Drawer/페이지
- 검증 요약
- 401/403/409/500 처리
- 상태 Badge
- 접근성
- 반응형
- 위험 조치 확인
- 권한 없는 버튼 숨김

## 20. 운영 시나리오

### 신규 직원

1. 직원 등록
2. 계정 연결
3. 조직 배정
4. 역할 Grant
5. 권한 확인
6. 세션 발급
7. 감사 확인

### 조직 이동

1. 새 배정 시작일 등록
2. 기존 배정 종료일 설정
3. Primary 갱신
4. 결재 정책 영향 확인
5. 캐시 무효화
6. 감사

### 퇴직

1. 계정 중지
2. 세션 폐기
3. 역할 종료
4. 배정 종료
5. 진행 결재 대체자 처리
6. 파일·업무 데이터 보존 적용

## 21. 테스트

- 조직 Cycle
- 유효기간 중첩
- 다중 Primary 충돌
- 역할 재부여 이력
- Deny 우선순위
- 결재 자기 승인
- 대리 만료
- 낙관적 잠금
- 첨부 감염
- 감사 변조
- 브라우저 권한

## 22. 체크리스트

- [ ] User, Employee, 배정이 분리됐다.
- [ ] 역할 변경 이력이 보존된다.
- [ ] 권한은 서버가 평가한다.
- [ ] 조직 Cycle을 차단한다.
- [ ] 결재 정책 버전이 고정된다.
- [ ] 자기 승인과 이해 상충을 차단한다.
- [ ] 첨부 검사와 격리가 연결된다.
- [ ] 감사 해시 Chain이 Tail 삭제를 검출한다.
- [ ] 대량 목록은 서버 페이징이다.

## 부록 A. 권한 평가 순서

1. 계정 활성·잠금·만료 상태
2. 세션과 인증 강도
3. 사용자·조직·직위·직책 배정의 유효기간
4. 역할과 권한의 활성·유효기간
5. 업무 대상 범위와 데이터 범위
6. 상호 배타 권한과 작성자·승인자 분리
7. 비상 권한의 범위·사유·만료

화면에서 버튼이 숨겨져 있어도 서버가 같은 순서로 검증한다.

## 부록 B. 결재 스냅샷

결재 시작 시 정책 버전, 단계, 조건, 후보 승인자와 업무 요약을 스냅샷으로 보존한다. 이후 조직이나 정책이 바뀌어도 진행 중 결재의 의미가 임의로 바뀌지 않는다.

## 부록 C. 대리결재 통제

- 위임자·수임자·권한 범위
- 시작·종료 시각
- 재위임 허용 여부
- 자기 승인·상호 승인 금지
- 원 승인자와 대리 승인자 표시
- 사유와 감사
- 만료 뒤 신규 승인 차단

## 부록 D. 대량 등록·내보내기

대량 작업은 동기 화면 요청으로 전체 처리하지 않는다. 사전 검증 보고서, 오류 행 파일, 비동기 실행, 진행률, 취소·재처리, 다운로드 권한·사유·만료를 제공한다.

## 30. 사용자·계정 생명주기

```text
초대/생성
→ 본인 확인·초기 Credential
→ 활성
→ 잠금/정지
→ 복구
→ 퇴직·만료
→ 비활성·보존
```

계정 상태와 직원 재직 상태를 같은 값으로 합치지 않는다. 조직 이동·휴직·퇴직이 계정, Role Assignment, 결재 대리와 Session에 미치는 영향을 명시한다.

## 31. Role·Permission Assignment

- Role과 Permission 정의 Owner를 구분한다.
- Assignment에는 시작·종료 시각, 부여 사유, 부여자와 승인 ID를 둔다.
- 유효기간이 끝난 권한을 UI에서만 숨기지 않고 서버 평가에서 제외한다.
- 권한 변경 뒤 기존 Session·Token·Cache가 언제 갱신되는지 정의한다.
- 고위험 Permission은 상시 Role보다 시간 제한 Assignment 또는 비상 권한을 우선한다.
- 삭제 대신 비활성화와 참조 영향 확인을 사용한다.

## 32. 결재 Snapshot

결재 요청은 정책을 매번 다시 조회해 과거 의미를 바꾸지 않는다. 요청 시점의 단계, 결재자 결정 근거, 대리결재, 금액·조직 조건과 Payload Hash를 Snapshot으로 보존한다.

| 상황 | 처리 |
|---|---|
| 결재 중 조직 이동 | 기존 Snapshot 유지, 필요 시 명시적 재상신 |
| 결재자 퇴직·부재 | 승인된 대리 또는 운영 재지정 절차 |
| 요청 내용 변경 | 기존 결재 무효화 후 새 Payload Hash로 재상신 |
| 작성자=최종 승인자 | 분리 정책에 따라 거부 |
| 중복 승인 요청 | operationId와 현재 상태로 멱등 처리 |

## 33. 첨부 처리

1. 파일명·경로를 정규화한다.
2. 크기·확장자·MIME·Magic Byte를 확인한다.
3. 임시 격리 영역에 저장하고 Hash를 계산한다.
4. 악성 파일 검사를 수행한다.
5. `CLEAN`, `QUARANTINED`, `REJECTED` 상태를 분리한다.
6. 다운로드 시 권한·사유·보존 정책을 확인한다.
7. 원본 파일명과 저장 경로를 분리하고 직접 경로 접근을 금지한다.
8. 삭제·보존·법적 보류를 Metadata와 Object에 함께 적용한다.

## 34. 업무 관리자 Audit

사용자·조직·권한·결재·첨부 변경에는 Actor, Target, Action, Reason, Before/After, Version, Approval ID, Transaction/Trace ID, 결과와 실패 코드를 남긴다. 민감 Field는 Audit에서도 마스킹하며, Hash Chain을 사용하는 경우 Chain 단절 검증과 복구 정책을 둔다.

## 부록 Z. 구현 추적 시작점

문서의 설명을 완료 근거로 사용하지 않는다. 아래 경로에서 실제 Consumer·구현·설정·SQL·Test 연결을 확인한다. 경로가 이동했다면 `git ls-files`와 `git grep -n`으로 최신 Owner를 다시 찾는다.

| 추적 대상 | 대표 경로 또는 명령 | 확인 목적 |
|---|---|---|
| Application | `cpf-biz-admin/src/main/java` | 사용자·조직·권한·결재 Owner |
| Frontend | `cpf-biz-admin/frontend/src` | 업무 관리자 화면 |
| DB | `cpf-tools/db/canonical`, Vendor `bza`/business schema | Schema·Migration·Seed |
| Security Test | `git ls-files cpf-biz-admin | Select-String "test|permission|approval|audit"` | 권한·상태 전이·감사 |

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
