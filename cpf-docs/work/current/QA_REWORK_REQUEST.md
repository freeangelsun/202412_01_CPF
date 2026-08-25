# QA 재검수 요청 준비

현재 Developer GPT Source 개발과 정적검증은 완료했으나 필수 로컬 Runtime 및 Codex 독립검증이 남아 있어 QA 최종 재검수 요청 상태가 아닙니다.

Runtime PASS 후 동일 Requirement ID로 다음 Evidence를 첨부해야 합니다.

- Java25 FullLocal 전체 결과
- Oracle/PostgreSQL/MariaDB verifier-owned Fresh/Upgrade/Rollback-Reapply 결과
- Batch FILE_WATCH/CENTER_CUT failure-recovery E2E
- MBW Approval decision/version/hash/execution/UNKNOWN reconcile E2E
- ADM/Backoffice Browser E2E
- Fresh Open Git external developer acceptance
- Source Identity/Manifest 일치
- Codex 독립 cross-check 결과
