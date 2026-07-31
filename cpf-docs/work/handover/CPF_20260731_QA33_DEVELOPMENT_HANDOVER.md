# CPF QA33 개발 인수인계

- Base SHA: `21eb93c7a7110f593e7d2db725046acb6635e7dd`
- Branch: `master`
- Commit/Push: 수행하지 않음
- README/Guide/Asset: 변경 금지 범위 유지

Overlay 적용 직후 아래 순서로 실행한다.

```powershell
python cpf-tools/scripts/verify-cpf-qa33-request-integrity.py --root .
.\gradlew.bat projects --no-daemon --stacktrace
.\gradlew.bat help --no-daemon --stacktrace
.\gradlew.bat clean test --no-daemon --stacktrace
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\verify-cpf-qa32-runtime.ps1 -Root .
python cpf-tools/scripts/verify-cpf-qa33-result-coverage.py --root .
```

`--release`는 exact-SHA Runtime Evidence와 모든 Requirement/Scenario 완료가 실제로 충족되기 전 실행 성공으로 간주하지 않는다.
