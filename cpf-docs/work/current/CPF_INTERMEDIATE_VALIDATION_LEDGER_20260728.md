# CPF Enterprise QA 중간 검증 Ledger

## 기준

- 기준 Repository SHA: `ca3cf8a12290903cc482b5e092cdb43e6bf8f1eb`
- 검증 대상: 중간 Root Overlay 작업본
- 실행 환경: Linux 작업 컨테이너, 설치 Java 기준
- Java 25 / Gradle 9.1 전체 Repository Build 환경은 아님

## 실제 실행 결과

| 검증 | 결과 | 비고 |
|---|---|---|
| Runtime Control Public API + 핵심 무의존 클래스 `javac` | PASS | `JAVAC_RC=0`, 오류 출력 0건 |
| Canonical Schema JSON parse | PASS | JSON 파싱 성공 |
| Canonical tableCount | PASS | 선언 165 / 실제 165 / 중복 0 |
| MariaDB V64 CREATE ↔ R64 DROP | PASS | 12 / 12, 누락·초과 0 |
| PostgreSQL V64 CREATE ↔ R64 DROP | PASS | 12 / 12, 누락·초과 0 |
| Oracle V64 CREATE ↔ R64 DROP | PASS | 12 / 12, 누락·초과 0 |
| Overlay 빈 파일 검사 | PASS | 0건 |
| Overlay class/log/tmp/bak/zip 잔존 검사 | PASS | 0건 |

## 미실행 또는 재검증 필요

- Java 25 + Gradle 9.1 전체 `clean assemble/test`
- 전체 Module compile 및 Spring AutoConfiguration 실제 기동
- MariaDB/PostgreSQL/Oracle fresh install/upgrade/rollback
- Flyway checksum 전체 재생성·tamper gate
- ADM/BZA Frontend build 및 Browser E2E
- Gateway streaming/multipart/range/backpressure Runtime 검증
- 2개 이상 Control Plane/Agent 인스턴스 HA/fencing/takeover
- Generated Domain 생성 및 Runtime Agent bootstrap
- Batch standalone package와 Scheduler/Worker Runtime
- QA Inventory 1,214개 최종 Traceability 및 실행 시나리오 201개 Evidence

## 완료 처리 금지 조건

위 미실행 검증을 수행하지 않고 `완료` 또는 Release PASS로 변경하면 안 된다. 실패가 나오면 같은 기준 Commit에서 Source/SQL/Test/Script/문서를 함께 수정한다.
