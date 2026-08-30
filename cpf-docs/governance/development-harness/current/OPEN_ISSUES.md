# CPF OPEN ISSUES — Current

현재 Source/Static Harness 보정은 완료됐지만 전체 QA 완료는 아니다. 아래 Mandatory Physical Acceptance가 남아 있다.

- `BLOCKED_EXTERNAL` Java25 Root Build/Test/Publication/SBOM
- `BLOCKED_EXTERNAL` Fresh Windows VS Code/Gradle Buildship Error=0 Warning=0
- `BLOCKED_EXTERNAL` Oracle/PostgreSQL/MariaDB DB3 Physical lifecycle
- `BLOCKED_EXTERNAL` Windows PowerShell Unified CLI/Generator lifecycle
- `BLOCKED_EXTERNAL` Batch 5-role + Worker×2 + kill/takeover/fencing/UNKNOWN/reconcile
- `BLOCKED_EXTERNAL` One-WAS transaction + File/DB log correlation + Runtime OpenAPI
- `BLOCKED_EXTERNAL` Frontend npm lint/typecheck/test/build + Browser E2E/a11y/error states
- `BLOCKED_EXTERNAL` Performance load/soak/backpressure
- `BLOCKED_EXTERNAL` Actual Open Git Fresh Release + Fresh Consumer + leakage 0
- `BLOCKED_EXTERNAL` Same Source Full Runtime Fresh Replay
- `NOT_EXECUTED` Codex/Claude Independent Review
- `NOT_EXECUTED` QA Final Acceptance

위 항목 중 하나라도 남으면 CPF 전체 완료가 아니다.

## 20260830 Claude 세션 신규 Finding (현재 WP와 Root Cause 별개)

- **CMN Common cache refresh 기능 미완성** — `cpf-common` 의 `JdbcCpfCodeService` / `JdbcCpfParameterService` 가
  Working Tree 에서 수정 중(M)이며, `test_cmn_code_message_durable_cache` 가 요구하는
  `refresh() { requireCache().clear(); }`, cache refresh event repository, listener 가 아직 없다.
  다중 파일 기능 구현이 필요하고 진행 중인 다른 작업과 충돌할 수 있어 이번 WP 에서 구현하지 않았다.
  상태: `OPEN` / 재실행 조건: 해당 기능 구현 완료 후 `pytest cpf-tools/db/tests/test_cmn_code_message_durable_cache.py`.
- **PostgreSQL DB lifecycle 미완성** — `check-admin-data-safety.ps1` 이
  `Selectable vendor has incomplete lifecycle: postgresql` 로 실패한다. DB3 lifecycle 작업 범위이며
  WP-R10 계열에 속한다. 상태: `OPEN` / 재실행 조건: postgresql lifecycle 완성 후 재실행.
- **`cryptography` prerequisite 미설치** — `cpf-tools/db/tools/cpf-backup-crypto.py` 의 의존성 선언을
  `cpf-tools/db/tools/requirements.txt` 로 추가했다(이번 세션 수정). 실제 설치는 사용자 환경 결정 사항이며
  설치 전까지 `test_cpf_backup_crypto` 5건은 FAIL 로 남는다.
  재실행 조건: `python -m pip install -r cpf-tools/db/tools/requirements.txt` 후 재실행.
- **openssl PATH prerequisite** — openssl 은 시스템에 존재하나(Git 3.5.4) PowerShell PATH 에 없어
  `test_release_target_trust` 7건이 FAIL 했다. PATH 에 포함하면 전부 PASS 한다(실측 확인).
