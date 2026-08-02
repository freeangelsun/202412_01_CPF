# QA37 Codex 작업 이력 독립 리뷰

## 결론

QA37 Codex는 단순 검수보다 훨씬 많은 Source 보완을 수행했다. Security, BZA, ADM, REF, Batch, Gateway, DB artifact generation과 여러 static gate에서 의미 있는 PASS가 기록됐다.

그러나 작업은 크레딧 한도에서 중단됐고, 이후 여러 번 Push되었다. 최신 `master` `38089a96e3f4c7c2ba05cda549785b47f67cd462`에 대해 전체 Java Fresh Build, 실제 3 Vendor DB lifecycle, Docker Runtime/Fault, Browser 3종, Supply-chain과 exact-SHA final evidence가 완결됐다는 근거는 없다.

## 확인된 완료 후보

- Security Starter focused tests 26/26
- BZA/ADM clean frontend verify at 당시 WIP
- REF lifecycle 1,014 executed, DB-dependent 2 skip
- Execution Runtime/Center-cut/Control-server/Scheduler focused tests
- Gateway 35/35
- QA30/31/32 and Enterprise/Architecture/Hygiene static gates
- EDU135 independent verification
- Canonical DB artifact generation/static parity
- Starter Root/BOM/publication focused checks

위 결과는 회귀 범위를 줄이는 데 사용하되 최신 exact-SHA PASS로 자동 승계하지 않는다.

## 미완료·미검증

- Oracle actual Fresh Install/Upgrade/Rollback/Reapply
- PostgreSQL actual lifecycle
- MariaDB actual lifecycle
- Fresh precondition object-count 0 and final cleanup
- Generated arbitrary Domain runtime bootstrap on each Vendor
- Kafka actual broker outage/rebalance/process-kill on latest SHA
- JMS/IBM MQ/RabbitMQ/TCP implementation and runtime
- Toxiproxy fault matrix
- OpenTelemetry exporter/outage/PII evidence
- multi-instance/fencing/process kill
- Playwright Chromium/Firefox/WebKit final run
- Trivy/ORT/final artifact SBOM/license/hash
- Java 25 one-shot full lifecycle at latest SHA
- latest exact-SHA Matrix/Evidence reconciliation

## 중단 시점 판단

Codex는 DB Container를 아직 변경하지 않았다고 명시했고 MariaDB lifecycle tooling을 보완하는 중이었다. 따라서 DB와 Docker runtime을 후순위로 둔 것은 크레딧 절감 순서와 Source prerequisite 때문이지만, 실행 전 상태이므로 완료로 볼 수 없다.

## 다음 검수의 원칙

- 과거 PASS를 반복하지 말되 exact-SHA 조건을 충족하지 않으면 final PASS로도 쓰지 않는다.
- Source prerequisite를 닫은 뒤 DB부터 미완료 Stage를 이어간다.
- 실제 결함은 검수 보고만 하지 말고 Source/Test/Generator/Evidence까지 보완한다.
