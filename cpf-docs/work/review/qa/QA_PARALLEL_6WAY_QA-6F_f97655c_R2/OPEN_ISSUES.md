# OPEN ISSUES — QA-6F R2

1. **Seed QA 직접보완 교차검토**  
   13개 Requirement는 Source/Test 보완 후 12/12 PASS지만 개발GPT·Codex 교차검토와 3 Vendor 실제 DB 재검수가 남았다.

2. **실제 Acceptance 위반 61건**  
   `QA_REWORK_REQUEST.md`와 `REQUIREMENT_STATUS.csv`에서 Requirement ID·축·Source 후보·성공/실패 기준을 관리한다.

3. **Runtime 미검증 5,032건**  
   Source 존재·Source 미확정·과거 Evidence 여부와 관계없이 실제 Consumer Runtime과 exact-SHA Evidence가 없으면 미검증이다.

4. **Data Lineage**  
   계약·Consumer·bounded fallback은 확인했으나 durable backend, Bean wiring, query/ADM, 권한/감사, multi-instance Runtime이 미확정이다.

5. **Data Reconciliation**  
   실제 UNKNOWN→ledger→worker 경로는 확인했으나 AutoConfiguration, 공식 3 Vendor DB, process kill/fencing, ADM/metric/alert Runtime이 미검증이다.

6. **환경 제한**  
   pwsh, 공식 3 Vendor DB, 전체 Gradle workspace, Browser/Fault 환경이 없어 관련 검증을 성공 처리하지 않았다.
