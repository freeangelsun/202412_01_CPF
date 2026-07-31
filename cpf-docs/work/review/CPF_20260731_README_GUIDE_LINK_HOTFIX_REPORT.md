# CPF README·Guide 링크 Hotfix R1

## 원인

R0 정리 패키지가 기존 `CPF_TOOLS_GUIDE.md`를 삭제했지만 `cpf-tools/README.md`를 Overlay에 포함하지 않아 문서 링크 Gate가 실패했다.

## 수정

- `cpf-tools/README.md`의 구형 Guide 링크를 `01_개발자매뉴얼.md`, `05_플랫폼운영매뉴얼.md`로 교체
- Guide 검증 대상에 `cpf-tools/README.md` 추가
- 삭제 Manifest에 포함된 Guide 이름을 새 정본이 다시 참조하지 않는지 검사 추가

## 적용 후 명령

```powershell
pwsh -NoProfile -ExecutionPolicy Bypass -File .\cpf-tools\scripts\apply-cpf-guide-cleanup.ps1 -Root .
```

## 판정

- 정적 패키지·경로 검증: 완료
- Windows PowerShell 실제 실행: 미검증
- Git Commit·Push·Branch·Tag·PR: 수행하지 않음
