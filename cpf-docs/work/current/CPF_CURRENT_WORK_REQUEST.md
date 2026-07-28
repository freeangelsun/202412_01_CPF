# CPF Current Work Request

## 기준

- Repository: `https://github.com/freeangelsun/202412_01_CPF`
- Branch: `master`
- 구현 보완 기준 Commit: `ecaddd581a88ede22b63116effd61313744b3fbe`
- 적용 Package: `CPF_20260728_02_FINAL_COMPLETION_ROOT_OVERLAY.zip`

## 현재 요청

`20260728_02` 보완 Overlay를 적용한 최신 master 전체를 기준으로 **통합 실행 검증과 발견 결함의 즉시 수리**를 수행한다.

구현 자체의 주요 잔여는 이번 Overlay에서 보완했다. 현재 작업은 다음 실행 검증을 통과시키고 Evidence를 남기는 것이다.

1. Java 25 / Gradle 9.1 전체 `clean test assemble`
2. MariaDB·PostgreSQL·Oracle install / migration / upgrade / rollback
3. ADM Runtime Change Center Browser 및 권한별 E2E
4. Gateway 대용량·multipart·Range·timeout·retry·target-down E2E
5. Batch Scheduler·Worker·Center-Cut·Host-Agent runtime 변경 E2E
6. Runtime Control 다중 인스턴스·재시작·부분 실패·drift·rollback
7. Generator ACC/MBR/EXS 생성·빌드·실행 parity
8. 기존 QA Inventory와 Scenario의 최신 Commit 재판정

실행하지 않은 항목은 완료로 기록하지 않는다. 실패가 발견되면 문서만 갱신하지 말고 Source·SQL·Test·Script·Guide를 함께 수리한다.
