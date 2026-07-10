# Codex 요청서 — CPF PFW Core Capability 실구현, ADM 관제 연결, OpenAPI/Sample Coverage, Evidence/Gate 강화

## 0. 이번 작업의 최종 목표

이번 작업은 문서 정리나 기존 산출물 보정만 하는 작업이 아니다.

CPF 최종 목표 기준에서 아직 부분 구현, 미검증, 재확인 필요로 남아 있는 PFW 핵심 capability를 실제 개발 가능한 수준까지 전진시키는 작업이다. 개발 결과는 소스, port, adapter, API, 테스트, EDU/참조 샘플, OpenAPI, sample coverage, evidence, report/gap/matrix에 일관되게 반영한다.

기준 repository:

https://github.com/freeangelsun/202412_01_CPF

기준 branch:

master

최종 목표 기준은 CPF_FINAL_TARGET_REQUIREMENTS.md다. 원본 확인이나 검색이 불안정하면 CPF_FINAL_TARGET_REQUIREMENTS_01.md ~ CPF_FINAL_TARGET_REQUIREMENTS_05.md를 보조로 참고한다. 목표파일 확인 없이 구현 범위를 임의로 축소하거나 완료 처리하지 않는다.

이번 작업의 최종 목표는 아래다.

1. PFW Service Call Engine이 registry, selectedInstanceId, call history, direct/LB mode, timeout/retry/failover/circuit result를 source/contract/API/sample/test 수준으로 제공한다.
2. PFW Broker가 envelope, idempotency, outbox/inbox, DLQ, replay, retry metadata를 source/contract/sample/test 수준으로 제공한다.
3. PFW FileTransfer가 LOCAL adapter, SFTP/FTP/SCP/SSH protocol plan, checksum, temp/rename/archive policy, history/query, duplicate prevention, unknown result를 제공한다.
4. PFW Security가 secret reference, credential provider, key/cert provider, signature, encryption/decryption port를 제공한다.
5. PFW Runtime/Admin Status가 worker control, heartbeat, ghost instance candidate, runtime health, instance registry status, ADM 조회 후보 API/DTO를 제공한다.
6. PFW Archive/Compression이 기존 ZIP/GZIP/checksum/zip-slip 기본 구현을 유지하면서 recursive, streaming 후보, duplicate, corrupted, size guard, sanitize, result/history metadata를 보강한다.
7. ADM 관제에서 PFW capability를 조회하거나 제어할 수 있는 API/DTO 후보가 생긴다.
8. 전체 REST API OpenAPI coverage 검증이 강화된다.
9. sample coverage matrix가 실제 sample/test/evidence/OpenAPI mapping 상태를 보여준다.
10. Java 25 LTS 목표는 문서 표기가 아니라 version/toolchain/compatibility/test evidence 기준으로 착수된다.
11. qualityGate가 거짓 완료, catalog-only 완료, missing evidence, OpenAPI 누락, ownership 위반을 잡는다.
12. 실행하지 않은 runtime 검증은 완료가 아니라 미검증으로 남는다.

Git commit, Git push, branch 생성은 하지 않는다. 민감정보 원문을 소스, 로그, evidence, 문서에 기록하지 않는다. 실행하지 않은 검증을 완료로 기록하지 않는다.

## 1. 현재 상태와 이번 작업의 문제

최신 master 기준으로 이미 완료 또는 완료 후보로 유지할 항목은 되돌리지 않는다. 단, 해당 evidence path가 실제로 없으면 완료 근거로 쓰지 않는다.

유지 후보:
- MariaDB full install 기존 evidence
- ADM runtime 기존 evidence
- ADM permission runtime 기존 evidence
- OpenAPI runtime 기존 evidence
- standard header E2E 기존 evidence
- composite transaction trace 기존 evidence
- transaction segment log 기존 evidence
- ADM transaction group/list/timeline 기존 evidence
- CMN fixed-length engine/advanced 샘플과 테스트
- EXS retry/unknown/reconciliation source/contract test
- PFW archive/compression ZIP/GZIP/checksum/zip-slip 기본 구현 및 테스트
- BAT/XYZ/CMN/EXS/PFW/ADM/BIZADM EducationSample 계열 기존 보강분
- sample coverage 228개 sampleId 기존 기준
- qualityGate 기존 evidence

현재 남은 핵심 문제:
1. PFW Service Call Engine은 contract 테스트는 있으나 registry, selectedInstanceId, call history, direct/LB mode, 다중 instance runtime 검증이 부족하다.
2. PFW Broker는 envelope/port 중심이며 DLQ, replay, idempotency, outbox/inbox 흐름이 아직 충분하지 않다. 실 Kafka/MQ runtime은 미검증이다.
3. PFW FileTransfer는 request/result 중심이며 LOCAL reference adapter, history/query, protocol plan, unknown result, checksum, duplicate 처리가 더 필요하다. SFTP/FTP/SCP/SSH runtime은 미검증이다.
4. PFW Security Credential은 secret reference 중심이며 key/cert provider, signature, encryption/decryption port가 더 필요하다. Vault/KMS runtime은 미검증이다.
5. PFW Runtime/Admin Status는 DTO/source smoke 수준이며 worker control, heartbeat, ghost instance, runtime health, ADM 조회 연결이 더 필요하다.
6. PFW Archive/Compression은 기본 ZIP/GZIP은 있으나 운영 케이스가 더 필요하고 TAR는 아직 후보/부분 구현으로 남아야 한다.
7. ADM 관제는 PFW capability와 연결되는 조회/제어 후보 API/DTO가 더 필요하다.
8. 전체 REST API OpenAPI coverage는 EDU API 일부가 아니라 전체 Controller/API 기준으로 강화되어야 한다.
9. sample coverage matrix는 실제 sample/test/evidence/OpenAPI mapping을 더 정확히 추적해야 한다.
10. Java 25 LTS는 toolchain/version/compatibility/test evidence 없이는 완료로 볼 수 없다.
11. architecture ownership scan은 재확인 필요 상태다.
12. report/evidence/index/matrix/gap은 새 개발 결과와 실제 검증 결과만 기준으로 맞춘다.

이번 작업은 위 문제를 “검토만” 하지 말고, 가능한 범위에서 실제 소스와 테스트를 작성해 개발 진도를 전진시킨다.

## 2. 공통 설계 기준

PFW는 프레임워크 기술 capability의 소유자다.

PFW 책임:
- Service Call Engine
- CpfWebClient/CpfRestClient
- timeout/retry/failover/circuit breaker
- service/endpoint/instance registry
- selectedInstanceId logging
- call history
- broker publish/consume port
- outbox/inbox port
- DLQ/replay
- file transfer port
- SFTP/FTP/SCP/SSH request/result/plan
- archive/compression port
- secret/credential/key/cert/signature/encryption provider port
- runtime heartbeat
- worker control
- health/ghost detection 후보
- audit context
- masking/security 기본
- ADM status DTO 후보

CMN은 프로젝트 공통 helper, parser, formatter, fixture, validation, converter 역할을 담당한다.

EXS는 외부연계 기술 소유자가 아니라 대외기관 업무 adapter/기관별 구현체다.

금지:
- PFW가 EXS/ACC/MBR/BAT/BIZADM/ADM/XYZ에 의존
- CMN이 업무 구현체에 의존
- 업무 주제영역 간 내부 기술 클래스 직접 재사용
- fixed-length/parser/formatter가 EXS 전용
- timeout/retry/circuit/failover가 EXS 전용
- broker/filetransfer/archive/security/servicecall 기술 engine이 업무 모듈 소유처럼 구현
- 업무 모듈에서 URL 직접 조합
- 업무 모듈에서 Controller 직접 호출
- 업무 모듈에서 타 주제영역 Repository/Mapper 직접 참조
- Spring Event만으로 핵심 거래 상태 전이, 외부 송신, unknown/reconciliation, DLQ/replay, multi-instance 전달을 완료 처리

Codex는 현재 코드 구조를 먼저 보고 가장 자연스러운 설계와 파일 위치를 선택한다. 아래 클래스명과 package는 작업 방향을 주기 위한 후보이며, 기존 구조가 더 적절하면 그 구조를 따른다. 단, ownership과 완료 기준은 지킨다.

## 3. PFW Service Call Engine 실구현

목표:
PFW Service Call Engine을 CPF MSA-first / modular-monolith-compatible 호출 표준 capability로 전진시킨다.

권장 위치:
- pfw/src/main/java/cpf/pfw/common/servicecall
- pfw/src/test/java/cpf/pfw/common/servicecall

필수 개발 항목:
- ServiceCallRequest
- ServiceCallResult
- ServiceCallHeader 또는 metadata
- transactionGlobalId, segmentId, parentSegmentId, sourceModule, targetModule, timeoutMs, retryPolicyId, circuitPolicyId field
- selectedInstanceId 기록 모델
- ServiceCallHistoryRecord
- ServiceCallHistoryPort 또는 조회 후보
- ServiceRegistryPort
- EndpointRegistryPort
- InstanceRegistryPort
- InstanceHealthStatus
- EndpointStatus
- CircuitStatus
- direct instance mode 선택 모델
- LB endpoint mode 선택 모델
- timeout/retry/failover/circuit breaker 결과를 call result에 연결하는 표준 field
- Remote Facade Proxy가 PFW Service Call Engine/CpfWebClient/CpfRestClient를 통과하는 contract/sample
- URL 직접 조합/Controller 직접 호출/타 Repository 직접 참조 금지 architecture check 후보

필수 sampleId 후보:
- PFW-EDU-CALL-006 selectedInstanceId logging
- PFW-EDU-CALL-007 call history
- PFW-EDU-CALL-008 service/endpoint/instance registry
- PFW-EDU-CALL-009 direct instance mode
- PFW-EDU-CALL-010 LB endpoint mode

권장 샘플:
- PfwServiceCallRequestEducationSample
- PfwSelectedInstanceLoggingEducationSample
- PfwServiceCallHistoryEducationSample
- PfwServiceRegistryEducationSample
- PfwDirectInstanceModeEducationSample
- PfwLbEndpointModeEducationSample
- PfwRemoteFacadeProxyEducationSample

필수 테스트:
- selectedInstanceId가 call result/history/context에 남는지 검증
- service/endpoint/instance registry 등록/조회/상태 모델 검증
- direct instance mode request/selection/result 검증
- LB endpoint mode request/result/remote header 수집 모델 검증
- call history 생성/조회 검증
- transactionGlobalId/header propagation 검증
- timeout/retry/failover/circuit breaker 상태 DTO와 call result 연결 검증
- 금지 호출 패턴 architecture scan 후보 검증

ADM 연결 후보:
- service call history 조회 API/DTO
- service registry 조회 API/DTO
- endpoint/instance health 조회 API/DTO
- selectedInstanceId 기반 호출 상세 조회 후보

완료 기준:
- 실제 source class 존재
- 실제 test class 존재
- EDU/참조 sample class 존재
- sample test 존재
- evidence 존재
- sample coverage 반영
- REST API 추가 시 OpenAPI 반영
- 다중 instance HTTP runtime 미실행 시 runtime은 미검증으로 기록

완료 불인정:
- CoverageCatalog만 추가
- DTO만 있고 테스트 없음
- 업무 모듈에서 URL 직접 조합
- Controller 직접 호출
- 타 주제영역 Repository/Mapper 직접 참조
- 다중 instance runtime 미실행인데 runtime 완료 기록

## 4. PFW Broker DLQ / Replay / Idempotency / Outbox / Inbox 실구현

목표:
PFW Broker를 단순 envelope/port에서 비동기 처리 표준 capability로 전진시킨다.

권장 위치:
- pfw/src/main/java/cpf/pfw/common/broker
- pfw/src/test/java/cpf/pfw/common/broker

필수 개발 항목:
- BrokerEnvelope
- BrokerHeader
- BrokerPublisherPort
- BrokerConsumerPort
- BrokerOutboxPort
- BrokerOutboxRecord
- BrokerInboxPort
- BrokerInboxRecord
- BrokerDlqPort
- BrokerDlqRecord
- BrokerReplayPort
- BrokerReplayRequest
- BrokerReplayResult
- DuplicateMessageResult
- Retry/backoff metadata
- Unknown broker result candidate
- in-memory 또는 test-support reference implementation

BrokerHeader 필수 후보:
- transactionGlobalId
- segmentId
- sourceModule
- targetModule
- messageId
- correlationId
- idempotencyKey
- retryCount
- createdAt

필수 sampleId 후보:
- PFW-EDU-BROKER-001 broker publish port
- PFW-EDU-BROKER-002 broker consume port
- PFW-EDU-BROKER-003 message envelope/header
- PFW-EDU-BROKER-004 outbox 후보
- PFW-EDU-BROKER-005 inbox 후보
- PFW-EDU-BROKER-006 DLQ 후보
- PFW-EDU-BROKER-007 replay 후보
- PFW-EDU-BROKER-008 broker idempotency

권장 샘플:
- PfwBrokerEnvelopeEducationSample
- PfwBrokerOutboxInboxEducationSample
- PfwBrokerIdempotencyEducationSample
- PfwBrokerDlqEducationSample
- PfwBrokerReplayEducationSample
- PfwBrokerRetryMetadataEducationSample

필수 테스트:
- publish/consume envelope contract 검증
- idempotencyKey 기반 duplicate 처리 검증
- outbox record 생성/상태 전이 검증
- inbox duplicate handling 검증
- DLQ record 생성 및 reason mapping 검증
- replay request/result 검증
- retry count/backoff metadata 검증
- 민감정보 원문 미기록 검증

ADM 연결 후보:
- broker status 조회
- DLQ 목록/상세 조회
- replay 요청/결과 조회
- broker message history 후보

완료 기준:
- PFW broker source/port/test/sample/evidence 존재
- sample coverage 반영
- OpenAPI 대상 API가 있으면 Swagger/OpenAPI 반영
- 실 Kafka/MQ runtime은 실행하지 않았으면 미검증 유지

완료 불인정:
- embedded/mock broker 검증을 실 Kafka/MQ runtime 완료로 기록
- Spring Event만으로 broker/DLQ/replay 대체
- broker engine이 업무 모듈 소유처럼 구현
- evidence 없이 완료 처리

## 5. PFW FileTransfer 표준 port / LOCAL adapter / history / unknown result 실구현

목표:
PFW FileTransfer를 운영 가능한 파일 전송 표준 capability로 전진시킨다.

권장 위치:
- pfw/src/main/java/cpf/pfw/common/filetransfer
- pfw/src/test/java/cpf/pfw/common/filetransfer

필수 개발 항목:
- FileTransferProtocol: LOCAL, SFTP, FTP, SCP, SSH
- FileTransferRequest
- FileTransferResult
- FileTransferClientPort
- FileTransferHistoryRecord
- FileTransferHistoryPort
- FileTransferPlan
- FileTransferChecksumPolicy
- FileTransferArchivePolicy
- FileTransferDuplicateKey
- FileTransferUnknownResult
- FileTransferFailureReason
- LOCAL reference adapter
- SFTP/FTP/SCP/SSH protocol plan과 adapter port
- credential reference 사용, secret 원문 저장 금지

필수 sampleId 후보:
- PFW-EDU-FILE-001 file transfer request/result
- PFW-EDU-FILE-002 SFTP/FTP/SCP/SSH protocol 후보
- PFW-EDU-FILE-003 checksum policy
- PFW-EDU-FILE-004 temp/rename/archive policy
- PFW-EDU-FILE-005 duplicate prevention
- PFW-EDU-FILE-006 transfer history

권장 샘플:
- PfwFileTransferProtocolPlanEducationSample
- PfwLocalFileTransferEducationSample
- PfwFileTransferHistoryEducationSample
- PfwFileTransferDuplicatePreventionEducationSample
- PfwFileTransferChecksumEducationSample
- PfwFileTransferUnknownResultEducationSample

필수 테스트:
- LOCAL adapter copy/move request/result 검증
- protocol별 plan 생성 검증
- checksum metadata 검증
- temp file / rename / archive policy 검증
- duplicate key 검증
- history query DTO/port 검증
- unknown result/failure reason contract 검증
- credential 원문 미기록 검증

ADM 연결 후보:
- file transfer history 조회
- file transfer failure reason 조회
- unknown result 조회
- 재처리 또는 상태 확인 후보 API/DTO

완료 기준:
- PFW filetransfer source/port/adapter/test/sample/evidence 존재
- sample coverage 반영
- OpenAPI 대상 API가 있으면 Swagger/OpenAPI 반영
- 실제 SFTP/FTP/SCP/SSH 접속 미실행 시 runtime은 미검증 유지

완료 불인정:
- 실제 SFTP/FTP/SCP/SSH 접속 없이 runtime 완료 기록
- credential 원문 저장
- file transfer engine을 EXS/BAT 같은 업무 모듈 소유처럼 구현
- source/test/evidence 없이 matrix만 완료 처리

## 6. PFW Security Credential / Key / Cert / Signature / Encryption port 실구현

목표:
PFW Security capability를 secret reference 수준에서 credential/key/cert/signature/encryption 표준 port로 전진시킨다.

권장 위치:
- pfw/src/main/java/cpf/pfw/common/security
- pfw/src/test/java/cpf/pfw/common/security

필수 개발 항목:
- SecretReference
- CredentialProviderPort
- KeyCertificateProviderPort
- SignatureProviderPort
- EncryptionProviderPort
- SignatureRequest
- SignatureResult
- SignatureVerificationResult
- EncryptionRequest
- EncryptionResult
- DecryptionRequest
- DecryptionResult
- CredentialRotationMetadata candidate
- audit-safe DTO
- local/test reference implementation
- JDK 기본 crypto로 가능한 범위의 local reference sample
- Vault/KMS provider port와 설정 후보

필수 sampleId 후보:
- PFW-EDU-SEC-001 secret reference
- PFW-EDU-SEC-002 credential provider port
- PFW-EDU-SEC-003 key/cert provider port
- PFW-EDU-SEC-004 signature verification port
- PFW-EDU-SEC-005 encryption/decryption port

권장 샘플:
- PfwSecretReferenceEducationSample
- PfwCredentialProviderPortEducationSample
- PfwKeyCertificateEducationSample
- PfwSignatureVerificationEducationSample
- PfwEncryptionDecryptionPortEducationSample

필수 테스트:
- secret 원문 미노출 검증
- key/cert provider contract 검증
- signature 생성/검증 contract 검증
- encryption/decryption contract 검증
- audit-safe DTO 검증
- evidence에 token/key/secret 원문 미노출 검증

sample coverage 보정:
- key/cert/signature/encryption sample은 masking/audit featureArea에 섞지 않는다.
- security package와 featureArea를 명확히 기록한다.

완료 기준:
- PFW security source/port/test/sample/evidence 존재
- secret/token/key 원문이 로그/evidence에 남지 않음
- sample coverage 반영
- Vault/KMS runtime 미실행 시 runtime은 미검증 유지

완료 불인정:
- key/cert/signature/encryption이 masking/audit 샘플에 섞임
- secret/token/key 원문 로그 또는 evidence 기록
- Vault/KMS runtime 미실행인데 완료 기록

## 7. PFW Runtime / Worker / Heartbeat / Ghost / Admin Status 실구현

목표:
PFW Runtime/Admin Status를 운영 상태 관리 capability로 전진시킨다.

권장 위치:
- pfw/src/main/java/cpf/pfw/common/runtime
- pfw/src/main/java/cpf/pfw/common/admin
- pfw/src/test/java/cpf/pfw/common/runtime
- pfw/src/test/java/cpf/pfw/common/admin

필수 개발 항목:
- WorkerControlCommand
- WorkerControlResult
- WorkerState
- WorkerStatusQuery
- RuntimeHealthStatus
- RuntimeInstanceStatus
- HeartbeatRecord
- HeartbeatPort
- GhostInstanceCandidate
- RuntimeAdminStatusDto
- scheduler/worker control audit candidate
- in-memory/test reference implementation
- ADM 조회 API/DTO 후보

필수 sampleId 후보:
- PFW-EDU-RUNTIME-001 runtime health
- PFW-EDU-RUNTIME-002 heartbeat
- PFW-EDU-RUNTIME-003 ghost instance candidate
- PFW-EDU-RUNTIME-004 instance status
- PFW-EDU-RUNTIME-005 admin status DTO
- PFW-EDU-RUNTIME-006 worker control

권장 샘플:
- PfwWorkerControlEducationSample
- PfwRuntimeWorkerStateEducationSample
- PfwRuntimeHealthEducationSample
- PfwRuntimeHeartbeatEducationSample
- PfwGhostInstanceCandidateEducationSample
- PfwAdminRuntimeStatusEducationSample

필수 테스트:
- worker pause/resume/drain/stop command 검증
- invalid command validation 검증
- runtime health status 검증
- heartbeat update/query 검증
- ghost instance candidate 판단 검증
- audit-safe DTO 검증
- ADM status DTO/source contract 검증

ADM 연결 후보:
- worker status 조회
- runtime health 조회
- heartbeat 조회
- ghost candidate 조회
- instance status 조회

완료 기준:
- PFW runtime/admin source/test/sample/evidence 존재
- ADM 조회 후보가 기존 구조와 충돌하지 않음
- OpenAPI 대상 API가 있으면 Swagger/OpenAPI 반영
- sample coverage 반영
- 실제 다중 instance runtime 미실행 시 runtime은 미검증 유지

완료 불인정:
- 다중 instance runtime 미실행인데 runtime 완료 기록
- worker control이 단순 문서나 DTO만 있고 테스트 없음
- ADM runtime 호출 미실행인데 ADM service registry runtime 완료 기록

## 8. PFW Archive / Compression 운영 케이스 보강

목표:
기존 ZIP/GZIP/checksum/zip-slip 구현을 유지하면서 운영 안정성 케이스를 보강한다.

권장 위치:
- pfw/src/main/java/cpf/pfw/common/archive
- pfw/src/test/java/cpf/pfw/common/archive

필수 개발/테스트:
- recursive directory zip
- large file streaming 후보
- overwrite=false duplicate prevention
- corrupted zip/gzip error handling
- max entry size guard
- max total size guard
- extract target sanitize
- archive result/history metadata
- checksum mismatch handling

필수 sampleId 후보:
- PFW-EDU-ARCHIVE-011 recursive directory zip
- PFW-EDU-ARCHIVE-012 large file streaming 후보
- PFW-EDU-ARCHIVE-013 overwrite=false duplicate prevention
- PFW-EDU-ARCHIVE-014 corrupted archive handling
- PFW-EDU-ARCHIVE-015 max entry/total size guard
- PFW-EDU-ARCHIVE-016 archive result/history metadata

TAR 기준:
- Java 표준만으로 무리하게 TAR를 구현하지 않는다.
- 외부 라이브러리 도입이 필요하면 사유와 대안을 기록하고 gap으로 남긴다.
- TAR를 구현하지 않았으면 archive 전체 완료로 기록하지 않는다.

완료 기준:
- 운영 케이스별 테스트 존재
- 기능별 sample 존재
- evidence 반영
- 기존 ZIP/GZIP/checksum/zip-slip 기능 회귀 없음

완료 불인정:
- TAR 미구현인데 archive 전체 완료 처리
- corrupted/size guard/security case 없이 운영 가능 수준으로 완료 처리
- evidence 없이 완료 처리

## 9. ADM 관제 API 연결 전진

목표:
PFW 핵심 capability가 운영자가 조회하거나 제어할 수 있는 플랫폼 기능으로 이어지도록 ADM API/DTO 후보를 전진시킨다.

대상:
- service call history 조회
- service/endpoint/instance registry 조회
- endpoint/instance health 조회
- broker status 조회
- DLQ 목록/상세 조회
- replay 요청/결과 조회
- file transfer history 조회
- file transfer failure/unknown result 조회
- runtime health 조회
- worker status 조회
- heartbeat 조회
- ghost candidate 조회
- credential provider status 조회

기준:
- 기존 ADM Controller/API/DTO 구조를 먼저 확인하고 그 구조에 맞춘다.
- 신규 API를 만들면 OpenAPI annotation과 고유 operationId를 반영한다.
- request/response schema, standard error response, required header, transactionGlobalId/header, 권한/감사/마스킹 설명을 반영한다.
- credential provider status는 secret 원문 없이 reference/status만 조회한다.
- 실제 runtime 호출을 하지 못하면 source/API contract와 runtime 상태를 분리한다.

권장 sampleId 후보:
- ADM-EDU-PFW-001 service call history 조회
- ADM-EDU-PFW-002 service registry 조회
- ADM-EDU-PFW-003 endpoint/instance health 조회
- ADM-EDU-PFW-004 broker DLQ 조회
- ADM-EDU-PFW-005 broker replay 요청
- ADM-EDU-PFW-006 file transfer history 조회
- ADM-EDU-PFW-007 runtime health 조회
- ADM-EDU-PFW-008 worker status 조회
- ADM-EDU-PFW-009 heartbeat/ghost candidate 조회
- ADM-EDU-PFW-010 credential provider status 조회

완료 불인정:
- DTO만 만들고 ADM API/샘플/테스트 없음
- API는 만들었으나 OpenAPI 누락
- ADM runtime 호출 미실행인데 runtime 완료 기록
- 운영자 조치 감사/권한 고려 없이 완료 처리

## 10. 전체 REST API OpenAPI Coverage 검증 강화

목표:
Swagger UI 접근 여부가 아니라 전체 REST API의 OpenAPI 품질을 검사하는 검증 체계를 만든다.

필수 검사:
- @Tag 누락
- @Operation 누락
- operationId 누락
- operationId 중복
- request schema 누락
- response schema 누락
- standard error response 누락
- required header 설명 누락
- transactionGlobalId/header 설명 누락
- 권한 설명 누락
- 마스킹 설명 누락
- 감사 설명 누락
- Controller method와 OpenAPI operation 비교 후보

권장 check task:
- checkOpenApiControllerCoverage
- checkOpenApiOperationId
- checkOpenApiSchema
- checkOpenApiStandardError
- checkOpenApiHeaders
- checkOpenApiSecurityAuditMasking

기준:
- EDU API만 검사하지 않는다.
- CPF 전체 REST Controller/API를 대상으로 한다.
- 가능한 항목은 qualityGate에 연결한다.
- runtime smoke를 실행하지 못하면 static/source coverage와 runtime coverage를 분리한다.

완료 불인정:
- Swagger UI 접근만으로 완료 처리
- EDU API만 검사하고 전체 API 검사 완료 처리
- schema/header/error/security/audit 설명 누락
- runtime smoke 미실행인데 runtime OpenAPI 완료 기록

## 11. sample-coverage-matrix 갱신과 gate 강화

목표:
sample coverage가 실제 샘플, 테스트, evidence, OpenAPI mapping을 추적하도록 만든다.

필수 컬럼 후보:
- sampleId
- module
- package
- featureArea
- sampleName
- sourcePath
- testPath
- evidencePath
- validationLevel
- runtimeRequired
- runtimeExecuted
- status
- notes
- swaggerTag
- swaggerOperationId
- openApiPath
- httpMethod
- openApiVerified

기준:
- 기존 sampleId를 삭제하지 않는다.
- 기존 완료 상태를 임의로 올리지 않는다.
- sourcePath는 실제 sample class 또는 실제 기능 테스트를 가리켜야 한다.
- testPath는 실제 기능/계약/fixture 테스트를 가리켜야 한다.
- evidencePath는 실제 존재해야 한다.
- CoverageCatalog는 완료 근거로 사용하지 않는다.
- REST sample은 swaggerTag, operationId, path, method, verification 여부를 기록한다.
- REST API가 아니면 N/A 또는 비대상 사유를 notes에 기록한다.
- duplicate sampleId를 검사한다.
- 하나의 sourcePath가 과도하게 많은 sampleId를 대표하면 warning 또는 실패로 잡는다.
- 가능한 항목은 qualityGate에 연결한다.

권장 check task:
- checkSampleCoverageColumns
- checkSampleOpenApiMapping
- checkSampleEvidencePathExistence
- checkSampleCatalogOnlyCompletion
- checkSampleDuplicateId
- checkSampleActualSourceAndTestPath
- checkSampleSourceDuplication

완료 불인정:
- matrix 컬럼만 늘리고 실제 sample/test/evidence 없음
- CoverageCatalog-only 완료 기록
- REST sample인데 OpenAPI mapping 없음
- evidencePath가 없는 파일을 가리킴

## 12. Java 25 LTS evidence 기반 착수

목표:
Java 25 LTS 목표를 문서 표기에서 실제 호환성 확인 단계로 전진시킨다.

작업:
- 현재 Java version evidence 생성
- 현재 Gradle version evidence 생성
- Gradle toolchain 설정 확인
- Java 17/21 hardcoding scan
- Spring Boot/Spring Framework/springdoc/annotation processor 호환성 확인
- 가능하면 Java 25로 compile/test/bootJar/qualityGate 시도
- 불가능하면 원인을 gap으로 기록

기준:
- Java 25로 실제 compile/test/bootJar/qualityGate를 수행한 evidence가 있으면 완료 후보
- Java 25가 없거나 toolchain 다운로드가 막히면 미검증 또는 재확인 필요
- Spring Boot major upgrade가 필요하면 무리하게 완료 처리하지 않는다.
- 문서 표기만으로 완료 처리하지 않는다.

권장 evidence:
- specs/evidence/20260709_03/java-version.log
- specs/evidence/20260709_03/gradle-version.log
- specs/evidence/20260709_03/java25-toolchain-scan.sanitized.json
- specs/evidence/20260709_03/java25-compatibility-result.sanitized.json

## 13. Architecture Ownership / Spring Event 검사 강화

목표:
PFW/CMN/업무 모듈 ownership 위반과 Spring Event 남용을 계속 잡을 수 있게 검사 체계를 강화한다.

검사 대상:
- PFW가 EXS/ACC/MBR/BAT/BIZADM/ADM/XYZ에 의존하는지
- CMN이 업무 구현체에 의존하는지
- 업무 주제영역 간 내부 기술 클래스 직접 재사용 여부
- fixed-length/parser/formatter가 EXS 전용인지
- timeout/retry/circuit/failover가 EXS 전용인지
- broker/filetransfer/archive/security/servicecall 기술 engine이 업무 모듈 소유처럼 구현됐는지
- credential/key/cert/secret provider port가 PFW ownership 기준에 맞는지
- Service Call Engine이 PFW ownership 기준에 맞는지
- Spring Event가 핵심 거래 상태 전이, 외부 송신, Saga/compensation, unknown/reconciliation, multi-instance 전달, broker DLQ/replay 중심으로 남용됐는지

권장 evidence:
- specs/evidence/20260709_03/architecture-ownership-scan.sanitized.json
- specs/evidence/20260709_03/spring-event-usage-scan.sanitized.json

완료 불인정:
- PFW가 업무 모듈에 의존
- CMN이 업무 구현체에 의존
- 업무 모듈 간 내부 기술 클래스 직접 재사용
- broker/filetransfer/archive/security/servicecall 기술 engine이 업무 모듈 소유처럼 구현
- 핵심 거래 흐름을 Spring Event만으로 대체

## 14. SQL / Flyway / install 반영 기준

이번 작업에서 persistent table이 필요하다고 판단되면 필요한 SQL/Flyway/install/smoke를 함께 반영한다.

대상 후보:
- service call history
- service/endpoint/instance registry
- broker outbox/inbox/DLQ/replay
- file transfer history
- runtime heartbeat/worker state
- admin status 조회용 table 또는 view 후보

기준:
- table이 필요 없고 in-memory/test-support/reference implementation으로 충분하면 SQL은 만들지 않아도 된다.
- source가 DB table을 전제로 하면 split SQL, Flyway, all_install, smoke 반영을 검토한다.
- 신규 빈 MariaDB full install을 실제 수행하지 않았으면 전체 설치 완료로 기록하지 않는다.

완료 불인정:
- source는 DB table을 전제로 하는데 SQL/Flyway/all_install 미반영
- 기존 개발 DB 일부 확인을 신규 빈 MariaDB full install 완료로 기록
- SQL 파일만 있고 smoke/evidence 없음

## 15. Evidence / Report / Matrix / Gap 정합성

목표:
개발 결과를 검수 가능하게 만들고, 없는 evidence나 stale evidence로 완료 처리하지 않는다.

수행 내용:
- CPF_STABILIZATION_REPORT.md의 신규/변경 evidence path 실제 존재 확인
- CPF_EVIDENCE_INDEX.md의 신규/변경 evidence path 실제 존재 확인
- specs/sample-coverage-matrix.md의 evidencePath 실제 존재 확인
- specs/기능_구현_매트릭스.html 상태값과 report/gap/evidence 정합성 확인
- CPF_GAP_MATRIX.md가 최신 gap을 추적하지 못하면 최소 보강
- 없는 evidence는 완료 근거에서 제거하거나 상태를 미검증/재확인 필요로 낮춤
- qualityGate log 실제 생성
- 기존 유지 증적은 stale 여부, profile/DB/branch/date 적합성 확인

권장 evidence:
- specs/evidence/20260709_03/report-evidence-path-check.sanitized.json
- specs/evidence/20260709_03/evidence-index-path-check.sanitized.json
- specs/evidence/20260709_03/sample-evidence-path-check.sanitized.json
- specs/evidence/20260709_03/matrix-report-consistency.sanitized.json
- specs/evidence/20260709_03/gap-matrix-consistency.sanitized.json
- specs/evidence/20260709_03/quality-gate.log

완료 기준:
- report/index/matrix/gap 상태값 일치
- 없는 evidence를 완료 근거로 사용하지 않음
- 상태값은 완료/부분 구현/미구현/미검증/실패/재확인 필요만 사용
- 실행하지 않은 검증은 미검증으로 기록

## 16. 권장 실행 명령

가능한 범위에서 아래 명령을 실행하고 evidence로 남긴다.

java -version
.\gradlew.bat --version
scripts/check-utf8.ps1 -CheckMojibake
.\gradlew.bat :pfw:test --offline --no-daemon --console=plain
.\gradlew.bat :adm:test --offline --no-daemon --console=plain
.\gradlew.bat test --offline --no-daemon --console=plain
.\gradlew.bat qualityGate --offline --no-daemon --console=plain

가능하면 관련 모듈 테스트도 실행한다.

.\gradlew.bat :cmn:test --offline --no-daemon --console=plain
.\gradlew.bat :xyz:test --offline --no-daemon --console=plain
.\gradlew.bat :bat:test --offline --no-daemon --console=plain
.\gradlew.bat :exs:test --offline --no-daemon --console=plain
.\gradlew.bat :bizadm:test --offline --no-daemon --console=plain

실행하지 못한 명령은 완료로 기록하지 말고 미검증으로 남긴다.

## 17. 신규 evidence 디렉터리

이번 작업의 신규 evidence는 아래에 모은다.

specs/evidence/20260709_03/

권장 evidence:
- pfw-test.log
- adm-test.log
- cmn-test.log
- xyz-test.log
- bat-test.log
- exs-test.log
- bizadm-test.log
- all-test.log
- quality-gate.log
- check-utf8-mojibake.log
- java-version.log
- gradle-version.log
- servicecall-contract-test.sanitized.json
- broker-contract-test.sanitized.json
- filetransfer-contract-test.sanitized.json
- security-credential-contract-test.sanitized.json
- runtime-worker-contract-test.sanitized.json
- adm-pfw-api-contract-test.sanitized.json
- archive-advanced-test.sanitized.json
- java25-toolchain-scan.sanitized.json
- java25-compatibility-result.sanitized.json
- sample-coverage-result.sanitized.json
- sample-catalog-only-scan.sanitized.json
- sample-actual-implementation-scan.sanitized.json
- sample-openapi-mapping-check.sanitized.json
- sample-source-test-path-check.sanitized.json
- sample-duplicate-id-check.sanitized.json
- sample-source-duplication-scan.sanitized.json
- openapi-controller-coverage.sanitized.json
- openapi-operation-id-check.sanitized.json
- openapi-schema-check.sanitized.json
- openapi-standard-error-check.sanitized.json
- openapi-header-security-audit-check.sanitized.json
- report-evidence-path-check.sanitized.json
- evidence-index-path-check.sanitized.json
- sample-evidence-path-check.sanitized.json
- matrix-report-consistency.sanitized.json
- gap-matrix-consistency.sanitized.json
- architecture-ownership-scan.sanitized.json
- spring-event-usage-scan.sanitized.json

## 18. 문서 / Matrix 반영

새로 개발하거나 검증한 결과는 아래에 필요한 만큼 반영한다.

- CPF_STABILIZATION_REPORT.md
- CPF_EVIDENCE_INDEX.md
- CPF_GAP_MATRIX.md
- specs/sample-coverage-matrix.md
- specs/기능_구현_매트릭스.html

기준:
- 장문 문서화로 작업을 대체하지 않는다.
- 상태값은 완료/부분 구현/미구현/미검증/실패/재확인 필요만 사용한다.
- 없는 evidence를 완료 근거로 쓰지 않는다.
- source/contract/API/sample/test 검증과 runtime 검증을 분리한다.
- 새로 개발한 기능은 가능한 범위에서 source/test/sample/OpenAPI/evidence/report가 함께 반영되어야 한다.
- 기존 완료 항목은 실제 evidence가 없거나 stale이면 완료로 유지하지 않는다.

## 19. 후순위 또는 미검증으로 남길 수 있는 항목

아래는 이번 요청에서 source/contract/API/sample/test를 전진시킬 수는 있지만, 실제 runtime을 수행하지 않았으면 완료로 기록하지 않는다.

- 실제 Kafka/MQ broker runtime
- 실제 SFTP/FTP/SCP/SSH 접속 runtime
- Vault/KMS 실제 연동
- 외부 WAS/JNDI runtime
- 브라우저 실제 click 검증
- 다중 WAS/다중 instance failover runtime
- remote real deploy
- TAR archive 실제 구현
- Spring Boot major upgrade 전체 완료
- 전체 CPF 최종 목표 일괄 완료

## 20. 완료 보고 필수 형식

완료 보고에는 아래를 포함한다.

1. 실제 구현한 기능 요약
   - PFW Service Call
   - PFW Broker
   - PFW FileTransfer
   - PFW Security
   - PFW Runtime/Admin
   - PFW Archive
   - ADM 관제 API
   - OpenAPI coverage
   - Sample coverage
   - Java 25
   - Architecture/Spring Event

2. 실제 구현한 source class 목록

3. 실제 구현한 test class 목록

4. 실제 추가/갱신한 sampleId 목록

5. catalog-only에서 실제 sample로 전환한 sampleId 목록

6. 여전히 catalog-only 또는 부분 구현인 sampleId와 사유

7. 실행한 검증 명령

8. 생성한 evidence 경로

9. 상태값별 결과
   - 완료
   - 부분 구현
   - 미구현
   - 미검증
   - 실패
   - 재확인 필요

10. 외부 runtime 미검증 항목
    - broker runtime
    - SFTP/FTP/SCP/SSH runtime
    - Vault/KMS runtime
    - 다중 instance runtime
    - browser click
    - remote real deploy
    - Java 25 compile/test/bootJar/qualityGate 미수행 여부

11. ownership 위반 여부

12. Spring Event 핵심 흐름 사용 여부

13. report/evidence/index/matrix/gap 정합성 결과

14. 다음 보강 후보

15. Git commit/push/branch 미수행 여부

## 21. 완료 불인정 기준

아래 중 하나라도 해당하면 완료로 기록하지 않는다.

- 실제 기능 소스 없이 report/matrix/evidence만 갱신
- CoverageCatalog-only sample을 완료 처리
- sample source/test/evidence 없이 완료 처리
- REST API인데 OpenAPI mapping 없음
- OpenAPI schema/header/error/security/audit 검증 없이 Swagger 완료 처리
- Java 25 문서 표기만 있고 evidence 없음
- qualityGate log 없음
- report/evidence/index/matrix/gap 불일치
- 실제 runtime 미실행인데 runtime 완료 기록
- PFW/CMN/업무 ownership 위반
- Spring Event로 핵심 거래 흐름을 대체
- broker/filetransfer/archive/security/servicecall 기술 engine을 업무 모듈 소유처럼 구현
- 실 Kafka/MQ runtime 없이 broker-real-integration 완료 기록
- 실 SFTP/FTP/SCP/SSH 접속 없이 file transfer runtime 완료 기록
- Vault/KMS 미연동인데 credential runtime 완료 기록
- 다중 instance 미실행인데 failover/selectedInstanceId runtime 완료 기록
- 브라우저 클릭 미실행인데 browser click 완료 기록
- 신규 DB table을 전제로 한 소스가 있는데 SQL/Flyway/install/smoke 반영 또는 gap 기록 없음

## 22. 이번 작업의 최종 산출 목표

이번 작업이 끝나면 아래 상태가 되어야 한다.

1. PFW Service Call Engine이 registry, selectedInstanceId, call history, direct/LB mode를 실제 sample/test/evidence로 보여준다.
2. PFW Broker가 DLQ/replay/outbox/inbox/idempotency를 source/contract/sample/test로 보여준다.
3. PFW FileTransfer가 LOCAL adapter와 SFTP/FTP/SCP/SSH plan, history, unknown result를 source/contract/sample/test로 보여준다.
4. PFW Security가 secret reference, key/cert, signature, encryption/decryption port를 source/contract/sample/test로 보여준다.
5. PFW Runtime/Admin이 worker control, heartbeat, ghost, health, ADM status DTO/API 후보를 source/contract/sample/test로 보여준다.
6. ADM은 PFW capability 관제 후보 API/DTO를 가진다.
7. Archive/Compression은 운영 케이스 테스트가 보강된다.
8. OpenAPI coverage gate가 전체 REST API 기준으로 강화된다.
9. sample coverage matrix가 단순 목록이 아니라 실제 구현/검증 상태를 보여준다.
10. qualityGate가 catalog-only, missing evidence, OpenAPI 누락, ownership 위반을 잡는다.
11. 외부 runtime 미실행 항목은 정직하게 미검증으로 남는다.