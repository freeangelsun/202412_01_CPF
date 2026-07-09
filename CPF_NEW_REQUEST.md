# Codex 요청서 — CPF 최종 EDU 샘플 실구현, 기능별 패키지 정리, 기능 검증 Coverage, 압축 Capability, Evidence/Gate 강화

## 0. Codex 공통 작업 지침

이번 작업은 CPF 최종 개발자 참조 샘플과 샘플 기반 기능 검증 coverage를 실구현하는 작업이다.

기준 repository:

```text
https://github.com/freeangelsun/202412_01_CPF
branch: master
```

기준 파일:

```text
CPF_FINAL_TARGET_REQUIREMENTS.md
CPF_REVIEW_PROGRESS_COMPLETION_GUIDE.md
CPF_STABILIZATION_REPORT.md
CPF_GAP_MATRIX.md
CPF_EVIDENCE_INDEX.md
specs/sample-coverage-matrix.md
specs/기능_구현_매트릭스.html
```

### 0.1 수정 금지 파일

아래 파일은 수정하지 않는다.

```text
CPF_FINAL_TARGET_REQUIREMENTS.md
CPF_REVIEW_PROGRESS_COMPLETION_GUIDE.md
CPF_NEW_REQUEST.md
```

`CPF_REVIEW_PROGRESS_COMPLETION_GUIDE.md`는 ChatGPT 검수 기준 파일이다. 읽고 기준으로 참고할 수는 있으나 수정하지 않는다.

### 0.2 Git 금지

```text
Git commit 금지
Git push 금지
branch 생성 금지
```

### 0.3 상태값

상태값은 아래 6개만 사용한다.

```text
완료
부분 구현
미구현
미검증
실패
재확인 필요
```

### 0.4 완료로 기록하면 안 되는 항목

아래는 실제 수행하지 않았으면 완료로 기록하지 않는다.

```text
실제 remote deploy
실제 external WAS/JNDI runtime
실제 Kafka/MQ/Redis runtime
실제 SFTP/FTP/SCP/SSH runtime
실제 Vault/KMS/HSM runtime
실제 browser click
실제 MariaDB 신규 빈 DB full install
```

가능한 검증은 아래 수준으로 분리한다.

```text
source-compile
unit-test
contract-test
fixture-test
source-smoke
profile-smoke
dry-run
runtime-smoke
external-runtime-required
```

실제 외부 인프라가 필요한 기능은 source/contract/fixture 수준으로 검증하고, runtime은 `미검증`으로 남긴다.

### 0.5 기술 ownership 기준

PFW는 프레임워크 기술 capability의 소유자다.

PFW 책임 후보:

```text
Service Call Engine
timeout/retry/failover/circuit breaker
service/endpoint/instance registry
selectedInstanceId logging
broker publish/consume port
outbox/inbox port
DLQ/replay 후보
file transfer port
SFTP/FTP/SCP/SSH request/result/plan
archive/compression port
ZIP/GZIP/TAR 후보
secret provider port
credential reference
token provider port
key/cert provider port
runtime heartbeat
distributed lock
health/ghost detection 후보
audit context
masking policy
ADM status DTO 후보
```

CMN은 프로젝트 공통 helper/rule/fixture 소유자다.

CMN 책임 후보:

```text
공통 코드
공통 메시지
validation helper
converter/helper
fixed-length layout/parser/formatter
fixed-length fixture
파일명/디렉터리 규칙 helper
공통 masking helper 후보
```

EXS는 외부연계 기술 소유자가 아니다. EXS는 대외업무 adapter/기관별 구현체다.

EXS 책임 후보:

```text
기관별 endpoint 설정
기관별 adapter
대외 전문 송수신 업무 구현체
대외 송수신 원장
대외 unknown result 처리
대외 reconciliation 구현체
대외 파일 송수신 업무 adapter
```

금지:

```text
PFW가 EXS/ACC/MBR/BAT/BIZADM에 의존
CMN이 업무 주제영역 구현체에 의존
ACC/MBR/BAT가 EXS 내부 기술 클래스를 공통 기능처럼 직접 사용
업무 주제영역 간 내부 기술 클래스 재사용
fixed-length parser/formatter가 EXS 전용
timeout/retry/circuit/failover가 EXS 전용
OAuth/JWT/mTLS가 EXS 전용
unknown result/reconciliation 표준 모델이 EXS 전용
```

### 0.6 Spring Event 기준

Spring Event는 핵심 거래 흐름의 중심 기술로 사용하지 않는다.

허용:

```text
감사/알림/cache invalidation/telemetry
이미 DB 상태 저장 후 부가 후처리
유실되어도 재계산 가능한 보조 hook
```

금지 또는 제한:

```text
핵심 거래 상태 전이를 ApplicationEvent/EventListener 체인에 의존
외부 송신을 outbox 없이 EventListener에서 직접 처리
Saga/compensation을 Spring Event만으로 처리
unknown result/reconciliation을 이벤트 체인에만 의존
multi-instance 전달을 Spring Event로 완료 주장
broker DLQ/replay를 Spring Event로 대체
```

## 1. 이번 작업의 배경과 문제

이전 작업에서 `specs/sample-coverage-matrix.md`가 생성되었고 약 192개 sampleId가 등록되었다.

하지만 현재 문제는 다음과 같다.

```text
1. sampleId 목록은 생겼지만 실제 샘플 class가 부족하다.
2. 다수 sampleId가 실제 샘플이 아니라 CoverageCatalog 파일을 sourcePath로 가진다.
3. CoverageCatalogTest는 sampleId 목록 검증일 뿐 기능 검증이 아니다.
4. BAT EDU는 catalog + BatTaskletEducationSample 수준으로 실제 케이스별 샘플이 거의 없다.
5. XYZ EDU는 CRUD/query 일부는 있으나 service-call/facade/header/idempotency/security/audit/file/broker/archive 등은 부족하다.
6. Kafka/MQ/SFTP/SSH 등은 source/contract와 runtime 검증이 분리되어야 한다.
7. ZIP/GZIP/TAR 등 압축/archive capability와 샘플이 부족하다.
8. qualityGate가 catalog-only sample을 완료로 통과시킬 수 있다.
9. evidence/log 파일 실제 존재 정합성도 재확인이 필요하다.
10. EDU 샘플이 catalog 또는 controller 한 곳에 몰리면 개발자가 기능별로 찾기 어렵다.
```

이번 작업은 `sampleId 목록 추가`가 아니라, **기존 sample coverage 목록을 유지하면서 catalog-only/부분 구현/미검증 항목을 실제 샘플 class + test + evidence로 전환**하는 것이다.

핵심 원칙:

```text
CoverageCatalog와 matrix 행은 샘플 구현이 아니다.
실제 개발자가 참고할 수 있는 sample class와 test가 있어야 한다.
샘플은 기능 검증 자산이어야 한다.
EDU 샘플은 기능별 package 하위에 정리되어야 한다.
```

## 2. 선행 확인 및 gap 분류

작업 시작 시 아래를 먼저 확인한다.

```powershell
git status --short
git ls-files > specs/evidence/20260708_05/git-files.txt
git ls-files "specs/evidence/**" > specs/evidence/20260708_05/evidence-files.txt
git ls-files "*EducationCoverageCatalog*.java" > specs/evidence/20260708_05/coverage-catalog-files.txt
git ls-files "*EducationSample*.java" > specs/evidence/20260708_05/education-sample-files.txt
git ls-files "*EducationSampleTest*.java" > specs/evidence/20260708_05/education-sample-test-files.txt
```

필수 산출:

```text
specs/evidence/20260708_05/pre-work-git-status.sanitized.txt
specs/evidence/20260708_05/sample-baseline-summary.sanitized.json
specs/evidence/20260708_05/catalog-only-baseline.sanitized.json
```

분류 기준:

```text
완료:
- 실제 sample class 존재
- 실제 test 존재
- evidence 존재
- sourcePath가 CoverageCatalog가 아님
- testPath가 CoverageCatalogTest만이 아님

부분 구현:
- 실제 sample class는 있으나 test/evidence가 부족
- source/contract만 있고 runtime은 없음
- catalog와 실제 샘플이 섞여 있음

미구현:
- sampleId만 있고 실제 샘플 class 없음

미검증:
- 외부 runtime 필요
- 테스트 미수행
- evidence 없음

실패:
- 완료로 기록했으나 source/test/evidence 없음
- ownership 위반
- Spring Event 핵심 흐름 사용
- 없는 evidence 참조
```

## 3. Evidence/log 정합성 재검증

리포트, evidence index, 기능 매트릭스, sample coverage matrix가 참조하는 모든 evidence/log path의 실제 파일 존재를 확인한다.

필수 확인 대상:

```text
CPF_STABILIZATION_REPORT.md
CPF_EVIDENCE_INDEX.md
CPF_GAP_MATRIX.md
specs/기능_구현_매트릭스.html
specs/sample-coverage-matrix.md
specs/evidence/20260708_04/*
specs/evidence/20260708_05/*
```

필수 작업:

```text
1. 모든 evidencePath/logPath 실제 존재 확인
2. 없는 path 제거 또는 실제 evidence 생성
3. stale evidence를 신규 완료 근거로 쓰지 않도록 정리
4. SKIPPED/미실행 evidence를 완료 근거로 쓰지 않도록 정리
5. checkEvidencePathExistence 또는 관련 check script가 .log 누락도 잡도록 보강
6. qualityGate에서 evidence/log 누락 시 실패 처리
```

필수 evidence:

```text
specs/evidence/20260708_05/evidence-path-existence-check.sanitized.json
specs/evidence/20260708_05/report-evidence-log-existence.sanitized.json
specs/evidence/20260708_05/evidence-index-consistency.sanitized.json
```

완료 기준:

```text
report/index/matrix/sample coverage가 참조하는 evidence/log 파일 실제 존재
없는 evidence/log 참조 0건
stale evidence 신규 완료 근거 0건
qualityGate가 evidence/log 누락을 실패로 잡음
```

## 4. sample coverage gate 강화

현재 sample coverage gate가 catalog-only sample을 통과시키면 안 된다.

### 4.1 CoverageCatalog 완료 근거 금지

아래 파일은 샘플 목록 관리용으로만 사용한다.

```text
*EducationCoverageCatalog.java
*EducationCoverageCatalogTest.java
```

규칙:

```text
sourcePath가 CoverageCatalog이면 status=완료 금지
testPath가 CoverageCatalogTest이면 validationLevel=unit-test 완료 금지
catalog-only 항목은 부분 구현, 미구현, 미검증 중 하나로만 기록
catalog-only가 완료이면 qualityGate 실패
```

### 4.2 sample source 중복 제한

하나의 sourcePath가 너무 많은 sampleId를 대표하면 실제 샘플이 부족한 것이다.

기준:

```text
CoverageCatalog는 완료 sampleId를 대표할 수 없음
하나의 실제 sample class는 최대 3~5개의 밀접한 sampleId만 대표 가능
10개 이상 sampleId가 같은 sourcePath를 공유하면 qualityGate warning 또는 실패
```

### 4.3 실제 샘플 강제

가능한 sampleId는 실제 샘플 class와 test class를 가진다.

권장 네이밍:

```text
<Feature>EducationSample.java
<Feature>EducationSampleTest.java
```

예:

```text
BatRetryEducationSample.java
BatRetryEducationSampleTest.java
XyzIdempotencyEducationSample.java
XyzIdempotencyEducationSampleTest.java
PfwArchiveEducationSample.java
PfwArchiveEducationSampleTest.java
```

필수 evidence:

```text
specs/evidence/20260708_05/sample-coverage-result.sanitized.json
specs/evidence/20260708_05/sample-catalog-only-scan.sanitized.json
specs/evidence/20260708_05/sample-source-duplication-scan.sanitized.json
specs/evidence/20260708_05/sample-actual-implementation-scan.sanitized.json
```

## 5. 기존 sample coverage 목록 유지

이전 요청에서 정의한 `specs/sample-coverage-matrix.md`의 전체 sampleId 목록은 삭제하거나 축소하지 않는다.

이번 작업은 기존 sampleId 목록을 대체하는 작업이 아니라, 기존 sampleId 전체를 유지한 상태에서 아래 항목을 실제 구현으로 전환하는 작업이다.

```text
부분 구현
미구현
미검증
catalog-only
```

sampleId를 삭제해야 할 경우에는 삭제하지 말고 `재확인 필요`로 남기고 사유를 기록한다.

## 6. EDU 샘플 패키지 구조 기준

EDU 샘플은 한 패키지에 몰아넣지 않는다.
개발자가 기능별로 쉽게 찾고 참고할 수 있도록 `edu` 하위에 기능별 package를 나눠 구성한다.

`catalog` 패키지는 sampleId 목록 관리와 coverage tracking 용도로만 사용한다.
실제 개발자 참조 샘플은 반드시 기능별 package 하위에 둔다.

### 6.1 XYZ EDU 패키지 기준

XYZ 온라인 업무 샘플은 아래와 같이 `xyz/src/main/java/cpf/xyz/edu` 하위에 기능별 package로 분리한다.

```text
xyz/src/main/java/cpf/xyz/edu/crud
xyz/src/main/java/cpf/xyz/edu/query
xyz/src/main/java/cpf/xyz/edu/pagination
xyz/src/main/java/cpf/xyz/edu/detail
xyz/src/main/java/cpf/xyz/edu/transaction
xyz/src/main/java/cpf/xyz/edu/servicecall
xyz/src/main/java/cpf/xyz/edu/facade
xyz/src/main/java/cpf/xyz/edu/header
xyz/src/main/java/cpf/xyz/edu/idempotency
xyz/src/main/java/cpf/xyz/edu/failure
xyz/src/main/java/cpf/xyz/edu/security
xyz/src/main/java/cpf/xyz/edu/audit
xyz/src/main/java/cpf/xyz/edu/validation
xyz/src/main/java/cpf/xyz/edu/telegram
xyz/src/main/java/cpf/xyz/edu/messaging
xyz/src/main/java/cpf/xyz/edu/filetransfer
xyz/src/main/java/cpf/xyz/edu/archive
xyz/src/main/java/cpf/xyz/edu/operation
```

테스트도 동일한 기능별 package 구조를 따른다.

```text
xyz/src/test/java/cpf/xyz/edu/crud
xyz/src/test/java/cpf/xyz/edu/query
xyz/src/test/java/cpf/xyz/edu/pagination
xyz/src/test/java/cpf/xyz/edu/detail
xyz/src/test/java/cpf/xyz/edu/transaction
xyz/src/test/java/cpf/xyz/edu/servicecall
xyz/src/test/java/cpf/xyz/edu/facade
xyz/src/test/java/cpf/xyz/edu/header
xyz/src/test/java/cpf/xyz/edu/idempotency
xyz/src/test/java/cpf/xyz/edu/failure
xyz/src/test/java/cpf/xyz/edu/security
xyz/src/test/java/cpf/xyz/edu/audit
xyz/src/test/java/cpf/xyz/edu/validation
xyz/src/test/java/cpf/xyz/edu/telegram
xyz/src/test/java/cpf/xyz/edu/messaging
xyz/src/test/java/cpf/xyz/edu/filetransfer
xyz/src/test/java/cpf/xyz/edu/archive
xyz/src/test/java/cpf/xyz/edu/operation
```

### 6.2 BAT EDU 패키지 기준

BAT 배치 샘플은 아래와 같이 `bat/src/main/java/cpf/bat/edu` 하위에 기능별 package로 분리한다.

```text
bat/src/main/java/cpf/bat/edu/job
bat/src/main/java/cpf/bat/edu/chunk
bat/src/main/java/cpf/bat/edu/transaction
bat/src/main/java/cpf/bat/edu/servicecall
bat/src/main/java/cpf/bat/edu/centercut
bat/src/main/java/cpf/bat/edu/logging
bat/src/main/java/cpf/bat/edu/idempotency
bat/src/main/java/cpf/bat/edu/retry
bat/src/main/java/cpf/bat/edu/restart
bat/src/main/java/cpf/bat/edu/reconciliation
bat/src/main/java/cpf/bat/edu/archive
```

테스트도 동일한 기능별 package 구조를 따른다.

```text
bat/src/test/java/cpf/bat/edu/job
bat/src/test/java/cpf/bat/edu/chunk
bat/src/test/java/cpf/bat/edu/transaction
bat/src/test/java/cpf/bat/edu/servicecall
bat/src/test/java/cpf/bat/edu/centercut
bat/src/test/java/cpf/bat/edu/logging
bat/src/test/java/cpf/bat/edu/idempotency
bat/src/test/java/cpf/bat/edu/retry
bat/src/test/java/cpf/bat/edu/restart
bat/src/test/java/cpf/bat/edu/reconciliation
bat/src/test/java/cpf/bat/edu/archive
```

### 6.3 EXS EDU 패키지 기준

EXS 대외연계 샘플도 기능별 package로 분리한다.

```text
exs/src/main/java/cpf/exs/edu/fixedlength
exs/src/main/java/cpf/exs/edu/endpoint
exs/src/main/java/cpf/exs/edu/auth
exs/src/main/java/cpf/exs/edu/servicecall
exs/src/main/java/cpf/exs/edu/retry
exs/src/main/java/cpf/exs/edu/unknown
exs/src/main/java/cpf/exs/edu/reconciliation
exs/src/main/java/cpf/exs/edu/ledger
exs/src/main/java/cpf/exs/edu/filetransfer
exs/src/main/java/cpf/exs/edu/archive
```

테스트도 동일한 구조로 둔다.

```text
exs/src/test/java/cpf/exs/edu/fixedlength
exs/src/test/java/cpf/exs/edu/endpoint
exs/src/test/java/cpf/exs/edu/auth
exs/src/test/java/cpf/exs/edu/servicecall
exs/src/test/java/cpf/exs/edu/retry
exs/src/test/java/cpf/exs/edu/unknown
exs/src/test/java/cpf/exs/edu/reconciliation
exs/src/test/java/cpf/exs/edu/ledger
exs/src/test/java/cpf/exs/edu/filetransfer
exs/src/test/java/cpf/exs/edu/archive
```

### 6.4 PFW EDU/Test-Support 패키지 기준

PFW capability 샘플은 PFW 하위 기능별 package에 둔다.
PFW는 기술 capability 소유자이므로 archive, broker, filetransfer, servicecall, security, runtime 등은 PFW 하위에 위치해야 한다.

```text
pfw/src/main/java/cpf/pfw/common/servicecall
pfw/src/main/java/cpf/pfw/common/broker
pfw/src/main/java/cpf/pfw/common/filetransfer
pfw/src/main/java/cpf/pfw/common/archive
pfw/src/main/java/cpf/pfw/common/security
pfw/src/main/java/cpf/pfw/common/runtime
pfw/src/main/java/cpf/pfw/common/admin
pfw/src/main/java/cpf/pfw/common/audit
pfw/src/main/java/cpf/pfw/common/masking
```

테스트는 아래 구조를 따른다.

```text
pfw/src/test/java/cpf/pfw/common/servicecall
pfw/src/test/java/cpf/pfw/common/broker
pfw/src/test/java/cpf/pfw/common/filetransfer
pfw/src/test/java/cpf/pfw/common/archive
pfw/src/test/java/cpf/pfw/common/security
pfw/src/test/java/cpf/pfw/common/runtime
pfw/src/test/java/cpf/pfw/common/admin
pfw/src/test/java/cpf/pfw/common/audit
pfw/src/test/java/cpf/pfw/common/masking
```

### 6.5 CMN EDU/Test-Support 패키지 기준

CMN은 기술 engine 소유자가 아니라 공통 helper, parser, formatter, fixture, validation 역할을 가진다.

```text
cmn/src/main/java/cpf/cmn/edu/fixedlength
cmn/src/main/java/cpf/cmn/edu/validation
cmn/src/main/java/cpf/cmn/edu/converter
cmn/src/main/java/cpf/cmn/edu/message
cmn/src/main/java/cpf/cmn/edu/code
cmn/src/main/java/cpf/cmn/edu/fixture
cmn/src/main/java/cpf/cmn/edu/file
cmn/src/main/java/cpf/cmn/edu/masking
```

테스트는 아래 구조를 따른다.

```text
cmn/src/test/java/cpf/cmn/edu/fixedlength
cmn/src/test/java/cpf/cmn/edu/validation
cmn/src/test/java/cpf/cmn/edu/converter
cmn/src/test/java/cpf/cmn/edu/message
cmn/src/test/java/cpf/cmn/edu/code
cmn/src/test/java/cpf/cmn/edu/fixture
cmn/src/test/java/cpf/cmn/edu/file
cmn/src/test/java/cpf/cmn/edu/masking
```

### 6.6 ADM/BIZADM EDU 패키지 기준

ADM/BIZADM 운영/관리 샘플도 기능별 package로 분리한다.

```text
adm/src/main/java/cpf/adm/edu/operation
adm/src/main/java/cpf/adm/edu/registry
adm/src/main/java/cpf/adm/edu/servicecall
adm/src/main/java/cpf/adm/edu/batch
adm/src/main/java/cpf/adm/edu/filetransfer
adm/src/main/java/cpf/adm/edu/broker
adm/src/main/java/cpf/adm/edu/runtime
adm/src/main/java/cpf/adm/edu/scheduler
```

```text
bizadm/src/main/java/cpf/bizadm/edu/auth
bizadm/src/main/java/cpf/bizadm/edu/menu
bizadm/src/main/java/cpf/bizadm/edu/api
bizadm/src/main/java/cpf/bizadm/edu/button
bizadm/src/main/java/cpf/bizadm/edu/audit
bizadm/src/main/java/cpf/bizadm/edu/download
bizadm/src/main/java/cpf/bizadm/edu/masking
```

테스트도 동일한 기능별 package 구조를 따른다.

### 6.7 패키지 구조 검증 기준

qualityGate 또는 sample coverage scan에서 아래를 검증한다.

```text
- EDU 실제 샘플이 catalog package에만 몰려 있지 않은지
- 기능별 package 하위에 sample class가 존재하는지
- test package도 main package와 동일한 기능별 구조를 가지는지
- sampleId의 featureArea와 sourcePath package가 일치하는지
- BAT EDU가 job/chunk/transaction/servicecall/centercut/logging/idempotency/retry/restart/reconciliation/archive로 분리되어 있는지
- XYZ EDU가 crud/query/pagination/detail/transaction/servicecall/facade/header/idempotency/failure/security/audit/validation/telegram/messaging/filetransfer/archive/operation으로 분리되어 있는지
- CoverageCatalog package만 있고 실제 기능별 package가 없으면 실패 또는 부분 구현
- 하나의 controller 또는 catalog에 모든 샘플을 몰아넣으면 완료 불가
```

완료 기준:

```text
- sampleId별 기능과 package가 맞음
- sourcePath가 기능별 edu package 하위에 있음
- testPath가 기능별 edu package 하위에 있음
- catalog는 coverage tracking 용도로만 사용
- 실제 개발자가 패키지만 보고 기능 샘플을 찾을 수 있음
```

## 7. XYZ EDU — 온라인 업무 개발자 참조 샘플 실구현

XYZ EDU는 별도 서비스가 아니라 XYZ 내부 개발자 교육 샘플 패키지다.

허용 위치:

```text
xyz/src/main/java/cpf/xyz/edu
xyz/src/test/java/cpf/xyz/edu
```

금지:

```text
별도 EDU module
별도 EDU bootJar
별도 EDU server.port
별도 EDU deploy env
EDU_SERVER_PORT
-PcpfModule=EDU 성공 처리
```

### 7.1 XYZ 필수 sampleId

아래 sampleId는 유지하고 가능한 범위에서 실제 sample class + test로 전환한다.

```text
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

XYZ-EDU-TLM-001 fixed-length parse
XYZ-EDU-TLM-002 fixed-length write
XYZ-EDU-TLM-003 round-trip validation
XYZ-EDU-TLM-004 invalid field validation

XYZ-EDU-MSG-001 broker publish 샘플
XYZ-EDU-MSG-002 broker consume 샘플
XYZ-EDU-MSG-003 outbox/inbox 샘플
XYZ-EDU-MSG-004 broker idempotency 샘플
XYZ-EDU-MSG-005 DLQ/replay 후보 샘플

XYZ-EDU-FILE-001 local file 처리
XYZ-EDU-FILE-002 SFTP transfer plan
XYZ-EDU-FILE-003 FTP 후보
XYZ-EDU-FILE-004 SCP 후보
XYZ-EDU-FILE-005 SSH command plan
XYZ-EDU-FILE-006 checksum
XYZ-EDU-FILE-007 temp/rename/archive policy
XYZ-EDU-FILE-008 transfer history

XYZ-EDU-ARCHIVE-001 업무 파일 ZIP 생성 샘플
XYZ-EDU-ARCHIVE-002 업무 파일 ZIP 해제 샘플
XYZ-EDU-ARCHIVE-003 GZIP 단건 파일 샘플
XYZ-EDU-ARCHIVE-004 zip-slip 방지 사용 샘플

XYZ-EDU-OPER-001 ADM 거래 로그 연계 후보
XYZ-EDU-OPER-002 실패 구간 추적 후보
```

### 7.2 XYZ 금지

```text
운영 샘플에서 URL 직접 조합
Controller 직접 호출
타 주제영역 Repository/Mapper 직접 참조
EXS 내부 기술 클래스를 공통 기능처럼 사용
Spring Event 핵심 거래 흐름 샘플
placeholder-only sample
CoverageCatalog만으로 완료 처리
```

외부 URL WebClient 샘플이 있다면 `external-http 교육용 예외`로 분리하고, 내부 주제영역 호출 샘플과 섞지 않는다.

## 8. BAT EDU — 배치 개발자 참조 샘플 실구현

BAT EDU는 BAT 내부 배치 교육 샘플 패키지다.

허용 위치:

```text
bat/src/main/java/cpf/bat/edu
bat/src/test/java/cpf/bat/edu
```

현재 BAT EDU는 catalog와 `BatTaskletEducationSample` 수준이므로 부족하다. 이번 작업에서 BAT는 최우선으로 실제 샘플을 확장한다.

### 8.1 BAT 필수 실제 샘플 클래스

가능한 범위에서 아래 실제 샘플 class와 test를 만든다.

```text
BatTaskletJobEducationSample.java
BatTaskletJobEducationSampleTest.java

BatChunkJobEducationSample.java
BatChunkJobEducationSampleTest.java

BatJobParameterValidationEducationSample.java
BatJobParameterValidationEducationSampleTest.java

BatJobStatusTransitionEducationSample.java
BatJobStatusTransitionEducationSampleTest.java

BatRetryEducationSample.java
BatRetryEducationSampleTest.java

BatRestartEducationSample.java
BatRestartEducationSampleTest.java

BatDuplicateExecutionGuardEducationSample.java
BatDuplicateExecutionGuardEducationSampleTest.java

BatTransactionEducationSample.java
BatTransactionEducationSampleTest.java

BatItemTransactionEducationSample.java
BatItemTransactionEducationSampleTest.java

BatPartialRollbackEducationSample.java
BatPartialRollbackEducationSampleTest.java

BatOnlineServiceCallEducationSample.java
BatOnlineServiceCallEducationSampleTest.java

BatServiceCallEngineEducationSample.java
BatServiceCallEngineEducationSampleTest.java

BatFacadeCallEducationSample.java
BatFacadeCallEducationSampleTest.java

BatCenterCutExecutionEducationSample.java
BatCenterCutExecutionEducationSampleTest.java

BatCenterCutTargetEducationSample.java
BatCenterCutTargetEducationSampleTest.java

BatCenterCutItemProcessingEducationSample.java
BatCenterCutItemProcessingEducationSampleTest.java

BatCenterCutRetryEducationSample.java
BatCenterCutRetryEducationSampleTest.java

BatCenterCutResultSummaryEducationSample.java
BatCenterCutResultSummaryEducationSampleTest.java

BatJobLogEducationSample.java
BatJobLogEducationSampleTest.java

BatIdempotencyEducationSample.java
BatIdempotencyEducationSampleTest.java

BatUnknownResultEducationSample.java
BatUnknownResultEducationSampleTest.java

BatReconciliationEducationSample.java
BatReconciliationEducationSampleTest.java

BatArchiveCompressionEducationSample.java
BatArchiveCompressionEducationSampleTest.java
```

### 8.2 BAT 필수 sampleId

아래 sampleId는 실제 샘플로 전환한다.

```text
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

BAT-EDU-ARCHIVE-001 배치 결과 파일 ZIP 압축 샘플
BAT-EDU-ARCHIVE-002 배치 결과 파일 GZIP 압축 샘플
BAT-EDU-ARCHIVE-003 압축 후 checksum 샘플
```

완료 기준:

```text
BAT edu 실제 sample class 15개 이상
BAT edu 실제 test class 15개 이상
center-cut만 있고 일반 배치가 없으면 완료 불가
일반 Tasklet 1개만 있으면 완료 불가
배치에서 온라인 호출 샘플 없으면 완료 불가
jobExecution 단위 로그 샘플 없으면 완료 불가
archive/compression 샘플 없으면 완료 불가
CoverageCatalog는 완료 근거로 사용 금지
```

## 9. PFW Archive/Compression Capability 추가

압축 기능은 특정 업무 주제영역이 아니라 프레임워크 capability 후보이므로 PFW가 소유한다.

### 9.1 기능 범위

PFW에 archive/compression 표준 port와 local implementation을 추가한다.

필수 기능:

```text
ZIP create
ZIP extract
GZIP compress
GZIP decompress
TAR create 후보
TAR extract 후보
파일 압축
디렉터리 압축
압축 전 checksum
압축 후 checksum
압축 해제 zip-slip 방지
허용 base directory 검증
max entry size 검증
max total size 검증
파일명 sanitize
temp file 생성
rename/archive policy
duplicate prevention
압축 결과 DTO
압축 이력 DTO 후보
```

주의:

```text
tar는 Java 표준만으로 부족하면 후보/부분 구현으로 남기고 무리한 dependency 추가 금지
외부 라이브러리 추가가 필요하면 사유와 대안 기록
password zip, 암호화 zip은 후순위 후보로 기록
secret/password/key 원문 evidence 기록 금지
```

### 9.2 권장 패키지

```text
pfw/src/main/java/cpf/pfw/common/archive
pfw/src/test/java/cpf/pfw/common/archive
```

권장 클래스:

```text
CpfArchiveService.java
CpfArchiveRequest.java
CpfArchiveResult.java
CpfArchiveEntry.java
CpfArchiveFormat.java
CpfArchivePolicy.java
CpfZipSlipGuard.java
CpfArchiveChecksum.java
LocalCpfArchiveService.java
```

권장 테스트:

```text
CpfArchiveServiceTest.java
CpfZipSlipGuardTest.java
CpfArchiveChecksumTest.java
```

### 9.3 archive sampleId 추가

`specs/sample-coverage-matrix.md`와 sample coverage gate에 아래 sampleId를 추가한다.

```text
PFW-EDU-ARCHIVE-001 ZIP create
PFW-EDU-ARCHIVE-002 ZIP extract
PFW-EDU-ARCHIVE-003 GZIP compress
PFW-EDU-ARCHIVE-004 GZIP decompress
PFW-EDU-ARCHIVE-005 checksum before/after
PFW-EDU-ARCHIVE-006 zip-slip 방지
PFW-EDU-ARCHIVE-007 max entry/total size guard
PFW-EDU-ARCHIVE-008 temp/rename/archive policy
PFW-EDU-ARCHIVE-009 duplicate prevention
PFW-EDU-ARCHIVE-010 TAR 후보

XYZ-EDU-ARCHIVE-001 업무 파일 ZIP 생성 샘플
XYZ-EDU-ARCHIVE-002 업무 파일 ZIP 해제 샘플
XYZ-EDU-ARCHIVE-003 GZIP 단건 파일 샘플
XYZ-EDU-ARCHIVE-004 zip-slip 방지 사용 샘플

BAT-EDU-ARCHIVE-001 배치 결과 파일 ZIP 압축 샘플
BAT-EDU-ARCHIVE-002 배치 결과 파일 GZIP 압축 샘플
BAT-EDU-ARCHIVE-003 압축 후 checksum 샘플

EXS-EDU-ARCHIVE-001 대외 송신 파일 압축 후보
EXS-EDU-ARCHIVE-002 대외 수신 파일 압축 해제 후보
```

완료 기준:

```text
PFW archive/compression port 존재
local implementation 존재
ZIP/GZIP unit test 존재
zip-slip 방지 test 존재
checksum test 존재
XYZ/BAT/EXS가 PFW archive capability를 사용하는 샘플 존재
TAR는 구현했으면 test, 미구현이면 부분 구현/후순위 사유 기록
```

## 10. Broker/Kafka/MQ 샘플 실구현

실제 Kafka/MQ runtime은 미검증으로 남긴다. 대신 PFW port/contract/in-memory adapter 수준 샘플과 업무 사용 샘플을 만든다.

### 10.1 PFW Broker 샘플

필수 sampleId:

```text
PFW-EDU-BROKER-001 broker publish port
PFW-EDU-BROKER-002 broker consume port
PFW-EDU-BROKER-003 message envelope/header
PFW-EDU-BROKER-004 outbox 후보
PFW-EDU-BROKER-005 inbox 후보
PFW-EDU-BROKER-006 DLQ 후보
PFW-EDU-BROKER-007 replay 후보
PFW-EDU-BROKER-008 broker idempotency
```

필수 구현 기준:

```text
BrokerPublishPort
BrokerConsumePort
BrokerMessageEnvelope
BrokerMessageHeader
OutboxPort 후보
InboxPort 후보
DlqReplayRequest 후보
InMemoryBrokerContractAdapter 후보
contract test
```

### 10.2 XYZ Broker 샘플

```text
XyzBrokerPublishEducationSample.java
XyzBrokerPublishEducationSampleTest.java

XyzBrokerConsumeEducationSample.java
XyzBrokerConsumeEducationSampleTest.java

XyzOutboxInboxEducationSample.java
XyzOutboxInboxEducationSampleTest.java

XyzBrokerIdempotencyEducationSample.java
XyzBrokerIdempotencyEducationSampleTest.java
```

### 10.3 BAT/EXS Broker 샘플

```text
BatBrokerPublishAfterJobEducationSample.java
BatBrokerPublishAfterJobEducationSampleTest.java

BatOutboxJobEducationSample.java
BatOutboxJobEducationSampleTest.java

ExsBrokerInboundEducationSample.java
ExsBrokerInboundEducationSampleTest.java

ExsBrokerOutboundEducationSample.java
ExsBrokerOutboundEducationSampleTest.java
```

완료 기준:

```text
실제 Kafka/MQ runtime은 미검증
PFW port/DTO/contract test 완료
업무 샘플은 PFW port 또는 facade 사용
CMN이 broker engine 소유자가 되지 않음
```

## 11. File Transfer / SFTP / FTP / SCP / SSH 샘플 실구현

실제 SFTP/SSH runtime은 미검증으로 남긴다. source/contract/plan 샘플은 실제 구현한다.

### 11.1 PFW File Transfer 샘플

필수 sampleId:

```text
PFW-EDU-FILE-001 file transfer request/result
PFW-EDU-FILE-002 SFTP/FTP/SCP/SSH protocol 후보
PFW-EDU-FILE-003 checksum policy
PFW-EDU-FILE-004 temp/rename/archive policy
PFW-EDU-FILE-005 duplicate prevention
PFW-EDU-FILE-006 transfer history
```

필수 구현 기준:

```text
FileTransferPort
FileTransferRequest
FileTransferResult
FileTransferProtocol
RemoteCommandRequest
RemoteCommandResult
ChecksumPolicy
TempRenameArchivePolicy
DuplicatePreventionPolicy
TransferHistory DTO 후보
contract test
```

### 11.2 XYZ File Transfer 샘플

```text
XyzLocalFileEducationSample.java
XyzLocalFileEducationSampleTest.java

XyzSftpTransferPlanEducationSample.java
XyzSftpTransferPlanEducationSampleTest.java

XyzSshCommandPlanEducationSample.java
XyzSshCommandPlanEducationSampleTest.java

XyzFileChecksumEducationSample.java
XyzFileChecksumEducationSampleTest.java

XyzFileTransferHistoryEducationSample.java
XyzFileTransferHistoryEducationSampleTest.java
```

### 11.3 BAT/EXS File Transfer 샘플

```text
BatResultFileTransferEducationSample.java
BatResultFileTransferEducationSampleTest.java

BatArchiveAndTransferEducationSample.java
BatArchiveAndTransferEducationSampleTest.java

ExsInstitutionFileSendEducationSample.java
ExsInstitutionFileSendEducationSampleTest.java

ExsInstitutionFileReceiveEducationSample.java
ExsInstitutionFileReceiveEducationSampleTest.java
```

주의:

```text
기술 engine은 PFW port가 소유
CMN은 파일명/경로 helper, fixture 중심
CmnFileExchangeService 직접 사용 구조가 있으면 PFW ownership 기준과 충돌 여부 검토
충돌하면 PFW port로 이동 또는 adapter 경계 분리
```

## 12. 고정길이 전문 샘플 실구현

고정길이 전문은 CMN parser/formatter + EXS 업무 adapter + XYZ 업무 사용 샘플 구조로 만든다.

### 12.1 CMN 샘플

```text
CmnFixedLengthLayoutEducationSample.java
CmnFixedLengthLayoutEducationSampleTest.java

CmnFixedLengthParserEducationSample.java
CmnFixedLengthParserEducationSampleTest.java

CmnFixedLengthFormatterEducationSample.java
CmnFixedLengthFormatterEducationSampleTest.java

CmnFixedLengthRoundTripEducationSample.java
CmnFixedLengthRoundTripEducationSampleTest.java

CmnFixedLengthInvalidFieldEducationSample.java
CmnFixedLengthInvalidFieldEducationSampleTest.java

CmnFixedLengthKoreanByteLengthEducationSample.java
CmnFixedLengthKoreanByteLengthEducationSampleTest.java

CmnFixedLengthRepeatingGroupEducationSample.java
CmnFixedLengthRepeatingGroupEducationSampleTest.java
```

필수 sampleId:

```text
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
```

### 12.2 EXS 샘플

```text
ExsFixedLengthSendAdapterEducationSample.java
ExsFixedLengthSendAdapterEducationSampleTest.java

ExsFixedLengthReceiveAdapterEducationSample.java
ExsFixedLengthReceiveAdapterEducationSampleTest.java

ExsFixedLengthErrorTelegramEducationSample.java
ExsFixedLengthErrorTelegramEducationSampleTest.java

ExsFixedLengthLedgerEducationSample.java
ExsFixedLengthLedgerEducationSampleTest.java
```

필수 sampleId:

```text
EXS-EDU-FIXED-001 고정길이 전문 송신 업무 adapter
EXS-EDU-FIXED-002 고정길이 전문 수신 업무 adapter
EXS-EDU-FIXED-003 오류 전문 처리
EXS-EDU-FIXED-004 송수신 전문 round-trip
```

### 12.3 XYZ 샘플

```text
XyzFixedLengthBusinessUseEducationSample.java
XyzFixedLengthBusinessUseEducationSampleTest.java
```

완료 기준:

```text
fixed-length parser/formatter가 EXS 전용으로 보이지 않음
CMN parser/formatter + EXS adapter + XYZ business use 구조 확인
fixture test 존재
```

## 13. Service Call / Facade / 타 주제영역 호출 샘플 실구현

### 13.1 PFW Service Call 샘플

필수 sampleId:

```text
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
```

### 13.2 XYZ/BAT 샘플

```text
XyzLocalFacadeEducationSample.java
XyzRemoteFacadeProxyEducationSample.java
XyzServiceCallEngineEducationSample.java
XyzTimeoutRetryFailoverEducationSample.java
XyzHeaderPropagationEducationSample.java

BatOnlineServiceCallEducationSample.java
BatServiceCallEngineEducationSample.java
BatFacadeCallEducationSample.java
```

완료 기준:

```text
내부 주제영역 호출은 PFW Service Call Engine 또는 Facade 기준
URL 직접 조합 금지
Controller 직접 호출 금지
타 주제영역 Repository/Mapper 직접 참조 금지
selectedInstanceId logging 샘플 포함
transactionGlobalId/header propagation 샘플 포함
```

## 14. 트랜잭션 / Idempotency / Unknown Result / Reconciliation 샘플 실구현

### 14.1 XYZ

```text
XyzTransactionStateEducationSample.java
XyzTransactionStateEducationSampleTest.java

XyzRollbackEducationSample.java
XyzRollbackEducationSampleTest.java

XyzIdempotencyEducationSample.java
XyzIdempotencyEducationSampleTest.java

XyzDuplicateRequestEducationSample.java
XyzDuplicateRequestEducationSampleTest.java

XyzUnknownResultEducationSample.java
XyzUnknownResultEducationSampleTest.java

XyzReconciliationCandidateEducationSample.java
XyzReconciliationCandidateEducationSampleTest.java
```

### 14.2 BAT

```text
BatIdempotencyEducationSample.java
BatUnknownResultEducationSample.java
BatReconciliationEducationSample.java
```

### 14.3 EXS

```text
ExsUnknownResultEducationSample.java
ExsUnknownResultEducationSampleTest.java

ExsReconciliationEducationSample.java
ExsReconciliationEducationSampleTest.java

ExsIdempotencyEducationSample.java
ExsIdempotencyEducationSampleTest.java
```

기준:

```text
핵심 상태 전이는 DB 상태/transactionGlobalId/segment/timeline 기준
Spring Event로 핵심 흐름 처리 금지
unknown result는 표준 모델/port/업무 adapter 구조로 표현
reconciliation은 source/contract 수준으로라도 샘플 제공
```

## 15. 보안 / 마스킹 / 감사 / 권한 샘플 실구현

### 15.1 XYZ

```text
XyzApiPermissionEducationSample.java
XyzApiPermissionEducationSampleTest.java

XyzDetailMaskingEducationSample.java
XyzDetailMaskingEducationSampleTest.java

XyzAuditEducationSample.java
XyzAuditEducationSampleTest.java

XyzDownloadAuditEducationSample.java
XyzDownloadAuditEducationSampleTest.java

XyzValidationEducationSample.java
XyzValidationEducationSampleTest.java
```

### 15.2 BIZADM

```text
BizAdmMenuPermissionEducationSample.java
BizAdmMenuPermissionEducationSampleTest.java

BizAdmApiPermissionEducationSample.java
BizAdmApiPermissionEducationSampleTest.java

BizAdmButtonPermissionEducationSample.java
BizAdmButtonPermissionEducationSampleTest.java

BizAdmAuditEducationSample.java
BizAdmAuditEducationSampleTest.java

BizAdmDownloadAuditEducationSample.java
BizAdmDownloadAuditEducationSampleTest.java

BizAdmMaskingPolicyEducationSample.java
BizAdmMaskingPolicyEducationSampleTest.java
```

### 15.3 PFW

```text
PfwAuditContextEducationSample.java
PfwAuditContextEducationSampleTest.java

PfwMaskingPolicyEducationSample.java
PfwMaskingPolicyEducationSampleTest.java

PfwCredentialReferenceEducationSample.java
PfwCredentialReferenceEducationSampleTest.java

PfwSecretProviderEducationSample.java
PfwSecretProviderEducationSampleTest.java

PfwTokenProviderEducationSample.java
PfwTokenProviderEducationSampleTest.java
```

완료 기준:

```text
민감정보 원문 기록 금지
secret/password/token/key/cert 원문 evidence 기록 금지
runtime security infra 없으면 source/contract 검증으로 분리
```

## 16. ADM / BIZADM 운영 샘플 실구현

ADM 운영 샘플은 browser 정본화가 아니라 API/source/static smoke 수준으로 둔다.

필수 sampleId:

```text
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

완료 기준:

```text
실제 browser click 없으면 browser 완료 금지
API/source/static smoke 수준으로 검증 가능
evidence에 validationLevel 명시
```

## 17. sample-coverage-matrix 갱신 기준

`specs/sample-coverage-matrix.md`는 아래 기준으로 갱신한다.

필수 컬럼:

```text
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

기준:

```text
sourcePath:
- 실제 샘플 class 또는 실제 기능 테스트여야 함
- CoverageCatalog는 완료 근거로 사용 금지

testPath:
- 실제 기능/계약/fixture 테스트여야 함
- CoverageCatalogTest는 완료 근거로 사용 금지

evidencePath:
- 실제 존재해야 함
- 완료 sample은 evidencePath 필수

status:
- 실제 샘플 구현 + 테스트 + evidence 있으면 완료
- source만 있으면 부분 구현
- 외부 runtime 필요하고 미실행이면 미검증
- catalog만 있으면 부분 구현 또는 미구현
```

## 18. qualityGate 강화

qualityGate에 아래 검사를 추가 또는 강화한다.

```text
checkSampleCatalogOnly
checkSampleSourceDuplication
checkSampleActualImplementation
checkSamplePackageStructure
checkArchiveCompressionCoverage
checkBatEduActualSamples
checkXyzEduActualSamples
checkPfwCapabilityOwnership
checkEvidenceLogExistence
checkEduNoStandaloneDeploy
checkNoSpringEventCoreFlow
checkNoForbiddenCrossDomainDependency
```

검사 기준:

```text
CoverageCatalog sourcePath로 완료 처리 금지
CoverageCatalogTest testPath로 완료 처리 금지
하나의 sourcePath가 과도하게 많은 sampleId를 대표하면 실패 또는 warning
EDU 실제 샘플이 catalog/controller 한 곳에 몰려 있으면 실패 또는 부분 구현
sampleId featureArea와 sourcePath package 불일치 시 warning 또는 실패
BAT EDU 실제 sample class 수 최소 기준 미달 시 실패
XYZ EDU 주요 영역 실제 sample class 수 최소 기준 미달 시 실패
archive/compression sampleId 누락 시 실패
evidence/log 파일 누락 시 실패
EDU 별도 deploy/module/env/port 발견 시 실패
Spring Event 핵심 흐름 샘플 발견 시 실패
ownership 위반 발견 시 실패 또는 재확인 필요
```

최소 기준:

```text
BAT edu 실제 sample class 15개 이상
BAT edu 실제 test class 15개 이상
XYZ edu 실제 sample class 20개 이상
XYZ edu 실제 test class 20개 이상
PFW archive/compression 실제 class/test 존재
PFW broker/file/security/runtime contract sample 존재
EXS fixed-length/file/unknown/recon adapter sample 존재
CMN fixed-length/helper fixture sample 존재
ADM/BIZADM 운영/권한/감사 sample 존재
```

필수 evidence:

```text
specs/evidence/20260708_05/sample-catalog-only-scan.sanitized.json
specs/evidence/20260708_05/sample-source-duplication-scan.sanitized.json
specs/evidence/20260708_05/sample-actual-implementation-scan.sanitized.json
specs/evidence/20260708_05/sample-package-structure-scan.sanitized.json
specs/evidence/20260708_05/bat-edu-actual-sample-scan.sanitized.json
specs/evidence/20260708_05/xyz-edu-actual-sample-scan.sanitized.json
specs/evidence/20260708_05/archive-compression-coverage.sanitized.json
```

## 19. Gradle / EDU / local port / deploy 정합성 재확인

이전 작업에서 EDU alias는 제거된 것으로 보이나 재검증한다.

필수 확인:

```text
settings.gradle에 별도 edu module 없음
build.gradle cpfServiceModules에 EDU alias 없음
-PcpfModule=EDU 실패
deploy/env/*-edu.env 없음
deploy/inventory EDU entry 없음
EDU_SERVER_PORT 없음
scripts/deploy/deploy-edu.* 없음
```

필수 evidence:

```text
specs/evidence/20260708_05/edu-standalone-deploy-scan.sanitized.json
specs/evidence/20260708_05/local-port-duplicate-scan.sanitized.json
specs/evidence/20260708_05/gradle-remote-deploy-task-scan.sanitized.json
```

local 실행 주체:

```text
ACC
MBR
EXS
ADM
BAT
BIZADM
XYZ
```

실행 주체 아님:

```text
PFW
CMN
EDU
```

## 20. 가비지 파일 / 빈 폴더 재검증

파일 삭제 후 빈 폴더가 남았는지 로컬 checkout 기준으로 검사한다.

필수 evidence:

```text
specs/evidence/20260708_05/garbage-file-scan.sanitized.json
specs/evidence/20260708_05/deleted-files.sanitized.json
specs/evidence/20260708_05/retained-review-required-files.sanitized.json
specs/evidence/20260708_05/empty-directory-scan.sanitized.json
specs/evidence/20260708_05/deleted-empty-directories.sanitized.json
specs/evidence/20260708_05/retained-empty-directories.sanitized.json
```

검사 대상:

```text
scripts/deploy
deploy/env
deploy/inventory
bat/src/main/java/cpf/bat/edu
bat/src/test/java/cpf/bat/edu
xyz/src/main/java/cpf/xyz/edu
xyz/src/test/java/cpf/xyz/edu
pfw/src/main/java/cpf/pfw/common/archive
pfw/src/test/java/cpf/pfw/common/archive
specs/evidence
각 모듈 src/main/resources
각 모듈 src/test/resources
```

## 21. 필수 실행 명령

가능한 범위에서 아래 명령을 실행하고 로그를 evidence로 남긴다.

```powershell
.\gradlew.bat :xyz:test --offline --no-daemon --console=plain
.\gradlew.bat :bat:test --offline --no-daemon --console=plain
.\gradlew.bat :pfw:test --offline --no-daemon --console=plain
.\gradlew.bat :cmn:test --offline --no-daemon --console=plain
.\gradlew.bat :exs:test --offline --no-daemon --console=plain
.\gradlew.bat :adm:test --offline --no-daemon --console=plain
.\gradlew.bat :bizadm:test --offline --no-daemon --console=plain

.\gradlew.bat checkDeployEnv -PcpfModule=ACC -PcpfEnv=dev -PcpfResultDir=specs/evidence/20260708_05 --offline --no-daemon --console=plain
.\gradlew.bat checkDeployInventory -PcpfModule=ACC -PcpfEnv=dev -PcpfResultDir=specs/evidence/20260708_05 --offline --no-daemon --console=plain
.\gradlew.bat checkPackagedDependencies -PcpfModule=ACC -PcpfResultDir=specs/evidence/20260708_05 --offline --no-daemon --console=plain
.\gradlew.bat remoteDeployDryRun -PcpfModule=ACC -PcpfEnv=dev -PcpfDeployMode=dryRun -PcpfResultDir=specs/evidence/20260708_05 --offline --no-daemon --console=plain

.\gradlew.bat qualityGate --offline --no-daemon --console=plain
```

필수 로그:

```text
specs/evidence/20260708_05/xyz-test.log
specs/evidence/20260708_05/bat-test.log
specs/evidence/20260708_05/pfw-test.log
specs/evidence/20260708_05/cmn-test.log
specs/evidence/20260708_05/exs-test.log
specs/evidence/20260708_05/adm-test.log
specs/evidence/20260708_05/bizadm-test.log
specs/evidence/20260708_05/check-deploy-env.log
specs/evidence/20260708_05/check-deploy-inventory.log
specs/evidence/20260708_05/check-packaged-dependencies.log
specs/evidence/20260708_05/remote-deploy-dry-run.log
specs/evidence/20260708_05/quality-gate.log
```

추가 scan:

```text
sample coverage scan
catalog-only scan
actual sample implementation scan
sample package structure scan
archive/compression coverage scan
evidence path existence scan
local port duplicate scan
empty directory scan
garbage file scan
ownership scan
Spring Event scan
UTF-8/mojibake scan
```

실행하지 말 것:

```text
실제 remoteDeploy remote mode
실제 external WAS/JNDI runtime
실제 Kafka/MQ/Redis runtime
실제 SFTP/FTP/SCP/SSH runtime
실제 Vault/KMS/HSM runtime
실제 browser click
실제 MariaDB 신규 빈 DB full install
```

## 22. 문서 / 리포트 기록

수정 가능:

```text
CPF_STABILIZATION_REPORT.md
CPF_EVIDENCE_INDEX.md
CPF_GAP_MATRIX.md
README.md
specs/기능_구현_매트릭스.html
specs/sample-coverage-matrix.md
specs/evidence/20260708_05/*
```

수정 금지:

```text
CPF_FINAL_TARGET_REQUIREMENTS.md
CPF_REVIEW_PROGRESS_COMPLETION_GUIDE.md
CPF_NEW_REQUEST.md
```

기록 기준:

```text
장문 정본화 금지
개발자 가이드 정본화 금지
샘플 위치/목적/검증 수준만 최소 기록
sampleId별 실제 구현/부분 구현/미검증 상태 정확히 기록
catalog-only를 완료로 기록 금지
외부 runtime 미실행은 미검증 유지
evidence/log path 실제 존재 기준으로 기록
```

## 23. 완료 보고 필수 형식

완료 보고에는 아래를 반드시 포함한다.

```text
1. 실제 구현한 sample class 목록
2. 실제 구현한 test class 목록
3. 기능별 package 구조 정리 결과
4. catalog-only에서 실제 sample로 전환한 sampleId 목록
5. 여전히 catalog-only인 sampleId 목록과 사유
6. 완료/부분 구현/미구현/미검증 sampleId count
7. BAT EDU 실제 샘플 수
8. XYZ EDU 실제 샘플 수
9. CMN 실제 샘플 수
10. EXS 실제 샘플 수
11. PFW 실제 샘플 수
12. ADM/BIZADM 실제 샘플 수
13. PFW archive/compression 구현 결과
14. ZIP/GZIP/TAR 처리 결과
15. Kafka/MQ 샘플 source/contract/runtime 분리 결과
16. SFTP/FTP/SCP/SSH 샘플 source/contract/runtime 분리 결과
17. Service Call/Facade 샘플 구현 결과
18. transaction/idempotency/unknown/reconciliation 샘플 구현 결과
19. 보안/마스킹/감사/권한 샘플 구현 결과
20. PFW/CMN/EXS ownership 위반 여부
21. Spring Event 핵심 흐름 사용 여부
22. evidence/log 존재 정합성 결과
23. qualityGate 강화 결과
24. 가비지 파일 삭제 결과
25. 빈 폴더 삭제 결과
26. EDU 별도 module/deploy/env/port 제거 유지 결과
27. 실제 실행 명령
28. evidence 경로
29. 외부 runtime 미검증 항목
30. 실패/재확인 필요 항목과 사유
31. 다음 보강 후보
32. Git commit/push/branch 미수행 여부
```

## 24. 완료 불인정 기준

아래 중 하나라도 해당하면 완료로 기록하지 않는다.

```text
CoverageCatalog만 만들고 완료 처리
sampleId만 추가하고 실제 sample class 없음
sample class만 있고 test/evidence 없음
CoverageCatalogTest를 기능 테스트처럼 완료 처리
EDU 샘플이 catalog/controller 한 곳에 몰려 있음
기능별 edu package 구조가 없음
BAT EDU가 1~2개 샘플 수준
XYZ EDU가 controller 몇 개 수준
archive/compression 기능 없음
ZIP/GZIP 테스트 없음
zip-slip 방지 테스트 없음
Kafka/MQ/SFTP/SSH runtime을 실제 실행 없이 완료 처리
PFW/CMN/EXS ownership 위반
Spring Event로 핵심 거래 흐름 처리
없는 evidence/log 참조
qualityGate가 catalog-only를 통과시킴
EDU 별도 deploy/module/env/port 재등장
```

## 25. 이번 작업의 최종 목표

이번 작업의 최종 목표는 아래다.

```text
1. sample coverage matrix가 단순 목록이 아니라 실제 샘플 구현 상태를 정확히 보여준다.
2. BAT EDU는 실제 배치 개발자가 참고할 수 있는 기능별 package와 케이스별 샘플을 가진다.
3. XYZ EDU는 온라인 개발자가 참고할 수 있는 CRUD/query/transaction/service-call/security/file/broker/archive 기능별 샘플을 가진다.
4. PFW는 archive/compression capability와 ZIP/GZIP/zip-slip/checksum 테스트를 가진다.
5. Kafka/MQ/SFTP/SSH는 source/contract/plan 샘플과 runtime 미검증 상태가 명확히 분리된다.
6. CMN fixed-length와 EXS adapter 역할이 분리된다.
7. 샘플이 곧 source/contract/unit/fixture 수준 기능 검증 자산이 된다.
8. qualityGate가 거짓 완료, catalog-only 완료, package 몰아넣기, 없는 evidence 완료를 막는다.
```
