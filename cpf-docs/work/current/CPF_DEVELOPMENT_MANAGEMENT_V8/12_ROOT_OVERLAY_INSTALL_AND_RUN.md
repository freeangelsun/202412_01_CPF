# Root Overlay 설치·실행

## 적용

ZIP을 Repository Root에 풀면 다음 경로만 추가한다.

- `cpf-docs/work/current/CPF_DEVELOPMENT_MANAGEMENT_V8/**`
- `cpf-tools/scripts/development-management/**`
- `cpf-tools/scripts/tests/development_management/**`

Commit·Push·삭제는 자동 수행하지 않는다.

## 사전 확인

```powershell
git fetch origin
git rev-parse origin/master
# 기대: faedf43a7baffdad456bf40f8e46d622db9cfc76
git status --short
```

Working Tree가 비어 있지 않다면 기존 변경을 덮어쓰지 말고 Overlay 충돌 파일을 먼저 비교한다.

## 실행

```powershell
python -m unittest discover -s cpf-tools/scripts/tests/development_management -p "test_*.py" -v
powershell -ExecutionPolicy Bypass -File cpf-tools/scripts/development-management/initialize-development-management.ps1
```

## 성공 기대

- Unit Test PASS
- Split Part SHA/행 수 PASS
- Requirement 30,558, Scenario 40,763
- Primary 미배정 0
- Static Management Validator PASS
