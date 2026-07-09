# Codex 요청서 — CPF 최종 가비지 정리, Evidence 정합성, Gradle 배포 표준, 전체 기능 샘플 Coverage 구축

## 0. 공통 작업 지침

이번 요청은 오늘 최종 정리 작업 후보로 본다.
단, 실제 외부 환경이 없는 항목을 완료로 포장하지 않는다.

### 수정 금지 파일

아래 파일은 수정하지 않는다.

```text id="forbidden-files"
CPF_FINAL_TARGET_REQUIREMENTS.md
CPF_REVIEW_PROGRESS_COMPLETION_GUIDE.md
CPF_NEW_REQUEST.md
```

`CPF_REVIEW_PROGRESS_COMPLETION_GUIDE.md`는 Codex 수정 대상이 아니라 ChatGPT 검수 기준 파일이다. 읽고 기준으로 삼을 수는 있으나 수정하지 않는다.

### Git 금지

```text id="git-forbidden"
Git commit 금지
Git push 금지
branch 생성 금지
```

### 상태값

상태값은 아래 6개만 사용한다.

```text id="status-values"
완료
부분 구현
미구현
미검증
실패
재확인 필요
```

### 완료로 기록하면 안 되는 항목

```text id="not-done-rules"
- 실제 서버 없는 remote deploy 완료
- dry-run을 real deploy로 기록
- 실제 WAS/JNDI runtime 완료
- 실제 Kafka/MQ/Redis runtime 완료
- 실제 SFTP/FTP/SSH runtime 완료
- 실제 Vault/KMS/HSM runtime 완료
- 실제 ADM browser click 완료
- 실제 MariaDB 신규 빈 DB full install 완료
- source-only smoke를 runtime 완료로 기록
- static UI smoke를 browser click 완료로 기록
- 없는 evidence 파일을 완료 근거로 기록
- stale evidence를 신규 완료 근거로 사용
- placeholder 샘플을 기능 샘플 완료로 기록
```

### 기본 실행 전제

CPF 기본 실행 전제는 아래로 둔다.

```text id="runtime-standard"
기본:
- Spring Boot executable bootJar
- embedded Tomcat
- java -jar 실행
- module별 server.port
- module별 datasource URL/env 기반 설정
- PFW/CMN은 업무 서비스 bootJar 내부 dependency

선택 호환:
- 외부 WAS
- JNDI datasource
- external WAS runtime
```

외부 WAS/JNDI는 설정 구조와 validation까지만 구현한다. 실제 WAS 설치, JNDI resource 등록, WAS runtime smoke는 Codex가 수행할 수 없으므로 미검증으로 기록한다.

## 1. 선행 검수 결과 반영

현재 검수 결과 기준으로 아래 문제를 먼저 해결한다.

```text id="review-findings"
1. report/evidence/matrix가 실제 없는 evidence 파일을 참조하는 후보가 있다.
2. scripts/deploy ps1/sh 삭제 기록과 GitHub tree 표시가 충돌하므로 로컬 git ls-files 기준 재확인이 필요하다.
3. empty directory scan evidence가 없다.
4. EDU는 별도 실행/배포 모듈이 아닌데 build.gradle에 EDU alias 후보가 남아 있다.
5. BAT edu 샘플이 1개 수준이다.
6. XYZ edu 샘플은 일부 있지만 CPF 전체 기능 coverage 체계가 없다.
7. sample coverage matrix가 없다.
8. 샘플별 source/test/evidence/check id 연결이 부족하다.
9. 실제 외부 인프라 없는 기능은 runtime 미검증으로 분리해야 한다.
10. 오늘 최종 작업 후보이므로 가비지, evidence, 샘플 coverage를 최대한 닫아야 한다.
```

## 2. Evidence 정합성 최종 복구

### 2.1 실제 파일 존재 검증

`CPF_STABILIZATION_REPORT.md`, `CPF_EVIDENCE_INDEX.md`, `specs/기능_구현_매트릭스.html`, `specs/sample-coverage-matrix.md`가 참조하는 모든 evidence path를 실제 파일 존재 기준으로 검증한다.

필수 작업:

```text id="evidence-tasks"
1. 모든 evidence path를 로컬 파일 존재 기준으로 확인
2. 없는 evidence path 제거 또는 실제 파일 생성
3. stale evidence를 신규 완료 근거로 쓰는 항목 제거
4. SKIPPED/미실행 evidence가 완료로 집계되지 않도록 보정
5. check-report-matrix-evidence-consistency.ps1가 실제 파일 존재 여부를 강제 검증하도록 보강
6. evidence 누락 시 qualityGate 실패 처리
```

필수 evidence:

```text id="evidence-files"
specs/evidence/20260708_04/evidence-path-existence-check.sanitized.json
specs/evidence/20260708_04/report-matrix-evidence-consistency.sanitized.json
```

완료 기준:

```text id="evidence-done"
- report/index/matrix/sample coverage가 참조하는 evidence 파일 실제 존재
- 없는 evidence 참조 0건
- stale evidence 신규 완료 근거 0건
- qualityGate가 evidence 누락을 잡음
```

## 3. 가비지 파일, ps1/sh, 빈 폴더 최종 정리

### 3.1 가비지 파일 전수 스캔

필수 산출:

```powershell id="garbage-scan-command"
git ls-files > specs/evidence/20260708_04/git-files.txt
git ls-files "*application*.yml" "*application*.yaml" "*application*.properties" > specs/evidence/20260708_04/config-files.txt
git ls-files "deploy/env/*" "deploy/inventory/*" > specs/evidence/20260708_04/deploy-config-files.txt
git ls-files "scripts/deploy/*" > specs/evidence/20260708_04/deploy-script-files-after-cleanup.txt
git ls-files "specs/evidence/**" > specs/evidence/20260708_04/evidence-files.txt
```

정리 대상:

```text id="garbage-targets"
- 구버전 application-local.yml/application-dev.yml/application-prod.yml/application-test.yml
- 신규 표준과 중복되는 yml
- 더 이상 import되지 않는 yml/properties
- 빈 env 값이 남은 deploy/env
- EDU 별도 env/deploy/inventory
- scripts/deploy/*.ps1
- scripts/deploy/*.sh
- placeholder remote-start/stop/restart/rollback
- stale evidence
- dead source/helper/DTO/template
- 자동생성 구버전 template
```

삭제 기준:

```text id="delete-rules"
- git grep 참조 없음
- Gradle build/test 참조 없음
- README/report/matrix/evidence 참조 없음
- qualityGate/check script 참조 없음
- 자동생성 템플릿 참조 없음
- 같은 목적의 신규 표준 파일 존재
```

애매하면 삭제하지 말고 `재확인 필요`로 남긴다.

필수 evidence:

```text id="garbage-evidence"
specs/evidence/20260708_04/garbage-file-scan.sanitized.json
specs/evidence/20260708_04/deleted-files.sanitized.json
specs/evidence/20260708_04/retained-review-required-files.sanitized.json
```

### 3.2 ps1/sh 정리

Gradle 중심 배포로 전환했으므로 `scripts/deploy/*.ps1`, `scripts/deploy/*.sh`는 원칙적으로 삭제한다.

남길 수 있는 경우:

```text id="script-keep-rules"
- Gradle task가 반드시 호출해야 하는 보조 구현체
- Linux/Windows agent별 실제 구현체로 명확히 필요한 경우
- README/report/evidence에 남기는 사유가 있는 경우
```

완료 기준:

```text id="script-done"
- scripts/deploy/*.ps1 잔존 0건 또는 사유 명확
- scripts/deploy/*.sh 잔존 0건 또는 사유 명확
- Jenkins 표준 호출은 gradlew만 사용
- Gradle task가 ps1/sh wrapper 수준이면 완료 불가
```

### 3.3 빈 폴더 스캔

Git은 빈 폴더를 추적하지 않으므로 로컬 checkout 기준으로 검사한다.

필수 evidence:

```text id="empty-dir-evidence"
specs/evidence/20260708_04/empty-directory-scan.sanitized.json
specs/evidence/20260708_04/deleted-empty-directories.sanitized.json
specs/evidence/20260708_04/retained-empty-directories.sanitized.json
```

검사 대상:

```text id="empty-dir-targets"
scripts/deploy
deploy/env
deploy/inventory
bat/src/main/java/cpf/bat/edu
bat/src/test/java/cpf/bat/edu
xyz/src/main/java/cpf/xyz/edu
xyz/src/test/java/cpf/xyz/edu
exs/src/main/java/cpf/exs
cmn/src/main/java/cpf/cmn
pfw/src/main/java/cpf/pfw
specs/evidence
각 모듈 src/main/resources
각 모듈 src/test/resources
```

완료 기준:

```text id="empty-dir-done"
- 빈 작업 폴더 제거
- 빈 package 폴더 제거
- 삭제 보류 빈 폴더는 사유 기록
- 삭제 후 build/test/qualityGate 통과
```

## 4. EDU 기준 정리

EDU는 별도 주제영역, 별도 서비스, 별도 bootJar, 별도 deploy 대상이 아니다.

기준:

```text id="edu-standard"
XYZ 내부 교육 샘플 패키지:
xyz/src/main/java/cpf/xyz/edu
xyz/src/test/java/cpf/xyz/edu

BAT 내부 배치 교육 샘플 패키지:
bat/src/main/java/cpf/bat/edu
bat/src/test/java/cpf/bat/edu
```

삭제/금지 대상:

```text id="edu-forbidden"
- settings.gradle 별도 edu module
- build.gradle cpfServiceModules EDU alias
- deploy/env/*-edu.env
- deploy/inventory EDU service entry
- deploy-edu.ps1
- deploy-edu.sh
- EDU_SERVER_PORT
- EDU_MODULE_ID
- EDU_INSTANCE_ID
- -PcpfModule=EDU 성공 처리
```

필수 작업:

```text id="edu-tasks"
1. -PcpfModule=EDU 입력 시 실패 처리
2. remoteDeploy 대상은 ACC/MBR/EXS/ADM/BAT/BIZADM/XYZ만 허용
3. EDU 관련 deploy/env/inventory/script 제거
4. README/report에서 EDU를 별도 서비스처럼 설명한 문구 제거
5. sample coverage에서는 XYZ-EDU, BAT-EDU sampleId로만 관리
```

완료 기준:

```text id="edu-done"
- 별도 edu module 없음
- 별도 edu deploy 대상 없음
- 별도 edu port 없음
- build.gradle EDU alias 없음
- XYZ/BAT 내부 edu 패키지만 존재
```

## 5. local/dev/stg/prod 환경파일과 포트 정합성

### 5.1 profile 표준

```text id="profiles"
local
dev
stg
prod
```

`prd`는 표준으로 쓰지 않는다.

### 5.2 실행 주체

```text id="runtime-modules"
ACC
MBR
EXS
ADM
BAT
BIZADM
XYZ
```

PFW/CMN/EDU는 실행 주체가 아니다.

### 5.3 local 포트 중복 금지

모든 실행 주체는 local에서 동시에 기동 가능해야 하므로 local server port가 중복되면 안 된다.

권장 local port 기준:

```text id="local-ports"
ACC     : 8080
MBR     : 8081
ADM     : 8090
BIZADM  : 8091
EXS     : 8092
BAT     : 8093
XYZ     : 8099
```

필수 작업:

```text id="port-tasks"
1. deploy/env/local-*.env 포트 중복 검사
2. application-<module>-local.yml 포트 중복 검사
3. local-services.json 포트 정합성 검사
4. runLocalServices 대상/포트 정합성 검사
5. EDU_SERVER_PORT 제거
6. PFW/CMN server.port 제거
```

필수 evidence:

```text id="port-evidence"
specs/evidence/20260708_04/local-port-duplicate-scan.sanitized.json
```

## 6. datasource URL/JNDI mode와 embedded bootJar 기준

기본 datasource는 URL/env 기반이다. JNDI는 외부 WAS 호환 옵션이다.

표준 env:

```text id="datasource-env"
<MODULE>_DATASOURCE_MODE=url|jndi

URL mode:
<MODULE>_DATASOURCE_URL
<MODULE>_DATASOURCE_USERNAME
<MODULE>_DATASOURCE_PASSWORD

JNDI mode:
<MODULE>_DATASOURCE_JNDI_NAME
```

기준:

```text id="datasource-rules"
- local/dev 기본은 url mode
- stg/prod도 실제 환경 없으면 safe default 또는 placeholder
- JNDI는 선택 호환
- 실제 WAS/JNDI runtime은 미검증 유지
- PFW/CMN은 datasource를 강제하지 않음
- env 값은 비워두지 않음
- secret 원문은 기록하지 않음
```

필수 evidence:

```text id="datasource-evidence"
specs/evidence/20260708_04/datasource-mode-scan.sanitized.json
```

## 7. Gradle 중심 remote deploy 최종 정리

Jenkins/운영/개발 표준 진입점은 Gradle이다.
`apply from`, 별도 include, ps1/sh 직접 호출 중심 구조는 지양한다.

### 7.1 필수 Gradle task

root `build.gradle` 내부에 명확한 `CPF Remote Deploy Tasks` section을 둔다.

필수 task:

```text id="gradle-tasks"
checkDeployEnv
checkDeployInventory
checkPackagedDependencies
remoteDeployDryRun
remoteDeploy
rollbackDeploy
```

필수 parameter:

```text id="gradle-params"
-PcpfModule=ACC|MBR|EXS|ADM|BAT|BIZADM|XYZ
-PcpfEnv=local|dev|stg|prod
-PcpfDeployMode=dryRun|remote
-PcpfRequireApproval=true|false
-PcpfResultDir=specs/evidence/20260708_04
```

`EDU`는 허용하지 않는다.

### 7.2 Gradle task 책임

```text id="gradle-responsibilities"
- module/env/mode parameter 검증
- env 파일 로딩/검증
- inventory 파일 로딩/검증
- :<module>:bootJar 실행
- bootJar 존재 확인
- bootJar 내부 PFW/CMN dependency 포함 검증
- artifact checksum 생성
- embedded Tomcat java -jar command plan 생성
- healthUrl template 검증
- optional WAS/JNDI config plan 생성
- dry-run evidence JSON 생성
- prod approval flag 검증
- remote mode 환경 부재 시 fail-fast
```

### 7.3 Jenkins 예시

```bash id="jenkins-example"
./gradlew remoteDeploy \
  -PcpfModule=ACC \
  -PcpfEnv=dev \
  -PcpfDeployMode=dryRun
```

운영 후보:

```bash id="jenkins-prod-example"
./gradlew remoteDeploy \
  -PcpfModule=ACC \
  -PcpfEnv=prod \
  -PcpfDeployMode=remote \
  -PcpfRequireApproval=true
```

실제 remote mode는 서버/권한/승인 정보 없으면 실행하지 않는다.

필수 evidence:

```text id="deploy-evidence"
specs/evidence/20260708_04/gradle-remote-deploy-task-scan.sanitized.json
specs/evidence/20260708_04/remote-deploy-dry-run.sanitized.json
```

## 8. CPF 전체 기능 Sample Coverage Matrix 생성

샘플은 단순 교육용 placeholder가 아니라 **기능 검증 자산**이다.
CPF가 지원하는 모든 기능은 개발자가 참고할 수 있는 샘플과 검증 수준을 가져야 한다.

### 8.1 필수 파일

```text id="sample-files"
specs/sample-coverage-matrix.md
specs/evidence/20260708_04/sample-coverage-result.sanitized.json
```

### 8.2 필수 컬럼

```text id="sample-cols"
sampleId
module
package
featureArea
sampleName
sourcePath
testPath
evidencePath
validationLevel
runtimeRequired
runtimeExecuted
status
notes
```

### 8.3 validationLevel

```text id="validation-levels"
source-compile
unit-test
contract-test
fixture-test
source-smoke
profile-smoke
runtime-smoke
external-runtime-required
```

### 8.4 Coverage 원칙

```text id="sample-rules"
- 모든 샘플은 sampleId를 가진다.
- 모든 샘플은 sourcePath를 가진다.
- 가능한 모든 샘플은 testPath를 가진다.
- 외부 runtime이 필요한 샘플은 runtimeRequired=true로 기록한다.
- 외부 runtime이 없으면 runtimeExecuted=false, status=미검증 또는 부분 구현으로 기록한다.
- placeholder-only sample은 완료 불가.
- 샘플 누락 항목은 미구현으로 matrix에 남긴다.
- sample coverage는 기능 매트릭스/evidence index와 연결한다.
```

## 9. XYZ EDU — 온라인 개발자 참조 샘플 전체 목록

XYZ edu는 온라인 업무 개발자가 신규 업무 개발 시 참고하는 샘플 모음이다.

패키지 후보:

```text id="xyz-packages"
xyz/src/main/java/cpf/xyz/edu/crud
xyz/src/main/java/cpf/xyz/edu/list
xyz/src/main/java/cpf/xyz/edu/pagination
xyz/src/main/java/cpf/xyz/edu/detail
xyz/src/main/java/cpf/xyz/edu/transaction
xyz/src/main/java/cpf/xyz/edu/servicecall
xyz/src/main/java/cpf/xyz/edu/facade
xyz/src/main/java/cpf/xyz/edu/header
xyz/src/main/java/cpf/xyz/edu/failure
xyz/src/main/java/cpf/xyz/edu/idempotency
xyz/src/main/java/cpf/xyz/edu/security
xyz/src/main/java/cpf/xyz/edu/audit
xyz/src/main/java/cpf/xyz/edu/validation
xyz/src/main/java/cpf/xyz/edu/telegram
xyz/src/main/java/cpf/xyz/edu/messaging
xyz/src/main/java/cpf/xyz/edu/filetransfer
xyz/src/main/java/cpf/xyz/edu/operation
```

필수 sampleId:

```text id="xyz-samples"
XYZ-EDU-CRUD-001 등록
XYZ-EDU-CRUD-002 수정
XYZ-EDU-CRUD-003 삭제
XYZ-EDU-CRUD-004 단건 조회
XYZ-EDU-CRUD-005 등록 validation 실패
XYZ-EDU-CRUD-006 수정 optimistic lock 후보
XYZ-EDU-LIST-001 단순 목록
XYZ-EDU-LIST-002 검색조건 목록
XYZ-EDU-LIST-003 상태값 필터
XYZ-EDU-LIST-004 기간 조건
XYZ-EDU-LIST-005 정렬 조건
XYZ-EDU-LIST-006 복합 검색조건
XYZ-EDU-LIST-007 목록 마스킹
XYZ-EDU-PAGE-001 offset pagination
XYZ-EDU-PAGE-002 keyset pagination
XYZ-EDU-PAGE-003 cursor 기반 조회 후보
XYZ-EDU-PAGE-004 대량 목록 조회 주의 샘플
XYZ-EDU-DETAIL-001 목록→상세
XYZ-EDU-DETAIL-002 상세 권한 체크
XYZ-EDU-DETAIL-003 상세 마스킹
XYZ-EDU-DETAIL-004 상세 감사 로그
XYZ-EDU-TRX-001 단일 테이블 트랜잭션
XYZ-EDU-TRX-002 복수 테이블 트랜잭션
XYZ-EDU-TRX-003 실패 rollback
XYZ-EDU-TRX-004 상태 전이
XYZ-EDU-TRX-005 transactionGlobalId 연결
XYZ-EDU-TRX-006 REQUIRES_NEW 감사 후보
XYZ-EDU-TRX-007 readOnly transaction
XYZ-EDU-CALL-001 Local Facade 호출
XYZ-EDU-CALL-002 Remote Facade Proxy 후보
XYZ-EDU-CALL-003 PFW Service Call Engine 호출
XYZ-EDU-CALL-004 timeout 처리
XYZ-EDU-CALL-005 retry 처리
XYZ-EDU-CALL-006 failover 처리
XYZ-EDU-CALL-007 circuit breaker 처리
XYZ-EDU-CALL-008 selectedInstanceId logging
XYZ-EDU-CALL-009 call history 확인 후보
XYZ-EDU-CALL-010 타 주제영역 호출 금지 패턴 설명
XYZ-EDU-HEADER-001 표준 헤더 전달
XYZ-EDU-HEADER-002 확장 헤더 전달
XYZ-EDU-HEADER-003 transactionGlobalId propagation
XYZ-EDU-HEADER-004 moduleId/instanceId propagation
XYZ-EDU-IDEMP-001 idempotency key 생성
XYZ-EDU-IDEMP-002 중복 요청 응답
XYZ-EDU-IDEMP-003 재처리 안전성
XYZ-EDU-FAIL-001 업무 오류 응답
XYZ-EDU-FAIL-002 시스템 오류 응답
XYZ-EDU-FAIL-003 unknown result 후보
XYZ-EDU-FAIL-004 사용자 메시지/내부 오류 분리
XYZ-EDU-SEC-001 API 권한 체크
XYZ-EDU-SEC-002 버튼/기능 권한 후보
XYZ-EDU-SEC-003 목록 마스킹
XYZ-EDU-SEC-004 상세 마스킹
XYZ-EDU-SEC-005 다운로드 권한 후보
XYZ-EDU-AUDIT-001 조회 감사
XYZ-EDU-AUDIT-002 변경 감사
XYZ-EDU-AUDIT-003 다운로드 감사 후보
XYZ-EDU-VALID-001 request validation
XYZ-EDU-VALID-002 enum/status validation
XYZ-EDU-VALID-003 converter/helper
XYZ-EDU-VALID-004 공통 메시지/오류 코드
XYZ-EDU-OPER-001 ADM 거래 로그 연계 후보
XYZ-EDU-OPER-002 실패 구간 추적 후보
```

금지:

```text id="xyz-forbidden"
- URL 직접 조합 운영 샘플
- Controller 직접 호출
- 타 주제영역 Repository/Mapper 직접 참조
- EXS 내부 기술 클래스를 공통 기능처럼 사용
- Spring Event 핵심 거래 흐름 샘플
- placeholder-only sample
```

## 10. BAT EDU — 배치 개발자 참조 샘플 전체 목록

BAT edu는 배치 개발자가 참고하는 샘플 모음이다.

패키지 후보:

```text id="bat-packages"
bat/src/main/java/cpf/bat/edu/job
bat/src/main/java/cpf/bat/edu/transaction
bat/src/main/java/cpf/bat/edu/servicecall
bat/src/main/java/cpf/bat/edu/centercut
bat/src/main/java/cpf/bat/edu/logging
bat/src/main/java/cpf/bat/edu/idempotency
bat/src/main/java/cpf/bat/edu/retry
bat/src/main/java/cpf/bat/edu/reconciliation
bat/src/test/java/cpf/bat/edu/...
```

필수 sampleId:

```text id="bat-samples"
BAT-EDU-JOB-001 일반 Tasklet Job
BAT-EDU-JOB-002 Chunk Job 후보
BAT-EDU-JOB-003 job parameter validation
BAT-EDU-JOB-004 job status transition
BAT-EDU-JOB-005 실패 처리
BAT-EDU-JOB-006 retry 정책
BAT-EDU-JOB-007 restart 정책
BAT-EDU-JOB-008 중복 실행 방지
BAT-EDU-JOB-009 scheduler run-once 후보
BAT-EDU-TRX-001 배치 단위 트랜잭션
BAT-EDU-TRX-002 item 단위 트랜잭션
BAT-EDU-TRX-003 부분 실패 rollback
BAT-EDU-TRX-004 성공/실패 카운트 반영
BAT-EDU-CALL-001 배치에서 온라인 주제영역 호출
BAT-EDU-CALL-002 Service Call Engine 기반 호출
BAT-EDU-CALL-003 Facade 기반 호출
BAT-EDU-CALL-004 timeout 처리
BAT-EDU-CALL-005 retry/failover 처리
BAT-EDU-CALL-006 batch transactionGlobalId 전달
BAT-EDU-CENTER-001 center-cut 기본 실행
BAT-EDU-CENTER-002 center-cut 대상 조회
BAT-EDU-CENTER-003 center-cut item 처리
BAT-EDU-CENTER-004 실패 item 재처리
BAT-EDU-CENTER-005 centerCutExecutionId 추적
BAT-EDU-CENTER-006 parent/child transactionGlobalId
BAT-EDU-CENTER-007 item result summary
BAT-EDU-LOG-001 jobExecutionId별 로그 경로 계산
BAT-EDU-LOG-002 jobInstanceId별 디렉터리
BAT-EDU-LOG-003 retry/restart 로그 덮어쓰기 방지
BAT-EDU-LOG-004 transactionGlobalId 로그 기록
BAT-EDU-LOG-005 centerCutExecutionId 로그 경로
BAT-EDU-IDEMP-001 배치 idempotency key
BAT-EDU-IDEMP-002 중복 실행 방지
BAT-EDU-UNKNOWN-001 unknown result 후보
BAT-EDU-RECON-001 배치 대사 후보
BAT-EDU-RECON-002 실패 건 재처리 후보
```

완료 기준:

```text id="bat-done"
- BAT edu 1개 샘플로 완료 처리 금지
- 일반 배치, center-cut, transaction, online call, job log 샘플 모두 필요
- 각 샘플 test/evidence 연결
- 실제 batch runtime 없으면 runtime은 미검증
```

## 11. CMN EDU/Test-Support — 공통 기능 샘플 전체 목록

CMN은 프로젝트 공통 helper/rule/fixture를 제공한다. 기술 engine 소유자가 아니다.

필수 sampleId:

```text id="cmn-samples"
CMN-EDU-FIXED-001 고정길이 layout 정의
CMN-EDU-FIXED-002 parser
CMN-EDU-FIXED-003 formatter
CMN-EDU-FIXED-004 parser/formatter round-trip
CMN-EDU-FIXED-005 field validation
CMN-EDU-FIXED-006 오류 전문 fixture
CMN-EDU-FIXED-007 padding/trim
CMN-EDU-FIXED-008 한글/byte length 검증
CMN-EDU-FIXED-009 nullable/default field
CMN-EDU-FIXED-010 반복부/목록부 후보
CMN-EDU-FIXED-011 전문 version/layout 선택
CMN-EDU-MSG-001 공통 메시지 조회
CMN-EDU-CODE-001 공통 코드 조회
CMN-EDU-VALID-001 validation helper
CMN-EDU-CONV-001 converter/helper
CMN-EDU-FILE-001 파일명 규칙 helper
CMN-EDU-FILE-002 디렉터리 규칙 helper
CMN-EDU-FIXTURE-001 테스트 fixture helper
CMN-EDU-MASK-001 공통 masking helper 후보
```

주의:

```text id="cmn-rules"
- Kafka/MQ/SFTP/FTP 기술 engine을 CMN 소유로 만들지 않는다.
- 고정길이는 CMN parser/formatter + EXS 업무 adapter 구조로 보여준다.
```

## 12. EXS EDU/업무 샘플 전체 목록

EXS는 외부연계 기술 소유자가 아니라 대외업무 adapter/구현체다.

필수 sampleId:

```text id="exs-samples"
EXS-EDU-FIXED-001 고정길이 전문 송신 업무 adapter
EXS-EDU-FIXED-002 고정길이 전문 수신 업무 adapter
EXS-EDU-FIXED-003 오류 전문 처리
EXS-EDU-FIXED-004 송수신 전문 round-trip
EXS-EDU-ENDPOINT-001 기관별 endpoint 설정
EXS-EDU-ENDPOINT-002 기관별 timeout/retry 설정 사용
EXS-EDU-AUTH-001 OAuth credential reference 후보
EXS-EDU-AUTH-002 JWT 후보
EXS-EDU-AUTH-003 mTLS 후보
EXS-EDU-CALL-001 PFW Service Call Engine 기반 대외 호출
EXS-EDU-RETRY-001 timeout 처리
EXS-EDU-RETRY-002 retry 처리
EXS-EDU-RETRY-003 circuit 처리
EXS-EDU-RETRY-004 failover 처리
EXS-EDU-UNKNOWN-001 unknown result 처리
EXS-EDU-RECON-001 reconciliation 처리
EXS-EDU-LEDGER-001 송수신 원장 기록
EXS-EDU-IDEMP-001 대외 요청 idempotency
EXS-EDU-FILE-001 대외 파일 송신 업무 adapter 후보
EXS-EDU-FILE-002 대외 파일 수신 업무 adapter 후보
```

실제 외부기관 runtime, 실제 OAuth/JWT/mTLS, 실제 SFTP는 미검증 유지.

## 13. PFW EDU/Test-Support — 프레임워크 capability 샘플 전체 목록

PFW는 기술 capability 소유자다.

필수 sampleId:

```text id="pfw-samples"
PFW-EDU-CALL-001 Service Call Engine 기본 호출
PFW-EDU-CALL-002 timeout
PFW-EDU-CALL-003 retry
PFW-EDU-CALL-004 failover
PFW-EDU-CALL-005 circuit breaker
PFW-EDU-CALL-006 selectedInstanceId logging
PFW-EDU-CALL-007 call history
PFW-EDU-CALL-008 service/endpoint/instance registry
PFW-EDU-CALL-009 direct instance mode
PFW-EDU-CALL-010 LB endpoint mode
PFW-EDU-BROKER-001 broker publish port
PFW-EDU-BROKER-002 broker consume port
PFW-EDU-BROKER-003 message envelope/header
PFW-EDU-BROKER-004 outbox 후보
PFW-EDU-BROKER-005 inbox 후보
PFW-EDU-BROKER-006 DLQ 후보
PFW-EDU-BROKER-007 replay 후보
PFW-EDU-BROKER-008 broker idempotency
PFW-EDU-FILE-001 file transfer request/result
PFW-EDU-FILE-002 SFTP/FTP/SCP/SSH protocol 후보
PFW-EDU-FILE-003 checksum policy
PFW-EDU-FILE-004 temp/rename/archive policy
PFW-EDU-FILE-005 duplicate prevention
PFW-EDU-FILE-006 transfer history
PFW-EDU-SEC-001 secret provider port
PFW-EDU-SEC-002 credential reference
PFW-EDU-SEC-003 token provider port
PFW-EDU-SEC-004 key/cert provider port
PFW-EDU-SEC-005 signature/encryption port
PFW-EDU-RUNTIME-001 heartbeat
PFW-EDU-RUNTIME-002 distributed lock
PFW-EDU-RUNTIME-003 health check
PFW-EDU-RUNTIME-004 ghost detection 후보
PFW-EDU-RUNTIME-005 scheduler control
PFW-EDU-RUNTIME-006 worker control
PFW-EDU-ADMIN-001 ADM status DTO 후보
PFW-EDU-AUDIT-001 audit context 후보
PFW-EDU-MASK-001 masking policy 후보
```

실제 Kafka/MQ/Redis, SFTP/SSH, Vault/KMS/HSM runtime은 미검증 유지.

## 14. ADM/BIZADM/운영 샘플 Coverage

운영/관리 기능도 개발자와 운영자가 참고할 샘플이 필요하다.

필수 sampleId:

```text id="adm-bizadm-samples"
ADM-EDU-OPR-001 transactionGlobalId 조회 후보
ADM-EDU-OPR-002 service call history 조회 후보
ADM-EDU-OPR-003 service registry 조회 후보
ADM-EDU-OPR-004 endpoint/instance health 조회 후보
ADM-EDU-OPR-005 circuit status 조회 후보
ADM-EDU-OPR-006 batch execution 조회 후보
ADM-EDU-OPR-007 file transfer history 조회 후보
ADM-EDU-OPR-008 broker status 후보
ADM-EDU-OPR-009 runtime health 후보
ADM-EDU-OPR-010 scheduler/worker 상태 후보

BIZADM-EDU-AUTH-001 메뉴 권한 샘플
BIZADM-EDU-AUTH-002 API 권한 샘플
BIZADM-EDU-AUTH-003 버튼 권한 샘플
BIZADM-EDU-AUDIT-001 변경 감사 샘플
BIZADM-EDU-DOWNLOAD-001 다운로드 권한/감사 샘플
BIZADM-EDU-MASK-001 마스킹 정책 샘플
```

browser click은 실제 환경 없으면 미검증 유지. source/API/static smoke는 가능 범위에서 검증한다.

## 15. 샘플 기반 기능 검증

각 샘플은 가능한 범위에서 test/smoke/evidence와 연결한다.

필수 연결:

```text id="sample-validation-map"
온라인 CRUD:
controller/service/repository test
validation test
audit/masking 호출 test

목록/pagination:
offset query test
keyset query test
search/sort condition test

타 주제영역 호출:
Service Call Engine contract test
Facade proxy test
URL 직접 호출 금지 scan

트랜잭션:
rollback test
상태 전이 test
transactionGlobalId propagation test

고정길이 전문:
layout parser test
formatter round-trip test
invalid field validation test
fixture 기반 전문 test

배치:
job parameter validation test
jobExecution log path test
retry/restart policy test
center-cut handler contract test

파일전송:
request/result DTO test
checksum/path policy test
adapter contract test

broker:
envelope/header/idempotency test
outbox/inbox contract test

보안/감사:
masking test
audit context test
download audit 후보 test

운영/관제:
status DTO test
query condition test
evidence/static smoke
```

## 16. 샘플 누락 방지 qualityGate

qualityGate에 아래를 추가한다.

```text id="sample-quality-gate"
- sample coverage matrix 존재 검사
- 필수 sampleId 존재 검사
- sample sourcePath 실제 존재 검사
- sample testPath 실제 존재 또는 사유 존재 검사
- sample evidencePath 실제 존재 또는 사유 존재 검사
- placeholder-only sample 검사
- runtimeRequired/runtimeExecuted 정합성 검사
- XYZ/BAT/EXS/CMN/PFW/ADM/BIZADM sample coverage 상태 검사
- EDU 별도 module/deploy/env 금지 검사
- build.gradle EDU alias 금지 검사
- local port 중복 검사
- scripts/deploy 잔존 검사
- empty directory scan 결과 검사
- evidence path 실제 존재 검사
```

완료 불인정:

```text id="sample-not-done"
- sample coverage matrix 없음
- BAT edu 샘플 1개로 완료 주장
- XYZ edu controller만 있고 test/evidence 미연결
- Kafka/MQ/SFTP/SSH 샘플이 plan/source 수준인데 runtime 완료 주장
- PFW/CMN/EXS ownership 위반 샘플
- 고정길이 전문이 EXS 전용 기술처럼 구현
- sourcePath 없는 sampleId
- evidencePath 없는 완료 sample
```

## 17. 기존 작업 영향도 점검

아래 영역이 이번 샘플/가비지/Gradle 변경으로 깨지지 않는지 확인한다.

```text id="impact-check"
- Service Call Engine
- PFW capability skeleton
- CMN fle/mqe warning
- architecture ownership scan
- Spring Event scan
- profile loading smoke
- deploy dry-run
- runLocalServices
- BAT center-cut
- XYZ edu existing samples
- EXS fixed-length samples
- evidence/report/matrix consistency
```

## 18. 필수 실행 명령

가능한 범위에서 실행하고 evidence를 남긴다.

```powershell id="commands"
.\gradlew.bat :xyz:test --offline --no-daemon --console=plain
.\gradlew.bat :bat:test --offline --no-daemon --console=plain
.\gradlew.bat :pfw:test --offline --no-daemon --console=plain
.\gradlew.bat :cmn:test --offline --no-daemon --console=plain
.\gradlew.bat :exs:test --offline --no-daemon --console=plain
.\gradlew.bat :adm:test --offline --no-daemon --console=plain
.\gradlew.bat :bizadm:test --offline --no-daemon --console=plain

.\gradlew.bat checkDeployEnv -PcpfModule=ACC -PcpfEnv=dev -PcpfResultDir=specs/evidence/20260708_04 --offline --no-daemon --console=plain
.\gradlew.bat checkDeployInventory -PcpfModule=ACC -PcpfEnv=dev -PcpfResultDir=specs/evidence/20260708_04 --offline --no-daemon --console=plain
.\gradlew.bat checkPackagedDependencies -PcpfModule=ACC -PcpfResultDir=specs/evidence/20260708_04 --offline --no-daemon --console=plain
.\gradlew.bat remoteDeployDryRun -PcpfModule=ACC -PcpfEnv=dev -PcpfDeployMode=dryRun -PcpfResultDir=specs/evidence/20260708_04 --offline --no-daemon --console=plain

.\gradlew.bat qualityGate --offline --no-daemon --console=plain
```

추가 scan은 Gradle task 또는 기존 script로 실행하되 evidence를 남긴다.

```text id="extra-scan"
garbage file scan
empty directory scan
sample coverage scan
evidence path existence scan
local port duplicate scan
EDU deploy/module alias scan
placeholder sample scan
```

실행하지 말 것:

```text id="do-not-run"
실제 remoteDeploy remote mode
실제 external WAS/JNDI runtime
실제 Kafka/MQ/Redis runtime
실제 SFTP/FTP/SSH runtime
실제 Vault/KMS/HSM runtime
실제 browser click
실제 MariaDB 신규 빈 DB full install
```

## 19. 산출물 기록

수정 가능:

```text id="docs-allowed"
CPF_STABILIZATION_REPORT.md
CPF_GAP_MATRIX.md
CPF_EVIDENCE_INDEX.md
specs/기능_구현_매트릭스.html
README.md
specs/sample-coverage-matrix.md
specs/evidence/20260708_04/*
```

수정 금지:

```text id="docs-forbidden"
CPF_FINAL_TARGET_REQUIREMENTS.md
CPF_REVIEW_PROGRESS_COMPLETION_GUIDE.md
CPF_NEW_REQUEST.md
```

기록 기준:

```text id="docs-rules"
- 문서 정본화 금지
- 샘플 위치/목적/검증 수준만 최소 기록
- report/index/matrix/sample coverage/evidence 상태 일치
- 실제 없는 evidence 참조 금지
- runtime 미실행 항목은 미검증 유지
```

## 20. Codex 완료 보고 필수 형식

완료 보고에는 아래를 반드시 포함한다.

```text id="report-format"
1. 실제 수정 파일 목록
2. 삭제 파일 목록
3. 삭제한 빈 폴더 목록
4. 삭제 보류 파일/폴더와 사유
5. scripts/deploy 잔존 여부
6. evidence path 정합성 복구 결과
7. EDU 별도 module/deploy/env/alias 제거 결과
8. local port 중복 검사 결과
9. Gradle remoteDeploy task 정합성 결과
10. sample coverage matrix 생성 결과
11. XYZ edu sample coverage 결과
12. BAT edu sample coverage 결과
13. CMN sample coverage 결과
14. EXS sample coverage 결과
15. PFW sample coverage 결과
16. ADM/BIZADM sample coverage 결과
17. 샘플별 test/evidence 연결 결과
18. placeholder-only sample 검사 결과
19. qualityGate 보강 결과
20. 실제 실행 명령
21. evidence 경로
22. 완료/부분 구현/미구현/미검증/실패/재확인 필요 분리
23. 실제 외부환경 미검증 항목과 사유
24. 대안 적용이 필요한 항목과 사유
25. Git commit/push/branch 미수행 여부
```
