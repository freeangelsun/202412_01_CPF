# CPF Current Work Request — QA39 Final Starter Decision and Runtime Closure

- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- Reviewed exact SHA: `9a9634eb1f28071d47c205cc35227b6d013a4536`
- Previous master SHA: `6a9890ef19ae54e6e3186ca011d5d7f984d49d9c`
- Previous QA38 baseline: `dafe5c0e5260ea8149234e8ab2e75347e75338c1`
- `development_status = 부분 구현`
- `verification_status = 실패`
- Runtime/DB/Frontend/Supply-chain = `미검증`
- Active QA Request: `cpf-docs/work/current/CPF_QA39_FINAL_DEVELOPMENT_REQUIREMENTS.md`
- Active Matrix: `cpf-docs/quality/CPF_QA39_FINAL_REQUIREMENT_MATRIX.csv`
- Active Delete Work Items: `cpf-docs/work/manifest/CPF_QA39_DELETE_WORK_ITEMS.csv`
- Developer Report Template: `cpf-docs/work/review/CPF_QA39_DEVELOPER_REPORT_AND_SELF_REVIEW_TEMPLATE.md`

## 요건 우선순위 — 필수

1. 최상위 목표와 Architecture/Specification
2. **`CPF_QA39_FINAL_DEVELOPMENT_REQUIREMENTS.md`와 `CPF_QA39_FINAL_REQUIREMENT_MATRIX.csv`**
3. `CPF_QA39_SELF_DEVELOPED_REQUIREMENTS.csv`와 개발 GPT가 추가 발굴한 자체요건
4. 구현 편의 판단·개발 리포트

**QA 개발요건과 자체 개발요건이 충돌하면 QA 개발요건이 무조건 우선한다.** 자체요건은 QA 요건을 보강할 수만 있고 약화·변경·취소할 수 없다. 충돌을 발견하면 해당 자체요건을 구현하지 말고 `CONFLICT`로 기록한 뒤 QA 요건에 맞게 수정한다.


## 최종 결정

- 완전 제거: AOP Service Access, Validation Starter, Resilience Starter, Feature Flag Starter, 미등록 FTPS/gRPC/S3/Realtime/SMB/SOAP/Webhook
- 공개 Aggregate/Profile 제거·내부화: Security/Cache Aggregate, Provider별·기술별 Profile, Quartz 공개 Starter
- 유지 Group: Data, Messaging, Integration, File, Notification, Security, Platform Operations
- Profile: minimal-domain, web-api, secure-api, browser-bff, event-service, batch-service
- OpenAPI는 web-api, Scheduler는 batch-service에 흡수

## 유지 조건

유지 Capability는 OSS Bean/메서드를 그대로 노출하지 않는다. 업무 개발자가 사용하는 간단한 CPF Public API, OSS 타입 비노출, Provider 교체, 고객사 SPI, 표준 보안·감사·마스킹, 실패·UNKNOWN·복구, 운영 추적·재처리, Generator 자동 조립을 실제 Consumer와 Runtime Evidence로 제공해야 한다.

## 개발 리포트 의무

개발 GPT는 변경 Source만 던지지 않는다. 구현 종료 전에 Developer Implementation Report와 독립 Self Review를 작성하여 exact SHA, 변경 파일/라인, Requirement 추적, Architecture 결정, 실제 명령·환경·시간·결과, 실패·미실행, 삭제·잔여참조, Evidence, 회귀 위험을 남긴다. QA는 보고를 그대로 승인하지 않지만 이 보고를 검수 진입점으로 사용한다.

## 정본·정리 규칙

- Repository Root에는 QA 안내 파일을 새로 만들지 않는다. 모든 QA 산출물은 `cpf-docs/**` 아래에 둔다.
- 활성 자체 개발요건 정본은 `CPF_QA39_SELF_DEVELOPED_REQUIREMENTS.csv` 하나다.
- 이전 QA 패키지가 만든 루트 파일과 중복·충돌 문서는 `CPF_QA39_REPOSITORY_CLEANUP_PATHS.txt`에 exact path로 관리한다.
- 제품 Source 삭제와 QA 문서 정리는 서로 다른 Manifest와 한 줄 명령으로 분리한다.
