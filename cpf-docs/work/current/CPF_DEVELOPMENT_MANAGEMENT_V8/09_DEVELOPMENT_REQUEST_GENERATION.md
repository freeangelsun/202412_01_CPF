# 개발 요청 생성 결과

생성 위치:

`cpf-docs/work/current/CPF_DEVELOPMENT_MANAGEMENT_V8/generated/campaigns/<campaign_id>/`

포함 파일:

- `DEVELOPMENT_SESSION_ASSIGNMENTS.csv`
- `DEV-..._REQUEST.md`
- `CAMPAIGN_MANIFEST.json`

각 요청에는 Baseline SHA, Work Package, Priority, Owner, Dependencies, Requirement/Scenario Map 참조, 허용·보호 경로, 필수 제품 결과, 구현 제안의 비강제성, 제출 Template가 들어간다.

Session 수는 Active Scope와 `MaxItemsPerSession`으로 매번 계산되며 프로젝트 전체를 영구 고정 분할하지 않는다.
