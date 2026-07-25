# CPF R13 Product Quality Guide

## 목적

R13은 CPF를 새로운 거대 제품으로 확장하는 작업이 아니라 Final Target에 이미 필요한 보안, 동시성, Observability, Fault Verification, Contract Compatibility와 Developer Experience를 상용 Framework 수준으로 닫기 위한 최소 표준을 정의한다.

## Health

- `/health/liveness`: JVM/Process 생존 여부
- `/health/readiness`: 필수 DB/Owner Runtime 준비 여부
- 필수 dependency DOWN은 HTTP 503
- LB/배포 probe는 readiness를 사용한다.

## 로그와 CSV

- 운영 로그 API는 raw DB value를 그대로 반환하지 않는다.
- Masking은 Frontend가 아니라 Backend response의 마지막 경계에서도 적용한다.
- CSV String/Header는 선행 whitespace/control을 제거해 첫 위험 문자 `=`, `+`, `-`, `@`를 검사하고 정책 버전을 Audit에 남긴다.

## MBR 동시성

- 자동 회원번호는 DB 분산 채번을 사용하고 AUTO/MANUAL 발급 이력을 MBR Owner API로 조회할 수 있게 한다.
- 회원/상태/권한 mutation은 `expectedVersion` CAS를 사용한다.
- 권한 grant/revoke는 idempotency key를 사용한다.
- 실제 미존재만 NotFound이고 DB/Schema/SQL 장애는 별도 오류다.

## Cache

- startup/scheduled preload는 같은 Bean의 `@Cacheable` self-invocation에 의존하지 않는다.
- DB snapshot을 먼저 성공적으로 읽은 후 기존 cache를 교체한다.
- mutation cache refresh는 commit 이후 수행한다.
- 전체 목록/개별 값 cache key를 분리한다. Response Code는 `ALL`/`CODE:`를 사용한다.
- 다중 인스턴스 refresh event DB write는 별도 `REQUIRES_NEW` 경계로 기록하고 일시 실패는 bounded retry queue에 보존한다.
- ADM cache summary에서 producer pending/drop/failure와 consumer lastEventId/failure 상태를 확인할 수 있다.

## OpenTelemetry Adapter

CPF 업무 코드는 `CpfTelemetry`만 본다. OTel type은 runtime adapter 내부에 둔다.

```yaml
cpf:
  observability:
    otel:
      enabled: true
      endpoint: http://127.0.0.1:4317
```

현재 R13은 Online Transaction trace foundation이다. Remote/Messaging/Batch/Center-Cut propagation과 metric/log export 완료를 의미하지 않는다.

## Feature Flag

업무 코드는 `CpfFeatureFlags`와 `CpfFeatureFlagContext`를 사용한다. 기본 property provider는 정의되지 않았거나 disabled/parse failure이면 caller safe default를 반환한다.

```yaml
cpf:
  feature-flags:
    example-flag:
      enabled: true
      percentage: 10
      value: true
```

Percentage bucket은 flag key + stable target key의 SHA-256으로 결정하여 같은 대상에 일관된 결과를 낸다. 대형 A/B 플랫폼은 CPF R13 범위가 아니다.

## Fault Injection

기본은 no-op이다. `test`, `verification`, `chaos` profile에서만 controlled provider를 만들 수 있고 별도 enable property와 target allowlist가 필요하다. Production Runtime을 일반 운영 도구처럼 장애 주입하는 기능은 제공하지 않는다.

## Contract Compatibility

`cpf-tools/scripts/check-contract-compatibility.ps1`은 REST/SHARED_API/EVENT/FIXED_LENGTH/FILE/BATCH의 공통 field contract를 비교한다.

Breaking 예:
- required field 제거
- type 변경
- optional → required
- 새로운 required field
- length/scale/encoding/position 변경

이 엔진은 실제 계약 Registry를 대체하지 않는다. 현재는 CI 배포 Gate의 기반이다.

## Generator Golden Path

별도 Developer Portal을 만들지 않는다. `cpf-tools/generator/create-domain.ps1`가 정본이며 DryRun, 충돌검사, ownership manifest, result JSON, public dependency boundary를 유지한다. Portal이 필요해져도 이 Engine 위에 얹어야 한다.
