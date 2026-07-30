# CPF 업무 관리자(BZA) 가이드

## 1. 목적

`cpf-biz-admin`은 고객 업무 운영을 위한 Backoffice다. 단순 CRUD가 아니라 조직, 사용자, 권한, 결재, 첨부, 알림과 감사를 장기간 안전하게 관리한다.

## 2. 주요 기능

- 사용자와 계정 상태
- Role과 Permission
- 조직과 직원
- Position, JobTitle과 Assignment
- 결재 정책과 실행
- 대리결재
- 알림
- 첨부와 검사
- 감사 Hash Chain
- Tenant와 환경 Scope

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
- 동시 수정 Version

## 4. Role

Role은 코드만 User Row에 저장하는 단순 구조가 아니다.

```text
User
→ UserRole Assignment
→ Role
→ Permission
→ Scope
```

Assignment 필드:

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

## 5. Permission

Permission은 다음 축을 조합한다.

- Resource
- Action
- HTTP Method
- API Pattern
- Menu
- Domain
- Environment
- Organization
- Data Scope
- Allow/Deny
- Validity

Deny 우선순위를 명확히 한다. UI 메뉴 숨김은 서버 권한 검사를 대신하지 않는다.

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

직원은 여러 조직 Assignment를 가질 수 있다.

## 8. Assignment

Assignment는 다음을 표현한다.

- 소속 조직
- 직위
- 직책
- 업무 Role
- 주 소속
- 겸직
- 파견
- 직무대행
- 유효기간

결재 시점의 조직과 역할은 Snapshot으로 보존한다.

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
- Timeout
- Escalation
- 유효기간

Published Policy는 직접 수정하지 않고 새 Version을 만든다.

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

Requester는 Body 값이 아니라 인증 사용자와 직원 Mapping에서 도출한다.

## 11. 대리결재

검증 항목:

- 대리 기간
- 대상 업무
- 위임자/수임자
- 조직 Scope
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

Route와 Component 실파일, API Permission을 Gate에서 확인한다.

## 13. Server Paging

대량 목록은 `/page` API 또는 Cursor를 사용한다.

- Stable Order
- Count 최적화
- Filter Allowlist
- Export 별도 처리
- Browser 전체 조회 금지

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
- Retention 미만료
- 감사

## 15. 알림

알림은 다음을 제공한다.

- Channel
- Template Version
- Recipient
- Reference
- 발송 상태
- 읽음
- Retry
- 만료
- Deep Link

알림 링크를 열 때 대상 업무 권한을 다시 확인한다.

## 16. Audit Hash Chain

각 Audit Row:

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
- 조직 Parent 변경 Lock
- Primary Assignment 직렬화
- Role Grant/Revoke operationId
- 결재 Action 중복 방지
- Notification Read Idempotency

## 18. Tenant

Tenant 기능 사용 시:

- Tenant Resolver
- User/Role Scope
- DB Predicate 또는 Schema
- Cache Key
- Log/Audit
- File Path
- Notification
- Batch

에 Tenant가 일관되게 반영돼야 한다.

## 19. Frontend 사용성

- 기능별 Route와 Directory
- 검색과 Paging
- 상세 Drawer/Page
- Validation 요약
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
3. 조직 Assignment
4. Role Grant
5. Permission 확인
6. Session 발급
7. 감사 확인

### 조직 이동

1. 새 Assignment 시작일 등록
2. 기존 Assignment 종료일 설정
3. Primary 갱신
4. 결재 정책 영향 확인
5. Cache 무효화
6. 감사

### 퇴직

1. 계정 중지
2. Session 폐기
3. Role 종료
4. Assignment 종료
5. 진행 결재 대체자 처리
6. 파일·업무 데이터 Retention 적용

## 21. Test

- 조직 Cycle
- 유효기간 중첩
- 다중 Primary 충돌
- Role 재부여 이력
- Deny 우선순위
- 결재 자기 승인
- 대리 만료
- Optimistic Lock
- 첨부 감염
- Audit 변조
- Browser 권한

## 22. 체크리스트

- [ ] User, Employee, Assignment가 분리됐다.
- [ ] Role 변경 이력이 보존된다.
- [ ] Permission은 서버가 평가한다.
- [ ] 조직 Cycle을 차단한다.
- [ ] 결재 Policy Version이 고정된다.
- [ ] 자기 승인과 이해 상충을 차단한다.
- [ ] 첨부 검사와 격리가 연결된다.
- [ ] Audit Hash Chain이 Tail 삭제를 검출한다.
- [ ] 대량 목록은 서버 Paging이다.
