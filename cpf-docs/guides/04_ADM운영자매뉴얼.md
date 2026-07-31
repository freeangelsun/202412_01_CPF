# CPF ADM 운영자 매뉴얼

> **기준 Repository** `freeangelsun/202412_01_CPF` · **기준 Branch** `master` · **문서 작성 기준 SHA** `d31bd127aa12bb9368933216642a5a9d25bd0bfd`  
> **문서 목적** ADM 화면을 이용해 플랫폼 상태를 조회하고 승인된 제어·대사·감사 업무를 수행하는 절차를 설명한다.  
> **주요 독자** 플랫폼 운영관리자, 승인자, 감사자, 보안 운영자, 장애 대응 담당자  
> **완료 결과** 독자가 메뉴별 조회·판단·조치·승인·결과 확인과 장애 초기 대응을 안전하게 수행한다.

## 0. 문서 사용 계약

이 문서는 ADM 화면 사용 절차를 다룬다. JVM·YAML·DB·배포·OS 운영은 05_플랫폼운영매뉴얼을 따른다. 실제 메뉴명과 Route는 최신 Frontend Route Registry와 권한 Catalog를 기준으로 확인한다.

문서의 예제는 다음 순서로 읽는다.

1. **제품 계약** — CPF가 보장해야 하는 규칙이다.
2. **현재 구현 확인** — 표에 제시한 Source·설정·API·SQL 경로를 최신 `master`에서 확인한다.
3. **실행 절차** — 명령을 실제 환경에서 실행하고 Exit Code와 결과를 확인한다.
4. **오류·복구** — 정상 경로만 보지 않고 중단·응답 유실·중복·부분 실패를 확인한다.
5. **Evidence** — 실행한 기준 SHA, 환경, 명령, 시작·종료 시각, Exit Code와 Sanitized 결과를 남긴다.

상태는 `완료`, `부분 구현`, `미구현`, `미검증`, `실패`, `재확인 필요`만 사용한다.


## 1. ADM 운영자와 플랫폼 운영자의 차이

| 구분 | ADM 운영자 | 플랫폼 운영자 |
|---|---|---|
| 주 도구 | ADM 화면과 운영 API | Shell·PowerShell·Service·DB·배포 도구 |
| 설정 | 승인된 설정 Version 조회·적용·대사 | YAML·환경변수·Secret·JVM·OS 설정 |
| 배포 | 배포 상태·승인·결과 조회 | Artifact 설치·Rolling·Rollback 실행 |
| 장애 | 거래·Instance·Job·Route 상태 판단과 조치 | Process·DB·Kafka·Network·Disk 복구 |
| 로그 | 검색·마스킹·반출 승인 | Collector·파일·보존·용량 운영 |

![ADM 위험 조치 운영 절차](../assets/guides/cpf-adm-operation-flow.svg)

## 2. 역할과 권한

대표 역할은 제품 정책에 맞게 분리한다.

- 조회 운영자 — 상태·로그·Trace·통계 조회
- 조치 운영자 — 허용된 Start·Stop·Drain·Restart 요청
- 승인자 — 위험 조치 검토·승인·반려
- 감사자 — 감사 Timeline·반출·승인 이력 조회
- 보안 운영자 — Session·권한·Secret Reference·Certificate 상태
- 비상 운영자 — 승인된 Break-glass 절차만 사용

메뉴가 보인다는 이유로 실행 권한이 있는 것은 아니다. 각 Action에서 서버 권한을 재검증한다.

## 3. 로그인과 Session

### 3.1 로그인 전

- 올바른 Environment와 ADM 주소인지 확인한다.
- Browser 인증서 경고를 무시하지 않는다.
- 공유 계정 사용 금지
- Screen Recording·Clipboard 정책 확인

### 3.2 Session

- Idle·Absolute Timeout
- 동시 Session 정책
- 권한 변경·퇴직·강제 종료 뒤 Session 만료
- CSRF 오류 시 무조건 재시도하지 말고 로그인 상태와 시간 동기 확인

### 3.3 로그아웃

작업 종료 뒤 로그아웃하고 다운로드한 민감 Artifact를 보존 정책에 따라 제거한다.

## 4. 화면 공통 사용법

### 4.1 Environment 표시

화면 상단의 Environment, Region, SystemCode, 사용자 Subject를 항상 확인한다. 운영·Staging 혼동을 방지하기 위해 색상만이 아니라 텍스트로 표시해야 한다.

### 4.2 검색

- 조회 기간과 최대 범위
- Service·Instance·SystemCode
- 상태와 Version
- transactionId·traceId·operationId
- Page Size와 Sort
- 참조 Catalog의 Capability

조회 결과의 **수집 시각**과 **Source Runtime**을 확인한다.

### 4.3 상태 Badge

상태를 색상만으로 판단하지 않는다. 상태 Text, 수집 시각, 원인 Code, 마지막 정상 시각을 본다.

### 4.4 Version과 Stale

상세 화면을 오래 열어둔 뒤 조치하지 않는다. Command 전에 최신 상태와 Version을 다시 조회한다.

## 5. 권장 메뉴 구조

실제 표시 이름은 Route Registry에 따라 달라질 수 있지만 기능 영역은 다음 책임을 가진다.

1. 대시보드
2. 서비스·인스턴스
3. 거래·호출 시도
4. 로그·Trace
5. Batch 실행
6. 설정·정책
7. Artifact·배포
8. 승인함
9. 감사·반출
10. 보안·Session
11. 선택형 Gateway

## 6. 대시보드

### 확인 항목

- Service와 Instance 총수·DOWN·DEGRADED
- Readiness 저하
- 최근 배포·설정 변경
- Batch 실패·중단·Unknown
- Gateway Instance 적용 불일치
- Kafka·DB·Collector 상태
- Pending Approval
- Reconcile 대기 건수

### 판단 원칙

Dashboard 집계만으로 장애를 확정하지 않는다. 상세 Source와 수집 시각을 확인한다.

## 7. 서비스·인스턴스

### 7.1 서비스 목록

검색: Service ID, SystemCode, Environment, Status, Version, Capability.

확인:

- Endpoint 수와 Instance 수
- Ready/Not Ready
- 배포 Version 분포
- 마지막 Heartbeat
- 최근 설정 Version
- 의존 서비스 상태

### 7.2 인스턴스 상세

- instanceId, host, port, zone
- start time, uptime
- liveness, readiness, functional probe
- active request, connection, queue
- artifact/config version
- JVM·resource 요약
- 최근 상태 변경과 Audit

### 7.3 Drain

1. 대상과 Environment를 재확인한다.
2. 현재 Active Request와 대체 Ready Instance를 확인한다.
3. Reason과 Change/Incident ID를 입력한다.
4. 승인 필요 여부를 확인한다.
5. Drain 요청 뒤 operationId를 기록한다.
6. Active Request가 감소하고 Routing에서 제외되는지 확인한다.
7. Timeout·Unknown이면 Owner 상태를 재조회한다.
8. 완료 뒤 Audit Timeline을 확인한다.

### 7.4 Resume

원인 제거, Health 정상, Version 정합성, Probe 성공을 확인한 뒤 Resume한다.

## 8. 거래·호출 시도

### 8.1 거래 조회

주요 Key:

- transactionId
- operationId
- idempotencyKey Hash
- 사용자·시스템 Subject
- 업무 Key의 마스킹 값
- 시작·종료·현재 상태

### 8.2 Attempt

Retry·Failover를 최종 한 행으로 보지 않는다. 각 Attempt의 대상, 시작·종료, HTTP/기술 결과, Timeout, Retry Policy, Unknown 여부를 확인한다.

### 8.3 결과 불명

`UNKNOWN_RESULT` 대응:

1. Blind Retry 금지
2. 요청 Hash·Idempotency Key 확인
3. 상대 Status API·Callback·대사 자료 조회
4. Local DB·Outbox·Inbox·Attempt 확인
5. 실제 결과 확정
6. 재처리·보상·운영 확정 선택
7. Reason·근거·승인·Audit 기록

## 9. 로그·Trace

### 9.1 로그 검색

- transactionId·traceId·operationId·attemptId
- systemCode·serviceId·instanceId
- 기간·Level·Error Code
- 민감정보 Masking 상태

### 9.2 Trace

- 진입 Span
- Local/Remote 호출
- Kafka Producer·Consumer
- DB·외부 API
- Batch Job·Step 연결
- Timeout·Retry·Circuit 상태

Trace가 없으면 로그와 Attempt 원장으로 연결하되 ID 불일치를 기록한다.

### 9.3 로그 반출

1. 검색 결과를 먼저 검토한다.
2. 필요한 최소 범위만 선택한다.
3. Reason과 Ticket을 입력한다.
4. 승인 또는 권한을 확인한다.
5. 서버가 생성한 Masked Artifact Hash와 만료를 확인한다.
6. 다운로드 뒤 안전한 저장 위치와 삭제 기한을 준수한다.
7. 원문이 노출됐으면 즉시 보안 사고 절차를 따른다.

## 10. Batch 운영

상세 개발 구조는 02 매뉴얼을 따른다. 운영 화면에서는 Spring Batch 상태가 정본이다.

### 10.1 목록

- Definition·Version
- JobInstance·JobExecution ID
- Business Date·Parameters
- Status·Exit Status
- Start·End·Duration
- Step·Partition 진행률
- Read·Write·Skip·Rollback Count
- Worker·Agent
- Approval·Reason

### 10.2 Start

- 승인된 Definition Version
- Parameter Schema·Masking
- 중복 JobInstance
- 동시 실행 정책
- Target·Topology
- Artifact Version
- Reason·Approval

### 10.3 Stop

Stop 요청을 즉시 완료로 보지 않는다. JobExecution이 실제 `STOPPED`로 전이되고 Worker가 중단됐는지 확인한다.

### 10.4 Restart

- 같은 JobInstance인가
- Restart 가능한 상태인가
- Definition·Artifact·File Hash가 동일한가
- 완료 Step 재실행 정책
- Unknown Side Effect 대사

### 10.5 Abandon

Abandon은 재시작 불가 상태로 만드는 고위험 조치다. 영향, 대체 Reprocess, 승인과 근거가 필요하다.

### 10.6 Partition 실패

실패 Partition만 선택 Restart할 수 있는지 확인하고 범위 중복·누락과 Stale Worker 결과를 검사한다.

## 11. 설정·정책

### 조회

- 설정 Key·Scope·Environment
- 현재 Version·적용 Version
- 값 분류와 Secret Reference
- Consumer별 ACK·NACK
- Drift·Partial Apply
- 마지막 변경·승인

### 적용

1. 변경 Preview와 영향 Consumer를 확인한다.
2. Secret 원문이 화면에 노출되지 않는지 확인한다.
3. Expected Version과 Approval을 확인한다.
4. 게시 뒤 Consumer별 적용 결과를 본다.
5. Partial이면 전체 성공으로 처리하지 않는다.
6. Rollback 또는 Reconcile을 수행한다.

## 12. Artifact·배포

- Source SHA
- Artifact Version·Hash·Signature
- SBOM·License·Vulnerability 결과
- 배포 대상과 Wave
- Instance별 적용 Version
- Readiness와 Probe
- 실패 Instance·Rollback 상태

ADM은 배포 상태를 조회·승인할 수 있지만 실제 OS·Service·DB 명령은 플랫폼 운영 매뉴얼을 따른다.

## 13. 승인함

### 승인자가 확인할 항목

- 요청자와 역할
- 대상 Environment·Resource
- Action과 영향
- 현재 Version·Expected Version
- Reason·Ticket
- Request Hash
- 만료와 실행 가능 시간
- 이전 유사 조치·사고
- Rollback·Reconcile 방법

승인 뒤 대상 상태가 바뀌면 재승인을 요구한다.

## 14. 감사

검색 축:

- actor·approver
- action·target
- operationId·transactionId
- result·errorCode
- 기간·Environment
- 민감정보 조회·다운로드

감사 Chain·Hash 검증 실패는 일반 검색 오류로 숨기지 않고 보안 담당자에게 전달한다.

## 15. 보안·Session 운영

- 사용자 Session 목록과 마지막 활동
- Force Logout
- Role 변경 재검증
- Concurrent Session 위반
- CSRF·Session Fixation Event
- Cookie 정책 상태
- Secret Reference·Rotation 상태
- Certificate 만료

Secret 원문 조회 기능을 일반 메뉴로 제공하지 않는다.

## 16. 선택형 Gateway 운영

Gateway가 설치된 환경에서만 메뉴를 노출한다. 상세 절차는 [91_게이트웨이매뉴얼](91_게이트웨이매뉴얼.md)을 따른다.

ADM에서는 다음을 확인한다.

- Route·Binding·Server Group Version
- Candidate·Active·Last Known Good
- Instance ACK/NACK
- Probe·Connection Test
- Configuration Mismatch
- Attempt·Unknown·Rollback

## 17. 위험 조치 표준 절차

### 공통 10단계

1. **대상 확인** — Environment, Resource ID, 현재 Version과 상태를 재확인한다.
2. **영향 확인** — 사용자·거래·다른 Instance·Batch·Route에 미치는 영향을 본다.
3. **대체 경로 확인** — Ready Instance, Rollback, Reprocess, Manual Operation 가능성을 확인한다.
4. **사유 기록** — Change·Incident·Ticket과 구체적인 실행 이유를 입력한다.
5. **권한·승인** — 필요 Permission, 작성자·승인자 분리와 승인 만료를 확인한다.
6. **Preview** — 실행 명령과 대상 Version·Request Hash를 검토한다.
7. **실행** — 한 번만 요청하고 operationId를 기록한다.
8. **상태 확인** — 요청 응답이 아니라 Owner Runtime의 실제 상태를 확인한다.
9. **대사·복구** — Unknown·Partial이면 Reconcile·Rollback·보상을 수행한다.
10. **감사 종료** — 결과, 근거, 후속 조치와 Evidence를 Timeline에 남긴다.


## 18. 사고 초기 15분

### 0~3분

- Environment와 영향 서비스 확인
- transactionId·traceId·operationId 수집
- 최근 배포·설정·승인 변경 확인
- 추가 위험 조치 중단

### 3~7분

- Service·Instance Readiness
- DB·Kafka·Network·Disk 요약
- Batch·Gateway·Session 영향
- 결과 불명과 중복 Side Effect 위험

### 7~12분

- Drain·Traffic 차단·Scheduler Pause 등 최소 안전 조치
- Owner와 플랫폼 운영자 호출
- 필요한 로그·Trace·Artifact 보존

### 12~15분

- Incident ID와 책임자
- 다음 판단 시각
- 복구·Rollback 후보
- 사용자·감사 커뮤니케이션

## 19. 메뉴별 문서 작성 표준

각 실제 메뉴는 다음 항목을 가져야 한다.

1. 메뉴 목적
2. 대상 사용자와 Permission
3. 진입 Route
4. 검색 조건과 Default
5. 목록 Column과 상태 의미
6. 상세 Field와 Source
7. 가능한 Action
8. 사유·승인·재인증
9. 정상 결과
10. 401·403·409·422·500·Unknown·Partial
11. 복구·대사
12. 감사·Evidence
13. Backend API·Frontend Source 경로

## 20. 운영 실습

### 실습 A — Instance Readiness 저하

- Dashboard에서 저하 확인
- Instance 상세·Probe·Version 확인
- 대체 Ready Instance 확인
- Drain 요청과 operationId 기록
- 플랫폼 운영자에게 Process·DB·Network 점검 전달
- Resume 전 Readiness·Functional Probe 재검증

### 실습 B — Batch Worker 중단

- JobExecution·StepExecution·Partition 확인
- Worker Lease와 Fencing Token 확인
- Stale 결과 차단 여부 확인
- Restart 또는 Reprocess 판단
- 완료 뒤 처리 건수·중복 Side Effect 확인

### 실습 C — 결과 불명 외부 연계

- 거래·Attempt 조회
- Blind Retry 차단
- 상대 Status·대사 확인
- 최종 상태 확정
- 필요 시 보상 승인
- Audit Timeline 종료

## 21. 운영자 체크리스트

- [ ] 항상 Environment와 현재 Version을 확인했다.
- [ ] 조회 결과의 Source와 수집 시각을 확인했다.
- [ ] 위험 조치 전에 영향·대체 경로·Rollback을 확인했다.
- [ ] Reason·Ticket·Approval을 정확히 입력했다.
- [ ] 동일 Command를 반복 클릭하지 않고 operationId를 추적했다.
- [ ] 요청 응답과 실제 Owner 상태를 구분했다.
- [ ] UNKNOWN_RESULT와 Partial을 전체 성공·실패로 축약하지 않았다.
- [ ] 민감정보 반출 범위와 만료·삭제 정책을 지켰다.
- [ ] 조치 뒤 Audit Timeline과 Evidence를 확인했다.
- [ ] OS·DB·배포 복구가 필요하면 05 플랫폼 운영 매뉴얼로 인계했다.
