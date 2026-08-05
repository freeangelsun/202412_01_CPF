# Artifact Smoke Test

임시 Root Overlay 복제본에서 다음 흐름을 실제 실행했다.

1. `generate_development_requests.py --allow-prebootstrap --max-items-per-session 20`
2. Active 항목 825개를 76개 동적 Session으로 배정
3. 첫 배정 항목의 Session Result를 `개발GPT_수행상태=완료`, `개발GPT_자체검수상태=완료`, Evidence와 Completion SHA 포함으로 병합
4. 해당 항목이 `완료 스킵`으로 전환되고 Active Scope에서 제외됨을 확인
5. QA Feed `REREVIEW` 적용
6. 해당 항목이 `재검수 대상`으로 바뀌고 Active Scope에 다시 포함됨을 확인

Smoke 대상: `CPF-WP-CENTER-UNKNOWN-01-CONTRACT_OWNERSHIP`

결과: PASS

주의: `--allow-prebootstrap`은 Artifact Smoke Test 전용이다. 실제 Repository 개발 요청은 Full Assignment PASS 후 생성한다.
