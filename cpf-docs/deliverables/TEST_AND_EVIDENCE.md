# TEST AND EVIDENCE — Canonical Currentization

## 판정

**개발 정본 Current-only 현행화: PASS**

이 PASS는 문서/정본 currentization 자체에 대한 판정이다. 새 Target에 대한 Product Source 구현/Java25 Runtime/DB3/Multi-WAS 전체가 완료됐다는 의미가 아니다. Source/QA Alignment Gap 13건은 `OPEN_ISSUES.md`와 `CANONICAL_SOURCE_GAP_BACKLOG.csv`에 남아 있다.

## 수행 검증

- Current Canonical Catalog: 205 Requirement, duplicate ID 0
- Legacy Alias 8개: Current Catalog 포함 0
- `bzaDB`, old Channel Header, callerChannel-as-System 등은 Current positive contract에서 제거하고 forbidden/negative context에만 허용
- System6/Operation/Instance/Generated Domain/DB Binding/MBW Web/Public Workspace/Public Binary/Bootstrap/EDU 35 Steering을 독립 Current Requirement로 반영
- Final Target에서 historical Amendment/Supersession/SHA/currentization section 제거
- Delete Manifest path unique 검증
- 삭제되는 canonical 문서를 요구하던 `check-work-context.ps1`, Cross-PC guide, derived Requirement row의 consumer reference currentize
- Source와 Target 차이는 Gap으로 기록; Target을 현재 Source에 맞춰 약화하지 않음
- Requirement Status/Derived Dataset/Official Guide의 후속 currentization 필요를 명시

## 미수행 / 미검증

- 새 205 Requirement Target에 대한 Product Source 전체 재개발 및 Java25 full build/runtime
- Live Oracle/PostgreSQL/MariaDB lifecycle
- Multi-WAS/process-kill/browser E2E

이들은 이번 문서 currentization 작업의 PASS로 승계하지 않는다.

## 최종 Overlay 적용 시뮬레이션

- `CANONICAL_PACKAGE_STATIC_GATE=PASS`
- Current Requirement: **205 / unique 205**
- Legacy Alias Current Catalog: **0**
- Delete Manifest: **479 unique / approved=true 479**
- 현재 DEV20 Source 복사본에서 실제 존재해 제거된 Manifest 대상: **50**
- 삭제 후 Manifest 잔존: **0**
- 삭제된 중복 Canonical 문서를 요구하는 text reference: **0** (Delete Manifest의 삭제기록 자체 제외)
- `ADR-*`, `r23~r27`, `documentation/20260816`, dated Handover/Checkpoint형 History 잔존: **0**
- `cpf-tools/build/gradle-plugin/src`: **보존 확인**
- 적용 시뮬레이션 Snapshot 파일 수: **8,233**
- `APPLY_DELETE_REFERENCE_CLOSURE=PASS`

이 검증은 별도 Snapshot 복사본에서 수행했으며 사용자의 실제 Repository에 삭제/Git write를 수행하지 않았다.
