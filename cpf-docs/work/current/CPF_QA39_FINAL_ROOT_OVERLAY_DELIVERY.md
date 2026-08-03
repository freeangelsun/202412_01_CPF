# CPF QA39 Final Root Overlay Delivery

## 적용

ZIP 내부의 Root 상대경로를 Repository Root에 덮어쓴다. 적용 후 `CPF_QA39_FINAL_CLEANUP.ps1`을 한 번 실행하면 기존 Canonical 이동 대상과 이번 작업의 중간 산출물만 정확한 Delete Manifest에 따라 정리된다.

Cleanup에는 Build, Test, Lint, Gradle, Node, Docker, Git 검증이 없다.

## 핵심 교정

- Starter 38개를 6 Public Profile·32 Internal Leaf·7 Owner Group으로 정본화
- Notification·Security 중첩 이동에서 유실된 `src` 구조 복구
- Public/Internal BOM 분리 및 Gradle `platform(...)` 함수 충돌 제거
- Feature Flag·Resilience API/SPI/Consumer·ADM UI·OpenAPI·3 Vendor SQL 추가
- Batch Provider 중립 Control Port와 Composite Worker Identity 적용
- Broker Client Adapter를 `cpf-core.internal`로 이동
- Naming Gate를 기준 HEAD Legacy 인식 방식으로 교정하여 기존 `common.broker` 계약 오탐 제거
- 오류 일괄 수집 Gate 유지

## 최종 상태

구현과 정적 검증은 완료했다. 전체 Build·Frontend·DB·Runtime·Publication은 Codex 검수 직전 단일 검증 회차로 남겨 두며 성공으로 기록하지 않는다.
