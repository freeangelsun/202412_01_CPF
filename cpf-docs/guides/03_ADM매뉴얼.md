# CPF ADM 매뉴얼 — 업무 연동과 권한별 조회·조치·승인·감사

> **주 독자**: 업무 개발자, ADM 연동 개발자, 조회자, 운영자, 승인자, 보안 담당자, 운영 관리자
> **완료 결과**: 업무 기능을 ADM에 연결하고, 권한에 따라 조회·판단·조치·승인·대사·감사를 수행한다.
> **Source 기준**: `freeangelsun/202412_01_CPF`, `master`, `dafe5c0e5260ea8149234e8ab2e75347e75338c1`

## 1. ADM의 정체성

ADM은 이용 조직이 새로 개발하는 별도 Framework가 아니다. CPF가 제공하는 운영 제품이다.

- 업무 개발자는 자기 업무 Owner의 Query·Command 계약을 ADM에 연결한다.
- 조회자와 운영자는 같은 ADM 화면을 권한에 따라 다르게 이용한다.
- 승인자는 위험 조치를 검토하고 승인·반려한다.
- 보안 담당자는 Permission·Data Scope·Masking·Audit을 점검한다.
- 플랫폼 운영자는 ADM Process·DB·배포·관측을 운영하지만 업무 원장을 직접 수정하지 않는다.

기존의 “ADM 개발자”와 “ADM 운영자” 문서를 분리하면 ADM 자체를 새로 개발하는 것으로 오해할 수 있으므로, 이 문서에서는 연동과 이용을 하나의 흐름으로 설명한다.

## 2. Owner와 경계

| 영역 | Owner |
|---|---|
| ADM Backend·Frontend·메뉴·사용자 경험 | `cpf-admin` |
| 업무 데이터·상태·업무 Command | 해당 업무 Module |
| Batch 실행 상태·제어 | `cpf-batch` |
| Gateway Route·게시·적용 | `cpf-gateway` |
| 조직·사용자·권한·결재 | `cpf-biz-admin` |
| DB·배포·Secret·관측 | 플랫폼 운영 영역 |

ADM은 다른 Owner의 DB를 직접 수정하지 않는다. Same-JVM Port 또는 Remote API로 조회·조치를 요청한다.

## 3. 기준 Source 위치

```text
cpf-admin/build.gradle
cpf-admin/src/main/**
cpf-admin/frontend/**
cpf-batch/contract/**
cpf-gateway/**
cpf-biz-admin/**
```

기준 Commit에서 `cpf-admin`은 WAR Plugin, Web MVC, WebFlux, JDBC, Security Starter와 Batch Contract 의존성을 선언한다. 실제 Route·Operation·Permission은 다음 정본을 함께 대조한다.

- Backend Controller·Service·Owner Port
- OpenAPI
- Generated Operation Contract
- Generated Route Operation Contract
- Orval 기반 Generated API Client
- Frontend Router·Menu·Component
- Permission 검사
- Browser Test

문서가 Source보다 앞서 새로운 Route·Button·Permission을 만들지 않는다.

## 4. 역할별 권한 모델

| 역할 | 기본 행동 | 금지 |
|---|---|---|
| 조회자 | 목록·상세·상태·Log·Metric 조회 | 상태 변경 |
| 운영자 | 허용된 재시도·중지·재개·대사 요청 | 승인 우회·DB 직접 수정 |
| 승인자 | 위험 조치 승인·반려 | 자기 요청 승인 |
| 보안 담당자 | 권한·가림·Audit·Export 점검 | 업무 상태 직접 변경 |
| 운영 관리자 | 메뉴·정책·조치 범위 관리 | 업무 Owner 책임 대체 |
| 업무 개발자 | Query·Command·Operation 계약 연결 | ADM 내부 DB에 업무 원장 복제 |
| ADM 확장 개발자 | 기본 제품으로 해결되지 않는 조직별 UI 확장 | Internal API 직접 참조 |

실제 Permission 문자열과 Scope는 Source에서 확인한다.

## 5. 업무 연동 계약

### 5.1 Query 계약

Query는 다음을 제공한다.

- 검색 Field와 Type
- 기본값
- 정렬
- Paging
- 목록 Column
- 상세 Field
- 상태와 Version
- Data Scope
- Masking
- 조회 시각·데이터 기준 시점
- Timeout
- 오류 Mapping

### 5.2 Command 계약

Command는 다음을 제공한다.

- Command ID
- 대상 ID
- `expectedVersion`
- `idempotencyKey`
- Reason
- Approval 필요 여부
- 입력값·범위
- Operation ID
- 현재 상태
- Timeout·결과 미확정
- Reconcile 방법
- Audit

### 5.3 Operation 조회

조치 응답을 받지 못했을 때 신규 Command를 만들지 않고 Operation ID로 조회한다.

```text
REQUESTED
IN_PROGRESS
WAITING_APPROVAL
WAITING_EXTERNAL
UNKNOWN_RESULT
PARTIAL_SUCCESS
FAILED_RETRYABLE
FAILED_FINAL
SUCCEEDED
```

실제 Owner가 다른 상태를 사용하면 Source 계약을 따른다.

## 6. Same-JVM과 Remote 연결

| 방식 | 사용 시점 | 필수 확인 |
|---|---|---|
| Same-JVM | ADM과 Owner가 같은 Process | Bean 경계·Transaction·Internal 직접 참조 금지 |
| Remote | 별도 WAS·서비스 | 인증·Audience·Deadline·오류·Retry·Trace |
| Read Model | 대량 조회·통합 목록 | 갱신 지연·정본 Owner·Drift |
| Async Command | 오래 걸리는 조치 | Operation·Outbox·결과 조회 |

두 방식은 Permission·Reason·Approval·Expected Version·Idempotency·Audit 의미가 같아야 한다.

## 7. ADM 연동 개발 절차

1. 업무 Owner와 상태·Command를 정한다.
2. ADM에서 필요한 Query·Command 목록을 만든다.
3. Public Port 또는 Remote API를 제공한다.
4. OpenAPI와 Operation Contract를 생성·검증한다.
5. Generated Client를 갱신한다.
6. Route·Menu·Component와 계약을 연결한다.
7. Permission·Data Scope·Masking을 Backend와 Frontend에 적용한다.
8. Reason·Approval·Expected Version을 조치 Form에 연결한다.
9. 정상·권한 거부·Version 충돌·Timeout·응답 유실을 시험한다.
10. Browser에서 Query·Command·Operation·Audit을 확인한다.
11. 운영 담당자에게 상태 판정과 정상화 절차를 인계한다.

## 8. 화면 설명 표준

각 화면은 다음 항목을 문서화한다.

| 항목 | 설명 |
|---|---|
| 메뉴 | 사용자가 찾는 위치 |
| Route | Source와 대조할 식별자 |
| Permission | 조회·조치·승인 권한 |
| 검색 Field | 이름·Type·기본값·범위 |
| Column | 표시값·정렬·가림 |
| 상세 Field | 상태·Version·식별자 |
| Button | 활성 조건·입력·결과 |
| Reason | 위험 조치 사유 |
| Approval | 정책·만료·승인자 |
| Expected Version | 동시 변경 방지 |
| 응답 유실 | Operation 조회 |
| 부분 적용 | 대상별 결과 |
| Retry·Reprocess | 허용 조건 |
| Reconcile | 원장·대상 대사 |
| Rollback·보상 | Owner가 제공하는 절차 |
| Audit | 요청·승인·결과·이전/이후 값 |

## 9. 공통 화면 이용 절차

### 9.1 목록 조회

1. 메뉴에 접근한다.
2. Permission과 Data Scope가 적용됐는지 확인한다.
3. 기준 시각과 검색 기본값을 확인한다.
4. 상태·기간·업무 Key를 입력한다.
5. 목록 Column의 가림 처리를 확인한다.
6. 총 건수와 Paging을 확인한다.
7. 상세로 이동한다.

정상 결과:

- 권한 범위 안의 데이터만 표시
- 검색 조건과 결과 수 일치
- 민감정보 가림
- 조회 Audit 또는 접근 Log 확인

### 9.2 상세 조회

확인 순서:

1. 업무 식별자
2. 현재 상태
3. Version
4. 최근 Operation
5. 외부 효과·Outbox
6. 오류 분류
7. Log·Trace
8. Audit
9. 허용 조치

### 9.3 조치 요청

1. 현재 상태와 Version을 다시 읽는다.
2. Button 활성 조건을 확인한다.
3. 대상·입력·Reason을 입력한다.
4. Preview가 있으면 범위·건수·영향을 확인한다.
5. 승인 필요 여부를 확인한다.
6. Command를 전송한다.
7. Operation ID를 저장한다.
8. 결과가 늦으면 Operation을 조회한다.
9. 업무 원장·대상·Audit에서 결과를 대사한다.

## 10. 권한·Data Scope·Masking

### Permission

- 메뉴 표시
- Route Guard
- Backend Authorization
- Command Permission
- Approval Permission
- Export Permission

모든 계층이 같은 계약을 사용해야 한다. Frontend Button 숨김만으로 권한을 구현하지 않는다.

### Data Scope

예:

- 전체 조직
- 소속 조직
- 하위 조직
- 담당 업무
- 본인 생성
- 승인 대상

Scope는 Query와 Command 모두에 적용한다.

### Masking

- 목록
- 상세
- Export
- Log
- Audit
- 오류 메시지

권한이 높아져도 원문 표시가 필요한 이유와 Audit을 남긴다.

## 11. Reason·Approval

### Reason

다음 조치에는 사유를 요구한다.

- 재처리
- 강제 중지·재개
- 결과 확정
- 설정 변경
- 대량 대상 조치
- Export
- 권한 변경

### Approval

승인 계약:

- 요청자와 승인자 분리
- 정책 Version
- 승인 유효시간
- 대상·입력 Hash
- 승인 후 변경 금지
- 반려 사유
- 만료
- 중복 승인 방지
- Audit

승인 후 대상 상태나 Version이 바뀌면 재승인을 요구하거나 Fail-closed한다.

## 12. Expected Version과 동시성

조치 Form은 상세 조회 시점의 Version을 전송한다.

충돌 시:

1. 변경된 최신 상태를 다시 조회한다.
2. 기존 입력을 자동 재적용하지 않는다.
3. 사용자에게 변경 내용을 표시한다.
4. 다시 조치할지 판단한다.
5. 필요한 경우 새 Reason·Approval을 받는다.

## 13. Timeout과 응답 유실

### Timeout 전

- 전체 Deadline
- ADM→Owner 호출 Timeout
- Retry 여부
- Operation 생성 시점
- Idempotency Key
- Trace

### Timeout 후

- 같은 Command를 새 Key로 반복하지 않는다.
- Operation ID 또는 Idempotency Key로 조회한다.
- Owner 원장과 외부 효과를 대사한다.
- 성공·실패·결과 미확정을 구분한다.
- 결과 미확정이면 Reconcile Command를 사용한다.

## 14. 부분 적용

대량 조치는 전체 성공만 표시하지 않는다.

| 대상 | 결과 | 다음 행동 |
|---|---|---|
| 성공 | 상태 유지 | 재실행 대상에서 제외 |
| 재시도 가능 실패 | 원인 제거 후 재처리 | 같은 Operation과 대상 Key 유지 |
| 최종 실패 | 사유 확인 | 보상·업무 판단 |
| 결과 미확정 | 원장·대상 대사 | 신규 실행 금지 |
| 권한 제외 | 조치하지 않음 | Scope·정책 확인 |

## 15. 온라인 업무 운영

조회 항목:

- Transaction ID
- Operation ID
- API
- 상태
- 응답 시간
- 오류 분류
- Idempotency
- Version
- Trace
- Audit

조치 예:

- 결과 대사 요청
- Retry 가능 Operation 재시도
- 실패 대상 재처리
- 정책이 허용한 상태 정정 Command

DB 직접 수정이나 상태 강제 변경은 허용하지 않는다.

## 16. Batch 운영

ADM에서 다음을 확인한다.

- Job·Version
- Schedule
- Parameter
- JobExecution·StepExecution
- Progress
- Worker·Lease·Fencing
- Retry·Skip
- 결과 미확정
- 업무 합계 대사
- 재시작·재처리·Abandon
- Approval·Audit

상세 절차는 [02 배치 개발 매뉴얼](02_배치개발매뉴얼.md)을 함께 사용한다.

## 17. 설정·배포 상태

ADM이 설정·배포 상태를 표시할 때 확인한다.

- 대상 인스턴스
- Desired Version
- Applied Version
- Checksum
- ACK·NACK
- 부분 적용
- Last Known Good
- Drift
- Reconcile
- Rollback 승인

실제 적용 책임은 각 Product Owner와 플랫폼 운영 영역에 있다.

## 18. Log·Metric·Trace

### Log

- 시간
- Service·Instance
- Transaction·Operation
- Error Code
- Masking
- 배포 Version

### Metric

- 요청량
- 오류율
- 지연
- Backlog
- Retry
- 결과 미확정
- Worker·Lease
- Resource

### Trace

- Parent·Child 관계
- Local·Remote
- 메시지·파일
- 외부기관
- Sampling
- 민감정보

ADM은 관측 데이터를 업무 원장과 동일시하지 않는다. 최종 상태는 Owner의 Operation·업무 원장으로 판정한다.

## 19. Audit

Audit에는 다음이 있어야 한다.

- Actor
- Role·Permission
- Data Scope
- Target
- Action
- Reason
- Approval
- Before·After
- Expected·Actual Version
- Operation
- Result
- Time
- Source IP·Client Context
- Masking·Export 여부

Audit 원문에 Password·Token·Secret을 저장하지 않는다.

## 20. Export

1. Export Permission을 확인한다.
2. 검색 조건과 Scope를 고정한다.
3. 예상 건수·민감정보를 표시한다.
4. Reason·Approval을 받는다.
5. 비동기 생성이면 Operation을 조회한다.
6. 파일 Checksum·만료·다운로드 횟수를 기록한다.
7. Audit과 삭제 정책을 확인한다.

## 21. Route·Menu 전수 대조 절차

```text
Backend Controller
→ OpenAPI Operation
→ Generated Operation Contract
→ Generated Route Contract
→ Frontend Router
→ Menu
→ Component
→ Permission
→ Browser Test
```

전수 대조 결과:

- Controller만 있고 화면이 없으면 부분 구현
- 화면만 있고 Backend가 없으면 미구현
- Permission 불일치면 실패
- Generated Client가 낡았으면 재확인 필요
- Browser Test를 실행하지 않았으면 미검증

## 22. Browser Test

필수 시나리오:

- 로그인·Session 만료
- 메뉴 Permission
- Data Scope
- Masking
- 검색 기본값
- Paging·정렬
- 상세 상태·Version
- Button 활성 조건
- Reason
- Approval
- Version 충돌
- Timeout
- 응답 유실
- 부분 적용
- Retry·Reprocess
- Reconcile
- Export
- Audit

기준 Commit에서 Browser Runtime을 직접 실행하지 않았으므로 문서 현행화만으로 성공 처리하지 않는다.

## 23. 장애와 정상화 Runbook

### ADM 접속 실패

1. Process·Health 확인
2. Session·인증 Provider 확인
3. DB·Owner API 확인
4. 최근 배포·설정 확인
5. Log·Trace 확인
6. Read-only 조회 가능 여부 확인
7. 원인 제거 후 기능 Smoke Test

### Owner 호출 실패

1. 대상 Service·Instance 확인
2. Deadline·Circuit 상태 확인
3. Operation 생성 여부 확인
4. 업무 원장 확인
5. 결과 미확정이면 Reconcile
6. 신규 Command 중복 전송 금지

### 화면과 상태 불일치

1. 조회 기준 시각 확인
2. Read Model 지연 확인
3. Owner 원장 조회
4. Drift Reconcile
5. Generated Client·Contract Version 확인
6. Audit 기록

## 24. 개발 인계표

| 항목 | 내용 |
|---|---|
| 업무 Owner | Module·담당팀 |
| Query | 목록·상세·필드·기본값 |
| Command | 입력·상태·Version |
| Permission | 조회·조치·승인·Export |
| Data Scope | 조직·업무 범위 |
| Masking | 필드·조건 |
| Reason·Approval | 정책·만료 |
| Operation | 상태·조회 |
| Timeout | 전체·하위 |
| Reconcile | 판정 절차 |
| Log·Metric·Trace | 이름·Label |
| Audit | 저장 항목 |
| Test | Contract·Browser·Fault |
| 미검증 | 환경·시나리오 |

## 25. 현재 상태 판정

| 항목 | 개발 상태 | 검증 상태 |
|---|---|---|
| ADM WAR·Frontend Build 구성 | Source 확인 | 실행 미검증 |
| Security Starter 연결 | Source 확인 | Browser·DB 미검증 |
| Batch Contract 연결 | Source 확인 | Runtime 미검증 |
| Generated Route·Operation·API Client | 인수인계상 반영 | 최신 전수 재확인 필요 |
| Query·Command·Approval·Audit | 기능별 상이 | Browser·Fault 미검증 |
| Same-JVM·Remote | 구조 존재 범위별 상이 | Timeout·결과 미확정 미검증 |

## 26. 완료 점검표

- [ ] ADM을 업무 제품 자체의 개발 영역으로 설명하지 않았다.
- [ ] 업무 Owner와 ADM Owner가 분리됐다.
- [ ] Route·Menu·Controller·Generated Contract가 일치한다.
- [ ] Permission·Data Scope·Masking이 Frontend·Backend에 함께 적용됐다.
- [ ] Reason·Approval·Expected Version이 위험 조치에 적용됐다.
- [ ] Timeout·응답 유실·부분 적용·결과 미확정 절차가 있다.
- [ ] Retry·Reprocess·Reconcile·보상·Rollback Owner가 명확하다.
- [ ] Log·Metric·Trace·Audit로 결과를 확인할 수 있다.
- [ ] Browser·Fault Test 실행 여부를 사실대로 기록했다.
