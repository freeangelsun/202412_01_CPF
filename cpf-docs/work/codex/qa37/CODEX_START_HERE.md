# CPF QA37 Codex Start Here
## 크레딧 절약형 전수 검수 시작점

## 1. 이번 검수 기준

- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- 검수 시작 기준 SHA: `eaab5108575b76a492703b00f3e050e8dc25cffb`
- Local Repository: `C:\dev\projects\jck\202412_01_CPF`
- Docker Runtime: `C:\dev\Docker\CPF`
- Secret: `C:\dev\Docker\Secrets`
- 검수 수준: 전수 검수
- 실행 방식: 위험 우선·Stage별 단일 실행·PASS Stage 재실행 금지
- 역할: 결함을 찾는 데서 끝내지 말고 영향 범위 보완 개발과 재검증까지 수행
- Git Commit·Push: 수행 금지

`HEAD`가 위 SHA와 달라도 `HEAD == origin/master`이고 Working Tree가 깨끗하면
실제 `HEAD`를 검수 기준으로 사용한다. 그 차이를 분석하느라 크레딧을 쓰지 않는다.

## 2. 처음 읽을 문서

처음에는 다음 두 문서만 읽는다.

1. 이 문서
2. `cpf-docs/work/codex/qa37/CPF_CODEX_QA37_FINAL_INDEPENDENT_VERIFICATION_REQUEST.md`

Docker Stage에 들어갈 때만 Docker 문서를 지정 순서대로 읽는다.
DB·Runtime·Frontend·Supply-chain 상세 문서도 해당 Stage에서만 읽는다.

Repository 전체를 처음부터 자유 탐색하지 않는다.

## 3. 첫 명령

```powershell
cd "C:\dev\projects\jck\202412_01_CPF"
pwsh -NoProfile -File .\cpf-tools\scripts\invoke-cpf-codex-preflight.ps1
```

결과 JSON은 `%TEMP%`에 저장된다.

판정:

- `sourceReady=false`: 비싼 검증을 시작하지 말고 기준선 결함만 해결한다.
- `sourceReady=true`, `environmentReady=false`: 정적·Java는 진행할 수 있으나 Docker Runtime은 Environment Blocker다.
- 둘 다 `true`: Stage 01부터 진행한다.

## 4. Stage 실행 원칙

모든 대형 명령은 아래 Wrapper로 실행한다.

```powershell
pwsh -NoProfile -File .\cpf-tools\scripts\invoke-cpf-codex-stage.ps1 `
  -StageId "<STAGE_ID>" `
  -Command '<실제 명령>'
```

실행 원장은 Repository 밖에 저장된다.

```text
C:\dev\Docker\CPF\output\codex\qa37\execution-ledger.csv
C:\dev\Docker\CPF\output\codex\qa37\logs\
```

동일 Stage가 이미 `PASS`면 Wrapper가 자동으로 건너뛴다.
`FAIL` Stage는 Root Cause 수정 후에만 `-AllowRerun`으로 다시 실행한다.

Codex 세션이 중단되거나 새 세션으로 바뀌면 원장을 읽고
마지막 PASS 다음 Stage부터 재개한다.

## 5. 전수 검수 Stage

| Stage | 범위 | 실패 시 |
|---|---|---|
| `00_PREFLIGHT` | Git·문서·Docker·Tool Snapshot | 비싼 Stage 금지 |
| `01_STATIC` | Hygiene·Secret·Ownership·Boundary·Source Closure | 공통 Root Cause만 수정 |
| `02_JAVA` | Java 25 Fresh Build·Test·Publication | 영향 Module Test 후 전체 1회 |
| `03_OPTIONAL` | Optional Pack 제거 Compile | 영향 Pack만 수정 |
| `04_DB_MARIA` | MariaDB Fresh·Upgrade·Rollback·Reapply | MariaDB 범위만 수정 |
| `05_DB_POSTGRES` | PostgreSQL Lifecycle | PostgreSQL 범위만 수정 |
| `06_DB_ORACLE` | Oracle Lifecycle | Oracle 범위만 수정 |
| `07_RUNTIME` | Kafka·Redis·Batch·Scheduler·Gateway | 기능군별 수정 |
| `08_FAULT` | Toxiproxy·Process Kill·Recovery | 실패 기능군만 재검증 |
| `09_OTEL` | Trace·Metric·Log·Masking | Export 경로만 수정 |
| `10_ADM` | ADM Clean Verify | ADM만 재검증 |
| `11_BZA` | BZA Clean Verify | BZA만 재검증 |
| `12_BROWSER` | Chromium·Firefox·WebKit | 실패 Browser·Route 우선 |
| `13_SUPPLY` | Trivy·SBOM·Secret·ORT License | 최종 Artifact 기준 1회 |
| `14_TRUTH` | Matrix·Evidence·exact SHA | 문서·Evidence만 수정 |

Stage는 위 순서대로 실행한다.
앞 Stage가 실패하면 뒤 대형 Stage를 실행하지 않는다.

## 6. 크레딧을 낭비하지 않는 탐색 규칙

- Gate가 지목한 Module과 Consumer부터 읽는다.
- 같은 용어로 Repository 전체를 여러 번 검색하지 않는다.
- Source Mapping과 Change Manifest를 우선 사용한다.
- `build`, `test`, `npm ci`, DB Lifecycle을 반복 실행하지 않는다.
- 하나의 Root Cause가 여러 실패를 만들면 원인을 한 번만 수정한다.
- 영향 Test가 성공한 뒤 최종 상위 Lifecycle을 한 번만 재실행한다.
- Source가 안정되기 전에 Browser와 Supply-chain을 실행하지 않는다.
- 환경 오류를 제품 Source 변경으로 우회하지 않는다.
- 과거 SHA Evidence를 현재 성공으로 승계하지 않는다.
- 실행하지 않은 항목은 `미검증`이다.

## 7. 보완 개발 원칙

실제 결함을 발견하면 다음을 하나의 완료 단위로 처리한다.

```text
Owner Source
Public API·SPI·Internal 경계
실제 Consumer
정상·오류·경계·부분 실패
재시도·복구·멱등성·동시성
DB 3 Vendor·Migration·Rollback
Generator
보안·권한·감사·마스킹
Unit·Integration·Runtime Test
Matrix·Evidence
```

부분 구현이나 Interface만 추가하고 완료 처리하지 않는다.

## 8. 안전 규칙

- `cpf-tools/build/**`는 정식 Source다. 가비지로 삭제하지 않는다.
- `README.md`, `cpf-docs/guides/**`, `cpf-docs/deliverables/**`를 임의 삭제하지 않는다.
- Git 추적 파일 자동 삭제·복구 금지
- Docker Image·Container·Volume·Runner·Secret 삭제 금지
- `git clean`, `reset --hard`, Docker prune·초기화 금지
- 전체 설치 Script 재실행 금지
- 모든 DB와 Service를 동시에 상시 기동하지 않는다.
- 검수 종료 시 이번 검수에서 시작한 Service만 중지한다.

가비지 정리가 필요하면 다음 Script를 Preview로 먼저 실행한다.

```powershell
pwsh -NoProfile -File .\cpf-tools\scripts\cleanup-cpf-generated-artifacts-safe.ps1
```

검토 후에만:

```powershell
pwsh -NoProfile -File .\cpf-tools\scripts\cleanup-cpf-generated-artifacts-safe.ps1 -Apply
```

이 Script는 Git 추적 파일이 포함된 디렉터리를 삭제 대상에서 제외한다.

## 9. 완료 판정

전수 Stage가 모두 PASS하고 Source Defect와 미실행 필수 검증이 0건이며,
Matrix·Evidence가 실제 결과와 일치할 때만 `완료`다.

최종 보고에는 다음만 간결하게 남긴다.

1. 기준 SHA
2. Source Defect와 수정
3. Environment Blocker
4. Stage별 PASS/FAIL/미검증
5. 변경 파일
6. Evidence·Artifact SHA-256
7. Working Tree
8. 최종 상태
