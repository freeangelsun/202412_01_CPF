# CPF Stabilization Report — 2026-07-24

## Baseline

- master review commit: `22b1874e67547372b51a4bcd21f47aea6fcb5c25`
- 상태: 전환 WIP / 부분 구현
- Canonical Requirement: 162
- Overlay는 Runtime 실행 전이며 성공 Evidence가 아니다.

## 살려야 할 기반

- Service Call/Registry/Health/Resilience
- Standard Header/transactionGlobalId/Trace
- Broker reliability
- Core Fixed-Length API/SPI
- Metadata Generator + central domain-template
- cmnDB minimal sample
- batDB physical ownership
- Central Vendor Pack skeleton

## 즉시 교정한 정적 기반

- Requirement 34개 복구와 8 alias mapping
- ADM/BZA 조직·Approval DDL baseline
- Central Vendor Pack fail-fast resolver/catalog + unit test baseline
- module-local vendor resource cleanup allowlist
- MariaDB split/generated install·verify 정적 parity
- build evidence output/publication gate/PowerShell 7 기준 보정

## 아직 완료가 아닌 핵심

- MBR/ACC Java/Mapper runtime contract
- ADM cross-owner DB boundary
- ADM/BZA approval engine/API/UI
- CenterCutRunner / batch runtime ownership
- Fresh MariaDB 123-table install/runtime
- non-Maria vendor product packs
- V6/V29 migration integrity
- full build/runtime/browser/failure/upgrade/rollback evidence

다음 Codex는 `CPF_CURRENT_WORK_REQUEST.md`와 Continuity State에 따라 위 항목을 실제 구동해 닫는다.
