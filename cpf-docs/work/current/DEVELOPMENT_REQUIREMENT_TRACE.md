# CPF Header / Context / Public API Development Requirement Trace

- Baseline ZIP SHA-256: `8b2e064accaead9e3b81bbf306c2197142621ffdc25aab6cba9a420ef613ad1f`
- Result source SHA-256: `c927382e9bd2b559a306e3ccf33183492190a83fbf11255b3a81f0a72c131f3a`
- 개발지침/Steering 충돌 시 이번 Header/Context 지침 + 관리팀 Public API Steering 의도를 최우선으로 정본에 현행화했다.

## Trace

| ID | Requirement | Implementation / Consumer | Verification | Status |
|---|---|---|---|---|
| HDR-001 | Canonical HTTP Header single source of truth / canonical six | CpfHttpHeaders/CpfHttpHeaderNames/runtime/filter/tests aligned | 완료 | 완료 |
| HDR-002 | Internal Domain canonical six automatic propagation | Typed Domain transport owns propagation; generic HTTP injects none | 완료 | 완료 |
| HDR-003 | External ingress generates/normalizes protected transaction/system context | Unverified external ingress defaults external; controller-before context creation | 완료 | 완료 |
| HDR-004 | Inbound missing/malformed/forged/system-target-operation mismatch enforcement | 400/403/409 mapping and pre-controller enforcement source/static gates PASS | 완료 | 완료 |
| HDR-005 | Canonical operationId parity across annotation/OpenAPI/domain header/ADM | ADM 337/337, Backoffice 96/96 exact OpenAPI/controller coverage PASS | 완료 | 완료 |
| HDR-006 | Dynamic custom Header Public API and protected mutation guard | Java21 actual-source header gate PASS 10 assertions | 완료 | 완료 |
| HDR-007 | Header policy: required/type/pattern/masking/propagation boundary | policy registry/log sanitizer/runtime enforcement currentized | 완료 | 완료 |
| HDR-008 | Trusted proxy clientIp normalization and untrusted forwarding rejection | trusted proxy resolver and ingress trust boundary implemented/tests added | 완료 | 완료 |
| HDR-009 | External outbound blocks CPF canonical six by default | CpfTypedHttpClient external boundary + static no-leak gate PASS | 완료 | 완료 |
| HDR-010 | Header/context failure correlation to logging/DB/ADM model | source/DB/ADM contract currentized; live failure correlation is next minimal runtime | 미검증 | Runtime 재확인 |
| HDR-011 | Legacy Channel/Execution/Caller/Target wire cleanup | current product static legacy-wire search PASS; historical migrations kept immutable | 완료 | 완료 |
| CTX-001 | CpfContexts transaction/system/operation Public API | current/require/capture/bind/run/call + canonical transaction/system/operation access | 완료 | 완료 |
| CTX-002 | Context capture/restore async Public API without business ThreadLocal | existing CpfContexts capture/bind reused; duplicate wrapper not added | 완료 | 완료 |
| CTX-003 | Canonical Transaction ID API and immutable transaction identity | transactionId format retained; instanceToken derived deterministically from instanceId | 완료 | 완료 |
| RTI-001 | WAS instanceId single ownership: explicit value else runtime hostname | CpfInstanceIdentity consolidated; synthetic local/domain fallback removed; multi-WAS runtime check pending | 미검증 | Runtime 재확인 |
| PUB-001 | Public/SPI/Internal boundary and OSS-aligned naming | CpfHttpHeaders canonicalized; duplicate CpfHeaders/Security/OpenAPI surfaces removed | 완료 | 완료 |
| PUB-002 | Domain Call Golden Path without manual Header composition | CpfDomainClientRouter/typed transport actual consumers retained | 완료 | 완료 |
| PUB-003 | External HTTP Golden Path without CPF internal header leakage | existing CpfTypedHttpClient optimized instead of duplicate CpfRestClient wrapper | 완료 | 완료 |
| PUB-004 | Security Public API avoids duplicate security-context implementations | registered com.cpf.security.resource canonical package retained; duplicate removed | 완료 | 완료 |
| PUB-005 | Logging/Audit/Runtime metadata ownership alignment | source/DB/ADM models aligned; live runtime correlation pending | 미검증 | Runtime 재확인 |
| PUB-006 | Cache/Messaging/Batch Public API inventory without unnecessary wrappers | existing owner APIs reused; no wrapper proliferation | 완료 | 완료 |
| PUB-007 | EDU/Generated Domain use Public Golden Path, no Internal/raw context plumbing | static no-internal/no-raw-context gates PASS; EDU raw ThreadLocal/HttpClient patterns cleaned | 완료 | 완료 |
| DB-001 | Same-meaning same-name transaction DB/Java/ADM fields + DB3 lifecycle | CLIENT_ID/ORIGINAL_SYSTEM_CODE/SYSTEM_CODE/CALLER_SYSTEM_CODE/TARGET_SYSTEM_CODE/TARGET_OPERATION_ID/INSTANCE_ID; DB3 gates PASS | 완료 | 완료 |
| DB-002 | EDU canonical operationId vs executionId DB lifecycle | refDB V95/U95, lifecycle/checksum PASS | 완료 | 완료 |
| OPENAPI-001 | Canonical OpenAPI profile + exact controller coverage | profile PASS; ADM 337/337, Backoffice 96/96 | 완료 | 완료 |
| HYGIENE-001 | Dead/duplicate/stale file and in-file garbage cleanup | 75 root-relative deletions; protected deletion 0; pyc 0 | 완료 | 완료 |
| RUNTIME-001 | Minimal Java25/live runtime re-verification for current change impact | Gradle wrapper distribution blocked by services.gradle.org DNS in assistant environment; user local minimal runtime required | 미검증 | Runtime 재확인 |
