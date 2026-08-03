# CPF 개발자 매뉴얼


## 문서 기준과 판정

- Repository: `https://github.com/freeangelsun/202412_01_CPF`
- Branch: `master`
- Source 기준 Commit: `e134c1f275c306c0e9ab4a044d9140ac4b3ca620`
- 최상위 목표 정본: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
- 문서 표준 정본: `cpf-docs/specification/CPF_DOCUMENTATION_STANDARD.md`
- 사용자 지시 적용: 요구사항에 정의되고 Source에 연결된 기능은 사용 가능한 제품 기능으로 설명한다.
- 이 문서 작업에서 직접 수행한 Runtime·DB·Browser·다중 인스턴스 검증: `미검증`
- 문서와 Source의 경로·식별자 정합성 검토: `완료`

> Runtime 미검증은 기능 절차를 생략하는 이유가 아니다. 다만 실행 결과를 직접 확인하지 않은 항목은 배포 승인 시 해당 환경의 Evidence로 다시 확인한다.


## 1. 개발 시작 전 결정

| 항목 | 예 | 결과 파일 |
|---|---|---|
| Domain ID/System Code | member/MBR | `domain-manifest.json` |
| Profile | `secure-api` | Manifest + Build Dependency |
| Data Provider | JDBC/MyBatis | `resolved-starter-lock.json` |
| Messaging Provider | Kafka/RabbitMQ/JMS/IBM MQ | Binding Config + Lock |
| DB Vendor | Oracle/PostgreSQL/MariaDB | Vendor SQL Pack |
| Security | Resource Server/Session | Permission·Data Scope 정책 |
| 운영 연결 | ADM/BZA/Gateway | OpenAPI·Owner Contract |

## 2. 환경·Build

```powershell
java -version
./gradlew.bat --version
./gradlew.bat clean test assemble --no-daemon --max-workers=1 --stacktrace
```

Java 25와 Gradle 9.1.0을 사용한다. Stack Version은 `gradle/cpf-stack.properties`를 임의 변경하지 않는다.

## 3. Generator로 신규 Domain 생성

1. `cpf-tools/generator/create-domain.ps1`을 실행한다.
2. Domain ID, Package, System Code, Profile, Capability Binding, DB Vendor를 입력한다.
3. Dry Run 결과에서 생성·수정·충돌 파일을 확인한다.
4. Apply 후 `domain-manifest.json`, `resolved-starter-lock.json`, 정책 파일을 검토한다.
5. 생성 Domain을 Fresh Build한다.
6. `CpfGeneratedDomainPolicyRuntimeVerifier`가 Manifest, Lock, 예외 Hash를 확인하는지 Test한다.

기존 Domain Upgrade는 `cpf-tools/generator/upgrade-domain.ps1`을 사용한다. 수동으로 Profile/Leaf Dependency를 섞지 않는다.

## 4. 계층별 구현

| 계층 | 책임 | 금지 |
|---|---|---|
| API | DTO, Validation, HTTP 계약 | Entity/Provider 타입 노출 |
| Application | Use Case, Transaction, 권한·멱등성 | Controller에 업무 규칙 작성 |
| Domain | 상태 전이, 불변식 | Spring/DB 직접 의존 |
| Persistence | CPF Data API/Mapper 구현 | 다른 Domain DB 접근 |
| Integration | CPF Integration/Messaging/File API | OSS Client 직접 사용 |

## 5. 조회 API 개발

1. 검색 Field, 기본값, 최대 Page Size, 정렬 Key를 정의한다.
2. Permission과 Data Scope를 Application 계층에서 적용한다.
3. `CpfDataOperations` 또는 `CpfJdbcOperations`로 Query를 수행한다.
4. Masking된 DTO로 반환한다.
5. Empty, Invalid Cursor, 권한 없음, DB Timeout을 Test한다.
6. ADM에서 Transaction/Trace/Audit를 확인한다.

## 6. 상태 변경 API

요청에는 Resource ID, 목표 상태, `expectedVersion`, `idempotencyKey`, `reason`을 포함한다.

```text
조회 현재 상태 → 권한/상태 전이 검증 → version 조건 Update → Audit 기록 → 응답
```

응답 유실 시 같은 멱등성 Key로 재요청하고 기존 결과를 반환한다. Version Conflict는 최신 상태를 다시 조회한 뒤 사용자가 재판단한다.

## 7. Transaction과 Outbox

업무 Row 변경과 Outbox Insert를 하나의 DB Transaction으로 처리한다. Broker 호출을 Transaction Commit 전에 직접 기다리지 않는다. Worker가 Claim/Lease/Fencing으로 Outbox를 처리한다.

## 8. Messaging 개발

1. 업무 Source는 `CpfBrokerClient`와 CPF DTO만 import한다.
2. `bindingName`, Message Key, Payload Type/Version, Idempotency Key를 설정한다.
3. Provider Binding을 Generator/Config에 선언한다.
4. 정상 Publish와 Consumer Contract Test를 실행한다.
5. Duplicate, Timeout, ACK 유실, DLQ를 재현한다.
6. UNKNOWN은 `CpfBrokerUnknownResultPort`로 대사한다.
7. ADM `/recoveryCenter`, `/incidents`, `/reliability`에서 상태를 확인한다.

Provider 교체는 업무 Source를 수정하지 않고 Binding과 Lock만 바꾼다.

## 9. Notification 개발

`CpfNotificationOperations`에 Template ID, Recipient, Channel, Variables, Idempotency Key를 전달한다. Provider는 `CpfNotificationProvider` SPI를 구현한다. Timeout 후 결과 불명은 `CpfNotificationReconciler`로 확인하고 무조건 재발송하지 않는다.

## 10. 외부 HTTP 연계

Target ID, Method, Path, Timeout Budget, Idempotency, Retry Class를 선언한다. 인증·거래 Header는 CPF가 적용한다. 응답 Code가 명시 실패면 정책에 따라 Retry/DLQ로 전환하고, Connection 종료 등 결과 불명은 Reconcile한다.

## 11. TCP·Fixed-length·ISO8583

- TCP: Framing, Charset, TLS, Correlation, Read/Write Timeout
- Fixed-length: Layout Version, Field Length, Padding, Encoding
- ISO8583: MTI, Bitmap, Field Spec, STAN/RRN, MAC, Reversal

Write 완료 후 Response가 유실되면 동일 전문을 재전송하기 전에 원 거래 조회나 Reversal 정책을 수행한다.

## 12. File·Attachment·Archive·SFTP

1. 파일 Size, Count, Extension, MIME, Checksum을 검증한다.
2. Storage/File Exchange SPI를 통해 저장·전송한다.
3. Archive는 Entry Count, Expanded Size, Path Traversal을 제한한다.
4. SFTP는 Host Key와 Path Allowlist를 검증한다.
5. 전송 Ledger에 Offset, Checksum, Remote Path, Attempt를 남긴다.
6. 중단 시 Resume 또는 새 전송 정책을 적용한다.

## 13. Security·Masking·Audit

- `secure-api`: JWT Issuer/Audience와 Permission 검증
- `browser-bff`: Session, CSRF, Cookie Policy
- Service Identity: 내부 Remote 호출 신원
- Secret: Reference만 Config에 두고 실제 값은 Provider에서 조회
- Audit: Actor, Resource, Before/After, Reason, Approval, Result

## 14. Feature Flag와 Resilience

Feature Flag는 `CpfFeatureFlagOperations`, Resilience는 `CpfResilienceExecutor`를 사용한다. 정책 변경은 ADM `/feature-flags`, `/resilience-policies`에서 Preview·Reason·Approval·Audit를 거친다.

## 15. Local/Remote 전환

Local Facade와 Remote Facade는 같은 DTO·Validation·Error를 제공한다. Remote 전환 시 업무 Source가 아니라 Binding/Endpoint만 변경한다. Timeout Budget, Authentication, Trace Propagation을 Remote 경계에서 추가한다.

## 16. DB Migration

세 Vendor에 Install/Migration/Rollback/Verify SQL을 제공한다. 신규 객체는 Owner와 Consumer를 명시하고, Upgrade 전 Backup Manifest와 Rollback/Forward Recovery를 준비한다.

## 17. OpenAPI·JavaDoc

Public API/SPI와 상태·오류·복구 로직에 JavaDoc을 작성한다. Backend OpenAPI를 생성한 뒤 Frontend Generated Client와 Operation Contract를 갱신한다. 수동 Generated Code 편집은 하지 않는다.

## 18. Test 순서

1. Unit: 상태 전이·Validation
2. Contract: API/SPI·Provider
3. Integration: DB/Broker/File
4. Fault: Timeout·ACK/Response 유실·Process Kill
5. Multi-instance: Lease·Fencing·Rebalance
6. Security: 권한·Masking·Secret Negative Case
7. ADM: 조회·조치·Audit 확인

## 19. EDU 실습

### 실습 A — 멱등 상태 변경

- Source 기준: `cpf-member/**`
- 작업: Version과 Idempotency Key가 있는 상태 변경 API
- 정상: 1회 Update, 동일 Key 재요청은 기존 결과
- 오류: 오래된 Version은 Conflict
- 운영 확인: Transaction, Audit, Idempotency Record

### 실습 B — Provider 교체

- 동일 업무 Source로 Kafka Binding을 RabbitMQ 또는 IBM MQ로 변경한다.
- Lock과 Config만 바뀌고 Java 업무 Class diff가 없어야 한다.
- Contract Test와 Runtime Publish/Consume를 실행한다.

### 실습 C — 응답 유실

- Broker 또는 외부 연계 응답을 Toxiproxy로 차단한다.
- 상태가 UNKNOWN으로 남는지 확인한다.
- Reconcile 후 성공/실패를 확정하고 Audit를 확인한다.

## 20. 운영 인계

Artifact ID/Hash, Profile/Lock, Property/Secret Reference, DB Migration, Health/Metric/Alert, Permission, Fault Test, Retry/Reconcile/rollback 절차와 미검증 범위를 전달한다.
