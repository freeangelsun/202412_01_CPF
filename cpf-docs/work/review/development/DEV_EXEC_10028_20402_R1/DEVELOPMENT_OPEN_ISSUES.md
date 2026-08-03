# DEVELOPMENT OPEN ISSUES

## 기존 개발GPT 미완료·미검증

| ID | 상태 | 내용 | 완료 조건 |
|---|---|---|---|
| DEV-001 | 미완료 | 전체 `10,375` Requirement의 Source·Consumer·Runtime 독립 전수검수 미수행 | exact SHA에서 Requirement별 검수 결과와 Evidence 제출 |
| DEV-002 | 미검증 | Java 25 Gradle 전체 Build/Test 및 Publication 미실행 | 전체 Gate 성공과 실패 0건 Evidence |
| DEV-003 | 미검증 | Oracle·PostgreSQL·MariaDB 실제 install/upgrade/rollback/drift 미실행 | 공식 3 Vendor Runtime Evidence |
| DEV-004 | 미검증 | Batch Process Kill·재기동·다중 Worker·Metadata DB Runtime 미실행 | UNKNOWN 복구·중복방지·fencing Runtime PASS |
| DEV-005 | 미검증 | ADM Browser E2E·권한별 Route/Menu/API 차단 미실행 | Browser E2E와 권한 우회 0건 |

## QA Finding 25건 — 기존 1~10,027

| Finding ID | 심각도 | Requirement | 대상 | 검수 결과 | 조치 기준 |
|---|---|---|---|---|---|
| QA-DEV-S4-001 | HIGH | CPF-SELF-DEV-S4-001..009 | Codex | 개발 Evidence와 실행 원장이 실제 Push SHA가 아닌 이전 기준 SHA를 검증 대상으로 기록 | 최신 master에서 재검수 후 요건 미충족·결함 확인 시: 최신 master SHA에서 각 Gate와 대체검증을 재실행하거나, 동일 파일 Hash를 검증하는 재현 가능한 Exact-SHA Evidence를 Codex 영역에 새로 작성 |
| QA-DEV-S4-002 | HIGH | CPF-XFER-S4-JAVA25 | Codex | Codex Java25/Gradle 이관 명령의 ExpectedSourceSha가 Push 전 SHA로 고정 | 최신 master에서 재검수 후 요건 미충족·결함 확인 시: Codex 검수 후보 SHA를 조회한 뒤 그 exact SHA로 명령과 모든 Evidence 경로를 갱신 |
| QA-DEV-S4-003 | CRITICAL | CPF-SELF-DEV-S4-002 | 개발GPT/Codex | Package Manifest와 원장이 포함됐다고 주장한 핵심 로그가 최신 Git에 존재하지 않음 | 최신 master에서 재검수 후 요건 미충족·결함 확인 시: 누락 Evidence를 Git 추적 가능한 경로에 추가하거나 Codex가 최신 SHA에서 다시 실행하여 자신의 Evidence로 제출. Package/Change/File Hash Manifest도 실제 Git 파일 집합으로 재생성 |
| QA-DEV-S4-004 | HIGH | CPF-SELF-DEV-S4-003 | 개발GPT | P02 Owner Boundary Gate가 Requirement에서 요구하는 전체 경계를 전수검사하지 않음 | 최신 master에서 재검수 후 요건 미충족·결함 확인 시: settings/build dependency graph와 모든 main Source import를 대상으로 공식 Module·Package Ownership/Public/SPI/Internal/순환/역방향/Consumer 규칙을 전수검사하도록 Gate와 Negative Test 보강 |
| QA-DEV-S4-005 | HIGH | CPF-SELF-DEV-S4-005 | 개발GPT | P03 Transaction 전수 Gate와 이관 성공기준이 서로 불일치 | 최신 master에서 재검수 후 요건 미충족·결함 확인 시: Java/SQL/API/OpenAPI/Frontend/Config의 transactionId/globalId/gid 별칭과 Annotation 중복·오용을 전체 Repository에서 검사하고 허용 Migration/alias 구역만 명시적 Allowlist로 제외 |
| QA-DEV-S4-006 | CRITICAL | CPF-SELF-DEV-S4-006 | 개발GPT | BZA 공용 API가 ADM 구현과 동일 파일로 치환되며 기존 bza* 공개 함수가 제거됨 | 최신 master에서 재검수 후 요건 미충족·결함 확인 시: 기존 BZA Public API를 유지하거나 명시적 호환 Alias를 제공하고 실제 BZA 전체 Consumer를 TypeScript로 Compile. ADM/BZA 메시지·Operation naming·Same-origin 정책도 각 제품 경계에 맞게 복원 |
| QA-DEV-S4-007 | MEDIUM | CPF-SELF-DEV-S4-006 | 개발GPT | Frontend Actor Body Guard가 string/FormData/URLSearchParams/Blob 본문을 검사하지 않음 | 최신 master에서 재검수 후 요건 미충족·결함 확인 시: 지원 Body 타입별 정책을 명시하고 FormData/URLSearchParams의 key를 검사. 문자열 JSON은 parse 후 검사하거나 raw body를 Privileged API에서 금지 |
| QA-DEV-S4-008 | HIGH | CPF-SELF-DEV-S4-006 | 개발GPT | Batch Actor Test가 Controller endpoint/Consumer 전체 호출 경로를 검증하지 않음 | 최신 master에서 재검수 후 요건 미충족·결함 확인 시: Controller를 실제 Client spy/fake와 구성하여 4개 privileged endpoint별 정상·누락 actor·중첩 alias·client exception·invalid request 상태를 검증 |
| QA-DEV-S4-009 | HIGH | CPF-SELF-DEV-S4-006 | 개발GPT | Batch mutation endpoint의 IllegalArgumentException 처리 방식이 일관되지 않음 | 최신 master에서 재검수 후 요건 미충족·결함 확인 시: 모든 mutation에서 Validation/Conflict/Unknown/Unavailable을 일관된 Error Contract로 매핑하고 Test 추가 |
| QA-DEV-S4-010 | CRITICAL | CPF-SELF-DEV-S4-001..009 | 개발GPT | QA 통과 전에 checkpoint Requirement의 development_status/verification_status를 완료로 기록 | 최신 master에서 재검수 후 요건 미충족·결함 확인 시: QA 통과 전 development_status는 부분 구현/재확인 필요, verification_status는 미검증/재확인 필요로 정정. 개발GPT 역할 상태와 전체 상태를 분리 |
| QA-DEV-S4-011 | HIGH | CPF-SELF-DEV-S4-001..009 | 개발GPT/Codex | 실행 원장 다수 명령이 placeholder라 재현 가능한 exact command가 아님 | 최신 master에서 재검수 후 요건 미충족·결함 확인 시: 실제 스크립트 경로, root, output path, exact SHA, exit code를 포함한 완전한 명령으로 교체 |
| QA-DEV-S4-012 | CRITICAL | CPF-SELF-DEV-S4-001 | 개발GPT | Split Master Gate가 verified SHA를 과거 Commit으로 하드코딩 | 최신 master에서 재검수 후 요건 미충족·결함 확인 시: 하드코딩을 제거하고 --expected-sha 또는 git rev-parse HEAD를 필수 입력으로 사용. Git working tree가 없으면 PASS가 아니라 미검증/실패로 처리하고 최신 SHA에서 Evidence 재생성 |
| QA-DEV-S4-013 | HIGH | CPF-SELF-DEV-S4-001 | 개발GPT | Split Gate가 execution_order의 형식·정렬·연속성을 검증하지 않음 | 최신 master에서 재검수 후 요건 미충족·결함 확인 시: execution_order canonical parser, phase/order 순서, 중복·누락·역전, Work Package 경계를 전수검증. Requirement/Scenario의 비정상 ID는 continuity 생략이 아니라 즉시 실패 |
| QA-DEV-S4-014 | CRITICAL | CPF-SELF-DEV-S4-002 | Codex | P01 Traceability PASS가 Git HEAD를 확인하지 않은 Overlay 실행 결과 | 최신 master에서 재검수 후 요건 미충족·결함 확인 시: Codex가 최신 Repository working tree에서 --expected-sha <HEAD> --require-clean으로 실행하고 실제 단일 Result Matrix 경로를 지정해 행·완료·검증 Coverage를 확인 |
| QA-DEV-S4-015 | HIGH | CPF-SELF-DEV-S4-002 | 개발GPT | Traceability main flow가 결과 원장 부재·0행을 정상 PASS로 허용 | 최신 master에서 재검수 후 요건 미충족·결함 확인 시: 개발 Checkpoint와 Release 모드를 분리하되 활성 단일 Requirement 원장은 항상 필수로 읽고, Scope 내 Requirement Coverage와 필수 역할 컬럼을 강제. sparse 보조 원장은 별도 명칭으로 분리 |
| QA-DEV-S4-016 | HIGH | CPF-SELF-DEV-S4-003 | 개발GPT | Owner Gate가 공식 Settings/Surface Policy/전체 Build Graph를 사용하지 않음 | 최신 master에서 재검수 후 요건 미충족·결함 확인 시: Settings/모든 Build/모든 main Source를 읽는 dependency+package graph Gate로 재개발 |
| QA-DEV-S4-017 | HIGH | CPF-SELF-DEV-S4-004 | 개발GPT | DB-less Gate와 Test가 실제 Spring Context·Consumer·DB 실패 경로를 실행하지 않음 | 최신 master에서 재검수 후 요건 미충족·결함 확인 시: Resolver/Consumer 전수 확인 후 Java21 Context 또는 독립 Harness와 실제 Service fallback Test 작성 |
| QA-DEV-S4-018 | CRITICAL | CPF-SELF-DEV-S4-005 | 개발GPT | 표준 실행 Annotation 0건인데 Gate PASS하며 미부착 Endpoint는 거래 Header 검증을 우회 | 최신 master에서 재검수 후 요건 미충족·결함 확인 시: 업무 Controller/Operation 전수 목록과 Annotation Coverage를 대조하고 미부착 업무 Endpoint를 실패시키며 Health/Swagger/Callback만 근거 있는 Allowlist로 제외 |
| QA-DEV-S4-019 | HIGH | CPF-SELF-DEV-S4-006 | 개발GPT/Codex | Operator Trust PASS가 실제 전체 App이 아닌 Frontend 2개·Controller 1개와 임시 Stub Harness에 한정 | 최신 master에서 재검수 후 요건 미충족·결함 확인 시: 실제 ADM/BZA Project 전체 Build와 위험 Mutation Endpoint별 인증 Actor/Audit Actor E2E Test를 실행 |
| QA-DEV-S4-020 | CRITICAL | CPF-SELF-DEV-S4-007 | 개발GPT | HTTP Client Endpoint Registry가 DNS Address 검증·Pin 없이 Hostname URL을 반환 | 최신 master에서 재검수 후 요건 미충족·결함 확인 시: 실제 HTTP Transport 연결 직전에 DNS Resolve·Address 검증·Pinned Connection을 구현하고 Gateway/Host Agent와 동등한 정책을 적용 |
| QA-DEV-S4-021 | MEDIUM | CPF-SELF-DEV-S4-007 | Codex/외부환경 | Durable Audit PASS가 Source Token Fixture뿐이며 실제 DB·다중 인스턴스·Process Kill Evidence가 없음 | 최신 master에서 재검수 후 요건 미충족·결함 확인 시: MariaDB/PostgreSQL/Oracle에서 2개 ADM 인스턴스, Owner 실행 전후 Process Kill, Relay Retry/Exhaustion을 실행 |
| QA-DEV-S4-022 | CRITICAL | CPF-SELF-DEV-S4-008 | 개발GPT | PostgreSQL과 Oracle Fresh Install SQL에 MariaDB 전용 LONGBLOB·MEDIUMTEXT 타입 포함 | 최신 master에서 재검수 후 요건 미충족·결함 확인 시: Canonical Type Mapping을 수정해 PostgreSQL은 BYTEA/TEXT 계열, Oracle은 BLOB/CLOB 계열로 재생성하고 모든 Lifecycle SQL을 재검사 |
| QA-DEV-S4-023 | HIGH | CPF-SELF-DEV-S4-008 | 개발GPT | DB Vendor Gate가 Lifecycle SQL 내용·Dialect·Parity·Rollback을 검사하지 않고 경로 존재만 PASS | 최신 master에서 재검수 후 요건 미충족·결함 확인 시: Vendor SQL Parse/Forbidden Token/Canonical Metadata Parity/Migration Checksum/Rollback 역연산 Gate를 추가 |
| QA-DEV-S4-024 | HIGH | CPF-SELF-DEV-S4-009 | 개발GPT | Starter Catalog packageBase와 실제 Persistence Product Config Package가 불일치 | 최신 master에서 재검수 후 요건 미충족·결함 확인 시: Catalog PackageBase 의미를 명확히 하고 실제 Source를 이동하거나 Catalog에 Owner/Export Package를 정확히 분리해 전수 검증 |
| QA-DEV-S4-025 | HIGH | CPF-SELF-DEV-S4-009 | 개발GPT/Codex | Starter Catalog baselineSha가 최신 Git과 다르며 Gate가 exact HEAD 정합성을 강제하지 않음 | 최신 master에서 재검수 후 요건 미충족·결함 확인 시: Catalog Revision/Source SHA를 최신 후보 SHA와 동기화하고 Gate에서 expected-sha/clean-tree를 필수 검증 |

QA 상세 근거: `cpf-docs/work/qa/qa-dev-r1-20260803-r2/`

삭제·정리 대상은 없다.
