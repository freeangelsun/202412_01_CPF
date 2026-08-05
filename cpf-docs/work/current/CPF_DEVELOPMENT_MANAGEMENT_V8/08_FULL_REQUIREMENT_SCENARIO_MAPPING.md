# 30,558 Requirement·40,763 Scenario 전수 Mapping

Artifact 생성 환경에서는 GitHub Connector가 약 8MB인 Split Part 원문을 반환하지 않아 실제 71,321행을 허위 생성하지 않았다. 대신 Repository Root에서 모든 Part의 Header·행 수·SHA-256을 먼저 검증한 뒤 Mapping하는 실행기를 포함한다.

## Mapping 원칙

1. 원장에 명시적 Work Item이 있으면 우선 사용한다.
2. 없으면 Canonical Requirement 아래 Work Package의 Axis·Acceptance·Scenario Class와 Requirement/Scenario 텍스트를 점수화한다.
3. Primary Work Package는 하나만 선택하고 동일 Canonical의 나머지 Package를 Supporting Scope로 기록한다.
4. 점수 0 또는 동률은 `manual_review_required=true`로 남긴다. 행을 누락시키거나 PASS로 숨기지 않는다.
5. Scenario는 Parent Requirement Mapping을 우선 승계한다.
6. 모든 Source Part SHA와 Logical Count가 맞지 않으면 즉시 실패한다.

## 완료 기준

- Requirement 30,558 / Primary 미배정 0
- Scenario 40,763 / Primary 미배정 0
- Unknown Canonical·Work Item 0
- Orphan Scenario 0
- Manual Review 건수 명시 및 최종 QA 전 해소
- `FULL_ASSIGNMENT_VALIDATION.json=PASS`
