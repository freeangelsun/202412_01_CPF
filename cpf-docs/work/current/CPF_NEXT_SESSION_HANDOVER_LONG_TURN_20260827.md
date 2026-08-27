# CPF 다음 세션 인수인계 — LONG-TURN / External Agent 운영 고정

## 1. 다음 세션이 반드시 알아야 할 변경

CPF 개발 기본 정본에 Codex/Claude LONG-TURN MODE를 추가했다. 이후 모든 Codex/Claude 요청서와 세션 인수인계는 이 규칙을 자동 승계한다.

## 2. 외부 에이전트 작업 순서

1. 에이전트가 이미 하던 작업을 먼저 완전히 종결한다.
2. 그 뒤 남은 크레딧으로 꼭 필요한 고위험 독립검증만 수행한다.
3. 기존 미완료 전체/Repository 전체를 전수 재검수하지 않는다.
4. Build/Compile, Logging 실제 추적, DB3, Batch, Generator, Performance/Open Git을 우선한다.
5. ADM/Backoffice/Frontend/Browser는 항상 가장 마지막이며 크레딧 부족 시 생략한다.
6. 계획/중간보고 후에도 같은 turn에서 계속 실행한다.
7. 크레딧이 줄어들면 신규 WP보다 이미 시작한 WP 완결을 우선한다.

## 3. LONG-TURN 목적

서버 측 사용량/시간 제한을 우회하는 것이 아니다. 허용된 범위에서 turn을 불필요하게 끊고 재시작하거나 같은 Build/DB/Runtime을 반복해 크레딧을 낭비하지 않도록 하는 CPF 운영 규칙이다.

## 4. 병행 Local Working Tree

- Git/HEAD/전체 Local Working Tree/전체 Source Identity는 작업 시작·적용·완료 차단 Gate로 사용하지 않는다.
- 다른 세션 변경을 조사하거나 reset/restore/clean/fetch/checkout하지 않는다.
- 각 세션은 자신의 할당 범위만 처리한다.
- 제품 RT-02/Performance 자체의 provenance 검증만 해당 기능 계약 안에서 수행한다.

## 5. 사용자 Local Full Runtime 시점

외부 에이전트가 작업 중일 때 Full Runtime을 반복하지 않는다. **Codex/Claude 작업이 완전히 끝난 뒤 최종 Local Full Runtime을 실행**한다.

## 6. 다음 세션이 요청서에 그대로 넣을 문구

> **LONG-TURN MODE:** 현재 Work Package를 끝내기 전에 자의로 turn을 종료하거나 다음 진행 여부를 묻지 마. 계획·중간보고는 중단점이 아니며 같은 turn에서 즉시 작업을 계속해. 현재 하던 작업을 먼저 Source·Consumer·Test/Runtime·Evidence·문서까지 완전히 종결한 뒤 남은 크레딧으로 필수 고위험 독립검증만 진행해. 기존 미완료 전체나 Repository 전체를 전수 재검수하지 마. 같은 Root Cause와 같은 Build/DB/Runtime은 묶어서 처리하고, 크레딧이 줄어들면 신규 범위를 시작하지 말고 현재 WP를 먼저 완결해. ADM/Frontend는 필수 핵심 검증 이후 최후순위로 둬. Git/HEAD/전체 Local Working Tree 상태를 작업 Gate로 쓰지 말고 다른 세션 변경을 건드리지 마. 서버 측 사용량 제한을 우회하는 지침은 아니며 플랫폼 강제 종료 시에만 정확한 Checkpoint/Handover를 남겨.
