# QA Requirement Detail — CPF-SELF-DEV-S4-006

## 판정

- QA 결과: `미통과`
- 기준 SHA: `2903de14eb9cd6cfcccf8e4d2a3489ee2e4193ca`
- Requirement: ADM/BZA/Batch 운영자 신뢰경계·중첩 Actor 제거
- QA 회차: `QA-DEV-R1`

## 실제 확인 파일

1. `cpf-tools/scripts/verify-cpf-operator-trust-boundary.py`
2. `cpf-tools/scripts/tests/test_verify_cpf_operator_trust_boundary.py`
3. `cpf-admin/frontend/src/shared/cpfApi.ts`
4. `cpf-biz-admin/frontend/src/shared/cpfApi.ts`
5. `cpf-admin/src/main/java/com/cpf/admin/common/base/AdmBaseController.java`
6. `cpf-admin/src/main/java/com/cpf/admin/opr/batch/runtime/BatchRuntimeControlController.java`
7. `cpf-admin/src/test/java/com/cpf/admin/opr/batch/runtime/BatchRuntimeControlControllerActorTest.java`
8. `cpf-docs/work/evidence/20260803/session4/P03_OPERATOR_TRUST_R2_TARGETED.json`
9. `cpf-docs/work/evidence/20260803/session4/JAVA21_SUBSTITUTE_VALIDATION.json`
10. `cpf-docs/work/evidence/20260803/session4/FRONTEND_SUBSTITUTE_VALIDATION.json`

## 확인된 구현

- ADM Base Controller는 `adm.operatorId` Request Attribute를 필수로 요구한다.
- Batch Controller는 Actor Alias 5종을 중첩 Map/List에서 제거하고 인증 Actor를 `requestedBy`로 재주입한다.
- ADM/BZA 공유 Client는 JSON Body와 Query의 Actor Alias를 차단한다.

## 미통과 근거

1. Targeted Evidence의 실제 Scan은 Frontend Source 2개, Controller 1개다.
2. Java 21 Evidence는 Batch Controller와 Test만 임시 Spring/JUnit Stub과 함께 Compile한다.
3. Frontend Evidence는 공유 `cpfApi.ts`만 임시 Dependency Stub으로 Typecheck/Node/Chromium Harness를 실행한다.
4. 실제 ADM/BZA 전체 App Consumer·Route·Generated Client Build를 검증하지 않는다.
5. 인증 Filter가 `adm.operatorId`를 설정하는 경로부터 모든 위험 Controller·Owner Command·Audit까지 End-to-End로 검증하지 않는다.
6. 기존 BZA `bzaQuery/bzaMutation/bzaApi/bzaInvokeOperation` 호환성 회귀 위험이 있다.
7. Raw String/FormData/URLSearchParams Body는 Object Property Guard를 우회할 수 있다.
8. Trust Violation Error 분류와 HTTP 응답 계약이 Endpoint별로 일관되게 검증되지 않았다.

## 재개발 요청

- 기존 BZA Public API 호환 Alias 또는 전체 Consumer Migration
- ADM/BZA 전체 Typecheck·Build·Unit·Playwright
- Auth Filter → Request Attribute → Controller → Owner → Audit End-to-End Test
- JSON/FormData/URLSearchParams/Raw Body Actor Alias Negative Matrix
- 모든 위험 Mutation Endpoint의 인증 Actor와 Audit Actor 일치 검증
- Java 21 대체검증과 Java 25 전용 검증 분리 기록

## 성공 기대 결과

- Browser Actor Alias 5종 × 모든 Body/Query 경로 차단
- 실제 ADM/BZA Consumer Compile 오류 0
- 인증되지 않은 Actor로 Owner 호출 0
- Owner/Audit Actor가 인증 Session Actor와 동일
