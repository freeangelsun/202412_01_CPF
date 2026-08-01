# CPF Docker 가이드

CPF Docker 개발·테스트 환경의 가이드 정본은 이 디렉터리에서 관리한다.

## 문서 메뉴

| 순서 | 문서 | 사용 시점 |
|---:|---|---|
| 1 | [개발·테스트 환경 안내](CPF_도커_개발테스트환경_안내.md) | 환경의 전체 범위와 기본 원칙을 처음 확인할 때 |
| 2 | [전체 구축 가이드](CPF_도커_개발테스트환경_전체구축가이드.md) | 새 PC에 환경을 처음 설치할 때 |
| 3 | [연동 및 사용 가이드](CPF_도커_연동및사용가이드.md) | 프로그램·CPF Module별 용도와 선택 기동 방법을 확인할 때 |
| 4 | [문제 해결 및 초기화 가이드](CPF_도커_문제해결및초기화가이드.md) | 오류 진단, 안전한 중지, 데이터 초기화가 필요할 때 |

## 관련 정본

- [Docker 개발·테스트 환경 구성 명세](../../architecture/CPF_도커_개발테스트환경_구성명세.md)
- `cpf-tools/environment/docker-development-test/`
- `cpf-docs/quality/CPF_도커개발테스트환경_요건시나리오결과.csv`
- `cpf-docs/work/current/CPF_도커개발테스트환경_완료보고.md`
- `cpf-docs/evidence/도커개발테스트환경/`

## 기본 운영 원칙

- Docker Desktop이 실행되어도 CPF Container는 자동 시작하지 않는다.
- CPF Container의 Restart Policy는 `no`를 유지한다.
- 작업에 필요한 Service만 명시적으로 시작한다.
- Docker Image·Runner·Volume·Secret은 명시적 승인 없이 삭제하지 않는다.
- CPF 업무 Database·Schema·User·Seed·Kafka Topic은 Repository Source와 Script를 기준으로 생성한다.
