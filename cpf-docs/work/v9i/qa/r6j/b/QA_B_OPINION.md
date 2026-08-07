# QA B Opinion

현재 SHA `3ed676061246c9db3e44f29e254c0393ecca3929`는 R6I 이전보다 Source 구조가 크게 개선됐지만 **GA/완료 판정 가능한 상태는 아니다**.

가장 먼저 수정할 것은 Release workflow NEW-002, consumer false-green NEW-003/NEW-005, Approval UNKNOWN reconcile NEW-004, LOGFAIL NEW-006이다. 이들은 실제 제품/검증 경로를 막거나 false-green을 만들 수 있는 P0이다.

EDU는 단순 역할 문자열 일괄 치환을 권고하지 않는다. Canonical Architecture가 ADM Product 내부 기능 복제를 금지하므로 `QA_B_EDU_ARCH_CLASSIFICATION.csv`를 중앙 Architecture가 먼저 확정해야 한다. 그 결정 없이 135 수량을 맞추기 위한 generic handler 증설은 구조 부채를 다시 만든다.

Runtime은 “환경이 없으므로 나중에”가 아니라 `QA_B_RUNTIME_GAP_MATRIX.csv`의 환경·명령·필수 Evidence가 확보되는 즉시 current exact SHA에서 실행되어야 한다. 실패한 Requirement는 같은 ID로 재개발/재검수하고 QA가 다시 판정해야 한다.
