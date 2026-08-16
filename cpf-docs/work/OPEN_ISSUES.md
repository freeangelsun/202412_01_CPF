# CPF Open Issues

1. **P0 / Windows FullLocal Runtime Closure** — Java25, PowerShell7, Docker, Browser가 있는 사용자 환경에서 FullLocal 재실행 필요.
2. **P0 / QA-B3-010** — DB3, Redis/Valkey, Kafka, Batch Process Kill/UNKNOWN/Reconcile, Browser, Topology, Performance live Evidence 필요.
3. **P1 / QA-B3-011** — Windows fresh extract + Java25 Gradle/Runtime lifecycle Evidence 필요.
4. **P1 / QA-B3-008** — Overlay 적용·commit 후 exact Git SHA 검수 필요.
5. **P1 / Assistant environment coverage** — Java21만 존재하고 Gradle wrapper distribution 다운로드가 네트워크 차단되었으며, Source ZIP에는 node_modules가 없어 Java25 Gradle 및 npm full verify를 현재 환경에서 성공 처리하지 않았다.
6. **P1 / NXT3 aggregate 재확인** — 현재 Assistant Linux에서는 aggregate child-process 종료 특성으로 14번째 이후 단일 프로세스 완주가 불안정했지만, 동일 22개 Gate를 개별 실행하여 전부 PASS했다. 다음 Windows FullLocal에서 canonical aggregate 22/22를 다시 확인한다.
7. **P1 / Frontend dependency runtime** — Source ZIP에는 `node_modules`가 없으므로 TypeScript compile/lint/unit/build와 Playwright는 성공 처리하지 않았다. Node만 필요한 ADM/BZA OpenAPI/Generated/Orval/Consumer 정적 Gate는 PASS했다.

제품 Source 삭제 요구는 없다.
