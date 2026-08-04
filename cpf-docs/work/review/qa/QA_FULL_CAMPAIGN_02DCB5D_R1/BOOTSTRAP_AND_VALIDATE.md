# Current 원장 Bootstrap 및 전체 검증

Repository Root에서 Overlay 적용 후 실행한다.

```powershell
python .\cpf-tools\scripts\build-cpf-full-qa-ledgers.py --root . --expected-sha 02dcb5d45646469f4950cf43c371706e00458616 --generated-at "2026-08-04T05:03:39+09:00" --updated-by "QA GPT" --json-output .\cpf-docs\evidence\qa\full-ledger-build.json
```

Builder 결과 기대:

```text
requirements=30558
scenarios=40763
status=PASS
```

각 Requirement/Scenario를 logical execution order로 검수하며 Current 원장의 같은 행을 갱신한다. 제품 전체 검증은 모든 수정이 반영된 **새 exact SHA**를 넣어 실행한다.

```powershell
pwsh -NoProfile -File .\cpf-tools\scripts\run-cpf-full-qa-validation.ps1 -ExpectedHead <NEW_EXACT_SHA> -Root .
```

`FULL_QA_PRODUCT_PASS_71321` 이전 단계가 하나라도 실패하면 제품 QA 통과가 아니다.
