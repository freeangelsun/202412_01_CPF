# CPF QA39 자체 개발 요건 목록

## 목표

QA38 Push 이후 확인된 39개 Defect를 공통 원인과 선행 의존성 기준으로 해결하고, 부분 구현·미구현·미검증·실패·재확인 필요를 남기지 않는다.

## 작업 순서 원칙

1. Truth Reset
2. Build/Artifact Graph
3. Ownership/Dependency
4. Generator/Profile
5. Leaf Provider Runtime
6. DB 3 Vendor
7. Consumer
8. Java/Frontend/Runtime/Browser/Supply-chain
9. exact-SHA Evidence와 Codex 독립 검수

## 요건

| ID | 우선순위 | Stage | Owner | 제목 | 원인 Defect |
|---|---|---|---|---|---|
| QA39-SELF-001 | P0 | 00_TRUTH_RESET | Governance/QA | 최신 SHA 상태 재판정 | QA39-DEF-012;QA39-DEF-013 |
| QA39-SELF-002 | P0 | 00_TRUTH_RESET | QA/Tooling | Exact-SHA Evidence 재발급 | QA39-DEF-012;QA39-DEF-014 |
| QA39-SELF-003 | P0 | 01_BUILD_GRAPH | Root Build | 모든 Module Settings 등록 | QA39-DEF-001 |
| QA39-SELF-004 | P0 | 01_BUILD_GRAPH | Build/Release | Artifact Registry 단일 정본화 | QA39-DEF-002 |
| QA39-SELF-005 | P0 | 01_BUILD_GRAPH | Build/Release | BOM 버전 보간 교정 | QA39-DEF-003 |
| QA39-SELF-006 | P0 | 01_BUILD_GRAPH | QA/Tooling | Internal FQN Classpath Gate | QA39-DEF-004 |
| QA39-SELF-007 | P0 | 01_BUILD_GRAPH | Architecture | Starter 순환 의존 제거 | QA39-DEF-005 |
| QA39-SELF-008 | P0 | 02_OWNERSHIP | Runtime Control | Runtime Control SPI 분해 | QA39-DEF-006 |
| QA39-SELF-009 | P0 | 02_OWNERSHIP | Messaging IBM MQ | IBM MQ Plugin 완결 | QA39-DEF-007 |
| QA39-SELF-010 | P0 | 02_OWNERSHIP | File Archive | Archive API Bean 회귀 복구 | QA39-DEF-008 |
| QA39-SELF-011 | P0 | 02_OWNERSHIP | cpf-batch | Batch Runtime 의존 범위 교정 | QA39-DEF-009 |
| QA39-SELF-012 | P0 | 03_GENERATOR | Generator | Provider Binding 실제 Dependency Resolution | QA39-DEF-010 |
| QA39-SELF-013 | P0 | 04_VERIFICATION | QA/Tooling | False-Green Gate 교체 | QA39-DEF-011 |
| QA39-SELF-014 | P0 | 04_VERIFICATION | All Modules | Java 25 Fresh 전체 Build/Test | QA39-DEF-014 |
| QA39-SELF-015 | P0 | 04_VERIFICATION | Build/Release | 전체 Publication/BOM Consumer 검증 | QA39-DEF-002;QA39-DEF-003;QA39-DEF-038 |
| QA39-SELF-016 | P1 | 05_TEST_COMPLETION | Starter Owners | Leaf Test 0개 제거 | QA39-DEF-016 |
| QA39-SELF-017 | P1 | 05_TEST_COMPLETION | QA/Tooling | Core-only 실제 Consumer Fixture | QA39-DEF-015 |
| QA39-SELF-018 | P1 | 06_DB | DB/Starter Owners | 3 Vendor Lifecycle 정본 | QA39-DEF-017;QA39-DEF-036 |
| QA39-SELF-019 | P1 | 07_INTEGRATION | Integration SFTP | SFTP 상용 Runtime 완결 | QA39-DEF-018 |
| QA39-SELF-020 | P1 | 07_INTEGRATION | Integration TCP | TCP Transport Lifecycle 완결 | QA39-DEF-019 |
| QA39-SELF-021 | P1 | 07_INTEGRATION | Notification | Notification Outbox Recovery | QA39-DEF-020 |
| QA39-SELF-022 | P1 | 07_INTEGRATION | Notification Email | Email Provider 완결 | QA39-DEF-021 |
| QA39-SELF-023 | P1 | 07_INTEGRATION | Notification SMS | SMS Shared Runtime | QA39-DEF-022 |
| QA39-SELF-024 | P1 | 08_SECURITY | Security | Resource Server fail-closed | QA39-DEF-023 |
| QA39-SELF-025 | P1 | 08_SECURITY | Security | Service Identity Secret/Replay | QA39-DEF-024 |
| QA39-SELF-026 | P1 | 07_INTEGRATION | Integration FTPS | FTPS 정식 Product화 | QA39-DEF-001;QA39-DEF-025 |
| QA39-SELF-027 | P1 | 07_INTEGRATION | Integration gRPC | gRPC 정식 Product화 | QA39-DEF-001;QA39-DEF-026 |
| QA39-SELF-028 | P1 | 07_INTEGRATION | Integration Object Storage | Object Storage 정식 Product화 | QA39-DEF-001;QA39-DEF-027 |
| QA39-SELF-029 | P1 | 07_INTEGRATION | Integration SMB | SMB 정식 Product화 | QA39-DEF-001;QA39-DEF-028 |
| QA39-SELF-030 | P1 | 07_INTEGRATION | Integration SOAP | SOAP 정식 Product화 | QA39-DEF-001;QA39-DEF-029 |
| QA39-SELF-031 | P1 | 07_INTEGRATION | Integration Webhook | Webhook Delivery Runtime | QA39-DEF-001;QA39-DEF-030 |
| QA39-SELF-032 | P1 | 07_INTEGRATION | Integration Realtime | Realtime 정식 Product화 | QA39-DEF-001;QA39-DEF-031 |
| QA39-SELF-033 | P1 | 09_MESSAGING | Messaging | Kafka/Rabbit/JMS/IBM MQ 양방향 완결 | QA39-DEF-032 |
| QA39-SELF-034 | P1 | 09_MESSAGING | Messaging | Named Binding/Bridge 실제 실행 | QA39-DEF-010;QA39-DEF-032 |
| QA39-SELF-035 | P1 | 10_SCHEDULER | Scheduler Quartz | Quartz JDBC Cluster Runtime | QA39-DEF-033 |
| QA39-SELF-036 | P1 | 02_OWNERSHIP | Architecture | Starter Internal Package 경계 | QA39-DEF-034 |
| QA39-SELF-037 | P1 | 11_CONSUMERS | Consumer Owners | 실제 Consumer 연결 | QA39-DEF-035 |
| QA39-SELF-038 | P1 | 11_CONSUMERS | ADM/BZA | 운영 화면 완결 | QA39-DEF-037 |
| QA39-SELF-039 | P1 | 12_SUPPLY_CHAIN | Build/Release | Supply-chain 완결 | QA39-DEF-038 |
| QA39-SELF-040 | P1 | 00_TRUTH_RESET | QA/Tooling | Merge-aware Overlay 적용 | QA39-DEF-039 |
| QA39-SELF-041 | P1 | 04_VERIFICATION | QA/Tooling | Properties Consumer Gate | QA39-DEF-007;QA39-DEF-018;QA39-DEF-019;QA39-DEF-023;QA39-DEF-025;QA39-DEF-029 |
| QA39-SELF-042 | P1 | 04_VERIFICATION | QA/Tooling | Bean/Consumer Liveness Gate | QA39-DEF-008;QA39-DEF-035 |
| QA39-SELF-043 | P0 | 13_FINAL_GATE | Governance/QA | 완료 지연 Gate | QA39-DEF-011;QA39-DEF-013;QA39-DEF-014 |
| QA39-SELF-044 | P0 | 13_FINAL_GATE | All Owners | Codex 독립 통합 검수 | ALL |

세부 Acceptance, Test, Evidence, Dependency는 `CPF_QA39_SELF_DEVELOPMENT_REQUIREMENTS.csv`를 정본으로 사용한다.
