# QA Rework Request

이번 개발 GPT 재개발은 입력 FullLocal 30 FAIL을 7개 Root Cause로 묶어 Source/Verifier/Generator/Frontend/Test/Evidence에 반영했다. 현재 Assistant/Fresh Apply 실행 가능 범위는 다시 검증했으며, QA-B3-008/010/011과 Windows Java25/Docker/Browser Runtime은 열어 둔다.

QA 재검수 시 다음을 우선 확인한다.

- FullLocal PASS/FAIL/SKIP_ENV/NOT_EXECUTED 상태 정확성 및 strict exit
- Java25 Gradle build/test/publication/SBOM
- DB3/Redis/Valkey/Kafka Live lifecycle
- Batch 2-worker Process Kill/UNKNOWN/Reconcile/Fencing
- Local 1-WAS + FileLog/DB Log/ADM same transaction correlation
- ADM/BZA Browser E2E/A11y 및 오류 상태
- Gateway/Topology, Security/Masking, Performance/Backpressure
- Fresh Apply 및 Evidence corruption negative

필수 Runtime 미검증 상태이므로 QA 최종 완료 요청이 아니라 재검수 요청이다.
