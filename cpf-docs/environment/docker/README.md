# CPF Docker 개발·시험 환경 문서

> **주 독자**: 개발자, 검수자, Docker 환경 운영자
> **기준 Commit**: `dafe5c0e5260ea8149234e8ab2e75347e75338c1`
> **문서 역할**: 공식 사용자 매뉴얼과 분리된 개발·통합시험·장애시험 환경 지원 문서

## 문서 메뉴

| 순서 | 문서 | 사용 시점 |
|---:|---|---|
| 1 | [환경 안내](CPF_도커_개발테스트환경_안내.md) | 범위·지원 상태·보호 정책 확인 |
| 2 | [전체 구축 가이드](CPF_도커_개발테스트환경_전체구축가이드.md) | 새 PC 전체 설치·기존 환경 증분 보완 |
| 3 | [연동 및 사용 가이드](CPF_도커_연동및사용가이드.md) | DB·Cache·Messaging·Tooling 선택 기동 |
| 4 | [확장 연동 서비스](CPF_도커_확장연동서비스_사용가이드.md) | WireMock·SFTP·Vault·Keycloak·Toxiproxy·OTel |
| 5 | [QA38 Messaging Fixture](CPF_QA38_메시징환경_사용가이드.md) | RabbitMQ·JMS(Artemis)·선택 IBM MQ 준비·검증 |
| 6 | [문제 해결 및 초기화](CPF_도커_문제해결및초기화가이드.md) | 오류 진단·중지·대상 한정 초기화 |

## 경로

```text
Repository Source : cpf-tools/environment/docker-development-test/
Runtime Root      : C:\dev\Docker\CPF
Secret Root       : C:\dev\Docker\Secrets
```

## 상태 해석

- Container/Image 준비는 Product Starter 구현을 의미하지 않는다.
- RabbitMQ·JMS·IBM MQ Runtime은 QA38 Provider 개발과 Contract Test를 위한 Fixture다.
- 실제 Product 기능은 Starter·Consumer·Operations·DB·Test·Evidence가 함께 있어야 한다.
- 실행하지 않은 Runtime은 `미검증`이다.

## 기본 보호 정책

- `restart: "no"`
- 설치 종료 시 Running Container 0
- `docker system prune`, Factory Reset, 전체 Image/Volume 삭제 금지
- 사용자 DB·Volume·Secret 임의 초기화 금지
- Secret 원문을 Repository·문서·Evidence·화면 출력에 저장하지 않음
- 정확한 Service·File·Volume만 대상으로 조치


## 전체 검증 환경 목표

이 환경의 목표는 CPF의 현재·신규 모듈을 실제로 설치하고 다음을 같은 기준으로 시험하는 것이다.

- Build Artifact와 Runtime Dependency
- 공식 DB Vendor
- Message Broker·Cache·File·외부 연계·보안·관측
- 정상·오류·부분 실패·Timeout·Process Kill
- Retry·Restart·Reprocess·Reconcile·Rollback
- Multi-instance·Lease·Fencing·Drift
- ADM·Log·Metric·Trace·Audit 확인

신규 Module이나 Provider가 개발되면 Container만 추가하지 않는다. Product Adapter·실제 Consumer·초기화 Fixture·검증 Script·정상화 절차와 함께 추가한다.
