# CPF Current Work Request

## 1. 목적

이 문서는 CPF 개발·QA의 유일한 Current 개발요청 정본이다. 신규 Requirement와 Steering은 Source 수정 전에 최상위 Target과 본 문서, Canonical Development/Closure Inventory에 먼저 병합하고 Acceptance를 확정한다. 구현 결과에 맞춰 Requirement를 사후 축소하지 않는다.

작업 순서는 `정본 현행화 → Requirement/Inventory 확정 → Source/Consumer 구현 → Generator/Initializer/DB/Frontend/Config → Test/Runtime → Evidence/Completion Review`로 고정한다.

## 2. Current Closure Inventory

- Canonical Development/Closure Inventory: **190행 / unique 190**
- Developer Requirement Ledger: **218행**
- Source Authority: 사용자가 전달한 최신 Local Working Tree 전체 Source를 기준으로 하며 Git history나 과거 Evidence를 Current 성공 근거로 자동 승계하지 않는다.
- 최종 Source Identity는 모든 Source 변경과 Current-only 삭제가 끝난 뒤 `cpf-source-state.py --scope source`로 확정한다.

## 3. Current Architecture

### 3.1 Physical Runtime DB

허용되는 Current Physical Runtime DB는 정확히 다음 4개다.

- `cpfDB` — CPF Platform/Common/Admin/Batch owner
- `mbwDB` — Backoffice(MBW) owner
- `mbrDB` — 선택된 Member Generated Domain owner
- `exsDB` — 선택된 External Generated Domain owner

Current Runtime/Schema/DataSource/Seed/Query target에 별도 legacy DB를 사용하지 않는다. Upgrade/Recovery에 실제 필요한 compatibility migration은 Current Runtime과 분리된 compatibility source로만 유지한다.

### 3.2 Backoffice

- 내부 Optional Business Administration Domain: `cpf-backoffice`, SystemCode `MBW`, DB `mbwDB`
- 외부 DB-less Channel/BFF: `cpf-backoffice-web`
- 외부 Reference Frontend: `cpf-backoffice-web/frontend`
- Backoffice 미선택 상태에서도 Root Build/Publication/Installer/Verifier와 필수 CPF Runtime이 정상이어야 한다.

### 3.3 Unified CLI / Developer Workspace

공식 Tooling Interface는 Java owner 기반 `cpf` 하나다.

Public commands:

`bootstrap / domain-new / domain-sync / build / test / run / stop / reset / status / help / doctor / version`

- `cpf.cmd`, `cpf.ps1`, `cpf`는 Thin Wrapper다.
- Internal command는 Public Release Capability에 물리적으로 노출하지 않는다.
- `cpf doctor`/`help`/`--version`은 Framework/CLI version, 선택 Domain/Capability, DB Vendor, Java, prerequisite, Build/Test/Run 사용법을 사람용/JSON으로 제공한다.
- `cpf build/test`는 별도 Build Engine이 아니라 Canonical Root Gradle을 사용한다.

### 3.4 Gradle / Capability

Developer task grouping은 기능 중심으로 관리한다.

- Build
- Test
- Domain
- Database
- Runtime
- Verification
- Publication
- Configuration/Discovery

Module은 Public Capability Group Owner, Internal Leaf/Foundation, Tooling, Generated/Customer Domain 중 exactly-one 역할을 가진다. Module Directory, settings, Dependency, Starter, Capability, BOM, Publication, Generator, Generated Domain, CLI, Open Git, Documentation은 동일 Canonical Catalog에서 projection한다.

### 3.5 Generated / Optional Domain Zero Dependency

다음을 모두 정상 지원한다.

- Generated Domain 0개
- 일부 Generated Domain만 선택
- 여러 Generated Domain
- Backoffice 선택/미선택
- 신규 Domain 추가
- Domain 삭제
- 삭제 후 재생성

Domain 부재는 `NOT_SELECTED/NOT_PRESENT`이며 필수 Platform Module 누락만 fail-fast한다. Domain mutation 뒤 settings/dependency/task/BOM/publication/DB/OpenAPI/Frontend/Test/Open Git stale reference는 0이어야 한다.

### 3.6 Open Git

`cpf-release/`는 Open Git 전달 전용 Current staging이며 CPF development master의 제품 Source/Git target이 아니다.

기본 `binary` Profile:

- Public Framework binary/POM/BOM/checksum/SBOM
- Public CLI
- Generated/Customer/Backoffice/Sample/EDU/Config/SQL/Test/Frontend Source
- Framework implementation Source/sources.jar/javadoc.jar/Internal Artifact/QA Evidence/Secret/history 0

Optional `source` Profile은 Canonical Public Source Allowlist만 projection한다. Release Tool은 Git add/commit/push를 자동 실행하지 않고 `VERIFIED`까지만 만든다.

## 4. Current-only / Legacy Zero

Repository는 현재 Architecture만 해석 가능하게 유지한다.

- 역할별 Current Canonical exactly-one
- 날짜·세션·checkpoint/RERUN/backup/중복 Handover·Completion·Delivery·Revalidation 제거
- Retired active Module/Route/DataSource/Seed/Config/Artifact/Entrypoint 제거
- Current Upgrade/Recovery Consumer가 없는 compatibility/history Source 제거
- 보호 경로와 Current Evidence는 보존
- 삭제는 Root-relative `DELETE_MANIFEST.csv` + Replacement/Consumer 0 + 보호경로 확인 후 수행

## 5. Completion Acceptance

개발 완료와 검증 완료를 분리한다. Source 구현·정적/계약 테스트가 끝났더라도 다음 Physical Acceptance가 실제 PASS하기 전 전체 완료로 판정하지 않는다.

- Java 25 Root Build/Test/Publication/SBOM
- Fresh VS Code Error 0 / Warning 0
- Oracle/PostgreSQL/MariaDB DB3 Physical Full Lifecycle
- Windows/Linux Unified CLI physical lifecycle
- Batch 5-role + Worker×2 kill/takeover/fencing/UNKNOWN/reconcile
- One-WAS actual transaction + rollback-surviving DB/File/Segment/Timeline logging
- ADM/Backoffice Runtime OpenAPI
- Frontend lint/typecheck/test/build + Browser E2E/a11y/error status
- Performance live/load/soak
- Actual Open Git Fresh Binary/Source Release
- Source leakage 0 / stale artifact 0
- Full Runtime `FAIL=0 / mandatory SKIP_ENV=0 / NOT_EXECUTED=0 / unresolved UNKNOWN=0`
- Source drift 0 / Managed drift 0 / mojibake 0 / legacy active DB 0
- Same Source Fresh Replay
- Canonical Inventory 190/190 Completion Review
- Codex required independent verification

## 6. Evidence 원칙

- 과거 SHA/Evidence를 현재 Source PASS로 승계하지 않는다.
- READY/PLANNED/NOT_EXECUTED/SKIP_ENV를 PASS로 기록하지 않는다.
- 각 Requirement Review에는 Source, Consumer, Test/Verifier, Runtime, Evidence, 완료 또는 미완료 사유를 명시한다.
- 민감정보 원문을 Evidence에 기록하지 않는다.
