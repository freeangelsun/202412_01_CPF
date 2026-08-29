# CPF Current Work Request and Status — Generated Projection

이 파일은 Authority가 아니라 `CURRENT_WORK_ITEM_REGISTRY.csv`와 `CURRENT_DEVELOPMENT_STATUS.csv`에서 생성되는 읽기용 Projection이다.

- Current Product Source Identity: `2935d73aacade2af79f2d5dac994c8f5bb7e74350d98a2780dd765bad2a0d5d4` / 8,449 product-source files
- Canonical Product Requirements: **218**
- Canonical Trace: **218 / 218**
- Detailed Bridge: **46**
- Requirement/Tracking Work: **394**
- Root Cause Execution WP: **16**
- Current Work Items: **410**
- Role Ledger: **1230 = 410 × 3**
- Test Execution Ledger: **820**
- Control Execution Ledger: **32**
- development_status: `{'완료': 199, '미완료': 211}`
- verification_status: `{'미검증': 410}`
- runtime_status: `{'NOT_EXECUTED': 395, 'VERIFICATION_PENDING': 2, 'BLOCKED_EXTERNAL': 13}`
- overall_status: `{'미검증': 399, '미구현': 11}`
- independent_reviewer_status: `{'미검증': 217, '미완료': 193}`
- qa_status: `{'미검증': 217, '미완료': 193}`

Root Cause Execution 순서는 `WP-H00 → WP-H01 → WP-H02 → WP-B01 → WP-B02 → WP-B03 → WP-CF01 → WP-RL01 → WP-DB01 → WP-CLI01 → WP-BAT01 → WP-ONE01 → WP-FE01 → WP-PF01 → WP-RL02 → WP-FIN01`이다.

Static/Contract PASS는 Physical Requirement를 대체하지 않는다. 실제 Java25 Root Build/Publication, Fresh VS Code Error=0 Warning=0, DB3/Batch/One-WAS/Browser/Performance/Open Git/Fresh Replay, Independent Reviewer, QA가 미실행이면 전체 완료가 아니다.
