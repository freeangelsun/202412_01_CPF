<!-- DEV-GPT-CURRENT-20260825 -->
# Developer GPT Currentization — 2026-08-25

- Baseline ZIP SHA-256: `d2e89aba1841a4387a473610db905415f8565fcf09d06a56a8afa3a1b33a3a48`
- Current Product Source SHA-256: `c79be31a71c15c02665d56e29c0f51244c91ab3894183775ce311cde3dbf40df`
- Canonical Requirements: `208`
- 개발 단계: Source/Static/Contract/Substitute 구현 가능한 범위 완료
- 실행 결과: 현재 Source에서 실행한 정적/계약/Substitute 검증 관측 FAIL `0`
- 필수 미검증: Java25 Root Gradle, Docker DB3 3사, Multi-instance/Process Kill, Kafka 2-worker, Browser E2E, Performance, Full Runtime/Fresh Replay
- QA 전달: 금지. 새 Full Runtime이 `FAIL=0 / SKIP_ENV=0 / NOT_EXECUTED=0 / UNVERIFIED=0`이 된 뒤 전환
- Customer Shared Library Generator / Windows <=200 / Docker self-managed prerequisite lifecycle는 상위 정본에 등록됨.

# CPF Current Work Request — Final Environment Revalidation

> 상위 정본: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`  
> Source/static 기준: C 개발/QA 관리_21 final-applied snapshot  
> 원칙: 이미 닫힌 Source Gap을 다시 개발하지 않는다. 미실행 Runtime을 PASS로 만들지 않는다.

## 1. Source/static closure

이번 개발 사이클에서 다음 Current Target을 구현·재검증했다.

1. `cpf-common` 고객 업무 공통 Product Owner 및 `cpf-starter-common` Runtime/AutoConfiguration 분리.
2. Generated Domain root `gradle.properties` Developer Contract, stateless ownership/stale-generated, 불필요한 YAML/lock/state 미생성, externalClients 실제 Consumer, DB Binding 분리.
3. EDU physical/executable `Online 20 + Batch 15 = 35`.
4. System6/Operation/Runtime Instance source contracts 및 same-host collision fencing.
5. Public Workspace/Shared Bootstrap/Public Binary isolated-consumer contracts.
6. ADM HttpOnly JDBC BFF Session/CSRF, Menu 64↔Route 68, permission identity, System6 UI, Commercial Page, Generated Consumer 337/337.
7. Backoffice/MBW currentization 및 Backend 96↔Web 96 contract.
8. DB3 Canonical Seed/Bundle/Renderer Oracle/PostgreSQL/MariaDB parity.
9. 208 Canonical Requirement developer ledger ↔ 30,605 derived logical requirements.
10. Current-only governance, Delete/Garbage lifecycle, repository-wide Java/Spring/ownership/dependency/hygiene gates.

상세 변경 및 208개 Requirement별 리뷰는:
- `cpf-docs/work/current/CPF_DEVELOPMENT_REQUIREMENT_REVIEW.md`
- `cpf-docs/work/current/CPF_DEVELOPMENT_REQUIREMENT_REVIEW.csv`
- `cpf-docs/deliverables/TEST_AND_EVIDENCE.md`

## 2. Current accepted Steering and remaining work

Current VS Code Working Tree 자체가 Primary Source다. Baseline/Overlay/ZIP을 다시 적용하지 않으며, Fresh Replay는 마지막에 별도 disposable workspace에서만 수행한다. 현재 추가 수용된 실행 범위는 다음과 같다.

1. Generated Domain: root `gradle.properties` Developer Contract, YAML/lock/hidden bookkeeping 0, Generator-owned Scratch/MBR/EXS regenerate 및 Java 25 build/test/runtime, Open Git/Source package hygiene.
2. Open Git: 실제 Fresh release 생성, public command 전수 실행, Framework publication, 신규 Domain generate/compile/test, authored ADM/Backoffice/UI/EDU Fresh build/runtime. Private Source와 Maven Local false green 금지.
3. Backoffice: retired `cpf-biz-admin` 복원 금지. 최신 `cpf-backoffice`/MBW와 실제 UI/Channel/BFF/Gateway optional topology를 역추적한 뒤 used/unused runtime 및 Header6→공식 Domain Invocation→Owner DB E2E.
4. ADM UI: menu/route/component/generated-client/backend inventory, fresh frontend/backend, 실제 Browser menu traversal, screenshots, permission/error/responsive/accessibility/console/network 검증.
5. Starter/Common: physical catalog/BOM/publication, AutoConfiguration actual consumer와 zero-footprint, Common owner/public API/consumer/Core·ADM 경계를 runtime으로 검증.
6. Java25 Root build/test, Public Binary isolated consumer, DB3 live lifecycle, Multi-WAS/recovery, Windows PowerShell 실행, root garbage no-regeneration Final Gate.
7. Batch Runtime Deep-Dive: `cpf-batch` owner/Public-Internal 경계, Tasklet/Chunk/Local·Remote modes, Scheduler/Worker/Agent, Restart·Retry·Recovery·Reconcile·Reprocess, UNKNOWN, multi-instance/process-kill, DB3, ADM/EDU/Generated/Open Git impact을 실제 Runtime 기준으로 검증한다. 30개 실행단위의 정본은 `CPF_BATCH_RUNTIME_DEEP_DIVE_WORK_PACKAGE.md`다.

실행되지 않은 항목은 `미검증`으로 유지한다. 결함은 Finding을 먼저 기록하고 실제 Owner Source/Test/Verifier/Canonical Requirement를 수정한 후 전체 영향범위 재검증으로만 닫는다. 역할별 상태 컬럼 ownership은 변경하지 않는다.

## 3. Canonical local integration command

```powershell
$log="$env:USERPROFILE\Downloads\gradle-problems.txt"; $start=Get-Date; ./gradlew clean build --continue --stacktrace 2>&1 | Tee-Object -FilePath $log; $code=$LASTEXITCODE; $failed=@(Select-String -Path $log -Pattern '^> Task .* FAILED$'); $testFailed=@(Select-String -Path $log -Pattern '^\s*\d+ tests? completed, \d+ failed'); Write-Host "`n========== FINAL REPORT =========="; Write-Host "Result        : $(if($code -eq 0){'PASS'}else{'FAIL'})"; Write-Host "ExitCode      : $code"; Write-Host "Failed Tasks  : $($failed.Count)"; Write-Host "Test Failures : $($testFailed.Count)"; if($failed.Count -gt 0){$failed | Select-Object -First 20 | ForEach-Object { Write-Host "  $($_.Line)" }}; Write-Host "Started       : $start"; Write-Host "Finished      : $(Get-Date)"; Write-Host "Log           : $([IO.Path]::GetFullPath($log))"; Write-Host "=================================="
```

정상 기대: `Result=PASS`, `ExitCode=0`, `Failed Tasks=0`, `BUILD SUCCESSFUL`.  
실패 전달 파일: `%USERPROFILE%\Downloads\gradle-problems.txt`.


## 2026-08-26 BAT-NO-REMOTE-KAFKA Current Steering

이 문서의 과거 `remote-kafka` / `REMOTE_PARTITION` / `REMOTE_CHUNK` / Provider-neutral Remote Adapter 관련 계획·보고는 **historical context**이며 현재 요구가 아니다. 사용자 직접 Steering `BAT-NO-REMOTE-KAFKA`가 이를 supersede한다. 일반 Batch·Worker·Scheduler·Center-Cut은 Kafka 없이 보존하고 Kafka 기반 Batch Remote Execution 전체 Surface를 실제 Consumer/Bean/Runtime/DB/Publication/Harness 호출경로 기준으로 제거한다. 새 Remote Transport/Broker를 만들지 않는다. 공용 Messaging Kafka는 별도 Owner/Consumer로만 판단한다.
