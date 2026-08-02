# Codex Review Index

## 기준

- master: `1eda8e12fe123281748a4388938c62f11819da1e`
- 이 패키지는 Source 구현이 아니라 Architecture·Requirement·Cleanup Overlay다.

## 검수 순서

1. `../../PRE_REVIEW.md`
2. `../../STARTERS_ARCHITECTURE_REVIEW.md`
3. Root 기준 `cpf-docs/governance/CPF_STARTER_ARCHITECTURE_AND_LIFECYCLE_POLICY.md`
4. `../../CORE_LIGHTWEIGHT_RUNTIME_DEPENDENCY_REVIEW.csv`
5. `../../FRAMEWORK_STARTER_CANDIDATE_ASSESSMENT.csv`
6. `../../NEXT_QA_CORE_LIGHTWEIGHT_STARTER_REQUIREMENTS.csv`
7. `../../DELETE_REVIEW.csv`와 `../../DELETE_MANIFEST.txt`
8. `../../POST_REVIEW.md`

## 핵심 결론

- `cpf-starters` 전체 삭제 금지
- `cpf-starters`는 정식 Fixed Product Container
- Core는 선택 Runtime을 분리해 경량화
- Domain/Product는 필요한 Starter만 명시적으로 선택
- 현재 7개는 개별 Closure와 세분화 필요
- RabbitMQ는 다음 QA의 Architecture Decision
- 50개 과거 Current 문서만 즉시 삭제 후보

## 반복 탐색 방지

전체 Repository 자유 검색부터 다시 시작하지 않는다. CSV에 기록된 Module·Dependency·Consumer·Gap부터 확인한다.
현재 master가 변경됐으면 SHA 영향 범위만 재확인하고 과거 Evidence를 자동 승계하지 않는다.

## 추가 검수 순서 — Profile·Bundle

1. `STARTER_PROFILE_AND_BUNDLE_DESIGN.md`
2. `STARTER_PROFILE_CATALOG_DRAFT.csv`
3. `NEXT_QA_CORE_LIGHTWEIGHT_STARTER_REQUIREMENTS.csv`의 034~040
4. `DOCUMENT_UPDATE_STATUS_AND_REMAINING_APPLY.md`

핵심 확인: BOM은 Runtime을 활성화하지 않으며, Profile/Aggregate가 미선택 Leaf Starter를 끌고 오지 않는지 검증한다.

## 최종 추가 검수 — Core·Base Starter

1. `CORE_AND_BASE_STARTER_FINAL_ARCHITECTURE_DECISION.md`
2. 다음 QA 041~045
3. `FINAL_INDEPENDENT_REVIEW.md`
4. `cpf-tools/scripts/apply-cpf-repository-starter-final-overlay-and-push.ps1`

검수 포인트: Core가 독립 계약 JAR로 남고 Base Starter가 선택 Runtime을 강제하지 않는지 확인한다.
