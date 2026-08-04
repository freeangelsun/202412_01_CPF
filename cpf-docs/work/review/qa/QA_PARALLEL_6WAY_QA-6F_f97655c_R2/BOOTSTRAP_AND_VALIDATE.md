# APPLY AND VALIDATE — QA-6F R2

## Apply

```powershell
$z=Get-ChildItem "$env:USERPROFILE\Downloads" -File -Filter "CPF_QA-6F_f97655c_R2_REQUIREMENT_BY_REQUIREMENT_QA_ROOT_OVERLAY*.zip" | Sort-Object LastWriteTime -Descending | Select-Object -First 1; if(-not $z){throw "Overlay ZIP을 찾을 수 없습니다."}; Expand-Archive -LiteralPath $z.FullName -DestinationPath (Get-Location).Path -Force
```

## Low-cost validation

```powershell
python -m unittest -v cpf-tools/scripts/tests/test_seed_bundle_plan_closure.py cpf-tools/scripts/tests/test_seed_bundle_runtime_contract.py cpf-tools/scripts/tests/test_oracle_sqlplus_secret_transport.py; git diff --check
```

## Git status

```powershell
git -c core.quotepath=false status --short --branch
```

## Cleanup

```powershell
Write-Host '정리 대상 없음: 이 Overlay는 Repository 임시파일을 생성하지 않습니다.'
```

- 기준 SHA: `f97655c1299936a1101bc3ec10239265ec3b502e`
- Commit/Push/Branch/Tag/PR/Release는 수행하지 않는다.
- 실제 Oracle/PostgreSQL/MariaDB lifecycle은 별도 환경에서 실행하고 성공 Evidence가 없으면 미검증으로 유지한다.
