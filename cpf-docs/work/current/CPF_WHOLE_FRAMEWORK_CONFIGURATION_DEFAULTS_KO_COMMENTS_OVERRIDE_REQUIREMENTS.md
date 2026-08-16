# CPF Whole-Framework Configuration Defaults / Korean Comments / Override Requirement

> Currentization review baseline: `92169d9918dd176e8322ac2f9dfc29ebe1d2ea12`; 실제 실행 시 최신 Git HEAD와 Runtime Evidence를 사용한다.
> 목적: CPF 전체를 설정값이 존재하는 수준에서 **찾기 쉽고, 안전한 기본 실행이 가능하며, 환경별 배포와 Source 확장이 가능한 상용 Framework**로 완결한다.

## 1. 적용 범위
core, starters, common, data, integration, messaging, file, notification,
security, platform-operations, web, profiles, gateway, batch, admin, biz-admin,
member, external, education, generator, tools/environment, deploy.

## 2. Local First, Production Safe
- local/test: 필요한 local host는 `127.0.0.1`.
- stable local port.
- real secret/sample credential repository 저장 금지.
- external institution은 simulator/stub를 명시 선택한 경우에만 loopback.
- prod: localhost/example/sample fallback 0, required binding 누락 fail-fast.

## 3. Environment Files
실행 Application은 역할에 맞게:
`application.yml`, `application-local.yml`, `application-test.yml`,
`application-dev.yml`, `application-stg.yml`, `application-prod.yml`
을 사용한다. 공통/환경별 설정을 중복 없이 정규화한다.

## 4. 한글 Comment Standard
주요 option은 가능하면 아래 Metadata를 한글로 제공한다.

`[역할] [기본값] [허용값] [단위] [적용범위] [우선순위] [변경] [보안] [운영주의] [실패조건] [관련기능]`

주석 처리된 option도 실제 key/default/validation과 일치해야 한다.

## 5. 주요 옵션
host/port/url/destination/path, timeout/retry/backoff, circuit/rate/bulkhead,
pool/thread/concurrency/queue, TTL/lease/checkpoint, page/chunk,
request/file/message size, TLS/mTLS/cert/trust, auth/session/secret,
health/drain, logging/trace/metric, schema/version/codec, feature flags,
error/recovery/reconcile, provider/binding selection을 반드시 검토한다.

## 6. Source Override Policy
각 property는 `CONFIG_LOCKED`, `SOURCE_DEFAULT`, `SOURCE_CUSTOMIZABLE`,
`PER_CALL_BOUNDED`, `RUNTIME_MANAGED` 또는 동등한 정책을 가진다.
Source 확장은 Typed Customizer/Builder/Options/SPI로 제공한다.
Production endpoint/credential/TLS/auth/secret은 Source에서 임의 우회할 수 없다.

## 7. Hardcoded Value Audit
전체 Source/Config/Deploy/Generator에서 URL/IP/port/timeout/retry/pool/thread/TTL/path/env/JVM option을 검색한다.
환경 종속 hardcoding은 Config/Binding으로 이동한다.

## 8. Gateway
Gateway는 listen/control bind/port, CORS/forwarded header, trusted proxy,
route/registry/routing, timeout/retry/circuit/rate/bulkhead,
auth/mTLS/TLS, request limit, TxId/context/idempotency,
error mapping, health/drain/maintenance, version/zone/weight/canary,
control-plane/audit/secret masking을 환경별 profile/한글주석/typed validation/runtime consumer와 맞춘다.

## 9. Batch
Scheduler/Agent/Worker/Center-Cut의 bind/port, polling, concurrency/thread/queue,
chunk/page, lease/heartbeat, checkpoint, retry/skip, businessDate, shutdown/drain,
DB/lock/control-plane 주요 설정을 같은 기준으로 정리한다.

## 10. Admin / BZA
backend endpoint, auth/session, paging/refresh interval, dangerous operation limits,
realtime/SSE, upload/download, masked display, capability availability 설정을
Backend/OpenAPI/Frontend Consumer와 맞춘다.

## 11. Common Product Service
Code/Message/Parameter/Calendar/Template의 cpfDB role, cache/refresh, locale,
business date/timezone, template storage/version/invalidation을 문서/JavaDoc/EDU와 맞춘다.

## 12. Test / Gate
- major config Korean comment coverage.
- profile coverage.
- prod localhost/sample fallback 0.
- local port collision 0.
- YAML comment/default/code default parity.
- IDE metadata parity.
- source override policy coverage.
- hardcoded environment value scan.
- selected/unselected config zero-footprint.
- Generator regenerated sample parity.
- Gateway/Batch/Admin/External actual consumer.
- exact-SHA evidence.


## 13. Call/Result 개발자 설정 노출

Domain/External/Messaging/Batch 주요 Call Option은 환경파일에 한글 설명을 제공한다.

- timeout/deadline
- retry/backoff
- idempotency
- reconcile
- remote-in-local-tx policy
- result/unknown handling
- log/masking
- route/version/zone hint

사용하지 않는 주요 option도 commented example로 검색 가능하게 한다.
