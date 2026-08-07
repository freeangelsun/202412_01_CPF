# R20 Runtime 미검증 범위

문서 기준 SHA는 `cd5baccb02245a980e5998aa0dc9bac579fc019f` (`07_04`)지만, R6J가 제품 Source를 독립 검수한 기준은 `3ed676061246c9db3e44f29e254c0393ecca3929` (`07_02`)다. `07_03`과 `07_04`는 문서·QA·Finalization 정책 변경이 중심이며 R6J에서 요구한 Product Source 재개발 결과 SHA가 아니다.

현재 문서 작업에서 직접 실행하지 않은 범위:
- Java 25 / Gradle 9.1 전체 build·publication·release qualification
- MariaDB/PostgreSQL/Oracle fresh install·migration·rollback·reapply·large lookup
- ADM/BZA authenticated Chromium/Firefox/WebKit E2E와 action permission role matrix
- Kafka/JMS/IBM MQ/RabbitMQ/TCP 실제 provider runtime 및 multi-instance
- Process Kill, lease/fencing, stale owner, response loss, durable UNKNOWN/reconcile
- FileLog disk-full/read-only/process-kill/spool/replay/loss alert
- authoritative metric/log/trace/timeline/alert/audit observability qualification
- Gateway ACK/NACK/PARTIAL/LKG 및 multi-instance reconciliation
- Rolling/Blue-Green/Canary, Backup/Restore, DR failover/failback
- performance/capacity/resource exhaustion, security negative corpus, artifact repository/supply-chain, Codex independent review

따라서 이 항목들은 `미검증`으로 유지하며 Product Release PASS 근거로 사용하지 않는다.
