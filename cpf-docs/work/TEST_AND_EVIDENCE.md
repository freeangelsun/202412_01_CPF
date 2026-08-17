# CPF TEST AND EVIDENCE — 2026-08-17 Session Close

작성 시각: `2026-08-17 20:44:55 +0900`
Baseline marker: `4b6f96796c3bf26b1c3324cc4d9b701bd9415acd`
제품/사용자 로컬 Java 기준: **Java 25**
GPT 환경 대체 검증 Java: **Java 21**
최종 전체 QA 판정: **RUNTIME_REVERIFY_REQUIRED / 세션 마감**

## 확인 완료
| 항목 | 결과 | 비고 |
|---|---|---|
| Java source syntax | PASS | 2,627 files 마지막 기록 |
| EDU canonical static acceptance | PASS | online 20 / batch 15 / total 35 |
| EDU operationId static parity | PASS | Canonical online operation mismatch 0 |
| NXT3 final gate | PASS | 22/22 |
| Korean source comment gate | PASS | 781 scanned / failure 0 |
| DB basic Python test | PASS | 82/82 |
| DB verification | PASS | 세션 중 75/75 확인 |
| Generator Python | PASS/SKIP_ENV | 21 PASS / 10 env skip / FAIL 0 |
| Generated Domain alternate compile | PASS | GPT Java21: member 26 / external 24 |
| ADM/BZA targeted OpenAPI/consumer | PASS after currentization | DLQ/typed-client drift 포함 보정 |
| Delete Manifest protected-path check | PASS | 보호 경로 삭제 0 |

## 실행하지 못했거나 최종 재실행이 필요한 검증
- Java25 Gradle targeted/full build/test
- Java25 FullLocal 전체 검증
- Header/Context live 400/403/409 + Controller-before rejection
- Operation auto-bootstrap + YML Seed once + ADM Policy preservation live test
- Channel LKG/maxStale/fail-close failure injection
- Multi-WAS policyVersion propagation and instanceId identity
- DB3 live install/upgrade/rollback/runtime query
- Docker/runtime/browser E2E
- 수정 후 전체 Verification suite end-to-end 1회 재실행

위 항목은 미실행을 PASS로 기록하지 않는다. 상세 인계는 `OPEN_ISSUES.md`와 `HANDOVER.md`를 따른다.
