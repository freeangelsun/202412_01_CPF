# CPF QA Closure Follow-up Implementation Report — 20260729_01

## 기준 SHA

`9e5d1676a9ccba55fedf4dfb633a9e710f487a02`

## 발견 결함

Notification Durable Outbox가 `PROCESSING` Claim 이후 Worker Crash가 발생한 경우 만료 Lease를 회수하지 못했다. 후보 조회가 `READY`, `RETRY`만 대상으로 하므로 운영자가 Retry/Cancel할 수도 없는 영구 고착 상태가 발생했다.

## 조치

1. 매 Poll 시작 시 만료 `PROCESSING`을 `UNKNOWN_RESULT`로 격리한다.
2. Provider 결과 불명 거래는 자동 재발송하지 않는다.
3. Lease, next_attempt_at을 제거하고 Version과 운영 수정자를 갱신한다.
4. Unit Test로 UNKNOWN_RESULT, PROCESSING 조건, 오류 코드, 자동 RETRY 금지를 고정한다.
5. Notification Portable SQL Gate가 Outbox 구현까지 검사하도록 확대한다.

## 판정

- Source: 완료
- Test Source: 완료
- Static Gate Source: 완료
- Gradle 실행: 미검증
- 공식 3 DB 실행: 미검증
- 다중 Instance Crash Recovery: 미검증
