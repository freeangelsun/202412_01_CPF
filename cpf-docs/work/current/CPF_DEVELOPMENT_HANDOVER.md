# CPF Development Handover — Current

## 1. Current basis

- Current local full-source ZIP: `CPF_FULL_SOURCE_FOR_NEXT_QA_20260820_122758(1).zip`
- SHA-256: `f73988097aef77a1bcc795ba66394326dd5a9f875a2d1b530e2c99e315cf5ceb`
- Files: `8,288`
- Exact Git SHA: supplied ZIP에 `.git`이 없으므로 확인 불가. 과거 SHA를 대체값으로 사용하지 않는다.
- Canonical Target: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md` (205 Current Requirement)
- Current Work Request: `cpf-docs/work/current/CPF_CURRENT_WORK_REQUEST.md`
- Current Evidence/Open Issues: `cpf-docs/deliverables/TEST_AND_EVIDENCE.md`, `OPEN_ISSUES.md`

## 2. Governance current-only cleanup

이번 정리에서는 `deliverables/`를 Current Evidence/Issue/Package 산출물의 유일 Owner로 고정하고 `work/` 동명 복제본, 과거 Work Request/Handover, 의미 흡수 완료 Steering을 제거한다. 삭제 전에 Final Target에 `operationId != executionId`, Gateway/Direct 동일 보안과 자동 fallback 금지 의미를 보강했다.

기존 canonical `DELETE_MANIFEST.csv`의 다른 historical/source 후보는 이번 범위에서 자동 일괄 삭제하지 않는다. 더 넓은 cleanup은 `CANON-GAP-010`으로 영향 검증 후 수행한다.

## 3. Latest local integration result

최신 사용자 로컬 Gradle 통합검증은 **FAIL**이다.

- `BUILD FAILED in 7m 22s`
- `355 actionable tasks: 354 executed, 1 up-to-date`
- `Build completed with 9 failures`
- 실패 Task는 Runtime Control compile, ADM frontend contract, Backoffice compile, Domain Call test compile, AI UNKNOWN test, ISO8583 test compile, Drain test, IBM MQ header test, JMS compile이다.

따라서 과거 Source/Static PASS를 현재 전체 PASS로 승계하지 않는다. 개발 시작 시 이 9개 실패를 Root Cause 묶음으로 처리한다.

## 4. Latest local integration one-line command

```powershell
$log="$env:USERPROFILE\Downloads\gradle-problems.txt"; $start=Get-Date; ./gradlew clean build --continue --stacktrace 2>&1 | Tee-Object -FilePath $log; $code=$LASTEXITCODE; $failed=@(Select-String -Path $log -Pattern '^> Task .* FAILED$'); Write-Host "`n========== FINAL REPORT =========="; Write-Host "Result       : $(if($code -eq 0){'PASS'}else{'FAIL'})"; Write-Host "ExitCode     : $code"; Write-Host "Failed Tasks : $($failed.Count)"; if($failed.Count -gt 0){$failed | Select-Object -First 20 | ForEach-Object { Write-Host "  $($_.Line)" }}; Write-Host "Started      : $start"; Write-Host "Finished     : $(Get-Date)"; Write-Host "Log          : $([IO.Path]::GetFullPath($log))"; Write-Host "=================================="
```

- Log: `$env:USERPROFILE\Downloads\gradle-problems.txt`
- 정상 기대 결과: `Result : PASS`, `ExitCode : 0`, `Failed Tasks : 0`, Gradle `BUILD SUCCESSFUL`
- 실패 시 다음 세션/개발 GPT에 전달할 파일: `gradle-problems.txt`
- 명령은 `Tee-Object`를 사용하므로 현재 Gradle Task가 콘솔에 계속 출력되고 로그에도 동시에 저장된다.

## 5. Development continuation rule

사용자가 전체 개발 시작을 지시하면 Final Target/Current Work Request/최신 Source/위 9개 실패를 함께 분석해 개발 목록·영향범위·우선순위·작업순서를 먼저 정리한다. QA/추가 Steering/새 로컬 로그는 기존 Backlog에 통합해 중복 개발을 피한다.

## 6. Completion rule

Source/API/SQL/Config/Frontend/Generator/Test/Evidence를 필요한 범위에서 함께 닫고, 실패는 수정 후 재검증한다. Live DB3, Multi-WAS/process-kill/recovery, Browser E2E 등 미실행 항목은 `미검증`으로 유지한다. 사용자 승인 없는 commit/push/branch/reset/restore/stash/clean/history rewrite는 하지 않는다.
