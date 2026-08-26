# OPEN ISSUES

제품 Source 보정 관점의 알려진 미처리 defect는 현재 정적/계약 Gate에서 발견되지 않았다. 다만 아래는 **검증 미완료**이며 QA 최종 PASS 조건이다.

1. Java25 + Gradle9.1 Root Build / Test / Publication actual execution.
2. DB3 physical runtime Fresh → Upgrade → Rollback/Reapply.
3. Kafka-free Batch two-worker/process-kill/UNKNOWN/reconcile + Gateway/Center-Cut E2E.
4. One-WAS 이후 Logging/OpenAPI/Browser/Performance 전체 Runtime.
5. VSCode Fresh JDT Problems 0 Error / 0 Warning 실측.
6. ADM npm lint/typecheck/test/build actual execution.

위 항목은 `PASS`로 기록하지 않았으며 완료 전 QA 최종 제출 상태로 간주하지 않는다.
