# CODEX REVALIDATION REQUEST — Developer GPT Runtime Pending — 2026-08-25

현재 Developer GPT Source를 **독립적으로** 재검증한다. 과거 PASS나 이전 Source SHA를 현재 성공으로 승계하지 않는다. 현재 단계는 QA 전달 전 결함 수렴 단계이며, live Runtime이 실제 PASS하기 전에는 QA-ready로 판정하지 않는다.

## 기준

- Baseline ZIP: `CPF_FULL_SOURCE_FOR_NEXT_QA_20260825_121103.zip`
- Baseline ZIP SHA-256: `d2e89aba1841a4387a473610db905415f8565fcf09d06a56a8afa3a1b33a3a48`
- Current Product Source SHA-256: `c79be31a71c15c02665d56e29c0f51244c91ab3894183775ce311cde3dbf40df`
- Canonical Requirements: `208`
- Developer Evidence: `cpf-docs/deliverables/TEST_AND_EVIDENCE.md`
- Delete lifecycle: `cpf-docs/deliverables/DELETE_MANIFEST.csv` — Windows 200자 경로 currentization 220개 파일
- Overlay policy: Baseline 대비 실제 `ADD/MODIFY`만 ZIP payload에 포함, unchanged payload `0`

## 독립 재검증 필수 범위

1. `GEN-CUSTOM-LIB`: `cpf library create/attach/sync/verify`, 한글 생성 설명, user-owned Source 보존, 선택 Domain만 dependency 연결, `com.cpf.*`/Internal dependency fail-closed.
2. `DEVEX-WINDOWS-PATH`: Repository 전체 Root-relative 경로+파일명 `<=200`, alias map 추적성, 200 초과 mutation fail-closed.
3. `DEVEX-DOCKER-LIFECYCLE`: down-state prerequisite 자동 기동, health+functional readiness, 기존 실행 컨테이너 보존, Harness-owned cleanup.
4. BAT/CEC `CENTER_CUT_RUNNER`, DB3 V138/V139 current-edge, 2-worker Kill/Lease/Fencing/UNKNOWN/Reconcile/Recovery.
5. Backoffice Approval CAS `expectedVersionNo + expectedPayloadHash`, SoD/optimistic-lock/read/history/execution-result 경계.
6. Context/System6/Operation/Instance identity와 Gateway/Backoffice/Domain Invocation 실제 Consumer.
7. ADM/Backoffice Generated Client 및 Approval Owner 경계, Browser 401/403/404/409/429/500/503, 접근성/반응형.
8. Open Git fresh generation/build/consumer, publication/BOM/catalog/SBOM/supply-chain.
9. Oracle/PostgreSQL/MariaDB Fresh→Seed→Runtime→Upgrade→Rollback/Reapply→Recovery.
10. 최종 Java25 Root Build, Full Runtime, Fresh Replay.

Java25/Docker/DB3/Browser/Public Binary 단계를 실제 실행하지 않았으면 PASS로 기록하지 않는다. Codex는 Codex-owned 상태/Evidence만 수정한다.

Generated at: `2026-08-25T17:53:08+09:00`
