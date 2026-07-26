# CPF Health / Service Registry 운영 가이드

## 1. 목적
CPF의 Health는 단순 HTTP 200 확인이 아니라 **인스턴스 자체 생존 여부와 트래픽 수신 가능 여부를 분리**하고, 다중 인스턴스 전체 상태는 Service Registry에서 종합하는 것을 표준으로 한다.

## 2. Probe 책임
### Liveness
현재 JVM/프로세스가 살아 있고 이벤트 루프/기본 Runtime이 응답할 수 있는지만 판단한다. 다른 인스턴스나 원격 서비스 전체를 fan-out 조회하지 않는다. 원격 fan-out을 넣으면 장애 시 probe 폭주와 연쇄 장애를 만들 수 있다.

### Readiness
현재 인스턴스가 실제 트래픽을 받아도 되는지를 판단한다. 해당 Runtime의 필수 로컬/Owner dependency만 확인한다. ADM은 admDB/cpfDB/MBR Owner 접근성을 확인하고, BAT는 batDB와 Worker/Runtime listener 상태를 확인한다.

### Service Registry
서비스/인스턴스 목록, 마지막 heartbeat/check 시각, health 상태, circuit 상태를 **인스턴스 단위**로 종합한다. 운영 화면은 Registry를 기준으로 전체 상태를 보여주고 개별 probe의 dependency reason으로 drill-down한다.

## 3. 표준 식별 필드
R14부터 ADM/BAT probe 응답은 가능한 범위에서 다음 값을 함께 제공한다.

| 필드 | 의미 |
|---|---|
| moduleId | CPF 논리 Module/System 식별 |
| wasId | WAS/Runtime 식별 |
| serverInstanceId | 재기동/다중 인스턴스를 구분하는 인스턴스 식별자 |
| hostName | Host 식별 |
| processId | JVM Process ID |
| profiles | 활성 Spring Profile |
| checkedAt | Probe 판정시각 |

`serverInstanceId`는 운영 로그/DB 로그/Service Registry/Health가 서로 연결되는 동일 식별자로 사용해야 한다.

## 4. 운영 권장값
- Liveness 주기: 짧게 유지하되 DB/외부 네트워크 조회 금지.
- Readiness: 필수 dependency만 포함하고 timeout을 짧게 제한.
- Registry health polling: probe와 별도 주기로 수행하며 동일 장애 target에 무제한 동시 요청하지 않는다.
- Down 인스턴스는 신규 라우팅에서 제거하고, 기존 진행 거래의 결과 불명 가능성은 별도 reconciliation으로 관리한다.
- Readiness 실패를 애플리케이션 자동 재시작 사유로 오용하지 않는다. 재시작 판단은 Liveness와 프로세스 정책으로 분리한다.

## 5. 검증 시나리오
1. 정상 인스턴스 2개에서 서로 다른 `serverInstanceId` 확인.
2. ADM DB 중단 시 Liveness=UP, Readiness=DOWN 확인.
3. BAT DB 중단 시 Liveness=UP, Readiness=DOWN 확인.
4. 한 인스턴스만 중단했을 때 Registry에서 해당 인스턴스만 DOWN인지 확인.
5. 복구 후 Registry/라우팅 복귀 시각과 transactionGlobalId 추적 확인.
6. Health endpoint에 Secret, DB URL credential, 개인정보가 노출되지 않는지 확인.

실제 다중 인스턴스 Runtime 결과는 Evidence가 없으면 `미검증`으로 기록한다.
