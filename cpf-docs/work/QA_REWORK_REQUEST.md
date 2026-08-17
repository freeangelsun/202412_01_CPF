# CPF QA Rework Request — Current

개발 GPT 범위 Source/Static 재개발은 완료했다. QA에는 이번 변경 영향의 **최소 Runtime 재검증**만 요청한다.

1. Java25 targeted compile/test
2. external ingress / internal Domain canonical six
3. 400 / 403 / 409 fail-fast 및 Controller 미실행
4. 실패/정상 transaction correlation FileLog ↔ DB Log ↔ ADM
5. external outbound canonical six 차단
6. explicit instanceId + hostname fallback + two-WAS distinct
7. V119/V120/refDB V95 upgrade/rollback smoke
8. OpenAPI/Generated Client 최소 smoke

이전 전체 FullLocal을 그대로 반복하지 말고, 기존 FAIL 중 위 영향범위와 직접 연결되는 Root Cause만 함께 재확인한다. Runtime Evidence가 없는 항목은 PASS로 처리하지 않는다.
