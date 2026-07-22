# CPF Gap Matrix

| ID | 상태 | 현재 Gap | 목표/완료 조건 |
|---|---|---|---|
| BASE-01 | 완료 | 최신 WIP Push 보존 | master 두 Commit 확인 |
| BASE-02 | 실패 | 관리 문서 기준선 e35 | 최신 master로 전면 갱신 |
| STRUCT-01 | 부분 구현 | 공식 Module은 존재, 전체 Runtime 불명 | Clean Build·Startup |
| STRUCT-02 | 부분 구현 | Package Rename 후 Reflection·Mapper 불명 | 전수 Contract Test |
| STRUCT-03 | 부분 구현 | CPF SystemCode 일부 경로만 확인 | Header·Error·Seed·UI E2E |
| LEGACY-01 | 재확인 필요 | PFW/XYZ 광역 치환 후 손상 가능 | Active 0 + Hash 무결성 |
| CORE-01 | 부분 구현 | Core Package 과대·Public 경계 불명 | API/SPI/Internal |
| CORE-02 | 미구현 | Utility·Config DX 정본 부족 | Cpf* API + Sample |
| CORE-03 | 미구현 | List/Page/Slice/Cursor 표준 불명 | Types·Limit·Runtime |
| COMMON-01 | 실패 | cmnDB 9 Table | Sample 1 Table |
| COMMON-02 | 실패 | File/Message 기술 기능 혼재 | Core/External 재소유 |
| COMMON-03 | 실패 | 채번 Framework화 | 제거 + BZA Sample |
| FIXED-01 | 실패 | cmnDB Fixed-Length | Core Engine·External Adapter |
| BZA-01 | 부분 구현 | 업무 원장성 Table 혼재 | Admin Owner 정리 |
| BZA-02 | 미구현 | 채번 Custom Sample | 선택형 UI·Guide |
| BATCH-01 | 실패 | Batch Table cpfDB 혼재 | batDB/bat_* Owner |
| BATCH-02 | 부분 구현 | Worker Source·Evidence 후보 | 2-process E2E |
| CUT-01 | 부분 구현 | Runner 책임·Parameter 불명 | Job/Item/Attempt E2E |
| CUT-02 | 미구현/미검증 | Global TPS·Backpressure | Cluster Runtime |
| CUT-03 | 미구현/미검증 | Failed-only Reprocess | History·Concurrency E2E |
| AGENT-01 | 미구현/미검증 | Primary/Secondary | Failover·Fencing |
| EXEC-01 | 부분 구현 | Java 중심 | Shell/File/Composite |
| GWY-01 | 부분 구현 | Module 존재 | Route·Failover Runtime |
| EXS-01 | 부분 구현 | Source·Schema 존재 | Institution Runtime |
| MSG-01 | 부분 구현 | Contract 존재 | Real Broker·Replay |
| FILE-01 | 부분 구현 | Contract/Code 혼재 | SFTP·File Watch E2E |
| SQL-01 | 실패 | Install가 Drop 포함 | Install/Reset 분리 |
| SQL-02 | 실패 | User/Grant 혼합 | Provision 분리 |
| SQL-03 | 재확인 필요 | Flyway Rename·Checksum | Baseline Decision |
| SQL-04 | 미검증 | Empty Install latest | MariaDB Evidence |
| SQL-05 | 미검증 | Upgrade·Rollback | Rehearsal |
| UI-01 | 부분 구현 | Vue Modernization | 구조·Production Build |
| UI-02 | 미검증 | Web/WAS 분리 | Nginx + Java |
| UI-03 | 미검증 | Browser | Login·Role·Control |
| SEC-01 | 부분 구현 | Source·Guide | Runtime Permission |
| SEC-02 | 미검증 | Masking·Audit | Browser·DB Evidence |
| DOC-01 | 실패 | Request/Gap stale | 최신 기준 갱신 |
| DOC-02 | 실패 | Premature DOCX | Markdown 정본 |
| DOC-03 | 부분 구현 | Guide high-level | 실제 API·Screen 반영 |
| ROOT-01 | 실패 | Work docs Root | cpf-docs 통합 |
| EVID-01 | 재확인 필요 | Evidence freshness | Commit Metadata |
| EVID-02 | 실패 | Static를 완료 근거로 사용 가능 | Runtime 분류 |
| BUILD-01 | 미검증 | latest clean test | 전체 종료코드 |
| BUILD-02 | 부분 구현 | qualityGate runtime 불포함 | Product Gate 추가 |
| PS-01 | 실패 | Gradle `powershell` hardcode | `pwsh` resolver |
| GEN-01 | 부분 구현 | Tool/Evidence 존재 | latest rerun |
| GEN-02 | 미검증 | 4 Vendor Matrix latest | Clean Re-execution |
| GEN-03 | 미검증 | ACC parity latest | DB/Runtime 포함 |
| DEPLOY-01 | 미구현/미검증 | 실제 Target 없음 | Dry-run 아닌 Deploy |
| DR-01 | 미검증 | Backup/Restore | Recovery Evidence |
