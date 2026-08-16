# CPF Developer Usability Review — 개발 GPT 판정

- 기준 Source: `92169d9918dd176e8322ac2f9dfc29ebe1d2ea12` + 이번 미커밋 Overlay
- 입력 리뷰: `CPF_DEVELOPER_USABILITY_REVIEW(2).md`
- 원칙: 기능 경계를 UX 편의 때문에 합치지 않고, Canonical Public Catalog와 실제 Consumer를 기준으로 판정한다.

| ID | 판정 | 개발 대응 |
|---|---|---|
| UX-001 | 부분 수용 - 개발 반영 | `domain dry-run/preflight`에 Canonical Public Catalog 기반 `selectionSummary` 추가. 선택 Public artifact/config prefix/runtime/Batch 선택 Owner를 노출한다. |
| UX-002 | 수용 - 개발 반영 | `preset=minimal`에서 persistence/http/resilience/cache/messaging/object-storage/sample 활성화를 fail-closed. 현재 선택/권장 조합/YAML 수정 경로까지 오류에 포함한다. |
| UX-003 | 부분 수용 | Generated Domain은 `online/`만 생성한다. `sampleTransaction=false`면 Sample Transaction을 만들지 않는다. 공식 MBR/EXS 회귀 Golden Path는 명시적으로 Sample을 유지한다. |
| UX-004 | 수용 방향 - 개발 반영 | 사용자 Summary는 별도 하드코딩 Catalog를 만들지 않고 기존 Canonical Catalog `visibility/ownerGroup/role/configPrefix`에서 파생한다. |
| UX-005 | 부분 수용 - 개발 반영 | 이번 변경으로 발생 가능한 preset 충돌은 해결 행동까지 제시한다. 기존 validation도 field path fail-fast를 유지한다. |
| UX-006 | 수용 - 기존 계약 강화 | Generated Domain direct dependency는 Public Starter만 허용하며 Summary에서 `internalArtifactsDirectlyExposed=[]`를 회귀 검증한다. |
| UX-007 | 수용 - 개발 반영 완료 | Batch는 `PROJECT_SETUP` 선택형 Capability. Domain Generator는 Batch 업무 Source/Runtime skeleton을 생성하지 않는다. |

## 검증

- `cpf-tools/verification/nxt3/verify_generator_presets.py`: minimal positive + minimal override negative + selectionSummary/Public-only 회귀.
- `cpf-tools/generator/engine/cpf_domain_generator.py`: minimal fail-closed, selectionSummary, Batch project-setup 경계.
- `cpf-tools/generator/contracts/cpf-domain.schema.json`: minimal preset schema constraint.
- Focused Evidence: `cpf-docs/work/evidence/current/GENERATOR_PRESET_UX.log`.


## V3 개발자 진입점/환경 설정 보완

- Public Starter 선택면은 `cpfModules`, 내부 전체는 `cpfModulesAll`로 분리한다.
- 실행 진입점은 `cpfRunLocal`, `cpfRunAdm`, `cpfRunBza`, `cpfRunGateway`, `cpfRunBatch`, `cpfRunEducation`로 구분한다.
- 환경 자원정책은 `gradle/cpf-runtime/common.properties` + `local/dev/test/stg/prod.properties`에서 중앙 관리한다.
- 메모리 단계는 250/500/750/1000MB, CPF JVM Heap 상한은 1000MB다. 특정 모듈에 `cpf-resource.properties`가 있을 때만 모듈 설정을 최우선 적용한다.
- 로컬 기본은 단일 통합 WAS이며 Batch는 Project Setup 선택형 Capability이므로 기본 기동하지 않는다.
