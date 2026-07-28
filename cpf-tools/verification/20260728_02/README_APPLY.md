# CPF 20260728_02 최종 보완 Overlay 적용 안내

## 1. 기준

- Repository: `https://github.com/freeangelsun/202412_01_CPF`
- Branch: `master`
- 적용 기준 Commit: `ecaddd581a88ede22b63116effd61313744b3fbe`
- Package 성격: `20260728_01` 중간 구현 이후 잔여 구현 보완 Root Overlay
- Commit/Push/Branch 생성: 수행하지 않음

이 Overlay는 기준 Commit과 정확히 일치하는 작업트리에 적용한다. 다른 Commit에 억지로 적용하지 않는다.

## 2. 한 줄 적용 명령

프로젝트 경로가 `C:\dev\projects\jck\202412_01_CPF`이고 ZIP이 Downloads에 있을 때:

```powershell
$z="$HOME\Downloads\CPF_20260728_02_FINAL_COMPLETION_ROOT_OVERLAY.zip"; $t="$env:TEMP\CPF_20260728_02_FINAL"; Remove-Item $t -Recurse -Force -ErrorAction SilentlyContinue; Expand-Archive $z $t -Force; pwsh -ExecutionPolicy Bypass -File "$t\cpf-tools\scripts\apply-20260728-02-final-completion.ps1" -ProjectRoot "C:\dev\projects\jck\202412_01_CPF"
```

적용 스크립트는 다음을 순서대로 수행한다.

1. Git Repository와 현재 HEAD 확인
2. 기준 Commit 일치 확인
3. 작업트리 Clean 확인
4. Overlay 정적 검증
5. Root 상대경로 그대로 복사
6. 이전 중간 문서 정리
7. 적용 후 정적 검증
8. Git 변경 목록 출력

## 3. 적용 후 필수 검증

```powershell
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\verify-20260728-02-final-completion.ps1 -ProjectRoot "C:\dev\projects\jck\202412_01_CPF"
```

전체 Gradle 검증은 Java 25와 Gradle 9.1 환경에서 실행한다.

```powershell
.\gradlew.bat clean test assemble --no-daemon
```

## 4. 주의

- 스크립트는 Commit, Push, Branch를 생성하지 않는다.
- `-AllowDirty`는 충돌 위험을 사용자가 명확히 감수할 때만 사용한다.
- 실DB, Browser, 다중 인스턴스 검증은 `CPF_CODEX_FINAL_RUNTIME_VALIDATION_REQUEST_20260728_02.md`를 따른다.

## 5. Package 무결성

ZIP 내부 `cpf-tools/verification/20260728_02/ROOT_OVERLAY_FILES.txt`와 `ROOT_OVERLAY_SHA256.txt`에서 Root Overlay 파일 목록과 개별 SHA-256을 확인할 수 있다. 배포 ZIP 자체 SHA-256은 함께 제공되는 `.sha256` 파일을 기준으로 확인한다.
