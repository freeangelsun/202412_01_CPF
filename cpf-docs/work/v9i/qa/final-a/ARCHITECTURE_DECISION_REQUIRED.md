# ARCHITECTURE DECISION REQUIRED

Basis: `b4b6b18b43e9ff83436ceb8b1816b31594e8d6eb`

1. **Release Qualification trust root**: Gate URL/token/HMAC authority를 사용자 공급값만으로 신뢰하지 않고 실제 CPF deployment/artifact에 결속하는 중앙 trust model(mTLS/cert/SPIFFE/CI-pinned authority 등)을 확정해야 합니다. 이는 `QA-A-FS1000-NEW-003`의 공통 Root Cause입니다.
2. `cpf_transaction_lineage`는 지침대로 **Primary Transaction Store가 아니라 normalized operational projection/index**로 유지하며 현재 port/adapter 방향은 이 원칙에 부합합니다. 별도 dual-primary로 확대하지 않습니다.
3. Online/Batch 다중도메인 Reference Sample은 Product 업무원장을 새로 만드는 방식이 아니라 범용 선택형/Generated Reference로 설계하되 실제 Domain A→B→C(/D) transaction/recovery 계약을 실행 가능하게 해야 합니다.
