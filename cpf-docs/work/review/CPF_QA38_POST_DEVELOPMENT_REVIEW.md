# CPF QA38 개발 후 독립 리뷰

## 1. 기준과 판정

- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- 개발 기준 SHA: `dafe5c0e5260ea8149234e8ab2e75347e75338c1`
- 통합 Requirement: 156건
- 통합 Scenario: 52건
- 개발 상태: 완료 156건
- 검증 상태: 완료 40건, 미검증 116건
- 삭제 확정 Legacy 파일: 160개
- 삭제 후 빈 폴더 후보: 24개

`개발 완료`는 Source·SQL·Config·Generator·Consumer·Test·검증 Gate가 구현됐다는 뜻이다. Java 25 전체 Build, 실제 DB·Broker·Browser 실행을 하지 않은 Requirement는 `검증 완료`로 승격하지 않았다.

## 2. 실제 구현 및 이관

### Core/Common 경량화

- `cpf-core`의 DataSource, MyBatis, AOP, OpenAPI, Security 조립, Broker Reliability, JDBC Channel Registry, Archive·Attachment Local Runtime, HTTP/Service Call, File Transfer, Remote Log, Runtime Control Agent/Plane/Applier, Logging Aspect, JDBC Routing Runtime을 Owner Starter로 이동했다.
- `cpf-common`의 Redis/Valkey, Caffeine, POI/XLSX, Validation Provider, DataSource/MyBatis/Runtime Control 기술 설정을 Owner Starter로 이동했다.
- 기존 Core/Common Source·Test·Resource는 새 Owner의 동일 계약 구현과 함께 이동하고 Delete Manifest에 exact path로 기록했다.

### Starter와 Consumer

- Base·Leaf·Aggregate·Profile 구조와 13개 Versioned Capability Profile을 구현했다.
- ADM, BZA, Gateway, Batch, Reference, Member가 Profile/Leaf Starter를 실제 Build Dependency로 소비하도록 연결했다.
- Generator는 `resolvedStarters`, Profile Version, Starter Version Lock, Named Provider Binding을 생성한다.
- RabbitMQ, Jakarta JMS, IBM MQ Plugin Boundary, Kafka 보완, Reliability JDBC, TCP, Fixed-length, ISO8583, SFTP, Notification/Email/SMS, Quartz Runtime을 구현했다.
- MariaDB, PostgreSQL, Oracle에 Reliability/SFTP/Notification/Webhook Migration·Rollback SQL을 같은 Logical Contract로 제공했다.

## 3. 개발 중 발견하고 수정한 실제 결함

1. ISO8583 Field 65 이상이 Secondary Bitmap offset 없이 Field 1~64로 오인되던 결함을 수정했다.
2. STX/ETX Binary payload 내부 ETX가 조기 종료되던 결함을 DLE escaping으로 수정했다.
3. TCP·IBM MQ Properties의 중복 Member 생성 결함을 제거했다.
4. Capability Binding의 두 번째 Default 등록 실패 후 잘못된 Binding이 Registry에 남는 비원자적 갱신을 후보 Map 검증 후 commit 방식으로 수정했다.
5. SFTP Adapter가 없을 때 `PLANNED` 성공 상태를 반환하던 거짓 구현을 제거하고 fail-closed 처리했다.
6. Core Source만 삭제하고 Test를 남겨 컴파일이 깨지는 문제를 방지하기 위해 Test도 Owner Starter로 이동했다.
7. 오래된 137-file Overlay가 실제 작업본을 덮는 Packaging 오류를 제거했다.
8. Generated Domain settings 오참조와 Artifact Catalog 누락을 보완했다.
9. 검증 Script의 고정 `/mnt/data` 경로를 제거하고 Repository Root 인자를 사용하도록 수정했다.

## 4. 실행 검증

- PASS: Starter/Project/AutoConfiguration/Profile/Artifact 구조 Gate
- PASS: Java Class-scope duplicate member Gate
- PASS: Oracle/PostgreSQL/MariaDB SQL semantic parity Gate
- PASS: 전체 Main Java `javac` parser 검사에서 구문 오류 패턴 없음
- PASS: JDK 21 Pure Runtime Harness 33개 검사
- PASS: Fixed-length, Binary/BCD/Hex/Endian/Unsigned, ISO8583 Primary/Secondary Bitmap·MAC
- PASS: TCP framing 4종, DLE escaping, oversize, correlation, orphan, timeout, `UNKNOWN_RESULT`, reconciliation
- PASS: Archive traversal 차단, Notification quiet-hours, SMS idempotency
- PASS: Named/default Messaging Binding ambiguity fail-closed, Schema quarantine
- PASS: Service Identity HMAC rotation, audience, nonce, expiry
- PASS: 보호 경로 포함 0건, 미구현 Marker 0건

## 5. 실행하지 못한 검증

실행환경에는 Java 25, Gradle, 외부 Dependency Cache, Docker, 실제 DB/Broker/SFTP, Node/Browser, Supply-chain Tool이 없었다. 다음은 성공으로 기록하지 않는다.

- Java 25 Fresh Cache 전체 Gradle Build/Test/Publication
- MariaDB/PostgreSQL/Oracle 실제 Fresh Install·Upgrade·Rollback·Reapply·Backup/Restore
- Kafka/RabbitMQ/JMS Provider/IBM MQ Plugin/SFTP 실제 Runtime과 장애·복구
- ADM/BZA Frontend Clean Verify와 Playwright Chromium/Firefox/WebKit
- Toxiproxy, OTel Collector, Multi-instance, Process Kill, Fencing
- SBOM, Vulnerability, License, Artifact Publication/Hash 검증

## 6. 삭제와 회귀 보호

- 삭제 확정 파일 160개는 `CPF_QA38_DELETE_MANIFEST.txt`와 적용 Script로 실제 삭제한다.
- 빈 폴더 후보 24개는 하위 경로부터 비어 있는 경우에만 제거한다.
- 다른 GPT 보호 경로는 Overlay·Delete Manifest·빈 폴더 Manifest에 포함하지 않았다.
- 기준 SHA 이후 Push는 기준 SHA의 후손이고 QA38 관리 경로와 겹치지 않을 때만 적용을 허용한다.
- Commit·Push는 수행하지 않았다.
