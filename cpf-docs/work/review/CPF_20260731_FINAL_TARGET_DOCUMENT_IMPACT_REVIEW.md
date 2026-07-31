# CPF Final Target 연관 문서 영향 검토

- Package ID: `CPF-20260731-FINAL-TARGET-DOCUMENT-SYNCHRONIZATION`
- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- Review baseline: `c1f273f1ea4fafac6fd5d23bd837adfc38a04497`
- Final Target blob: `262077e913db1d83731c0f3b643565859af431c1`
- 작성: `2026-07-31T19:07:00+09:00`
- README·공식 Guide 수정: 없음
- 사용자 Git write: 없음

## 1. 목적

상세 현행화된 `CPF_FINAL_TARGET_REQUIREMENTS.md`를 여러 AI·PC·작업자가 참조할 때 과거 QA Count, 오래된 SHA, Stack 상태, 문서 경로와 Owner 정책 때문에 오판하지 않도록 활성 정본을 동기화한다.

## 2. 수정 판정

| 파일 | 문제 | 조치 |
|---|---|---|
| `CPF_REQUIREMENT_CONTINUITY_LEDGER.md` | 162개 이력은 있으나 QA33 138개와 Canonical 162개 구분 부족 | Count·ID 체계·변경 절차·완료율 규칙 상세화 |
| `CPF_NO_PARTIAL_IMPLEMENTATION_COMPLETION_STANDARD.md` | QA32 62/60/202/23을 영구 완료 조건처럼 하드코딩 | QA 독립 영구 표준, Requirement/Package/GA 완료 분리 |
| `CPF_OSS_LICENSE_AND_SUPPLY_CHAIN_STANDARD.md` | 도구 역할은 있으나 동일 final artifact identity와 양방향 대조 부족 | Artifact별 Syft, ORT evaluate/report, tool/config/hash 강화 |
| `ADR_OSS_FIRST_PLATFORM_DIRECTION.md` | 상태가 QA32 개발용이고 과거 SHA에 고정 | Product Direction으로 승격, 현재 경계·완료 기준 상세화 |
| `CPF_BUILD_VS_BUY_MATRIX.md` | QA32 작업 요약처럼 보임 | 영구 Product Decision Matrix, Canonical Mapping 추가 |
| `CPF_STACK_SUPPORT_AND_MIGRATION_DECISION.md` | Boot 3.4.13 TRANSITION으로 현재 Source와 충돌 | Boot 4.1/Java25/Gradle9 TARGET 반영, GA와 구분 |
| `CPF_CURRENT_WORK_REQUEST.md` | QA31과 과거 SHA를 활성 작업으로 지시 | QA33 Current Request로 교체 |
| `CPF_CODEX_CONTINUITY_STATE.md` | 과거 Overlay SHA와 Handover를 가리킴 | 최신 Final Target·QA33 포인터로 교체 |
| QA33 Package/Instruction/Request/Handover | baseline과 ID 의미가 오래됨 | Canonical 162와 QA33 138/414 분리, 현재 정본 읽기 순서 |
| `CPF_DOCUMENTATION_STANDARD.md` | Final Target 경로가 filename만 표시되고 Gateway 매뉴얼 파일명 불일치 | 안전 Script로 full path와 실제 파일명 보정, 동기화 규칙 삽입 |
| Change Impact Ledger | 최신 Final Target 상세화 checkpoint 없음 | 기존 이력 보존 후 최신 checkpoint prepend |
| QA32 Next Development in `current` | 활성 QA33와 충돌 | exact blob 확인 후 current에서 제거, historical pointer 보존 |

## 3. 수정하지 않은 문서

- `README.md`
- `cpf-docs/guides/**`
- 과거 Review
- 과거 Evidence
- 과거 Completion Report
- 날짜가 고정된 과거 QA Package Matrix

이 문서들은 당시 사실 또는 사용자 별도 관리 범위다. 소급 수정하면 Evidence와 history를 왜곡하므로 그대로 보존한다.

## 4. 정본 우선순위

1. Final Target
2. Requirement Continuity
3. Architecture/ADR/Specification
4. Completion/Evidence Governance
5. Current Work Request
6. Active QA Package
7. 실제 Source·SQL·API·Test·Runtime
8. 과거 Review/Handover/Evidence

실제 구현 상태는 문서 완료 표시보다 우선하지만, 구현 변경이 Final Target과 충돌하면 Architecture/Requirement 절차를 거쳐 정본을 함께 수정한다.

## 5. 적용 방식

ZIP을 Repository Root에 풀고 다음을 실행한다.

```powershell
python cpf-tools/scripts/sync-cpf-final-target-document-references.py --root . --apply
python cpf-tools/scripts/sync-cpf-final-target-document-references.py --root . --check
python cpf-tools/scripts/verify-cpf-final-target-document-consistency.py --root .
python cpf-tools/scripts/verify-cpf-qa33-request-integrity.py --root .
```

Script는 README와 Guide를 수정하지 않는다.

## 6. Rollback

- Overlay 적용 전 `git status`, `git diff`, 대상 파일 Hash를 보관한다.
- 다른 작업자 변경이 있으면 적용을 중단한다.
- Sync Script는 예상 Git blob과 marker가 다르면 fail-closed한다.
- 사용자 승인 없이 reset/restore/clean을 실행하지 않는다.
