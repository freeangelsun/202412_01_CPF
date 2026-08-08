# CPF QA A/B Final Full-Scope Central Merge Report

## 1. Basis and verdict

- QA/central basis: `b4b6b18b43e9ff83436ceb8b1816b31594e8d6eb` (`07_09`)
- Product Source baseline: `f0aa49f29cba3cfd6ae12b0ddd4e118d05fff16c` (`07_08`)
- `07_08 → 07_09` Product Source change: **0**
- QA A verdict: **FAIL / REDEVELOPMENT REQUIRED + UNVERIFIED / RELEASE_BLOCKED**
- QA B verdict: **FAIL / REDEVELOPMENT REQUIRED + UNVERIFIED / RELEASE_BLOCKED**
- Central verdict: **FAIL / REDEVELOPMENT REQUIRED + UNVERIFIED / RELEASE_BLOCKED**

Both uploaded ZIPs were independently checked before merge:
- QA A: 48 files, unsafe path 0, internal SHA256 **47/47 matched**
- QA B: 32 files, unsafe path 0, internal SHA256 **31/31 matched**

## 2. QA A summary

- Special 1000: PASS 0 / FAIL 64 / 미검증 361 / 재확인 575
- Canonical 169: PASS 0 / FAIL 3 / 미검증 87 / 재확인 79
- Central 31: PASS 5 / FAIL 9 / 미검증 13 / 재확인 4
- Runtime 13: PASS 0 / FAIL 4 / 미검증 9
- New findings: 5 = P0 3 / P1 2

Distinct strengths:
- executable Online A→B→C(/D) sample gap,
- executable Batch→A→B→C recovery sample gap,
- six qualification gates accepting a non-CPF fake target/authority,
- Javadoc method-contract gap,
- operation-level OpenAPI error contract gap.

## 3. QA B summary

- Special 1000: PASS 0 / FAIL 30 / 미검증 559 / 재확인 411
- Canonical 169: FAIL 19 / 미검증 53 / 재확인 97
- Central 31: FAIL 7 / 미검증 24
- Runtime 13: PASS 0 / 미검증 13
- New findings: 7 = P0 6 / P1 1

Distinct strengths:
- current exact-SHA evidence false-green,
- timeline query failure swallowed into NOT_APPLICABLE,
- missing authenticated first-hop starter-system producer/wiring,
- FileLog head-of-line blocking,
- persistence-mybatis package ownership drift,
- typed generated-client false-green,
- Javadoc gap.

## 4. Central merged denominators

### Special Review 1,000
Central worst-case rule (`FAIL > 미검증 > 재확인 > PASS`):
- **FAIL 87**
- **미검증 548**
- **재확인 필요 365**
- **PASS 0**

### Canonical 169
- **FAIL 20**
- **미검증 87**
- **재확인 필요 62**
- **PASS 0**

### Existing Central 31
QA A/B worst-case merge:
- **FAIL 14**
- **미검증 17**
- PASS 0

FAIL IDs:
`002, 006, 007, 013, 016, 017, 018, 019, 020, 021, 022, 023, 029, 030`.

### Runtime 13
- **FAIL 4**: RUNTIME-06/07/08/09 from QA A false-green attack
- **미검증 9**
- PASS 0

## 5. New Finding normalization

Raw current new findings: **12**.
Javadoc findings (`QA-A-FS1000-NEW-004`, `QA-B-1000-NEW-006`) are one root cause.
Normalized roots: **11**.

### Mapped to existing Central IDs
1. `QA-B-1000-NEW-001` → `CENTRAL-FINAL-002` exact-SHA evidence provenance
2. `QA-B-1000-NEW-003` → `CENTRAL-FINAL-006` transactionId trust/wiring
3. `QA-B-1000-NEW-005` → `CENTRAL-FINAL-007` module/package ownership
4. `QA-A-FS1000-NEW-005` → `CENTRAL-FINAL-013` ADM/BZA OpenAPI error contract
5. `QA-B-1000-NEW-004` → `CENTRAL-FINAL-016` + `017` FileLog durable retry/test
6. `QA-B-1000-NEW-007` → `CENTRAL-FINAL-030` HIGH/CRITICAL typed generated client

`QA-A-FS1000-NEW-003` also keeps `CENTRAL-FINAL-018~023` under FAIL/revalidation because fake-target evidence survived, while a new cross-cutting trust-root action is added below.

### New Central IDs
- `CENTRAL-FINAL-032` P0 — qualification target/authority root-of-trust
- `CENTRAL-FINAL-033` P0 — timeline query failure classification
- `CENTRAL-FINAL-034` P1 — Public API/SPI Korean Javadoc completeness
- `CENTRAL-FINAL-035` P0 — executable Online multi-domain integrated sample
- `CENTRAL-FINAL-036` P0 — executable Batch multi-domain integrated sample

Current Central total: **36**.
Current merged status: **FAIL 19 / 미검증 17**.
Priority total: **P0 26 / P1 10**.
Current FAIL priority: **P0 13 / P1 6**.

## 6. Source fixes that must be preserved

Both QA results recognize meaningful prior repairs. Developer must not regress them:
- Approval terminal fencing tuple,
- Center-Cut terminal-only reconcile,
- exact structured Batch identity,
- transactionId trust/replay policy direction,
- ADM CSP tightening,
- Core persistence ports/adapters and DB masking,
- V107 lineage schema/projection,
- hardened FileLog writer path and >8MiB dedup,
- BZA retired operation removal,
- EDU PRODUCT_ADM/MERGE redirect restructuring,
- retained ADM role alignment,
- BZA Approval Simulation permission/generated path.

## 7. Central decision on exact-SHA evidence

An overlay produced before the user's commit cannot truthfully know its future successor commit SHA.

Therefore Developer must:
- never promote predecessor evidence to successor PASS,
- never hard-code a historical SHA denylist as the primary rule,
- distinguish Product-source basis, evidence execution basis, and recording/packaging commit,
- make verification dynamically bind to the actual checkout HEAD under test,
- treat pre-push Developer verification as development evidence only,
- require post-push QA/Codex/CI execution against the actual successor SHA for final exact-SHA PASS.

Final Release still requires execution on the actual release candidate SHA. No READY/PLANNED/NOT_EXECUTED promotion is allowed.

## 8. Release decision

Release remains blocked because:
- Central FAIL 19 remains,
- Special 1000 FAIL 87 remains,
- Canonical FAIL 20 remains,
- Runtime FAIL 4 + 미검증 9 remains,
- exact-SHA provenance is not closed,
- integrated Online/Batch mandatory samples are absent,
- qualification authority false-green remains.

Next owner: **Developer GPT rework**, followed by successor-SHA QA A/B re-audit.
