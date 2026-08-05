# CPF Development Management V8 — Review Index

## 기준

- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- Package 생성 기준: `faedf43a7baffdad456bf40f8e46d622db9cfc76` (`04_06`)
- 마지막 확인 `origin/master`: `be03808e0bed7aa02c2978c5aa73b315359bbead` (`04_07`)
- 최상위 정본: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
- 관리 정본: `cpf-docs/work/current/CPF_DEVELOPMENT_MANAGEMENT_V8/`
- 상태: **관리 패키지 Push 확인 / 30,558·40,763 전수 Bootstrap 미실행**
- Repository 최상위 신규 항목: **없음**

## 이번 보정

1. 삭제된 루트 README를 Package/Change Manifest에서 제거
2. V8 신규 세션 인수인계 추가
3. SHA와 Commit 메시지 정합성 보정
4. Repository 최상위 신규 파일·디렉터리 금지 명문화
5. 동일 Campaign/Revision 덮어쓰기 금지
6. Session별 고유 결과 경로와 Artifact Manifest 도입
7. 기존 제품 파일 Baseline 충돌 시 덮어쓰기 금지
8. 세션 인수인계의 exact-path 정리 명령 의무화
9. 제품 필수 Source와 Evidence는 자동 정리 명령에서 제외
10. 관리 Script의 불변 출력 경로 검사와 Test 추가

## 핵심 관리 동작

- 169 Canonical Requirement를 유지하면서 775 Work Package로 실행
- 30,558 Requirement와 40,763 Scenario를 Repository Split Part에서 전수 Mapping
- 28 Baseline Stabilization과 24 Gap 항목 관리
- `작업 대상·재개발 대상·재검수 대상`만 개발 요청에 포함
- `완료 스킵·해당 없음 스킵`은 다음 개발 요청에서 제외
- QA Reopen Feed가 들어오면 관련 항목만 다시 포함
- 중앙 State는 Session이 직접 수정하지 않고 Merge Script가 갱신
- Campaign·Session 산출물은 불변 고유 경로에 보존

## 실행 순서

```powershell
git fetch origin
git rev-parse origin/master
git status --short

powershell -ExecutionPolicy Bypass -File cpf-tools/scripts/development-management/initialize-development-management.ps1

powershell -ExecutionPolicy Bypass -File cpf-tools/scripts/development-management/generate-development-requests.ps1 `
  -CampaignId DEV-YYYYMMDD-R01 `
  -AssignmentRevision 1 `
  -MaxItemsPerSession 8
```

## 판정 경계

- `FULL_ASSIGNMENT_VALIDATION.json=PASS` 전에는 Requirement·Scenario 전수 배정 완료가 아니다.
- `완료 스킵`은 개발 GPT 반복 요청 제외 상태이며 QA 통과가 아니다.
- GitHub Actions/Combined Status가 없으므로 `04_07`의 Build·Runtime 성공을 주장하지 않는다.
- 제품 완료는 최신 통합 Git에 대한 QA 통과로만 판정한다.
