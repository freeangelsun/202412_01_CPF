# [최상위 실행 선언] 이번 요청의 100% = CPF 프로젝트 자체의 최종 완성

이 지침에서 100%는 `93행`, `56 Finding`, `34 direct rework` 같은 목록의 100%를 뜻하지 않는다.

**이번 개발 요청으로 CPF 프로젝트 자체를 상용 Framework 완료 상태까지 끝낸다는 의미다.**

아래 목록은 알려진 최소 작업목록이다.
개발 중 추가 미비점이 발견되면 그것도 현재 요청의 개발범위다.
“요건에 없었다”, “다음 QA가 찾으면 수정한다”, “다음 회차에서 한다”를 종료 논리로 사용하지 않는다.

현재 환경에서 구현 가능한 미비점이 하나라도 남아 있으면 계속 개발한다.
외부 Runtime 부재로 실행만 불가능한 항목을 제외하고 부분 구현·미구현을 계획적으로 남기지 않는다.

최상위 운영정책:
`cpf-docs/work/v9i/CPF_PROJECT_FINAL_100_PERCENT_COMPLETION_MANDATE.md`

---

# CPF 개발GPT R6J — 전체 100% 완료 통합 실행지침

## 0. 이번 지침의 의미

이 지침은 **일부 Finding 보정 지침이 아니다.**
이번 개발GPT의 배정범위는 CPF의 현재 미완료 전체이며 목표는 **100% 완료**다.

- Repository: `https://github.com/freeangelsun/202412_01_CPF`
- Branch: `master`
- 현재 QA/중앙 기준 SHA: `3ed676061246c9db3e44f29e254c0393ecca3929`
- 최상위 정본: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
- 중앙 Requirement 원장: `cpf-docs/work/v9i/qa/r6j/CENTRAL_REQUIREMENT_STATUS.csv`
- 중앙 Finding 원장: `cpf-docs/work/v9i/qa/r6j/CENTRAL_INTEGRATED_FINDINGS.csv`
- Runtime 원장: `cpf-docs/work/v9i/qa/r6j/CENTRAL_RUNTIME_QUALIFICATION_MATRIX.csv`

작업 시작 시 latest `origin/master`, exact SHA, Working Tree를 다시 확인한다.
과거 완료/Evidence/PASS를 자동 승계하지 않는다.

## 1. 전체 완료 목표

이번 개발GPT는 다음을 **한 번에 전부 배정받는다.**

1. 중앙 Requirement **93/93 전부**
2. 중앙 Finding **56/56 전부**
3. 현재 알려진 직접 Source/Contract/Gate 재개발 34건
4. Source가 개선되었으나 Runtime/Evidence가 남은 항목 전부
5. Runtime Qualification 전체 축
6. 최상위 `CPF_FINAL_TARGET_REQUIREMENTS.md`의 전체 GA Acceptance
7. 개발 과정에서 새로 발견하는 모든 결함
8. Architecture/Ownership/Consumer/DB/Generator/Frontend/Logging/Recovery/Artifact/Evidence 정합성

**34건은 전체 Scope가 아니다.**
34건은 현재 93행 중 즉시 Source 재개발이 확정된 결함 묶음일 뿐이다.
개발GPT의 종료 조건은 34/34가 아니라 **전체 93/93 + 56/56 + Runtime/GA 조건의 100% 완료**다.

## 2. 중간 Wave의 의미

Wave/우선순위는 작업 순서를 위한 내부 정렬일 뿐이다.

- Wave 완료
- 특정 P0 완료
- 34 direct rework 완료
- 일부 Test 성공
- 일부 Runtime 성공
- Checkpoint ZIP 생성
- 진행률 80/90/99%

어느 것도 작업 종료 사유가 아니다.

**최종 종료 목표는 100%다.**
환경 때문에 실행이 불가능한 Runtime만 `미검증`으로 남길 수 있지만,
그 경우에도 구현 가능한 Source/Test/Script/Config/SQL/Frontend/Generator/Evidence는 전부 완료해야 한다.
미검증 Runtime이 남아 있으면 CPF 전체를 완료라고 표현하지 않는다.

## 3. 93행 Requirement 전체 배정

아래 93행을 **하나도 제외하지 않고** 개발·자체검수한다.

| Exact ID | Priority | Area | Central Status | Required Next Action |
|---|---|---|---|---|
| AB-R6-001 | P0 | Management/Quality | REWORK_REQUIRED | Evidence를 exact current SHA에 commit/bind하고 SHA/command/cwd/tool/exit/hash를 재작성 |
| AB-R6-002 | P0 | Runtime/Release | REWORK_REQUIRED | current exact SHA release workflow SUCCESS artifact 필요 |
| AB-R6-003 | P0 | Runtime/Release | SOURCE_RESOLVED_RUNTIME_REQUIRED | clean exact-SHA Release runner 실행 Evidence |
| AB-R6-004 | P0 | ADM/Frontend/Contract | SOURCE_RESOLVED_RUNTIME_REQUIRED | authenticated real backend 3-browser PASS |
| AB-R6-005 | P0 | ADM/Frontend/Contract | SOURCE_RESOLVED_RUNTIME_REQUIRED | current source consumer path independently verified |
| AB-R6-006 | P0 | ADM/Frontend/Contract | SOURCE_RESOLVED_RUNTIME_REQUIRED | current source consumer path independently verified |
| AB-R6-007 | P0 | Approval/Security | SOURCE_RESOLVED_RUNTIME_REQUIRED | current source independently verified; broader routes tracked in NEW-007 |
| AB-R6-008 | P1 | ADM/Frontend/Contract | SOURCE_RESOLVED_RUNTIME_REQUIRED | current source independently verified |
| AB-R6-009 | P0 | ADM/Frontend/Contract | SOURCE_RESOLVED_RUNTIME_REQUIRED | current SHA regeneration zero-diff |
| AB-R6-010 | P0 | ADM/Frontend/Contract | REWORK_REQUIRED | runtime OpenAPI export + parity + generation zero diff |
| AB-R6-011 | P1 | ADM/Frontend/Contract | SOURCE_RESOLVED_RUNTIME_REQUIRED | BACKEND_RUNTIME OpenAPI lifecycle PASS |
| AB-R6-012 | P1 | ADM/Frontend/Contract | REWORK_REQUIRED | full 332-operation runtime OpenAPI consumer closure |
| AB-R6-013 | P0 | Management/Quality | REWORK_REQUIRED | remove retired 410 ops from route consumer metadata and close duplicate stale consumers |
| AB-R6-014 | P0 | Management/Quality | REWORK_REQUIRED | execute real mutation/failure behavior against product runtime and retain exact evidence |
| AB-R6-015 | P1 | Runtime/Release | SOURCE_RESOLVED_RUNTIME_REQUIRED | 3-vendor live runner process behavior PASS |
| AB-R6-016 | P0 | BZA/Frontend | SOURCE_RESOLVED_RUNTIME_REQUIRED | current source independently verified |
| AB-R6-017 | P0 | BZA/Frontend | SOURCE_RESOLVED_RUNTIME_REQUIRED | fix NEW-002 + real BZA 3-browser release PASS |
| AB-R6-018 | P2 | ADM/Frontend/Contract | SOURCE_RESOLVED_RUNTIME_REQUIRED | browser TTL/reload/response-loss runtime PASS |
| AB-R6-019 | P0 | Approval/Security | SOURCE_RESOLVED_RUNTIME_REQUIRED | current source independently verified |
| AB-R6-020 | P0 | Approval/Security | REWORK_REQUIRED | implement observation-only reconcile for all UNKNOWN-producing owner ports + process-kill test |
| AB-R6-021 | P0 | Approval/Security | SOURCE_RESOLVED_RUNTIME_REQUIRED | current source independently verified |
| AB-R6-022 | P0 | Approval/Security | SOURCE_RESOLVED_RUNTIME_REQUIRED | DB3 + concurrent multi-instance single-use PASS |
| AB-R6-023 | P0 | Approval/Security | SOURCE_RESOLVED_RUNTIME_REQUIRED | prod/stg startup negative/positive tests |
| AB-R6-024 | P1 | Approval/Security | SOURCE_RESOLVED_RUNTIME_REQUIRED | 3-vendor direct SQL overlap/update/delete + rollback/reapply PASS |
| AB-R6-025 | P0 | Runtime/Release | REWORK_REQUIRED | durable post-owner outcome + owner observation reconcile + DB outage test |
| AB-R6-026 | P1 | Management/Quality | SOURCE_RESOLVED_RUNTIME_REQUIRED | remote multi-instance wrong/missing endpoint negative PASS |
| AB-R6-027 | P1 | Management/Quality | SOURCE_IMPROVED_RUNTIME_REQUIRED | 2-instance JDBC DQ replay/process-kill PASS |
| AB-R6-028 | P0 | EDU/Sample | REWORK_REQUIRED | fix/reclassify entire ADM family after architecture decision; QA37 --compile must pass |
| AB-R6-029 | P0 | EDU/Sample | REWORK_REQUIRED | architecture classification + actual consumer runtime per retained EDU |
| AB-R6-030 | P0 | EDU/Sample | REWORK_REQUIRED | architecture decision and product/extension reclassification |
| AB-R6-031 | P0 | EDU/Sample | SOURCE_RESOLVED_RUNTIME_REQUIRED | catalog/handler exact parity after classification |
| AB-R6-032 | P0 | EDU/Sample | REWORK_REQUIRED | reclassify or bind to public extension/integration contract; real approval runtime test |
| AB-R6-033 | P0 | EDU/Sample | REWORK_REQUIRED | remove client-supplied authorization semantics; bind server authority; browser role matrix |
| AB-R6-034 | P0 | EDU/Sample | REWORK_REQUIRED | actual concurrent/version/browser conflict PASS |
| AB-R6-035 | P0 | EDU/Sample | REWORK_REQUIRED | retained EDU actual target runtime + partial failure/reprocess PASS |
| AB-R6-036 | P1 | EDU/Sample | REWORK_REQUIRED | reclassify or bind public configuration extension with actual rollback runtime |
| AB-R6-037 | P0 | EDU/Sample | REWORK_REQUIRED | architecture reclassify; retained extensions must use actual public contracts |
| AB-R6-038 | P0 | EDU/Sample | REWORK_REQUIRED | fix source then run QA37 --compile at exact SHA and commit evidence |
| AB-R6-039 | P0 | EDU/Sample | RUNTIME_OR_EVIDENCE_REQUIRED | retained EDU target runtime evidence per family/topology |
| AB-R6-040 | P0 | Runtime/Release | RUNTIME_OR_EVIDENCE_REQUIRED | Codex independent review + mandatory release qualification |
| FDEV-001 | P0 | Final Development | RUNTIME_OR_QA_REVERIFY_REQUIRED | Linked finding을 닫고 current exact-SHA release qualification으로 재검증 |
| FDEV-002 | P0 | Final Development | RUNTIME_OR_QA_REVERIFY_REQUIRED | Linked finding을 닫고 current exact-SHA release qualification으로 재검증 |
| FDEV-003 | P0 | Final Development | RUNTIME_OR_QA_REVERIFY_REQUIRED | Linked finding을 닫고 current exact-SHA release qualification으로 재검증 |
| FDEV-004 | P0 | Final Development | RUNTIME_OR_QA_REVERIFY_REQUIRED | Linked finding을 닫고 current exact-SHA release qualification으로 재검증 |
| FDEV-005 | P0 | Final Development | RUNTIME_OR_QA_REVERIFY_REQUIRED | Linked finding을 닫고 current exact-SHA release qualification으로 재검증 |
| FDEV-006 | P0 | Final Development | RUNTIME_OR_QA_REVERIFY_REQUIRED | Linked finding을 닫고 current exact-SHA release qualification으로 재검증 |
| FDEV-007 | P0 | Final Development | RUNTIME_OR_QA_REVERIFY_REQUIRED | Linked finding을 닫고 current exact-SHA release qualification으로 재검증 |
| FDEV-008 | P0 | Final Development | RUNTIME_OR_QA_REVERIFY_REQUIRED | Linked finding을 닫고 current exact-SHA release qualification으로 재검증 |
| FDEV-009 | P0 | Final Development | RUNTIME_OR_QA_REVERIFY_REQUIRED | Linked finding을 닫고 current exact-SHA release qualification으로 재검증 |
| FDEV-010 | P0 | Final Development | RUNTIME_OR_QA_REVERIFY_REQUIRED | Linked finding을 닫고 current exact-SHA release qualification으로 재검증 |
| FDEV-011 | P0 | Final Development | RUNTIME_OR_QA_REVERIFY_REQUIRED | Linked finding을 닫고 current exact-SHA release qualification으로 재검증 |
| FDEV-012 | P0 | Final Development | RUNTIME_OR_QA_REVERIFY_REQUIRED | Linked finding을 닫고 current exact-SHA release qualification으로 재검증 |
| FDEV-013 | P0 | Final Development | RUNTIME_OR_QA_REVERIFY_REQUIRED | Linked finding을 닫고 current exact-SHA release qualification으로 재검증 |
| FDEV-014 | P0 | Final Development | RUNTIME_OR_QA_REVERIFY_REQUIRED | Linked finding을 닫고 current exact-SHA release qualification으로 재검증 |
| FDEV-015 | P0 | Final Development | RUNTIME_OR_QA_REVERIFY_REQUIRED | Linked finding을 닫고 current exact-SHA release qualification으로 재검증 |
| FDEV-016 | P0 | Final Development | RUNTIME_OR_QA_REVERIFY_REQUIRED | Linked finding을 닫고 current exact-SHA release qualification으로 재검증 |
| FDEV-017 | P0 | Final Development | RUNTIME_OR_QA_REVERIFY_REQUIRED | Linked finding을 닫고 current exact-SHA release qualification으로 재검증 |
| FDEV-018 | P0 | Final Development | RUNTIME_OR_QA_REVERIFY_REQUIRED | Linked finding을 닫고 current exact-SHA release qualification으로 재검증 |
| FDEV-019 | P0 | Final Development | RUNTIME_OR_QA_REVERIFY_REQUIRED | Linked finding을 닫고 current exact-SHA release qualification으로 재검증 |
| FDEV-020 | P0 | Final Development | RUNTIME_OR_QA_REVERIFY_REQUIRED | Linked finding을 닫고 current exact-SHA release qualification으로 재검증 |
| FDEV-021 | P0 | Final Development | RUNTIME_OR_QA_REVERIFY_REQUIRED | Linked finding을 닫고 current exact-SHA release qualification으로 재검증 |
| FDEV-022 | P0 | Final Development | RUNTIME_OR_QA_REVERIFY_REQUIRED | Linked finding을 닫고 current exact-SHA release qualification으로 재검증 |
| FDEV-023 | P0 | Final Development | RUNTIME_OR_QA_REVERIFY_REQUIRED | Linked finding을 닫고 current exact-SHA release qualification으로 재검증 |
| FDEV-024 | P0 | Final Development | RUNTIME_OR_QA_REVERIFY_REQUIRED | Linked finding을 닫고 current exact-SHA release qualification으로 재검증 |
| FDEV-025 | P0 | Final Development | RUNTIME_OR_QA_REVERIFY_REQUIRED | Linked finding을 닫고 current exact-SHA release qualification으로 재검증 |
| MGR-HARDEN-001 | P0 | Architecture/Ownership | RUNTIME_OR_QA_REVERIFY_REQUIRED | current exact-SHA hardening qualification + immutable evidence |
| MGR-HARDEN-002 | P0 | Reliability/Commands | RUNTIME_OR_QA_REVERIFY_REQUIRED | current exact-SHA hardening qualification + immutable evidence |
| MGR-HARDEN-003 | P0 | DB/Compatibility | RUNTIME_OR_QA_REVERIFY_REQUIRED | current exact-SHA hardening qualification + immutable evidence |
| MGR-HARDEN-004 | P0 | Supply-chain | QA_RECHECK_REQUIRED | current exact-SHA hardening qualification + immutable evidence |
| MGR-HARDEN-005 | P1 | Performance/Resource | RUNTIME_OR_QA_REVERIFY_REQUIRED | current exact-SHA hardening qualification + immutable evidence |
| MGR-HARDEN-006 | P1 | Observability/Audit | RUNTIME_OR_QA_REVERIFY_REQUIRED | current exact-SHA hardening qualification + immutable evidence |
| MGR-HARDEN-007 | P0 | Security | RUNTIME_OR_QA_REVERIFY_REQUIRED | current exact-SHA hardening qualification + immutable evidence |
| MGR-HARDEN-008 | P0 | DR/Recovery | RUNTIME_OR_QA_REVERIFY_REQUIRED | current exact-SHA hardening qualification + immutable evidence |
| MGR-HARDEN-009 | P1 | Artifact Consumer | RUNTIME_OR_QA_REVERIFY_REQUIRED | current exact-SHA hardening qualification + immutable evidence |
| MGR-HARDEN-010 | P0 | Generator | RUNTIME_OR_QA_REVERIFY_REQUIRED | current exact-SHA hardening qualification + immutable evidence |
| MGR-HARDEN-011 | P1 | Traceability/Hygiene | QA_RECHECK_REQUIRED | current exact-SHA hardening qualification + immutable evidence |
| MGR-HARDEN-012 | P0 | Compatibility/Failure | RUNTIME_OR_QA_REVERIFY_REQUIRED | current exact-SHA hardening qualification + immutable evidence |
| R6J-CENTRAL-NEW-001 | P0 | Architecture/EDU | ARCH_DECISION_DONE_REWORK_REQUIRED | 중앙 EDU-ADM 17 분류결정에 따라 Product 기능은 ADM으로 귀속하고 adopter-facing Public Extension만 EDU에 유지; QA37/Catalog/Manual/Test/Generator 동시 정렬 |
| R6J-CENTRAL-NEW-002 | P0 | Runtime/Release | REWORK_REQUIRED | CPF_FRONTEND_URL/CPF_ADM_FRONTEND_URL을 단일 canonical 변수로 정렬하고 workflow preflight/runner/self-test를 일치 |
| R6J-CENTRAL-NEW-003 | P0 | Verification Tool/Frontend | REWORK_REQUIRED | unconditional true/permission bypass를 죽이는 AST/behavior/browser mutation을 추가하고 high-risk action exact grant를 검증 |
| R6J-CENTRAL-NEW-004 | P0 | Verification Tool/Observability | REWORK_REQUIRED | known traffic/failure를 발생시키고 authoritative metric/log/trace/timeline/alert/audit store를 독립 조회하도록 qualifier 재구현 |
| R6J-CENTRAL-NEW-005 | P0 | Management/Requirement | REWORK_REQUIRED | 현재 canonical 기준 거래/로그 Requirement를 개발 원장과 Traceability에 추가하고 Source/Test/Evidence와 연결 |
| R6J-CENTRAL-NEW-006 | P0 | ADM/Transaction Timeline | REWORK_REQUIRED | multi-source transaction timeline/tree aggregator와 source freshness/partial/stale 상태를 구현하고 generated client/UI consumer 연결 |
| R6J-CENTRAL-NEW-007 | P0 | DB/Transaction Logging | REWORK_REQUIRED | DB3 canonical schema/index/join model을 보강하고 retention/partition/archive/purge 및 대량 transactionId lookup test 구현 |
| R6J-CENTRAL-NEW-008 | P0 | EDU/Security | REWORK_REQUIRED | framework-owned authenticated security context에서 actor/roles/scope를 해석하고 spoofable client header는 무시/거부 |
| R6J-CENTRAL-NEW-009 | P0 | EDU/Process Security | REWORK_REQUIRED | process environment clear+allowlist, 최소 payload IPC, strict permission/encryption, deterministic cleanup/crash scrub 구현 |
| R6J-CENTRAL-NEW-010 | P1 | OpenAPI/Frontend | REWORK_REQUIRED | 422를 runtime OpenAPI/committed spec/generated client/ADM·BZA UX/error matrix에 일관되게 반영 |
| R6J-CENTRAL-NEW-011 | P0 | BZA/Frontend/Consumer | REWORK_REQUIRED | retired GET를 active consumer metadata에서 제거하고 explicit retirement UX/waiver를 canonical하게 처리 |
| R6J-CENTRAL-NEW-012 | P0 | Approval/Recovery | REWORK_REQUIRED | BAT/Gateway/Broker/Center-Cut 등 각 Owner에 idempotent observation reconcile을 구현하고 DB outage/response loss/process kill test |
| R6J-CENTRAL-NEW-013 | P0 | ADM/Recovery | REWORK_REQUIRED | RecoveryCenter mutation을 canonical command component로 통합하거나 동일 action grant/expectedVersion/reason/audit 계약 적용 |
| R6J-CENTRAL-NEW-014 | P0 | Core/Logging | REWORK_REQUIRED | File log write failure에 durable spool/retransmit/dedup/sequence/checksum/quarantine/loss metric+alert owner를 구현 |
| R6J-CENTRAL-NEW-015 | P1 | ADM/Frontend/Security | REWORK_REQUIRED | Secrets/FeatureFlags/OpenAPI/Resilience/Operators/FileJobs 등 direct controls를 exact action manifest/hasButton으로 통일 |
| R6J-CENTRAL-NEW-016 | P1 | BZA/Frontend/Security | REWORK_REQUIRED | Inbox/Submissions/Policies/Delegations action manifest/gating을 통일하고 unauthorized browser/API test |

각 행은:
`Acceptance 확인 → 실제 Source/Consumer/호출경로 → 결함 재현 → 구현 → Test → 오류/경계/UNKNOWN/Recovery → Security/Audit → DB/Frontend/Generator 영향 → Runtime/Evidence → 자체검수`
순서로 닫는다.

## 4. 56 Finding 전체 재검증

아래 56건은 모두 current result SHA에서 다시 닫아야 한다.

| Finding | Priority | Area | Central Status | Issue |
|---|---|---|---|---|
| AB-R6-001 | P0 | Management/Quality | REWORK_REQUIRED | Current result SHA와 R6S12 Evidence provenance 미결속 |
| AB-R6-002 | P0 | Runtime/Release | REWORK_REQUIRED | Current master Push에 Release Workflow 실행 없음 |
| AB-R6-003 | P0 | Runtime/Release | SOURCE_RESOLVED_RUNTIME_REQUIRED | Release Runner가 clean exact-SHA qualification을 증명하지 못함 |
| AB-R6-004 | P0 | ADM/Frontend/Contract | SOURCE_RESOLVED_RUNTIME_REQUIRED | Playwright Release Gate 실제 Product wiring 미완성 |
| AB-R6-005 | P0 | ADM/Frontend/Contract | SOURCE_RESOLVED_RUNTIME_REQUIRED | Integration Closure UI operation permission이 실제 ADM Session과 연결되지 않음 |
| AB-R6-006 | P0 | ADM/Frontend/Contract | SOURCE_RESOLVED_RUNTIME_REQUIRED | ADM generic RouteOperationWorkbench가 전용 화면의 permission/Strict JSON을 우회 |
| AB-R6-007 | P0 | Approval/Security | SOURCE_RESOLVED_RUNTIME_REQUIRED | Approval 위험 Operation UI에 action-level permission 미적용 |
| AB-R6-008 | P1 | ADM/Frontend/Contract | SOURCE_RESOLVED_RUNTIME_REQUIRED | ADM 63 Route 중 4개가 59개 sidebar canonical menu에서 누락 |
| AB-R6-009 | P0 | ADM/Frontend/Contract | SOURCE_RESOLVED_RUNTIME_REQUIRED | ADM generated route-operation contract가 12 Route에서 stale |
| AB-R6-010 | P0 | ADM/Frontend/Contract | REWORK_REQUIRED | Backend validation과 committed ADM OpenAPI/Generated artifact drift |
| AB-R6-011 | P1 | ADM/Frontend/Contract | SOURCE_RESOLVED_RUNTIME_REQUIRED | Frontend enrich script가 validation/security response 계약의 제2 정본 역할 |
| AB-R6-012 | P1 | ADM/Frontend/Contract | REWORK_REQUIRED | Generated Client가 high-risk 실제 Consumer를 compile-time으로 충분히 구속하지 않음 |
| AB-R6-013 | P0 | Management/Quality | REWORK_REQUIRED | Operation consumer Gate False Green |
| AB-R6-014 | P0 | Management/Quality | REWORK_REQUIRED | R6 Behavior Mutation Gate가 실제 mutation execution이 아닌 tautology |
| AB-R6-015 | P1 | Runtime/Release | SOURCE_RESOLVED_RUNTIME_REQUIRED | DB Runner Security Test가 process behavior가 아니라 문자열 검사 중심 |
| AB-R6-016 | P0 | BZA/Frontend | SOURCE_RESOLVED_RUNTIME_REQUIRED | BZA Workbench permission code가 canonical manifest와 불일치 |
| AB-R6-017 | P0 | BZA/Frontend | SOURCE_RESOLVED_RUNTIME_REQUIRED | BZA Release Integrity 미완성 |
| AB-R6-018 | P2 | ADM/Frontend/Contract | SOURCE_RESOLVED_RUNTIME_REQUIRED | Frontend idempotency storage 정책/표현 불일치 |
| AB-R6-019 | P0 | Approval/Security | SOURCE_RESOLVED_RUNTIME_REQUIRED | Approval Owner Registry 4D binding이 exact tuple이 아니라 fuzzy matching |
| AB-R6-020 | P0 | Approval/Security | REWORK_REQUIRED | Process Kill 후 Approval EXECUTING/RUNNING 고착 경로 |
| AB-R6-021 | P0 | Approval/Security | SOURCE_RESOLVED_RUNTIME_REQUIRED | Public DQ capability 안전성이 Provider 구현 규율에 의존 |
| AB-R6-022 | P0 | Approval/Security | SOURCE_RESOLVED_RUNTIME_REQUIRED | HMAC capability 자체에 expiry/nonce-consumption single-use 없음 |
| AB-R6-023 | P0 | Approval/Security | SOURCE_RESOLVED_RUNTIME_REQUIRED | Integration Closure secret가 raw ConfigurationProperties 문자열로 주입 가능 |
| AB-R6-024 | P1 | Approval/Security | SOURCE_RESOLVED_RUNTIME_REQUIRED | Approval Policy immutability/active overlap이 DB까지 닫히지 않음 |
| AB-R6-025 | P0 | Runtime/Release | REWORK_REQUIRED | Owner success 후 DB finalization/DB outage의 durable UNKNOWN 보장 부족 |
| AB-R6-026 | P1 | Management/Quality | SOURCE_RESOLVED_RUNTIME_REQUIRED | BAT Approved Remote Port가 localhost/local instance default로 fail-open |
| AB-R6-027 | P1 | Management/Quality | SOURCE_IMPROVED_RUNTIME_REQUIRED | InMemory DQ replay는 multi-instance/process-kill idempotency 증거가 아님 |
| AB-R6-028 | P0 | EDU/Sample | REWORK_REQUIRED | EDU 135 Catalog ↔ Handler/Scenario requiredRole 불일치 |
| AB-R6-029 | P0 | EDU/Sample | REWORK_REQUIRED | EDU 135의 5종 Test가 실제 8종 Product Consumer Runtime을 증명하지 않음 |
| AB-R6-030 | P0 | EDU/Sample | REWORK_REQUIRED | EDU-ADM 17이 요구 의미보다 template/common-state-machine 중심 |
| AB-R6-031 | P0 | EDU/Sample | SOURCE_RESOLVED_RUNTIME_REQUIRED | EDU-ADM readOnly 정본 불일치 |
| AB-R6-032 | P0 | EDU/Sample | REWORK_REQUIRED | EDU-ADM-04 승인 교육 예제가 실제 승인 정책/SoD/만료/범위를 구현하지 않음 |
| AB-R6-033 | P0 | EDU/Sample | REWORK_REQUIRED | EDU-ADM-08 보안 교육 예제가 masking/IDOR/browser role matrix를 구현하지 않음 |
| AB-R6-034 | P0 | EDU/Sample | REWORK_REQUIRED | EDU-ADM-09 version conflict/browser flow 의미 불일치 |
| AB-R6-035 | P0 | EDU/Sample | REWORK_REQUIRED | EDU-ADM-10 bulk target semantics가 pseudo partition으로 대체 |
| AB-R6-036 | P1 | EDU/Sample | REWORK_REQUIRED | EDU-ADM-11 maintenance/LKG rollback 의미가 선언에 그침 |
| AB-R6-037 | P0 | EDU/Sample | REWORK_REQUIRED | EDU-ADM-12\~17 핵심 운영 의미가 generic JDBC state machine으로 대체 |
| AB-R6-038 | P0 | EDU/Sample | REWORK_REQUIRED | QA37 EDU Source Closure Gate가 semantic drift를 탐지하지 못함 |
| AB-R6-039 | P0 | EDU/Sample | RUNTIME_OR_EVIDENCE_REQUIRED | Current SHA에서 EDU 135 target runtime 증거 없음 |
| AB-R6-040 | P0 | Runtime/Release | RUNTIME_OR_EVIDENCE_REQUIRED | QA 진입 전 Codex/필수 Target Runtime 미완료 |
| R6J-CENTRAL-NEW-001 | P0 | Architecture/EDU | ARCH_DECISION_DONE_REWORK_REQUIRED | ADM Product와 EDU-ADM generic duplicate implementation의 Architecture 충돌 |
| R6J-CENTRAL-NEW-002 | P0 | Runtime/Release | REWORK_REQUIRED | Release Workflow ADM frontend URL preflight 변수명 불일치 |
| R6J-CENTRAL-NEW-003 | P0 | Verification Tool/Frontend | REWORK_REQUIRED | Frontend contract gate가 risky-operation permission bypass mutation을 탐지하지 못함 |
| R6J-CENTRAL-NEW-004 | P0 | Verification Tool/Observability | REWORK_REQUIRED | Observability qualification이 self-attested boolean proof로 false-green 가능 |
| R6J-CENTRAL-NEW-005 | P0 | Management/Requirement | REWORK_REQUIRED | 07_02 거래·파일·DB Logging 신규 정본이 R6I 77행 개발원장에 미반영 |
| R6J-CENTRAL-NEW-006 | P0 | ADM/Transaction Timeline | REWORK_REQUIRED | ADM transactionId one-shot view가 Message/DLQ/Batch/File/Trace/Audit까지 통합하지 못함 |
| R6J-CENTRAL-NEW-007 | P0 | DB/Transaction Logging | REWORK_REQUIRED | Transaction DB schema/query가 trace/span/request/idempotency/tenant/batch/message/file 식별자를 충분히 연결하지 못함 |
| R6J-CENTRAL-NEW-008 | P0 | EDU/Security | REWORK_REQUIRED | EDU runtime authorization이 caller-provided actor/roles/data-scope header를 신뢰 |
| R6J-CENTRAL-NEW-009 | P0 | EDU/Process Security | REWORK_REQUIRED | PROCESS EDU consumer가 parent environment 상속 및 full payload temp JSON 기록 |
| R6J-CENTRAL-NEW-010 | P1 | OpenAPI/Frontend | REWORK_REQUIRED | Approval backend 422와 committed OpenAPI/generated client/frontend error taxonomy drift |
| R6J-CENTRAL-NEW-011 | P0 | BZA/Frontend/Consumer | REWORK_REQUIRED | Retired HTTP 410 Approval GET가 route metadata/workbench real consumer로 계산 |
| R6J-CENTRAL-NEW-012 | P0 | Approval/Recovery | REWORK_REQUIRED | UNKNOWN-producing Approval Owner 다수가 observation reconcile 구현 부재 |
| R6J-CENTRAL-NEW-013 | P0 | ADM/Recovery | REWORK_REQUIRED | RecoveryCenter가 canonical Reliability mutation의 permission/CAS 계약을 약화한 중복 Consumer |
| R6J-CENTRAL-NEW-014 | P0 | Core/Logging | REWORK_REQUIRED | CPF-LOGFAIL durable spool/retry/dedup/loss recovery owner 부재 |
| R6J-CENTRAL-NEW-015 | P1 | ADM/Frontend/Security | REWORK_REQUIRED | HIGH/CRITICAL ADM 전용 화면 action-level permission projection 불균일 |
| R6J-CENTRAL-NEW-016 | P1 | BZA/Frontend/Security | REWORK_REQUIRED | BZA Approval 계열 mutation button action-level permission projection 누락 |

Source-resolved 상태도 완료가 아니다.
새 변경으로 회귀하지 않았는지 확인하고 필요한 Runtime/Evidence까지 수행한다.

## 5. 최상위 Requirement 전체 재검토

93행과 56 Finding만 보고 끝내지 않는다.

`CPF_FINAL_TARGET_REQUIREMENTS.md` 전체를 다시 읽고 다음 축을 Repository 전체에서 독립 검수한다.

- Public API / SPI / Internal / Ownership
- 실제 Consumer와 전체 호출 경로
- 정상/오류/경계/부분 실패/UNKNOWN
- Idempotency/CAS/Concurrency/Retry/Reconcile
- Multi-instance/Process Kill/Rolling
- Security/Permission/SoD/Secret/Masking/Audit
- Oracle/PostgreSQL/MariaDB
- Install/Upgrade/Rollback/Forward/Backup/Restore
- Config/Profile/AutoConfiguration
- Starter/BOM/Publication/Artifact
- Generator/Generated Domain/Sample/EDU
- ADM/BZA/Gateway/Batch Product
- Frontend/OpenAPI/Generated Client
- Observability/SLI/SLO/Logging/Trace
- Performance/Capacity/Backpressure
- DR
- Supply-chain/SBOM/License/Signature/Trust
- LOCAL_DEV/REMOTE/OFFLINE
- Documentation/Evidence/Manifest/Hash
- Repository Hygiene/Dead/Stale/Duplicate/Dual-primary

정본에 있는데 중앙 원장에 빠진 미완료 항목을 찾으면 `DEV-R6J-SELF-*`로 즉시 추가한다.

## 6. Architecture — ADM / EDU

ADM은 CPF가 완제품으로 제공하는 Product다.
도입 개발자가 ADM 본체를 다시 개발하지 않는다.

EDU는 adopter가 실제 사용하는 Public API/SPI/Extension/Integration 예제만 제공한다.

R6J 중앙 결정:
- PRODUCT_ADM: 9
- EXTENSION_SAMPLE: 4
- MERGE_EDU: 4

17/135 숫자 보존은 목표가 아니다.
Product mimic, generic JDBC state-machine, dummy handler로 개수를 맞추지 않는다.

나머지 EDU도 동일 기준으로 검토하여 Product duplication이나 실제 Consumer 부재를 발견하면 자체 Requirement로 처리한다.

## 7. 거래·로그 — Release P0

거래가 다른 거래를 호출해도 동일 `transactionId`를 유지한다.

전체:
- nested transaction
- REST/SOAP/fixed/file/webhook
- Gateway
- Async
- Message producer/consumer/retry/DLQ
- Batch/Scheduler/Center-Cut
- worker/agent
- UNKNOWN/reconcile

를 하나의 lineage로 연결한다.

### ADM
운영자는 **transactionId 하나로 전체 호출 흐름을 한 번에 조회**해야 한다.

Timeline/Tree에:
- request/result
- nested segment
- remote attempts
- Message/DLQ
- Batch job/step/partition/worker
- File/Remote log
- Trace
- Audit
- UNKNOWN/Reconcile
- partial/stale/missing source
를 연결한다.

### File Log
- standard structured fields
- path/file naming/encoding
- rotation/compression/retention
- bounded queue/backpressure
- disk-full/read-only/permission failure
- durable spool
- retry/backoff
- dedup
- sequence/checksum
- quarantine
- shutdown drain
- process kill/restart
- terminal loss metric/alert
- masking/redaction

### DB Log
DB3 모두:
- transactionId/trace/span/segment/parent/attempt
- request/idempotency
- tenant/channel/actor
- remote system/operation
- batch/message/file IDs
- index
- retention/partition/archive/purge
- migration/rollback/forward
- large lookup performance
를 구현·검증한다.

## 8. Approval / UNKNOWN / Recovery

UNKNOWN을 만들 수 있는 모든 Owner를 전수 찾는다.

BAT/Gateway/Broker/Center-Cut/DataQuality 등:
- observation-only reconcile
- lease expiry
- process kill
- response loss
- owner success + DB finalization failure
- multi-instance takeover
- duplicate reconcile
를 닫는다.

RUNNING→UNKNOWN까지만 만들고 영구 고착되면 실패다.

## 9. ADM/BZA Product 100%

ADM 63 Route와 BZA 전체 Route를 다시 전수 확인한다.

각 Route:
- search/list/paging/detail
- mutation/state transition
- generated client actual consumer
- action-level permission
- expectedVersion/CAS
- reason
- audit
- 401/403/404/409/422/429/500/503
- realtime freshness/reconnect/stale
- accessibility/responsive
- browser 3-engine
를 확인한다.

Generic Workbench가 실제 Product Consumer를 대신하면 안 된다.

## 10. Verification Gate False-Green 0

새 Gate도 제품코드와 동일하게 QA 대상이다.

반드시 mutation/adversarial test:
- permission bypass
- unconditional true
- stale owner
- wrong operation
- fake observability boolean
- token/string-only survivor
- retired API counted as active
- generated metadata self-reference
- runtime missing but PASS
를 죽인다.

## 11. EDU Security / Process

Client header를 권한 근거로 신뢰하지 않는다.
Authenticated framework-owned context를 사용한다.

PROCESS consumer:
- environment clear + allowlist
- Secret env 금지
- full payload temp 금지
- strict file permission
- minimum IPC
- crash/process-kill cleanup
- evidence secret 0

## 12. OpenAPI / DB3 / Generator / Artifact

- runtime OpenAPI ↔ committed spec ↔ generated client ↔ frontend parity
- 422 포함 error taxonomy
- DB3 empty install/upgrade/rollback/forward
- Generator create→runtime→remove→regenerate
- generated domain/sample/manual/EDU parity
- BOM/publication/internal visibility
- duplicate catalog 0
- LOCAL_DEV/REMOTE/OFFLINE artifact
- SBOM/license/signature/trust

## 13. Runtime Qualification 전체

아래 Runtime 축도 이번 **전체 배정 Scope**다.

| Runtime Gate | Current Status | Required Environment |
|---|---|---|
| Java25+Gradle9.1 clean build/publication | 미검증 | self-hosted Windows cpf-r6 |
| Oracle/PostgreSQL/MariaDB live lifecycle | 미검증 | DB3 credentials and empty isolated DB/schema |
| ADM authenticated Chromium/Firefox/WebKit | 미검증 | real ADM URL + 3 auth states |
| BZA authenticated Chromium/Firefox/WebKit | 미검증 | real BZA URL + auth state |
| Approval process-kill/UNKNOWN reconcile | 미검증 | 2+ instances + shared DB + owner services |
| Broker/network/DB-finalization faults | 미검증 | fault proxy/broker/shared DB |
| Performance/resource/backpressure | 미검증 | live system probes |
| Security negative corpus | 미검증 | authenticated roles + corpus |
| DR/backup/restore | 미검증 | vendor DB + backup key + chaos probe |
| Generator create→runtime→remove→regenerate | 미검증 | Java25+DB3+fresh consumer |
| Full ADM 332-operation closure | 미검증 | BACKEND_RUNTIME OpenAPI export |
| Codex independent review | 미검증 | GitHub current exact SHA |
| Transaction/Logging end-to-end lineage | 미검증 | 2+ instances + DB3/log store + broker + batch worker + authenticated ADM |

추가로 실제 current result SHA에서:
- Java25 + Gradle9.1
- DB3
- authenticated ADM/BZA Chromium/Firefox/WebKit
- 2+ instance/process kill/network/broker/DB failure
- transaction/logging E2E
- approval UNKNOWN
- performance/load/soak
- authoritative observability
- security negative corpus
- DR
- LOCAL/REMOTE/OFFLINE artifact
- Generator lifecycle
- Codex
- Release workflow
를 모두 수행한다.

실행 불가하면 구현까지 끝내고 `미검증` 사유/환경/명령/기대 Evidence를 남긴다.
실행하지 않은 것은 절대 PASS가 아니다.

## 14. 자체 발견 결함

개발 중 문제를 발견하면 다음 작업으로 미루지 않는다.

`DEV-R6J-SELF-001...`로 등록하고 같은 개발 범위에서:
- Source
- Test
- SQL
- API
- Config
- Frontend
- Generator
- Script
- Evidence
까지 처리한다.

명시 Requirement보다 더 큰 Root Cause가 보이면 Root Cause 단위로 Repository 전체를 수정한다.

## 15. 진행률

진행률은 **전체 100% 목표 기준**으로 보고한다.

필수:
- Requirement reviewed/closed: x/93
- Central Findings closed: x/56
- Direct known rework implemented: x/34
- Self findings: closed/total
- ADM routes verified: x/63
- BZA routes verified: x/total
- EDU architecture reviewed: x/current canonical total
- Transaction/Logging acceptance: x/total
- Runtime qualification: x/total
- GA canonical axes: x/total

34/34만 달성하고 “완료”라고 쓰면 안 된다.

## 16. 최종 종료 조건

개발GPT 자체 작업 종료는 다음을 모두 만족해야 한다.

- 93/93 개발·자체검수 완료
- 56/56 current source 재판정
- 34/34 known direct rework 완료
- 자체 발견 결함 처리 완료
- 최상위 Requirement 전 범위 재검토 완료
- 실행 가능한 모든 Gate 실패 0
- Runtime 실행 가능 항목 전부 실행
- 미실행 Runtime은 정확히 미검증
- Evidence 실재/Hash/current source binding
- Manifest/ZIP/CRC/Hash PASS
- 보호경로/삭제 정책 준수
- stale/generated/dual-primary 문제 0

**제품 100% 완료 선언은 QA가 한다.**
개발GPT는 미검증 Runtime이 하나라도 남으면 제품 전체 완료라고 쓰지 않는다.

## 17. QA 재검수 목표

다음 QA A/B도 일부 영역만 검사하는 것이 아니다.

Primary 영역은 검수 관점을 회전하기 위한 역할 배정일 뿐이며,
**각 QA의 최종 책임은 CPF 전체 100% 재검수**다.

각 QA는:
- 93/93
- 56/56
- self findings
- 최상위 Requirement 전체
- Runtime/GA 전체
를 독립적으로 검수한다.

A/B 모두 PASS하고 중앙 통합에서도 미통과/미검증/false-green이 없을 때만 최종 완료 후보가 된다.

## 18. Git / 삭제

GPT는 Commit/Push/Branch/Tag/PR/Release를 하지 않는다.
`git clean`, `git reset --hard`, `git restore .` 금지.

삭제는 exact Root-relative `DELETE_MANIFEST.csv`에 제안하고 사용자 승인 전 수행하지 않는다.

## 19. 최종 결과물

Root-relative 단일 최종 ZIP:
- REVIEW_INDEX.md
- REQUIREMENT_STATUS.csv
- DEVELOPMENT_SESSION_RESULT.csv
- DEVELOPMENT_RESULT.md
- CENTRAL_FINDING_RESULT.csv
- SELF_DISCOVERED_FINDINGS.csv
- EDU_ARCH_MIGRATION_MATRIX.csv
- TRANSACTION_LOGGING_MATRIX.csv
- RUNTIME_QUALIFICATION_MATRIX.csv
- CHANGE_MANIFEST.csv
- TEST_AND_EVIDENCE.md
- OPEN_ISSUES.md
- DELETE_MANIFEST.csv
- PACKAGE_MANIFEST.json
- SHA256SUMS.txt
- CODEX_REVIEW_REQUEST.md
- HANDOVER.md
- 변경 Source/SQL/API/Test/Config/Frontend/Generator/Script

Checkpoint는 최종본이 아니다.
최종에는 모든 Checkpoint 변경을 다시 통합한 최신 전체 Overlay 하나를 만든다.


## 20. 강제 Finalization Rule

이 지침의 의도는 “이번 회차에서 최대한 많이 처리”가 아니다.
**이번 회차에서 현재 남은 전체를 끝낸다**가 기본 가정이다.

개발GPT는 작업 계획을 세울 때부터 다음 회차를 전제로 Scope를 남기지 않는다.
`부분 구현`, `미구현`, `추후 개발`, `다음 회차 처리`, `후속 보완`을 정상 종료계획에 두지 않는다.

외부 Runtime 때문에 현재 실행 불가한 항목만 `미검증`으로 남길 수 있다.
그 외 구현 가능한 미비점은 반드시 현재 회차에서 개발·Test·Evidence까지 닫는다.

세션 한계가 임박할 경우 Checkpoint를 만든 뒤 동일 Scope를 연속 수행한다.
Checkpoint 생성 자체를 작업 종료로 간주하지 않는다.
