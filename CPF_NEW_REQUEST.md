# Codex 요청서 05 — Evidence 정합성 복구, 환경/Profile 표준화, 원격배포 기반, PFW Capability 실사용 전진

## 0. Codex 공통 작업 지침

아래 기준은 이번 요청서의 모든 작업에 우선 적용한다. 개별 작업 항목에 반복해서 적혀 있지 않아도 반드시 지킨다.

1. CPF 표준 스펙은 흔들지 않는다.
2. 기술 capability는 업무 주제영역에 종속시키지 않는다.
3. PFW는 기본 프레임워크 기술 기능을 소유한다.
4. CMN은 프로젝트 공통 커스텀/업무 공통 확장을 소유한다.
5. EXS/ACC/MBR/BAT/BIZADM/EDU/XYZ는 PFW/CMN capability를 사용하는 consumer, adapter, 업무 설정, 업무 구현체 역할만 한다.
6. EXS는 외부연계 기술 소유자가 아니다. EXS는 대외업무 대표 adapter/구현체다.
7. Kafka/MQ/Redis Stream 같은 broker 기술 capability는 PFW가 소유한다.
8. SSH/SFTP/FTP/FTPS 같은 file transfer 기술 capability도 PFW 기본 capability 후보로 둔다.
9. OAuth/JWT/mTLS, timeout/retry/circuit/failover, unknown result, reconciliation, outbox/inbox/idempotency, broker port, file transfer port는 PFW/CMN 공통 capability로 둔다.
10. fixed-length parser/formatter/layout, 공통 메시지/코드, 공통 validation/helper/fixture는 CMN capability로 둔다.
11. PFW/CMN이 업무 주제영역에 의존하면 안 된다.
12. 업무 주제영역 간 내부 기술 클래스 재사용을 금지한다.
13. 업무 코드는 URL 직접 조합, Controller 직접 호출, 타 주제영역 DB/Mapper/Repository 직접 접근을 금지한다.
14. Spring Event는 핵심 거래 흐름, 외부 송신, saga/compensation, unknown result, reconciliation, multi-instance 전달, DLQ/replay의 중심 기술로 사용하지 않는다.
15. 핵심 흐름은 DB 상태, transactionGlobalId, segment/timeline, outbox/inbox, idempotency, broker/scheduler 재처리 구조를 우선한다.
16. `CPF_FINAL_TARGET_REQUIREMENTS.md`는 로컬 checkout 기준으로 관련 키워드/REQ-ID/섹션을 검색해 작업 기준을 잡는다.
17. 목표파일을 삭제/축약/덮어쓰기 하지 않는다.
18. 문서는 개발 중 정본화하지 않고 검수 가능한 최소 기록만 남긴다.
19. 실행하지 않은 검증을 완료로 기록하지 않는다.
20. source/test/SQL/Flyway/all_install/smoke/evidence/report/matrix 정합성을 맞춘다.
21. Git commit/push/branch 생성은 금지한다.

## 1. 이번 요청의 목적

이번 요청은 문서 정리만 하는 작업이 아니다.
지난 작업에서 추가된 PFW capability skeleton, architecture ownership scan, Spring Event scan을 기준으로 **보고/증적 정합성을 복구하고**, 동시에 **환경/profile 표준화와 원격 배포 기반을 크게 전진**시킨다.

작업 비중은 아래를 목표로 한다.

```text
개발/소스/스크립트/검증: 70%
report/evidence/matrix/gap 최소 정합성: 20%
후속 runtime 준비: 10%
```

## 2. 선행 필수 — report/evidence/matrix/guide 정합성 복구

### 2.1 `20260708_01` evidence 정합성 반영

현재 `specs/evidence/20260708_01`에는 아래 증적이 존재한다.

* `architecture-ownership-scan.sanitized.json`
* `spring-event-usage-scan.sanitized.json`
* `service-call-engine-runtime-success.sanitized.json`
* `service-call-engine-circuit-transition.sanitized.json`
* `service-call-engine-failover.sanitized.json`
* `service-registry-health-runtime.sanitized.json`
* `adm-service-registry-runtime-result.json`
* `adm-service-registry-ui-static-smoke.sanitized.json`

그런데 GitHub master 기준으로 `CPF_STABILIZATION_REPORT.md`, `CPF_EVIDENCE_INDEX.md`가 아직 `20260707_03` 또는 이전 증적 중심으로 보일 수 있다. 이를 반드시 정합화한다.

필수 작업:

* `CPF_STABILIZATION_REPORT.md`에 `20260708_01` 작업 결과 반영
* `CPF_EVIDENCE_INDEX.md`에 `20260708_01` evidence 경로 반영
* `CPF_GAP_MATRIX.md`에 남은 gap 반영
* `specs/기능_구현_매트릭스.html`의 check id/status/evidence 동기화
* `check-report-matrix-evidence-consistency.ps1`가 `20260708_01` 경로를 제대로 검증하는지 확인

완료 기준:

* 완료/부분 구현/미검증 상태가 report, evidence index, matrix, gap에서 서로 일치
* 존재하지 않는 evidence 경로 제거
* stale evidence를 새 작업 완료 근거로 사용하지 않음
* `pfw-capability-skeleton`은 `부분 구현`
* `architecture-ownership-scan`은 failure 0건이지만 warning 6건으로 `재확인 필요`를 유지
* `spring-event-usage-scan`은 forbidden 0건이면 완료 후보
* `pfw-service-call-engine-runtime`은 실제 다중 서비스 HTTP runtime 전까지 `부분 구현`
* `adm-service-registry-runtime`은 실제 `-RunRuntime` 전까지 `미검증`
* static UI smoke는 browser click 완료로 올리지 않음

### 2.2 `CPF_REVIEW_PROGRESS_COMPLETION_GUIDE.md` 최신 기준 반영 재확인

사용자가 제공한 최신 교체용 기준을 repo root의 `CPF_REVIEW_PROGRESS_COMPLETION_GUIDE.md`에 반영한다.

필수 포함:

* Codex 공통 작업 지침
* PFW/CMN/업무 주제영역 ownership
* EXS는 외부연계 기술 소유자가 아니라 대외업무 대표 adapter/구현체
* Kafka/MQ/Redis Stream은 PFW broker capability
* SSH/SFTP/FTP/FTPS는 PFW file transfer capability
* Spring Event 남용 금지
* 중간 문서 최소 기록
* 요청서 템플릿 맨 앞에 Codex 공통 작업 지침
* PFW/CMN 연계 공통 capability + EXS adapter 기준
* architecture rule / forbidden dependency / evidence 기준

주의:

* 문서 정본화 금지
* HTML/PDF 생성 금지
* 장문 설명 추가 금지
* 검수와 요청서 작성에 필요한 기준만 반영
* `CPF_FINAL_TARGET_REQUIREMENTS.md`는 수정하지 않음

완료 기준:

* `CPF_REVIEW_PROGRESS_COMPLETION_GUIDE.md`에서 `Codex 공통 작업 지침` 검색 가능
* `EXS는 외부연계 기술의 소유자가 아니다` 검색 가능
* `PFW broker capability`, `file transfer capability`, `Spring Event` 기준 검색 가능
* UTF-8/mojibake check 통과

## 3. Architecture ownership warning 6건 해소 또는 명확화

지난 scan 결과 기준 재확인 필요 후보가 6건 있다.

* `cmn/src/main/java/cpf/cmn/fle/core/CmnFileProtocol.java`
* `cmn/src/main/java/cpf/cmn/fle/core/CmnFileTransferRequest.java`
* `cmn/src/main/java/cpf/cmn/fle/core/CmnRemoteCommandRequest.java`
* `cmn/src/main/java/cpf/cmn/fle/service/CmnFileExchangeService.java`
* `cmn/src/main/java/cpf/cmn/mqe/service/CmnMessageBridgeService.java`
* `xyz/src/main/java/cpf/xyz/edu/controller/XyzServiceCallEducationController.java`

필수 작업:

1. CMN `fle` 파일들이 실제 file transfer technical engine인지, 아니면 프로젝트 공통 파일 규칙/helper인지 분류한다.
2. 실제 SSH/SFTP/FTP/remote command 기술 engine이면 PFW `filetransfer`/`runtime` port 쪽으로 이동 또는 adapter 분리 후보로 만든다.
3. CMN에 남길 경우 이름/패키지/주석/역할을 “프로젝트 공통 파일 규칙/helper”로 명확히 한다.
4. CMN `mqe`가 broker engine인지 message envelope/helper인지 분류한다.
5. broker 기술 engine이면 PFW `broker` port 쪽으로 이동 또는 adapter 분리 후보로 만든다.
6. XYZ EDU URL literal은 교육용 fixture인지, 실제 업무 코드의 direct URL인지 분리한다.
7. 교육용이면 scan allowlist 또는 `education-only` marker로 명확히 한다.
8. 실제 호출이면 Service Call Engine registry 기반으로 변경한다.

완료 기준:

* architecture ownership scan warning이 0건이 되거나,
* warning이 남는 경우 각 warning별로 “의도된 예외 / 후속 보정 필요 / 실제 위반”을 report/gap에 명확히 기록
* failure는 0건 유지
* qualityGate 통과

## 4. PFW capability skeleton 보강

지난 작업에서 PFW broker/filetransfer/security/runtime/admin skeleton은 착수됐다. 이번 요청에서는 누락 후보와 실사용 연결 가능성을 보강한다.

### 4.1 Broker capability 보강

필수 후보:

* `CpfBrokerHistoryPort`
* broker publish/consume history DTO
* outbox/inbox/idempotency 연계 후보 interface
* DLQ/replay request/result DTO
* broker health/status query DTO 보강

완료 기준:

* compile 가능한 interface/record
* PFW unit/contract test 추가
* Kafka/MQ/Redis 실제 runtime은 미검증 유지
* 업무 주제영역에 broker client 직접 구현 금지 scan 유지

### 4.2 File Transfer capability 보강

필수 후보:

* SSH/SFTP/FTP/FTPS protocol enum 또는 value object
* temp file / rename / archive policy 후보
* checksum validation 후보
* duplicate prevention 후보
* transfer retry policy 후보
* transfer history query DTO

완료 기준:

* compile 가능한 interface/record
* PFW unit/contract test 추가
* 실제 SFTP/FTP/SSH 서버 runtime은 미검증 유지
* EXS/BAT에 file transfer engine이 직접 박히지 않도록 scan 유지

### 4.3 Security/Credential capability 보강

필수 후보:

* `CpfTokenProviderPort`
* `CpfCredentialProviderPort`
* credential reference validation
* secret 원문 출력 방지 test
* mTLS/JWT/OAuth credential linkage 후보 interface

완료 기준:

* 원문 secret/password/token/private key가 DTO, log, evidence에 남지 않음
* PFW unit/contract test 추가
* 실제 vault/HSM/secret manager 연동은 미검증 또는 후순위 유지

### 4.4 Runtime capability 보강

필수 후보:

* lock acquire/release result
* heartbeat request/result
* ghost detection result
* scheduler/worker control request/result
* runtime health query DTO

완료 기준:

* PFW compile/test 통과
* 실제 multi-instance runtime은 미검증 유지
* ADM 관제 후보 DTO와 연결 가능한 구조

## 5. 환경별 yml/profile 표준화

### 5.1 profile 표준

CPF profile 표준은 아래 4개로 통일한다.

```text
local : 개발자 로컬 PC
dev   : 개발 서버 / 개발존
stg   : 스테이징 / 검증존
prod  : 운영존
```

`prd`가 아니라 `prod`를 표준으로 사용한다.

### 5.2 PFW/CMN 환경별 yml

PFW/CMN은 실행 주체가 아니다. 다만 각 업무 서비스 bootJar 내부 classpath에서 library로 동작하므로 zone별 공통 정책을 제공하기 위한 환경별 yml을 둔다.

PFW:

```text
pfw/src/main/resources/application-pfw.yml
pfw/src/main/resources/application-pfw-local.yml
pfw/src/main/resources/application-pfw-dev.yml
pfw/src/main/resources/application-pfw-stg.yml
pfw/src/main/resources/application-pfw-prod.yml
```

CMN:

```text
cmn/src/main/resources/application-cmn.yml
cmn/src/main/resources/application-cmn-local.yml
cmn/src/main/resources/application-cmn-dev.yml
cmn/src/main/resources/application-cmn-stg.yml
cmn/src/main/resources/application-cmn-prod.yml
```

PFW/CMN yml은 공통 기본값과 zone별 기본 정책만 제공한다. `spring.application.name`, `server.port`, `spring.profiles.active`, 업무 datasource, module-id, instance-id, logging path를 강제로 덮지 않는다.

### 5.3 업무 서비스 환경별 yml

대상 모듈:

```text
ACC / MBR / EXS / ADM / BAT / BIZADM / EDU / XYZ
```

각 업무 서비스는 아래 구조를 가진다.

```text
<module>/src/main/resources/application.yml
<module>/src/main/resources/application-<module>.yml
<module>/src/main/resources/application-<module>-local.yml
<module>/src/main/resources/application-<module>-dev.yml
<module>/src/main/resources/application-<module>-stg.yml
<module>/src/main/resources/application-<module>-prod.yml
```

각 업무 서비스의 `application.yml`은 `spring.config.import` 방식으로 PFW/CMN 및 자기 module 설정을 명시적으로 로드한다.

ACC 예시:

```yaml
spring:
  config:
    import:
      - optional:classpath:application-pfw.yml
      - optional:classpath:application-pfw-${spring.profiles.active:local}.yml
      - optional:classpath:application-cmn.yml
      - optional:classpath:application-cmn-${spring.profiles.active:local}.yml
      - optional:classpath:application-acc.yml
      - optional:classpath:application-acc-${spring.profiles.active:local}.yml
```

동일 구조를 MBR/EXS/ADM/BAT/BIZADM/EDU/XYZ에도 적용한다.

### 5.4 환경변수 prefix 규칙

환경변수 중복을 막기 위해 실행 주체 설정은 module prefix를 사용한다.

```text
ACC_*
MBR_*
EXS_*
ADM_*
BAT_*
BIZADM_*
EDU_*
XYZ_*
```

예시:

```text
ACC_SERVER_PORT
ACC_DATASOURCE_URL
ACC_DATASOURCE_USERNAME
ACC_DATASOURCE_PASSWORD
ACC_MODULE_ID
ACC_INSTANCE_ID
ACC_WAS_ID
ACC_LOG_BASE_PATH
ACC_SERVICE_REGISTRY_MODE
```

PFW/CMN 공통 정책은 아래 prefix를 사용한다.

```text
CPF_PFW_*
CPF_CMN_*
```

예시:

```text
CPF_PFW_HTTP_CONNECT_TIMEOUT_MS
CPF_PFW_HTTP_READ_TIMEOUT_MS
CPF_PFW_RETRY_MAX_ATTEMPTS
CPF_PFW_CIRCUIT_FAILURE_THRESHOLD
CPF_PFW_SERVICE_REGISTRY_MODE
CPF_PFW_FILETRANSFER_TIMEOUT_MS
CPF_PFW_BROKER_PUBLISH_TIMEOUT_MS

CPF_CMN_CACHE_TTL_SECONDS
CPF_CMN_MESSAGE_CACHE_TTL_SECONDS
CPF_CMN_FILE_NAMING_POLICY
CPF_CMN_VALIDATION_STRICT_MODE
```

공통 fallback은 허용하되, 최종 실행 설정은 module prefix가 우선해야 한다.

금지:

```text
- PFW/CMN yml이 spring.application.name을 강제
- PFW/CMN yml이 server.port를 강제
- PFW/CMN yml이 spring.profiles.active를 강제
- PFW/CMN yml이 업무 datasource를 덮어씀
- PFW/CMN yml이 module-id를 PFW/CMN으로 고정
- secret/token/password/private key/cert 원문을 yml에 기록
- 여러 모듈이 같은 SERVER_PORT, DATASOURCE_URL만 사용해서 충돌
```

### 5.5 profile loading smoke

각 모듈별로 local/dev/stg/prod config loading smoke를 만든다.

검증 항목:

```text
- application-pfw.yml import 여부
- application-pfw-<profile>.yml import 여부
- application-cmn.yml import 여부
- application-cmn-<profile>.yml import 여부
- application-<module>.yml import 여부
- application-<module>-<profile>.yml import 여부
- module-id가 PFW/CMN이 아니라 ACC/MBR/EXS/ADM/BAT 등 자기 값인지
- server.port가 module prefix 환경변수 기준으로 적용되는지
- datasource placeholder가 module prefix 기준인지
- log path가 module prefix 기준인지
- PFW/CMN 설정이 업무 서비스 실행 설정을 덮지 않는지
```

완료 기준:

* ACC/MBR/EXS/ADM/BAT/BIZADM/EDU/XYZ 환경별 yml 구조 반영
* PFW/CMN 환경별 yml 구조 반영
* 각 업무 서비스가 spring.config.import로 명시 로딩
* module prefix 환경변수 규칙 반영
* profile loading smoke evidence 생성
* secret 원문이 yml/evidence/log에 남지 않음

## 6. 주제영역별 packaging 및 원격 배포쉘 표준화

### 6.1 packaging 기준

PFW/CMN은 별도 WAS/서버로 배포하지 않는다. 각 업무 서비스 bootJar/distribution의 runtime dependency로 포함한다.

기본 표준:

```text
- self-contained Spring Boot executable jar 1개 배포
- thin jar + external lib 폴더 방식은 후순위 옵션
- 업무 서비스 bootJar 내부 BOOT-INF/lib에 PFW/CMN jar 포함 여부 검증
- PFW/CMN 소스를 업무 주제영역으로 복사하지 않음
- PFW/CMN jar를 주제영역별 lib 폴더에 수동 복사하는 구조를 표준으로 보지 않음
```

PFW/CMN이 바뀌면 영향을 받는 업무 서비스 bootJar를 다시 빌드하고 재배포하는 구조로 본다.

### 6.2 공통 원격 배포쉘

원격 배포쉘은 환경별로 완전히 별도 구현하지 않는다.

기준:

```text
공통 deploy-module.ps1 / deploy-module.sh를 배포 엔진으로 만들고,
Module과 Env 파라미터 및 deploy/env, deploy/inventory 설정으로 local/dev/stg/prod를 모두 대응한다.

모듈별 deploy-acc.ps1, deploy-mbr.ps1 등은 공통 엔진을 호출하는 얇은 wrapper로 둔다.
환경별 차이는 코드 분기가 아니라 env/inventory 설정으로 관리한다.

prod는 같은 엔진을 사용하되 RequireApproval, RunQualityGate, checksum, backup, rollback, health check를 강제한다.
```

필수 구현 후보:

```text
scripts/deploy/deploy-module.ps1
scripts/deploy/deploy-module.sh

scripts/deploy/deploy-acc.ps1
scripts/deploy/deploy-mbr.ps1
scripts/deploy/deploy-exs.ps1
scripts/deploy/deploy-adm.ps1
scripts/deploy/deploy-bat.ps1
scripts/deploy/deploy-bizadm.ps1
scripts/deploy/deploy-edu.ps1
scripts/deploy/deploy-xyz.ps1

scripts/deploy/remote-start-module.ps1
scripts/deploy/remote-stop-module.ps1
scripts/deploy/remote-restart-module.ps1
scripts/deploy/remote-health-check.ps1
scripts/deploy/rollback-module.ps1
scripts/deploy/check-packaged-dependencies.ps1
```

### 6.3 deploy env 파일

환경 파일:

```text
deploy/env/local-acc.env
deploy/env/dev-acc.env
deploy/env/stg-acc.env
deploy/env/prod-acc.env

deploy/env/local-mbr.env
deploy/env/dev-mbr.env
deploy/env/stg-mbr.env
deploy/env/prod-mbr.env
```

동일 기준으로 EXS/ADM/BAT/BIZADM/EDU/XYZ 생성한다.

env 파일의 환경변수는 module prefix 규칙을 따른다.

ACC 예시:

```text
SPRING_PROFILES_ACTIVE=dev
ACC_MODULE_ID=ACC
ACC_INSTANCE_ID=acc-dev-01
ACC_WAS_ID=accAP01
ACC_SERVER_PORT=8080
ACC_LOG_BASE_PATH=/logs/cpf/acc
ACC_DATASOURCE_URL=
ACC_DATASOURCE_USERNAME=
ACC_DATASOURCE_PASSWORD=
```

secret 원문을 실제 값으로 채우지 않는다. template 또는 placeholder만 둔다.

### 6.4 inventory

inventory:

```text
deploy/inventory/local-services.json
deploy/inventory/dev-services.json
deploy/inventory/stg-services.json
deploy/inventory/prod-services.template.json
```

inventory에는 실제 민감정보를 기록하지 않는다.

필수 후보 필드:

```text
env
module
hostAlias
sshHostEnvKey
sshUserEnvKey
deployBase
healthUrl
serviceName
portEnvKey
profile
approvalRequired
rollbackEnabled
```

### 6.5 배포쉘 필수 기능

`deploy-module.ps1` 필수 기능:

```text
1. module/env 입력
2. :<module>:bootJar 실행
3. qualityGate 선택 실행
4. bootJar 내부 PFW/CMN dependency 포함 여부 검사
5. artifact checksum 생성
6. 원격 서버 배포 디렉터리 확인/생성
7. 기존 jar 백업
8. 신규 jar 전송
9. 원격 stop
10. 원격 start
11. health check
12. service registry heartbeat 확인
13. 실패 시 rollback
14. deployment evidence JSON 생성
```

### 6.6 dry-run / real remote 구분

실제 서버 접속정보가 없으면 real remote deploy 완료로 기록하지 않는다.

완료/미검증 기준:

```text
- local dry-run 또는 packaging 검증까지만 완료/부분 구현으로 기록
- 실제 원격 전송/stop/start/health check 미수행 시 remote deploy는 미검증
- 배포 evidence에는 module, env, artifact, checksum, target host alias, deploy path, result, rollback 여부를 기록
- 비밀번호, private key, token, 접속정보 원문은 evidence에 남기지 않음
```

## 7. Service Call Engine runtime closure 준비 보강

이전 작업은 source/unit contract 중심이다. 이번 요청에서는 실제 runtime 검증으로 가기 위한 준비를 더 강화한다.

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

## 8. ADM Service Registry runtime/browser 준비 보강

필수 보강:

* `scripts/smoke-adm-service-registry-runtime.ps1 -RunRuntime` 실제 실행 조건 정리
* service/endpoint/instance/health/routing/circuit/call-history API 목록 정리
* browser click script 또는 Playwright 환경 조건 정리
* static UI smoke와 browser click smoke를 명확히 분리
* ADM 화면에서 service/endpoint/instance/health/circuit/call-history가 운영자가 보기 좋은 최소 구조인지 점검
* 권한/감사/마스킹/검색조건이 누락된 경우 gap으로 남김

실행하지 못하면 `미검증` 유지.

## 9. SQL/Flyway/all_install 재검증 준비

이번 작업에서 MariaDB full install을 직접 하지 못하면 완료로 올리지 않는다.

필수 보강:

* service registry 관련 split SQL/Flyway/all_install/99_smoke_check 정합성 점검
* broker/filetransfer/security skeleton이 DB 테이블을 만들지 않는다면 SQL 반영 불필요 사유 기록
* DB 테이블을 추가한다면 split SQL/Flyway/all_install/99_smoke_check 모두 반영
* 실제 MariaDB 신규 빈 DB 재설치가 불가능하면 `미검증` 유지
* `CPF_GAP_MATRIX.md`에 남길 것

## 10. qualityGate 보강

qualityGate에 아래 항목을 연결한다.

* ownership architecture scan
* Spring Event misuse scan
* service-call boundary scan
* profile loading smoke
* deploy dry-run/package check
* packaged PFW/CMN dependency check
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
* qualityGate가 실제로 새 scan/smoke를 호출하는지 evidence에 기록
* warning/NEEDS_REVIEW 항목은 완료로 숨기지 않고 report/gap에 남김

## 11. 필수 실행 명령

가능한 범위에서 아래를 실행하고 evidence를 남긴다.

```powershell
.\gradlew.bat :pfw:test --offline --no-daemon --console=plain
.\gradlew.bat :cmn:test --offline --no-daemon --console=plain
.\gradlew.bat :adm:test --offline --no-daemon --console=plain --tests cpf.adm.opr.controller.AdmServiceRegistryControllerTest
.\gradlew.bat :pfw:test --offline --no-daemon --console=plain --tests cpf.pfw.common.servicecall.*

powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-service-call-boundary.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-architecture-ownership.ps1 -ResultDir specs/evidence/20260708_02
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-spring-event-usage.ps1 -ResultDir specs/evidence/20260708_02
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-profile-loading.ps1 -ResultDir specs/evidence/20260708_02
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/deploy/check-packaged-dependencies.ps1 -Module ACC -ResultDir specs/evidence/20260708_02
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/deploy/deploy-module.ps1 -Module ACC -Env dev -DryRun -BuildBeforeDeploy -ResultDir specs/evidence/20260708_02

powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-report-matrix-evidence-consistency.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-feature-evidence.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-html-docs.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-utf8.ps1 -CheckMojibake

powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-service-call-engine-runtime.ps1 -ResultDir specs/evidence/20260708_02
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-service-call-engine-circuit-runtime.ps1 -ResultDir specs/evidence/20260708_02
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-service-call-engine-failover-runtime.ps1 -ResultDir specs/evidence/20260708_02
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-service-registry-health-runtime.ps1 -ResultDir specs/evidence/20260708_02
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-adm-service-registry-ui-static.ps1 -ResultDir specs/evidence/20260708_02

.\gradlew.bat qualityGate --offline --no-daemon --console=plain
```

실제 runtime 환경이 준비되어 있으면 아래도 실행한다.

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/runtime-start-services.ps1 -Modules ACC,MBR,EXS,ADM -BuildBeforeRun
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-service-call-engine-runtime.ps1 -RunRuntime -RequireRuntime -ResultDir specs/evidence/20260708_02
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-adm-service-registry-runtime.ps1 -RunRuntime -ResultDir specs/evidence/20260708_02
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-mariadb-full-install.ps1
```

실행하지 못한 명령은 완료로 기록하지 말고 미검증과 사유를 기록한다.

## 12. 산출물 기록 기준

문서 정본화 금지. 아래 최소 기록만 한다.

* `CPF_STABILIZATION_REPORT.md`: 상태값, 실행 명령, evidence 경로, 미검증 사유
* `CPF_GAP_MATRIX.md`: 남은 gap과 우선순위
* `CPF_EVIDENCE_INDEX.md`: 실제 존재하는 evidence 경로만 기록
* `specs/기능_구현_매트릭스.html`: check id/status/evidence 최소 정합성
* `README.md`: 깨진 smoke 명령이나 주요 진입점만 최소 수정
* `CPF_REVIEW_PROGRESS_COMPLETION_GUIDE.md`: 최신 공통 기준 반영 여부 확인

## 13. 완료 불인정 기준

아래 중 하나라도 해당하면 완료로 기록하지 않는다.

* PFW/CMN이 업무 주제영역에 의존
* 업무 주제영역이 EXS 내부 기술 클래스를 공통 기능처럼 사용
* Kafka/SFTP/FTP/OAuth/JWT/mTLS/retry/circuit/unknown result/reconciliation이 특정 업무 주제영역 전용으로 구현
* Spring Event만으로 핵심 거래/외부 송신/복구/대사 완료 주장
* PFW/CMN yml이 업무 서비스 실행 주체 설정을 덮어씀
* module prefix 없이 공통 환경변수만 사용해 모듈 간 충돌 가능
* source-only smoke를 real runtime으로 기록
* static UI smoke를 browser click 완료로 기록
* embedded/mock broker를 real broker 완료로 기록
* dry-run deploy를 real remote deploy 완료로 기록
* 기존 개발 DB 확인을 신규 MariaDB full install 완료로 기록
* 없는 evidence 파일을 완료 근거로 참조
* skeleton/interface만 만들고 runtime 완료로 기록
* `CPF_FINAL_TARGET_REQUIREMENTS.md`를 삭제/축약/덮어쓰기
* Git commit/push/branch 생성

## 14. Codex 완료 보고 필수 형식

작업 완료 보고에는 아래를 반드시 포함한다.

```text
1. 실제 수정한 핵심 파일
2. report/evidence/matrix/gap 정합성 복구 결과
3. CPF_REVIEW_PROGRESS_COMPLETION_GUIDE.md 최신 기준 반영 여부
4. architecture ownership warning 6건 처리 결과
5. 새로 추가/보강한 PFW capability package/interface 목록
6. CMN 책임 경계 보강 내용
7. ownership scan 결과
8. Spring Event scan 결과
9. local/dev/stg/prod yml/profile 반영 결과
10. module prefix 환경변수 반영 결과
11. PFW/CMN config import 검증 결과
12. bootJar 내부 PFW/CMN dependency 포함 검증 결과
13. 원격 배포쉘/dry-run 검증 결과
14. Service Call Engine runtime/source-only 구분
15. ADM Service Registry runtime/browser 검증 여부
16. SQL/Flyway/all_install 반영 여부
17. 실제 실행 명령
18. evidence 경로
19. 완료/부분 구현/미구현/미검증/실패/재확인 필요 분리
20. 후속 gap
21. Git commit/push/branch 미수행 여부
```

보고 시 금지:

```text
- 실행하지 않은 검증을 완료라고 쓰지 말 것
- dry-run을 real remote deploy라고 쓰지 말 것
- static UI smoke를 browser click이라고 쓰지 말 것
- source-only smoke를 runtime 완료라고 쓰지 말 것
- 없는 evidence 파일을 참조하지 말 것
```
