# CPF 현재 통합 개발·1차 자체검수 Review

> 전달 입력 baseline: `92169d9918dd176e8322ac2f9dfc29ebe1d2ea12`
> 현재 exact Git SHA: Repository에서는 `git rev-parse HEAD`, `git archive`에서는 `BASE_SHA.txt` export-subst 값을 사용한다. 이 문서는 result commit SHA를 자기참조로 고정하지 않는다.
> 상태 정본: `cpf-docs/work/current/CPF_REQUIREMENT_MASTER.csv` + 19 Parts (30,605 logical requirements).

## 현재 결론

- QA_B Session 3의 P0 Product 4건(transactionId, Batch UNKNOWN SQL, Generated Batch drift, ADM Incident/Canonical DB)을 Source/SQL/Generator/Test/Gate까지 보완했다.
- Generator는 `com.customer.*` 강제를 제거하고 기본 package를 `domain.name`으로 사용한다. DB Vendor는 Domain 정본과 분리된 Setup/Local Profile에서 선택하며 암묵적 MariaDB fallback은 제거했다.
- 위험조치는 Approval Snapshot → Owner Command → Audit/Reconcile 경계를 강화했고 직접 우회 API를 fail-closed했다.
- ADM/BZA OpenAPI/Generated Client/Consumer, DB3 static, Batch, Fixed-Length, QA verifier currentization을 재검증했다.
- Testing-tools 최종 파일별 회귀는 362 tests 중 340 PASS / 22 pwsh-only SKIP, DB verification 75/75 PASS.
- Root Gradle/Live DB3/Process Kill/Browser/Distributed/Security/Performance/Windows portability는 미검증이므로 최종 QA 완료가 아니다.

세부 실행 증거: `TEST_AND_EVIDENCE.md`
잔여 Runtime: `OPEN_ISSUES.md`
Codex 재검증 최소 범위: `current/CPF_CODEX_REVALIDATION_SCOPE.md`
독립 QA finding 개발 재검증: `QA_FINDING_REVALIDATION.csv`

## 통합 상태 문서 정리

- Requirement 상태 정본은 `current/CPF_REQUIREMENT_MASTER.csv` + Parts이며, `REQUIREMENT_STATUS.csv`는 호환 Projection이다.
- 과거 `FINAL_QA_20_50`, `HARDENING_40_STATUS`, `QA_B3_REMEDIATION_STATUS`, `FINAL_COMMERCIAL_READINESS_REVIEW`는 필요한 상태를 이 문서·`OPEN_ISSUES.md`·`QA_FINDING_REVALIDATION.csv`·`TEST_AND_EVIDENCE.md`·`CPF_CODEX_REVALIDATION_SCOPE.md`에 병합한 뒤 제거한다.
- Hardening 40의 **요구 자체**는 `current/CPF_COMMERCIAL_HARDENING_40_CROSSMAP.md`에서 계속 유지한다. 상태만 별도 CSV로 중복 관리하지 않는다.
- 현재 정적/독립 Gate의 제품 FAIL은 0으로 재검산했으나 Java25 Root Gradle, Live DB3, Process Kill, Provider 장애, Browser, Deployment, Security/Performance는 `OPEN_ISSUES.md`의 미검증 상태를 유지한다.
- 독립 QA finding 25건의 개발 보완 상태는 `QA_FINDING_REVALIDATION.csv` 하나에서 관리한다. QA/Codex 최종 판정은 해당 역할 소유자가 갱신한다.


## Repository Currentization / History-less Cleanup

- Git history를 보존소로 사용하지 않는다. 필요한 현재 Requirement/Decision/Evidence/Codex continuity는 canonical current 문서에 병합한 뒤 과거 세션·날짜·checkpoint·campaign 문서/도구를 제거한다.
- Governance history ledger, 단계성 QA/Hardening 상태 문서, 회차별 Evidence, R4/R6 campaign helper/test, 미사용 legacy verifier, 중복 Cache/Batch migration tombstone, legacy Generated `com.customer.*` Source를 cleanup 대상으로 확정했다.
- Active Tool이 삭제 경로를 다시 요구하지 않는지 전수 검사했으며 deleted-path stale reference는 0건이다.
- 최종 Delete Manifest는 184개 파일, Garbage Decision 184개, protected delete 0, directory delete 0이다.
- 비보호 빈 디렉터리는 0개이며 사용자 적용 명령은 파일 삭제 후 비보호 빈 폴더도 bottom-up으로 제거한다.
- `cpf-tools/build/**`는 제품 Gradle Plugin/BOM Source이므로 cleanup 대상이 아니다.
