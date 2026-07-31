# CPF QA32 Push 후 자체검수 결과

## 1. 검수 기준

- Repository: `https://github.com/freeangelsun/202412_01_CPF`
- Branch: `master`
- 기준 SHA: `1536a0d59004ebade7dcb29383cbe2e758547f8e`
- Commit: `20260731_03`
- 검수 제외:
  - 모든 `README*`
  - `cpf-docs/guides/**`
  - `cpf-docs/assets/readme/**`
  - `cpf-docs/assets/guides/**`
  - 별도 README·Guide 작업 결과
- 사용자 승인 전 Commit·Push·Branch·Tag·PR 생성 없음

## 2. 최종 판정

**현재 master는 QA32 최종 완료 상태가 아니다.**

Matrix에는 개발 상태가 모두 `완료`로 기록되어 있지만 실제 검증 완료는 일부에 불과하며,
Gradle 초기화 경로에 P0 결함이 확인되었다.

| 구분 | 전체 | 개발 상태 완료 | 검증 완료 | 미검증 |
|---|---:|---:|---:|---:|
| Requirement | 62 | 62 | 집계 포함 | 집계 포함 |
| Defect | 60 | 60 | 집계 포함 | 집계 포함 |
| Scenario | 222 | 222 | 집계 포함 | 집계 포함 |
| **통합** | **344** | **344** | **99** | **245** |

`CPF_20260730_QA32_UNRESOLVED_REGISTER.csv`에는 아직 7개 검증 묶음이 남아 있다.

- 미검증: 6개
- 재확인 필요: 1개
- 잔여 0건 아님

## 3. 직접 확인한 P0 결함

### P0-01 Gradle Composite Build 참조 파손

`settings.gradle`은 다음 경로를 계속 참조한다.

- `includeBuild 'cpf-tools/build/platform-bom'`
- `includeBuild 'cpf-tools/build/gradle-plugin'`

그러나 최신 master에는 `cpf-tools/build` 경로가 존재하지 않는다.
같은 Commit에서 다음 파일들이 삭제됐다.

- `cpf-tools/build/platform-bom/build.gradle`
- `cpf-tools/build/platform-bom/settings.gradle`
- `cpf-tools/build/gradle-plugin/build.gradle`
- `cpf-tools/build/gradle-plugin/settings.gradle`
- `cpf-tools/build/gradle-plugin/src/main/groovy/com/cpf/build/CpfDomainConventionPlugin.groovy`

따라서 `gradlew help`, 전체 Build, Test 이전의 Gradle 초기화 단계부터 실패할 가능성이 매우 높다.

**판정: 실패**

### P0-02 QA32 All Gate가 전체 검증 Gate가 아님

`cpf-tools/scripts/verify-cpf-qa32-all.ps1`은 다음 Python 정적 검사만 실행한다.

- Primary Engine 문자열·파일 검사
- Repository Security 정적 검사
- Supply-chain 정적 검사
- Generator 정적 검사
- Result Matrix 상태 검사

다음은 실행하지 않는다.

- Gradle 초기화
- Java Compile/Test
- ADM/BZA npm Build
- Playwright
- Oracle·PostgreSQL·MariaDB
- Kafka Remote Runtime
- Gateway·Agent 장애 복구 Runtime

따라서 이 스크립트의 Exit 0을 QA32 전체 완료 Evidence로 사용할 수 없다.

**판정: 실패**

### P0-03 Runtime Evidence Script의 거짓 양성 위험

`verify-cpf-qa32-runtime.ps1`에는 다음 문제가 있다.

1. 함수 내부 `$results += ...`는 Scope 처리 없이 사용되어 최종 Evidence의 `results`가 비어 있을 수 있다.
2. Frontend 실행을 하나의 ScriptBlock에서 연속 실행하고 마지막 `$LASTEXITCODE`만 읽는다.
3. `npm ci`, `typecheck`, `test`, `build` 중 앞 단계가 실패해도 뒤 명령이 실행되어 마지막 Playwright 결과로 덮일 수 있다.
4. Native command별 즉시 실패와 단계별 stdout/stderr Evidence가 없다.
5. 실행 실패 시 `Pop-Location`과 Evidence 종료 기록이 보장되지 않는다.

**판정: 실패**

### P0-04 Completion Gate가 Matrix 자기선언을 신뢰

`verify-cpf-qa32-completion.py`의 비 Release 모드는 Result Matrix의
`development_status=완료`만 검사한다.

또한 Primary Engine Gate는 실제 Runtime 실행보다 다음에 크게 의존한다.

- 파일 존재
- 특정 문자열 존재
- 일부 Legacy 파일 부재
- 금지 문자열 검색
- Migration 파일명 V82~V85 존재

Source→Consumer→Runtime→Evidence를 검증하지 않으므로
Matrix 344건을 모두 완료로 기록한 뒤 정적 Gate가 통과하는 자기확인 구조가 된다.

**판정: 실패**

### P0-05 최신 exact SHA Runtime Evidence 없음

Push 후 SHA는 `1536a0d59004ebade7dcb29383cbe2e758547f8e`이지만 Unresolved Register의 `QA32-V007`은
최신 exact SHA Evidence 재생성을 `재확인 필요`로 유지한다.

따라서 이전 Overlay Base SHA나 Working Tree Evidence를 현재 master 검증 결과로 승계할 수 없다.

**판정: 미검증**

## 4. 자체검수 결과 요약

| 항목 | 판정 |
|---|---|
| 최신 Push 반영 확인 | 완료 |
| QA32 Source 대규모 반영 | 완료 |
| README·Guide 검토 | 제외 |
| Gradle 초기화 | 실패 가능성 확정 |
| Java 25 전체 Build/Test | 미검증 |
| ADM/BZA Build·Playwright | 미검증 |
| 3개 DB Lifecycle | 미검증 |
| Kafka Remote Batch Runtime | 미검증 |
| Gateway·Agent 장애 복구 | 미검증 |
| Supply-chain Final Artifact | 미검증 |
| exact-SHA Evidence | 미검증 |
| QA32 최종 완료 | 실패 |

## 5. 회사 PC에서 즉시 확인할 명령

### Composite Build 경로 검사

```powershell
@("cpf-tools/build/platform-bom","cpf-tools/build/gradle-plugin") | ForEach-Object { if (-not (Test-Path $_)) { Write-Error "MISSING COMPOSITE BUILD: $_" } else { Write-Host "OK: $_" } }
```

### Gradle 초기화 검사

```powershell
.\gradlew.bat help --no-daemon --stacktrace
```

현재 Source 기준으로 위 명령은 먼저 수정한 뒤 통과시켜야 한다.

## 6. 완료 금지 조건

다음 중 하나라도 남으면 QA32 완료 처리 금지다.

- Gradle 초기화 실패
- Result Matrix 미검증 행 존재
- Unresolved Register 행 존재
- Runtime Evidence의 Source SHA 불일치
- 단계별 Exit Code·stdout·stderr 누락
- Frontend 앞 단계 실패가 뒤 단계 성공으로 덮이는 구조
- DB·Kafka·Gateway 장애·복구 Scenario 미실행
- Legacy Engine 또는 이중 Primary 잔존
