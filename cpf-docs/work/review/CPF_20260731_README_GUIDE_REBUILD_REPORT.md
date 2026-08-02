# CPF README·Guide 정본 재구축 결과

- 기준 Repository: `freeangelsun/202412_01_CPF`
- 기준 Branch: `master`
- 기준 SHA: `d31bd127aa12bb9368933216642a5a9d25bd0bfd`
- 작성일: `2026-07-31`
- Git Commit·Push·Branch·Tag·PR: 수행하지 않음

## 1. 결과

기존 Guide가 독자·용도·개념·운영 절차를 여러 파일에 중복 분산하던 구조를 다음 8개 정본으로 통합했다.

- `cpf-docs/guides/00_프레임워크안내.md`
- `cpf-docs/guides/01_개발자매뉴얼.md`
- `cpf-docs/guides/02_배치개발매뉴얼.md`
- `cpf-docs/guides/03_ADM개발자매뉴얼.md`
- `cpf-docs/guides/04_ADM운영자매뉴얼.md`
- `cpf-docs/guides/05_플랫폼운영매뉴얼.md`
- `cpf-docs/guides/90_BZA매뉴얼.md`
- `cpf-docs/guides/91_게이트웨이매뉴얼.md`

## 2. 핵심 정정

- README의 `업무 시스템을 위한`을 `시스템 구축과 운영을 위한`으로 정정했다.
- `실패를 숨기지 않고 복구 가능한 상태로 남깁니다`를 `처리 상태를 잃지 않고 안전하게 이어갑니다`로 정정했다.
- 개발자, Batch 개발자, ADM 개발자, ADM 운영자, 플랫폼 운영자의 독자와 작업을 분리했다.
- BZA와 Gateway를 선택 제품 매뉴얼로 분리했다.
- QA32 Spring Batch Primary Engine과 OSS Consumer 전수 이관·Legacy 제거 원칙을 반영했다.
- Transaction, Kafka, File, 외부 API, Security, DB, Runtime, Restart, Unknown, Evidence를 단계별 절차로 보강했다.
- 11개 Guide Diagram과 README Guide Map을 추가·갱신했다.

## 3. 기존 파일 삭제

정확한 삭제 대상은 `cpf-docs/work/manifest/CPF_GUIDE_REBUILD_DELETE_MANIFEST.txt`에 있다.

한 줄 적용:

```powershell
pwsh -NoProfile -File .\cpf-tools\scripts\apply-cpf-guide-cleanup.ps1 -Root .
```

Script는 와일드카드를 사용하지 않고 Manifest에 있는 정확한 상대경로만 삭제한 뒤 새 정본·이미지·구형 링크·금지 문구를 검증한다.

## 4. 검증

```powershell
pwsh -NoProfile -File .\cpf-tools\scripts\verify-cpf-guide-system.ps1 -Root . -RequireLegacyRemoved
pwsh -NoProfile -ExecutionPolicy Bypass -File .\cpf-tools\scripts\check-document-links.ps1
git diff --check
git status --short
```

현재 생성 환경에는 PowerShell이 없어 PowerShell Script 자체 실행은 미검증이다. 동일 규칙의 Python 검증으로 파일 존재, Markdown 상대 Link, 이미지, 구형 문구, Manifest 경로와 ZIP 구조를 검사한다.

## 5. 완료 판정 주의

문서가 QA32 목표 구조를 상세히 설명하더라도 실제 Source Consumer 이관·Legacy 제거·Runtime Failure/Recovery Evidence가 완료되지 않았다면 제품 기능을 완료로 판정하지 않는다.


## R1 링크 정합성 수정

- R0 패키지에서 `cpf-tools/README.md`가 Overlay에 포함되지 않아, 삭제된 `CPF_TOOLS_GUIDE.md` 링크가 Repository에 남는 결함을 수정했다.
- `cpf-tools/README.md`를 `01_개발자매뉴얼.md`와 `05_플랫폼운영매뉴얼.md`로 이관했다.
- `verify-cpf-guide-system.ps1`의 필수 문서에 `cpf-tools/README.md`를 추가하고 삭제 Manifest의 기존 Guide 이름 재참조를 검사하도록 보강했다.
- PowerShell 실행은 Linux 생성 환경에 `pwsh`가 없어 미검증이며, Windows Repository에서 적용 Script 재실행이 필요하다.
