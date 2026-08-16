# CPF 운영자 매뉴얼 작성 표준 지침

## 사용자 탐색성 보완 규칙 (2026-08-16)

- 운영자 문서는 화면/기능 목록보다 `증상·업무 목적 → 확인 위치 → 판단 기준 → 허용된 조치 → 확인` 흐름을 우선한다.
- Summary만 읽어도 어느 화면에서 무엇을 확인할지 판단할 수 있어야 하며, 내부 구현 Class/Package/QA 정보는 일반 운영 본문에서 제외한다.
- 위험 조치는 권한·사유·승인·감사·결과 확인을 짧고 명확하게 연결한다.

## 1. 문서 목적

본 지침은 CPF의 운영자용 매뉴얼을 작성·검수할 때 적용하는 공통 작성 기준이다.

대상 문서는 주로 다음과 같다.

- `cpf-docs/guides/04_ADM운영자매뉴얼.md`
- `cpf-docs/guides/05_플랫폼운영매뉴얼.md`
- `cpf-docs/guides/90_BZA매뉴얼.md` 중 운영자 영역
- `cpf-docs/guides/91_Gateway매뉴얼.md` 중 운영자 영역
- 그 밖에 공식 사용자 문서 안에 포함되는 운영 절차·장애 대응·복구 절차

운영자 매뉴얼의 목표는 기능을 소개하는 데 있지 않다.

**CPF를 처음 접한 운영자가 개발자에게 Source를 물어보거나 별도의 구두 설명을 받지 않고도 문서에서 필요한 업무를 찾아 다음 업무를 수행할 수 있어야 한다.**

> 상황 인지 → 기능/화면 탐색 → 현재 상태 확인 → 정상/비정상 판단 → 작업 수행 → 결과 확인 → 실패 원인 분류 → Retry/Restart/Reprocess/Reconcile/Compensation/Rollback 판단 → 복구 → 정상화 검증 → Audit 확인 → 필요 시 Escalation

따라서 운영자 매뉴얼은 다음 질문에 직접 답할 수 있어야 한다.

1. 이 기능은 무엇인가?
2. 언제 사용하는가?
3. 내가 수행해도 되는 작업인가?
4. 어떤 권한이 필요한가?
5. 어디로 들어가야 하는가?
6. 무엇을 조회해야 하는가?
7. 화면에 보이는 값은 무엇을 의미하는가?
8. 정상 상태는 무엇인가?
9. 이상 상태는 무엇인가?
10. 버튼을 눌러도 되는 조건은 무엇인가?
11. 실행하면 내부 상태가 어떻게 변하는가?
12. 실행 결과를 어디서 확인하는가?
13. 오류가 나면 다시 눌러도 되는가?
14. 요청 결과를 모르면 어떻게 확인하는가?
15. 일부만 처리되었으면 어떻게 하는가?
16. 재처리 대상은 어떻게 결정하는가?
17. 이미 상대 시스템에서 처리되었는지는 어떻게 확인하는가?
18. 이전 상태로 복구할 수 있는가?
19. 누가 승인해야 하는가?
20. Reason은 왜 입력하고 어디에 기록되는가?
21. 누가 언제 어떤 작업을 했는지 어디서 확인하는가?
22. 장애가 정상화되었다고 무엇을 기준으로 판단하는가?
23. 개발자 또는 다른 운영 조직에 넘겨야 하는 기준은 무엇인가?

이 질문에 답할 수 없다면 해당 기능의 운영자 문서는 충분하지 않은 것으로 판정한다.

---

# 2. 최우선 작성 원칙

## 2.1 실제 구현이 문서보다 우선한다

운영자 매뉴얼 작성 시 다음 순서로 사실을 확인한다.

1. 실제 Source
2. Frontend Route·Component·State
3. Backend Controller·Application Service·Port
4. SQL·Migration·Repository
5. Configuration
6. Permission·Role 정의
7. Batch·Scheduler·Messaging 구성
8. Test
9. 기존 공식 문서
10. 과거 보고서·이전 문서

기존 매뉴얼에 적혀 있다는 이유만으로 사실로 간주하지 않는다.

화면에 존재하지 않는 버튼, 구현되지 않은 API, Source에 없는 상태, 실제로 사용되지 않는 Property, 존재하지 않는 Permission을 문서에 만들어 넣지 않는다.

---

## 2.2 기능 설명이 아니라 운영 절차를 작성한다

다음과 같은 표현으로 끝내지 않는다.

- 사용자를 관리한다.
- 장애를 확인한다.
- 배치를 재처리할 수 있다.
- 로그를 조회한다.
- 상태를 변경한다.
- Gateway Route를 관리한다.
- 권한을 관리한다.
- 승인 후 반영된다.

반드시 다음 수준까지 연결한다.

> 누가 → 어떤 권한으로 → 어떤 화면/명령에서 → 어떤 조건을 입력하고 → 어떤 대상을 선택하여 → 어떤 작업을 실행하고 → 어떤 상태 변경이 발생하며 → 성공 여부를 무엇으로 확인하고 → 실패한 경우 어떤 상태가 남으며 → 재시도 또는 복구 여부를 어떻게 결정하고 → 최종 정상화를 무엇으로 판정하는가

---

# 3. 운영자 매뉴얼의 기본 독자

최소 다음 독자를 모두 고려한다.

| 독자필요한 정보 |                                             |
| -------- | ------------------------------------------- |
| 신규 운영자   | 시스템 구조, 메뉴 위치, 기본 용어, 정상 상태                 |
| 일반 운영자   | 조회, 검색, 일상 운영, 일반 조치                        |
| 고급 운영자   | 장애 분석, 상태 확인, 재처리                           |
| 승인자      | 위험 작업의 요청 내용, 영향 범위, 승인 기준                  |
| 보안 운영자   | 권한, Masking, Session, Audit                 |
| 플랫폼 운영자  | Process, DB, Kafka, Config, Deploy, Backup  |
| 업무 운영자   | 업무 건 상태, 재처리, 대사                            |
| 장애 대응자   | 증상→원인→확인→복구 Runbook                         |
| 감사 대응자   | 작업자, 변경 전후 값, Reason, Approval, Audit Trail |
| 인수인계 담당자 | 시스템 현황, 운영 경계, 미검증 영역                       |

---

# 4. 문서 탐색성을 최우선으로 설계한다

운영 중인 사용자는 처음부터 끝까지 매뉴얼을 읽지 않는다.

대부분 다음 형태로 검색한다.

- 이 메뉴가 뭐지?
- `UNKNOWN_RESULT`가 떴는데?
- 이 버튼 눌러도 되나?
- 왜 버튼이 비활성화되어 있지?
- 이 에러코드는 뭐지?
- 이 Job 다시 돌려도 되나?
- Kafka Lag이 증가했는데?
- 외부 시스템 Timeout인데 다시 보내도 되나?
- Gateway Publish 실패했는데?
- 설정 변경이 일부 서버에만 반영됐는데?
- 사용자가 로그인이 안 된다고 하는데?
- 권한을 줬는데 메뉴가 안 보이는데?
- 데이터가 서로 다른데 어느 쪽이 맞나?

따라서 각 매뉴얼은 **업무 흐름 목차와 검색형 Reference를 동시에 제공**해야 한다.

---

# 5. 문서 첫 부분에 반드시 있어야 할 Quick Navigation

각 운영자 매뉴얼 초반에는 최소 다음 Navigation을 둔다.

## 5.1 “무엇을 하려는가?” 기준 업무 찾기

예:

| 하고 싶은 일이동할 절       |                         |
| ------------------ | ----------------------- |
| 시스템 상태 확인          | 시스템 상태 조회               |
| 사용자 찾기             | 사용자 조회                  |
| 사용자 권한 확인          | 권한 조회                   |
| 장애 Job 확인          | Batch 실행 이력             |
| 실패 Job 재처리         | Batch 재처리               |
| UNKNOWN\_RESULT 확인 | 결과 불명확 건 대사             |
| Gateway Route 확인   | Route 조회                |
| Route 변경           | Route 변경 및 승인           |
| Audit 확인           | 감사 이력 조회                |
| 로그 추적              | Trace/Correlation ID 조회 |

---

## 5.2 “현재 증상이 무엇인가?” 기준 찾기

| 증상우선 확인이동할 절 |                   |                 |
| ------------ | ----------------- | --------------- |
| 화면 접속 불가     | 서비스 Health, 인증    | 화면 접속 장애        |
| 버튼 비활성       | 상태, Permission    | Button 활성 조건    |
| 조회 결과 0건     | 검색 조건, Data Scope | 조회 문제           |
| 처리 Timeout   | 처리 상태 확인          | Timeout 대응      |
| 결과 불명확       | 중복 실행 금지 여부       | UNKNOWN\_RESULT |
| 일부 서버만 변경    | Instance 상태       | Partial Apply   |
| Kafka Lag 증가 | Consumer 상태       | Kafka 장애        |
| DB 응답 지연     | Pool, Lock, Query | DB 장애           |
| Batch 중단     | Job/Step 상태       | Batch 장애        |
| 외부 시스템 응답 없음 | Timeout/Attempt   | 외부 연계 장애        |

---

## 5.3 “상태값으로 찾기”

가능한 주요 상태를 초반에 색인한다.

예:

| 상태의미운영자 기본 행동   |          |                |
| --------------- | -------- | -------------- |
| READY           | 실행 가능    | 선행 조건 확인       |
| RUNNING         | 처리 중     | 중복 실행 금지 여부 확인 |
| SUCCESS         | 정상 종료    | 결과 검증          |
| FAILED          | 실패       | 실패 원인 확인       |
| PARTIAL         | 일부 처리    | 성공/실패 대상 분리    |
| UNKNOWN\_RESULT | 결과 확정 불가 | 재실행 전에 대사      |
| CANCELLED       | 취소       | 후속 상태 확인       |

실제 상태명은 반드시 Source 기준으로 작성한다.

---

# 6. 매뉴얼 전체 권장 대목차

운영자 매뉴얼은 최소 다음 구조를 가진다.

## 1. 매뉴얼 사용법

## 2. 제품/서비스 운영 범위

## 3. Architecture와 운영 대상

## 4. 역할·권한·책임

## 5. 운영 용어와 상태

## 6. 메뉴 및 기능 전체 지도

## 7. 일상 운영 절차

## 8. 조회·검색·상태 확인

## 9. 변경 작업

## 10. 승인 작업

## 11. 위험 작업

## 12. 모니터링

## 13. 로그·Metric·Trace

## 14. 장애 판단

## 15. Retry

## 16. Restart

## 17. Reprocess

## 18. Reconcile

## 19. Compensation

## 20. Rollback

## 21. UNKNOWN\_RESULT

## 22. 부분 실패

## 23. Security

## 24. Permission·Data Scope·Masking

## 25. Audit

## 26. Config 변경

## 27. 배포 및 변경 관리

## 28. Backup·Restore

## 29. DR

## 30. 장애 Runbook

## 31. 정기 운영 점검

## 32. 운영 인수인계

## 33. FAQ

## 34. Error/상태/명령 Reference

## 35. Source·Config·SQL·Test 근거

## 36. 미구현·미검증·제한사항

각 매뉴얼의 성격에 따라 내용을 확장하되 위 핵심 영역을 생략해서는 안 된다.

---

# 7. 기능별 작성 단위

각 기능 설명은 동일한 구조를 사용한다.

## 7.1 기능 Header

각 기능 시작 부분에 다음 표를 둔다.

| 항목내용         |                              |
| ------------ | ---------------------------- |
| 기능명          | 운영자가 검색할 실제 기능명              |
| 기능 ID        | 식별 가능한 경우                    |
| 목적           | 왜 사용하는가                      |
| 대상 역할        | 누가 사용하는가                     |
| Owner Module | 실제 소유 Module                 |
| 실제 Consumer  | 실제 호출/사용 주체                  |
| 메뉴           | ADM/BZA 등 실제 메뉴              |
| Route        | Frontend Route               |
| API          | 관련 Public/Administrative API |
| Permission   | 필요한 Permission               |
| Data Scope   | 조회/변경 범위 제한                  |
| 중요도          | 조회/일반변경/위험조치                 |
| 승인 필요        | Yes/No                       |
| Audit        | 생성되는 Audit 종류                |
| 구현 상태        | 완료/부분 구현/미구현/미검증/실패/재확인 필요   |
| Source       | Controller/Service/Component |
| Test         | 관련 Test                      |

---

# 8. 화면 설명 작성 기준

운영 화면은 단순 Screenshot으로 설명하지 않는다.

모든 실제 Route와 화면에 대해 다음을 작성한다.

## 8.1 화면 기본 정보

| 항목내용           |                       |
| -------------- | --------------------- |
| 화면명            | 실제 표시명                |
| 메뉴 위치          | 1차 > 2차 > 3차 메뉴       |
| Route          | 실제 Route              |
| Component      | 실제 Frontend Component |
| 접근 Permission  | 실제 Permission         |
| 대상 역할          | 운영자/관리자/승인자           |
| 목적             | 이 화면에서 끝낼 수 있는 업무     |
| 진입 조건          | 필요한 사전 상태             |
| 주요 Backend API | 실제 호출 API             |

---

# 9. 검색 영역 작성 기준

검색 Field는 전수 작성한다.

| 컬럼필수          |                         |
| ------------- | ----------------------- |
| 화면 Label      | 필수                      |
| 내부 Field명     | 가능하면 작성                 |
| Type          | 필수                      |
| 필수 여부         | 필수                      |
| Default       | 필수                      |
| 허용 값          | 필수                      |
| 최소/최대값        | 해당 시                    |
| 날짜 기준         | 해당 시                    |
| Timezone      | 날짜/시간이면                 |
| 검색 방식         | exact/contains/prefix 등 |
| 다중 선택         | 여부                      |
| Data Scope 영향 | 필수                      |
| Masking 영향    | 해당 시                    |
| 입력 Validation | 필수                      |
| 비정상 입력 결과     | 필수                      |
| 관련 상태         | 해당 시                    |

특히 날짜 조건은 다음을 명시한다.

- 시작일 포함 여부
- 종료일 포함 여부
- Local Time 기준
- DB 저장 Timezone
- 하루의 시작·종료 처리 방식
- 최대 검색 기간
- 기본 검색 기간
- 기간 미지정 시 동작

---

# 10. 목록 Grid 작성 기준

화면의 모든 Column을 작성한다.

| 항목설명         |                 |
| ------------ | --------------- |
| Column 명     | 실제 화면 Label     |
| 데이터 의미       | 업무 의미           |
| Source Field | 가능한 경우          |
| 표시 형식        | 숫자/날짜/상태        |
| 정렬 가능        | Yes/No          |
| 기본 정렬        | 기준              |
| Null 표시      | 어떻게 표현되는가       |
| Masking      | 적용 여부           |
| Data Scope   | 영향              |
| 클릭 동작        | 상세 이동 등         |
| 상태별 강조       | 존재 시            |
| 운영자 판단 기준    | 이 값으로 무엇을 판단하는가 |

운영자가 Column을 보고 판단해야 하는 값에는 반드시 **판정 방법**을 작성한다.

예:

> `Last Attempt Time`이 오래됐다고 장애로 단정하지 않는다. 현재 상태가 RUNNING인지, Lease가 유효한지, Worker Heartbeat가 존재하는지 함께 확인한다.

---

# 11. 상세 화면 작성 기준

상세 Field도 전수 작성한다.

특히 다음 유형을 구분한다.

- 업무 Key
- 내부 ID
- 상태
- Version
- Attempt
- Created/Updated Time
- 요청자
- 승인자
- Reason
- Error Code
- Error Message
- Trace ID
- Correlation ID
- Idempotency Key
- External Reference
- Checksum
- Instance
- Worker
- Lease/Fence 정보

운영자가 직접 수정 가능한 필드와 읽기 전용 필드를 명확히 구분한다.

---

# 12. 버튼·Action 설명 기준

모든 Button/Action은 전수 작성한다.

| 항목내용                |               |
| ------------------- | ------------- |
| 버튼명                 | 화면 표시명        |
| 목적                  | 실제 수행 작업      |
| 필요한 상태              | 활성 조건         |
| 비활성 조건              | 왜 누를 수 없는가    |
| Permission          | 필요 권한         |
| 입력값                 | Modal/Form 입력 |
| Reason              | 필수 여부         |
| Approval            | 필요 여부         |
| Expected Version    | 사용 여부         |
| Idempotency         | 적용 여부         |
| Backend Command/API | 실제 구현         |
| 성공 상태               | 실행 후 상태       |
| 실패 상태               | 오류 시 상태       |
| Timeout 시 상태        | 중요            |
| 중복 클릭 영향            | 중요            |
| 재실행 가능 여부           | 중요            |
| Audit               | 기록 내용         |
| 복구 방법               | 실패 시 조치       |

특히 **버튼을 누르면 안 되는 조건**을 별도 작성한다.

---

# 13. 정상 처리 절차 작성 형식

모든 운영 작업은 번호가 있는 실제 절차로 작성한다.

예시 형식:

### 작업: 실패 건 상세 확인

**목적**

처리 실패 원인과 재처리 가능 여부를 판단한다.

**선행 조건**

- 대상 건 식별자 확보
- 조회 Permission 보유

**절차**

1. ADM의 해당 관리 화면으로 이동한다.
2. 업무 Key를 입력한다.
3. 조회한다.
4. 현재 `Status`를 확인한다.
5. `Attempt Count`를 확인한다.
6. `Last Error Code`를 확인한다.
7. `Correlation ID`를 확인한다.
8. 관련 Log를 조회한다.
9. 외부 연계 기능이면 상대 시스템 처리 여부를 확인한다.
10. 아래 재처리 판단표에 따라 후속 작업을 선택한다.

**정상 결과**

- 대상 건이 하나로 식별된다.
- 상태와 마지막 처리 결과가 확인된다.
- 후속 조치가 결정된다.

**확인 근거**

- 화면 상태
- Backend 상태
- Audit
- Log

---

# 14. 상태 전이 표는 필수다

상태가 존재하는 기능은 반드시 상태 전이 표를 작성한다.

| 현재 상태Action다음 상태실행 가능 역할실패 시 상태재실행 |
| ---------------------------------- |

상태명이 많거나 복잡한 경우 State Diagram을 추가한다.

단, 그림만 두지 않는다.

그림 아래에 반드시 표로 같은 내용을 검색 가능하게 제공한다.

---

# 15. 운영 상태 판정 기준

모든 기능에는 최소 세 종류의 상태 기준이 필요하다.

## 정상

어떤 값이면 정상인지.

## Warning

즉각 장애는 아니지만 확인이 필요한 조건.

## Critical

운영 조치 또는 Escalation이 필요한 조건.

예:

| 항목정상WarningCritical확인 방법 |
| ------------------------ |

수치 기준이 Source·설정·운영 정책에 존재하지 않으면 임의 값을 만들지 않는다.

그 경우 다음처럼 기록한다.

> 현재 구현에는 Warning/Critical 수치 기준이 정의되어 있지 않음. Metric은 조회 가능하나 임계치 정책은 미검증.

---

# 16. UNKNOWN\_RESULT를 독립적으로 다룬다

운영자 매뉴얼에서 가장 중요한 영역 중 하나다.

다음 상황을 명확히 구분한다.

- 요청 자체가 실패함
- 요청이 서버에 도달하지 않음
- 서버에서 처리 실패함
- 서버에서 처리 성공했지만 응답이 유실됨
- 외부 시스템에서 성공했지만 CPF가 응답을 받지 못함
- CPF와 외부 시스템 상태가 다름

운영자가 Timeout 화면만 보고 동일 요청을 다시 실행해서는 안 되는 경우를 명확히 작성한다.

UNKNOWN\_RESULT 절차에는 최소 다음이 필요하다.

1. 대상 요청 식별
2. Idempotency Key 확인
3. Attempt 확인
4. Correlation ID 확인
5. 내부 상태 확인
6. 외부 Reference 확인
7. 상대 시스템 상태 확인
8. Outbox/Inbox 확인
9. Audit 확인
10. Reconciliation 실행 여부 판단
11. 재처리 가능 여부 결정
12. 정상화 판정

---

# 17. Retry와 Reprocess를 구분한다

운영자가 혼동하지 않도록 독립적으로 정의한다.

## Retry

같은 처리 Attempt를 다시 시도하는 개념인지 확인한다.

## Reprocess

실패 또는 특정 상태의 업무 건을 새 처리 대상으로 다시 넣는 것인지 확인한다.

## Restart

중단된 Job/Process를 Checkpoint 또는 Metadata 기준으로 이어서 실행하는 것인지 확인한다.

## Reconcile

서로 다른 시스템 상태를 비교하여 실제 상태를 확정하는 것인지 확인한다.

## Compensation

이미 확정된 처리를 역처리하거나 보상 Transaction으로 정정하는 것인지 확인한다.

## Rollback

Config·Deployment·Version·변경 작업을 이전 기준으로 돌리는 것인지 확인한다.

각 기능마다 이 중 무엇을 지원하는지 명시한다.

---

# 18. 복구 선택표를 제공한다

운영자가 직접 판단할 수 있도록 다음 표를 기능마다 제공하는 것이 좋다.

| 상황RetryRestartReprocessReconcileCompensationRollback |   |   |   |   |   |   |
| ---------------------------------------------------- | - | - | - | - | - | - |
| Validation 실패                                        |   |   |   |   |   |   |
| 일시적 통신 실패                                            |   |   |   |   |   |   |
| 응답 유실                                                |   |   |   |   |   |   |
| 일부 처리                                                |   |   |   |   |   |   |
| 이미 외부 반영                                             |   |   |   |   |   |   |
| 설정 오류                                                |   |   |   |   |   |   |

실제 구현을 확인해 작성한다.

---

# 19. 부분 실패 처리

다건 작업은 반드시 부분 실패 가능성을 검토한다.

예:

- 100건 요청
- 80건 성공
- 15건 실패
- 5건 결과 불명확

이 경우 전체를 다시 실행하면 중복 처리가 발생할 수 있다.

따라서 다음을 설명한다.

- 성공 건 식별 방법
- 실패 건 식별 방법
- UNKNOWN\_RESULT 건 식별 방법
- 전체 Request ID
- 개별 Item ID
- 재처리 가능한 단위
- 중복 방지 방식
- Partial 상태
- 성공/실패 건수
- Reconciliation 기준
- 최종 완료 기준

---

# 20. 동시성 운영 절차

다음 구현이 존재하는 경우 운영자에게 의미를 설명한다.

- Optimistic Lock
- Expected Version
- Pessimistic Lock
- Lease
- Claim
- Fencing Token
- Leader Election
- Distributed Lock
- Job Lock

예:

> 상세 화면을 연 이후 다른 운영자가 먼저 상태를 변경한 경우 현재 화면의 Version과 서버 Version이 달라질 수 있다. 이 경우 기존 화면 값으로 재실행하지 말고 최신 데이터를 다시 조회한 후 작업 필요 여부를 다시 판단한다.

---

# 21. 권한 설명

Permission 이름만 나열해서는 안 된다.

| Permission가능한 작업불가능한 작업Data Scope위험도승인 |
| -------------------------------------- |

Role과 Permission을 분리한다.

- Role은 권한 묶음
- Permission은 실제 Action 허용 기준
- Data Scope는 볼 수 있는 데이터 범위

실제 구현이 이 구조와 다르면 Source에 맞춘다.

---

# 22. Data Scope

운영자가 “왜 다른 사람은 보는데 나는 안 보이지?”를 스스로 판단할 수 있어야 한다.

설명할 내용:

- 조직 기준
- 업무 영역 기준
- Tenant 기준
- 사용자 범위
- 전체 조회 권한
- 관리자 예외
- Export 적용 범위
- 집계 데이터 적용 방식

조회 결과가 0건일 때 Data Scope부터 확인해야 하는 경우를 명시한다.

---

# 23. Masking

다음을 구분한다.

- 목록 Masking
- 상세 Masking
- Export Masking
- Log Masking
- Audit Masking
- 관리자 원문 조회

각 항목에 대해 다음을 작성한다.

- 적용 Field
- Masking 예시
- 해제 가능 여부
- 필요한 Permission
- Reason
- Approval
- Audit

---

# 24. Reason

위험 작업은 Reason이 필요한지 확인한다.

문서에는 다음을 작성한다.

- 어떤 작업에서 필요한가
- 필수 여부
- 최소 길이
- 허용 형식
- 저장 위치
- 누가 조회할 수 있는가
- Audit과 연계되는가
- 승인자에게 보이는가

---

# 25. Approval

Approval이 존재하는 경우 전체 Life Cycle을 작성한다.

예:

> DRAFT → REQUESTED → APPROVED → EXECUTING → APPLIED

실제 상태 기준으로 작성한다.

반드시 다음 경우를 포함한다.

- 승인 요청
- 승인
- 반려
- 요청 취소
- 승인 후 대상 데이터 변경
- 승인 후 실행 실패
- 승인 후 일부 반영
- 승인 만료
- 재승인 조건

---

# 26. Audit

Audit는 “기록됨”으로 끝내지 않는다.

운영자가 직접 찾을 수 있도록 작성한다.

| Audit Field의미 |          |
| ------------- | -------- |
| Actor         | 작업자      |
| Action        | 수행 작업    |
| Target        | 대상       |
| Before        | 변경 전     |
| After         | 변경 후     |
| Reason        | 사유       |
| Approval      | 승인 정보    |
| Timestamp     | 수행 시각    |
| Result        | 결과       |
| Trace ID      | 관련 Trace |

실제 Audit Schema에 존재하는 Field만 작성한다.

---

# 27. 로그 설명

“로그를 확인한다”는 금지한다.

최소 다음을 설명한다.

| 항목내용           |            |
| -------------- | ---------- |
| 로그 위치          | 실제 위치      |
| Logger         | 가능한 경우     |
| 검색 Key         | 업무 ID      |
| Trace ID       | 존재 여부      |
| Correlation ID | 존재 여부      |
| Request ID     | 존재 여부      |
| Error Code     | 검색 방식      |
| 정상 예           | 어떤 패턴      |
| 실패 예           | 어떤 패턴      |
| 민감정보           | Masking 여부 |

운영 절차에서 사용할 검색 명령도 실제 환경에서 확인된 것만 작성한다.

---

# 28. Metric

Metric 설명에는 다음이 필요하다.

- Metric 이름
- 의미
- 단위
- Label
- 정상 판정에 사용하는 방식
- 증가하면 무엇을 의미하는가
- 감소하면 무엇을 의미하는가
- 0이어야 하는가
- 누적 Counter인지 Gauge인지
- Instance별인지 전체인지

---

# 29. Trace

분산 요청이면 최소 다음 흐름을 설명한다.

> Browser → ADM Backend → Application → Gateway → Target → Kafka/DB

그리고 운영자가 어느 ID를 이용하여 연결해서 봐야 하는지 작성한다.

---

# 30. Error Code Reference

Error Code 전체 또는 운영에 의미 있는 Error를 표로 제공한다.

| Error Code화면 메시지의미원인Retry운영 조치Escalation |
| ---------------------------------------- |

Source에서 확인하지 못한 Error를 만들지 않는다.

---

# 31. 장애 Runbook 작성 방식

Runbook은 다음 순서로 작성한다.

### 증상

운영자가 처음 보는 현상.

### 영향

사용자·업무·데이터에 미치는 영향.

### 즉시 확인

가장 먼저 확인할 것.

### 상세 확인

화면·명령·Metric·Log·DB.

### 원인 분류

가능한 원인들을 순서대로.

### 조치

원인별 실제 작업.

### 위험

조치 전 알아야 할 위험.

### 정상화 판정

복구 후 확인할 항목.

### 후속 조치

Audit, Incident, Root Cause 전달 등.

### Escalation

운영자가 처리하지 않고 개발/인프라/보안 조직에 넘겨야 하는 기준.

---

# 32. 증상 기반 Runbook Index

문서 후반에는 다음 형태의 역색인을 제공한다.

| 증상가능 원인첫 확인 위치Runbook |                   |          |         |
| --------------------- | ----------------- | -------- | ------- |
| 화면 5xx                | Backend 장애        | Health   | ADM-001 |
| 로그인 실패                | 인증/Session        | Security | SEC-001 |
| Job 멈춤                | Worker/Lease      | Batch    | BAT-003 |
| Consumer Lag          | Consumer 장애       | Kafka    | MSG-002 |
| 처리 중복 의심              | Retry/Idempotency | Attempt  | TX-004  |
| DB Timeout            | Pool/Lock         | DB       | DB-002  |

---

# 33. Runbook ID 체계

운영자가 전화나 장애 Ticket에서 쉽게 공유할 수 있도록 ID를 부여할 수 있다.

예:

- ADM-001
- APP-001
- DB-001
- MSG-001
- BAT-001
- GW-001
- SEC-001
- CFG-001
- DEP-001
- DR-001

다만 새로운 별도 문서를 만드는 것이 아니라 해당 공식 매뉴얼 내부에서 사용한다.

---

# 34. 정기 운영 업무

일일·주간·월간 구분 자체가 중요한 것이 아니라 실제 필요한 점검 주기를 Source와 운영 구조 기준으로 정의한다.

표 형식:

| 점검 항목주기확인 위치정상 기준이상 시 조치담당 |
| -------------------------- |

예상 가능한 항목:

- Application Health
- DB Connection
- Kafka Consumer
- Scheduler
- Batch 실패 건
- UNKNOWN\_RESULT
- 미대사 건
- Disk
- Memory
- Certificate
- Backup
- Audit
- 계정
- Permission
- Config Drift

실제 대상만 포함한다.

---

# 35. 명령어 Reference

운영에 CLI가 사용된다면 명령어를 기능별로 흩어 놓는 것에 그치지 말고 별도 Reference도 제공한다.

| 명령목적실행 위치필요 권한읽기/변경위험도관련 절 |
| -------------------------- |

명령별 상세 설명:

- 명령
- Argument
- Option
- Default
- 실행 예
- 예상 출력
- 오류 출력
- Exit Code
- 부작용
- 재실행 가능 여부
- Rollback
- 실행 후 검증

---

# 36. SQL 작성 기준

운영자가 직접 SQL을 실행하게 하는 경우 특히 엄격히 작성한다.

다음을 반드시 구분한다.

### 조회 SQL

상태 확인 목적.

### 변경 SQL

운영자가 실제 실행 가능한지 확인한다.

가능하면 관리 API나 공식 Tool이 있다면 임의 SQL 변경보다 해당 경로를 우선한다.

변경 SQL을 안내해야 한다면:

- DB Vendor
- 대상 Schema
- 대상 Table
- WHERE 조건
- 예상 건수
- Transaction
- Backup
- Commit
- Rollback
- 실행 전 SELECT
- 실행 후 SELECT
- Audit 영향
- 동시 실행 위험

을 작성한다.

---

# 37. Config Property Reference

플랫폼 운영자 매뉴얼의 Property는 최소 다음 컬럼을 가진다.

| 컬럼                   |
| -------------------- |
| Property Key         |
| Environment Variable |
| Type                 |
| Default              |
| Required             |
| 허용 범위                |
| Consumer             |
| Profile              |
| Secret 여부            |
| 변경 시 재기동             |
| Instance별/공통         |
| 잘못된 값의 증상            |
| 확인 명령                |
| 정상 결과                |
| Rollback 방법          |

---

# 38. 설정 변경 절차

Config 변경은 다음 흐름을 작성한다.

1. 현재 값 확인
2. 변경 목적 확인
3. 영향 범위 확인
4. 변경 대상 Instance 확인
5. 사전 Backup
6. 변경
7. 적용
8. 적용 결과 확인
9. Instance별 동일성 확인
10. Health 확인
11. 기능 Smoke Test
12. Metric/Log 확인
13. 문제 시 Rollback
14. Audit 확인

---

# 39. Partial Apply

여러 Instance에 변경을 적용할 수 있는 시스템은 부분 적용을 독립적으로 설명한다.

예:

- 5개 Instance 중 4개 적용
- 1개 실패

다음을 설명한다.

- 전체 상태
- Instance별 상태
- ACK/NACK
- Version
- Checksum
- LKG
- Drift
- Reconciliation
- Rollback 범위

---

# 40. 배포 운영

배포에는 최소 다음 내용을 작성한다.

## 배포 전

- Artifact
- Version
- Checksum
- Migration
- Config
- Secret
- Dependency
- Capacity
- Backup

## 배포 중

- Instance 상태
- Health
- Error
- Traffic

## 배포 후

- Version
- Health
- API
- DB
- Kafka
- Batch
- 주요 기능
- Metric
- Log

## 실패

- 중단 기준
- Rollback 조건
- DB Migration Rollback 가능 여부
- 이전 Artifact
- 이전 Config

---

# 41. Backup·Restore

Backup 설명은 “백업을 수행한다”로 끝내지 않는다.

필수 내용:

- Backup 대상
- 제외 대상
- Backup 방식
- 저장 위치
- 보존 정책
- Encryption
- 접근 권한
- Backup 성공 확인
- Restore 선행 조건
- Restore 절차
- Restore 후 데이터 검증
- Application 재연결
- Kafka/Event 영향
- Point-in-Time 가능 여부
- 미검증 Vendor

---

# 42. DR

DR 절차에는 다음이 필요하다.

- 장애 선언 기준
- 의사결정자
- Primary 상태 확인
- Secondary 상태 확인
- 데이터 차이
- 마지막 복제 위치
- Failover 순서
- Application 연결 변경
- Kafka/DB 연계
- 사용자 영향
- 전환 확인
- 복귀 조건
- Failback
- 대사
- Audit

---

# 43. 보안 사고 운영

보안 관련 운영자 매뉴얼은 최소 다음 상황을 다룬다.

- 계정 잠김
- 비정상 로그인
- Session 문제
- Permission 오설정
- Data Scope 오설정
- Masking 문제
- Secret 노출 의심
- Certificate 만료/오류
- Audit 이상
- 민감 데이터 Export

단, 실제 구현 범위에 없는 항목은 미구현 또는 미검증으로 표시한다.

---

# 44. 운영 위험도 분류

Action을 최소 다음 수준으로 분류하는 것이 좋다.

| 등급의미예 |                |                |
| ----- | -------------- | -------------- |
| R0    | 조회             | 상태 조회          |
| R1    | 낮은 영향 변경       | 개인 설정          |
| R2    | 업무 처리 변경       | 재처리            |
| R3    | 다수 사용자/서비스 영향  | Config Publish |
| R4    | 데이터/서비스 중대한 영향 | Rollback/DR    |

등급 자체는 CPF 정책으로 실제 채택할 경우에만 공식화한다.

---

# 45. “절대 먼저 하지 말아야 하는 작업” 영역

장애 상황별로 잘못된 초기 조치가 큰 문제를 만들 수 있으면 명시한다.

예:

### UNKNOWN\_RESULT

먼저 동일 요청을 재실행하지 않는다.

### Batch 중단

Job Metadata를 확인하기 전에 임의 신규 실행하지 않는다.

### Partial Apply

실패 Instance 확인 전에 전체 재배포하지 않는다.

### DB 불일치

원본 판정 전에 한쪽 데이터를 임의 수정하지 않는다.

실제 구현과 운영 정책에 근거해서 작성한다.

---

# 46. Escalation 기준

운영자가 모든 문제를 해결하려 하지 않도록 범위를 정의한다.

| 조건전달 대상전달 정보 |       |                           |
| ------------ | ----- | ------------------------- |
| Source 결함 의심 | 개발팀   | Trace, Error, 재현          |
| DB 장애        | DBA   | Query, Lock, Session      |
| 네트워크         | 인프라   | 대상, 시간, Endpoint          |
| 인증 문제        | 보안    | User, Result, Audit       |
| 외부 시스템       | 연계 담당 | Correlation, External Ref |

Escalation 시 필요한 Evidence를 정확히 작성한다.

---

# 47. 장애 전달용 Evidence Pack

운영자가 개발자에게 장애를 전달할 때 최소 다음 정보를 확보하도록 한다.

- 발생 시각
- 대상 환경
- 사용자
- 기능
- 업무 Key
- 상태
- Error Code
- Error Message
- Trace ID
- Correlation ID
- Request ID
- Attempt
- Instance
- Screenshot
- 관련 Log
- 수행한 조치
- 현재 영향
- 재현 여부

민감정보가 포함될 수 있는 값은 보안 정책을 따른다.

---

# 48. FAQ 작성 기준

FAQ는 실제 운영 질문 형태로 작성한다.

좋은 예:

### Q. 조회 결과가 없는데 데이터가 삭제된 것인가?

확인 순서를 설명한다.

### Q. 실패 건을 다시 실행해도 되는가?

상태와 Idempotency 기준을 설명한다.

### Q. Timeout이 발생했다. 다시 눌러도 되는가?

UNKNOWN\_RESULT 여부부터 확인하도록 설명한다.

### Q. 버튼이 비활성화되어 있다.

Permission, 상태, Expected Version 등을 확인하도록 설명한다.

단순 정의형 FAQ보다 **운영 판단형 FAQ**를 우선한다.

---

# 49. Source Evidence 표

각 주요 기능 끝에는 근거를 남긴다.

| 구분파일확인 내용  |       |              |
| ---------- | ----- | ------------ |
| Frontend   | 실제 경로 | Route/Button |
| Controller | 실제 경로 | API          |
| Service    | 실제 경로 | 처리 흐름        |
| Repository | 실제 경로 | 데이터          |
| Migration  | 실제 경로 | Schema       |
| Config     | 실제 경로 | Property     |
| Test       | 실제 경로 | 검증 범위        |

Line Number는 Source 변경에 따라 빨리 낡을 수 있으므로 필요성을 판단한다.

---

# 50. Test와 문서의 연결

운영 절차가 실제 어떤 Test로 검증되는지 연결한다.

다음 유형을 구분한다.

- Unit
- Integration
- API
- DB
- Browser
- Multi-instance
- Failover
- Fault Injection
- Recovery
- Security

실행하지 않은 Test를 성공했다고 기록하지 않는다.

---

# 51. 미검증 표시

다음은 명확히 구분한다.

- 완료
- 부분 구현
- 미구현
- 미검증
- 실패
- 재확인 필요

예:

> PostgreSQL 단일 Instance 기준 Source와 Test 확인. Oracle Runtime 실행은 미검증.

처럼 범위와 조건을 적는다.

---

# 52. Screenshot 사용 규칙

Screenshot은 보조 자료다.

Screenshot만으로 설명하지 않는다.

화면이 바뀌어도 검색 가능한 텍스트 정보가 남아 있어야 한다.

Screenshot 사용 시:

- 화면 전체 목적
- 강조 영역
- 번호
- 번호별 설명
- 실제 Route
- 화면 버전 차이 주의

를 함께 기록한다.

---

# 53. Diagram 사용 규칙

Architecture·Flow·State Diagram은 이해를 돕는 역할로 사용한다.

반드시 텍스트 또는 표를 병행한다.

Diagram 하나만 보고 운영 절차를 추론하게 하지 않는다.

---

# 54. 운영 시나리오 작성

기능별로 최소 정상 시나리오와 주요 실패 시나리오를 제공한다.

예:

### 정상 시나리오

“실패한 Batch Job을 확인하고 재처리”

### 실패 시나리오

“외부 호출 Timeout 후 UNKNOWN\_RESULT”

### 부분 실패 시나리오

“100건 처리 중 3건 실패”

### 동시성 시나리오

“두 운영자가 같은 건을 동시에 변경”

### 권한 시나리오

“조회는 가능하지만 Action Permission 없음”

---

# 55. 신규 운영자 교육 흐름

문서를 처음 읽는 사람은 다음 순서로 학습할 수 있어야 한다.

1. CPF와 대상 시스템이 무엇인지 이해
2. 운영자 역할 이해
3. 메뉴 구조 이해
4. 상태값 이해
5. 조회 연습
6. 정상 상태 판단
7. 일반 변경 작업
8. 승인 작업
9. 실패 건 확인
10. Retry와 Reprocess 차이
11. UNKNOWN\_RESULT
12. Reconcile
13. 장애 Runbook
14. Audit
15. 운영 인수인계

---

# 56. 매뉴얼 안의 실습

운영 매뉴얼에 EDU 성격의 실습이 포함된다면 다음까지 작성한다.

- 선행 환경
- 계정
- 권한
- 대상 데이터
- 시작 상태
- 수행 절차
- 입력값
- 예상 화면
- 예상 상태
- DB 변화
- Log
- Metric
- Audit
- 오류 재현
- Fault Injection
- 복구
- 최종 확인
- 초기 상태 복원

---

# 57. ADM 운영자 매뉴얼 추가 필수 조건

`04_ADM운영자매뉴얼.md`는 특히 화면의 실제 구현과 전수 대조한다.

각 Route마다 최소 다음을 작성한다.

- Menu
- Route
- Component
- Permission
- 검색 Field
- 기본값
- 목록 Column
- 상세 Field
- 상태
- Button
- 활성 조건
- 입력값
- Validation
- Reason
- Approval
- Expected Version
- Idempotency
- 응답 유실
- Partial Apply
- Retry
- Reprocess
- Reconcile
- Rollback
- Audit
- Error
- 관련 API

가상 화면을 만들지 않는다.

---

# 58. 플랫폼 운영자 매뉴얼 추가 필수 조건

`05_플랫폼운영매뉴얼.md`에는 최소 다음 범위를 깊게 다룬다.

- 지원 환경
- Artifact
- Checksum
- Directory
- 계정
- Property
- Environment Variable
- Profile
- Secret
- Certificate
- DB
- Migration
- Drift
- Kafka
- 설치
- 기동
- 종료
- Health
- 배포
- Instance
- Rolling 관련 실제 지원 범위
- Config 변경
- Partial Apply
- Log
- Metric
- Trace
- Capacity
- Backup
- Restore
- Upgrade
- Rollback
- DR
- DB 장애
- Kafka 장애
- Instance 장애
- Disk
- Memory
- Network
- Security

---

# 59. BZA 운영 영역 추가 필수 조건

`90_BZA매뉴얼.md` 운영 영역은 최소 다음을 다룬다.

- 초기 관리자
- 조직
- 직원
- 사용자
- Role
- Permission
- Data Scope
- 승인
- 위임
- 대결
- Attachment
- Notification
- Session
- Masking
- Audit
- Export
- Backup
- Restore
- Upgrade
- Rollback

각 관리 기능은 상태 변화와 운영 영향까지 설명한다.

---

# 60. Gateway 운영 영역 추가 필수 조건

`91_Gateway매뉴얼.md`는 최소 다음을 운영 관점으로 설명한다.

- Route
- Predicate
- Filter
- Rewrite
- Target
- Discovery
- Load Balancing
- Authentication
- Authorization
- HMAC
- Audience
- Body Hash
- Nonce
- SSRF
- TLS
- Timeout
- Retry
- Circuit Breaker
- Bulkhead
- Idempotency
- Attempt Ledger
- UNKNOWN\_RESULT
- Validation
- Version
- Checksum
- Approval
- Publish
- ACK
- NACK
- Partial Apply
- LKG
- Rollback
- Scale-out
- Drift
- Reconciliation
- Probe
- Health

특히 Route 변경은 **작성→검증→승인→배포→Instance별 반영 확인→오류→Rollback** 흐름을 작성한다.

---

# 61. 페이지 하단 “운영자 판단 요약”

긴 기능 설명 뒤에는 다음 Summary를 제공하면 좋다.

### 정상

무엇을 확인하면 되는가.

### 주의

어떤 조건이면 확인이 필요한가.

### 하지 말 것

무엇을 임의 실행하면 안 되는가.

### 실패 시

어디로 이동해야 하는가.

### 정상화 판정

어떤 조건을 만족해야 종료할 수 있는가.

---

# 62. 기능 간 Cross-Link

운영자는 한 기능에서 다른 기능으로 이동한다.

예:

> Batch 실패 → Kafka 확인 → UNKNOWN\_RESULT → Reconciliation → Audit

따라서 관련 절을 직접 연결한다.

다만 동일 내용을 여러 곳에 복사하지 않고 정본 절을 만들고 링크한다.

---

# 63. 문서에서 제공해야 하는 Reference 종류

운영자가 빠르게 찾아볼 수 있도록 최소 다음 Reference를 고려한다.

- Menu Reference
- Route Reference
- Permission Reference
- Status Reference
- Button/Action Reference
- Error Code Reference
- Command Reference
- Property Reference
- Metric Reference
- Log Search Key Reference
- Runbook Reference
- API Reference
- Audit Event Reference

---

# 64. “한눈에 보기” 표 작성 기준

한눈에 보기 표는 단순 나열이 아니라 운영 판단에 필요한 정보를 압축해야 한다.

나쁜 예:

| 기능설명 |          |
| ---- | -------- |
| 재처리  | 실패 건 재처리 |

좋은 예:

| Action가능한 상태Permission중복 위험승인실행 후 상태실패 시 |
| ---------------------------------------- |

---

# 65. 용어 사전

운영자가 개발 용어를 모를 수 있으므로 CPF에서 실제 사용하는 용어를 설명한다.

예:

- Idempotency
- Expected Version
- Attempt
- UNKNOWN\_RESULT
- Reconcile
- Compensation
- Lease
- Claim
- Fencing
- LKG
- Drift
- ACK/NACK
- Checkpoint
- Misfire
- Outbox
- Inbox

사전 정의뿐 아니라 운영 시 언제 등장하는지도 작성한다.

---

# 66. 장애별 의사결정 Tree

복잡한 장애는 질문 형태로 안내한다.

예:

### 처리 실패인가?

→ Yes

### 외부 시스템 호출이 있었는가?

→ Yes

### 외부 시스템 처리 여부를 확인할 수 있는가?

→ No

### 현재 상태가 UNKNOWN\_RESULT인가?

→ Yes

### Reconciliation 수단이 있는가?

→ Yes

→ Reconciliation 수행 후 결과에 따라 재처리 여부 결정

이런 Decision Tree는 특히 Retry가 위험한 기능에 유용하다.

---

# 67. 문서 품질 금지 사례

다음 상태에서는 해당 영역을 작성 완료로 판정하지 않는다.

- 기능 이름만 있음
- Screenshot만 있음
- API URL만 있음
- 버튼 설명만 있음
- 정상 케이스만 있음
- 오류 메시지만 있음
- “담당자 문의”로 끝남
- Source 경로만 있음
- Command만 있고 결과 설명 없음
- SQL만 있음
- Retry 가능 여부 없음
- UNKNOWN\_RESULT 고려 없음
- 부분 실패 고려 없음
- 권한 없음
- Audit 없음
- 복구 절차 없음
- 정상화 판정 없음
- 실제 화면과 Field가 다름
- 실제 Permission과 문서가 다름

---

# 68. 작성자가 반드시 검토해야 할 운영 질문

각 기능마다 다음 질문에 Yes라고 답할 수 있어야 한다.

| 질문확인                        |   |
| --------------------------- | - |
| 처음 보는 운영자가 기능을 찾을 수 있는가?    |   |
| 메뉴 위치를 알 수 있는가?             |   |
| 필요한 Permission을 알 수 있는가?    |   |
| 검색 조건을 알 수 있는가?             |   |
| 모든 Column 의미를 알 수 있는가?      |   |
| 모든 상태를 알 수 있는가?             |   |
| 버튼 활성 조건을 알 수 있는가?          |   |
| 실행 전 조건을 알 수 있는가?           |   |
| 실행 영향을 알 수 있는가?             |   |
| 정상 결과를 판정할 수 있는가?           |   |
| 오류를 구분할 수 있는가?              |   |
| Retry 판단이 가능한가?             |   |
| 중복 처리 위험을 알 수 있는가?          |   |
| UNKNOWN\_RESULT를 처리할 수 있는가? |   |
| 부분 실패를 처리할 수 있는가?           |   |
| Reprocess 판단이 가능한가?         |   |
| Reconcile 방법을 알 수 있는가?      |   |
| Rollback 조건을 알 수 있는가?       |   |
| Audit를 찾을 수 있는가?            |   |
| 정상화 여부를 확인할 수 있는가?          |   |
| Escalation 기준을 알 수 있는가?     |   |

---

# 69. 최종 검수 시 실제 사용자 관점 테스트

작성자가 문서를 읽는 테스트만 해서는 안 된다.

다음과 같은 질문을 임의로 뽑아 **목차 또는 검색으로 답을 찾을 수 있는지** 확인한다.

### 예 1

“사용자가 로그인할 수 없다고 연락했다.”

문서에서 원인 확인 절차를 찾을 수 있어야 한다.

### 예 2

“Batch Job이 FAILED다.”

재실행 여부 판단과 복구 절차까지 찾을 수 있어야 한다.

### 예 3

“외부 호출이 Timeout이다.”

무조건 Retry하면 안 된다는 판단을 문서에서 할 수 있어야 한다.

### 예 4

“Gateway 설정이 일부 Instance에만 적용됐다.”

Partial Apply 대응을 찾을 수 있어야 한다.

### 예 5

“내 화면에는 이 버튼이 없다.”

Permission·상태·Data Scope를 확인할 수 있어야 한다.

### 예 6

“어제 누가 이 설정을 변경했는가?”

Audit 검색 방법을 찾을 수 있어야 한다.

---

# 70. 운영자 매뉴얼 Definition of Done

운영자 매뉴얼의 해당 기능은 최소 다음 조건을 충족해야 완료로 판정할 수 있다.

1. 실제 구현과 문서가 일치한다.
2. 실제 Route를 확인했다.
3. 실제 Permission을 확인했다.
4. 검색 Field를 확인했다.
5. Column을 확인했다.
6. 상세 Field를 확인했다.
7. Action을 확인했다.
8. 상태를 확인했다.
9. 정상 흐름이 설명되어 있다.
10. 실패 흐름이 설명되어 있다.
11. Timeout을 검토했다.
12. 응답 유실을 검토했다.
13. 동시성을 검토했다.
14. Retry를 검토했다.
15. Restart를 검토했다.
16. Reprocess를 검토했다.
17. Reconcile을 검토했다.
18. Compensation을 검토했다.
19. Rollback을 검토했다.
20. 부분 실패를 검토했다.
21. Permission을 설명했다.
22. Data Scope를 설명했다.
23. Masking을 설명했다.
24. Reason을 설명했다.
25. Approval을 설명했다.
26. Audit 확인법이 있다.
27. Log 확인법이 있다.
28. Metric 확인법이 있다.
29. Trace 확인법이 있다.
30. 오류별 운영 조치가 있다.
31. 장애 Runbook이 있다.
32. 정상화 판정 기준이 있다.
33. Escalation 기준이 있다.
34. Source Evidence가 있다.
35. Test 근거가 있다.
36. 실제 실행하지 않은 항목은 미검증으로 표시했다.
37. 신규 운영자가 문서만으로 작업을 따라갈 수 있다.
38. Source를 직접 읽지 않아도 일상 운영이 가능하다.
39. 오류 발생 시 개발자에게 질문하기 전에 수행할 1차 진단이 가능하다.
40. 위험한 중복 실행이나 임의 복구를 방지할 정보가 있다.

---

# 71. 운영자 매뉴얼 작성의 최종 품질 기준

운영자 매뉴얼은 다음 수준을 목표로 한다.

**“무엇이 있는지 알려주는 문서”가 아니라**
**“실제 운영 중 무엇을 보고 어떻게 판단하고 무엇을 실행하고 실패하면 어떻게 정상 상태까지 돌려놓는지를 안내하는 문서”여야 한다.**

신규 운영자가 다음 과정을 혼자 수행할 수 있어야 한다.

> 업무 찾기
> → 메뉴 찾기
> → 권한 확인
> → 현재 상태 확인
> → 정상/비정상 판정
> → Action 가능 여부 판단
> → 작업 수행
> → 결과 확인
> → 실패 분류
> → 중복 위험 판단
> → Retry/Restart/Reprocess/Reconcile/Compensation/Rollback 선택
> → 복구 수행
> → 상태·데이터·Log·Metric·Audit 대조
> → 정상화 판정
> → 필요 시 Evidence와 함께 Escalation

문서가 이 흐름 중 하나를 끊는다면 해당 부분은 추가 작성이 필요한 것으로 판단한다.

---

# 72. 가장 중요한 작성 원칙 요약

운영자에게 다음 여섯 가지를 남기지 않는 것을 기본 원칙으로 한다.

**“어디서 하지?”**

→ Menu·Route·명령 위치를 제공한다.

**“이 값이 뭐지?”**

→ Field·Column·상태·Error의 의미를 제공한다.

**“지금 정상인가?”**

→ 객관적인 정상·이상 판정 기준을 제공한다.

**“이거 다시 해도 되나?”**

→ Retry·Idempotency·UNKNOWN\_RESULT·중복 위험 판단 기준을 제공한다.

**“문제가 났는데 어떻게 되돌리지?”**

→ Restart·Reprocess·Reconcile·Compensation·Rollback 절차를 제공한다.

**“이제 정상화된 건가?”**

→ 화면뿐 아니라 상태·DB·외부 시스템·Log·Metric·Audit 중 실제 확인 수단을 이용한 종료 판정 기준을 제공한다.

이 여섯 질문을 운영자가 문서만으로 해결할 수 있게 만드는 것을 CPF 운영자 매뉴얼의 공통 작성 기준으로 한다.