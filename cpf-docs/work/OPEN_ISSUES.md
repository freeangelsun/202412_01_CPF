# OPEN ISSUES — Runtime / Exact-SHA Closure

> 입력 baseline provenance: `4b6f96796c3bf26b1c3324cc4d9b701bd9415acd`  
> 결과 Content SHA-1: `470ce244d05cdd2674385eb743630e2537f2963c`  
> 결과 Content SHA-256: `f049bf01a59cf57bc823ef59656516c867db9cab2aed6262abc26c4d840d2618`  
> 현재 결과는 미커밋 Overlay이며 QA 최종 완료가 아니다.

## 개발 GPT에서 닫힌 범위

- NXT3 **22/22 PASS**.
- Testing Tools **366 PASS / 22 SKIP / FAIL 0**.
- DB **86/86**, DB verification **75/75**.
- Generator **27 PASS / 10 SKIP**.
- Runtime/Security/Release/OpenAPI **108 PASS / 2 SKIP**.
- Verification **45/45**, Docker fixture **6/6**.
- Gradle logical project tree, Starter truth, dependency/owner/admin/supply-chain/zero-footprint gates PASS.
- 사용자 Windows root projection path gate PASS(max full path **213/240**), 상대경로 160 초과 26건 warning 유지.
- QA-B3 **22/25 개발 GPT 완료**, 완료 Finding은 개별 exact command/Evidence 보유.


- 사용자 적용 후 Full Source 재검수에서 Evidence `*.log` 전달 누락, ADM/BZA tracked OpenAPI drift, 보호 deliverables 날짜 경로 false FAIL을 발견·수정하고 정적 회귀 PASS까지 재검수했다.
- `CPF_LOCAL_VALIDATION_20260816_124024.zip`을 분석해 [01]~[05] PASS와 [06] NXT3 단계의 Shell/log-directory + generated-cache 오탐 결함을 수정했다. 수정본 FullLocal 재실행 결과는 아직 없음.

## 아직 OPEN

1. `QA-B3-008`: post-commit exact Git SHA. 현재 Runtime/Evidence identity는 Git-independent content digest로 고정했으며 Git 조회는 수행하지 않음.
2. `QA-B3-010`: Java25 + Live DB3 + Cache/Kafka + Process Kill/UNKNOWN/Reconcile + Browser + Topology + Security + Deployment + Performance FullLocal 실제 실행.
3. `QA-B3-011`: Windows fresh extract + Java25 Gradle/Runtime 실제 실행. 정적 실제-root projection만 PASS.
4. QA 최종 판정/Canonical Requirement별 Runtime completion은 QA 권한 영역이며 개발 GPT가 완료 처리하지 않음.
5. 수정본 FullLocal에서 FileLog↔DB↔ADM transactionId/traceId correlation, recovery loss=0, WAS fatal log=0, secret raw leak=0 Runtime Evidence 확인.

## 다음 검증

사용자 Windows에서 아래 FullLocal 한 번으로 가능한 범위를 최대 수집한다.

```powershell
pwsh -NoProfile -ExecutionPolicy Bypass -File .\cpf-tools\verification\tools\run-cpf-local-full-validation.ps1 -ResourceProfile local -OutputRoot "$HOME\Downloads" -FullLocal
```

첫 FAIL에서 멈추지 않고 독립 단계를 계속 수행하며 최종 ZIP을 생성한다. FAIL이 하나라도 남으면 strict exit non-zero다.

## 삭제

이번 Overlay의 실제 원본 대비 삭제 대상은 0건이다. 기존 `CPF_DELETE_MANIFEST.csv`는 history/governance 자료로 보존하며 사용자 승인 없는 자동 삭제는 하지 않는다. `cpf-tools/build/**`는 제품 Source다.
