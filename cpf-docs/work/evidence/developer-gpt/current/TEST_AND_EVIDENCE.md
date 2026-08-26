# CPF 개발GPT TEST AND EVIDENCE

- 기준 입력 ZIP SHA-256: `111eb1734b863bc59cc845a488a61f5f35c0a95622c4db2f7fd53d1741cc5ff8`
- 최종 Candidate Source Identity SHA-256: `1401783383dd19c9d95e412b20fe6709aa724f3e0cbd10d8b43664607fe143df`
- Source files: 8299 / bytes: 38278033
- Managed Identity SHA-256: `4211e765e1892f07352ba2fb26252990f1dd705a717a9fc9a78e3f3d1266b807`
- Git 사용: Workspace Git write 없음. RT-02 provenance는 Local Git read-only 원칙이며 baseline 부재 시 `UNAVAILABLE`.

## PASS

- VSCode 전달 진단: 923건 전수 분류 및 원 진단 Source condition closure. 설정으로 warning 숨김 없음.
- DB tests: 217 PASS / 2 environment-dependent SKIP; DB verification: 86 PASS; 별도 Candidate low-cost DB suite: 131 PASS / 2 SKIP.
- Runtime/Security/Release Python gates: 94 PASS.
- Verification Python gates: 77 PASS.
- Generator/Open Git/Public Python gates: 85 PASS.
- Testing tooling: 391 PASS.
- 기타 Docker-static/Supply-chain/OpenAPI/Audit/Release: 47 PASS.
- Split Master: Requirement 30,605 / Scenario 40,806 / Execution 30,605 / missing cross-link 0.
- Exhaustive execution-scope: 30,605 execution / verification scope 10,605; newly found 17 scenario gaps and 6 owner-map groups corrected.
- Windows path: relative max 199, full max 233, failures 0.
- `verify_nxt3_config_contract.py`: PASS, failures=0 (active product source/config secret-like literal scan included).
- `verify_nxt3_hygiene.py`: PASS, protected_delete=0.
- `verify-cpf-batch-no-remote-kafka.py`: PASS, scannedFiles=3654, errors=0.
- `verify-cpf-batch-agent-fail-closed.py`: PASS.
- Generated Domain javac: member PASS / external PASS.
- ADM `verify:primary`: PASS.
- ADM OpenAPI source: PASS, operations=337/public=337.
- ADM operation consumer closure: PASS, operations=337/consumed=336/waived=1.

## 실제 실행하지 못한 검증

다음 항목은 이 sandbox에 Java 25, Gradle 9.1 distribution, PowerShell, Docker, 실제 VSCode JDT 및 설치된 Frontend `node_modules`가 없어 성공 처리하지 않았다.

- Java 25 + Gradle 9.1 Root Build / full test / publication
- Oracle/PostgreSQL/MariaDB physical Fresh/Upgrade/Rollback/Reapply runtime
- Kafka-free Batch 5-role / two-worker / process-kill / UNKNOWN / reconcile runtime
- One-WAS, ADM/BZA runtime OpenAPI release, Browser E2E, Performance live
- VSCode Fresh Import/JDT Problems `0 Error / 0 Warning` 실측
- ADM eslint/vue-tsc/vitest/vite build. `npm ci`는 sandbox network timeout으로 dependency를 확보하지 못했으며 `verify:generated`는 TypeScript compiler 부재 지점까지 진행했다.

## 이전 Runtime은 PASS로 승계하지 않음

이전 사용자 로컬 Full Runtime은 `PASS=135 FAIL=14 SKIP_ENV=2 NOT_EXECUTED=7`, ExitCode 1이었다. 이번 Source Identity와 다르므로 원인 분석 자료로만 사용한다.
