# R4 Codex·QA 독립 검수 요청

R4는 개발 GPT가 실제 구현·Java21/Node/Python 대체 Runtime·자체검수를 수행한 제출 후보이며 QA 판정은 변경하지 않았다.

## 우선 독립 검수 대상

1. Batch 위험명령 `expectedVersion`, Approval ALL/ANY/N_OF_M, Request Fingerprint, V100/R100 Ledger가 ADM → BAT Owner까지 닫혔는지 확인한다.
2. Batch Abandon의 `ABANDONING` 선점, 동시 호출 1회, UNKNOWN 대사, V99/R99 3 Vendor Lifecycle을 확인한다.
3. Runtime Command의 사전 실패 `FAILED`, 외부 호출 이후 불명확 `UNKNOWN`, Target 중복 차단과 부분 결과 보존을 확인한다.
4. ADM/BZA Generated Mutator가 Actor Alias와 비인가 `operatorId`를 차단하며 BZA가 ADM Login 예외를 상속하지 않는지 확인한다.
5. Break-glass·Attachment·Session·Maintenance 위험조치가 Generated operation, 사유, 권한, 마스킹, 접근성에 연결되는지 확인한다.
6. DNS 주소 검증과 socket address pinning이 DNS rebinding·mixed private/public 결과를 차단하는지 확인한다.
7. Work Package 291개와 Requirement 10,558개가 개별 Acceptance·Scenario·Source·Consumer·Evidence를 연결하고 QA/Codex 영역을 변경하지 않았는지 확인한다.
8. `run-cpf-r4-exact-head-validation.ps1`로 Java25·3 DB·Browser·실제 Audit Runtime을 실행한다.

## QA 상태

개발 GPT 구현 및 자체검수 완료 후보이며, 최종 완료는 QA 통과 전까지 인정되지 않는다.
