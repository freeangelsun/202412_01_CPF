# CPF Public API / Generated Domain Guide

## 1. 목적

Generated Domain과 고객 업무 모듈은 `com.cpf.core.common.*` 구현 package를 직접 import하지 않는다. 업무 코드가 사용할 수 있는 정본은 다음 두 경계다.

- `com.cpf.core.api.*`: 안정적인 개발자 Public API/DTO/Annotation/Facade
- `com.cpf.core.spi.*`: 고객·업무 모듈이 구현하는 확장 Port

`com.cpf.core.common.*`은 CPF Runtime 내부 구현이며 Generator 산출물의 계약이 아니다.

## 2. Golden Domain 공개 경계

| 기능 | 업무 코드가 사용하는 API | Runtime 구현 |
|---|---|---|
| Base | `api.base.*` | Domain 내부 service/controller |
| Online/Shared 실행 | `api.execution.CpfOnlineTransaction` | Logging AOP/Execution Catalog |
| Batch metadata | `api.execution.CpfBatchJob` | `cpf-batch` |
| HTTP service call | `api.http.CpfHttpClient` | `CpfWebClient` + ServiceCallEngine |
| SQL/DataSource | `api.database.CpfSqlResources`, `CpfDataSources` | 중앙 Vendor Pack resolver |
| Transaction Context | `api.logging.CpfTransactionContext` | Core Context/Filter/AOP |
| Broker | `api.broker.CpfBrokerClient` | Transactional Outbox adapter |
| File | `api.filetransfer.CpfFileTransferClient` | file transfer engine/history/reconciliation |
| Center-Cut | `api.centercut.*`, `spi.centercut.*` | `cpf-batch` Runner/transport |
| Masking | `api.security.CpfMasking` | Core masking policy |
| Paging | `api.page.*`, `api.util.CpfPages` | Repository/Mapper |

## 3. AOP 자동 합류

Generator가 만든 Controller/Service가 `@CpfOnlineTransaction`을 사용하면 Core `LoggingAspect`와 `CpfExecutionCatalogScanner`가 Public Annotation을 직접 읽는다. 따라서 신규 Domain이 Core 내부 Annotation을 알아야 할 이유가 없다.

Generated Domain의 Runtime 조건:

1. `cpf-core` dependency가 존재한다.
2. CPF AutoConfiguration이 활성화된다.
3. 표준 Header Filter/Logging Runtime이 구성된다.
4. Controller method에 Public execution metadata를 선언한다.
5. 내부전용 호출은 `visibility="INTERNAL"`, `gatewayAllowed=false`로 명시한다.

## 4. Local/Remote 호출

업무 Service는 `CpfHttpClient` 또는 typed Local/Remote Facade를 사용한다. Gateway는 외부 진입 경계이며 내부 Domain 간 호출의 재경유 경로가 아니다. ServiceCall Runtime이 endpoint/instance health, retry, circuit, transaction header propagation을 적용한다.

## 5. Messaging

Generated Domain은 Kafka/Rabbit/Outbox 구현을 직접 호출하지 않고 `CpfBrokerClient.enqueue(CpfBrokerPublishRequest)`를 사용한다. Runtime에 `CpfBrokerOutboxPort`가 있으면 Public Boundary AutoConfiguration이 adapter를 자동 연결한다.

메시지 ID와 idempotency key를 서로 다른 의미로 유지한다. publish timeout/결과불명은 성공으로 추정하지 않는다.

## 6. File Transfer

업무 코드는 `CpfFileEndpoint`, `CpfFileRequest`, `CpfFileTransferClient`만 사용한다. Credential은 원문이 아닌 `CpfCredentialReference`로 전달한다. 내부 Runtime은 duplicate/history/reconciliation을 담당한다.

## 7. Center-Cut

`center-cut` capability를 Generator에 주면 Domain Handler와 내부 endpoint가 생성된다.

```powershell
pwsh .\cpf-tools\generator\create-domain.ps1 `
  -DomainName loan -SystemCode LON `
  -Capabilities 'database,remote-call,center-cut' -Apply
```

BAT는 `CenterCutTargetProvider`/`CenterCutHandler` SPI를 사용한다. Remote 호출의 `UNKNOWN_RESULT`는 `FAILED`나 `RETRY_REQUESTED`로 자동 확정하지 않는다. 운영 재조정/수동확정 정책이 별도로 판단한다.

## 8. Architecture Gate

적용 후 반드시 실행한다.

```powershell
pwsh .\cpf-tools\scripts\check-r11-public-boundary.ps1
```

이 Gate는 Golden Generator의 `com.cpf.core.common.*` 참조와 Generated Business Module의 internal import를 fail-closed한다.
