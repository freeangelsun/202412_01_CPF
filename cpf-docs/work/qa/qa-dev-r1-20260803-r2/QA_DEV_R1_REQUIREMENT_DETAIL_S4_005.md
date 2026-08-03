# QA Requirement Detail — CPF-SELF-DEV-S4-005

## 판정

- QA 결과: `미통과`
- 기준 SHA: `2903de14eb9cd6cfcccf8e4d2a3489ee2e4193ca`
- Requirement: Transaction ID·표준 실행 ID 정본 Gate
- QA 회차: `QA-DEV-R1`

## 실제 확인 파일

1. `cpf-tools/scripts/verify-cpf-transaction-id-standard.py`
2. `cpf-tools/scripts/tests/test_verify_cpf_transaction_id_standard.py`
3. `cpf-tools/scripts/check-transaction-id-standard.ps1`
4. `cpf-core/src/main/java/com/cpf/core/common/logging/TransactionIdGenerator.java`
5. `cpf-core/src/main/java/com/cpf/core/common/header/CpfInboundHeaderValidator.java`
6. `cpf-core/src/main/java/com/cpf/core/common/web/TransactionHeaderValidationInterceptor.java`
7. `cpf-admin/frontend/src/shared/transaction.ts`
8. `cpf-starters/profiles/web-api/src/main/java/com/cpf/starter/profile/webapi/internal/openapi/CpfOpenApiAutoConfiguration.java`
9. `cpf-docs/work/evidence/20260803/session4/P03_TRANSACTION_ID_R2_TARGETED.json`

## 소스상 확인된 구현

- Transaction ID 형식은 17+3+7+7, 총 34자리다.
- Server Generator와 ADM Frontend Generator가 같은 길이 계약을 사용한다.
- Inbound Validator는 필수 Header와 Transaction ID 형식을 검사한다.
- OpenAPI는 거래 Annotation이 있는 API에 표준 Header를 문서화한다.

## 치명적 미통과 근거

1. 제출 Evidence의 `executionAnnotationCount`가 `0`인데 `status=PASS`다.
2. Gate는 Annotation이 0개인 Repository를 실패시키지 않는다.
3. `TransactionHeaderValidationInterceptor`는 Handler에 거래 Annotation이 없으면 즉시 `true`를 반환한다.
4. 따라서 실제 제품 Controller가 Annotation 0건이면 업무 Header·표준 실행 ID 검증이 모든 Endpoint에서 건너뛰어진다.
5. Gate는 Controller/Route 전체와 Annotation Coverage를 대조하지 않는다.
6. `globalId`, `gid` 등 Legacy 식별자 성공 기준도 전체 Repository에 강제하지 않는다.
7. Evidence와 명령은 최신 Push SHA가 아닌 Targeted Fixture 기준이다.

## 재개발 요청

- 모든 업무 Controller/Endpoint를 거래 대상 또는 명시적 Allowlist로 분류
- 업무 Endpoint의 표준 Annotation Coverage 100% 강제
- Health, Swagger, Callback 등 제외 Endpoint는 근거 있는 Allowlist로 관리
- Annotation 0건·미부착 업무 Endpoint Negative Test 추가
- Legacy `globalId/gid` Source·SQL·API·Frontend 전수 Scan 추가
- 최신 exact SHA에서 Header/Annotation/Controller Coverage Evidence 생성

## 성공 기대 결과

- 업무 Endpoint 미부착 0건
- 제외 Endpoint는 목적·Owner·근거가 명시된 Allowlist와 일치
- Annotation 수와 Controller/Operation 수가 Evidence에 기록
- Unannotated 업무 Endpoint가 Gate에서 즉시 실패
