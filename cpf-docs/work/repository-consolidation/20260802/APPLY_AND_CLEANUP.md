# 적용·정리 절차

## 1. Overlay 적용

ZIP을 Repository Root에 그대로 푼다.

이 패키지는 신규 Governance·Review 파일만 추가한다. 활성 Codex 파일과 Product Source를 덮어쓰지 않는다.

## 2. 충돌 확인

```powershell
git status --short
```

Codex 결과와 병합 후 `PROTECTED_PATHS.txt` 영역에 이 Overlay가 만든 변경이 없어야 한다.

## 3. 삭제 Preview

```powershell
$manifest='.\cpf-docs\work\repository-consolidation\20260802\DELETE_MANIFEST.txt'; Get-Content -LiteralPath $manifest | Where-Object { $_ -and -not $_.StartsWith('#') }
```

## 4. 사용자 승인 후 삭제

`DELETE_COMMAND.txt`의 한 줄 명령을 Repository Root에서 실행한다.

- Manifest의 정확한 파일만 삭제
- Wildcard 삭제 없음
- 후보 중 수정 파일이 하나라도 있으면 삭제 전 전체 중단
- 없는 파일은 경고만 출력

## 5. 삭제 후 확인

```powershell
git status --short
git diff --name-status
```

문서 Link Gate와 Current Primary 참조 검증은 Codex 결과 병합 후 실행한다.

## 6. 금지

```text
git clean
git reset --hard
git restore .
git stash
```
