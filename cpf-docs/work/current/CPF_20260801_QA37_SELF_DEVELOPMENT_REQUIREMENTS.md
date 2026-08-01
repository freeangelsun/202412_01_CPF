# CPF QA37 자체 개발요건 목록

- 기준 SHA: `23a16f35a5633ce1317920468a69fef00c1a6a41`
- Requirement Namespace: `QA37-REQ-*`
- 총 Requirement: `64`건
- 목적: latest master의 Build Source 복구, EDU 32 Source Closure, Manual EDU 135 통합 구현, Docker Runtime 검증
- 상세 Matrix: `cpf-docs/quality/CPF_20260801_QA37_REQUIREMENT_MATRIX.csv`

## 우선순위

### P0

- Root `build.gradle` 복구
- Included Build Source Closure
- Current Request·exact-SHA Evidence 복구
- EDU 32 완료 판정 해제와 실제 Source Closure
- Manual EDU 135 ID 편입과 수량 Gate
- README·Manual 보호 경계
- P0 통과 전 Codex·대규모 Runtime 금지

### P1

- Manual EDU 135 실제 기능군 개발
- Architecture Blocker·Decision·Follow-up
- Java 25·Frontend·3DB·Kafka·Redis·Browser·Fault·Supply-chain
- Root Overlay·Evidence·Codex Package

## 수용 기준

각 Requirement는 Matrix의 Owner, 변경 대상, Acceptance, Verification, Regression Protection을 충족해야 한다.

Tool·Catalog·Matrix·문서만 생성한 항목은 Product Source 완료가 아니다. 기존 Source 재사용도 실제 Source·Class·Method·Consumer·Test·latest SHA 근거가 없으면 완료 처리하지 않는다.
