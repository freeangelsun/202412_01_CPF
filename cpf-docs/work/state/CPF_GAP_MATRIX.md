# CPF Gap Matrix — 22b1874e67547372b51a4bcd21f47aea6fcb5c25 Review / 2026-07-24 Overlay

## 기준

- Source Review Baseline: `22b1874e67547372b51a4bcd21f47aea6fcb5c25`
- Canonical Requirement: 162 + Legacy Alias 8
- Overlay Runtime: 직접 실행 미수행

| Priority | Requirement/Area | 판정 | 핵심 Gap | 다음 완료 조건 |
|---|---|---|---|---|
| P0 | DB-INSTALL | 실패 → Overlay 후 미검증 | split/generated/central 불일치 | 123-table 정적 bundle parity + Fresh MariaDB install/verify |
| P0 | DB-MULTI-VENDOR | 부분 구현 | module-local fallback/비-Maria 미구현 | Central-only Runtime, MariaDB E2E, 타 Vendor는 환경 없으면 미검증 |
| P0 | SAMPLE-MBR | 실패 | mbr_sample_item vs old Auth Mapper | Source/Mapper/API/Test를 minimal sample 계약으로 교정 |
| P0 | SAMPLE-ACC | 부분 구현 | 공통 sample template 미완 | Generator와 동일 logical sample contract |
| P0 | ARCH-BOUNDARY / DB-OWNERSHIP | 실패 | ADM cross-owner DB 접근 | Owner Query/Command Local/Remote contract |
| P0 | ADM Approval | 미구현 | 위험조치 Approval Engine 없음 | Policy/SoD/Owner Command/API/UI/Runtime |
| P0 | BZA-APPROVAL | 부분 구현 | 부서합의/ALL·ANY·N_OF_M/위임/정책 버전 미연결 | Policy→participant snapshot→decision Engine/API/UI E2E |
| P0 | CENTER-RUNNER | 미구현 | 독립 Runtime 없음 | cpf-batch CenterCutRunner + multi-instance recovery |
| P0 | DB-MIGRATION / REL-MIG | 실패 | V6/V29 checksum | 역사 보존 원인 확정 + upgrade/rollback evidence |
| P0 | TEST-EVIDENCE / DOC-GOV | 실패 | stale evidence/index 불일치 | stale 삭제 + 새 commit 기반 evidence |
| P0 | REQ-GOV | 부분 구현 | 133→126 ID 유실 | 162 canonical + continuity ledger 유지 |
| P1 | DB-MULTI | 재확인 필요 | Multi datasource/read replica 추적 유실 | datasource routing/failover/transaction contract 구현 검증 |
| P1 | Batch/Core Ownership | 부분 구현 | runtime 구현 일부 core 잔존 | contract만 core, runtime cpf-batch |
| P1 | Generator | 부분 구현 | lifecycle Runtime 미검증 | arbitrary domain create→remove→regenerate E2E |
| P1 | ADM/BZA Frontend | 미검증 | 신규 approval UI 없음 | production build + browser E2E |
| P1 | Security/Operations restored IDs | 재확인 필요 | 일부 ID가 catalog에서 소실됐던 이력 | 162 matrix로 source/evidence 전수 대조 |
