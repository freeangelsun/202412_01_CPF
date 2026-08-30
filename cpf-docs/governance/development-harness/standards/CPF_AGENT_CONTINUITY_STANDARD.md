> Development Harness 내부 통합 표준. 이 파일은 독립 정본이 아니며 `CPF_DEVELOPMENT_HARNESS.md`의 통제를 받는다.

# CPF AI 개발·QA 연속 실행 표준

> 역할: Developer GPT/Codex/QA의 **현재 실행 방식**만 정의한다. 제품 Architecture/Requirement는 `../product/CPF_PRODUCT_ARCHITECTURE_AND_REQUIREMENTS.md`를 유일한 상위 정본으로 사용한다.

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

### 5.1 실행 명령과 prerequisite 판정

사용자에게 Test/Runtime/Build/Release 명령을 만들기 전에 **Current Source가 요구하는 prerequisite를 다시 확인**한다.

- Java/Node/npm/Python/PowerShell/Docker/DB/Browser 등의 required version/range는 과거 대화나 이전 세션의 숫자를 재사용하지 않는다.
- 사용자 PC의 actual version에 맞춰 Framework requirement나 verifier expected를 바꾸지 않는다.
- Current Source의 canonical bootstrap/verifier/toolchain/package metadata/lock/runtime script를 우선 조회하고 `required / actual / source`를 기록한다.
- mismatch이면 어떤 prerequisite gate에서 막혔는지 먼저 판정한다. 아직 진입하지 못한 DB3/Batch/Frontend 등 후속 Runtime을 FAIL/PASS로 추정하지 않는다.
- Host Tool은 exact patch/minor 숫자보다 `cpf-tools/verification/contracts/cpf-toolchain-compatibility.json`의 최소 기능/major compatibility를 우선한다. Project-owned Wrapper/Lock/Container pin과 Host prerequisite pin을 혼동하지 않는다.
- 환경 교정은 canonical bootstrap이 소유하는 경우 그 방식을 우선한다. 전역 설치·다운그레이드처럼 사용자 환경을 바꾸는 조치는 자동 기본값으로 만들지 않고 영향과 복구를 명시한다.
- 환경 교정 후에는 prerequisite gate 다음 단계만 부분 실행해 완료 처리하지 말고 원래의 최대강도 canonical command를 다시 실행한다.
- 명령 자체가 stale version을 고정해 사용자가 반복 수정해야 하는 구조를 만들지 않는다. Source가 이미 요구값을 제공하는 경우 그 값을 검증하는 canonical entrypoint를 사용한다.

## 6. Git / 삭제 안전

사용자 승인 없이 commit/push/branch/tag/reset/restore/stash/clean/history 변경을 하지 않는다. 삭제는 Root-relative Delete Manifest로 관리하고 보호경로와 제품 Source를 broad delete하지 않는다.

## 7. 완료

완료 판정은 Final Target의 공통 완료축을 따른다. 미실행 Runtime은 `미검증`이며 PASS가 아니다. 최종 패키지에는 변경/검증/Gap/Delete/Manifest/Hash와 다음 검수 요청을 포함한다.

## 8. 세션 시작 Preflight와 자동 Merge

모든 역할은 새 개발 전에 `CPF_WORK_ITEM_SESSION_MERGE_AND_REPORT_STANDARD.md`를 적용한다.

- 고유 sessionKey를 발급한다.
- `CPF_DEVELOPMENT_HARNESS.md`의 Current Merge Control State를 읽는다.
- `evidence/*/current/sessions/*/SESSION_MANIFEST.json` 전체를 검색한다.
- 미Merge/PARTIAL/CONFLICT 세션이 있으면 신규 개발보다 먼저 Work Item별 Report/Evidence를 검증·Merge한다.
- 같은 Work Item에 병렬 결과가 있으면 마지막 파일 우선으로 덮어쓰지 않는다.
- Merge 상태를 currentize한 뒤 Mandatory Pending/Conflict=0일 때만 신규 Work Item을 시작한다.
- 사용자가 매 세션마다 Merge를 별도로 지시해야 하는 구조로 운영하지 않는다.

세션 결과는 역할별 Evidence 경로에 남기며 `CURRENT_WORK_ITEM_REGISTRY.csv` 외 별도 작업대상 목록을 만들지 않는다.

