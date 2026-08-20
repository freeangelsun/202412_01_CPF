# CPF Current Work Request — Final Environment Revalidation

> 상위 정본: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`  
> Source/static 기준: C 개발/QA 관리_21 final-applied snapshot  
> 원칙: 이미 닫힌 Source Gap을 다시 개발하지 않는다. 미실행 Runtime을 PASS로 만들지 않는다.

## 1. Source/static closure

이번 개발 사이클에서 다음 Current Target을 구현·재검증했다.

1. `cpf-common` 고객 업무 공통 Product Owner 및 `cpf-starter-common` Runtime/AutoConfiguration 분리.
2. Generated Domain root `cpf-domain.yaml`, ownership/stale-generated, externalClients 실제 Consumer, DB Binding 분리.
3. EDU physical/executable `Online 20 + Batch 15 = 35`.
4. System6/Operation/Runtime Instance source contracts 및 same-host collision fencing.
5. Public Workspace/Shared Bootstrap/Public Binary isolated-consumer contracts.
6. ADM HttpOnly JDBC BFF Session/CSRF, Menu 64↔Route 68, permission identity, System6 UI, Commercial Page, Generated Consumer 337/337.
7. Backoffice/MBW currentization 및 Backend 96↔Web 96 contract.
8. DB3 Canonical Seed/Bundle/Renderer Oracle/PostgreSQL/MariaDB parity.
9. 205 Canonical Requirement developer ledger ↔ 30,605 derived logical requirements.
10. Current-only governance, Delete/Garbage lifecycle, repository-wide Java/Spring/ownership/dependency/hygiene gates.

상세 변경 및 205개 Requirement별 리뷰는:
- `cpf-docs/work/current/CPF_DEVELOPMENT_REQUIREMENT_REVIEW.md`
- `cpf-docs/work/current/CPF_DEVELOPMENT_REQUIREMENT_REVIEW.csv`
- `cpf-docs/deliverables/TEST_AND_EVIDENCE.md`

## 2. Remaining work — environment acceptance only

Source 재개발이 아니라 다음 환경 실행 Evidence가 남아 있다.

1. Java25 final `./gradlew clean build --continue --stacktrace`.
2. Oracle/PostgreSQL/MariaDB live install → migration → seed → runtime query → upgrade → rollback.
3. Multi-WAS / same-host multi-process / process-kill / lease-expiry / restart/reconcile.
4. ADM/Backoffice Browser E2E (Chromium/Firefox/WebKit, responsive, 401/403/404/409/429/500/503).
5. Reachable Public Binary Repository 기반 isolated consumer.
6. Windows PowerShell 5.1 apply/delete/verification runtime.
7. Commercial GA/edition/license/support Product/Legal/QA decision.

이 항목은 실행되지 않았다면 `미검증`으로 유지한다. 실패가 발생하면 같은 Requirement ID에서 Root Cause 단위로 Source/Test/Verifier/Config/Evidence를 재개발한다.

## 3. Canonical local integration command

```powershell
$log="$env:USERPROFILE\Downloads\gradle-problems.txt"; $start=Get-Date; ./gradlew clean build --continue --stacktrace 2>&1 | Tee-Object -FilePath $log; $code=$LASTEXITCODE; $failed=@(Select-String -Path $log -Pattern '^> Task .* FAILED$'); $testFailed=@(Select-String -Path $log -Pattern '^\s*\d+ tests? completed, \d+ failed'); Write-Host "`n========== FINAL REPORT =========="; Write-Host "Result        : $(if($code -eq 0){'PASS'}else{'FAIL'})"; Write-Host "ExitCode      : $code"; Write-Host "Failed Tasks  : $($failed.Count)"; Write-Host "Test Failures : $($testFailed.Count)"; if($failed.Count -gt 0){$failed | Select-Object -First 20 | ForEach-Object { Write-Host "  $($_.Line)" }}; Write-Host "Started       : $start"; Write-Host "Finished      : $(Get-Date)"; Write-Host "Log           : $([IO.Path]::GetFullPath($log))"; Write-Host "=================================="
```

정상 기대: `Result=PASS`, `ExitCode=0`, `Failed Tasks=0`, `BUILD SUCCESSFUL`.  
실패 전달 파일: `%USERPROFILE%\Downloads\gradle-problems.txt`.
