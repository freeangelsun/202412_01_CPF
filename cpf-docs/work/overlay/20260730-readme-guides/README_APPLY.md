# CPF README·가이드 문서 패키지 적용 안내

## 1. 적용 방법

ZIP 파일의 내용은 CPF Repository Root 상대경로로 구성돼 있다.

1. ZIP을 임시 폴더에 푼다.
2. 압축을 푼 폴더의 `README.md`, `cpf-docs`, `cpf-tools`를 CPF Repository Root에 복사한다.
3. 같은 이름의 문서는 덮어쓴다.
4. 삭제 Script를 실행한다.
5. 검증 Script를 실행한다.
6. `git diff --check`, `git status --short`, `git diff --stat`로 변경 범위를 확인한다.
7. 사용자가 직접 검토한 후 Commit한다.

## 2. 문서 원칙

- README와 Guide는 모든 최종 제품 요건이 충족된 완성 제품 기준으로 작성한다.
- 구현 진행률, 미완료 상태와 세션 작업 이력은 Work/Review/Evidence에서 관리한다.
- 본문은 한글 중심이며, Class·Method·설정 Key·상태 Code 같은 식별자만 영문을 유지한다.
- 기존 Guide 파일은 가능한 같은 경로에서 덮어써 Link를 보존한다.
- 신규 Guide는 README 문서 안내에 연결한다.

## 3. 삭제

Archive 사본이 확인된 과거 Active 문서와 Root Overlay 잔재만 정리한다.

```powershell
pwsh -NoProfile -ExecutionPolicy Bypass -File .\cpf-docs\work\overlay\20260730-readme-guides\delete-obsolete-document-artifacts.ps1
```

Archive 사본이 없으면 삭제 Script는 중단한다.

## 4. 검증

```powershell
pwsh -NoProfile -ExecutionPolicy Bypass -File .\cpf-docs\work\overlay\20260730-readme-guides\verify-readme-guide-package.ps1
pwsh -NoProfile -ExecutionPolicy Bypass -File .\cpf-tools\scripts\check-document-links.ps1
git diff --check
git status --short
git diff --stat
```

## 5. 신규 Guide

- Gateway 운영
- 비동기·메시징·보상
- 관측·장애대응·복구
- 설정·Runtime 정책 배포
- 설치·업그레이드·되돌리기
- Test와 Evidence

## 6. Git

이 패키지는 Commit, Push, Branch와 PR을 생성하지 않는다.
