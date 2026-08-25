# CPF README Visual Storyboard v1.1

README에는 목차를 두지 않는다. Hero 다음에 CPF 전체 Architecture Map을 먼저 보여주고 번호형 H1을 이어간다.

| 순서 | 섹션 | 핵심 Value | 권장 Visual |
|---|---|---|---|
| Hero | CPF / Core Platform Framework | 한 줄 제품 정의와 제품 인상 | README_HERO |
| Architecture | Hero 직후 | Domain·Framework·Gateway·Backoffice·Batch·DB·Operations 위치 | README_ARCHITECTURE_MAP |
| 1 | 배포 구조가 달라도 호출 계약 유지 | Same JVM/Remote, System6, Trace Identity | SPLIT_COMPARE 또는 OPERATIONS_TRACE |
| 2 | 불확실성까지 거래 상태로 관리 | Local vs Remote, UNKNOWN/Reconcile, Idempotency, Saga/TCC/XA | RECOVERY_STATE_MAP |
| 3 | Batch 실행/제어/복구 분리 | 역할, 4가지 복구, Lease/Fencing | BATCH_CONTROL_EXECUTION_MAP |
| 4 | Domain/Starter/DB Canonical | Generator, Starter/Provider, DB3 | LIFECYCLE_PIPELINE |
| 5 | Integration/Gateway/Backoffice Boundary | Optional Gateway, Owner Domain, Integration Recovery | OWNERSHIP_BOUNDARY |
| 6 | Operations Trace/Safe Control | IDs, Log/Trace/Timeline, Approval/Audit | OPERATIONS_TRACE |
| 7 | Framework Common Capability | Cache/File/Messaging/Notification/Security 공통 기능 | CAPABILITY_LANDSCAPE |
| 8 | Start/Build/Test | Dev Shell, Generator CLI, Build/Test | LIFECYCLE_PIPELINE(앞 그림과 다른 Layout) |
| 9 | License | Community & Evaluation License 한 문장 | 그림 없음 |

하나의 Visual Type을 기계적으로 반복하지 않는다. 5~8개의 잘 만든 시각화가 14~20개의 반복 그림보다 우선한다.
