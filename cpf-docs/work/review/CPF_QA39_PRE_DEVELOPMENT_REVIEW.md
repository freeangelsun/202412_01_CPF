# CPF QA39 Pre-Development Review

## Baseline

- SHA: `54bcc10887a83b933685bff462c0b0d7df824923`
- Previous baseline: `dafe5c0e5260ea8149234e8ab2e75347e75338c1`
- Review method: GitHub exact-file review and commit comparison
- Local fresh clone/build: 실행 환경 DNS 제한으로 미실행
- Independent CI evidence: 확인되지 않음

## Current state

- `development_status = 부분 구현`
- `verification_status = 실패`
- Runtime/DB/Frontend/Supply-chain = `미검증`

## Architecture findings

- 공식 Gradle graph에 Starter 49개가 등록돼 있다: Leaf 36개, Profile 13개.
- 공개 선택면과 내부 구현 단위가 구분되지 않는다.
- Security/Cache Aggregate는 상호 배타 Provider를 동시에 포함한다.
- Provider별 Event Profile이 별도 Artifact로 노출되어 사용자가 Provider 조합을 직접 관리한다.
- 유사 파일/전문/알림 기능이 개별 Starter로 과세분화됐다.
- 일부 신규 모듈은 settings에 등록조차 되지 않았다.

## Product-value findings

가치가 명확한 영역은 Messaging reliability, HTTP service call, security identity/resource server, cache port, file transfer ledger, attachment port, archive safety, TCP/ISO8583, notification outbox, secret provider registry다. 그러나 대부분 실제 Consumer, 운영 API, 다중 인스턴스, 결과불명, Runtime Evidence가 부족하다.

가치가 불충분한 영역은 현재 AOP service-access, Validation, Resilience, Feature Flag, Quartz thin configuration, OTLP exporter-only 조립이다. QA39에서는 제거를 기본값으로 한다.

## Development risks

- 한 줄 압축 Source와 wildcard import로 정적 결함이 숨는다.
- 상태 Matrix를 일괄 완료로 바꾼 뒤 Runtime을 미검증으로 남기는 방식이 반복됐다.
- Catalog/BOM/Evidence 정본이 분산돼 다른 AI가 stale 완료를 승계한다.
- Consumer build.gradle이 Profile와 Leaf를 중복 참조해 Ownership이 흐려졌다.

## Required development baseline

개발 전 `CPF_QA39_FINAL_REQUIREMENT_MATRIX.csv`를 고정하고 모든 Starter에 Value Contract와 제거 판정을 기록한다. 신규 Starter 추가는 금지하며 먼저 공개 Surface 축소와 기존 결함 수정을 수행한다.

## Final decision update

완전 제거·내부화·7개 유지 Group과 6개 Profile을 잠정안이 아닌 개발 기준선으로 확정한다. 개발 GPT 자체요건은 이 QA 기준선을 변경할 수 없다.
