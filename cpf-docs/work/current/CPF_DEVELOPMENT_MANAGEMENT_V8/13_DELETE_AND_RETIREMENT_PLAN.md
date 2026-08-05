# 삭제·정리 계획

승인 없이 삭제하지 않는다.

현재 삭제 후보는 `cpf-docs/work/current/CPF_DEVELOPMENT_WORKLIST_V7_1` 한 경로뿐이다. V8이 V7.1 Work Package 전체를 포함하지만 다음 조건이 모두 충족된 뒤에만 삭제한다.

1. V8 Root Overlay Push 완료
2. V8 Unit Test와 Full Assignment Validator PASS
3. Repository 전체 참조 검색에서 활성 V7.1 경로 참조 0
4. 사용자 명시 승인

승인 후 exact-path PowerShell 명령은 `DELETE_COMMANDS.ps1`에 있다. 광범위 `git clean`, `git reset --hard`, `git restore .`는 사용하지 않는다.
