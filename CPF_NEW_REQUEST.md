# Codex 요청서 — 가비지 정리, 환경/Profile 정합성, Gradle 배포 표준, WAS 호환 설정, 자동생성/BAT 보강

## 0. Codex 공통 작업 지침

이번 요청은 실제 개발서버, 운영서버, 외부 WAS, Jenkins 실환경, real broker, real SFTP/FTP/SSH, Vault/KMS/HSM, MariaDB 신규 빈 DB, ADM browser runtime 환경을 Codex가 직접 준비해 검증하는 작업이 아니다.

Codex가 할 수 있는 범위와 할 수 없는 범위를 명확히 구분한다.

```text id="codex-scope"
Codex 완료 가능:
- source 구현
- yml/env/profile 정리
- Gradle task 구현
- bootJar packaging 검증
- PFW/CMN dependency 포함 검증
- config loading 검증
- datasource URL/JNDI mode 설정 구조 구현
- external WAS/JNDI command/config plan 생성
- remote deploy dry-run
- garbage file scan/delete
- source/unit/contract test
- report/evidence/matrix/gap 정합성 복구

Codex 완료 불가:
- 실제 외부 WAS 설치
- 실제 WAS datasource/JNDI resource 등록
- 실제 개발서버 원격 배포
- 실제 원격 stop/start/restart/rollback
- 실제 broker runtime
- 실제 SFTP/FTP/SSH runtime
- 실제 Vault/KMS/HSM runtime
- 실제 ADM browser click
- 실제 MariaDB 신규 빈 DB full install
```

기본 실행 표준은 아래로 둔다.

```text id="runtime-base"
CPF 기본 실행 표준:
- Spring Boot executable bootJar
- embedded Tomcat
- java -jar 실행
- module별 server.port
- module별 datasource URL/env 기반 설정
- PFW/CMN은 업무 서비스 bootJar 내부 dependency
```

외부 WAS/JNDI는 기본이 아니라 선택 호환 옵션이다.

```text id="runtime-optional"
선택 호환:
- 외부 WAS 배포
- WAS datasource/JNDI
- JNDI resource name
- WAR 배포 후보

단, 실제 WAS/JNDI runtime 검증은 환경이 없으면 미검증으로 남긴다.
```

반드시 지킬 기준:

```text id="global-rules"
- Git commit 금지
- Git push 금지
- branch 생성 금지
- CPF_FINAL_TARGET_REQUIREMENTS.md 수정 금지
- CPF_REVIEW_PROGRESS_COMPLETION_GUIDE.md 수정 금지
- CPF_NEW_REQUEST.md 수정 금지
- 실행하지 않은 검증 완료 기록 금지
- dry-run을 real remote deploy로 기록 금지
- source-only smoke를 runtime 완료로 기록 금지
- static UI smoke를 browser click 완료로 기록 금지
- external WAS/JNDI config validation을 실제 WAS runtime 완료로 기록 금지
- embedded/mock broker를 real broker 완료로 기록 금지
- placeholder script를 실제 구현 완료로 기록 금지
- stale evidence를 신규 완료 근거로 사용 금지
- 없는 evidence 경로를 완료 근거로 기록 금지
- secret/password/token/private key/cert 원문 기록 금지
```

상태값은 아래 6개만 사용한다.

```text id="status-values"
완료
부분 구현
미구현
미검증
실패
재확인 필요
```

실패/미검증 항목은 같은 방식으로 반복 요청하지 말고 원인과 대안을 보고한다.

## 1. 선행 검수 결과 반영

현재 검수 기준으로 아래 문제가 확인되었다. 이번 작업은 이 문제를 먼저 반영한다.

```text id="review-summary"
1. scripts/deploy 아래 ps1/sh 배포 파일은 존재하나 실제 원격 배포 엔진이 아니라 dry-run/placeholder 중심이다.
2. Jenkins-ready 배포 표준은 ps1/sh 직접 호출이 아니라 Gradle task 중심이어야 한다.
3. root build.gradle에는 ps1을 직접 호출하는 checkDeployDryRun류 구조가 있어 Gradle-native 표준으로 부족하다.
4. yml/profile/env 구조가 신규 표준과 구버전 파일이 혼재되어 있다.
5. deploy/env 파일에 빈 값이 남아 있다.
6. PFW/CMN yml에 실행 주체 설정이나 datasource 기본값으로 오해될 수 있는 설정이 남아 있다.
7. datasource는 URL mode를 기본으로 하되 외부 WAS/JNDI mode도 설정 구조는 지원해야 한다.
8. 실제 외부 WAS/JNDI runtime 검증은 Codex가 수행할 수 없으므로 미검증으로 남겨야 한다.
9. 주제영역 자동 생성 기능이 새 profile/env/deploy 표준을 따라야 한다.
10. BAT edu 패키지 누락, BAT Job 실행 단위 로그 파일 분리도 보강해야 한다.
```

## 2. 가비지 파일 / 불필요 파일 / stale 설정 전수 정리

이번 작업의 최우선 항목이다.

### 2.1 전수 목록 산출

아래 파일 목록을 evidence로 남긴다.

```powershell id="file-scan"
git ls-files > specs/evidence/20260708_03/git-files.txt
git ls-files "*application*.yml" "*application*.yaml" "*application*.properties" > specs/evidence/20260708_03/config-files.txt
git ls-files "deploy/env/*" "deploy/inventory/*" > specs/evidence/20260708_03/deploy-config-files.txt
git ls-files "scripts/deploy/*" > specs/evidence/20260708_03/deploy-script-files.txt
git ls-files "specs/evidence/**" > specs/evidence/20260708_03/evidence-files.txt
```

### 2.2 정리 대상

아래를 전수 점검한다.

```text id="cleanup-targets"
1. yml/properties/env
   - 구버전 application-dev.yml/local.yml/prod.yml/test.yml
   - 신규 application-<module>-*.yml와 중복되는 파일
   - 더 이상 spring.config.import에서 로드되지 않는 파일
   - spring.profiles.include 구버전 방식만 사용하는 파일
   - module prefix 규칙과 충돌하는 env key
   - 빈 값이 남은 env 파일
   - local/dev/stg/prod 표준과 맞지 않는 파일

2. scripts/deploy
   - Gradle task에서 호출되지 않는 ps1/sh
   - placeholder만 출력하는 remote-start/stop/restart/rollback
   - Jenkins 표준과 충돌하는 Windows-only ps1
   - Gradle task로 대체 가능한 중복 script
   - 실제 기능 없이 evidence만 만드는 script

3. source/test/template
   - dead source
   - 더 이상 import되지 않는 DTO/helper
   - 구버전 자동생성 template
   - BAT edu로 이동해야 하는 예제 코드
   - EXS/타 업무영역에 잘못 위치한 공통 기술 코드

4. evidence/report
   - 없는 evidence path
   - stale evidence를 신규 완료 근거로 쓰는 항목
   - SKIPPED인데 완료로 오인되는 항목
   - 이전 evidence와 신규 evidence가 중복된 항목
```

### 2.3 삭제 기준

확정 삭제 기준:

```text id="delete-rules"
- git grep으로 참조 없음
- Gradle build/test에서 참조 없음
- script에서 호출 없음
- README/report/matrix/evidence에서 참조 없음
- 자동생성 템플릿에서 참조 없음
- qualityGate/check script에서 참조 없음
- 같은 목적의 신규 표준 파일이 존재
```

삭제 금지:

```text id="delete-forbidden"
- CPF_FINAL_TARGET_REQUIREMENTS.md
- CPF_REVIEW_PROGRESS_COMPLETION_GUIDE.md
- CPF_NEW_REQUEST.md
- 사용자가 명시한 기준 파일
- 직접 확인되지 않은 파일
- 대용량 목표파일 원본
- 후속 작업 필요성이 있으나 판단 근거가 부족한 파일
```

애매한 파일은 삭제하지 말고 `재확인 필요`로 남긴다.

필수 evidence:

```text id="cleanup-evidence"
specs/evidence/20260708_03/garbage-file-scan.sanitized.json
specs/evidence/20260708_03/deleted-files.sanitized.json
specs/evidence/20260708_03/retained-review-required-files.sanitized.json
```

완료 기준:

```text id="cleanup-done"
- 확정 가비지 파일 삭제
- 삭제 보류 파일은 재확인 필요로 기록
- 삭제 후 build/test/qualityGate 통과
- 삭제로 인한 yml/profile/import/deploy/auto-generation 깨짐 없음
```

## 3. yml/properties/env 정리 및 profile 표준화

### 3.1 profile 표준

CPF profile은 아래 4개로 통일한다.

```text id="profiles"
local : 개발자 로컬 PC
dev   : 개발 서버 / 개발존
stg   : 스테이징 / 검증존
prod  : 운영존
```

`prd`는 표준으로 쓰지 않는다.

### 3.2 업무 서비스 표준 구조

대상 모듈:

```text id="modules"
ACC / MBR / EXS / ADM / BAT / BIZADM / EDU / XYZ
```

각 업무 서비스는 아래 구조로 통일한다.

```text id="module-yml-structure"
<module>/src/main/resources/application.yml
<module>/src/main/resources/application-<module>.yml
<module>/src/main/resources/application-<module>-local.yml
<module>/src/main/resources/application-<module>-dev.yml
<module>/src/main/resources/application-<module>-stg.yml
<module>/src/main/resources/application-<module>-prod.yml
```

기존 `application-local.yml`, `application-dev.yml`, `application-prod.yml`, `application-test.yml`는 신규 표준으로 이관 후 삭제 후보로 분류한다.

테스트 전용이 실제 필요하면 `application-<module>-test.yml`로 명확히 바꾸고 사유를 기록한다.

### 3.3 PFW/CMN 표준 구조

PFW/CMN은 실행 주체가 아니지만 zone별 공통 정책 파일은 둔다.

PFW:

```text id="pfw-yml"
pfw/src/main/resources/application-pfw.yml
pfw/src/main/resources/application-pfw-local.yml
pfw/src/main/resources/application-pfw-dev.yml
pfw/src/main/resources/application-pfw-stg.yml
pfw/src/main/resources/application-pfw-prod.yml
```

CMN:

```text id="cmn-yml"
cmn/src/main/resources/application-cmn.yml
cmn/src/main/resources/application-cmn-local.yml
cmn/src/main/resources/application-cmn-dev.yml
cmn/src/main/resources/application-cmn-stg.yml
cmn/src/main/resources/application-cmn-prod.yml
```

PFW/CMN 금지:

```text id="pfw-cmn-forbidden"
- spring.application.name 강제
- server.port 강제
- spring.profiles.active 강제
- 업무 datasource 강제
- module-id를 PFW/CMN으로 고정
- instance-id/was-id 강제
- 업무 logging path 강제
- local password성 값 기본 기록
```

PFW/CMN 허용:

```text id="pfw-cmn-allowed"
- cpf.pfw.* 공통 기술 정책
- cpf.cmn.* 공통 규칙/helper 정책
- retry/timeout/circuit/filetransfer/broker/security/cache/parser 기본값
```

### 3.4 import 방식

각 업무 서비스의 `application.yml`은 `spring.config.import`로 명시 로딩한다.

예시:

```yaml id="import-example"
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

`spring.profiles.include`와 `spring.config.import`가 혼재하면 `spring.config.import` 기준으로 정리한다.

### 3.5 env 값 기본 정책

env/yml 값을 비워두지 않는다.

허용:

```text id="env-defaults"
- local과 동일한 safe default
- localhost 기반 safe default
- __REPLACE_BY_ENV__ placeholder
- __REPLACE_BY_WAS_JNDI__ placeholder
- disabled/off/dry-run 같은 안전 기본값
```

금지:

```text id="env-empty-forbidden"
ACC_SERVER_PORT=
ACC_DATASOURCE_URL=
ACC_DATASOURCE_USERNAME=
ACC_DATASOURCE_PASSWORD=
```

secret 원문 금지:

```text id="secret-forbidden"
- password 실제값
- token 실제값
- private key
- certificate 원문
- client secret
- SSH key
```

## 4. 실행 방식과 datasource 표준

### 4.1 기본 실행 방식

CPF 기본 실행은 외부 WAS가 아니라 Spring Boot embedded Tomcat 기준이다.

```text id="embedded-runtime"
- bootJar 1개 생성
- java -jar <module>.jar 실행
- --spring.profiles.active=<env> 사용
- module별 server.port 사용
- PFW/CMN은 bootJar 내부 dependency
```

remoteDeploy dry-run은 아래 command plan을 생성해야 한다.

```text id="java-jar-plan"
build:
./gradlew :acc:bootJar

run:
java -jar acc/build/libs/acc-*.jar --spring.profiles.active=dev

health:
http://<host>:<ACC_SERVER_PORT>/actuator/health
```

### 4.2 datasource mode

datasource는 URL 직접 설정을 기본으로 한다.

```text id="ds-priority"
기본:
<MODULE>_DATASOURCE_MODE=url

선택 호환:
<MODULE>_DATASOURCE_MODE=jndi
```

URL mode:

```text id="url-mode"
<MODULE>_DATASOURCE_URL
<MODULE>_DATASOURCE_USERNAME
<MODULE>_DATASOURCE_PASSWORD
```

JNDI mode:

```text id="jndi-mode"
<MODULE>_DATASOURCE_JNDI_NAME
```

예시:

```text id="ds-example"
ACC_DATASOURCE_MODE=url
ACC_DATASOURCE_URL=jdbc:mariadb://localhost:3306/cpf_acc
ACC_DATASOURCE_USERNAME=cpf_acc
ACC_DATASOURCE_PASSWORD=__REPLACE_BY_ENV__

ACC_DATASOURCE_MODE=jndi
ACC_DATASOURCE_JNDI_NAME=java:comp/env/jdbc/cpfAccDataSource
```

### 4.3 WAS/JNDI 대응 기준

외부 WAS/JNDI는 구현/설정 구조만 지원한다.

```text id="was-jndi-rule"
- 외부 WAS/JNDI 설정 구조는 구현한다.
- 실제 WAS 설치/기동/리소스 등록은 요구하지 않는다.
- JNDI config validation까지만 완료 가능하다.
- 실제 WAS/JNDI runtime smoke는 환경이 없으면 미검증으로 기록한다.
- JNDI mode가 없어도 embedded bootJar URL mode가 기본 실행 기준이다.
```

검증 기준:

```text id="ds-validation"
- URL mode profile loading 검증
- JNDI mode placeholder/config validation 검증
- PFW/CMN이 datasource를 덮어쓰지 않는지 검증
- 빈 datasource env 값이 없는지 검증
- 실제 WAS/JNDI 연동은 미검증 유지
```

## 5. Gradle 중심 배포 구조

### 5.1 기준

Jenkins/운영/개발 표준 진입점은 Gradle로 통일한다.

```text id="gradle-standard"
Jenkins는 scripts/deploy/*.ps1 또는 *.sh를 직접 호출하지 않는다.
Jenkins 표준 호출은 ./gradlew remoteDeploy ... 형태로 둔다.
```

사용자 기준상 `apply from`, 별도 include, ps1/sh 중심 구조는 지양한다.
가능한 root `build.gradle` 내부에 명확한 `CPF Remote Deploy Tasks` section을 둔다.

### 5.2 필수 Gradle task

root `build.gradle`에 아래 task를 구현한다.

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
-PcpfModule=ACC|MBR|EXS|ADM|BAT|BIZADM|EDU|XYZ
-PcpfEnv=local|dev|stg|prod
-PcpfDeployMode=dryRun|remote
-PcpfRequireApproval=true|false
-PcpfResultDir=specs/evidence/20260708_03
```

Jenkins 호출 예시:

```bash id="jenkins-dryrun"
./gradlew remoteDeploy \
  -PcpfModule=ACC \
  -PcpfEnv=dev \
  -PcpfDeployMode=dryRun
```

운영 후보:

```bash id="jenkins-prod"
./gradlew remoteDeploy \
  -PcpfModule=ACC \
  -PcpfEnv=prod \
  -PcpfDeployMode=remote \
  -PcpfRequireApproval=true
```

### 5.3 Gradle task 책임

Gradle task 안에서 처리한다.

```text id="gradle-responsibility"
- module/env/mode parameter 검증
- env 파일 로딩/검증
- inventory 파일 로딩/검증
- :<module>:bootJar 실행
- bootJar 존재 확인
- bootJar 내부 PFW/CMN dependency 포함 검증
- artifact checksum 생성
- embedded Tomcat java -jar command plan 생성
- optional WAS/JNDI config plan 생성
- healthUrl template 검증
- dry-run evidence JSON 생성
- prod approval flag 검증
- remote mode 환경 부재 시 fail-fast
```

### 5.4 실제 remote mode 기준

실제 원격 서버가 없으면 remote mode를 완료로 기록하지 않는다.

현재 Codex가 할 수 있는 범위:

```text id="remote-possible"
- dry-run
- package 검증
- checksum 생성
- command plan 생성
- evidence 생성
```

할 수 없는 범위:

```text id="remote-impossible"
- 실제 원격 전송
- 실제 원격 stop/start
- 실제 원격 health check
- 실제 rollback
```

### 5.5 ps1/sh 정리

`scripts/deploy/*.ps1`, `scripts/deploy/*.sh`는 전수 점검한다.

정리 기준:

```text id="script-cleanup"
- Gradle task로 대체된 script는 삭제 후보
- placeholder만 출력하는 remote-start/stop/restart/rollback은 삭제 후보
- Gradle에서 호출되지 않는 script는 삭제 후보
- Windows-only ps1인데 Linux/Jenkins 대응이 없으면 삭제 또는 재확인 필요
- 실제 구현이 필요한 OS별 보조 script로 남길 경우 사유 기록
```

완료 기준:

```text id="gradle-deploy-done"
- Jenkins 표준 진입점은 gradlew
- ps1 직접 호출 task 제거 또는 재확인 필요 기록
- remoteDeployDryRun evidence 생성
- remoteDeploy는 실제 서버 없으면 미검증 유지
```

## 6. deploy/env 및 inventory 정리

### 6.1 deploy/env

대상:

```text id="env-files"
deploy/env/local-*.env
deploy/env/dev-*.env
deploy/env/stg-*.env
deploy/env/prod-*.env
```

정리 기준:

```text id="env-rules"
- 빈 값 금지
- module prefix 사용
- datasource mode 포함
- URL mode 기본
- JNDI mode 선택 호환
- log path safe default 포함
- secret은 placeholder 사용
- 실제 운영값 기록 금지
```

예시:

```text id="env-example"
SPRING_PROFILES_ACTIVE=dev
ACC_MODULE_ID=ACC
ACC_INSTANCE_ID=acc-dev-01
ACC_WAS_ID=accAP01
ACC_SERVER_PORT=8080
ACC_LOG_BASE_PATH=./logs/acc
ACC_DATASOURCE_MODE=url
ACC_DATASOURCE_URL=jdbc:mariadb://localhost:3306/cpf_acc
ACC_DATASOURCE_USERNAME=cpf_acc
ACC_DATASOURCE_PASSWORD=__REPLACE_BY_ENV__
ACC_DATASOURCE_JNDI_NAME=java:comp/env/jdbc/cpfAccDataSource
```

### 6.2 inventory

대상:

```text id="inventory-files"
deploy/inventory/local-services.json
deploy/inventory/dev-services.json
deploy/inventory/stg-services.json
deploy/inventory/prod-services.template.json
```

필수 필드:

```text id="inventory-fields"
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
runtimeMode
approvalRequired
rollbackEnabled
```

`runtimeMode` 후보:

```text id="runtime-mode"
embedded-bootjar
external-was
```

기본은 `embedded-bootjar`다.

## 7. 주제영역 자동 생성 기능 영향도 보정

이번 profile/env/deploy/Gradle 변경은 주제영역 자동 생성 기능에 반드시 반영한다.

### 7.1 점검 대상

```text id="generator-targets"
- create-domain script
- create-domain template
- generated sample module
- README/guide의 신규 모듈 생성 절차
- 자동 생성되는 yml/properties/env
- 자동 생성되는 build.gradle/settings.gradle 반영
- 자동 생성되는 deploy env/inventory
- 자동 생성되는 smoke/test/evidence skeleton
```

### 7.2 자동 생성 결과 표준

신규 모듈 생성 시 아래가 생성되어야 한다.

```text id="generated-files"
<module>/src/main/resources/application.yml
<module>/src/main/resources/application-<module>.yml
<module>/src/main/resources/application-<module>-local.yml
<module>/src/main/resources/application-<module>-dev.yml
<module>/src/main/resources/application-<module>-stg.yml
<module>/src/main/resources/application-<module>-prod.yml

deploy/env/local-<module>.env
deploy/env/dev-<module>.env
deploy/env/stg-<module>.env
deploy/env/prod-<module>.env

deploy/inventory/* 에 module 후보 반영
```

자동 생성되는 설정은 아래를 포함한다.

```text id="generated-config"
- PFW/CMN spring.config.import
- module prefix env
- datasource mode url/jndi
- embedded bootJar runtimeMode 기본값
- Gradle remoteDeploy에서 사용할 module/env 정보
```

### 7.3 자동 생성 금지 구조

```text id="generator-forbidden"
- spring.profiles.include 구버전 방식
- SERVER_PORT, DATASOURCE_URL 같은 공통 env만 사용
- module prefix 없는 환경변수 생성
- PFW/CMN import 누락
- EXS 내부 기술 클래스를 기본 dependency로 추가
- 주제영역 간 Controller/Repository/Mapper 직접 참조 생성
- deploy env/inventory 누락
- local/dev/stg/prod 중 일부만 생성
- 빈 yml/env 파일 생성
- secret/password/token 원문 포함
- ps1 직접 호출 기준 문서 생성
- external WAS/JNDI를 기본 실행 전제로 생성
```

### 7.4 검증

테스트용 신규 주제영역 1개를 생성해 아래를 검증한다.

```text id="generator-validation"
- 생성된 yml/env 구조
- PFW/CMN import
- module prefix 환경변수
- datasource url/jndi mode
- embedded bootJar runtimeMode
- Gradle deploy task와 연동 가능한 env/inventory
- ownership scan 통과
- build/test 영향 없음
```

검증용 임시 생성물은 테스트 후 삭제하고 evidence만 남긴다.

## 8. 최근 작업 영향도 점검

어제/그제 작업과 이번 작업이 충돌하지 않는지 확인한다.

대상:

```text id="impact-targets"
- Service Call Engine
- PFW broker/filetransfer/security/runtime/admin capability skeleton
- CMN fle/mqe warning 후보
- architecture ownership scan
- Spring Event scan
- qualityGate
- profile loading smoke
- deploy dry-run/package check
- report/matrix/evidence
- 주제영역 자동 생성 기능
- BAT center-cut
```

필수 확인:

```text id="impact-validation"
- profile 변경 후 service registry/endpoint/instance 설정이 깨지지 않는지
- module-id/instance-id/was-id가 업무 모듈 기준으로 잡히는지
- PFW/CMN yml이 실행 주체 설정을 덮지 않는지
- ownership scan false positive가 늘지 않는지
- Spring Event scan이 계속 qualityGate에 연결되는지
- deploy dry-run이 Gradle task 기준으로 동작하는지
- 기존 evidence가 stale 완료 근거로 남지 않는지
```

## 9. CMN migration warning 처리

Architecture ownership warning으로 남은 CMN 후보를 정리한다.

대상 후보:

```text id="cmn-warning"
CmnFileExchangeService
CmnMessageBridgeService
```

처리 기준:

```text id="cmn-warning-rules"
- 실제 기술 engine이면 PFW port/capability로 이동 또는 adapter 분리
- CMN에 남길 경우 프로젝트 공통 helper/rule/service임을 명확히 함
- broker/filetransfer 기술 ownership을 CMN이 갖는 형태는 금지
- migration이 크면 이번 작업에서는 재확인 필요로 남기고 후속 설계안을 기록
```

완료 기준:

```text id="cmn-warning-done"
- warning 0건 또는
- warning 유지 시 사유와 후속 이동 계획 명확화
```

## 10. BAT edu 패키지 보정

사용자 요구 기준: BAT 예제는 BAT 주제영역 안에 `edu` 패키지를 만들어 구성한다.

현재 `bat/src/main/java/cpf/bat/edu`가 없으면 생성한다.

기준:

```text id="bat-edu"
- BAT 운영/기본 구현 코드는 cpf.bat.centercut, cpf.bat.job, cpf.bat.operation 등 실무 패키지 유지
- 교육/예제 목적 코드는 cpf.bat.edu 하위로 이동 또는 wrapper/example 구성
- BAT edu는 별도 edu 모듈과 혼동되지 않게 package/README/test에서 명확히 표현
- 기존 centercut 샘플성 코드가 있다면 역할을 재분류
```

필수 검증:

```text id="bat-edu-validation"
- bat/src/main/java/cpf/bat/edu 존재
- bat/src/test/java/cpf/bat/edu 후보 존재
- build/test 통과
- sample/edu/operation 패키지 역할 구분
- README/report/matrix/evidence 최소 기록
```

## 11. BAT 배치 Job 실행 단위 로그 파일 분리

사용자 요구 기준: WAS/worker 인스턴스별 로그가 아니라 **배치 Job 실행 단위별 로그 파일**이 필요하다.

### 11.1 기준

```text id="bat-job-log"
- jobName별 로그 디렉터리 분리
- jobInstanceId 또는 jobInstanceKey 기록
- jobExecutionId 또는 runId별 로그 파일 생성
- transactionGlobalId 기록
- centerCutExecutionId가 있는 경우 centerCutExecutionId별 로그 디렉터리 생성
- restart/retry 실행은 기존 로그를 덮지 않고 execution별 로그로 분리
- 같은 job이 동시에 여러 건 실행되어도 로그 파일이 섞이지 않음
- worker/WAS instanceId는 파일 분리 기준이 아니라 로그 필드로 포함
- BAT_INSTANCE_ID / BAT_WAS_ID는 실행 위치 추적용으로 사용
- 로그 파일명/evidence에 secret/개인정보 원문을 남기지 않음
```

### 11.2 권장 경로

일반 batch job:

```text id="bat-job-path"
./logs/bat/jobs/<jobName>/<yyyyMMdd>/jobInstance-<jobInstanceId>/execution-<jobExecutionId>.log
```

center-cut:

```text id="centercut-log-path"
./logs/bat/centercut/<centerCutExecutionId>/parent.log
./logs/bat/centercut/<centerCutExecutionId>/summary.log
./logs/bat/centercut/<centerCutExecutionId>/items/item-<itemId>.log
```

### 11.3 구현 기준

이번 작업에서는 실제 대량 runtime 검증을 요구하지 않는다.

가능 범위:

```text id="bat-log-possible"
- 로그 경로 계산 component
- jobName/jobExecutionId/runId/transactionGlobalId 기반 file name policy
- unit test
- source/profile smoke
- evidence JSON
```

실제 runtime 실행이 없으면:

```text id="bat-log-no-runtime"
- job execution runtime log 분리 완료로 기록하지 않음
- 부분 구현 또는 미검증으로 기록
```

## 12. qualityGate 보강

qualityGate는 실제로 Codex가 실행 가능한 정적/소스/패키징/dry-run 검증 중심으로 구성한다.

필수 연결:

```text id="qualitygate-items"
- garbage file scan
- yml/profile consistency scan
- env empty value scan
- datasource mode scan
- Gradle deploy task scan
- ps1/sh direct-call scan
- create-domain template scan
- BAT edu package scan
- BAT job log path policy test
- ownership scan
- Spring Event scan
- service-call boundary scan
- profile loading smoke
- packaged dependency check
- remoteDeployDryRun through Gradle task
- report/matrix/evidence consistency
- feature evidence check
- UTF-8/mojibake check
```

금지:

```text id="qualitygate-forbidden"
- 실제 서버 없는 상태에서 real remote deploy를 qualityGate 필수로 걸지 말 것
- 실제 WAS 없는 상태에서 JNDI runtime을 qualityGate 필수로 걸지 말 것
- real broker 없는 상태에서 broker runtime을 qualityGate 필수로 걸지 말 것
- browser 없는 상태에서 browser click을 qualityGate 필수로 걸지 말 것
```

## 13. 필수 실행 명령

가능한 범위에서 아래 명령을 실행하고 evidence를 남긴다.

```powershell id="commands"
.\gradlew.bat :pfw:test --offline --no-daemon --console=plain
.\gradlew.bat :cmn:test --offline --no-daemon --console=plain
.\gradlew.bat :bat:test --offline --no-daemon --console=plain
.\gradlew.bat :adm:test --offline --no-daemon --console=plain --tests cpf.adm.opr.controller.AdmServiceRegistryControllerTest
.\gradlew.bat :pfw:test --offline --no-daemon --console=plain --tests cpf.pfw.common.servicecall.*
.\gradlew.bat :pfw:test --offline --no-daemon --console=plain --tests cpf.pfw.common.capability.*

powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-architecture-ownership.ps1 -ResultDir specs/evidence/20260708_03
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-spring-event-usage.ps1 -ResultDir specs/evidence/20260708_03
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-service-call-boundary.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-profile-loading.ps1 -ResultDir specs/evidence/20260708_03

.\gradlew.bat checkDeployEnv -PcpfModule=ACC -PcpfEnv=dev -PcpfResultDir=specs/evidence/20260708_03 --offline --no-daemon --console=plain
.\gradlew.bat checkDeployInventory -PcpfModule=ACC -PcpfEnv=dev -PcpfResultDir=specs/evidence/20260708_03 --offline --no-daemon --console=plain
.\gradlew.bat checkPackagedDependencies -PcpfModule=ACC -PcpfResultDir=specs/evidence/20260708_03 --offline --no-daemon --console=plain
.\gradlew.bat remoteDeployDryRun -PcpfModule=ACC -PcpfEnv=dev -PcpfDeployMode=dryRun -PcpfResultDir=specs/evidence/20260708_03 --offline --no-daemon --console=plain

powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-report-matrix-evidence-consistency.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-feature-evidence.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-html-docs.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-utf8.ps1 -CheckMojibake

.\gradlew.bat qualityGate --offline --no-daemon --console=plain
```

주의:

```text id="command-rules"
- remoteDeploy 실제 remote mode는 실행하지 않는다.
- 실제 서버 접속 정보가 없으면 remoteDeploy는 dryRun까지만 수행한다.
- 실제 WAS/JNDI runtime 검증은 수행하지 않는다.
- 실행하지 않은 검증은 미검증으로 기록한다.
```

## 14. report/evidence/matrix/gap 기록 기준

문서 정본화 금지. 최소 기록만 한다.

수정 대상:

```text id="docs-update"
CPF_STABILIZATION_REPORT.md
CPF_GAP_MATRIX.md
CPF_EVIDENCE_INDEX.md
specs/기능_구현_매트릭스.html
README.md
```

수정 금지:

```text id="docs-forbidden"
CPF_REVIEW_PROGRESS_COMPLETION_GUIDE.md
CPF_FINAL_TARGET_REQUIREMENTS.md
CPF_NEW_REQUEST.md
```

기록 기준:

```text id="docs-rule"
- 완료/부분 구현/미구현/미검증/실패/재확인 필요 상태값 일치
- 실제 존재하는 evidence 경로만 기록
- 삭제 파일/보류 파일 evidence 기록
- dry-run은 dry-run으로 기록
- ps1/sh placeholder 제거/보류 사유 기록
- remote deploy 실제 미검증 유지
- external WAS/JNDI 실제 runtime 미검증 유지
- datasource JNDI config validation과 runtime 검증을 분리
- BAT job log runtime 미실행 시 부분 구현/미검증 유지
```

## 15. 완료 불인정 기준

아래는 완료로 기록하지 않는다.

```text id="not-done"
- ps1/sh만 만들고 Jenkins-ready Gradle 배포 완료 주장
- Gradle task가 ps1 호출 wrapper일 뿐인데 Gradle 배포 완료 주장
- 실제 서버 없이 remote deploy 완료 주장
- 실제 WAS 없이 external WAS/JNDI runtime 완료 주장
- env 값이 빈 상태인데 profile/env 완료 주장
- JNDI mode 없이 WAS datasource 호환 완료 주장
- PFW/CMN이 datasource/module-id/server.port를 강제하는데 설정 표준 완료 주장
- 구버전 yml이 남아 있는데 가비지 정리 완료 주장
- 자동 생성 기능이 구버전 yml/env를 만들고 있는데 profile 표준 완료 주장
- BAT edu 패키지 없이 BAT 예제 보정 완료 주장
- BAT job execution별 로그 runtime 없이 runtime 완료 주장
- stale evidence를 신규 완료 근거로 사용
- CPF_REVIEW_PROGRESS_COMPLETION_GUIDE.md 수정
```

## 16. Codex 완료 보고 필수 형식

완료 보고는 아래 형식으로 작성한다.

```text id="report-format"
1. 실제 수정한 핵심 파일
2. 삭제한 파일 목록과 삭제 사유
3. 삭제 보류/재확인 필요 파일 목록과 사유
4. yml/properties/env 정리 결과
5. PFW/CMN 설정 강제 제거 결과
6. embedded bootJar 실행 표준 반영 결과
7. datasource URL/JNDI mode 반영 결과
8. external WAS/JNDI config validation 결과와 runtime 미검증 사유
9. deploy/env 빈 값 제거 및 placeholder 반영 결과
10. Gradle remote deploy task 구현 결과
11. ps1/sh 삭제/보류/재분류 결과
12. Jenkins 호출 예시
13. create-domain 자동 생성 영향도 보정 결과
14. CMN migration warning 처리 결과
15. BAT edu 패키지 보정 결과
16. BAT job execution log policy 구현/검증 결과
17. 기존 Service Call/PFW capability/profile/deploy/evidence 영향도 점검 결과
18. qualityGate 연결 결과
19. 실제 실행 명령
20. evidence 경로
21. 완료/부분 구현/미구현/미검증/실패/재확인 필요 분리
22. 대안 적용이 필요한 항목과 사유
23. Git commit/push/branch 미수행 여부
```

보고 시 금지:

```text id="report-forbidden"
- 실행하지 않은 검증을 완료로 쓰지 말 것
- 같은 실패를 같은 방식으로 재요청하지 말 것
- 실제 서버 없는 remote deploy를 완료로 쓰지 말 것
- 실제 WAS 없는 JNDI runtime을 완료로 쓰지 말 것
- dry-run을 real deploy로 쓰지 말 것
- placeholder script를 실제 구현으로 쓰지 말 것
- 삭제하지 않은 가비지 후보를 숨기지 말 것
- 미정리 yml/env를 완료로 포장하지 말 것
```
