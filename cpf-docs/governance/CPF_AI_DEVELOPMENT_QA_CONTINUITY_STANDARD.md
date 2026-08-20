# CPF AI 개발·QA 연속 실행 표준

> 역할: Developer GPT/Codex/QA의 **현재 실행 방식**만 정의한다. 제품 Architecture/Requirement는 `CPF_FINAL_TARGET_REQUIREMENTS.md`를 유일한 상위 정본으로 사용한다.

## 1. Source Identity

- 공식 Repository 작업이면 최신 승인 baseline과 실제 Local Working Tree를 함께 확인한다.
- uncommitted/untracked Source가 있으면 실제 Local Working Tree가 실행 대상이며 commit 기준 Source로 덮어쓰지 않는다.
- ZIP 작업이면 ZIP SHA-256과 Content Tree를 기록한다.
- 과거 대화의 완료율/PASS/Evidence를 새 Source의 PASS로 자동 승계하지 않는다.

## 2. 작업 시작

사용자가 명시적으로 개발 시작을 요청하기 전에는 자료 취합·분석만 한다. 개발 시작 시:

1. Final Target과 Current Work Request를 읽는다.
2. 실제 Source/SQL/API/Test/Config/Frontend/Generator를 확인한다.
3. 개발 목록, 영향범위, 우선순위, 작업순서를 정리한다.
4. 사용자 리뷰가 필요한 최초 계획을 제시한 뒤 개발을 시작한다.

새 QA/로컬 테스트/Steering은 기존 요구에 병합하여 중복 개발을 피한다. Architecture가 잘못됐거나 더 나은 방향이 보이면 Source에 맞추지 말고 Target 품질을 기준으로 의견을 제시한다.

## 3. 지속 실행

개발 시작 후 특별한 중지/변경 요청이나 안전상 필수 판단이 없으면 구현→저비용 Gate→실패 집계→보정→Build/Test/Runtime→자체검수→Evidence→패키징까지 연속 진행한다.

진행 보고는 중단점이 아니다. 화면에는 전체 기준 진행률, 현재 작업, 완료 항목, 잔여 항목을 주기적으로 표시한다.

## 4. 오류 처리

첫 오류 하나만 고치지 않는다.

1. 가능한 독립 Gate를 계속 실행한다.
2. 실패를 Root Cause별로 묶는다.
3. Repository 전체에서 같은 패턴의 잠복 결함을 찾는다.
4. Source/Test/Generator/Catalog/Verifier/문서를 일괄 보정한다.
5. 최소 검증 묶음부터 재실행하고 전체 회귀로 확장한다.

Verifier가 대상 0건, stale path, old policy를 검사하면서 PASS하는 False Green을 별도 결함으로 본다.

## 5. Local 통합 테스트 UX

장시간 명령은 콘솔에 진행상황을 보이면서 로그에도 저장한다. `Out-File`만 사용해 화면을 완전히 무응답으로 만드는 명령을 기본으로 제공하지 않는다.

Gradle 기본 예:

```powershell
$log="$env:USERPROFILE\Downloads\gradle-problems.txt"; $start=Get-Date; ./gradlew clean build --continue --stacktrace 2>&1 | Tee-Object -FilePath $log; $code=$LASTEXITCODE; Write-Host "`n========== FINAL REPORT =========="; Write-Host "Result   : $(if($code -eq 0){'PASS'}else{'FAIL'})"; Write-Host "ExitCode : $code"; Write-Host "Started  : $start"; Write-Host "Finished : $(Get-Date)"; Write-Host "Log      : $log"; Write-Host "=================================="
```

세션 인수인계에는 최신 통합 테스트 한 줄, 로그 위치, 정상 기대 결과, 실패 시 전달할 로그 파일명을 포함한다.

## 6. Git / 삭제 안전

사용자 승인 없이 commit/push/branch/tag/reset/restore/stash/clean/history 변경을 하지 않는다. 삭제는 Root-relative Delete Manifest로 관리하고 보호경로와 제품 Source를 broad delete하지 않는다.

## 7. 완료

완료 판정은 Final Target의 공통 완료축을 따른다. 미실행 Runtime은 `미검증`이며 PASS가 아니다. 최종 패키지에는 변경/검증/Gap/Delete/Manifest/Hash와 다음 검수 요청을 포함한다.
