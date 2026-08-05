# IR-S02-RUNTIME — Multi-JVM·Process Kill·운영 Runtime 검증

- Parent request: `DEVGPT-V9-S01`
- Integration owner: `DEVGPT-V9-S02`
- Baseline SHA: `fc207ac5560da59f352ee0c5f83199177f2987b4`
- Status: `미완료 / 재확인 필요`

## Required implementation and validation

1. Lock/Idempotency/Resilience/Deadline/State consumers를 최소 2개 JVM 또는 분리 WAS에서 실행한다.
2. lease 경쟁, stale fencing token, owner epoch 변경, retry budget, cancellation, UNKNOWN reconcile을 동시 실행한다.
3. 처리 중 Process Kill 후 재기동하여 중복 업무 실행과 terminal loss가 없음을 검증한다.
4. local/remote 및 mixed-version 경계에서 상태·오류·감사·trace가 유지되는지 확인한다.
5. 명령, Exit Code, 실제 결과, exact SHA, 로그를 `impacted_ids.csv`의 각 ID에 연결한다.

요청서 제출만으로 완료가 아니다. S02 적용·Push 후 최신 `origin/master`의 원 Consumer 회귀 Evidence가 필요하다.
