# CPF 브로셔형 README·상세 가이드 적용 안내

## 기준

- 기준 Branch: `master`
- 기준 Commit: `693cc77bde4c830b78ca1408dec7e34ef84cd11d` (`20260730_06`)
- 적용 방식: Repository Root 상대경로 덮어쓰기

## 포함 범위

- 브로셔형 Root `README.md`
- Desktop·Mobile 대응 README 이미지와 편집용 SVG
- 문서 홈
- 구조·배포 가이드와 용어·계약 참조
- 기존 상세 가이드 전면 보완본
- 최신 Gateway·서비스 등록부·로그 보호·Batch 실행 제어 개발 내용 반영

## 적용

1. ZIP을 임시 폴더에 푼다.
2. 압축을 푼 내용 전체를 CPF Repository Root에 복사한다.
3. 같은 경로의 파일은 덮어쓴다.
4. 검증 Script를 실행한다.
5. `git diff --check`, `git status --short`, `git diff --stat`로 변경 범위를 확인한다.
6. 사용자가 직접 검토한 뒤 Commit한다.

## 검증

```powershell
pwsh -NoProfile -ExecutionPolicy Bypass -File .\cpf-docs\work\overlay\20260730-readme-guides\verify-readme-guide-package.ps1
pwsh -NoProfile -ExecutionPolicy Bypass -File .\cpf-tools\scripts\check-document-links.ps1
git diff --check
git status --short
git diff --stat
```

## 삭제 정책

현재 개발이 진행 중이므로 기존 Overlay, Work, Review, Handover와 Evidence 파일은 자동 삭제하지 않는다. 실제 개발 담당자가 Consumer와 정본 역할을 확인한 뒤 불필요하다고 판정한 파일만 별도 승인 후 정리한다.

`delete-obsolete-document-artifacts.ps1`은 이번 패키지에서 안전 안내용으로 변경했으며 기본 실행 시 파일을 삭제하지 않는다.

## Git

이 패키지는 Commit, Push, Branch와 PR을 생성하지 않는다.
