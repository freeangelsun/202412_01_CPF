# CPF QA38 Push 이후 Source 전수 리뷰

## 1. 기준

- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- 검토 기준 SHA: `54bcc10887a83b933685bff462c0b0d7df824923`
- Commit: `20260802_10`
- Commit 규모: `7,004 additions / 1,363 deletions / 8,367 changes`
- 기존 QA38 개발 기준 SHA: `dafe5c0e5260ea8149234e8ab2e75347e75338c1`
- 검토 대상:
  - Root Overlay 변경·신규 파일: **452개**
  - Delete Manifest 경로: **160개**
  - 경로 단위 검토 행: **612개**
  - Java Main Source: **232개**
  - Java Test Source: **49개**
  - Starter `build.gradle` 디렉터리: **55개**
  - Capability Profile: **13개**

파일별 판정은 `cpf-docs/work/review/CPF_QA38_POST_PUSH_SOURCE_AUDIT_MATRIX.csv`에 612개 경로를 한 행씩 기록했다.  
삭제 경로는 최신 HEAD의 실제 부재와 대체 Consumer까지 작업트리 Gate로 재확인해야 하므로 `재확인 필요`로 보수적으로 판정했다.

## 2. 총평

**현재 상태는 전체 완료가 아니다. 최종 판정은 `실패`다.**

이유는 단순히 외부 환경 검증이 남았기 때문이 아니다. 최신 Push Source 자체에 다음과 같은 P0 결함이 존재한다.

- 정식 Gradle Build 대상에서 빠진 Integration Module 7개
- 내부 Class Owner와 Compile Classpath가 맞지 않는 Import 7개
- BOM 51개 Constraint의 literal `${project.version}` 오류
- HTTP Starter와 Runtime Control 사이의 순환 경계
- Archive 실제 Core API 구현 Bean 단절
- Batch Contract/Testkit까지 Runtime Profile이 전이되는 Ownership 오염
- Provider Binding이 실제 생성 Dependency를 바꾸지 않는 Generator 결함
- 기존 QA38 Gate가 위 결함을 놓치고 PASS하는 False Green
- Push 이전 SHA Evidence를 현재 SHA 성공 증적으로 사용하는 Stale Evidence
- Source 결함이 있는데도 156개 Requirement를 전부 개발 완료로 표시한 Truth 결함

따라서 기존 Matrix의 `development_status=완료 156건`은 유지할 수 없다.

## 3. 기존 검증과 강화 검증의 차이

### 기존 QA38 Gate 실행 결과

- `verify-qa38-structure.py`: PASS
- `verify-qa38-java-duplicates.py`: PASS
- `verify-qa38-sql-parity.py`: PASS

### Push 이후 강화 Gate 실행 결과

- 미등록 제품 Module: **7**
- 내부 Classpath 오류: **7**
- Artifact Catalog 누락: **7**
- BOM literal version: **51**
- Product Java가 있으나 Test 0개 Module: **18**
- Archive Legacy API 구현 Bean 단절: **1**
- Batch Runtime Profile 전체 subproject 오염: **1**
- False Complete Matrix: **156개 전부**
- Stale exact-SHA 파일: **3**
- 최종 Exit Code: **1**

강화 Gate Source는 `cpf-tools/verification/qa39/verify-qa39-post-push-closure.py`에 포함했다.

## 4. Requirement 재판정

| 상태 | 건수 | 의미 |
|---|---:|---|
| 완료 | 11 | 정적 검토에서 직접 위반을 발견하지 않은 통제 |
| 부분 구현 | 81 | Source 일부는 있으나 Build·Consumer·Runtime·Test·Evidence가 닫히지 않음 |
| 미검증 | 29 | 최신 SHA에서 실제 환경 검증을 실행하지 않음 |
| 실패 | 22 | Source·Build·Truth의 확정 결함이 Acceptance를 위반 |
| 재확인 필요 | 13 | 정본·이력·삭제 후 참조를 전체 Repository에서 다시 대조해야 함 |

상세 재판정은 `CPF_QA38_POST_PUSH_REQUIREMENT_RECALIBRATION.csv`에 156개 전부 기록했다.

## 5. P0 확정 결함

| Defect | 영역 | 실제 결함 | 영향 |
|---|---|---|---|
| QA39-DEF-001 | Gradle Settings | build.gradle과 Source가 존재하는 7개 Integration 모듈이 settings.gradle include/projectDir에 없다. | 전체 Build·Test·Publication·BOM·Consumer에서 완전히 제외된다. 파일 존재만으로 완료 표시된 상태다. |
| QA39-DEF-002 | Artifact Graph | 누락된 7개 Integration 모듈이 Artifact Catalog, BOM, Platform Version 정본에도 없다. | Publication·Version Lock·SBOM·License·Upgrade 대상에서 누락된다. |
| QA39-DEF-003 | BOM | QA38 BOM constraint 51개가 Groovy single quote 안에 ${project.version}을 사용한다. | 버전이 보간되지 않아 literal ${project.version}이 POM에 기록되거나 Publication이 실패한다. |
| QA39-DEF-004 | Compile Classpath | 내부 Class Owner와 Gradle Compile Classpath를 대조해 7개 미선언/비노출 참조를 확인했다. | Java 전체 Compile 실패 가능성이 높고 현재 Static Gate는 이를 탐지하지 못한다. |
| QA39-DEF-005 | Dependency Cycle | HTTP Starter가 Runtime Control의 CpfRuntimeCanonicalHash를 참조하고 Runtime Control은 HTTP Starter에 의존한다. | 누락 의존성을 단순 추가하면 순환 의존이 된다. |
| QA39-DEF-006 | Ownership | Runtime Control Client가 HTTP, JDBC, Messaging, SFTP를 직접 끌고 Webhook·Observability 구현까지 직접 Import하는 God Starter다. | 선택 Capability 제거가 불가능하고 optional 제품을 강제로 전이하며 동일 JVM/MSA 경계를 왜곡한다. |
| QA39-DEF-007 | IBM MQ | CpfNamedBrokerClient 직접 의존이 없고, CCDT/Channel/TLS Properties가 실제 ConnectionFactory에 적용되지 않는다. | Compile 실패 가능성과 함께 IBM MQ 기능이 설정 선언 수준에 머문다. |
| QA39-DEF-008 | Archive Wiring | AutoConfiguration이 새 단순 Zip Service를 Bean으로 등록하고, 실제 Core API 구현 LocalCpfArchiveService는 등록하지 않는다. | 기존 CpfArchiveService API Consumer가 Bean을 받지 못하고 TAR/GZIP/Streaming 구현이 Dead Source가 된다. |
| QA39-DEF-009 | Batch Ownership | subprojects 전체에 Event Kafka와 Scheduled Profile을 implementation으로 주입한다. | contract·testkit까지 Runtime Starter를 오염시키고 Public Contract의 경량성과 역방향 의존을 깨뜨린다. |
| QA39-DEF-010 | Generator Binding | ProviderBindings는 검증·Manifest 기록에만 사용되고 생성 Gradle dependency를 변경하지 않는다. 모든 Profile이 Kafka/Rabbit/JMS/IBM MQ 등을 공통 허용한다. | EVENT_KAFKA에 messaging=rabbitmq를 지정해도 Kafka Profile이 생성되는 등 설정과 실제 Provider가 불일치한다. |
| QA39-DEF-011 | Verification False Green | 기존 Gate는 7개 미등록 Module, 내부 Classpath 오류, BOM literal version, Dead Bean을 놓치고 PASS했다. | 실제 Build가 닫히지 않았는데 QA38_STATIC_VALIDATION_PASS가 생성된다. |
| QA39-DEF-012 | Evidence Exact SHA | Evidence와 Package/Catalog의 QA38 SHA가 dafe5c0 또는 99fefc/R3 상태이며 최신 54bcc108 SHA가 아니다. | 현재 Commit의 성공 Evidence로 사용할 수 없다. |
| QA39-DEF-013 | Truth Matrix | 156개 Requirement의 development_status가 모두 완료지만 Source P0 결함과 Build 미실행이 존재한다. | 개발 완료와 검증 완료가 사실과 불일치하며 후속 작업이 누락된다. |
| QA39-DEF-014 | Build/Publication | Java 25 Fresh Cache 전체 Gradle Build·Test·Publication을 실행하지 않았다. | 현재 Compile Classpath·BOM 결함이 Push 전에 발견되지 않았다. |

## 6. P1 주요 미완결

| Defect | 영역 | 실제 결함 |
|---|---|---|
| QA39-DEF-015 | Core-only Fixture | 테스트가 Class 이름 prefix만 확인하며 Core-only Application boot와 optional dependency 부재를 증명하지 않는다. |
| QA39-DEF-016 | Test Coverage | Java Source가 있는 18개 Module에 자체 Test Source가 0개다. |
| QA39-DEF-017 | DB Lifecycle | 신규 DB Pack은 대부분 V1 Install과 R1 Drop만 있고 이전 Version Upgrade·Reapply·Conflict 경로와 실제 실행 Evidence가 없다. |
| QA39-DEF-018 | SFTP | Password 원문 Property, 승인된 Known-hosts 구현 부재, 설정만 보는 UP Health, ledgerRequired 미사용, Resume 충돌·Remote checksum·Lease recovery 미완성이다. |
| QA39-DEF-019 | TCP | Correlation Registry와 Reconnect Policy가 실제 Client 전송 흐름에 연결되지 않고 Unknown Result Store가 메모리 전용이다. Health는 항상 UP이며 TLS secret도 원문이다. |
| QA39-DEF-020 | Notification Core | CLAIMED lease 만료 재회수, process-kill recovery, 다중 인스턴스 locking, UNKNOWN_RESULT reconcile가 없다. Preference는 메모리 전용이고 variable_json은 실제 JSON이 아니다. |
| QA39-DEF-021 | Email | SimpleMailMessage만 사용하며 Template rendering, HTML/Attachment, Bounce/DSN/Callback가 없다. SMTP 예외 UNKNOWN_RESULT를 Worker가 완료 처리한다. |
| QA39-DEF-022 | SMS SPI | Idempotency와 Receipt 상태가 Process Memory Map에만 있고 Rate Limit·Callback authentication·Multi-instance 공유가 없다. |
| QA39-DEF-023 | Resource Server Security | audiences가 비어 있으면 Audience 검증이 전부 통과하고 clockSkew Property가 Decoder에 적용되지 않으며 Health가 실제 JWK 상태를 검사하지 않는다. |
| QA39-DEF-024 | Service Identity | HMAC Secret이 원문 Property이며 Nonce replay store가 없고 발급/검증 Audit·Key source rotation 계약이 없다. |
| QA39-DEF-025 | FTPS | verifyHostname=true를 검증하지만 FTPSClient endpoint checking에 적용하지 않고 raw password를 사용한다. Test와 Build 등록도 없다. |
| QA39-DEF-026 | gRPC | Unary generic byte client만 있고 TLS material/authority, Streaming, Backpressure, Metadata policy, Status mapping Test가 없다. Build에도 미등록이다. |
| QA39-DEF-027 | Object Storage | Default credential chain 외 Secret policy, paginated list, multipart/resume, download size/checksum 검증, copy-delete 보상/unknown result가 없다. Build 미등록이다. |
| QA39-DEF-028 | SMB | raw credential, 단순 path normalize, download 완료 후 size 검사, resume/checksum/unknown-result/recovery/Test가 없다. Build 미등록이다. |
| QA39-DEF-029 | SOAP | timeout Property가 WebServiceTemplate transport에 적용되지 않고 WSDL contract/marshaller/Fault detail/WS-Security 실제 Adapter가 없다. Build 미등록이다. |
| QA39-DEF-030 | Webhook | Module 미등록, signing-secret 원문 Environment, duplicate idempotency insert 처리 없음, retry scheduler/lease/reconcile 없음, HTTP non-2xx를 단순 FAILED 처리한다. |
| QA39-DEF-031 | Realtime | SSE가 Long.MAX_VALUE retry를 사용하고 jitter/cancel/circuit/offset resume가 없으며 WebSocket reconnect/scale-out session registry/Test가 없다. Build 미등록이다. |
| QA39-DEF-032 | Messaging Providers | Provider별 publish 골격은 있으나 consume/ack/redelivery/DLX/DLT/rebalance/reconnect/unknown reconcile와 자체 Test가 부족하거나 없다. |
| QA39-DEF-033 | Quartz | clustered=true를 설정하지만 JDBC JobStore, DataSource, 3 Vendor Quartz schema, node health/recovery/Test가 없다. |
| QA39-DEF-034 | Package Boundary | 물리 Owner는 Starter로 이동했지만 Package가 com.cpf.core.common.*로 유지돼 외부 Consumer가 Internal 구현을 Core 계약처럼 참조할 수 있다. |
| QA39-DEF-035 | Consumer Runtime | Dependency 연결은 추가됐지만 실제 Application boot, Route/OpenAPI, 기능 Consumer와 Optional removal 검증이 없다. |
| QA39-DEF-036 | Official DB Vendors | Oracle·PostgreSQL·MariaDB 실제 Fresh/Upgrade/Rollback/Reapply가 최신 SHA에서 실행되지 않았다. |
| QA39-DEF-037 | Frontend/Operations | 이번 Commit은 Build dependency만 변경했으며 신규 Messaging/TCP/SFTP/Notification 운영 조회·승인·재처리 화면 연결을 증명하지 않는다. |
| QA39-DEF-038 | Supply Chain | 최신 SHA의 SBOM, Vulnerability, License, Artifact Hash, published artifact completeness 검증이 없다. |
| QA39-DEF-039 | Multi-GPT Apply | R4는 앞선 과도한 충돌 차단을 제거하는 과정에서 겹치는 파일도 강제 덮어쓰는 방식이 됐다. |

## 7. 왜 전체 완료가 되지 않았는가

### 7.1 파일 존재를 기능 완료로 판정했다

Module, Class, Interface, AutoConfiguration, SQL 파일이 존재한다는 사실을 실제 Build·Bean 조립·Consumer·Runtime 동작보다 먼저 완료로 환산했다.  
그 결과 `settings.gradle`에 포함되지 않은 7개 Module도 완료로 기록됐다.

### 7.2 검증 순서가 역전됐다

정상 순서는 `Module Graph → Compile Classpath → Build → Consumer → Runtime → Evidence → Matrix 완료`여야 한다.  
이번에는 Source 생성과 일부 JDK Harness 후 Matrix를 완료 처리했고, 실제 Gradle Compile과 Publication을 실행하지 못했다.

### 7.3 기존 Gate가 발견 목록을 하드코딩했다

QA38 구조 Gate는 “필수로 알고 있는 Module이 존재하는가”만 검사했다.  
Repository에 새 `build.gradle` 디렉터리가 추가됐는데 Settings에 빠진 경우를 발견하지 못했다.

### 7.4 물리 이동과 Architecture 이동을 혼동했다

Source를 Starter 경로로 옮겼지만 Package·Import·Owner Contract를 함께 재설계하지 않아 다음이 발생했다.

- `http-client → runtime-control-client` 구현 참조
- `runtime-control-client → http-client` 의존
- Runtime Control의 Webhook·Observability 직접 참조
- Starter 안에 `com.cpf.core.common.*` 구현 Package 잔존

### 7.5 Capability 범위를 한 번에 너무 넓게 열었다

QA38 한 묶음에서 Core 이관, 7개 신규 Integration, Messaging 4종, TCP, Notification, Quartz, Generator, DB를 동시에 완료 처리했다.  
각 Capability의 정상·오류·경계·복구·다중 인스턴스 Acceptance를 개별 종료하지 못했다.

### 7.6 Exact-SHA Evidence가 Push 이후 갱신되지 않았다

개발 중 생성한 `dafe5c0` 또는 `99fefc` Evidence를 `54bcc108` Source의 성공 증적으로 승계했다.  
과거 Evidence는 참고 자료일 뿐 현재 Commit의 PASS가 아니다.

### 7.7 Matrix 완료 값을 검증 결과보다 먼저 결정했다

156개 Requirement가 모두 개발 완료로 작성돼 있어, 이후 발견된 결함과 미실행 검증을 수용할 상태 축이 사라졌다.

## 8. 다음에는 미완료를 남기지 않는 방법

1. **Truth Reset부터 시작한다.** 최신 SHA에서 기존 완료 상태를 먼저 제거하고 실제 상태로 재판정한다.
2. **Build Graph를 첫 번째 P0로 닫는다.** Settings·Artifact Registry·BOM·Platform Properties·Publication을 하나의 정본으로 생성한다.
3. **모든 Java 수정 전 Import→Owner→Classpath Gate를 실행한다.** 순환 의존과 미선언 참조를 Source 단계에서 차단한다.
4. **Capability를 작은 완료 단위로 처리한다.** 한 Capability는 Source, AutoConfiguration, Consumer, Unit, Context, Integration, Fault, DB, Evidence가 모두 끝나야 다음 Capability로 이동한다.
5. **Provider Binding은 Metadata가 아니라 Dependency 결과로 검증한다.** 생성 `build.gradle`과 Manifest의 `resolvedStarters`가 동일해야 한다.
6. **Runtime Test가 없는 Capability는 완료 금지한다.** 실제 Provider 환경이 없더라도 Fixture/Test/Gate Source를 먼저 구현하고 상태는 `미검증`으로 유지한다.
7. **Matrix는 Evidence에서 생성한다.** 필수 Evidence가 없으면 `완료` 값을 입력할 수 없게 Truth Gate를 둔다.
8. **Push 후 exact SHA에서 통합 검증을 한 번만 실행한다.** 저비용 Gate → Java Build → Frontend → DB → Runtime → Supply-chain 순서로 반복 낭비 없이 수집한다.
9. **Codex는 최종 독립 검수만 수행한다.** 개발 미완료를 Codex로 넘기지 않는다.
10. **부분 구현이 하나라도 있으면 Project 완료 보고를 차단한다.**

## 9. 보호할 성공 기능

- `cpf-core`의 Public API/SPI, 거래 Context, Error, Masking, Identifier 계약
- 기존 Security Session/JDBC, Kafka, Cache, Observability, Resilience, Feature Flag, Secret Starter의 성공 기능
- Fixed-length/ISO8583/TCP Codec에서 이미 발견·수정된 Secondary Bitmap, STX/ETX DLE escaping, Oversize 차단
- 기존 Batch 독립 executable과 명시적 Publication Repository 정책
- Oracle·PostgreSQL·MariaDB 공식 지원 범위
- Protected path:
  - `cpf-docs/deliverables/**`
  - `cpf-docs/guides/**`
  - `cpf-docs/environment/docker/**`
  - `cpf-tools/environment/docker-development-test/**`

## 10. 다음 작업 정본

- 자체 개발 요건: `cpf-docs/work/current/CPF_QA39_SELF_DEVELOPMENT_REQUIREMENTS.csv`
- 통합 개발 요청: `cpf-docs/work/current/CPF_QA39_INTEGRATED_DEVELOPMENT_REQUEST.md`
- Defect Register: `cpf-docs/work/review/CPF_QA38_POST_PUSH_DEFECT_REGISTER.csv`
- Source별 Audit: `cpf-docs/work/review/CPF_QA38_POST_PUSH_SOURCE_AUDIT_MATRIX.csv`
- Requirement 재판정: `cpf-docs/work/review/CPF_QA38_POST_PUSH_REQUIREMENT_RECALIBRATION.csv`
- Handover: `cpf-docs/work/handover/CPF_QA38_TO_QA39_HANDOVER.md`
- Codex 검수 요청: `cpf-docs/work/codex/qa39/CPF_CODEX_QA39_FINAL_INDEPENDENT_VALIDATION_REQUEST.md`

## 11. 검토 한계

현재 실행환경에서는 GitHub exact Commit의 변경 목록과 R4 Root Overlay 전체를 대조해 변경·삭제 대상 612개를 파일 단위로 검토했다.  
네트워크 DNS 차단으로 전체 Repository Clone과 Java 25 Gradle Build는 실행하지 못했다. 따라서 전체 Compile·DB·Broker·Frontend·Browser·Supply-chain 결과는 PASS로 기록하지 않았다.
