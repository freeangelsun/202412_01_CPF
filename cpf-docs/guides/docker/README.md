# CPF Docker 가이드

CPF Docker 개발·테스트 환경의 사용자 문서 정본은 이 디렉터리에서 관리한다.

## 문서 메뉴

| 순서 | 문서 | 사용 시점 |
|---:|---|---|
| 1 | [개발·테스트 환경 안내](CPF_도커_개발테스트환경_안내.md) | 전체 범위, 설치 정책, 지원·제외 Runtime을 확인할 때 |
| 2 | [전체 구축 가이드](CPF_도커_개발테스트환경_전체구축가이드.md) | 새 PC 전체 설치 또는 기존 PC 증분 보완 시 |
| 3 | [연동 및 사용 가이드](CPF_도커_연동및사용가이드.md) | DB·Cache·Broker·Toolchain을 작업별로 선택 기동할 때 |
| 4 | [확장 연동 서비스 사용 가이드](CPF_도커_확장연동서비스_사용가이드.md) | 외부 REST Mock, SFTP, Vault, OIDC/OAuth2를 사용할 때 |
| 5 | [문제 해결 및 초기화 가이드](CPF_도커_문제해결및초기화가이드.md) | 오류 진단, 안전한 중지, 제한된 데이터 초기화가 필요할 때 |

## 메시징 표준

CPF의 공식 Message Broker는 **Kafka**다. Kafka가 CPF의 MQ 역할을 수행한다. RabbitMQ, ActiveMQ, IBM MQ, JMS Broker는 현재 정본과 Source의 공식 Primary가 아니므로 기본 Docker 환경에 설치하지 않는다. 향후 실제 Adapter·Consumer·Requirement가 승인될 때 별도 선택 Runtime으로 추가한다.

## 관련 정본

- [Docker 개발·테스트 환경 구성 명세](../../architecture/CPF_도커_개발테스트환경_구성명세.md)
- `cpf-tools/environment/docker-development-test/`
- `cpf-docs/quality/CPF_도커개발테스트환경_요건시나리오결과.csv`
- `cpf-docs/work/current/CPF_도커개발테스트환경_완료보고.md`
- `cpf-docs/evidence/도커개발테스트환경/`

## 기본 운영 원칙

- Docker Desktop이 실행되어도 CPF Container는 자동 시작하지 않는다.
- 모든 CPF Container의 Restart Policy는 `no`다.
- 필요한 Service만 명시적으로 시작하고 시작한 Service만 중지한다.
- Image·Runner·Volume·Secret·Repository Source를 광역 삭제하지 않는다.
- CPF 업무 DB·Schema·User·Seed·Kafka Topic은 Repository Source와 Script를 기준으로 생성한다.
- 계정 비밀번호와 Token은 `C:\dev\Docker\Secrets`에만 저장하고 문서·Git·Evidence·화면 출력에 남기지 않는다.
- 상태 확인만으로 Runtime 기능 완료를 판정하지 않고 실제 연결·오류·복구 시나리오를 실행한다.
