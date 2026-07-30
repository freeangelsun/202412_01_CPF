# CPF 업무 관리자(BZA) 가이드

[← 문서 홈](README.md) · [제품 소개](../../README.md) · [구조와 배포](CPF_ARCHITECTURE_AND_TOPOLOGY_GUIDE.md) · [용어와 계약](CPF_TERMINOLOGY_AND_CONTRACT_REFERENCE.md)

> **대상**: 업무 관리자, 권한 관리자, 결재 운영자
> **목적**: 조직·사용자·역할·권한·결재·알림·첨부를 감사 가능하게 운영한다.
> **관련 문서**: [ADM·BZA 화면 표준](CPF_ADMIN_BZA_UI_STANDARD_GUIDE.md) · [보안·재해복구·데이터 보존](CPF_SECURITY_DR_RETENTION_GUIDE.md)

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
