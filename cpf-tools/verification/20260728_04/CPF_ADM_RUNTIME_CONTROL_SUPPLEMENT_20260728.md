# CPF ADM Runtime Control 통합 QA 보완 기준 — 20260728

## 1. 적용 원칙

이 문서는 기존 CPF 통합 QA 요청서를 대체하지 않는다. ADM 실시간 운영·제어 범위를 명확히 하며, 기존 Requirement와 실행 시나리오에 **Root Cause와 실제 Runtime Consumer 기준으로 병합**한다. 동일 기능을 이름만 바꿔 중복 집계하지 않는다.

## 2. 공통 제어 흐름

ADM 조회 → 환경·도메인·시스템·Application Group·Instance 선택 → Desired / Actual / Version / 적용 상태 조회 → 영향 범위 Preview → 권한 확인 → 변경 사유 → 승인 또는 Break Glass → expectedVersion / operationId / request hash 검증 → 대상별 Delivery → Instance별 ACK·실제 적용 결과 → 성공·부분 성공·실패·UNKNOWN_RESULT·RESTART_REQUIRED 구분 → Retry·취소·Rollback·재조정 → Offline 복귀 동기화 → Drift 탐지·복구 → 변경 전후와 운영 결과 불변 감사 기록

## 3. 독립 관리 Capability

1. **공통코드**: 시행 시점, Domain/Group/Instance 대상, Cache Version·ACK, Offline 복귀, 중복·역전 방지, 이전 Version 유지·Rollback.
2. **메시지·응답코드**: 다국어·Mapping 실시간 적용, 실제 API 응답 E2E, 참조 무결성, Preview·차단·Rollback, Applied Version·Drift.
3. **설정·Parameter·Feature Flag**: Global/환경/Domain/Group/Instance 계층, Safe Default·Override, Dynamic/Restart 구분, Type·Schema·보안 검증, TTL 자동 복원.
4. **Cache**: 상태·Version·갱신 시각·항목 수, Full/Incremental/Key Invalidate, Transaction 정합성, Durable Event·Checkpoint·Replay, 유실·중복·역전·Poison 처리, Catch-up·ACK·Backlog·Drift.
5. **온라인 거래**: 거래/그룹/Domain 활성·차단·Drain·Maintenance, 신규 접수와 진행 거래 정책 분리, Timeout·Retry·Circuit·Bulkhead·Rate Limit, Trace Boost TTL, UNKNOWN_RESULT 재조정, Local/Remote 동일 적용.
6. **Service Call·Remote Call**: Local/Remote 동일 계약, Endpoint·Timeout·Retry·Circuit·Load Balance·Failover, Service/Endpoint/Instance/Group/Zone/Cell 대상, HTTP Loopback 금지, 응답 유실·UNKNOWN_RESULT, 실제 Consumer E2E.
7. **Gateway**: Route·Method·Header·Auth·Target·Weight·Priority·Zone/Cell, Timeout·Retry·Circuit·Rate/Payload Limit, Drain 제외, Canary·Wave·Rollback·ACK, Dry Run·Last Good Snapshot, Multipart·Range·Streaming.
8. **로그·추적**: Logger/Package/거래/Domain/Instance 로그 레벨, Sampling·Trace Boost, TTL 복원, Masking 실시간 적용, Desired/Actual/Drift, Instance 로그 목록·다운로드.
9. **Batch·Scheduler·Worker·Center-Cut·Agent**: Schedule/Job/Worker/Runner 제어, Pause/Resume/Cancel/Kill, 재처리, Lock·Fencing, Ghost·Lease·Partition 재배정, UNKNOWN_RESULT 확정, Checkpoint 재시작·중복 방지.
10. **Security Runtime Policy**: Masking·Password·Permission·JWT Key·인증서·Secret Reference, 보안 하향 Override 금지, Rotation·Grace, Fail-Closed, Version·ACK, Secret 원문 비노출.
11. **외부연계·Messaging·File**: 기관 Endpoint·전문·Retry, Broker Consumer·DLQ, File/SFTP/Webhook, 미설치 제품 Simulator·Fake Provider·TestKit, 실제 연동 미실행은 미검증, Interface/YAML만으로 완료 금지.
12. **Instance 관리**: 환경→Cluster→Domain/System→Application Group→Instance, 역할 구분, Host/Port/JVM/Version/Commit/Heartbeat/Lease, 상태·Drift 집계, Rolling Version 혼재, Membership Version·CAS·정책·감사.
13. **Instance 로그 다운로드**: 대상·유형별 목록, Rotation/Archive, 단일/ZIP Streaming, 취소·Timeout·부분 실패, Path Traversal/Symlink/Root 차단, Secret 파일 차단, Masking·임시파일 삭제·권한·사유·승인·감사.
14. **관제·알림**: Event→Outbox→Worker→Provider, 원 거래와 실패 분리, 규칙·Severity·Suppression·Silence·Escalation, Email/SMS/Webhook/ADM, Preview·Test Send·재발송, Mock 성공을 실제 Provider 완료로 판정 금지, 공식 3 DB SQL 호환.

## 4. 완료 추적 체인

Requirement → Public API/SPI → 실제 Runtime Consumer → DB·Migration·Rollback → ADM API·UI·권한 → 정상·오류·경계·부분 실패 → 다중 Instance·재기동·Offline 복귀 → Retry·Recovery·Rollback → Security·Masking·Approval·Audit → EDU·OpenAPI·JavaDoc·Guide → 자동 Test·Fault Test → 최신 master Commit Evidence

DB 저장, Swagger 노출, 현재 JVM 메모리 변경, Mock 성공만으로 완료 처리하지 않는다. 실제 실행하지 않은 항목은 성공으로 기록하지 않는다.
