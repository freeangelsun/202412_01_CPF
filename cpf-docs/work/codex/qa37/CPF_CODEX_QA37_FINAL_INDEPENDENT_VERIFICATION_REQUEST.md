# CPF QA37 Codex 독립 검수·보완 개발·완료 요청서
## Docker 개발·테스트 환경 통합 / 저비용 단일 실행판

## 1. 목적

최신 `master`의 실제 Source, SQL, API, Test, Config, Frontend, Script,
Migration, Generator, Matrix, Evidence를 기준으로 QA37 범위를 독립 검수한다.

검수에서 실제 Source Defect가 발견되면 영향 범위 안에서 보완 개발하고,
관련 Test와 Gate를 작성·수정한 뒤 최소 단위와 최종 통합 단위로 재검증한다.

문서의 `완료`, `Full Completion`, `Codex Ready` 표시는 증거로 사용하지 않는다.
실제 실행 결과와 exact SHA가 일치할 때만 완료로 판정한다.

## 2. 기준 Repository와 Git 제한

- Repository: `https://github.com/freeangelsun/202412_01_CPF`
- Branch: `master`
- 이 요청서 작성 시 확인한 원격 SHA:
  `866b2ff8bbc2a7aaecf91a617b58d79e9a1308a2`
- 실제 검수 기준 SHA: 시작 시 확인한 `HEAD == origin/master`의 exact SHA
- 사용자 Local Repository:
  `C:\dev\projects\jck\202412_01_CPF`
- Docker Runtime:
  `C:\dev\Docker\CPF`
- Secret:
  `C:\dev\Docker\Secrets`

사용자의 명시적 승인 없이 다음을 수행하지 않는다.

```text
Commit
Push
Branch
Tag
PR
Reset
Restore
Stash
Clean
추적 파일 삭제
Docker Image·Container·Runner·Volume·Compose·Script·Secret 삭제
```

다음 명령은 금지한다.

```text
git add -A
git clean
git reset --hard
docker system prune
docker image prune
docker volume prune
Docker Desktop 초기화
전체 설치 Script 재실행
```

## 3. 보호 대상

아래 경로는 다른 작업자의 정식 산출물일 수 있으므로 임의 수정·삭제하지 않는다.

```text
README.md
cpf-docs/guides/**
cpf-docs/deliverables/**
cpf-docs/work/current/CPF_CUSTOMER_MANUAL_EDU_IMPLEMENTATION_REQUIREMENTS.md
```

그 밖의 Git 추적 파일도 Owner, Consumer, 참조, 다른 작업자의 사용 여부를
확인하지 않고 삭제하거나 복구하지 않는다.

삭제가 필요하다고 판단하면 실제 삭제 대신 다음만 작성한다.

```text
Delete Candidate
Root 상대경로
삭제 근거
Owner
Consumer
참조 위치
회귀 위험
```

기존 QA37 ZIP과 검증되지 않은 Finalizer는 재사용하지 않는다.

## 4. 최초 읽기 순서

### 4.1 Docker 환경 문서

Docker를 사용하기 전에 반드시 아래 순서로 읽는다.

1. `cpf-docs/guides/docker/README.md`
2. `cpf-docs/guides/docker/CPF_도커_개발테스트환경_안내.md`
3. `cpf-docs/guides/docker/CPF_도커_연동및사용가이드.md`
4. `cpf-docs/architecture/CPF_도커_개발테스트환경_구성명세.md`

오류 또는 초기화가 필요할 때만 읽는다.

5. `cpf-docs/guides/docker/CPF_도커_문제해결및초기화가이드.md`

다른 PC에 새로 설치할 때만 읽는다.

6. `cpf-docs/guides/docker/CPF_도커_개발테스트환경_전체구축가이드.md`

현재 PC는 이미 환경이 설치되어 있으므로 6번 문서의 전체 설치 절차와
설치 Script를 다시 실행하지 않는다.

### 4.2 CPF 정본과 QA37 최소 읽기 세트

Docker 문서를 읽은 다음 아래 파일만 우선 읽는다.

1. `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
2. `cpf-docs/work/current/CPF_CURRENT_WORK_REQUEST.md`
3. `cpf-docs/work/current/CPF_20260801_QA37_SOURCE_READINESS_REPORT.md`
4. `cpf-docs/work/current/CPF_20260801_QA37_VERIFICATION_READINESS_REPORT.md`
5. `cpf-docs/work/codex/qa37/REVIEW_INDEX.md`
6. `cpf-docs/work/codex/qa37/OPEN_ISSUES.md`
7. `cpf-docs/quality/CPF_20260801_QA37_REQUIREMENT_RESULT_MATRIX.csv`
8. `cpf-docs/quality/CPF_20260801_QA37_MANUAL_EDU_135_COVERAGE_MATRIX.csv`
9. 이 요청서

DB, Generator, Runtime, Evidence 상세 문서는 해당 Stage에 진입할 때만 읽는다.
처음부터 Repository 전체를 자유 탐색하지 않는다.

## 5. Docker 환경 인수 조건

사용자가 전달한 준비 상태는 다음과 같다.

```text
필수 Image 13/13
기존 Runner Image 3/3 보존
Container 7/7 Created/Stopped
Running Container 0
Volume 5/5
CPF 업무 Schema·Data·Seed 없음
```

준비된 Service:

```text
MariaDB
PostgreSQL
Oracle
Redis
Kafka
Toxiproxy
OpenTelemetry Collector
```

준비된 Tool:

```text
Java 25
Node.js 22 및 npm
PowerShell 7
Python 3
Git
Playwright Chromium·Firefox·WebKit
MariaDB Client
PostgreSQL psql
Oracle SQL*Plus
Docker CLI·Compose
Trivy
OSS Review Toolkit
curl
jq
openssl
zip
unzip
```

이 상태를 성공으로 가정하지 말고 읽기 전용 Preflight에서 실제 상태를 확인한다.
환경이 다르면 자동 재설치·삭제·초기화하지 않고 차이를 기록한다.

모든 CPF Container는 `restart=no`를 유지한다.
PC 또는 Docker Desktop 재실행만으로 자동 기동되도록 변경하지 않는다.

## 6. Codex 크레딧 절감 원칙

1. 최초 읽기 세트와 Change Manifest부터 읽고 결함 근거가 있을 때만 탐색 범위를 확장한다.
2. 동일 Gate, 전체 Build, `npm ci`, DB Lifecycle, Browser Suite를 이유 없이 반복하지 않는다.
3. 각 대형 Stage는 원칙적으로 한 번 실행한다.
4. Stage 실패 시 후속 대형 Stage를 시작하지 않는다.
5. 로그에서 가장 먼저 발생한 공통 Root Cause를 해결한다.
6. 같은 원인으로 여러 파일이 수정되면 파일별 전체 검증을 반복하지 않는다.
7. 영향 Module·Vendor·기능군 Test만 먼저 실행한다.
8. 관련 수정이 모두 끝난 뒤 상위 통합 Lifecycle을 마지막에 한 번만 재실행한다.
9. exact SHA, 명령, 환경, Exit Code, Artifact Hash가 모두 같은 기존 Evidence만 재사용할 수 있다.
10. SHA 또는 환경이 다르면 과거 Evidence를 현재 성공으로 승계하지 않는다.
11. Source가 안정되기 전에 Trivy, ORT, 전체 Browser Suite를 실행하지 않는다.
12. 환경이 없는 검증을 위해 제품 Source를 우회 수정하지 않는다.
13. Docker Service는 해당 Stage에 필요한 것만 시작한다.
14. 공식 DB 3종을 동시에 실행하지 않고 Vendor별로 한 종류씩 검증한다.
15. 정상 경로가 성공한 뒤에만 Toxiproxy 장애 검증을 실행한다.

## 7. Stage 0 — Read-only 기준선과 환경 Snapshot

### 7.1 Git 기준선

```powershell
Set-Location "C:\dev\projects\jck\202412_01_CPF"
git fetch origin master
$head=(git rev-parse HEAD).Trim()
$remote=(git rev-parse origin/master).Trim()
if($head -ne $remote){ throw "HEAD/origin mismatch: $head / $remote" }
git -c core.quotepath=false status --short --branch
git -c core.quotepath=false diff --name-status
git -c core.quotepath=false diff --cached --name-status
git stash list
```

Working Tree가 깨끗하지 않으면 임의 복구, 삭제, Stash, Reset을 하지 않는다.
사용자 변경, 다른 작업자 산출물, 검수 중 생성물을 구분할 수 없으면 중단 보고한다.

### 7.2 Docker 상태와 초기 Running 목록

```powershell
$dockerRoot="C:\dev\Docker\CPF"
$repoRoot="C:\dev\projects\jck\202412_01_CPF"

pwsh -NoProfile -File "$dockerRoot\cpf-env.ps1" -Action status
pwsh -NoProfile -File "$dockerRoot\cpf-tooling.ps1" -Action status

$initialRunning=@(
  docker ps --filter "name=cpf-" --format "{{.Names}}" |
  Where-Object { $_ -and $_.Trim() } |
  ForEach-Object { $_.Trim() }
)

docker ps -a --filter "name=cpf-" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
docker volume ls --filter "name=cpf-"
docker network ls --filter "name=cpf"
docker inspect cpf-mariadb cpf-postgresql cpf-oracle cpf-redis cpf-kafka cpf-toxiproxy cpf-otel-collector `
  --format "{{.Name}} restart={{.HostConfig.RestartPolicy.Name}} status={{.State.Status}}"
```

다음 조건을 확인한다.

```text
필수 Container 7개 존재
각 Container Restart Policy = no
초기 Running 목록 기록
Volume 5개 존재
cpf_default Network 존재
Secret 원문 출력 금지
```

환경 차이는 Evidence에 기록한다.
Image, Volume, Secret, Script를 삭제하거나 다시 만들지 않는다.

## 8. Stage 1 — 저비용 Source·Contract Gate

```powershell
Set-Location "C:\dev\projects\jck\202412_01_CPF"
git diff --check
python .\cpf-tools\scripts\verify-cpf-qa37-source-closure.py --root .
```

함께 확인할 저비용 계약:

```text
Repository Hygiene
Secret Pattern
Ownership·Dependency Boundary
Public API·SPI·Internal 경계
Route·OpenAPI Contract
JSON·CSV Syntax
SQL Semantic Parity
Generator Parity
Evidence Contract
Source·Matrix·Evidence 정합성
```

`verify-cpf-qa37-source-closure.py`의 필수 판정:

- Overlay Package 검증 시 Overlay 파일 집합과 Overlay Manifest를 비교한다.
- Merged Repository 검증 시 전체 Repository 파일 수를 Overlay `fileCount`와 비교하지 않는다.
- Merged Repository에서는 필수 Source, Package, Consumer, DB, Generator,
  Matrix 계약만 검증한다.
- 문자열 치환 방식 임시 수정은 금지한다.
- 결함이면 정식 Source와 독립 Test를 작성한다.

Stage 1이 실패하면 Java, Frontend, DB, Runtime, Browser, Supply-chain을 시작하지 않는다.

## 9. Stage 2 — Java 25 Fresh Lifecycle

가능하면 설치된 통합 Runner를 사용한다.

```powershell
pwsh -NoProfile -File "C:\dev\Docker\CPF\run-full-toolchain.ps1" `
  -RepoRoot "C:\dev\projects\jck\202412_01_CPF"
```

Runner 내부에서 Repository는 `/workspace/cpf`다.
Java 검증은 Fresh Gradle Cache로 한 번 실행한다.

```powershell
$env:GRADLE_USER_HOME=Join-Path $env:TEMP ("cpf-gradle-qa37-"+[guid]::NewGuid())
.\gradlew.bat --no-daemon --stacktrace clean qa37JavaLifecycle
```

사전에 `tasks`, 전체 `build`, 동일 Lifecycle을 반복하지 않는다.

실패 시:

1. 최초 Root Cause Module 식별
2. 영향 Module Test 실행
3. 관련 결함 일괄 수정
4. 전체 Java Lifecycle 마지막 1회 재실행

Optional Pack 제거 Compile Matrix:

```powershell
pwsh -NoProfile -File .\cpf-tools\scripts\verify-cpf-reference-feature-removal.ps1 -Root .
```

검증 대상:

```text
Batch Off
Operations Off
Backoffice Off
Gateway Simulator Off
Core Only
```

## 10. Stage 3 — 공식 DB 3종 Fresh Lifecycle

### 10.1 공통 원칙

공식 Vendor는 다음 3종뿐이다.

```text
Oracle
PostgreSQL
MariaDB
```

MySQL, MSSQL, H2를 공식 완료 증적에 포함하지 않는다.

Docker 설치 과정에서 CPF 업무 Database, Schema, User, Table, Migration,
Seed, Kafka Topic을 임의 생성하지 않는다.

반드시 Repository 정본을 사용한다.

```text
cpf-tools/config/database-install.default.json
cpf-tools/db/vendor-pack-manifest.json
cpf-tools/db/vendor/
cpf-tools/scripts/initialize-cpf-database.ps1
cpf-tools/scripts/invoke-official-db-vendor-sql.ps1
cpf-tools/scripts/initialize-generated-domain-databases.ps1
```

현재 CPF Schema·Data·Seed가 없는 상태를 시작점으로 삼는다.
관리자 접속 정보는 `C:\dev\Docker\Secrets`에서 Script가 참조하게 하며,
Secret 원문을 Console, Log, Evidence에 출력하지 않는다.

Volume 삭제나 `docker compose down -v`로 Fresh 상태를 만들지 않는다.
Repository의 Install, Rollback, Drop, Reapply 절차로 Lifecycle을 검증한다.

### 10.2 MariaDB

필요한 경우 MariaDB만 시작한다.

```powershell
pwsh -NoProfile -File "C:\dev\Docker\CPF\cpf-env.ps1" -Action up -Target batch-mariadb
```

Kafka가 필요 없는 순수 DB 단계에서는 실제 Compose 구성과 Guide가 허용하는
더 작은 Target이 있는지 먼저 확인하고 가장 작은 Target을 사용한다.
임의 Target 이름은 만들지 않는다.

검증:

```text
Fresh Install
V93
V94
Verify
Runtime Query
U94
U93
V93/V94 Reapply
최종 Verify
Batch Off Lifecycle
Idempotent Reapply
Different Hash Conflict
```

완료 후 MariaDB와 이 Stage에서 함께 시작한 Service만 중지한다.

### 10.3 PostgreSQL

```powershell
pwsh -NoProfile -File "C:\dev\Docker\CPF\cpf-env.ps1" -Action up -Target batch-postgresql
```

MariaDB와 동일한 Lifecycle을 PostgreSQL 정본 SQL로 실행한다.
완료 후 이번 Stage에서 시작한 Service만 중지한다.

### 10.4 Oracle

```powershell
pwsh -NoProfile -File "C:\dev\Docker\CPF\cpf-env.ps1" -Action up -Target batch-oracle
```

Oracle User, Schema, Grant, Install, Upgrade, Rollback, Reapply를
Repository Source로 실행한다.
완료 후 이번 Stage에서 시작한 Service만 중지한다.

### 10.5 Vendor 공통 판정

```text
V93/U93 Manual EDU 135 Operation Ledger
V94/U94 Reference Batch Job Pack
Install·Migration·Rollback·Runtime Query·Verify·Checksum 정합
7개 Core EDU Table
3개 Optional Batch Table
PK·FK·Index·Column·Status 의미
Rollback FK Drop 순서
Checksum Self-update 금지
Generated Domain EDU/REF Batch Table 0건
Batch Off 상태의 Batch Table과 Runtime 의존 0건
```

## 11. Stage 4 — Runtime와 장애·복구

정상 기능 검증을 먼저 완료한 뒤 장애 검증으로 이동한다.

기능군별 필요한 Service만 기동한다.

```text
JDBC Command/Query → 대상 DB
Cache → Redis
Messaging·Outbox·Batch Worker → Kafka, 필요 시 대상 DB·Redis
Fault Injection → 대상 Service + Toxiproxy
Telemetry → 대상 Application + OpenTelemetry Collector
```

### 11.1 정상 Runtime

```text
JDBC CRUD·Query
권한
Optimistic Lock
Idempotency
HTTP 정상·202 Unknown Result
Outbox·Inbox·DLQ
File Safe Path·Checksum·Resume
Spring Batch 30 Job·Step
Checkpoint·Restart·Partition·Remote Worker
Batch Off 기동
Optional Pack On/Off
Multi-instance Lease·Fencing
Log·Metric·Trace·Audit Correlation
Masking
```

### 11.2 Toxiproxy

대상 Vendor별 장애 검증 시에만 시작한다.

MariaDB:

```powershell
pwsh -NoProfile -File "C:\dev\Docker\CPF\cpf-tooling.ps1" -Action up -Target fault-mariadb
```

PostgreSQL:

```powershell
pwsh -NoProfile -File "C:\dev\Docker\CPF\cpf-tooling.ps1" -Action up -Target fault-postgresql
```

Oracle:

```powershell
pwsh -NoProfile -File "C:\dev\Docker\CPF\cpf-tooling.ps1" -Action up -Target fault-oracle
```

Redis·Kafka:

```powershell
pwsh -NoProfile -File "C:\dev\Docker\CPF\cpf-tooling.ps1" -Action up -Target fault-infra
```

Proxy Port:

```text
MariaDB 13306
PostgreSQL 15432
Oracle 11521
Redis 16379
Kafka 19093
Toxiproxy API 8474
```

검증:

```text
Latency
Timeout
Connection Cut
Reset
Retry
Circuit Breaker
Unknown Result
Reconciliation
Duplicate Delivery
Process Kill
Recovery
```

장애 조건 제거:

```powershell
pwsh -NoProfile -File "C:\dev\Docker\CPF\cpf-tooling.ps1" -Action reset-faults
```

### 11.3 OpenTelemetry

Trace·Metric·Log 검증 시에만 Collector를 사용한다.

```powershell
pwsh -NoProfile -File "C:\dev\Docker\CPF\cpf-tooling.ps1" -Action up -Target tools
```

Endpoint:

```text
OTLP gRPC 127.0.0.1:4317
OTLP HTTP 127.0.0.1:4318
Collector Metric 127.0.0.1:8888/metrics
Output C:\dev\Docker\CPF\output\otel
```

민감정보, 인증정보, 개인정보 원문이 Telemetry Output에 남지 않는지 확인한다.

## 12. Stage 5 — Frontend와 Browser

Source와 Backend가 안정된 뒤 실행한다.

ADM:

```powershell
Push-Location .\cpf-admin\frontend
npm ci
npm run verify
Pop-Location
```

BZA:

```powershell
Push-Location .\cpf-biz-admin\frontend
npm ci
npm run verify
Pop-Location
```

`verify`가 client generation, lint, typecheck, unit test, production build를
포함하면 하위 명령을 다시 개별 실행하지 않는다.

Browser E2E는 Backend와 해당 DB·Infra만 기동한 상태에서
Chromium, Firefox, WebKit을 각각 한 번 실행한다.

필수 오류 처리:

```text
401
403
404
409
429
500
503
```

필수 UI 품질:

```text
검색
Paging
상세조회
상태 표현
권한
위험 조치 확인
접근성
반응형
Generated Client 실제 연결
```

## 13. Stage 6 — Supply-chain

Source와 최종 Artifact가 확정된 뒤 한 번 실행한다.

Trivy:

```powershell
pwsh -NoProfile -File "C:\dev\Docker\CPF\run-trivy.ps1" `
  -RepoRoot "C:\dev\projects\jck\202412_01_CPF"
```

필수 결과:

```text
Vulnerability
Misconfiguration
Secret Pattern
CycloneDX SBOM
```

결과 경로:

```text
C:\dev\Docker\CPF\output\trivy
```

ORT:

```powershell
pwsh -NoProfile -File "C:\dev\Docker\CPF\run-ort.ps1" -Action requirements
pwsh -NoProfile -File "C:\dev\Docker\CPF\run-ort.ps1" `
  -Action analyze `
  -RepoRoot "C:\dev\projects\jck\202412_01_CPF"
```

정책:

```text
cpf-tools/supply-chain/approved-primary-oss.csv
cpf-tools/supply-chain/license-policy.yml
cpf-tools/supply-chain/ort/evaluator.rules.kts
```

결과 경로:

```text
C:\dev\Docker\CPF\output\ort
```

Output과 Cache는 Source가 아니므로 Repository에 Commit하지 않는다.
외부 제공 Evidence에는 로컬 절대경로와 민감정보를 제거한다.

## 14. 최소 재검증 Matrix

| 변경 범위 | 즉시 재검증 | 마지막 상위 재검증 |
|---|---|---|
| Source Closure Gate | Gate Unit Test | Stage 1 |
| Java Package·Binding | 영향 Module Test | Java Lifecycle |
| Batch Source | Batch Test, On/Off Compile | Java·Batch Runtime |
| V93 Core DB | 해당 Vendor Static Parity | 해당 Vendor 전체 V93 |
| V94 Batch DB | Static Parity, Batch Off | 해당 Vendor V94/U94 |
| Generator | Golden Template Test | 3 Vendor Generator Parity |
| Runtime Adapter | 해당 기능군 | 해당 Runtime Family |
| ADM | ADM `npm run verify` | ADM Browser |
| BZA | BZA `npm run verify` | BZA Browser |
| Telemetry | 영향 Export Test | OTel Runtime |
| Supply-chain 설정 | 영향 Tool | Supply-chain Stage |
| Matrix·Evidence | Truth Contract | Stage 1 |

환경이 없는 항목은 Source 변경으로 우회하지 않는다.

## 15. Source Defect와 Environment Blocker

### Source Defect

```text
Compile·Test 실패
Public API·SPI·Internal 경계 위반
Consumer Binding 누락
Concrete Adapter 누락
Gate False Green·Fail-Always·Self-dirty
DB 3 Vendor Drift
Install·Upgrade·Rollback·Reapply 누락
Generator 유입
Optional Pack 역의존
재시도·복구·멱등성·동시성 계약 불일치
권한·감사·마스킹 결함
```

### Environment Blocker

```text
Docker Engine 장애
필수 Image·Container·Volume 누락
Java·Node·Browser·DB Client 실행 불가
Network·Registry·Proxy 정책 차단
Oracle Image·License 문제
Disk·Memory·실행 권한 부족
Trivy·ORT Runtime 문제
```

Environment Blocker는 실제 명령, 시작·종료 시각, Exit Code, 오류,
환경 상태를 기록하고 `미검증`으로 판정한다.

## 16. 검수 종료와 Service 정리

검수 시작 당시 실행 중이던 Container는 그대로 유지한다.
이번 검수에서 새로 시작한 Container만 중지한다.

```powershell
$currentRunning=@(
  docker ps --filter "name=cpf-" --format "{{.Names}}" |
  Where-Object { $_ -and $_.Trim() } |
  ForEach-Object { $_.Trim() }
)

$startedByThisRun=@(
  $currentRunning |
  Where-Object { $_ -notin $initialRunning }
)

foreach($name in $startedByThisRun){
  docker stop $name
}
```

종료 전 장애 조건을 제거한다.

```powershell
pwsh -NoProfile -File "C:\dev\Docker\CPF\cpf-tooling.ps1" -Action reset-faults
```

다음을 유지한다.

```text
Image
Volume
Runner
Tool
Compose
Script
Secret
기존 데이터
```

정리 목적으로 `prune`, Volume 삭제, Docker Desktop 초기화를 실행하지 않는다.

## 17. Evidence

각 실행마다 기록한다.

```text
기준 Commit SHA
결과 Commit SHA
Working Tree 상태
실행 명령
Profile과 환경
Tool·Runtime Version
시작·종료 시각
Exit Code
Requirement·Scenario
Artifact 경로와 SHA-256
실제 결과
시작한 Service
중지한 Service
민감정보 제거 여부
```

과거 Commit 또는 다른 환경의 Evidence를 현재 성공으로 자동 승계하지 않는다.

## 18. 완료 조건

다음을 모두 충족해야 `완료`다.

```text
HEAD == origin/master == result SHA
Working Tree Clean
Stage 1 저비용 Gate PASS
Java 25 Fresh Lifecycle PASS
Optional Pack 제거 Compile PASS
ADM/BZA Clean Verification PASS
Oracle Fresh Lifecycle PASS
PostgreSQL Fresh Lifecycle PASS
MariaDB Fresh Lifecycle PASS
Kafka·Redis·Batch·Scheduler Runtime PASS
Toxiproxy Fault·Recovery PASS
OpenTelemetry Trace·Metric·Log PASS
Playwright Chromium·Firefox·WebKit PASS
Trivy·SBOM·Secret PASS
ORT Dependency·License PASS
Source Defect 0건
실행되지 않은 필수 검증 0건
Matrix와 Evidence exact SHA 일치
민감정보 원문 0건
```

허용 상태:

```text
완료
부분 구현
미구현
미검증
실패
재확인 필요
```

Source가 존재하거나 Commit됐다는 이유만으로 완료 처리하지 않는다.

## 19. Codex 최종 보고 형식

1. 검수 시작 SHA와 종료 SHA
2. 초기 Working Tree와 종료 Working Tree
3. Docker 초기 상태와 종료 상태
4. Source Defect
5. Environment Blocker
6. 변경 Source·SQL·Test·Script·Config
7. Requirement·Defect별 영향
8. Owner Module과 실제 Consumer
9. 실행 명령·시각·Exit Code
10. 실행한 검증
11. 실행하지 못한 검증
12. Artifact와 SHA-256
13. Evidence 경로
14. Delete Candidate
15. 최종 상태
16. 남은 작업이 있으면 최소 영향 범위

Git Commit과 Push는 수행하지 않는다.
