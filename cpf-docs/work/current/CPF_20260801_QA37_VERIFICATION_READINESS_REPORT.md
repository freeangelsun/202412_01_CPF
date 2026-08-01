# CPF QA37 개발 보정 및 Codex 검수 준비 보고

기준 SHA: `1edd96c6dcc69b0b4d6e9e22a0709d910d7cfb04`

## 완료 오판 원인

이전 보고가 잘못된 이유는 다음 세 가지다.

1. Overlay 단독 정적 PASS를 전체 Repository 통합 완료로 확대 해석했다.
2. Handler·Test 파일 수를 실제 Product Consumer와 Runtime 검증 완료로 잘못 해석했다.
3. Matrix에 `미검증·재확인 필요`가 남아 있는데도 완료 문구와 `FULL_COMPLETION` 산출물명을 사용했다.

## 재발 방지

전체 완료는 아래 8개가 exact result SHA에서 모두 `완료/exitCode=0`일 때만 허용한다.

1. merged Repository Source Gate
2. Java 25 Fresh Build
3. ADM/BZA Frontend Clean Verify
4. Oracle·PostgreSQL·MariaDB Runtime Lifecycle
5. Fault/Recovery Runtime
6. Browser Runtime
7. Supply-chain
8. exact result SHA Evidence

`verify-cpf-qa37-completion-truth.py`가 미충족 상태의 완료 문구와 `FULL/COMPLETION` Package ID를 차단한다.

## Source 구조 보정

- EDU를 `cpf-reference` 단일 모듈과 `refDB`에 격리
- 생성형 도메인과 제품 BZA 의존 제거
- ID 중심 Package를 기능 중심 Package로 재배치
- Batch를 `com.cpf.reference.batch`와 `CPF_REF_BAT_*` SQL Pack으로 묶어 통째 제거 가능하게 구성
- Operations·Backoffice·Gateway Simulator를 optional Package로 격리
- 135개 Handler를 JDBC·HTTP·File·Process·Outbox·Spring Batch·REF Gateway Concrete Consumer에 Binding
- Query 변경 시 3 Vendor·V93/V94·U93/U94·Runtime Query·Verify·Checksum·Generator 제외 계약 동시 검증

## 현재 정확한 상태

- Overlay에서 수행 가능한 Source/구문/정적/결정론적 Self-test: PASS
- merged Repository와 요구 버전 Runtime 검증: 미검증
- 전체 판정: `미검증`
- Codex 투입 준비: 완료

Codex는 `cpf-docs/work/codex/qa37/CPF_CODEX_QA37_FINAL_INDEPENDENT_VERIFICATION_REQUEST.md`의 Stage 0부터 시작한다. Stage 0 실패 시 Java/npm/DB/Browser를 실행하지 않는다.
