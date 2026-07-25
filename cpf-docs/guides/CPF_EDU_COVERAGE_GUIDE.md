# CPF EDU Coverage Guide

## 1. 원칙
CPF EDU는 별도 장난감 규격을 만들지 않는다. Generated Domain과 운영 Runtime이 사용하는 `com.cpf.core.api.*` / `com.cpf.core.spi.*` 계약을 그대로 사용한다. 예제에서 `com.cpf.core.common.*`를 직접 import하지 않는다.

## 2. Reference EDU
| 주제 | 대표 위치 | 확인/보강 기준 |
|---|---|---|
| Foundation API / Paging | `cpf-reference/.../utility/controller/RefFoundationApiEduController.java` | `CpfPage`, `CpfPages`, Public execution annotation |
| 자료형/Map/List/JSON | `RefDataTransformEduController.java` | `CpfValues`, `CpfMaps`, `CpfJson` |
| Fixed-Length | `cpf-reference/.../telegram/ReferenceFixedLengthBusinessUseEducationSample.java` | Layout + 전문→Map/JSON, Map/JSON→전문 |
| Batch | `cpf-reference/.../batch` | BAT Owner `CpfBatchOperationsPort` 사용. Core legacy runtime 직접 호출 금지 |
| Center-Cut | `cpf-reference/.../centercut` | Public API/SPI Handler/Provider + BAT가 호출하는 internal item endpoint |

오류/경계 예제는 null/invalid boolean, fixed-length layout 불일치, batch adapter 미구성, center-cut FAILED/UNKNOWN_RESULT를 포함해야 한다.

## 3. BAT EDU / Runtime
`cpf-batch`는 독립 Boot application이며 `job/smoke`, `job/failure`, `job/heartbeat`, `job/centercut`과 실제 Runner/Worker/Scheduler를 교육 대상으로 사용한다. 업무 Domain은 BAT Runtime 클래스를 복사하지 않는다.

Center-Cut 표준 흐름:
1. BAT가 target을 claim/mark running한다.
2. transactionId는 전체 흐름에서 승계하고 item마다 segment ID를 분리한다.
3. 동일 JVM은 Handler SPI를 직접 호출할 수 있다.
4. 분리 WAS는 ServiceCall registry/health/failover를 통해 Domain internal endpoint를 호출한다.
5. 응답 유실 등 결과를 확정할 수 없으면 `UNKNOWN_RESULT`로 남긴다.
6. ADM은 BAT Owner operations API를 통해 조회/통제한다.

## 4. Generated Domain Golden Template
Generator capability `center-cut`은 Public `CenterCutHandler`와 internal item endpoint를 생성한다. `database`, `messaging`, `file`, `external`, `batch` 등 다른 capability도 공개 API/SPI만 소비해야 한다.

## 5. Gate
적용 후 다음 명령으로 EDU/Public 경계를 확인한다.

```powershell
pwsh -File .\cpf-tools\scripts\check-r11-public-boundary.ps1
pwsh -File .\cpf-tools\scripts\check-r11-runtime-entrypoints.ps1
```

실제 Spring/DB/Browser 동작은 통합 검증에서 실행 로그와 기준 SHA를 Evidence로 남긴다.
