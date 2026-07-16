# CPF 최신 정본 preflight 기준 보강 적용 안내 — 2026-07-16

## 기준

- repository: `freeangelsun/202412_01_CPF`
- branch: `master`
- 직전 확인 commit: `dd77c10d6a0a63b2cd31c309c83614a60f39d683`

## 교체할 파일

1. `CPF_FINAL_TARGET_REQUIREMENTS.md`
2. `CPF_NEW_REQUEST.md`
3. `CPF_REVIEW_PROGRESS_COMPLETION_GUIDE.md`

이번 변경은 작업 절차와 정본 우선순위를 명확히 한 것이므로 report/gap/evidence 상태는 별도로 변경하지 않았다.

## 추가된 핵심 의무

- 정본은 단일 파일이며 계속 갱신되는 살아 있는 기준서
- 모든 작업 시작 전 최신 master의 정본 전체 재확인
- 시작 commit SHA와 정본 blob SHA 기록
- 일부 발췌·검색·기억·오래된 로컬 사본만으로 완료 판정 금지
- 작업 중 master/정본 갱신 시 완료 전에 재대조
- 정본 변경으로 stale해진 evidence 재판정
- 직접 확인하지 못한 경우 명시하고 완료 판정 금지
- 삭제된 01~05 분할 보조본 참조 금지

## push 전 확인

```bash
git diff --check
git grep "CPF_FINAL_TARGET_REQUIREMENTS_0"
git grep "최신 정본"
git diff --stat
```

## 권장 commit message

```text
20260716_05 최신 정본 preflight 및 갱신 영향 재검토 기준 보강
```
