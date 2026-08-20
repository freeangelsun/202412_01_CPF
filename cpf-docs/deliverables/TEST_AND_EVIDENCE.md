# TEST AND EVIDENCE — Current Canonical / Local Validation

## 1. Current Source Identity

- Current local full-source ZIP: `CPF_FULL_SOURCE_FOR_NEXT_QA_20260820_122758(1).zip`
- SHA-256: `f73988097aef77a1bcc795ba66394326dd5a9f875a2d1b530e2c99e315cf5ceb`
- Files in supplied ZIP: `8,288`
- `.git`: supplied ZIP에는 없음. exact Git SHA를 추정하거나 과거 SHA로 대체하지 않는다.

## 2. Canonical Currentization Verification

- `CPF_FINAL_TARGET_REQUIREMENTS.md` Current Catalog: **205 Requirement / duplicate ID 0**
- Current canonical entrypoint는 Final Target / Path Map / Document Index / Current Work Request / Requirement Status / Deliverables Evidence+Open Issues / Current Handover로 단일화한다.
- `cpf-docs/deliverables/`를 Current Evidence/Issue/Package 산출물의 단일 위치로 사용한다.
- `cpf-docs/work/`의 동명 Evidence/Open Issues/QA Rework/Change Manifest/Package Manifest와 과거 Work Request/Handover는 Current Truth 경쟁을 막기 위해 제거한다.
- `STEERING_INTERPRETATION.md` 삭제 전에 System6 trust, Backoffice boundary, Optionality, instanceId, Public Distribution, EDU 20+15 등의 의미가 Final Target에 존재함을 검산했고, 상대적으로 약했던 `operationId != executionId` 및 Gateway/Direct 동일 보안·자동 fallback 금지를 Final Target에 명시적으로 흡수했다.

## 3. Latest User-local Gradle Integration Validation

실제 사용자 로컬 `clean build --continue --stacktrace` 실행 결과는 **FAIL**이다. 부분 PASS를 전체 PASS로 승격하지 않는다.

- Result: `BUILD FAILED`
- Duration: `7m 22s`
- Actionable tasks: `355` (`354 executed`, `1 up-to-date`)
- Gradle summary: **Build completed with 9 failures**

실패 Task:

1. `:internal:platform-operations:runtime-control:compileJava` — JdbcTemplate query overload ambiguity 5건 + undefined `operationId` 1건
2. `:apps:admin:frontendBuild` — backend contract test가 `request.setRequestUser(operator)` 과거 계약을 요구
3. `:apps:backoffice:compileJava` — removed/relocated Public API·dependency 참조(`CpfRestController`, `CpfPasswordEncoder`, attachment API) 34 errors
4. `:framework:integration:compileTestJava` — `CpfDomainOperationRegistry.invoke(...)` 변경 후 Test consumer parity 누락
5. `:internal:integration:ai:test` — `timeoutWithUnsafeFallbackIsUnknown()` 1 fail
6. `:internal:integration:iso8583:compileTestJava` — `CpfIso8583Codec` constructor contract/Test 불일치
7. `:internal:platform-operations:health:test` — `drainBlocksNewWork()` 1 fail
8. `:starters:messaging:ibm-mq:test` — null/blank header provider-call-before validation contract 1 fail
9. `:starters:messaging:jms:compileJava` — `writeContextProperties(...)` checked Exception 미처리

이 9건은 개발 시작 시 Source/Test/Consumer/Owner 기준으로 Root Cause를 묶어 보정하고 재검증한다. Test expected만 낮춰서 PASS로 만들지 않는다.

## 4. Environment / Warning Evidence

- ADM Frontend Node: `v24.11.0` — declared Node range `>=22.18.0 <25` 안에 있음.
- npm actual: `11.6.1`, package requirement: `10.9.2` — engine warning이므로 개발 시 toolchain policy 정합화 대상.
- `glob@10.5.0` deprecation/security warning 존재.
- 일부 Source 한글 주석이 깨진 형태로 로그에 노출되어 encoding/source corruption 전수 확인 대상.

## 5. Current QA Interpretation

- Canonical document role collision cleanup: **이 Overlay 범위에서는 구현 완료 후 정적 검증 대상**
- Product development status: **미완료**
- Latest local verification status: **실패**
- Overall product completion: **완료 아님**
- Live DB3, Multi-WAS/process-kill/recovery, Browser E2E 등 미실행 Runtime은 계속 `미검증`이다.

## 6. Evidence Rule

Current Evidence는 이 파일 하나를 사용한다. 삭제되는 `cpf-docs/work/TEST_AND_EVIDENCE.md`의 과거 PASS를 Current PASS로 승계하지 않는다. 과거에 유효했던 설계 판단은 Final Target에 흡수하고, 실행 결과는 현재 Source에서 재검증한다.
