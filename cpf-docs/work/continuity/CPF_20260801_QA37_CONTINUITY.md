# CPF QA37 Continuity

Baseline은 `1edd96c6dcc69b0b4d6e9e22a0709d910d7cfb04`다. 과거 완료 선언과 Evidence를 자동 승계하지 않는다.

## 지속 계약

- EDU Owner: `cpf-reference`
- EDU DB: `refDB`
- 생성형 도메인·제품 BZA 연결 금지
- 기능 중심 Package, 숫자 ID Package 금지
- Core V93 7 Table과 Optional Batch V94 3 Table 분리
- Batch/Operations/Backoffice/Gateway Simulator Package 단위 제거 가능
- Query 변경 시 3 Vendor·Install·Upgrade·Rollback·Runtime·Verify·Checksum·Generator 제외 계약 동시 갱신

## 완료 승계 금지

Source Candidate와 Runtime 완료를 분리한다. merged Source, Java25, Frontend, 3DB, Fault/Recovery, Browser, Supply-chain, exact SHA Evidence가 모두 PASS하기 전 전체 상태는 `완료`가 아니다.
