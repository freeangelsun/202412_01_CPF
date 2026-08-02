# CPF Tools 매뉴얼 — 생성·빌드·DB·실행·검증 도구 사용 절차

> **주 독자**: CPF를 처음 사용하는 업무 개발자, 빌드·배포 담당자, DBA, 플랫폼 운영자, 검수자
> **완료 결과**: 목적에 맞는 `cpf-tools/` 도구를 선택하고 사전 점검·Dry Run·실행·결과 확인·재실행·되돌리기를 수행한다.

## 문서 기준

- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- 기준 Commit: `3b600702502e53877e30cbac594987b371e2186b` (`20260802_08`)
- 활성 개발 요구: `cpf-docs/work/current/CPF_QA38_FINAL_DEVELOPMENT_REQUIREMENTS.md`
- 실제 Source·SQL·API·Config·Frontend·Script·Test가 설명보다 우선한다.
- 실행하지 않은 Build·DB·Runtime·Browser·다중 인스턴스·장애 시험은 `미검증`이다.


## 1. CPF Tools의 역할

`cpf-tools/`는 제품 Source와 사용자 작업을 연결하는 도구군이다. 하나의 실행 파일이나 독립 제품이 아니며, 기능별 Owner와 사용 조건이 다르다.

기준 Commit에서 확인되는 대표 영역:

```text
cpf-tools/build/          Gradle Plugin·Platform BOM
cpf-tools/generator/      업무 영역 생성과 계약 생성
cpf-tools/runtime/        Local Web·Batch Runtime
cpf-tools/scripts/        설치·기동·상태·검증 Script
cpf-tools/db/             Vendor Pack·Migration·검증
cpf-tools/verification/   품질 Gate·정합성 검사
cpf-tools/environment/    개발·시험 환경
```

실제 하위 경로는 최신 Repository에서 확인한다. 존재하지 않는 도구 이름이나 매개변수를 문서가 먼저 만들지 않는다.

## 2. 처음 사용하는 사람의 기본 순서

1. Repository 절대경로를 지정하고 현재 Branch·Commit·변경 상태를 확인한다.
2. 수행 목적을 생성·빌드·DB·실행·검증·환경 중 하나로 분류한다.
3. 해당 Script의 `param`·Help·예제와 변경 대상을 읽는다.
4. Dry Run 또는 조회 명령을 먼저 실행한다.
5. 생성·변경 Manifest와 충돌을 확인한다.
6. 적용 명령을 실행한다.
7. 종료 코드·생성 파일·Build·DB·Health를 확인한다.
8. 오류가 나면 최초 실패 단계와 보호 대상을 기록한다.
9. 동일 입력 재실행의 멱등성을 확인한다.
10. 되돌리기·정리 범위를 정확한 파일과 데이터로 제한한다.

## 3. 공통 사전 점검

어느 폴더에서 실행해도 되도록 Repository 절대경로를 사용한다.

```powershell
$repo='C:\dev\\projects\\jck\\202412_01_CPF'
if(-not(Test-Path -LiteralPath $repo -PathType Container)){throw "Repository가 없습니다: $repo"}
git -C $repo remote -v
git -C $repo branch --show-current
git -C $repo rev-parse HEAD
git -C $repo rev-parse origin/master
git -C $repo status --short
git -C $repo diff --name-status
git -C $repo diff --stat
git -C $repo ls-files --others --exclude-standard
java -version
& (Join-Path $repo 'gradlew.bat') --version
pwsh --version
```

판정:

- 기존 변경은 보호한다.
- 사용자 승인 없이 Reset·Restore·Clean·Stash를 수행하지 않는다.
- Script가 다른 작업자의 파일을 삭제하거나 전체 미추적 파일을 정리하면 실행하지 않는다.
- 기준 Commit과 실제 실행 Commit을 기록한다.

## 4. 도구 선택 지도

| 하려는 일 | 시작 위치 | 먼저 확인할 문서 |
|---|---|---|
| 신규 업무 영역 생성 | `cpf-tools/generator/` | 01 CPF 개발자 매뉴얼 |
| Starter 선택·검증 | Build·Generator·Consumer | 90 CPF Starters 매뉴얼 |
| Build Plugin·BOM 사용 | `cpf-tools/build/` | 이 문서의 Build 장 |
| DB 신규 설치·Migration | `cpf-tools/db/`, `cpf-tools/scripts/` | 05 CPF 플랫폼 운영 매뉴얼 |
| Local Web·Batch 실행 | `cpf-tools/runtime/`, `cpf-tools/scripts/` | 이 문서의 Local Runtime 장 |
| 품질 Gate·정합성 검사 | `cpf-tools/verification/` | 기술 표준서 |
| Docker 개발·시험 환경 | `cpf-tools/environment/docker-development-test/` | `cpf-docs/environment/docker/` |

## 5. 업무 영역 생성 도구

기준 Script:

```text
cpf-tools/generator/create-domain.ps1
```

### 5.1 Dry Run

```powershell
$repo='C:\dev\\projects\\jck\\202412_01_CPF'
pwsh -NoProfile -ExecutionPolicy Bypass -File (Join-Path $repo 'cpf-tools\generator\create-domain.ps1') `
  -DomainName payment `
  -SystemCode PAY `
  -DatabaseVendor postgresql `
  -DryRun
```

Dry Run에서 확인:

- Project·Package·Port·DB 식별자
- 생성·수정 예정 경로
- 예약어·중복·경로 충돌
- Database Vendor
- Capability 입력
- 사용자 수정 영역과 Framework 관리 영역

Dry Run이 실제 파일을 변경하면 결함으로 분류한다.

### 5.2 Apply 전 결정값

| 항목 | 결정 기준 |
|---|---|
| DomainName | 영문 소문자·숫자 규칙 |
| SystemCode | 정확한 3자리 식별자 |
| PackageName | 조직 Namespace와 Java 규칙 |
| Port | Local 충돌·배포 할당표 |
| DatabaseVendor | MariaDB·PostgreSQL·Oracle |
| Schema·Table Prefix | DB 표준과 충돌 확인 |
| DependencyModel | Root Project 또는 게시 Artifact |
| Capabilities | 실제 구현된 선택 기능만 |
| ProductionProfile | 운영 가짜 Adapter 차단 |

### 5.3 생성 후 확인

```powershell
$repo='C:\dev\\projects\\jck\\202412_01_CPF'
& (Join-Path $repo 'gradlew.bat') projects
& (Join-Path $repo 'gradlew.bat') :cpf-payment:clean :cpf-payment:test :cpf-payment:assemble
```

실제 Project 이름은 생성 결과를 따른다.

확인:

- `settings.gradle` 또는 독립 Build 연결
- Build Dependency와 Starter 선택
- Source·Test·Config·Migration
- 3개 Vendor 의미 일치
- OpenAPI·JavaDoc
- Runtime Agent·배포 Descriptor
- 생성 Manifest·Version Lock
- 사용자 영역 보존

기준 Commit의 Capability Profile·`resolvedStarters`·Version Lock은 구현 상태를 재확인해야 한다.

### 5.4 중단·재실행

- 생성 Manifest에 포함된 경로만 영향 범위로 사용한다.
- 생성 전 존재한 파일을 삭제하지 않는다.
- 같은 입력 재실행 시 중복 Source·SQL·설정이 생기지 않아야 한다.
- 다른 입력과 충돌하면 Fail-closed한다.
- 광범위한 Wildcard 삭제를 사용하지 않는다.

## 6. Build Plugin과 Platform BOM

기준 Owner:

```text
cpf-tools/build/gradle-plugin/
cpf-tools/build/platform-bom/
```

`settings.gradle`은 Included Build로 Plugin과 BOM을 연결한다.

### 6.1 Artifact Mode

지원 Mode:

```text
LOCAL_DEV
REMOTE
OFFLINE
```

환경에 따라 다음 값을 확인한다.

- `CPF_ARTIFACT_MODE`
- Remote Repository URL·User·Password
- Local Repository 경로
- Offline Repository 경로

Secret을 Command Line이나 Log에 출력하지 않는다.

### 6.2 Build 실행

```powershell
$repo='C:\dev\\projects\\jck\\202412_01_CPF'
& (Join-Path $repo 'gradlew.bat') projects
& (Join-Path $repo 'gradlew.bat') clean test assemble
```

긴 전체 Build 전에 대상 Project의 Compile·Test를 먼저 실행한다. 성공한 낮은 비용 Gate를 같은 Commit에서 이유 없이 반복하지 않는다.

### 6.3 BOM 확인

- BOM은 Version Alignment만 수행한다.
- Starter·Runtime Dependency를 자동 활성화하지 않는다.
- 게시 POM과 Consumer Dependency Graph가 일치해야 한다.

## 7. Local Runtime

기준 논리 Project와 물리 경로:

```text
:cpf-local-runtime       → cpf-tools/runtime/cpf-local-runtime
:cpf-local-batch-runtime → cpf-tools/runtime/cpf-local-batch-runtime
```

대표 Script:

```powershell
$repo='C:\dev\\projects\\jck\\202412_01_CPF'
pwsh -NoProfile -ExecutionPolicy Bypass -File (Join-Path $repo 'cpf-tools\scripts\start-cpf-local.ps1')
pwsh -NoProfile -ExecutionPolicy Bypass -File (Join-Path $repo 'cpf-tools\scripts\status-cpf-local.ps1')
pwsh -NoProfile -ExecutionPolicy Bypass -File (Join-Path $repo 'cpf-tools\scripts\stop-cpf-local.ps1')
```

### 7.1 기동 전

- 필요한 DB·Message Broker·Secret Provider 확인
- Port 충돌 확인
- Profile과 업무 Module 확인
- 운영 환경에서 Local Fake Adapter 차단 확인

### 7.2 정상 결과

- 대상 Process만 실행
- Health·Readiness 결과 확인
- Process Registry와 PID 일치
- Log에 Secret 없음
- 종료 후 잔여 Process와 Lock File 없음

### 7.3 실패 처리

1. 최초 실패 Process와 Log를 확인한다.
2. 이미 실행 중인 Process를 중복 기동하지 않는다.
3. `stop-cpf-local.ps1`의 대상 범위를 확인한다.
4. 잔여 PID가 실제 Process와 일치하는지 확인한다.
5. 원인 제거 후 대상만 재기동한다.

## 8. DB 도구

DB 절차는 [05 CPF 플랫폼 운영 매뉴얼](05_CPF_플랫폼운영매뉴얼.md)과 [데이터베이스 표준서](../deliverables/데이터베이스표준서.md)를 함께 사용한다.

기본 원칙:

1. Vendor별 Empty 상태에서 시작한다.
2. Canonical Metadata·Generator Query를 먼저 수정한다.
3. 생성된 Vendor Pack을 비교한다.
4. Install·Verify·Upgrade·Rollback·Reapply를 실행한다.
5. Object Count·Checksum·권한·Seed를 확인한다.
6. 다른 사용자의 DB·Schema·Volume을 초기화하지 않는다.

대표 초기화 Script가 존재할 때 실제 매개변수를 먼저 확인한다.

```powershell
Get-Help .\cpf-tools\scripts\initialize-cpf-database.ps1 -Detailed
```

문서의 예시보다 Script의 실제 `param` 계약을 우선한다.

## 9. 검증 도구

`cpf-tools/verification/`의 Gate는 Source 존재 수를 세는 데 그치지 않고 다음을 확인해야 한다.

- Module Ownership과 의존 방향
- 실제 Consumer
- Public API·SPI·Internal
- 3개 Vendor SQL 의미
- Config·Property·Profile
- Frontend Route·Permission·Generated Client
- Test·Scenario·Evidence exact SHA
- Secret·PII 비노출
- Root Hygiene·중복·Stale File

실행 결과에는 다음을 기록한다.

```text
기준 Commit
명령
환경
시작·종료 시각
종료 코드
PASS·FAIL Count
최초 실패 위치
Sanitized Evidence
미검증 범위
```

## 10. Docker 개발·시험 환경

문서 정본:

```text
cpf-docs/environment/docker/
```

환경 Source:

```text
cpf-tools/environment/docker-development-test/
```

목표는 CPF의 현재·신규 모듈을 실제로 설치하고 정상·오류·부분 실패·재시작·대사까지 시험하는 것이다. Container가 `Up`인 것만으로 제품 기능을 완료 처리하지 않는다.

현재 문서화 범위:

- Oracle·PostgreSQL·MariaDB
- Redis
- 현재 Message Broker와 QA38 추가 Provider Fixture
- WireMock·SFTP·Vault·Keycloak
- Toxiproxy·OpenTelemetry Collector
- Java·Gradle·Node·Browser·보안·공급망 Toolchain

신규 모듈이 개발되면 다음을 함께 추가한다.

1. Image·Version·License
2. Compose Service·Network·Volume·Secret
3. 초기화 Fixture
4. Product Adapter·실제 Consumer
5. 정상·오류·부분 실패·Process Kill Scenario
6. 검증 Script와 정상 결과
7. 안전한 중지·데이터 보존·Rollback
8. 문서와 Evidence

## 11. 도구 실패 보고 형식

```text
Tool ID·Script
기준 Commit
입력·Profile·환경
보호 대상
Expected
Actual
최초 실패 단계
종료 코드
생성·변경된 파일
재실행 가능 여부
정리·Rollback 범위
Sanitized Log·Evidence
```

## 12. 개발 요청으로 전달할 조건

다음은 매뉴얼로 우회하지 않고 개발 요청으로 전달한다.

- Help·`param`·실제 동작 불일치
- Dry Run이 파일을 변경
- Manifest 없이 광범위하게 생성·삭제
- 일부 실패 후 재실행이 중복 생성
- 오류 단계가 표시되지 않음
- Secret이 Process 인자·Log·Evidence에 노출
- 지원 Vendor·Starter·Provider 목록 Drift
- Generated Source·Build·DB·OpenAPI 불일치
- 검증 Gate가 실제 Consumer·Runtime 없이 PASS
- Docker Fixture와 Product 기능을 혼동

## 13. EDU 사용과 검증

EDU는 도구·제품 기능을 처음 배우는 실행 예제다. 문서 목록만으로 개발 완료를 판정하지 않는다.

확인 순서:

1. EDU ID와 Source Handler가 존재한다.
2. Resource Contract와 필수 입력이 존재한다.
3. 실제 Consumer·DB·Broker·File·외부 연계가 연결된다.
4. Test가 정상·오류·경계를 Assertion한다.
5. 실행 API 또는 Script가 실제 결과를 만든다.
6. ADM·Log·Metric·Trace·Audit에서 같은 식별자를 확인한다.
7. Process Kill·응답 유실·부분 실패 후 정상화를 확인한다.

기준 Commit에서 일부 EDU Handler와 공통 실행 계약은 확인됐지만 전체 EDU의 Runtime 실행은 `미검증`이다.

## 14. 작업 종료 점검

```powershell
$repo='C:\dev\\projects\\jck\\202412_01_CPF'
git -C $repo status --short
git -C $repo diff --name-status
git -C $repo diff --stat
git -C $repo diff --check
git -C $repo ls-files --others --exclude-standard
```

추가 확인:

- 생성 Manifest와 실제 파일 일치
- 임시 Script·Log·Build·중복 ZIP 없음
- Secret·Token·Password 없음
- 다른 작업자의 변경 보존
- 정확한 대상만 정리

## 15. 완료 점검표

- [ ] 목적에 맞는 Tool Owner를 선택했다.
- [ ] Help·Parameter·선행 조건을 확인했다.
- [ ] 조회·Dry Run을 먼저 수행했다.
- [ ] 변경 Manifest와 보호 대상을 확인했다.
- [ ] 정상 결과와 실패 결과를 구분했다.
- [ ] 동일 입력 재실행이 멱등하다.
- [ ] Secret과 조직 데이터가 노출되지 않는다.
- [ ] Build·DB·Runtime·문서가 일치한다.
- [ ] 미실행 Runtime은 `미검증`으로 기록했다.
- [ ] 정리·Rollback이 정확한 대상으로 제한된다.

## 16. Tool 사용성 검수 기준

처음 사용하는 사람이 Source를 읽지 않고도 다음을 확인할 수 있어야 한다.

| 항목 | 필수 내용 |
|---|---|
| 목적 | 어떤 결과를 만드는 도구인지 |
| 대상 역할 | 개발자·DBA·운영자·검수자 |
| 실행 위치 | 어느 폴더에서도 가능한 절대경로 예 |
| 선행 조건 | Java·PowerShell·Docker·DB·권한 |
| Parameter | 이름·Type·Default·필수·허용값 |
| Dry Run | 변경 없는 사전 검토 방법 |
| 변경 범위 | 생성·수정·삭제 예정 파일·DB Object |
| 정상 결과 | Exit Code·파일·Build·DB·Health |
| 오류 | 최초 실패 단계·로그·실패 분류 |
| 재실행 | 멱등성·충돌·중단 이후 행동 |
| Rollback | 정확한 파일·Manifest·DB Recovery |
| 보호 대상 | 다른 작업자의 변경·전체 미추적 파일 |

Help와 실제 Parameter가 다르면 문서 문제가 아니라 Tool 결함으로도 검토한다.

## 17. 도구별 안전한 실행 Wrapper

명령을 현재 폴더에 의존시키지 않는다.

```powershell
$repo='C:\dev\projects\jck\202412_01_CPF'
$tool=Join-Path $repo 'cpf-tools\generator\create-domain.ps1'
if(-not(Test-Path -LiteralPath $tool -PathType Leaf)){throw "도구가 없습니다: $tool"}
Get-Help $tool -Full
```

실행 전에는 `$repo`, Script 존재, Git 상태를 확인하고, 실행 후에는 Exit Code를 실행 직후 저장한다.

```powershell
pwsh -NoProfile -ExecutionPolicy Bypass -File $tool -DomainName payment -SystemCode PAY -DatabaseVendor postgresql -DryRun
$exit=$LASTEXITCODE
if($exit -ne 0){throw "도구 실행 실패: exit=$exit"}
```

## 18. Tool Inventory와 Owner 작업 요청

각 Tool은 다음 원장으로 관리해야 한다.

```text
Tool ID / Script Path / Owner
Purpose / Consumer / Parameter
Dry Run / Manifest / Idempotency
Exit Code / Log / Evidence
Rollback / Protected Paths
Test / Last Verified Commit
```

다음은 개발 검토 요청이다.

| ID | 조건 | 판정 |
|---|---|---|
| `TOOLS-HELP-001` | Script Parameter와 Help·문서 불일치 | 재확인 필요 |
| `TOOLS-MANIFEST-001` | 생성·수정 경로 Manifest 없음 | 재확인 필요 |
| `TOOLS-IDEMP-001` | 같은 입력 재실행 시 중복 생성 | 실패 시 개발 요청 |
| `TOOLS-ROLLBACK-001` | 정확한 Rollback·Recovery 없음 | 부분 구현 |
| `TOOLS-PATH-001` | 현재 폴더에 의존하는 명령 | 문서·Script 보완 필요 |
| `TOOLS-DELETE-001` | 광역 삭제·전체 미추적 정리 | 실행 금지·개발 요청 |
