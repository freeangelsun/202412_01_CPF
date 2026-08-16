# Codex Revalidation Scope after GPT 누적 보완 — 2026-08-15

> 전달 입력 baseline: `92169d9918dd176e8322ac2f9dfc29ebe1d2ea12`
> 원칙: 기존 Codex Runtime PASS를 무조건 폐기하거나 처음부터 반복하지 않는다. **GPT 변경으로 영향 받은 영역 + 기존 미수행 Runtime만** 재검증한다.

## 변경 영향으로 재오픈

- Batch Capability/Generated Domain 분리: Batch 미선택 zero-footprint, `cpf-starter-batch` 초기 구성 선택, Generated Domain Online-only 회귀.
- Java/Gradle dependency currentization: 사용자 full build에서 실패했던 provider/runtime/test classpath 영역 최소 compile/test 재검증.
- Required CI test inventory 확대: Windows/pwsh 포함 full `cpf-tools` pytest 재실행.

- TransactionId/Header: HTTP/Batch/Message/Remote propagation, retry, UNKNOWN/Reconcile lineage.
- Generator/Generated Domain: direct domain package(`member`, `external`), setup DB profile, regenerate/diff/idempotency, DB3 generated-domain composite.
- DB3: ADM Incident canonical `adm_incident_*`, V92→V118 upgrade, R118 rollback/reapply, DB source-plan derivation.
- Batch: Scheduler UNKNOWN explicit approval Reconcile, CAS/idempotency/audit, RuntimeCommand executor trust boundary.
- Redis/Valkey: provider-neutral Cache owner/common runtime 변경 영향의 multi-instance/failure/reconnect.
- ADM/BZA: 최신 OpenAPI/Orval/Generated Client/Consumer + Browser E2E.
- 위험조치: File Job/Feature Flag/Secret/Cache/Dynamic Log/Service Registry Approval → Owner Command → Result/Audit/Reconcile.
- Fixed-Length: 실제 runtime encode/decode/masking/layout-version/ADM journey.

## 기존 미수행이므로 우선 실행

1. Java25 Root build/test/publication.
2. Oracle/PostgreSQL/MariaDB full live lifecycle.
3. Local Integrated / Distributed runtime.
4. Process Kill → UNKNOWN → Reconcile → double effect 0.
5. Kafka/Messaging 및 Batch kill/restart.
6. Gateway OFF/ON, Security adversarial, Deployment, Performance/Backpressure.
7. Windows clone/extract/build long-path.

## 크레딧 절약

- Static Source 검색/문서 재검토를 처음부터 반복하지 않는다.
- 변경 영향 없는 과거 Runtime PASS는 Evidence/Source 영향도를 먼저 판정한다.
- 영향 있는 부분만 최소 회귀 후 다음 미수행 Runtime으로 진행한다.
- Codex 결과는 `CODEX_FINAL_VALIDATION_RESULT.md`에 실제 명령/Exit Code/환경/새 exact SHA로 기록한다.

## Repository cleanup 영향

- 단계성 문서/Evidence/retired tool 삭제 자체는 unrelated Runtime PASS를 재오픈하지 않는다.
- 다만 Batch runtime role metadata/currentizer에서 inactive compatibility shim 의존을 제거했으므로 Batch deployment/fresh-host 경로는 기존 미수행 Deployment Runtime 범위에서 확인한다.
- Cache verifier/owner 경로 currentization은 기존 Redis/Valkey 재검증 범위에 포함한다.
- History rewrite 후 exact SHA가 새 root commit으로 바뀌므로 최종 Release Evidence는 새 SHA에서 최소 smoke + 기존 미수행 Runtime을 기록한다.
- 삭제된 과거 campaign test/verifier를 복원하여 재검증하지 않는다. 현재 canonical Gate만 사용한다.
