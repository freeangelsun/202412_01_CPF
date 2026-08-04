# Bootstrap and Validate

Committed 제품 통과 경로는 clean exact Git checkout만 허용합니다. Commit 전 Root Overlay 검증은 기반 Git HEAD가 `ff8c596042583eba665a5475b1c3e43d2ef39ba6`인 상태에서 아래 hashed Source Manifest를 사용합니다.

```powershell
python .\cpf-tools\scripts\build-cpf-full-qa-ledgers.py --root . --identity-mode source-manifest --expected-sha ff8c596042583eba665a5475b1c3e43d2ef39ba6 --source-manifest .\cpf-docs\\work\\review\\qa\\QA_PARALLEL_B_FF8C596_COMPLETION\\SOURCE_MANIFEST.csv --source-manifest-sha256 cac36a215b3d6b2281da827be180271705157e35ccdfad78a1fa851697db21ef --generated-at "<KST ISO-8601>" --updated-by "QA-B" --json-output .\cpf-docs\evidence\qa\qa-b-ledger-build.json
```

이 패키지는 reset, restore, stash, clean, commit, push, branch, tag, PR, release, repository 삭제를 수행하지 않습니다.
