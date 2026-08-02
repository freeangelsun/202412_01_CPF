# CPF Repository·문서·Gate·Starter 통합 정리 패키지

이 패키지는 이전 `CPF_20260802_REPOSITORY_DOCUMENT_GATE_CONSOLIDATION_ROOT_OVERLAY.zip`을 대체한다.

## 적용 범위

- 문서 정본·History 압축
- Repository Root Folder 역할 지도
- Generated Domain 생성·삭제 Governance
- 구형·날짜 Gate 통합 정책
- `cpf-account` 삭제 이력 조사
- `cpf-starters` 정식 `FIXED_PRODUCT_CONTAINER` 편입
- **Lightweight Core + Explicit Opt-in Starter** 정책
- 현재 7개 Starter의 Dependency·Consumer·Gap 기준선
- Framework 전체 Starter 후보·이관·금지 영역 평가
- 다음 QA Core 경량화·Starter 세분화 요청서와 45개 개발요건
- Starter 사용·선택 Guide 초안과 Guide·Deliverable 갱신 요청
- Starter Profile·Aggregate Starter·BOM 역할과 그룹 등록 설계
- Core 독립 계약·Base Starter·Common 선택 업무공통 최종 Architecture
- Profile Catalog 초안·Provider 충돌·Resolved Lock·Drift Gate 요건
- 최상위 목표 정본과 기존 Guide의 실제 미반영 상태 명시
- 과거 `work/current` 문서 50개 삭제 후보

## 적용 순서

1. 최신 `origin/master` exact SHA와 활성 Codex Working Tree를 확인한다.
2. ZIP을 Repository Root에 Overlay한다.
3. 신규 Governance·Review·Next QA 문서를 검토한다.
4. 현재 QA 변경과 경로 충돌이 없는지 확인한다.
5. 삭제는 별도 승인 후 `DELETE_COMMAND.txt`의 명령으로만 수행한다.
6. 다음 QA는 `NEXT_QA_CORE_LIGHTWEIGHT_STARTER_MODULARIZATION_REQUEST.md`를 기준으로 시작한다.

## 제외·보호

- 활성 Codex QA37 파일
- 기존 README·고객 매뉴얼·Deliverable 원본
- Product Source·SQL·Frontend
- Evidence
- `cpf-tools` 실제 Script·Workflow 수정

가이드·Deliverable은 현재 Source를 임의로 추정해 덮어쓰지 않고 다음 Starter 세분화 구현과 같은 변경 단위에서 갱신하도록 요청서만 제공한다.
삭제·Commit·Push는 수행하지 않았다.

- Codex 종료 후 한 줄로 Overlay·삭제·Commit·Push하는 안전 Script
