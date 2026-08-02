# CPF QA38 Root Overlay 적용·검증

## 적용 조건

- Repository Root에서 실행
- Branch `master`
- Working Tree clean
- 현재 HEAD는 기준 SHA `dafe5c0e5260ea8149234e8ab2e75347e75338c1`의 후손
- 기준 SHA 이후 QA38 관리 경로와 충돌하는 Commit이 없어야 함

## 적용

Overlay ZIP을 임시 디렉터리에 풀고 다음 Script를 실행한다.

`cpf-tools/scripts/apply-qa38-root-overlay.ps1`

Script는 보호 경로를 검사하고, Overlay 파일을 복사하고, Delete Manifest 160개 파일을 실제 삭제하고, 비어 있는 폴더를 제거한 뒤 통합 검증을 실행한다.

## 검증

`cpf-tools/scripts/verify-qa38-starter-closure.ps1`

## Git 반영

적용 후 `git diff --check`와 `git status --short --branch`를 확인하고 사용자가 Commit·Push한다.
