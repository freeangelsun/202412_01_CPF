# ADDITIONAL DEVELOPMENT REQUIRED

## QA-A-FS1000-NEW-001 — P0 — 실행 가능한 Online Domain A→B→C(/D) 다중도메인 통합 거래 예제가 확인되지 않음
- Requirement: CPF-RV-0001~0008 / SAMPLE-EDU
- Actual Source: cpf-reference/src/main/java/com/cpf/reference/transaction/ReferenceTransactionEducationSample.java; cpf-reference/.../EduDev22Handler.java; dev-final/EDU_135_MATRIX.csv
- Consumer/Call Path: 현재 ReferenceTransactionEducationSample은 단일 stateChangingWork + registerRemote 구조이고 Saga EDU도 generic state-machine/outbox 패턴이다. Controller→Domain A→B→C(/D) 서비스/Repository/Remote 실제 체인이 아니다.
- Reproduction: 현재 exact Product tree와 EDU 135 inventory에서 MultiDomain/Domain A→B→C 실행형 통합 예제를 검색하고 Reference Transaction/Saga 구현을 직접 확인.
- Expected: 성공/B/C/D 실패/DB exception/remote timeout/remote success+local DB fail/duplicate/concurrent/retry/UNKNOWN/reconcile을 실제 A→B→C(/D) 체인으로 실행 가능해야 함.
- Actual: 해당 통합 실행 예제를 찾지 못함. 프레임워크 단위 기능/개별 EDU는 존재하나 사용자 강제 통합 Acceptance를 충족하지 않음.
- Root Cause: 교육/샘플이 기능별 패턴 중심으로 분절되어 전체 다중도메인 거래 Acceptance를 하나의 실행형 Product sample로 닫지 않음.
- Required Fix: 실제 Domain A/B/C(/D) service/repository/remote consumer를 가진 선택형 Reference/Generated sample을 추가하고 rollback/UNKNOWN/reconcile/log/ADM timeline까지 테스트.
- Reverification: Java25 current-SHA integration + DB before/after + fault injection + transactionId/segment/attempt/ADM timeline evidence.

## QA-A-FS1000-NEW-002 — P0 — 실행 가능한 Batch→Domain A→B→C 통합 예제가 확인되지 않음
- Requirement: CPF-RV-0009~0015 / BAT-SHARED / SAMPLE-EDU
- Actual Source: cpf-reference/src/main/java/com/cpf/reference/batch/**; dev-final/EDU_135_MATRIX.csv
- Consumer/Call Path: Batch feature별 scheduler/chunk/retry/center-cut 예제는 존재하나 Scheduler/Job/Step→Domain A→B→C→DB/Remote를 하나의 통합 실행 흐름으로 연결한 예제를 확인하지 못함.
- Reproduction: cpf-reference batch source tree와 EDU inventory에서 multi-domain/domain chain을 전역 확인.
- Expected: 성공/실패 rollback/skip/retry/restart/checkpoint/process-kill/multi-instance/no-duplicate + transaction lineage까지 실행 가능해야 함.
- Actual: 사용자 강제 Batch 다중도메인 통합 예제를 찾지 못함.
- Root Cause: Batch 교육 시나리오가 기술 feature 단위로 분리되어 실제 업무 다중도메인 호출과 복구를 종단간 증명하지 못함.
- Required Fix: Batch→Domain A→B→C integrated reference job/step과 실제 DB/remote owner를 제공하고 process-kill/restart/no-duplicate까지 자동화.
- Reverification: Java25/Spring Batch live DB3 또는 공식 test profile에서 exact execution/job/step/transaction identity + before/after data evidence.

## QA-A-FS1000-NEW-003 — P0 — Release Qualification 6종이 실제 CPF Target/Authority가 아닌 단일 가짜 localhost 증거를 신뢰함
- Requirement: Testing/Mutation/Chaos + Runtime Qualification
- Actual Source: run-resource-contract.py; run-batch-contract.py; run-broker-contract.py; run-r6-security-negative-qualification.py; run-r6-dr-chaos-probe.py; run-r6-observability-qualification.py
- Consumer/Call Path: QA가 CPF와 무관한 단일 localhost 서버로 measured resource/batch identity/broker offset/security semantic/DR hash/Observability signed store evidence를 모두 생성. 현재 END_SHA를 사용한 Gate 6개가 모두 Exit 0/PASS.
- Reproduction: evidence/false-green/*-fake-pass.log 및 *.exit. observability/DR는 END_SHA로 재실행, 6종 모두 exact END_SHA 공격 실행.
- Expected: Gate가 release candidate의 실제 deployment/artifact/authority만 신뢰해야 하며 caller가 URL/token/HMAC key/authority/evidence path를 전부 임의 구성해 PASS시킬 수 없어야 함.
- Actual: 가짜 localhost가 요구 schema/identity/signature 모양만 충족하면 6/6 PASS.
- Root Cause: 측정 semantic은 강화됐지만 trusted target identity와 evidence authority root-of-trust가 release pipeline 밖에서 독립 고정되지 않음.
- Required Fix: mTLS/cert/SPIFEE 또는 CI-controlled immutable target allowlist + signed deployment/artifact attestation + out-of-band authority key pinning. 사용자 공급 URL/키만으로 Release PASS 금지.
- Reverification: 동일 가짜 localhost mutation 6종이 모두 non-zero로 KILL되고 실제 authorized target에서는 PASS.

## QA-A-FS1000-NEW-004 — P1 — Public API method-level Javadoc가 param/return/throws/null/side-effect 계약을 충분히 문서화하지 않음
- Requirement: CPF-RV-0022~0025 / DEVEX-COMMENT
- Actual Source: cpf-core/src/main/java/com/cpf/core/api/http/CpfHttpClient.java; cpf-core/src/main/java/com/cpf/core/api/page/CpfPageRequest.java
- Consumer/Call Path: 두 공개 계약 모두 class-level 한글 설명은 있으나 public method별 @param/@return/@throws/null/side-effect 계약 Javadoc가 없다.
- Reproduction: 현재 exact Product source의 공개 API 샘플 직접 검수.
- Expected: 사용자 강제 Review Point 0022~0024와 Canonical DEVEX-COMMENT에 맞는 소비자 중심 한글 Javadoc.
- Actual: class-level 설명은 개선됐으나 method 계약 문서가 빈 곳이 존재.
- Root Cause: Javadoc 품질 기준이 public type 존재/설명 중심이고 method contract completeness가 전수 강제되지 않음.
- Required Fix: Public API/SPI 전수 method Javadoc gate를 추가하고 param/return/throws/null/default/side-effect/thread-safety를 실제 계약에 맞게 작성.
- Reverification: Java25 Javadoc task warning/error=0 + mutation으로 method contract 제거 시 Gate FAIL.

## QA-A-FS1000-NEW-005 — P1 — Checked-in OpenAPI의 표준 오류 계약이 전체 Public operation에 일관되게 노출되지 않음
- Requirement: CPF-RV-0026~0030 / OpenAPI Contract
- Actual Source: cpf-admin/frontend/openapi/cpf-openapi.json; cpf-biz-admin/frontend/openapi/cpf-openapi.json
- Consumer/Call Path: ADM 330 operations 중 401/403/500/503 선언은 각각 18개 수준이며, BZA 80 operations는 401/403/429/500/503은 80개지만 400은 0개. 실제 Generated Client 소비와 오류 UX 계약의 전수 검증이 필요.
- Reproduction: 현재 Product-identical OpenAPI JSON을 operation별 response code로 기계 집계.
- Expected: 각 Public operation에 실제 적용되는 정상/400/401/403/404/409/429/500/503 또는 명시적 대체(예: 422)와 Error DTO/Auth/Permission이 정확히 문서화.
- Actual: 표준 오류 response가 Product별/operation별로 불균일하고 ADM은 특히 sparse.
- Root Cause: Global error handler와 checked-in OpenAPI operation response 문서화가 전수 1:1로 닫히지 않음.
- Required Fix: Runtime OpenAPI exporter에서 공통 Error response를 실제 적용 범위에 자동 주입하고 Orval client 재생성/zero-drift/오류 UX test.
- Reverification: Controller↔runtime OpenAPI↔checked-in spec↔generated client response matrix 전수 비교.

## Reopened Central Actions
- CENTRAL-FINAL-002 evidence/current SHA
- CENTRAL-FINAL-013 OpenAPI error contract
- CENTRAL-FINAL-017 FileLog recovery test completeness
- CENTRAL-FINAL-018~023 qualification false-green/provenance
- CENTRAL-FINAL-026 Runtime Qualification 13
