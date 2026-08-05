# 병렬 개발 세션 운영과 불변 산출물

개발 세션 수는 고정하지 않는다. Active Scope를 Dependency 순서와 Owner/공유 파일 경계로 동적 분할한다.

## 불변 Campaign·Session 경로

```text
요청:
cpf-docs/work/current/CPF_DEVELOPMENT_MANAGEMENT_V8/generated/campaigns/<campaign-id>/REV-<nnn>/

결과:
cpf-docs/work/current/development-session-results/<campaign-id>/<session-id>/REV-<nnn>/
```

- 동일 Campaign/Revision 경로는 재사용하지 않는다.
- 기존 경로가 비어 있지 않으면 생성기는 실패한다.
- 과거 요청·결과·Evidence를 덮어쓰거나 삭제하지 않는다.
- 재요청은 Revision을 증가시키거나 새 Campaign ID를 사용한다.

## 충돌 방지

- Governance·Requirement/Scenario 원장·V8 중앙 State는 Session 직접 수정 금지다.
- Public API/SPI, Root Build, DB Canonical, Generator, OpenAPI Source는 Integration Owner가 단독 반영한다.
- Generated Output은 직접 편집하지 않는다.
- 일반 Module Source는 배정된 Owner Session만 변경한다.
- Baseline 이후 대상 파일이 변경됐으면 덮어쓰지 않고 충돌 요청을 제출한다.
- 다른 Session 경계가 필요하면 `CROSS_SESSION_CHANGE_REQUEST_TEMPLATE.csv`를 제출한다.
- 세션은 Session·Requirement·Scenario Result와 Artifact Manifest를 제출하고 중앙 Merge Script가 상태를 갱신한다.

## 산출물 보존

세션 결과는 고유 경로에 추가하며 다른 세션과 파일명을 공유하지 않는다. 통합 완료 후에도 사용자 승인 전에는 삭제하지 않는다.

세션 종료 인수인계에는:

- 생성·수정 파일 전수 Manifest
- 사전/최종 Hash
- 통합 대상
- 보존 대상
- 정리 가능 대상
- exact-path PowerShell 삭제 한 줄 명령

을 포함한다.

제품 필수 Source와 Test는 정리 명령에 넣지 않는다.

## 순서

1. P0 Baseline Stabilization
2. Public Contract·Ownership·DB Canonical·Generator Source
3. 실제 Provider·Consumer·Runtime·Frontend
4. 오류·부분 실패·UNKNOWN·복구·Reconcile
5. Security·Audit·Masking·Approval
6. Runtime/DB Vendor/Multi-instance/Fresh clone 검증
7. Session 결과 병합 후 QA Handoff
