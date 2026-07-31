# CPF QA33 개발 결과 보고

## 기준과 범위

- 작업 기준 원격 SHA: `21eb93c7a7110f593e7d2db725046acb6635e7dd`
- 산출물 형태: Commit 없는 Root 상대경로 Overlay
- README·Guide·Asset: 미수정

## 실제 수정

- 삭제된 BOM Included Build 복원: `cpf-platform-bom`, Maven Publication, Local/Staging/Internal Task
- 삭제된 Convention Plugin Included Build 복원: Java 25 Convention, Publication, Test
- ADM/BZA/Gateway Starter project path 정정
- Gateway 중복 `bootJar` 제거
- 전 Subproject MariaDB 강제 주입 제거
- ADM 삭제 함수 import 제거
- BFF Credential Response에서 Token·Session ID 제거 및 알려지지 않은 Credential DTO fail-closed
- QA32 Runtime/Completion Gate를 exact SHA·clean tree·명령별 exit code·Evidence hash 기반으로 강화
- Evidence Template·Schema·negative fixture 추가
- Archive Entry를 Path/InputStream 기반 bounded streaming 계약으로 확장하고 임시 파일 atomic publish·부분 실패 cleanup·중복 entry 차단 추가

## 자체 정적 검증

- Python Gate syntax compile: 통과
- JSON Template/Schema/Fixture parse: 통과
- Overlay 비밀값 패턴 검사: 별도 Manifest 생성 시 수행
- Root-relative ZIP exclusion 검사: 수행

## 실행하지 못한 검증

현재 실행 환경은 GitHub Repository를 로컬로 Clone할 DNS 연결과 Java 25/Gradle Dependency Cache, Node Registry, Oracle/PostgreSQL/MariaDB/Kafka/Browser/Agent topology를 제공하지 않았다. 따라서 다음은 성공으로 기록하지 않는다.

- `gradlew.bat projects`, `help`, `clean test`
- ADM/BZA `npm ci`, Orval, build, Playwright 3 Browser
- 3 DB Vendor install/upgrade/rollback/drift
- Kafka rebalance/broker outage/worker crash/reply loss
- Scheduler/Batch/Gateway/Agent 다중 인스턴스 장애 복구
- ORT/Syft/Grype Final Artifact Scan

## 판정

확정 Source Blocker는 수정했으나 QA33 전체 138 Requirement와 414 Scenario의 Runtime Evidence가 생성되지 않았으므로 전체 완료 판정은 금지한다. Overlay 적용 후 사용자 환경에서 정본 Gate를 실행하고 실패 Source를 이어서 수정해야 한다.
