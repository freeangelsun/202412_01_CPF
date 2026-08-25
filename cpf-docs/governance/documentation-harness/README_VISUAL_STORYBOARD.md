# CPF README Visual Storyboard v1.2

README에는 목차를 두지 않는다. Hero 다음에 CPF 전체 Architecture Map을 먼저 보여주고 번호형 H1/H2를 이어간다. 대메뉴·중메뉴는 번호와 차등 여백으로 구분한다.

| 순서 | 섹션 | 핵심 Value | 권장 Visual | 설명 규칙 |
|---|---|---|---|---|
| Hero | CPF / Core Platform Framework | 한 줄 제품 정의와 제품 인상 | README_HERO | 보조 문장은 1~2줄 |
| Architecture | Hero 직후 | Domain·Framework·Gateway·Backoffice·Batch·DB·Operations 위치 | README_ARCHITECTURE_MAP | 그림 아래 전체 구조 1~2문장 |
| 1 | 호출·오케스트레이션 | Same JVM/Remote Domain Invocation, System6, Trace Identity | SPLIT_COMPARE / OPERATIONS_TRACE | Topology가 바뀌어도 업무 계약 유지 강조 |
| 2 | 거래 상태와 복구 | Local/Remote 경계, UNKNOWN/Reconcile, Idempotency, Saga/TCC/XA | RECOVERY_STATE_MAP | 실패를 억지 성공/실패 판정하지 않는 이유 |
| 3 | Batch 실행/제어/복구 | 역할, 4가지 복구, Lease/Fencing | BATCH_CONTROL_EXECUTION_MAP | 운영자가 무엇을 선택하는지 |
| 4 | Domain/Starter/DB Canonical | Generator, Starter/Provider, DB3 | LIFECYCLE_PIPELINE | 생성부터 Runtime까지 연결 |
| 5 | Gateway/Integration/Backoffice Boundary | Gateway 선택/미선택, Owner Domain | GATEWAY_OPTIONALITY + OWNERSHIP_BOUNDARY | 내부 Domain↔Domain은 Gateway 미경유를 명시 |
| 6 | Operations Trace/Safe Control | IDs, Log/Trace/Timeline, Approval/Audit | OPERATIONS_TRACE | 거래 추적과 안전 조치 |
| 7 | Framework Common Capability | 공통 Capability | CAPABILITY_LANDSCAPE | 뻔한 기능 나열보다 직접 편의 위주 |
| 8 | Bootstrap/Build/Test/Runtime | 개발환경 준비와 실행 | LIFECYCLE_PIPELINE 또는 COMMAND JOURNEY | 실제 Source 검증 명령만 사용 |
| 9 | 역할별 매뉴얼 | Developer/Batch/Operator/Gateway/Spec/Architecture/DB | 그림 불필요 | 링크 + 한 줄 목적 |
| 10 | License | Community & Evaluation License | 그림 없음 | 고정 한 문장 |

핵심 장점은 한 문단에 Bold 문구를 연속 나열하지 않는다. 한 줄 한 메시지와 일관된 marker/bullet을 사용한다. 의미 있는 그림에는 바로 아래 간결한 한국어 설명을 둔다.
