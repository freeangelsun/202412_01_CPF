# QA 재개발·재검수 요청 — Canonical Target Currentization

최상위 Target이 Current-only 205 Requirement로 현행화되었다. 개발 GPT가 QA 상태를 임의 변경하지 않는다. QA는 기존 상태 중 아래 영향 Requirement를 재개발/재검수 대상으로 재개방할지 판정한다.

주요 영향: `ARCH-BOUNDARY`, `ARCH-STARTER`, `CMN-*`, `CPF-HEADER`, `CPF-CONTEXT`, `CPF-TXID`, `ONBOARD-DOMAIN`, `SAMPLE-MBR`, `REL-BUILD` 및 신규 `CPF-SYSTEM6`, `CPF-INSTANCE`, `CPF-OPERATION`, `GEN-DOMAIN`, `GEN-SETUP`, `DB-BINDING`, `MBW-WEB`, `REL-PUBLIC-WORKSPACE`, `REL-PUBLIC-BINARY`, `DEVEX-BOOTSTRAP`, `EDU-CANONICAL`.

세부 Source Gap은 `CANONICAL_SOURCE_GAP_BACKLOG.csv`를 사용한다.

현재 `cpf-docs/work/REQUIREMENT_STATUS.csv`에는 신규 11 Canonical ID가 없으므로 QA/중앙 관리가 원장 구조와 상태를 갱신해야 한다. Developer GPT는 QA 상태를 임의 생성하지 않는다.
