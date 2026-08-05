# V8에서 V9 격리 Namespace로 이관

## 이관 방식

기존 V8 경로를 이동하거나 수정하지 않는다. V9를 다음 새 경로에 독립 추가한다.

```text
cpf-docs/work/current/CPF_DEVGPT_CONTROL_V9
cpf-tools/scripts/devgpt-control-v9
cpf-tools/scripts/tests/devgpt_control_v9
```

따라서 현재 작업 중인 V8 기반 개발 GPT 자료와 V9 신규 요청은 섞이지 않는다.

## 사용 전환

1. V9 Root Overlay를 Push한다.
2. V9 정적 Validator와 Unit Test를 실행한다.
3. Requirement·Scenario 전수 Bootstrap을 V9에서 실행한다.
4. 신규 개발 요청은 V9 Generator만 사용한다.
5. 현재 진행 중인 V8 개발 결과를 통합하고 QA Handoff를 완료한다.
6. V8 활성 참조와 보존 Evidence가 남지 않았는지 확인한다.
7. 사용자 승인 후 기존 V8 Namespace를 삭제한다.

## 기존 V8 제거 대상

```text
cpf-docs/work/current/CPF_DEVELOPMENT_MANAGEMENT_V8
cpf-tools/scripts/development-management
cpf-tools/scripts/tests/development_management
cpf-docs/work/current/development-session-results
```

V8 기반 작업이 끝나기 전에는 삭제하지 않는다.

## 승인 후 PowerShell 한 줄 명령

```powershell
$paths=@("cpf-docs/work/current/CPF_DEVELOPMENT_MANAGEMENT_V8","cpf-tools/scripts/development-management","cpf-tools/scripts/tests/development_management","cpf-docs/work/current/development-session-results"); $paths | ForEach-Object { if (Test-Path -LiteralPath $_) { Remove-Item -LiteralPath $_ -Recurse -Force } }
```
