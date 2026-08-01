# CPF Reference EDU Package Index

> 이 문서는 `cpf-reference` 단일 모듈에 포함된 실행형 EDU 예제를 기능별 Package로 찾기 위한 Source Index다.
> Requirement ID는 추적용이며 Package 이름에는 사용하지 않는다. 생성형 업무 도메인과 제품 BZA 모듈에 의존하지 않는다.

## 고정 경계

- 필수 온라인 예제: `com.cpf.reference.online`
- 필수 플랫폼 예제: `com.cpf.reference.platform`
- 공통 실행·복구 원장: `com.cpf.reference.edu.runtime`, 중앙 `refDB`의 `CPF_EDU_*`
- REF 외부기관 모의 연계: `com.cpf.reference.edu.counterparty`
- 선택 Batch Pack: `com.cpf.reference.batch`, 중앙 `refDB`의 `CPF_REF_BAT_*`
- 선택 운영 화면 시나리오: `com.cpf.reference.optional.operations`
- 선택 Back-office 시나리오: `com.cpf.reference.optional.backoffice`
- 선택 Gateway Simulator: `com.cpf.reference.optional.gateway`

## 제거 원칙

Batch를 사용하지 않으면 `com.cpf.reference.batch`, `edu/batch`, V94/U94 Batch SQL Pack을 함께 제외한다. 온라인·플랫폼·공통 EDU는 Batch Package나 `CPF_REF_BAT_*` Table을 참조하지 않는다. 다른 선택 Pack도 해당 Feature Toggle과 Package/Resource 묶음으로 제거할 수 있다.

## Requirement → Source

| Requirement | 기능 | Package | Handler | Consumer | Feature Pack | DB |
|---|---|---|---|---|---|---|
| `EDU-DEV-01` | Generator 기반 신규 업무 영역 생성 | `com.cpf.reference.online.generator.domain` | `EduDev01Handler.java` | `PROCESS` | `core` | `refDB` |
| `EDU-DEV-02` | 권한·범위가 적용된 목록·상세 조회 | `com.cpf.reference.online.query.scoped` | `EduDev02Handler.java` | `JDBC_QUERY` | `core` | `refDB` |
| `EDU-DEV-03` | 등록·수정·상태 변경과 감사 | `com.cpf.reference.online.command.audit` | `EduDev03Handler.java` | `JDBC_COMMAND` | `core` | `refDB` |
| `EDU-DEV-04` | 동시 수정과 예상 Version 충돌 | `com.cpf.reference.online.concurrency.optimisticlock` | `EduDev04Handler.java` | `JDBC_COMMAND` | `core` | `refDB` |
| `EDU-DEV-05` | 지급 등록 멱등성·응답 유실·결과 대사 | `com.cpf.reference.online.idempotency.payment` | `EduDev05Handler.java` | `JDBC_COMMAND` | `core` | `refDB` |
| `EDU-DEV-06` | 같은 애플리케이션·분리 서비스 호출 동등성 | `com.cpf.reference.online.servicecall.topology` | `EduDev06Handler.java` | `HTTP` | `core` | `refDB` |
| `EDU-DEV-07` | Kafka Outbox·Inbox·중복 소비·재처리 | `com.cpf.reference.online.messaging.outboxinbox` | `EduDev07Handler.java` | `OUTBOX` | `core` | `refDB` |
| `EDU-DEV-08` | 파일 업로드·검사·첨부·다운로드 | `com.cpf.reference.online.file.attachment` | `EduDev08Handler.java` | `FILE` | `core` | `refDB` |
| `EDU-DEV-09` | 외부 REST 신용조회와 결과 미확정 | `com.cpf.reference.online.counterparty.rest` | `EduDev09Handler.java` | `HTTP` | `core` | `refDB` |
| `EDU-DEV-10` | 고정길이 전문 기관 이체 | `com.cpf.reference.online.counterparty.fixedwidth` | `EduDev10Handler.java` | `HTTP` | `core` | `refDB` |
| `EDU-DEV-11` | 권한·데이터 범위·개인정보 가림·감사 | `com.cpf.reference.online.security.authorization` | `EduDev11Handler.java` | `JDBC_COMMAND` | `core` | `refDB` |
| `EDU-DEV-12` | Cache·기능 전환·Secret 교체 | `com.cpf.reference.online.runtime.featuremanagement` | `EduDev12Handler.java` | `JDBC_COMMAND` | `core` | `refDB` |
| `EDU-DEV-13` | 알림·비동기 내보내기·다운로드 감사 | `com.cpf.reference.online.notification.export` | `EduDev13Handler.java` | `OUTBOX` | `core` | `refDB` |
| `EDU-DEV-14` | Oracle·PostgreSQL·MariaDB 동일 의미 Migration | `com.cpf.reference.online.database.migration` | `EduDev14Handler.java` | `PROCESS` | `core` | `refDB` |
| `EDU-DEV-15` | 지급 업무 장애 주입·복구·운영 인계 | `com.cpf.reference.online.resilience.recovery` | `EduDev15Handler.java` | `JDBC_COMMAND` | `core` | `refDB` |
| `EDU-DEV-16` | 대용량 목록 검색·정렬·Cursor Paging | `com.cpf.reference.online.query.cursor` | `EduDev16Handler.java` | `JDBC_QUERY` | `core` | `refDB` |
| `EDU-DEV-17` | 대량 등록 사전검증·부분 오류 보고·재업로드 | `com.cpf.reference.online.file.bulkimport` | `EduDev17Handler.java` | `JDBC_COMMAND` | `core` | `refDB` |
| `EDU-DEV-18` | 논리 삭제·복원·보존기간 만료 | `com.cpf.reference.online.lifecycle.softdelete` | `EduDev18Handler.java` | `JDBC_COMMAND` | `core` | `refDB` |
| `EDU-DEV-19` | 기준일·유효기간이 있는 기준정보 | `com.cpf.reference.online.reference.effectiveperiod` | `EduDev19Handler.java` | `JDBC_COMMAND` | `core` | `refDB` |
| `EDU-DEV-20` | 다단계 고객 업무 상태기계와 취소·재개 | `com.cpf.reference.online.workflow.statemachine` | `EduDev20Handler.java` | `JDBC_COMMAND` | `core` | `refDB` |
| `EDU-DEV-21` | Transactional Outbox 게시 지연·재시작 | `com.cpf.reference.online.messaging.transactionaloutbox` | `EduDev21Handler.java` | `OUTBOX` | `core` | `refDB` |
| `EDU-DEV-22` | 서비스 간 Saga 보상·수동 확정 | `com.cpf.reference.online.workflow.saga` | `EduDev22Handler.java` | `OUTBOX` | `core` | `refDB` |
| `EDU-DEV-23` | 공통 입력검증·오류 계약·OpenAPI 일치 | `com.cpf.reference.online.contract.validation` | `EduDev23Handler.java` | `JDBC_COMMAND` | `core` | `refDB` |
| `EDU-DEV-24` | 장시간 비동기 Operation 조회·취소 | `com.cpf.reference.online.asyncoperation.lifecycle` | `EduDev24Handler.java` | `JDBC_COMMAND` | `core` | `refDB` |
| `EDU-DEV-25` | Webhook Callback 서명·재전송·Replay 방지 | `com.cpf.reference.online.counterparty.webhook` | `EduDev25Handler.java` | `HTTP` | `core` | `refDB` |
| `EDU-DEV-26` | SFTP 수신·송신·완료 파일 원자 처리 | `com.cpf.reference.online.file.sftp` | `EduDev26Handler.java` | `FILE` | `core` | `refDB` |
| `EDU-DEV-27` | SOAP·XML 외부기관 연계와 Fault 처리 | `com.cpf.reference.online.counterparty.soap` | `EduDev27Handler.java` | `HTTP` | `core` | `refDB` |
| `EDU-DEV-28` | 대용량 Multipart 업로드·중단 재개 | `com.cpf.reference.online.file.multipart` | `EduDev28Handler.java` | `FILE` | `core` | `refDB` |
| `EDU-DEV-29` | 악성코드 검사·격리·승인 해제 | `com.cpf.reference.online.file.quarantine` | `EduDev29Handler.java` | `FILE` | `core` | `refDB` |
| `EDU-DEV-30` | Object Storage 보존·버전·법적 보류 | `com.cpf.reference.online.file.objectstorage` | `EduDev30Handler.java` | `FILE` | `core` | `refDB` |
| `EDU-DEV-31` | 다중 채널 알림 선호·재시도·대체 채널 | `com.cpf.reference.online.notification.multichannel` | `EduDev31Handler.java` | `OUTBOX` | `core` | `refDB` |
| `EDU-DEV-32` | 개인정보 암호화·Tokenization·Key Rotation | `com.cpf.reference.online.security.cryptography` | `EduDev32Handler.java` | `JDBC_COMMAND` | `core` | `refDB` |
| `EDU-DEV-33` | 인증 Token 만료·갱신·폐기·세션 강제 종료 | `com.cpf.reference.online.security.session` | `EduDev33Handler.java` | `JDBC_COMMAND` | `core` | `refDB` |
| `EDU-DEV-34` | API 사용량 제한·고객별 Quota·초과 처리 | `com.cpf.reference.online.api.quota` | `EduDev34Handler.java` | `JDBC_COMMAND` | `core` | `refDB` |
| `EDU-DEV-35` | 기능 전환 Canary·Kill Switch·사용자 Segment | `com.cpf.reference.online.runtime.featuretoggle` | `EduDev35Handler.java` | `JDBC_COMMAND` | `core` | `refDB` |
| `EDU-DEV-36` | Cache Stampede·Negative Cache·원본 정합성 | `com.cpf.reference.online.cache.consistency` | `EduDev36Handler.java` | `JDBC_COMMAND` | `core` | `refDB` |
| `EDU-DEV-37` | 온라인 분산 Lease·Fencing·소유권 상실 | `com.cpf.reference.online.concurrency.lease` | `EduDev37Handler.java` | `JDBC_COMMAND` | `core` | `refDB` |
| `EDU-DEV-38` | 다중 Tenant 격리·설정·데이터 범위 | `com.cpf.reference.online.security.multitenancy` | `EduDev38Handler.java` | `JDBC_COMMAND` | `core` | `refDB` |
| `EDU-DEV-39` | 업무일자·시간대·휴일 Calendar | `com.cpf.reference.online.calendar.businessday` | `EduDev39Handler.java` | `JDBC_COMMAND` | `core` | `refDB` |
| `EDU-DEV-40` | 금액·통화·반올림·환율 Version | `com.cpf.reference.online.money.exchange` | `EduDev40Handler.java` | `JDBC_COMMAND` | `core` | `refDB` |
| `EDU-DEV-41` | 감사 증적 Export·무결성 Hash·검증 | `com.cpf.reference.online.audit.evidence` | `EduDev41Handler.java` | `FILE` | `core` | `refDB` |
| `EDU-DEV-42` | 로그·Metric·Trace 상관관계와 Sampling | `com.cpf.reference.online.observability.correlation` | `EduDev42Handler.java` | `JDBC_COMMAND` | `core` | `refDB` |
| `EDU-DEV-43` | API Version 전환·하위 호환·폐기 | `com.cpf.reference.online.api.versioning` | `EduDev43Handler.java` | `JDBC_COMMAND` | `core` | `refDB` |
| `EDU-DEV-44` | Event Schema 진화·호환성·Dead Letter | `com.cpf.reference.online.messaging.schema` | `EduDev44Handler.java` | `OUTBOX` | `core` | `refDB` |
| `EDU-DEV-45` | 조회 모델·검색색인 Eventual Consistency | `com.cpf.reference.online.query.searchindex` | `EduDev45Handler.java` | `JDBC_QUERY` | `core` | `refDB` |
| `EDU-BAT-01` | 업무일 마감 Tasklet | `com.cpf.reference.batch.tasklet.close` | `EduBat01Handler.java` | `SPRING_BATCH` | `batch` | `refDB` |
| `EDU-BAT-02` | 회원 등급 10,000건 Chunk | `com.cpf.reference.batch.chunk.membergrade` | `EduBat02Handler.java` | `SPRING_BATCH` | `batch` | `refDB` |
| `EDU-BAT-03` | CSV 입출력 배치 | `com.cpf.reference.batch.file.csv` | `EduBat03Handler.java` | `SPRING_BATCH` | `batch` | `refDB` |
| `EDU-BAT-04` | 8개 범위 Partition | `com.cpf.reference.batch.partition.range` | `EduBat04Handler.java` | `SPRING_BATCH` | `batch` | `refDB` |
| `EDU-BAT-05` | Manager·Worker·Lease·Fencing | `com.cpf.reference.batch.remote.worker` | `EduBat05Handler.java` | `SPRING_BATCH` | `batch` | `refDB` |
| `EDU-BAT-06` | 센터컷 Preview·승인·실행 | `com.cpf.reference.batch.centercut.approval` | `EduBat06Handler.java` | `SPRING_BATCH` | `batch` | `refDB` |
| `EDU-BAT-07` | 영업일 23시 Scheduler | `com.cpf.reference.batch.scheduler.businessday` | `EduBat07Handler.java` | `SPRING_BATCH` | `batch` | `refDB` |
| `EDU-BAT-08` | Job Pack Version·Artifact 배포 | `com.cpf.reference.batch.jobpack.version` | `EduBat08Handler.java` | `SPRING_BATCH` | `batch` | `refDB` |
| `EDU-BAT-09` | 중지·재시작·실패건 재처리 | `com.cpf.reference.batch.recovery.restart` | `EduBat09Handler.java` | `SPRING_BATCH` | `batch` | `refDB` |
| `EDU-BAT-10` | 실행 요청 응답 유실·결과 대사 | `com.cpf.reference.batch.reconcile.requestloss` | `EduBat10Handler.java` | `SPRING_BATCH` | `batch` | `refDB` |
| `EDU-BAT-11` | 조건 분기·다단계 Job Flow | `com.cpf.reference.batch.flow.conditional` | `EduBat11Handler.java` | `SPRING_BATCH` | `batch` | `refDB` |
| `EDU-BAT-12` | Retry·Skip·No-Skip 예외 분류 | `com.cpf.reference.batch.faulttolerance.retryskip` | `EduBat12Handler.java` | `SPRING_BATCH` | `batch` | `refDB` |
| `EDU-BAT-13` | Writer Commit 장애 후 Checkpoint 재시작 | `com.cpf.reference.batch.checkpoint.writercommit` | `EduBat13Handler.java` | `SPRING_BATCH` | `batch` | `refDB` |
| `EDU-BAT-14` | JobParameter 식별·중복 실행·새 Instance | `com.cpf.reference.batch.instance.parameter` | `EduBat14Handler.java` | `SPRING_BATCH` | `batch` | `refDB` |
| `EDU-BAT-15` | 지연 도착 데이터·Backfill·재산출 | `com.cpf.reference.batch.backfill.latearrival` | `EduBat15Handler.java` | `SPRING_BATCH` | `batch` | `refDB` |
| `EDU-BAT-16` | Watermark 기반 증분 수집·재시작 | `com.cpf.reference.batch.incremental.watermark` | `EduBat16Handler.java` | `SPRING_BATCH` | `batch` | `refDB` |
| `EDU-BAT-17` | 암호화·압축·Checksum 파일 산출 | `com.cpf.reference.batch.file.secureoutput` | `EduBat17Handler.java` | `SPRING_BATCH` | `batch` | `refDB` |
| `EDU-BAT-18` | 수신 파일 Header·Detail·Trailer 대사 | `com.cpf.reference.batch.file.validation` | `EduBat18Handler.java` | `SPRING_BATCH` | `batch` | `refDB` |
| `EDU-BAT-19` | 다중 파일 Fan-in·Fan-out | `com.cpf.reference.batch.file.faninout` | `EduBat19Handler.java` | `SPRING_BATCH` | `batch` | `refDB` |
| `EDU-BAT-20` | Scheduler Misfire·Catch-up·건너뛰기 | `com.cpf.reference.batch.scheduler.misfire` | `EduBat20Handler.java` | `SPRING_BATCH` | `batch` | `refDB` |
| `EDU-BAT-21` | 중복 실행 방지·동시 실행 허용 범위 | `com.cpf.reference.batch.concurrency.execution` | `EduBat21Handler.java` | `SPRING_BATCH` | `batch` | `refDB` |
| `EDU-BAT-22` | 휴일 Calendar·영업일 순번 JobParameter | `com.cpf.reference.batch.calendar.businessday` | `EduBat22Handler.java` | `SPRING_BATCH` | `batch` | `refDB` |
| `EDU-BAT-23` | Stop·Abandon·Restart 의미 분리 | `com.cpf.reference.batch.lifecycle.stopabandon` | `EduBat23Handler.java` | `SPRING_BATCH` | `batch` | `refDB` |
| `EDU-BAT-24` | Remote Worker 유실·재할당·중복 결과 차단 | `com.cpf.reference.batch.remote.reassignment` | `EduBat24Handler.java` | `SPRING_BATCH` | `batch` | `refDB` |
| `EDU-BAT-25` | Partition 편향 감지·재분할 | `com.cpf.reference.batch.partition.rebalance` | `EduBat25Handler.java` | `SPRING_BATCH` | `batch` | `refDB` |
| `EDU-BAT-26` | 센터컷 결과 대사·차이 보정·재실행 | `com.cpf.reference.batch.centercut.reconcile` | `EduBat26Handler.java` | `SPRING_BATCH` | `batch` | `refDB` |
| `EDU-BAT-27` | Job Pack Checksum·호환성·이전 Version 복구 | `com.cpf.reference.batch.jobpack.recovery` | `EduBat27Handler.java` | `SPRING_BATCH` | `batch` | `refDB` |
| `EDU-BAT-28` | Host Agent Offline·명령 ACK 유실 | `com.cpf.reference.batch.agent.offline` | `EduBat28Handler.java` | `SPRING_BATCH` | `batch` | `refDB` |
| `EDU-BAT-29` | Dry Run·건수 Preview·표본 확인 | `com.cpf.reference.batch.dryrun.preview` | `EduBat29Handler.java` | `SPRING_BATCH` | `batch` | `refDB` |
| `EDU-BAT-30` | 대용량 처리 성능·용량·Backpressure | `com.cpf.reference.batch.performance.backpressure` | `EduBat30Handler.java` | `SPRING_BATCH` | `batch` | `refDB` |
| `EDU-ADM-01` | 기존 ADM 기능 재사용 판단 | `com.cpf.reference.optional.operations.reuse` | `EduAdm01Handler.java` | `JDBC_COMMAND` | `operations` | `refDB` |
| `EDU-ADM-02` | 고객 업무 조회 연동 | `com.cpf.reference.optional.operations.query` | `EduAdm02Handler.java` | `JDBC_QUERY` | `operations` | `refDB` |
| `EDU-ADM-03` | 안전한 운영 조치 | `com.cpf.reference.optional.operations.command` | `EduAdm03Handler.java` | `JDBC_COMMAND` | `operations` | `refDB` |
| `EDU-ADM-04` | 승인 필요한 위험 조치 | `com.cpf.reference.optional.operations.approval` | `EduAdm04Handler.java` | `JDBC_COMMAND` | `operations` | `refDB` |
| `EDU-ADM-05` | 비동기 작업·응답 유실 | `com.cpf.reference.optional.operations.asyncoperation` | `EduAdm05Handler.java` | `JDBC_COMMAND` | `operations` | `refDB` |
| `EDU-ADM-06` | 부분 성공·대상별 복구 | `com.cpf.reference.optional.operations.partialrecovery` | `EduAdm06Handler.java` | `JDBC_COMMAND` | `operations` | `refDB` |
| `EDU-ADM-07` | 고객 전용 화면 추가의 마지막 선택 | `com.cpf.reference.optional.operations.customscreen` | `EduAdm07Handler.java` | `JDBC_COMMAND` | `operations` | `refDB` |
| `EDU-ADM-08` | 권한·데이터 범위·Masking·사유 입력 연동 | `com.cpf.reference.optional.operations.search` | `EduAdm08Handler.java` | `JDBC_COMMAND` | `operations` | `refDB` |
| `EDU-ADM-09` | Expected Version 충돌 화면·재조회·재적용 | `com.cpf.reference.optional.operations.detail` | `EduAdm09Handler.java` | `JDBC_QUERY` | `operations` | `refDB` |
| `EDU-ADM-10` | 대상 일괄 조치·부분 성공·결과 파일 | `com.cpf.reference.optional.operations.bulk` | `EduAdm10Handler.java` | `JDBC_COMMAND` | `operations` | `refDB` |
| `EDU-ADM-11` | 설정·기능전환·유지보수 창 운영 | `com.cpf.reference.optional.operations.configuration` | `EduAdm11Handler.java` | `JDBC_COMMAND` | `operations` | `refDB` |
| `EDU-ADM-12` | Incident·Recovery Center 종단간 복구 | `com.cpf.reference.optional.operations.incident` | `EduAdm12Handler.java` | `JDBC_COMMAND` | `operations` | `refDB` |
| `EDU-ADM-13` | 감사 증적·다운로드·승인 반출 | `com.cpf.reference.optional.operations.evidence` | `EduAdm13Handler.java` | `JDBC_COMMAND` | `operations` | `refDB` |
| `EDU-ADM-14` | Topology·Health·Capacity Drill-down | `com.cpf.reference.optional.operations.topology` | `EduAdm14Handler.java` | `JDBC_COMMAND` | `operations` | `refDB` |
| `EDU-ADM-15` | Log·Trace·Transaction 상관 검색 | `com.cpf.reference.optional.operations.correlation` | `EduAdm15Handler.java` | `JDBC_COMMAND` | `operations` | `refDB` |
| `EDU-ADM-16` | 알림 Acknowledge·Escalation·교대 인계 | `com.cpf.reference.optional.operations.notification` | `EduAdm16Handler.java` | `JDBC_COMMAND` | `operations` | `refDB` |
| `EDU-ADM-17` | Browser 세션 만료·재로그인·위험 조치 안전성 | `com.cpf.reference.optional.operations.session` | `EduAdm17Handler.java` | `JDBC_COMMAND` | `operations` | `refDB` |
| `EDU-BZA-01` | 조직·직원·발령·기준일 | `com.cpf.reference.optional.backoffice.organization` | `EduBackoffice01Handler.java` | `JDBC_COMMAND` | `backoffice` | `refDB` |
| `EDU-BZA-02` | 사용자·역할·권한·실효 권한 | `com.cpf.reference.optional.backoffice.authorization` | `EduBackoffice02Handler.java` | `JDBC_COMMAND` | `backoffice` | `refDB` |
| `EDU-BZA-03` | 결재정책 Version·경로 사전 계산 | `com.cpf.reference.optional.backoffice.policysimulation` | `EduBackoffice03Handler.java` | `JDBC_COMMAND` | `backoffice` | `refDB` |
| `EDU-BZA-04` | 상신·승인·반려·철회·취소 | `com.cpf.reference.optional.backoffice.approvalflow` | `EduBackoffice04Handler.java` | `JDBC_COMMAND` | `backoffice` | `refDB` |
| `EDU-BZA-05` | 위임·대결·대행 책임 | `com.cpf.reference.optional.backoffice.delegation` | `EduBackoffice05Handler.java` | `JDBC_COMMAND` | `backoffice` | `refDB` |
| `EDU-BZA-06` | 첨부·알림·감사·다운로드 | `com.cpf.reference.optional.backoffice.evidence` | `EduBackoffice06Handler.java` | `JDBC_COMMAND` | `backoffice` | `refDB` |
| `EDU-BZA-07` | 초기 관리자 Bootstrap·첫 로그인·권한 인계 | `com.cpf.reference.optional.backoffice.directory` | `EduBackoffice07Handler.java` | `JDBC_COMMAND` | `backoffice` | `refDB` |
| `EDU-BZA-08` | 조직 개편·기준일·과거 이력 유지 | `com.cpf.reference.optional.backoffice.reorganization` | `EduBackoffice08Handler.java` | `JDBC_COMMAND` | `backoffice` | `refDB` |
| `EDU-BZA-09` | 입사·이동·휴직·퇴사 Joiner-Mover-Leaver | `com.cpf.reference.optional.backoffice.lifecycle` | `EduBackoffice09Handler.java` | `JDBC_COMMAND` | `backoffice` | `refDB` |
| `EDU-BZA-10` | 역할 충돌·직무분리·실효 권한 Simulation | `com.cpf.reference.optional.backoffice.separationofduties` | `EduBackoffice10Handler.java` | `JDBC_QUERY` | `backoffice` | `refDB` |
| `EDU-BZA-11` | 위임 중첩·기간 만료·결재 경로 재계산 | `com.cpf.reference.optional.backoffice.approvalhistory` | `EduBackoffice11Handler.java` | `JDBC_COMMAND` | `backoffice` | `refDB` |
| `EDU-BZA-12` | 계정 잠금·비밀번호 초기화·세션 강제 종료 | `com.cpf.reference.optional.backoffice.attachment` | `EduBackoffice12Handler.java` | `JDBC_COMMAND` | `backoffice` | `refDB` |
| `EDU-BZA-13` | 개인정보 Masking·감사 조회·승인 Export | `com.cpf.reference.optional.backoffice.privacyexport` | `EduBackoffice13Handler.java` | `JDBC_COMMAND` | `backoffice` | `refDB` |
| `EDU-BZA-14` | 고객 업무 승인 결과 반영·실패 Rollback | `com.cpf.reference.optional.backoffice.rollback` | `EduBackoffice14Handler.java` | `JDBC_COMMAND` | `backoffice` | `refDB` |
| `EDU-GW-01` | Server Group·Health·Load Balancing | `com.cpf.reference.optional.gateway.servergroup` | `EduGw01Handler.java` | `REFERENCE_GATEWAY` | `gateway` | `refDB` |
| `EDU-GW-02` | Route·Predicate·Path Rewrite | `com.cpf.reference.optional.gateway.route` | `EduGw02Handler.java` | `REFERENCE_GATEWAY` | `gateway` | `refDB` |
| `EDU-GW-03` | 인증·권한·TLS·HMAC·Nonce | `com.cpf.reference.optional.gateway.security` | `EduGw03Handler.java` | `REFERENCE_GATEWAY` | `gateway` | `refDB` |
| `EDU-GW-04` | Timeout·Retry·Circuit Breaker·Bulkhead | `com.cpf.reference.optional.gateway.resilience` | `EduGw04Handler.java` | `REFERENCE_GATEWAY` | `gateway` | `refDB` |
| `EDU-GW-05` | Draft·검증·승인·게시·부분 적용 | `com.cpf.reference.optional.gateway.publish` | `EduGw05Handler.java` | `REFERENCE_GATEWAY` | `gateway` | `refDB` |
| `EDU-GW-06` | Attempt Ledger·UNKNOWN_RESULT·LKG 복구 | `com.cpf.reference.optional.gateway.reconcile` | `EduGw06Handler.java` | `REFERENCE_GATEWAY` | `gateway` | `refDB` |
| `EDU-GW-07` | Service Discovery·Target Failover·복귀 | `com.cpf.reference.optional.gateway.registry` | `EduGw07Handler.java` | `REFERENCE_GATEWAY` | `gateway` | `refDB` |
| `EDU-GW-08` | SSRF Allowlist·DNS Rebinding·내부망 차단 | `com.cpf.reference.optional.gateway.health` | `EduGw08Handler.java` | `REFERENCE_GATEWAY` | `gateway` | `refDB` |
| `EDU-GW-09` | Header 정리·경로·요청·응답 변환 | `com.cpf.reference.optional.gateway.drain` | `EduGw09Handler.java` | `REFERENCE_GATEWAY` | `gateway` | `refDB` |
| `EDU-GW-10` | Body 크기·Content-Type·Schema Validation | `com.cpf.reference.optional.gateway.rejection` | `EduGw10Handler.java` | `REFERENCE_GATEWAY` | `gateway` | `refDB` |
| `EDU-GW-11` | Command 멱등성·Attempt Ledger·응답 유실 | `com.cpf.reference.optional.gateway.version` | `EduGw11Handler.java` | `REFERENCE_GATEWAY` | `gateway` | `refDB` |
| `EDU-GW-12` | 다중 인스턴스 설정 Drift·Reconcile | `com.cpf.reference.optional.gateway.ratecontrol` | `EduGw12Handler.java` | `REFERENCE_GATEWAY` | `gateway` | `refDB` |
| `EDU-GW-13` | Canary·가중치 Routing·Version Rollback | `com.cpf.reference.optional.gateway.audit` | `EduGw13Handler.java` | `REFERENCE_GATEWAY` | `gateway` | `refDB` |
| `EDU-GW-14` | Gateway 관측·개인정보 가림·감사 | `com.cpf.reference.optional.gateway.recovery` | `EduGw14Handler.java` | `REFERENCE_GATEWAY` | `gateway` | `refDB` |
| `EDU-OPS-01` | 신규 환경 설치·Artifact·Checksum 검증 | `com.cpf.reference.platform.install.artifact` | `EduOps01Handler.java` | `PROCESS` | `core` | `refDB` |
| `EDU-OPS-02` | Profile·환경변수·설정값 전체 검증 | `com.cpf.reference.platform.configuration.validation` | `EduOps02Handler.java` | `PROCESS` | `core` | `refDB` |
| `EDU-OPS-03` | Secret·Certificate 배포·교체·만료 대응 | `com.cpf.reference.platform.security.secretrotation` | `EduOps03Handler.java` | `PROCESS` | `core` | `refDB` |
| `EDU-OPS-04` | DB 3종 신규 설치·Migration·Drift·Rollback | `com.cpf.reference.platform.database.lifecycle` | `EduOps04Handler.java` | `PROCESS` | `core` | `refDB` |
| `EDU-OPS-05` | Kafka Topic·ACL·Consumer Group Lifecycle | `com.cpf.reference.platform.messaging.kafka` | `EduOps05Handler.java` | `PROCESS` | `core` | `refDB` |
| `EDU-OPS-06` | 기동·종료·Health·Dependency 순서 | `com.cpf.reference.platform.lifecycle.startstop` | `EduOps06Handler.java` | `PROCESS` | `core` | `refDB` |
| `EDU-OPS-07` | Rolling 배포·Session·Connection Drain | `com.cpf.reference.platform.deployment.rolling` | `EduOps07Handler.java` | `PROCESS` | `core` | `refDB` |
| `EDU-OPS-08` | Blue-Green·Canary 전환·되돌리기 | `com.cpf.reference.platform.deployment.bluegreen` | `EduOps08Handler.java` | `PROCESS` | `core` | `refDB` |
| `EDU-OPS-09` | 설정 변경 Partial Apply·Reconcile | `com.cpf.reference.platform.configuration.reconcile` | `EduOps09Handler.java` | `PROCESS` | `core` | `refDB` |
| `EDU-OPS-10` | Log·Metric·Trace 수집 장애·보존·용량 | `com.cpf.reference.platform.observability.pipeline` | `EduOps10Handler.java` | `PROCESS` | `core` | `refDB` |
| `EDU-OPS-11` | Backup·Restore·시점 복구·대사 | `com.cpf.reference.platform.recovery.backuprestore` | `EduOps11Handler.java` | `PROCESS` | `core` | `refDB` |
| `EDU-OPS-12` | 재해복구 전환·복귀·Split-Brain 방지 | `com.cpf.reference.platform.recovery.disaster` | `EduOps12Handler.java` | `PROCESS` | `core` | `refDB` |
| `EDU-OPS-13` | Disk·Memory·Network·DB 장애 Runbook | `com.cpf.reference.platform.runbook.infrastructure` | `EduOps13Handler.java` | `PROCESS` | `core` | `refDB` |
| `EDU-OPS-14` | 보안 사고·계정·키·세션 긴급 차단 | `com.cpf.reference.platform.security.incident` | `EduOps14Handler.java` | `PROCESS` | `core` | `refDB` |
| `EDU-OPS-15` | Version Upgrade·DB 호환·Application Rollback | `com.cpf.reference.platform.upgrade.compatibility` | `EduOps15Handler.java` | `PROCESS` | `core` | `refDB` |
