# CPF QA B R6J Deep Re-Audit Report

## 1. 결론

- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- QA exact SHA: `3ed676061246c9db3e44f29e254c0393ecca3929`
- QA B 결과: **미통과**
- 기존 QA Finding: **40건 전수 재판정**
- 신규 Finding: **8건** (`QA-B-R6J-NEW-001` ~ `008`)
- 총 Finding: **48건** — P0 **37**, P1 **10**, P2 **1**
- Finding 단위 정적 통과: **7/48**
- 미통과/미검증: **41/48**
- R6I Requirement 원장 QA 결과: **7 통과 / 70 미통과 (77행 전수 작성)**
- Product Source 수정: **0건**
- Commit/Push/Branch/Tag/PR/Delete/Cleanup: **0건**

이번 문서는 이전 `CPF_QA_B_R6J_RESULT_3ed6760.zip`을 **SUPERSEDED** 처리하는 재검수 결과다. 이전 패키지의 빠른 대표검수 판정을 승계하지 않았다.

## 2. 실제 수행 범위

1. 최신 master exact SHA 재확인 및 canonical requirement 재독해.
2. 개발 원장 77행(AB 40 + FDEV 25 + MGR-HARDEN 12) 전수 QA 상태 작성.
3. 개발 측 핵심 Evidence log 14개를 exact SHA로 **14/14 직접 fetch** → 모두 `404_NOT_FOUND`.
4. ADM 63 Route를 63행 Matrix로 구성하고 route registry/router/session/workbench 및 전용/공유 component를 역추적.
5. BZA 26 Route는 **26/26 page source를 current exact SHA에서 직접 열람**하고 router/session/common component와 대조.
6. EDU 135는 135행 ID Matrix를 작성. QA37 verifier가 135 source/scenario/5 tests/consumer binding/parity를 어떻게 검사하는지 source를 검수했고, ADM 17은 **17/17 handler를 직접 열람**했다. 다른 118 handler는 connector archive가 없어 개별 파일 본문을 모두 직접 열지 못했으므로 PASS하지 않고 `UNVERIFIED_CURRENT_HANDLER`로 명시했다.
7. Approval Service/Repository/Recovery Worker/Owner SPI 및 주요 Owner Adapter의 UNKNOWN/reconcile 경로를 역추적.
8. Oracle/PostgreSQL/MariaDB V105 migration + 실제 R105 rollback + vendor pack 연결 검수.
9. File logging writer의 path/permission/rotation/process lock/failure path를 canonical `CPF-LOGFAIL`과 대조.
10. GitHub Release workflow와 release runner/DB3 runner의 input/clean-SHA/toolchain/runtime gate 연결을 대조.

## 3. 새로 확정한 핵심 결함

- `NEW-002 P0`: Release workflow가 `CPF_FRONTEND_URL`을 export하면서 preflight는 `CPF_ADM_FRONTEND_URL`을 검사하여 qualification을 차단.
- `NEW-003 P0`: BZA approvalInbox route metadata에 permanent HTTP 410 legacy GET가 남아 Generic Workbench/consumer gate의 false consumer가 됨.
- `NEW-004 P0`: Approval SPI default reconcile이 `ADM-RECONCILE-UNSUPPORTED`; UNKNOWN을 만들 수 있는 다수 BAT/Gateway/Broker/CenterCut Owner가 reconcile override 없음.
- `NEW-005 P0`: ADM RecoveryCenter가 동일 Reliability mutation을 action grant/expectedVersion 없이 호출하는 stale duplicate consumer.
- `NEW-006 P0`: Canonical `CPF-LOGFAIL`이 요구하는 durable spool/retransmit/dedup/loss recovery가 inspected FileLog owner에 없음. 현재 failure path는 counter + warning + false 반환.
- `NEW-007 P1`: 여러 HIGH/CRITICAL ADM 화면의 action-level UI permission projection 불일치.
- `NEW-008 P1`: BZA Approval mutation 화면도 menu 진입 권한만으로 mutation controls가 노출되는 action-level parity 문제.
- `NEW-001 P0`: ADM Product와 generic EDU-ADM 중복 구현의 canonical Architecture 충돌은 여전히 중앙 결정 필요.

## 4. EDU 135 판정

- QA37 verifier는 `45 DEV + 30 BAT + 17 ADM + 14 BZA + 14 GW + 15 OPS = 135`를 강제하고 family canonical role을 고정한다.
- ADM canonical role은 `CPF_ADM_OPERATOR`.
- current EDU-ADM handler **17/17**은 `CPF_REFERENCE_PLATFORM_OPERATOR`를 선언한다.
- 따라서 current source에서 QA37 `--compile` parity가 실제 실행됐다면 ADM role parity는 FAIL이어야 한다.
- 개발 측 `edu-adm17-compile.log` / `edu-adm17-selftest.log`는 current repository에 없음.
- 특히 ADM-08은 request payload의 `permission` 문자열이 `RAW`를 포함하는지로 원문 노출 의미를 결정한다. Server authority가 아니다.
- Canonical은 ADM 제품 내부 기능을 generic REF EDU에 복제하지 말라고 명시하므로, role 문자열만 맞추는 식의 수정은 금지한다.

## 5. Approval/Recovery 판정

Source 개선:
- 4D exact owner tuple.
- execution lease/fence/stale RUNNING→UNKNOWN sweeper.
- snapshot hash verification.
- DQ HMAC TTL + durable nonce + atomic consume + ADM-owned security gateway.
- 3 Vendor V105/R105 approval hardening lifecycle source.

미해결:
- Owner mutation 후 UNKNOWN 발생 시 `AdmApprovalService.reconcile()`은 Owner `reconcile()`만 호출한다.
- SPI default는 UNKNOWN/UNSUPPORTED이다.
- 다수 Owner Adapter가 execute에서 UNKNOWN을 만들 수 있으나 reconcile override가 없다.
- 따라서 “stale RUNNING 고착 해소”는 되었어도 “UNKNOWN 결과 확정 복구”는 제품 전체에서 닫히지 않았다.

## 6. ADM/BZA 판정

ADM:
- 63 route registry, router direct URL guard, server session menu/button projection, GET-only generic workbench는 개선 확인.
- IntegrationClosure/Approvals/Notifications 등은 action-level permission이 잘 연결됨.
- RecoveryCenter는 stale duplicate mutation 계약이 존재.
- Operators/Secrets/FeatureFlags/OpenAPI/Resilience/FileJobs 일부 direct controls는 exact action-level gating이 불균일.

BZA:
- **26/26 page source 직접 검수 완료**.
- router는 direct URL menu 권한을 403으로 차단.
- common CrudTable은 create/update/disable/PII_RAW action permission과 expectedVersion을 사용.
- 하지만 Approval Inbox/Submissions/Policies/Delegations mutation은 action-level UI projection이 일관되지 않음.
- approvalInbox route metadata에는 retired 410 legacy GET 2종이 남음.

## 7. Evidence/Release 판정

- 개발 문서가 참조하는 핵심 log 14개: **14/14 current master에서 404**.
- current commit에 Release workflow run/status Evidence 없음.
- 기존 `environment.txt`도 Java21, Java25/Gradle9.1/DB3/browser/multiprocess unavailable을 기록.
- Release runner 자체는 exact HEAD/clean tree/Java25/Gradle9.1/QA37 compile/build/publication/runtime OpenAPI/browser/DB3/multiprocess/hardening을 요구하도록 개선됨.
- 그러나 GitHub workflow preflight 변수명 결함으로 실제 qualification entry가 현재 Source에서 깨져 있음.

## 8. 완료 판정

QA B 검수 문서 작성은 완료했지만 CPF Requirement는 **완료 처리하지 않는다**.
`development_status=완료`, `verification_status=완료`의 전체 승격 조건인 current exact-SHA QA 통과가 충족되지 않았다.

Direct rework와 Architecture-decision-dependent rework는 `QA_B_REWORK_CLASSIFICATION.csv`에 exact ID로 구분했다. Runtime 전용 미검증 항목은 `QA_B_RUNTIME_GAP_MATRIX.csv`에 환경/명령/Evidence를 명시했다.
