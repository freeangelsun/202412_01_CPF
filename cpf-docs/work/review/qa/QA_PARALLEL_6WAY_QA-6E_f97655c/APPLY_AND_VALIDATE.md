# Overlay 적용 및 저비용 검증

## 적용 한 줄
```powershell
$z=Get-ChildItem "$env:USERPROFILE\Downloads" -File -Filter "CPF_QA_QA-6E_f97655c_ACTUAL_ROOT_OVERLAY*.zip" | Sort-Object LastWriteTime -Descending | Select-Object -First 1; if(-not $z){throw "Overlay ZIP을 찾을 수 없습니다."}; Expand-Archive -LiteralPath $z.FullName -DestinationPath (Get-Location).Path -Force
```

## 저비용 검증 한 줄
```powershell
python .\cpf-docs\work\review\qa\QA_PARALLEL_6WAY_QA-6E_f97655c\verify_package.py -Root . -ExpectedHead f97655c1299936a1101bc3ec10239265ec3b502e
```

## Git Status 한 줄
```powershell
git diff --check; git -c core.quotepath=false status --short --branch
```

정리 대상 없음.
