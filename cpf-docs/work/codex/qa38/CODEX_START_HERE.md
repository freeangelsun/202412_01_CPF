# CPF QA38 Codex Start Here — Final

Mission: Final Requirement 156개를 실제 Source·SQL·Generator·Consumer·Runtime 기준으로 검수하고 보완한다.

## First read
1. CPF_CURRENT_WORK_REQUEST.md
2. CPF_QA38_FINAL_DEVELOPMENT_REQUIREMENTS.md
3. CPF_QA38_FINAL_REQUIREMENT_MATRIX.csv
4. CPF_QA38_STARTER_INDEPENDENT_REVIEW.md
5. CPF_CODEX_CONTINUITY_STATE.md
6. REVIEW_INDEX.md

과거 날짜별 요청을 활성 요청으로 읽지 않는다.

## Priority
Baseline/Protected → Source Graph → Core/Starter → MQ/JMS/IBM MQ/TCP → DB Fresh →
Consumer/Artifact → Java/Frontend/Runtime/Fault/Browser/Supply → Truth/Hygiene.

## 금지
Commit/Push/Reset/Restore/Stash/Clean, 사용자 DB Reset/Drop, 보호 경로 수정,
Vendor SQL First, 미실행 PASS, 환경 부재로 Requirement 삭제.
