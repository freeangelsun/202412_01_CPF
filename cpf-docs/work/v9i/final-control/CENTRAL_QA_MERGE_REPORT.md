# CPF Final QA A/B 중앙 Merge 판정

- Basis SHA: `3aa1dd12f8a5938d33feb6ed598b3dd2442bf2e2` (`07_05`)
- QA A package SHA-256: `b4e8929066517bf122ef2ea2d9fd54a7b43f29f5a37e14afa0839700cb0e203b`
- QA B package SHA-256: `a1929d223125cc93182013030bf141856125efc35581fae8b7b7906b00336f95`
- Latest master recheck at merge start: `3aa1dd12f8a5938d33feb6ed598b3dd2442bf2e2`
- Central verdict: **FAIL / REDEVELOPMENT REQUIRED + UNVERIFIED / RELEASE_BLOCKED**

## 1. 중앙 분모

최상위 프로젝트 완료 분모는 **Canonical 169 Requirement**다.

개발 원장 `93`, 기존 중앙 Finding `56`, 개발 자체발견 `4`는 모두 입력자료다.
QA A 신규 25 + QA B 신규 8은 중복 2개 계열을 합쳐 **신규 독립 개발 Action 31개(P0 22 / P1 9)**로 중앙 정규화했다.

33이라는 숫자나 31이라는 숫자 자체가 개발 Scope의 상한이 아니다.
이번 개발 Scope는 **CPF 프로젝트 169 Requirement 전체 + 기존 56 + 신규 31 + self-found + Runtime 13 + 개발 중 추가발견 전체**다.

## 2. 중앙에서 직접 재확인한 Source 결함

현재 exact SHA에서 다음은 중앙이 실제 Source를 다시 열어 QA 지적을 확인했다.

- `AdmApprovalRepository.finishExecution* / markExecutionUnknown / integrity transition`의 terminal UPDATE에 `FENCE_TOKEN` 조건이 없다.
- `BatchRuntimeApprovalOwnerCommandAdapter.matchesAny()`가 모든 row 값을 합친 뒤 `haystack.contains(needle)`로 identity를 선택한다.
- `CenterCutApprovalOwnerCommandAdapter`가 `RUNNING`, `RETRYING`을 failedCount=0 조건에서 성공으로 확정할 수 있다.
- `TransactionContextFilter`가 외부 Header의 transactionId를 `generateOrUse()`에 넘기고, Generator는 문법상 유효하면 그대로 사용한다.
- `CpfFileLogRecoverySpool` 기본 root가 `java.io.tmpdir`; replay가 직접 `Files.writeString(APPEND)`하고 8MiB 이하에서만 marker dedup을 수행한다.

따라서 위 P0는 문서 해석 문제가 아니라 current-source deterministic defect로 처리한다.

## 3. 중앙 Architecture 결정

1. **Core persistence**: `cpf-core`는 API/SPI/기술 중립 계약만 소유한다. MyBatis/JDBC 구현은 downstream provider/starter가 소유한다.
2. **Transaction lineage**: `cpf_transaction_lineage`는 normalized operational lineage projection/index로 사용한다. 원래 도메인/메시지/배치/외부로그 저장소를 대체하는 dual-primary가 아니다. idempotent writer가 projection을 유지한다.
3. **EDU-ADM**: PRODUCT_ADM/MERGE_EDU 13개는 runtime handler가 아니어야 한다. 삭제 승인 없이도 concrete handler/bean/registry/duplicate Product logic을 제거하고 reference/redirect metadata로 비실행화한다.
4. **EDU retained role**: 02/03/04/07은 canonical `CPF_ADM_OPERATOR`를 따른다.
5. **Retired BZA API**: compatibility 410 구현은 남길 수 있으나 active OpenAPI/generated client/consumer count에서는 제외한다.
6. **HIGH/CRITICAL Frontend**: strict generated-client gate를 유지한다. Gate를 약화하지 않는다.
7. **FileLog spool**: 임시 디렉터리 기본값 금지. 운영 지속성 있는 managed spool root + autonomous retry lifecycle을 소유한다.
8. **Transaction trust boundary**: 외부 caller가 내부 transactionId를 결정할 수 없다. trusted internal hop만 lineage propagation 가능.

## 4. 문서/매뉴얼은 개발GPT 범위에서 분리

사용자 지시에 따라 이번 Product Developer GPT는 다음을 수정하지 않는다.

- `README.md`
- `cpf-docs/guides/**`
- `cpf-docs/deliverables/**`
- `cpf-docs/assets/manuals/**`
- `cpf-docs/assets/readme/**`
- `cpf-docs/specification/CPF_DOCUMENTATION_STANDARD.md`

고객 매뉴얼/PDF/DOCX/README의 시각·한글화·편집 품질은 **별도 Documentation Finalization 작업**으로 처리한다.

단, 개발 제어용 `cpf-docs/work/**`와 Requirement 정본 `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`는 개발 결과/Evidence/Canonical count 정합성 목적에 한해 수정 가능하다.

## 5. 최종 개발 목표

이번 개발은 subset closure가 아니라 **CPF Product Source Finalization**이다.

부분 구현, 구현 가능한 미구현, known P0/P1, false-green verification, Consumer 단절, ownership 위반을 다음 회차로 계획 이월하지 않는다.
