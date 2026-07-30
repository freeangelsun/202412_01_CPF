# CPF 브로셔형 README·전문 가이드 적용 안내

## 기준

- 기준 Branch: `master`
- 기준 Commit: `b7c6146e952c10b885952fa2bc6b6786f4611d86` (`20260730_10`)
- 적용 방식: Repository Root 상대경로 덮어쓰기

## 포함 범위

- 브로셔형 Root `README.md`
- Desktop·Mobile 전용 README 시각 자료와 편집용 SVG
- Hero 바로 아래 CPF 전체 구조도
- 핵심 가치와 제품 생명주기 브로셔 패널
- 문서 홈과 25개 상세 가이드
- 모든 상세 가이드의 문서 계약, Owner·Consumer·완료 판정
- 실제 Source·Controller·Port·Config·SQL·Test 추적 시작점
- 정상·오류·부분 실패·복구·Rollback·Evidence 실행 절차
- 최신 Gateway 대상 경로·Canonical 제어 서명·분산 Nonce·서비스 호출 Attempt·감사된 로그 반출·FILE_PROCESS·서명 검증·V81 DB 개발 내용

## 적용

1. ZIP을 임시 폴더에 푼다.
2. 압축을 푼 내용 전체를 CPF Repository Root에 복사한다.
3. 같은 경로의 파일은 덮어쓴다.
4. 검증 Script를 실행한다.
5. `git diff --check`, `git status --short`, `git diff --stat`로 변경 범위를 확인한다.
6. README를 GitHub Desktop 폭과 Mobile 폭에서 직접 확인한다.
7. 사용자가 검토한 뒤 Commit·Push 여부를 결정한다.

## 검증

```powershell
pwsh -NoProfile -ExecutionPolicy Bypass -File .\cpf-docs\work\overlay\20260730-readme-guides\verify-readme-guide-package.ps1
pwsh -NoProfile -ExecutionPolicy Bypass -File .\cpf-tools\scripts\check-document-links.ps1
git diff --check
git status --short
git diff --stat
```

정상 정적 검증 Summary:

```text
문서: 27
시각 자료: 40
README picture: 10 이상
```

## 검토 우선순위

1. README 첫 화면: Hero → CPF 전체 구조 순서
2. Mobile에서 전체 구조·가치·제품 생명주기 그림의 글자 크기
3. 가이드의 `## 0. 문서 계약`과 실제 Owner 일치 여부
4. 가이드 부록 Z의 Source 경로와 최신 Master 일치 여부
5. API·상태 Code·설정 Key가 OpenAPI·Source와 같은지
6. 실행하지 않은 검증이 성공으로 기록되지 않았는지

## 삭제 정책

개발이 진행 중이므로 Overlay, Work, Review, Handover와 Evidence를 자동 삭제하지 않는다. 실제 개발 담당자가 정본 역할과 Consumer를 확인하고 사용자가 승인한 경우에만 별도 작업으로 정리한다.

`delete-obsolete-document-artifacts.ps1`은 안내용이며 기본 실행 시 파일을 삭제하지 않는다.

## Git

이 패키지는 Commit, Push, Branch와 PR을 생성하지 않는다.
