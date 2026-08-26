# CPF README Visual Storyboard v1.3

README에는 목차를 두지 않는다. Hero 다음에 CPF 전체 Architecture Map을 먼저 보여주고 번호형 H1/H2를 이어간다. 대메뉴·중메뉴는 번호와 차등 여백으로 구분한다. H2/H3 하위 내용은 공통 Content Rail로 묶고, 주요 Visual은 다크 README 위에서 밝은 Surface로 분리한다. 모든 Visual을 네모+화살표로 그리지 않고 내용별 시각 문법을 선택한다.

| 순서 | 섹션 | 핵심 Value | 권장 Visual | 설명 규칙 |
|---|---|---|---|---|
| Hero | CPF / Core Platform Framework | 한 줄 제품 정의와 제품 인상 | README_HERO | 밝은 focal surface + editorial layer, 보조 문장 1~2줄 |
| Architecture | Hero 직후 | Domain·Framework·Gateway·Backoffice·Batch·DB·Operations 위치 | README_ARCHITECTURE_MAP | 밝은 canvas의 Layer/Plane/Ownership Zone. 수평 Box chain 금지 |
| 1 | 호출·오케스트레이션 | Same JVM/Remote Domain Invocation, System6, Trace Identity | SPLIT_COMPARE / OPERATIONS_TRACE | Split field 또는 Trace Lane. 단순 Box chain 금지 |
| 2 | 거래 상태와 복구 | Local/Remote 경계, UNKNOWN/Reconcile, Idempotency, Saga/TCC/XA | RECOVERY_STATE_MAP | State Ring/Orbit + Reconcile Branch |
| 3 | Batch 실행/제어/복구 | 역할, 4가지 복구, Lease/Fencing | BATCH_CONTROL_EXECUTION_MAP | Control Plane ribbon + Execution Lane |
| 4 | Domain/Starter/DB Canonical | Generator, Starter/Provider, DB3 | LIFECYCLE_PIPELINE | Lifecycle Spine + Milestone / Vendor Band |
| 5 | Gateway/Integration/Backoffice Boundary | Gateway 선택/미선택, Owner Domain | GATEWAY_OPTIONALITY + OWNERSHIP_BOUNDARY | Split Boundary + Nested Zone. 내부 Domain↔Domain은 Gateway 미경유 |
| 6 | Operations Trace/Safe Control | IDs, Log/Trace/Timeline, Approval/Audit | OPERATIONS_TRACE | Multi-lane Trace Rail + Event Marker |
| 7 | Framework Common Capability | 공통 Capability | CAPABILITY_LANDSCAPE | Mosaic/Cluster Landscape, 동일 카드 Grid 금지 |
| 8 | Bootstrap/Build/Test/Runtime | 개발환경 준비와 실행 | LIFECYCLE_PIPELINE 또는 COMMAND JOURNEY | 실제 Source 검증 명령만 사용 |
| 9 | 역할별 매뉴얼 | Developer/Batch/Operator/Gateway/Spec/Architecture/DB | 그림 불필요 | 링크 + 한 줄 목적 |
| 10 | License | Community & Evaluation License | 그림 없음 | 고정 한 문장 |

핵심 장점은 한 문단에 Bold 문구를 연속 나열하지 않는다. 한 줄 한 메시지와 일관된 marker/bullet을 사용한다. 의미 있는 그림에는 바로 아래 간결한 한국어 설명을 둔다.


모든 Visual은 제목·이미지·1~2문장 설명을 하나의 블록으로 묶는다. 설명은 다음 H1/H2보다 해당 Visual에 더 가깝게 두며, `[PDF]`/`[DOCX]` 문서 링크는 실제 형식과 정확히 일치시킨다. 산출물 보정은 직전 PASS Visual을 보존하는 PATCH_FIRST가 기본이다.
