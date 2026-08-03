# CPF QA39 Codex 독립 최종 검수 요청서

## 기준
- Repository: `https://github.com/freeangelsun/202412_01_CPF`
- Branch: `master`
- Overlay Base SHA: `4aea798c913787e86341809e2cef2b9495cbf7ba`
- 정본: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
- QA Matrix: `cpf-docs/work/matrix/CPF_QA39_REQUIREMENT_SCENARIO_RESULT_MATRIX.csv`

## 검수 범위
이번 변경 Source·SQL·Test·Config·Frontend·Script·Catalog·BOM·Evidence 전체. 보호 경로는 변경 대상으로 보지 않는다. 기능 재설계나 불필요한 이름 변경을 하지 않는다.

## 보호할 성공 기능
6 Profile, Runtime Control Owner Applier, Broker UNKNOWN_RESULT/Reconcile, Notification Outbox/Receipt, SFTP Path Policy, Oracle/PostgreSQL/MariaDB Canonical Source, Generator resolved lock/exception policy, 기존 공개 계약 호환성.

## 한 번만 실행할 통합 순서
1. HEAD/origin/master/Working Tree와 적용 후 exact SHA 기록.
2. `cpf-tools/verification/qa39/test-qa39-company-laptop-environment.ps1`과 `invoke-qa39-low-cost-gates.ps1` 각 1회. Python 설치를 요구하거나 호출하지 않는다.
3. Java25 Fresh Cache 전체 Build/Test/Publication 1회.
4. ADM/BZA clean npm lifecycle와 Chromium/Firefox/WebKit 1회.
5. Oracle/PostgreSQL/MariaDB Fresh Install→Upgrade→Runtime Query→Rollback→Reapply 1회.
6. Kafka/RabbitMQ/JMS/IBM MQ/TCP/SFTP/SMTP/OTLP와 process kill/ACK loss/UNKNOWN_RESULT/reconcile/fencing 1회.
7. Generator 전체 Profile/Provider 조합과 재생성 idempotency 1회.
8. SBOM/License/Vulnerability/POM/Publication/Hash 1회.
9. Matrix/Evidence exact SHA 정합과 Clean checkout after 확인.

## 이미 수행한 검증
합성 적용/재적용, Catalog/물리/BOM, 명칭, DB 정적 parity, OpenAPI/Generated/Route, Java 21 합성 `-Werror` (Product 60, Spring/JDBC Adapter 78, Controller 39, Unit Test Source 14, Batch/Kafka 25 Class), TypeScript strict, diff/JSON/CSV/Secret/Hygiene, R2 추출 실패 상태→R3 Java-only 적용 64 operations(구조 56 + R2 Python 잔여 8 제거)→재적용 0 operations, 고객 Provider Fixture compile/runtime. 동일 합성 Fixture를 반복하지 않는다. 실제 Repository의 저비용 Gate는 적용 확인을 위해 1회만 실행한다.

## 완료 처리 금지 조건
부분 구현, 미구현, 미검증, 실패, stale SHA, READY/PLANNED/NOT_EXECUTED Evidence, 환경 없는 Runtime 성공, Public BOM 내부 Leaf, Legacy 경로/Artifact, Dirty checkout, 보호 경로 변경 중 하나라도 존재하면 완료 금지.

## 실패 기록
명령, 시작/종료 시각, Tool/Runtime Version, Profile, Exit Code, Sanitized 오류, Source Defect 또는 Environment Blocker 구분을 Evidence에 기록한다. 수정 시 영향 범위의 최소 Gate부터 재실행하고 전체 대형 검증을 반복 남발하지 않는다.
