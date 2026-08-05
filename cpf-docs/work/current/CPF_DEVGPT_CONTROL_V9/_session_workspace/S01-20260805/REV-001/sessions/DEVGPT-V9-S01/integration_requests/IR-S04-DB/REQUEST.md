# IR-S04-DB — Distributed Store·Migration·3 Vendor 검증

- Parent request: `DEVGPT-V9-S01`
- Integration owner: `DEVGPT-V9-S04`
- Baseline SHA: `fc207ac5560da59f352ee0c5f83199177f2987b4`
- Status: `미완료 / 재확인 필요`

## Required implementation and validation

1. Oracle, PostgreSQL, MariaDB용 distributed `CpfLockStore` Provider와 fencing sequence/row-version/lease CAS를 구현 또는 기존 정본 Provider 경로를 명시한다.
2. service-identity용 distributed replay guard Provider를 구현하여 다중 인스턴스 token replay를 원자적으로 차단한다.
3. State, transaction log DB, privacy/audit 저장소의 DDL/DML/index/FK/metadata를 3 Vendor에서 맞춘다.
4. Install, Upgrade, Seed, Rollback, Process Kill, lease expiration, partial commit, UNKNOWN reconcile을 실행한다.
5. monotonic fencing, stale token rejection, duplicate request replay, audit append-only 및 원문 비저장을 assertion한다.
6. 명령, Exit Code, 실제 결과, exact SHA, SQL Evidence를 `impacted_ids.csv`의 각 ID에 연결한다.

Single-JVM InMemory 구현은 분산 완료 근거가 아니다. S04 적용·Push 후 최신 `origin/master` 회귀 Evidence가 필요하다.
