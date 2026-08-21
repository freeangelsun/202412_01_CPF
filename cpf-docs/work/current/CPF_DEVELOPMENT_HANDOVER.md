# CPF Development Handover — C 개발/QA 관리_22 Final Development Package

## 1. Basis

- Baseline ZIP: `CPF_FULL_SOURCE_FOR_NEXT_QA_20260820_230143.zip`
- Baseline SHA-256: `8b2e064accaead9e3b81bbf306c2197142621ffdc25aab6cba9a420ef613ad1f`
- Baseline files: `8,323`
- Final Fresh Replay V3 Source SHA-256: `c927382e9bd2b559a306e3ccf33183492190a83fbf11255b3a81f0a72c131f3a`
- Source-scope files: `8,146`
- Canonical Requirements: `205`
- Consolidated QA findings: `63` (`61 CLOSED / 2 BLOCKED_EXTERNAL`)
- GitHub/master was intentionally not used as the current baseline.

## 2. Final development result

All implementation, source/static verification, verifier repair, regression tests, Evidence and Fresh Replay work executable in the assistant environment is complete. Major closure areas are Generated Domain lifecycle, per-domain DB Bootstrap, Runtime fencing/Retention CAS/central runtime authority, cpf-common ownership, ADM Session/RBAC/typed consumers, Backoffice security, EDU physical/executable 20+15, DB3 contracts, Public distribution and non-vacuous Final tooling.

Canonical static `12/12 PASS`; Python aggregate `761 passed / 34 environment skips / 0 failed`; Frontend actual-consumer `8/8 PASS`; Package/Evidence integrity PASS. Details are in `cpf-docs/deliverables/TEST_AND_EVIDENCE.md`.

## 3. Delete lifecycle

`DELETE_MANIFEST.csv` has `635` rows:

- `601 HISTORICAL_ALREADY_ABSENT`
- `34 PENDING_USER_EXECUTION` with `approved=true`, `precondition=SATISFIED`, `user_approved=false`
- Fresh Replay applies pending candidates only in the replay Snapshot: `31` removed / `3` already absent.
- Developer GPT did not delete these paths from the user's Working Tree.
- `apply_delete_manifest.ps1` requires a non-empty `-UserApprovalRef` before pending user-execution deletions can run. Protected paths remain fail-closed.

## 4. Local integration

```powershell
$log="$env:USERPROFILE\Downloads\gradle-problems.txt"; $start=Get-Date; python .\cpf-tools\verification\tools\run-cpf-canonical-verifiers.py --root .; $static=$LASTEXITCODE; .\gradlew clean build --continue --stacktrace -PcpfIncludeGeneratedDomains=true 2>&1 | Tee-Object -FilePath $log; $gradle=$LASTEXITCODE; $failed=@(Select-String -Path $log -Pattern '^> Task .* FAILED$'); $testFailed=@(Select-String -Path $log -Pattern '^\s*\d+ tests? completed, \d+ failed'); Write-Host "`n========== CPF FINAL REPORT =========="; Write-Host "StaticGate     : $(if($static -eq 0){'PASS'}else{'FAIL'})"; Write-Host "Gradle         : $(if($gradle -eq 0){'PASS'}else{'FAIL'})"; Write-Host "GradleExitCode : $gradle"; Write-Host "Failed Tasks   : $($failed.Count)"; Write-Host "Test Failures  : $($testFailed.Count)"; if($failed.Count -gt 0){$failed | Select-Object -First 30 | ForEach-Object {Write-Host "  $($_.Line)"}}; Write-Host "Started        : $start"; Write-Host "Finished       : $(Get-Date)"; Write-Host "Log            : $([IO.Path]::GetFullPath($log))"; Write-Host "======================================"
```

Expected: Static PASS + Gradle `BUILD SUCCESSFUL`. The final Java25 run is mandatory external acceptance and has not been fabricated as PASS.

## 5. External acceptance

Overall product QA completion remains blocked only by the two external Finding groups listed in `OPEN_ISSUES.md`: Java25/Public-Binary and live DB3/Multi-instance/Browser. Any discovered source defect reopens development; it is not waived.

## 6. Git safety

No commit, push, branch, tag, reset, restore, stash, clean or history rewrite was performed. The user applies the Overlay/Delete Manifest, verifies, reviews `git status`, and decides Git write actions.
