# CPF 20260728_01 Root Overlay 적용

이 디렉터리는 프로젝트 Root 상대경로를 유지한 Overlay의 검증 기록이다.

1. ZIP 내용을 `C:\dev\projects\jck\202412_01_CPF`에 덮어쓴다.
2. `git status --short`와 `git diff --stat`을 확인한다.
3. 아래 명령을 실행한다.

```powershell
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\apply-20260728-enterprise-qa-closing.ps1 -Root "C:\dev\projects\jck\202412_01_CPF"
```

4. `CODEX_FINAL_VERIFICATION_AND_REPAIR_REQUEST.md` 순서로 전체 검증/수정을 수행한다.
5. 사용자 승인 전 commit/push/branch를 생성하지 않는다.

Overlay는 파일 삭제를 ZIP extraction만으로 표현할 수 없으므로 stale SQL 삭제는 `cleanup-20260728-enterprise-qa.ps1`이 담당한다.
