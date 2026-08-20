# CPF 개발 정본 Current-only 현행화 리뷰

## 목적

현재 Source를 정본에 맞춘 것이 아니라, CPF가 상용 Business Platform Framework로 도달해야 하는 최적 Target을 다시 정의했다. Source와 다른 부분은 `CANONICAL_SOURCE_GAP_BACKLOG.csv`로 분리했다.

## 핵심 현행화

- 누적 Amendment/Steering/History 구조 제거, Current 규칙만 유지
- Canonical Requirement 194개를 보존하고 최신 Steering 11개를 독립 Requirement로 승격해 총 205개로 명확화
- Legacy Alias 8개를 Current Catalog에서 제거
- `cpf-core` 최소 Kernel, `cpf-common` 고객 업무 공통 Owner, Starter는 Runtime 조립 Owner로 경계 확정
- System6 remote 6개 전파/Receiver 검증, Browser authoring 금지
- Operation Caller System Policy와 optional Channel Policy 분리
- Runtime instance same-host multi-process collision readiness 규칙 추가
- Generated Domain root `cpf-domain.yaml` source-controlled logical definition 확정
- environment DB Binding/Secret 분리, DB3 및 vendor 변경 fail-safe 상세화
- MBW/mbwDB Backoffice와 DB-less Backoffice Web BFF 경계 명확화
- Public Git Workspace와 Public Binary Repository 분리
- Local Bootstrap을 제품 기능으로 정본화
- EDU Online 20 + Batch 15 exact functional-group catalog 명시
- 로컬 통합 Test는 progress + Tee log + final report 원칙 반영

## 왜곡 방지 원칙

설명량을 줄이기 위해 Requirement 의미를 압축하지 않았다. 중복 문서는 제거하지만 호출경로·Owner·실패·복구·DB·Generator·Frontend·Evidence처럼 구현을 바꾸는 정보는 Final Target에 직접 유지한다.

## 다음 개발

이 Overlay 적용 후 `CANONICAL_SOURCE_GAP_BACKLOG.csv`를 현재 Source 기준으로 재검수해 Source를 Target에 맞춰 개발한다. 정본을 Source에 맞춰 되돌리지 않는다.

## 적용 Closure 결과

Overlay + Delete Manifest를 별도 Full Source Snapshot에 적용하여 삭제 잔존 0, 중복 Canonical 참조 0, History 패턴 잔존 0, 보호 Build Source 보존을 확인했다.
