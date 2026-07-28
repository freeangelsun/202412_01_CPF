# CPF 20260728_02 Validation Ledger

- 기준 Commit: `ecaddd581a88ede22b63116effd61313744b3fbe`
- 실행 시각: `2026-07-28T21:19:43+09:00`
- 실행 환경: Linux container, OpenJDK/Javac 21.0.10, Node 22.16.0, TypeScript 5.8.3
- 민감정보: targeted secret scan PASS

| 검증 | 결과 | Evidence |
|---|---|---|
| Overlay required files / JSON / enum / DB parity / package-path / secret | PASS | `STATIC_VALIDATION_RESULT.json`, `evidence/OVERLAY_STATIC_VALIDATION_FINAL.txt` |
| Runtime API compile + behavior | PASS | `evidence/RUNTIME_API_RECHECK.txt` |
| Runtime state compile/parity | PASS | `evidence/RUNTIME_STATE_COMPILE.txt` |
| Gateway transport compile + spool behavior | PASS | `evidence/GATEWAY_TRANSPORT_RECHECK.txt` |
| Gateway API Client principal/IP/CERT/quota | PASS | `evidence/GATEWAY_SECURITY_RECHECK.txt` |
| Batch 5 actual-consumer policies | PASS | `evidence/BATCH_RUNTIME_5_CONSUMER_POLICY.txt` |
| External Institution applier | PASS | `evidence/EXTERNAL_APPLIER_RECHECK.txt` |
| ADM TypeScript syntax | PASS | `evidence/FRONTEND_TYPESCRIPT_RECHECK.txt` |
| Java 25 / Gradle 9.1 full build | 미검증 | 현재 환경 Java 21, 전체 Repository 미탑재 |
| Oracle/PostgreSQL/MariaDB real DB | 미검증 | DB 환경 필요 |
| Browser / multi-instance / partial failure | 미검증 | Runtime 환경 필요 |
| QA Inventory 1,214 / Scenario 201 전수 재판정 | 미검증 | Overlay 적용 Commit과 통합환경 필요 |
