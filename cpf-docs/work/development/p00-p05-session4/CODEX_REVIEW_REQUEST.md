# Codex 독립 검수 및 환경 검증 이관 요청

## 기준

- Branch: `master`
- 적용 기준 SHA: `d2adc89f344fa1f93a2f9291f6576ce69be05239`
- 이전 개발 기준 SHA: `a6856e7557f586875796172ac6ebae22bb87958e`
- 최신 Commit 변경은 보호 문서 14개뿐이며 Overlay 경로와 중첩 0건이다. 근거: `cpf-docs/work/evidence/20260803/session4/BASELINE_REBASE_AND_OVERLAP.json`
- 이 패키지는 P00~P05 개발 Checkpoint이며 10,027건 전체 QA 완료를 주장하지 않는다.

## 먼저 확인할 개발 결과

1. Split Master 30,558/40,763/30,558과 논리 1~10,027 범위를 검증한다.
2. Traceability 분할 로더가 기존 공개 함수·회귀 계약을 보존하는지 확인한다.
3. ADM/BZA `cpfApi.ts`의 Actor alias 차단과 Batch Controller의 중첩 Map/List 재귀 Sanitizer를 검토한다.
4. Java 21 실제 Source Compile, Unit Test 2건, Runtime Harness, Class Major 65 Evidence를 확인한다.
5. ADM/BZA 실제 변경 Source의 TypeScript, Node, Chromium Runtime Evidence를 확인한다.
6. DB exactly-three와 Starter Catalog/Profile/Physical/Public Visibility Gate를 확인한다.
7. 기존 QA38/Final Gate를 새 Gate가 대체하거나 약화하지 않았는지 확인한다.

## 실행 검증 이관

세부 대상은 `ENVIRONMENT_VALIDATION_HANDOFF.csv`의 각 `transfer_id` 행이 정본이다. 각 행에는 Requirement/Scenario, Owner, 완료 파일, Consumer 경로, 개발 GPT 실행 결과, 준비·실행 명령, 성공·실패 기준, 요구 Evidence, 위험을 기록했다.

- Java 25/Gradle: `CPF-XFER-S4-JAVA25`
- MariaDB/PostgreSQL/Oracle 실제 Lifecycle: `CPF-XFER-S4-DB-*`
- ADM/BZA 전체 Frontend와 Playwright: `CPF-XFER-S4-FRONTEND-*`
- 전체 Repository Owner/Transaction Scan: `CPF-XFER-S4-P02-FULL-SCAN`, `CPF-XFER-S4-P03-TX-FULL-SCAN`
- Publication/SBOM/서명: `CPF-XFER-S4-PUBLICATION-SUPPLY-CHAIN`

Codex가 환경을 보유하면 해당 행의 명령을 실행하고 Evidence를 기록한다. Codex도 환경을 보유하지 않으면 Source 결함과 환경 부재를 분리해 외부 환경 담당으로 이관하며, 같은 환경 사유를 개발 GPT에 되돌리지 않는다. Source·Test·Script 결함을 발견한 경우에만 같은 Requirement ID로 재개발을 요청한다.

## 상태 수정 권한

Codex는 Codex 영역과 Codex Evidence만 수정한다. 개발 GPT·QA 영역은 수정하지 않으며, 미실행 항목을 PASS로 승격하지 않는다.
