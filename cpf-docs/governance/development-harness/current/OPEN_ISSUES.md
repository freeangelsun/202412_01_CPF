# CPF OPEN ISSUES — Current

Current Product Source Identity: `1289304269e6f684cb9c32414efadbcfa179b5f7208bcd42f6ff1d5dff15a87f` / 8,450 files.

현재 DevGPT가 재현 가능한 Source/Static/Harness mandatory defect는 이번 범위에서 수정 및 회귀검증됐다. **전체 CPF 완료는 아니며 아래 Physical/Independent/QA Acceptance가 남아 있다.**

- `VERIFICATION_PENDING` Windows Fresh VS Code/Buildship/JDT Error=0 Warning=0.
- `VERIFICATION_PENDING` Current Source Java25-target Root Build/Test/Publication/SBOM Fresh Replay.
- `VERIFICATION_PENDING` DB3 Oracle/PostgreSQL/MariaDB Physical Full Lifecycle Fresh Replay.
- `VERIFICATION_PENDING` Windows/Linux Unified CLI/Generator Physical Lifecycle.
- `VERIFICATION_PENDING` Batch 5-role + Worker×2 + kill/takeover/fencing/UNKNOWN/reconcile.
- `VERIFICATION_PENDING` One-WAS actual transaction + File/DB log correlation + Runtime OpenAPI.
- `VERIFICATION_PENDING` ADM/Backoffice Fresh npm lifecycle + Browser E2E/a11y/401/403/404/409/429/500/503.
- `VERIFICATION_PENDING` Performance load/soak/backpressure/resource leak.
- `NOT_EXECUTED` Actual Open Git Fresh Release + Public Consumer + Leakage 0.
- `VERIFICATION_PENDING` Same Source Full Runtime + Fresh Replay.
- `NOT_EXECUTED` Codex/Claude Independent Review.
- `NOT_EXECUTED` QA Final Acceptance.

과거 `90e4890d...` Source에서 Java25 Build/Publication/SBOM 및 DB3 Physical PASS 근거가 있으나 Current Source와 다르므로 provenance일 뿐 Current PASS로 승계하지 않는다.

### 이번 세션에서 닫힌 stale Open Issue

- Claude session `MANIFEST_MISSING`: provenance 검증 Manifest 복구 및 Merge 완료.
- PostgreSQL lifecycle 미완성/cryptography/openssl PATH를 Current Source 자체의 unresolved product defect로 유지하던 과거 문구: 최신 Runtime/Source 근거와 불일치하여 제거. 실제 Current Source Fresh Replay 결과가 새 Finding을 만들 때만 재개방한다.
- `npm ci` prettier lock mismatch: Source 수정 및 contract regression PASS. Physical online npm lifecycle은 별도 Acceptance로 남김.
- VS Code source-empty profile class output: Source 수정 및 regression PASS. Physical Windows Problems 0/0은 별도 Acceptance로 남김.
