# Test and Evidence

## 이 Artifact에서 실제 실행한 검증

- 기준 `origin/master` 조회: `faedf43a7baffdad456bf40f8e46d622db9cfc76` / `04_06`
- Static 관리 항목: 827 = Work Package 775 + Stabilization 28 + Gap 24
- Canonical Coverage: 169
- 개발 상태 Index/State ID 동일성
- Mandatory Dependency Cycle 검사
- Unit Test: `cpf-tools/scripts/tests/development_management`
- Package Manifest Hash 검산

## 실행하지 못한 검증

- 30,558 Requirement Part와 40,763 Scenario Part의 실제 전수 Mapping
- 현재 사용자 Local Working Tree 검사
- V8 적용 후 Fresh Clone/Runtime/DB Vendor/Frontend 실행

이유: Artifact 생성 환경의 GitHub Connector는 약 8MB Split Blob의 텍스트를 반환하지 않았고, Local Container는 GitHub DNS에 접근할 수 없었다. 따라서 전수 Mapping을 허위 PASS 처리하지 않고 Repository 실행 단계로 남겼다.

## Repository 재실행 명령

```powershell
python -m unittest discover -s cpf-tools/scripts/tests/development_management -p "test_*.py" -v
powershell -ExecutionPolicy Bypass -File cpf-tools/scripts/development-management/initialize-development-management.ps1
powershell -ExecutionPolicy Bypass -File cpf-tools/scripts/development-management/validate-development-management.ps1 -RequireFullAssignment
```
