# CPF Docker 개발·시험 환경 문서

> **주 독자**: 개발자, 검수자, Docker 환경 운영자
> **기준 Commit**: `3b600702502e53877e30cbac594987b371e2186b` (`20260802_08`)
> **문서 역할**: CPF의 현재·신규 모듈을 실제 Runtime과 연결해 정상·오류·부분 실패·재시작·대사를 검증하는 환경 문서

## 문서 메뉴

| 순서 | 문서 | 사용 시점 |
|---:|---|---|
| 1 | [환경 안내](CPF_도커_개발테스트환경_안내.md) | 목표·현재 서비스·미편입 범위·보호 정책 확인 |
| 2 | [전체 구축 가이드](CPF_도커_개발테스트환경_전체구축가이드.md) | 새 PC 설치·기존 환경 증분 보완 |
| 3 | [연동 및 사용 가이드](CPF_도커_연동및사용가이드.md) | DB·Redis·Kafka·통합 Fixture 선택 기동 |
| 4 | [확장 연동 서비스](CPF_도커_확장연동서비스_사용가이드.md) | WireMock·SFTP·Vault·Keycloak·Toxiproxy·OTel 사용 |
| 5 | [QA38 Messaging 편입 가이드](CPF_QA38_메시징환경_사용가이드.md) | RabbitMQ·JMS·IBM MQ 개발 후 Docker 편입 기준 |
| 6 | [문제 해결 및 초기화](CPF_도커_문제해결및초기화가이드.md) | 오류 진단·정상화·대상 한정 초기화 |

## 경로

```text
Repository Source : cpf-tools/environment/docker-development-test/
Runtime Root      : C:\dev\Docker\CPF
Secret Root       : C:\dev\Docker\Secrets
Repository Root   : C:\dev\projects\jck\202412_01_CPF
```

명령은 현재 폴더에 의존하지 않고 위 경로를 변수로 지정한다.

## 현재 Source에서 확인된 Runtime

```text
MariaDB / PostgreSQL / Oracle
Redis / Kafka
WireMock / SFTP / Vault / Keycloak
Toxiproxy / OpenTelemetry Collector
```

RabbitMQ·Jakarta JMS·IBM MQ·TCP Simulator·Notification Provider는 최신 `settings.gradle`에 정식 Starter가 등록되지 않았으므로 현재 설치 완료 범위로 표시하지 않는다.

## 상태 해석

- Image·Container 준비는 CPF Product 기능 구현을 의미하지 않는다.
- Product 기능은 Starter·Adapter·실제 Consumer·Config·DB·Operations·Test가 함께 있어야 한다.
- Container Health는 연결 가능성이고, 업무 성공은 Product Test와 대사로 판정한다.
- 실행하지 않은 Runtime은 `미검증`이다.
- 다른 Commit에서 실행한 Evidence를 최신 Commit의 성공으로 승계하지 않는다.

## 기본 보호 정책

- `restart: "no"`
- 설치 종료 시 Running CPF Container 0
- 필요한 Service만 시작하고 이번 작업에서 시작한 Service만 중지
- `docker system prune`, `docker volume prune`, Docker Factory Reset 금지
- 사용자 DB·Volume·Image·Secret 임의 초기화 금지
- Secret 원문을 Repository·문서·Evidence·화면 출력에 저장하지 않음
- 정확한 Service·File·Volume만 대상으로 조치

## 전체 검증 환경 목표

신규 Module·Starter·Provider 편입은 다음 묶음으로 수행한다.

```text
Product Source·Build
→ Starter·Adapter·실제 Consumer
→ Compose Service·Image·License
→ Secret·Network·Port·Volume
→ 초기화 Fixture
→ 정상 Contract Test
→ Timeout·Network Loss·Process Kill·부분 실패
→ Retry·Restart·Reprocess·Reconcile·Rollback
→ ADM·Log·Metric·Trace·Audit
→ StopAfter·데이터 보존
```

Container만 먼저 추가한 상태는 `부분 구현` 또는 `미검증`으로 기록한다.
