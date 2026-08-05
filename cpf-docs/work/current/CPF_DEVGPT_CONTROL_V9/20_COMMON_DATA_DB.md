# CPF V8 상세 개발 Work Package — Common·Data·DB Lifecycle

- Canonical Requirement: 18개
- 실행 Work Package: 93개
- Canonical IDs: `CMN-CALENDAR`, `CMN-CODE`, `CMN-EXTENSION`, `CMN-MSG`, `CMN-SAMPLE-DB`, `CMN-TEMPLATE`, `DATA-LINEAGE`, `DATA-RETENTION`, `DB-BACKUP`, `DB-FRESH`, `DB-INSTALL`, `DB-MIGRATION`, `DB-MULTI`, `DB-MULTI-VENDOR`, `DB-OWNERSHIP`, `DB-PERF`, `DB-ROLLBACK`, `DB-SQL`
- 각 Work Package는 독립 개발 단위이며, 동일 호출 경로를 숫자 때문에 중간 절단하지 않는다.
- 구현 제안은 비강제이고 CPF 정본·표준·기존 Architecture가 우선한다.

## AI/Git 분할 Index

이 영역은 93개 Work Package를 2개 파일로 분할했다. 내용은 축소하지 않았으며 `WORK_ITEM_INDEX.csv`에서 Work Item별 정확한 Part를 찾는다.

| Part | Work Package | 범위 | 크기 |
|---|---:|---|---:|
| `20_COMMON_DATA_DB_PART_01.md` | 53 | `CPF-WP-CMN-EXTENSION-01-CONTRACT_OWNERSHIP` → `CPF-WP-DB-MIGRATION-05-DATA_MIGRATION` | 207,689B |
| `20_COMMON_DATA_DB_PART_02.md` | 40 | `CPF-WP-DB-MIGRATION-06-GENERATION_COMPATIBILITY` → `CPF-WP-DATA-RETENTION-05-DATA_MIGRATION` | 156,390B |
