# Codex 요청서 04 — CPF 표준 스펙 고정, 기술 Ownership 보정, PFW 공통 Capability 대형 착수, Service Call Runtime Closure 준비

## 0. Codex 공통 작업 지침

아래 기준은 이번 요청서의 모든 작업에 우선 적용한다. 개별 작업 항목에 반복해서 적혀 있지 않아도 반드시 지킨다.

1. CPF 표준 스펙은 흔들지 않는다.
2. 기술 capability는 업무 주제영역에 종속시키지 않는다.
3. PFW는 기본 프레임워크 기술 기능을 소유한다.
4. CMN은 프로젝트 공통 커스텀/업무 공통 확장을 소유한다.
5. EXS/ACC/MBR/BAT/BIZADM/EDU/XYZ는 PFW/CMN capability를 사용하는 consumer, adapter, 업무 설정, 업무 구현체 역할만 한다.
6. EXS는 외부연계 기술 소유자가 아니다. EXS는 대외업무 대표 adapter/구현체다.
7. OAuth/JWT/mTLS, timeout/retry/circuit/failover, fixed-length parser/formatter/layout, unknown result, reconciliation, outbox/inbox/idempotency, broker port는 PFW/CMN 공통 capability로 둔다.
8. Kafka/MQ/Redis Stream 같은 broker 기술 capability는 PFW가 소유한다.
9. SSH/SFTP/FTP/FTPS 같은 파일 전송 기술 capability도 PFW 기본 capability 후보로 둔다.
10. CMN은 프로젝트 공통 메시지 규격, topic naming rule, 파일명/디렉터리 규칙, validation/helper/fixture를 담당한다.
11. 업무 주제영역은 PFW/CMN capability를 사용해 업무별 adapter와 업무 처리를 구현한다.
12. PFW/CMN이 업무 주제영역에 의존하면 안 된다.
13. 업무 주제영역 간 내부 기술 클래스 재사용을 금지한다.
14. 업무 코드는 URL 직접 조합, Controller 직접 호출, 타 주제영역 DB/Mapper/Repository 직접 접근을 금지한다.
15. Spring Event는 핵심 거래 흐름, 외부 송신, saga/compensation, unknown result, reconciliation, multi-instance 전달, DLQ/replay의 중심 기술로 사용하지 않는다.
16. 핵심 흐름은 DB 상태, transactionGlobalId, segment/timeline, outbox/inbox, idempotency, broker/scheduler 재처리 구조를 우선한다.
17. `CPF_FINAL_TARGET_REQUIREMENTS.md`는 로컬 checkout 기준으로 관련 키워드/REQ-ID/섹션을 검색해 작업 기준을 잡는다.
18. 목표파일을 삭제/축약/덮어쓰기 하지 않는다.
19. 문서는 개발 중 정본화하지 않고 검수 가능한 최소 기록만 남긴다.
20. 실행하지 않은 검증을 완료로 기록하지 않는다.
21. source/test/SQL/Flyway/all_install/smoke/evidence/report/matrix 정합성을 맞춘다.
22. Git commit/push/branch 생성은 금지한다.

## 1. 이번 요청의 최우선 목적

이번 요청은 후속 개발이 흔들리지 않도록 CPF 표준 스펙과 기술 ownership 틀을 먼저 고정하고, 동시에 PFW 공통 capability 기반을 크게 넓히는 작업이다.

이번 요청에서 우선순위는 아래 순서다.

1. `CPF_REVIEW_PROGRESS_COMPLETION_GUIDE.md` 교체/보강
2. PFW/CMN/업무 주제영역 ownership 기준을 source scan/qualityGate에 반영
3. EXS 기술 종속 금지 구조 점검 및 위반 후보 정리
4. Spring Event 남용 금지 scan 추가
5. PFW 공통 capability 패키지/port/interface 대형 착수
6. PFW Service Call Engine 실제 runtime closure 준비 강화
7. ADM Service Registry runtime/browser 검증 준비 강화
8. SQL/Flyway/all_install/evidence 정합성 유지
9. report/matrix/evidence/gap 최소 정합성 기록

## 2. 필수 완료 범위

### 2.1 `CPF_REVIEW_PROGRESS_COMPLETION_GUIDE.md` 교체/보강

사용자가 제공한 최신 교체용 `CPF_REVIEW_PROGRESS_COMPLETION_GUIDE.md` 내용을 repo root의 동일 파일에 반영한다.

필수 반영:

* Codex 공통 작업 지침 섹션
* PFW/CMN/업무 주제영역 ownership 기준
* EXS는 외부연계 기술 소유자가 아니라 대외업무 대표 adapter/구현체라는 기준
* Kafka/MQ/Redis Stream은 PFW broker capability라는 기준
* SSH/SFTP/FTP/FTPS는 PFW file transfer capability 후보라는 기준
* CMN은 프로젝트 공통 규칙/helper/fixture 담당이라는 기준
* Spring Event 남용 금지 기준
* 중간 문서 최소 기록 기준
* 요청서 템플릿 맨 앞에 Codex 공통 작업 지침 포함
* PFW/CMN 연계 공통 capability + EXS adapter 기준
* Architecture rule / forbidden dependency / evidence 기준

주의:

* 문서 정본화 목적의 장문 추가 금지
* HTML/PDF 생성 금지
* 기능 목표를 새로 장문 확장하지 말 것
* 검수와 요청서 작성에 필요한 기준만 반영
* 기존 기준과 충돌하는 EXS 기술 소유 표현을 제거하거나 보정

완료 기준:

* `CPF_REVIEW_PROGRESS_COMPLETION_GUIDE.md`에 위 기준이 반영됨
* `check-utf8.ps1 -CheckMojibake` 통과
* `check-report-matrix-evidence-consistency.ps1` 통과
* report/evidence에 지침 보강 evidence 기록

### 2.2 기술 ownership architecture scan 강화

기존 `scripts/check-service-call-boundary.ps1`를 확장하거나 별도 `scripts/check-architecture-ownership.ps1`를 추가한다.

필수 scan 항목:

* PFW가 `cpf.exs`, `cpf.acc`, `cpf.mbr`, `cpf.bat`, `cpf.bizadm`, `cpf.adm`, `cpf.xyz` 구현체에 의존하지 않는지
* CMN이 업무 주제영역 구현체에 의존하지 않는지
* ACC/MBR/BAT/BIZADM/XYZ가 EXS 내부 기술 클래스를 공통 기능처럼 import하지 않는지
* 업무 주제영역 간 Controller/Repository/Mapper 직접 import 금지
* 업무 코드의 raw `WebClient.builder`, `RestTemplate`, `RestClient.create`, URL 직접 조합 금지
* fixed-length parser/formatter가 EXS 전용으로만 존재하지 않는지
* timeout/retry/circuit/failover가 EXS 전용 구현으로 고립되지 않았는지
* OAuth/JWT/mTLS/secret/key/cert 처리가 EXS 또는 특정 업무 모듈 전용으로 고립되지 않았는지
* unknown result/reconciliation 표준 모델이 EXS 전용으로 고립되지 않았는지
* Kafka/MQ/Redis Stream adapter가 업무 주제영역 전용으로 박혀 있지 않은지
* SSH/SFTP/FTP/FTPS 전송 기술이 EXS/BAT 등 특정 업무 영역 전용으로 박혀 있지 않은지

완료 기준:

* architecture ownership scan script 추가 또는 기존 script 확장
* qualityGate에 연결
* scan 성공 evidence 생성
* 위반이 있으면 숨기지 않고 report/gap에 `부분 구현` 또는 `재확인 필요`로 남김

### 2.3 Spring Event 남용 scan 추가

Spring Event 사용처를 source scan한다.

필수 scan 대상:

* `ApplicationEventPublisher`
* `@EventListener`
* `ApplicationListener`
* custom domain event class
* EventListener에서 외부 송신하는 코드
* EventListener 체인으로 거래 상태 전이를 처리하는 코드
* Saga/compensation/unknown result/reconciliation을 EventListener 중심으로 처리하는 코드
* multi-instance 전달, DLQ/replay를 Spring Event로 대체하려는 코드

완료 기준:

* 단순 hook/telemetry/cache invalidation/감사 보조 용도는 허용
* 핵심 거래 흐름에 해당하면 report/gap에 `재확인 필요` 또는 `부분 구현`으로 남김
* scan script 또는 JSON evidence 생성
* qualityGate에 최소 scan 연결

### 2.4 PFW 공통 capability 패키지/port/interface 착수

이번 요청은 PFW 공통 capability를 실제 구현 완료하라는 뜻이 아니라, 후속 개발이 업무 주제영역에 잘못 박히지 않도록 기본 틀을 잡는 작업이다. 단, 문서만 쓰지 말고 package/interface/skeleton/test를 포함한다.

PFW capability 후보 패키지:

```text
pfw.common.servicecall
pfw.common.broker
pfw.common.filetransfer
pfw.common.storage
pfw.common.security
pfw.common.transaction
pfw.common.runtime
pfw.common.audit
pfw.common.admin
```

필수 skeleton:

1. Broker capability

   * `CpfBrokerPublisher`
   * `CpfBrokerConsumer`
   * `CpfBrokerMessage`
   * `CpfBrokerEnvelope`
   * `CpfBrokerResult`
   * `CpfBrokerDlqPort`
   * `CpfBrokerReplayPort`
   * `CpfBrokerHealthPort`
   * Kafka/MQ/Redis Stream adapter 후보는 interface/port까지만 착수 가능

2. File Transfer capability

   * `CpfFileTransferClient`
   * `CpfFileTransferPort`
   * `CpfFileTransferRequest`
   * `CpfFileTransferResult`
   * `CpfFileTransferEndpoint`
   * `CpfFileTransferHistoryPort`
   * `CpfFileTransferHealthPort`
   * SSH/SFTP/FTP/FTPS adapter 후보는 interface/port까지만 착수 가능

3. Security/Credential capability

   * `CpfCredentialRef`
   * `CpfSecretProviderPort`
   * `CpfKeyProviderPort`
   * `CpfCertificateProviderPort`
   * `CpfSignaturePort`
   * `CpfEncryptionPort`
   * mTLS/JWT/OAuth credential linkage 후보

4. Runtime Control capability

   * `CpfDistributedLockPort`
   * `CpfHeartbeatPort`
   * `CpfHealthCheckPort`
   * `CpfGhostDetectorPort`
   * scheduler/worker 연계 후보

5. Admin status/query DTO 후보

   * broker status query DTO
   * file transfer status query DTO
   * credential status query DTO
   * runtime health status query DTO

완료 기준:

* 최소 compile 가능한 interface/skeleton
* PFW test 추가
* 업무 주제영역 의존 없음
* README 또는 report에 “착수/부분 구현”으로 최소 기록
* 실제 Kafka/SFTP/FTP runtime은 실행하지 않았으면 `미검증`
* skeleton만 만들고 완료로 기록하지 않음

### 2.5 CMN 책임 경계 보강

CMN은 기술 엔진 자체가 아니라 프로젝트 공통 규칙/확장/helper를 담당한다.

CMN 후보:

* 공통 코드/메시지
* 프로젝트 표준 오류/상태 코드 확장
* broker topic naming rule
* message envelope extension helper
* 파일명/디렉터리 규칙
* fixed-length layout/parser/formatter
* CSV/TSV/JSON/XML helper
* payload validation
* fixture/test helper
* 업무 공통 converter/helper

필수 작업:

* CMN에 기술 engine 구현이 들어가지 않도록 ownership scan 기준에 반영
* CMN이 PFW port를 사용하는 helper 구조로 잡히도록 최소 package/README/report 기록
* 기존 CMN fixed-length engine이 EXS 전용이 아님을 report/gap 또는 guide에 최소 기록
* CMN이 업무 주제영역 구현체에 의존하지 않는지 scan

### 2.6 EXS/ACC/MBR/BAT/BIZADM 업무 주제영역 점검

각 업무 주제영역이 PFW/CMN capability consumer/adapter 역할인지 점검한다.

필수 점검:

* EXS 내부에 공통 HTTP/retry/circuit/OAuth/JWT/mTLS/fixed-length parser가 독점 구현되어 있지 않은지
* ACC/MBR/BAT가 EXS 내부 기술 클래스를 import하지 않는지
* 업무 주제영역이 타 주제영역 DB/Mapper/Repository를 직접 호출하지 않는지
* 업무 주제영역이 PFW Service Call Engine, PFW broker/filetransfer/security port, CMN parser/helper를 사용하는 방향인지
* 기존 `exs-timeout-retry-runtime` 완료 표기가 “EXS가 PFW capability를 사용한 업무 runtime 검증”인지, “EXS가 기술 소유”처럼 오해될 표현인지 점검

완료 기준:

* 위반 없음 또는 위반 후보 목록 생성
* 위반 후보가 있으면 source path, 이유, 권장 이동 위치(PFW/CMN/업무 adapter)를 기록
* 위반 후보를 완료로 숨기지 않음

### 2.7 Evidence/report/matrix/gap 정합성 유지

현재 `20260707_03` evidence 기준을 유지하되, 이번 작업의 evidence는 새 디렉터리 `specs/evidence/20260708_01`에 생성한다.

필수 반영 파일:

* `CPF_STABILIZATION_REPORT.md`
* `CPF_GAP_MATRIX.md`
* `CPF_EVIDENCE_INDEX.md`
* `specs/기능_구현_매트릭스.html`
* `README.md`
* qualityGate 관련 scripts

완료 기준:

* 새 check id는 report/evidence/matrix/gap 간 일치
* 완료/부분 구현 항목은 실제 존재하는 evidence 경로만 참조
* 없는 `.log` 또는 stale evidence 경로 제거
* `pfw-service-call-engine`은 실제 다중 서비스 HTTP runtime 전까지 `부분 구현`
* `adm-service-registry-runtime`은 실제 `-RunRuntime` 전까지 `미검증`
* PFW broker/filetransfer/security skeleton은 `부분 구현` 또는 `착수` 성격으로 기록
* broker/filetransfer real runtime은 실제 실행 전까지 `미검증`
* ADM static UI는 browser click 완료가 아니라 static smoke로만 기록
* MariaDB full install 재실행 전까지 이번 작업 기준 완료로 새로 올리지 않음
* 상태값은 `완료`, `부분 구현`, `미구현`, `미검증`, `실패`, `재확인 필요`만 사용

## 3. 보강 범위

### 3.1 PFW Service Call Engine runtime closure 준비 보강

이전 작업은 source/unit contract 중심이다. 이번에는 실제 runtime 검증을 위한 준비를 더 강화한다.

필수 보강:

* `runtime-start-services.ps1`와 service-call smoke 연계 확인
* ACC/MBR/EXS/ADM multi-service 기동 후 호출 시나리오 정의
* success/failure/timeout/retry/failover/circuit/call-history 검증 항목 명확화
* `RunRuntime` 없이 실행한 source smoke와 실제 runtime smoke를 evidence에서 구분
* `RequireRuntime` 옵션이 있는 경우 source-only로 성공 처리하지 않도록 유지/강화
* selectedInstanceId, transactionGlobalId, segment/timeline, call history가 runtime smoke에서 확인될 수 있도록 준비
* service/endpoint/instance registry seed가 runtime 테스트에 필요한 최소 데이터를 갖추는지 확인

실제 multi-service runtime이 환경상 불가능하면:

* 완료로 기록하지 말 것
* `미검증`으로 남길 것
* 필요한 포트/profile/env/DB 조건을 report/gap에 최소 기록할 것

### 3.2 ADM Service Registry runtime/browser 준비 보강

필수 보강:

* `scripts/smoke-adm-service-registry-runtime.ps1 -RunRuntime` 실제 실행 조건 정리
* service/endpoint/instance/health/routing/circuit/call-history API 목록 정리
* browser click script 또는 Playwright 환경 조건 정리
* static UI smoke와 browser click smoke를 명확히 분리
* ADM 화면에서 service/endpoint/instance/health/circuit/call-history가 운영자가 보기 좋은 최소 구조인지 점검
* 권한/감사/마스킹/검색조건이 누락된 경우 gap으로 남김

실행하지 못하면 `미검증` 유지.

### 3.3 SQL/Flyway/all_install 재검증 준비

이번 작업에서 MariaDB full install을 직접 하지 못하면 완료로 올리지 않는다.

필수 보강:

* service registry 관련 split SQL/Flyway/all_install/99_smoke_check 정합성 점검
* broker/filetransfer/security skeleton이 DB 테이블을 만들지 않는다면 SQL 반영 불필요 사유 기록
* DB 테이블을 추가한다면 split SQL/Flyway/all_install/99_smoke_check 모두 반영
* 실제 MariaDB 신규 빈 DB 재설치가 불가능하면 `미검증` 유지
* `CPF_GAP_MATRIX.md`에 남길 것

### 3.4 qualityGate 보강

qualityGate에 아래 항목을 연결한다.

* ownership architecture scan
* Spring Event misuse scan
* service-call boundary scan
* report/matrix/evidence consistency
* feature evidence check
* UTF-8/mojibake check
* HTML docs check
* forbidden state value check
* missing evidence path check
* SKIPPED evidence 완료 오인 방지 check

완료 기준:

* qualityGate 성공 로그 생성
* 실패 시 실패로 남김
* qualityGate가 실제로 새 scan을 호출하는지 evidence에 기록

## 4. 착수 범위

아래는 큰 구조를 잡되 실제 runtime 완료로 기록하지 않는다.

### 4.1 PFW Broker capability 착수

목표:

* Kafka/MQ/Redis Stream을 PFW broker capability로 둘 수 있는 port/interface skeleton 마련
* publish/consume/retry/DLQ/replay/ordering/idempotency/outbox/inbox 연계 후보 구조 정의
* broker message envelope에 transactionGlobalId, segment, producerModule, consumerModule, messageId, idempotencyKey 후보 포함
* broker health/status/call-history 후보 DTO 마련
* ADM broker 관제 후보 route/API 설계 메모 최소 기록

완료 판정:

* compile/test 통과 시 `부분 구현`
* real broker 검증 전까지 `미검증` 유지
* Kafka/MQ/Redis Stream 실제 adapter는 이번 요청에서 완료로 주장하지 않음

### 4.2 PFW File Transfer capability 착수

목표:

* SSH/SFTP/FTP/FTPS를 PFW file transfer capability로 둘 수 있는 port/interface skeleton 마련
* connection profile, endpoint, credential, timeout/retry, checksum, temp/rename/archive, duplicate prevention, transfer history 후보 구조 정의
* file transfer request/result에 transactionGlobalId, segment, endpointCode, remotePath, localPath, checksum, fileSize, transferStatus 후보 포함
* ADM file transfer 관제 후보 route/API 설계 메모 최소 기록

완료 판정:

* compile/test 통과 시 `부분 구현`
* 실제 SFTP/FTP/SSH 서버 검증 전까지 `미검증`
* EXS 파일 송수신 업무 구현은 PFW file transfer port를 사용하는 adapter로만 착수 가능

### 4.3 PFW Security/Credential capability 착수

목표:

* secret/key/cert/mTLS/JWT/OAuth/private key/known_hosts/encryption/signature를 PFW 기본 capability 후보로 정리
* credential reference 방식과 원문 secret 저장 금지 기준 마련
* 업무 주제영역에 credential 처리 기술이 박히지 않도록 port 기준 마련
* masking/audit와 연결 후보 정리

완료 판정:

* compile/test 통과 시 `부분 구현`
* 실제 key vault/HSM/secret manager 연동은 미검증 또는 후순위

### 4.4 ADM 관제 확장 후보 착수

목표:

* ADM Service Registry 관제와 연계해서 broker/filetransfer/security/runtime health 관제 후보를 잡는다.
* 실제 화면 완성보다 API/DTO/route 후보와 static UI marker 수준으로 착수한다.
* browser click은 실제 실행 전까지 `미검증`.

후보 메뉴:

* Service Registry
* Broker Registry / Message History
* File Transfer Endpoint / Transfer History
* Credential Reference / Certificate Status
* Runtime Health / Heartbeat / Ghost
* Circuit / Retry / DLQ / Replay

## 5. 후순위/제외 범위

이번 요청에서 완료로 주장하지 말 것:

* 실제 Kafka/MQ/Redis Stream real broker runtime 완료
* 실제 SFTP/FTP/SSH 서버 송수신 runtime 완료
* ADM browser click 완료
* ACC/MBR/EXS/ADM 2대/3대 multi-instance 실검증 완료
* MariaDB 신규 빈 DB full install 완료, 실제 실행하지 않은 경우
* PDF/HTML 정본화
* 전체 최종 목표파일 split 완료
* 전체 CPF 상용화 완료
* PFW broker/filetransfer/security capability 상용 수준 완성

## 6. 필수 실행 명령

가능한 범위에서 아래를 실행하고 evidence를 남긴다.

```powershell
.\gradlew.bat :pfw:test --offline --no-daemon --console=plain --tests cpf.pfw.common.servicecall.*
.\gradlew.bat :adm:test --offline --no-daemon --console=plain --tests cpf.adm.opr.controller.AdmServiceRegistryControllerTest
.\gradlew.bat :pfw:test --offline --no-daemon --console=plain

powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-service-call-boundary.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-architecture-ownership.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-spring-event-usage.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-report-matrix-evidence-consistency.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-feature-evidence.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-html-docs.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-utf8.ps1 -CheckMojibake

powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-service-call-engine-runtime.ps1 -ResultDir specs/evidence/20260708_01
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-service-call-engine-circuit-runtime.ps1 -ResultDir specs/evidence/20260708_01
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-service-call-engine-failover-runtime.ps1 -ResultDir specs/evidence/20260708_01
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-service-registry-health-runtime.ps1 -ResultDir specs/evidence/20260708_01
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-adm-service-registry-ui-static.ps1 -ResultDir specs/evidence/20260708_01

.\gradlew.bat qualityGate --offline --no-daemon --console=plain
```

실제 runtime 환경이 준비되어 있으면 아래도 실행한다.

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/runtime-start-services.ps1 -Modules ACC,MBR,EXS,ADM -BuildBeforeRun
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-service-call-engine-runtime.ps1 -RunRuntime -RequireRuntime -ResultDir specs/evidence/20260708_01
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-adm-service-registry-runtime.ps1 -RunRuntime -ResultDir specs/evidence/20260708_01
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-mariadb-full-install.ps1
```

실행하지 못한 명령은 완료로 기록하지 말고 미검증과 사유를 기록한다.

## 7. 산출물 기록 기준

문서 정본화 금지. 아래 최소 기록만 한다.

* `CPF_STABILIZATION_REPORT.md`: 상태값, 실행 명령, evidence 경로, 미검증 사유
* `CPF_GAP_MATRIX.md`: 남은 gap과 우선순위
* `CPF_EVIDENCE_INDEX.md`: 실제 존재하는 evidence 경로만 기록
* `specs/기능_구현_매트릭스.html`: check id/status/evidence 최소 정합성
* `README.md`: 깨진 smoke 명령이나 주요 진입점만 최소 수정
* `CPF_REVIEW_PROGRESS_COMPLETION_GUIDE.md`: 사용자가 제공한 최신 기준 반영

## 8. 완료 불인정 기준

아래 중 하나라도 해당하면 완료로 기록하지 않는다.

* PFW/CMN이 업무 주제영역에 의존
* 업무 주제영역이 EXS 내부 기술 클래스를 공통 기능처럼 사용
* Kafka/SFTP/FTP/OAuth/JWT/mTLS/retry/circuit/unknown result/reconciliation이 특정 업무 주제영역 전용으로 구현
* Spring Event만으로 핵심 거래/외부 송신/복구/대사 완료 주장
* source-only smoke를 real runtime으로 기록
* static UI smoke를 browser click 완료로 기록
* embedded/mock broker를 real broker 완료로 기록
* 기존 개발 DB 확인을 신규 MariaDB full install 완료로 기록
* 없는 evidence 파일을 완료 근거로 참조
* skeleton/interface만 만들고 runtime 완료로 기록
* `CPF_FINAL_TARGET_REQUIREMENTS.md`를 삭제/축약/덮어쓰기
* Git commit/push/branch 생성

## 9. Codex 완료 보고 시 필수 포함

작업 완료 보고에는 아래를 반드시 포함한다.

1. 실제 수정한 핵심 파일
2. 새로 추가한 PFW capability package/interface 목록
3. ownership scan 결과
4. Spring Event scan 결과
5. Service Call Engine runtime/source-only 구분
6. ADM Service Registry runtime/browser 검증 여부
7. SQL/Flyway/all_install 반영 여부
8. 실제 실행 명령
9. evidence 경로
10. 완료/부분 구현/미검증/실패/재확인 필요 분리
11. 후속 gap
12. Git commit/push/branch 미수행 여부
