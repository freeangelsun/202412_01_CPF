# QA Rework Request

## QA-B-RW-001 — Canonical ledger build 및 QA-B partition 확정

**환경/권한:** complete clean clone, `master` exact SHA `ff8c596042583eba665a5475b1c3e43d2ef39ba6`, 모든 split Part, Python, Git read 권한.

```powershell
git fetch origin
git branch --show-current
git rev-parse HEAD
git rev-parse origin/master
git status --short
python .\cpf-tools\scripts\build-cpf-full-qa-ledgers.py --root . --identity-mode git-clean --expected-sha ff8c596042583eba665a5475b1c3e43d2ef39ba6 --generated-at "<KST ISO-8601>" --updated-by "QA-B" --json-output .\cpf-docs\evidence\qa\qa-b-full-ledger-build.json
python .\cpf-tools\scripts\validate-cpf-full-qa-ledgers.py --requirements .\cpf-docs\work\current\REQUIREMENT_STATUS.csv --scenarios .\cpf-docs\work\current\SCENARIO_STATUS.csv --expected-requirements 30558 --expected-scenarios 40763 --expected-sha ff8c596042583eba665a5475b1c3e43d2ef39ba6 --json-output .\cpf-docs\evidence\qa\qa-b-ledger-validation.json
python .\cpf-tools\scripts\validate-qa-partition-coverage.py --plan .\cpf-docs\\work\\review\\qa\\QA_PARALLEL_B_FF8C596_COMPLETION\\QA_PARTITION_PLAN.csv --expected-total 30558 --expected-sha ff8c596042583eba665a5475b1c3e43d2ef39ba6 --requirement-ledger .\cpf-docs\work\current\REQUIREMENT_STATUS.csv --scenario-ledger .\cpf-docs\work\current\SCENARIO_STATUS.csv --json-output .\cpf-docs\evidence\qa\qa-b-partition-validation.json
```

**성공:** 30,558/40,763 exact coverage, 중복·고아 Scenario·WP/hash 오류 0, QA-B 9,962 logical rows와 연결 Scenario가 정확히 한 partition에 배정됨.
**실패:** missing/duplicate row, stale SHA, hash mismatch, gap/overlap, orphan Scenario 또는 non-zero exit.

## QA-B-RW-002 — BAT UNKNOWN Consumer/Runtime

Java 25 Gradle module test, 실제 JDBC fault injection, HTTP error mapper 및 reconcile test를 실행합니다. durable `UNKNOWN`, 표준 UnknownResultException, blind retry 차단, cause/suppressed 보존, Owner side effect 최대 1회를 모두 증명해야 합니다.

## QA-B-RW-003 — DB Lifecycle

세 Vendor disposable DB와 DDL/DML 권한으로 FreshInstall/Upgrade/RollbackReapply를 실행합니다. MariaDB는 non-empty R100 거부 → export/reconcile/delete → R100 재시도 → V100 reapply까지 원본 SQL client 로그와 object/row count를 보존합니다.

## QA-B-RW-004 — 전체 제품 Gate

승인된 통합 후 새 exact commit에서 다음을 실행합니다.

```powershell
pwsh -NoProfile -File .\cpf-tools\scripts\run-cpf-full-qa-validation.ps1 -ExpectedHead <NEW_EXACT_SHA> -Root .
```

한 단계라도 실패하거나 dirty/stale SHA, 행별 Evidence 누락, open issue가 남으면 제품 QA는 `미통과`입니다.
