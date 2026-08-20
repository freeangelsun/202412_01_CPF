# Codex 독립 문서 검토 요청

기준 Source SHA: `92169d9918dd176e8322ac2f9dfc29ebe1d2ea12`

## 검토 목적

개발 GPT가 작성·자체검수한 공식 7종 문서를 실제 Source 기준으로 독립 재검수한다. 기존 PASS 보고를 신뢰하지 말고 Source/Consumer/Config/Test와 문서 내용을 다시 대조한다.

## 우선 검토

1. README의 Generated Domain과 Batch Capability 경계가 Source/최종 정책과 어떻게 맞는지 확인한다.
2. 02의 Public Profile/Provider/공통 기능 선택 및 build.gradle 예제가 실제 사용자 Golden Path인지 확인한다.
3. 03의 Batch Annotation, Job/Step, Partition/Worker/Scheduler/Recovery 계약을 실제 Source와 대조한다.
4. 04/05의 ADM 운영 Route, 상태, 위험 Action, Recovery 절차가 실제 Consumer와 일치하는지 확인한다.
5. 06의 Gateway Route/Auth/Rate/Retry/Runtime Apply/API 계약을 실제 Source와 대조한다.
6. 07의 Public API/SPI/DTO/Config/State/Error/DB/HTTP 계약과 Source reference를 표본이 아니라 핵심 계약 전체 관점에서 검수한다.
7. `CPF_DOCUMENTATION_TO_DEVELOPMENT_REVIEW.md`와 `CPF_DEVELOPER_USABILITY_REVIEW.md` 항목의 타당성을 독립 판정한다.

## 검수 규칙

- 문서가 Source에 없는 기능/API를 생성하지 않았는지 확인한다.
- Source 구현이 사용자 확정 계약과 충돌하면 문서를 억지로 Source에 맞추지 말고 별도 개발 보완 대상으로 판정한다.
- 문서 역할 분리를 유지한다. 개발·운영·정확한 Reference가 서로 중복 Owner가 되지 않게 본다.
- Codex 검수 결과는 Codex 영역에만 기록하고 개발 GPT/QA 상태를 임의 수정하지 않는다.
- Git Commit/Push/Branch/Delete는 사용자 승인 없이 수행하지 않는다.
