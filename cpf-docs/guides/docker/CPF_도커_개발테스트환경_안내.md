# CPF 도커 개발·테스트 환경 안내

상위 메뉴: [CPF Docker 가이드](README.md)

이 문서는 CPF를 다른 PC에 동일하게 구축하고, 개발자·QA·자동화 도구가 같은 환경을 반복 사용하는 방법을 안내한다.

## 문서 순서

새 PC 최초 구성:

```text
cpf-docs/guides/docker/CPF_도커_개발테스트환경_전체구축가이드.md
```

설치된 환경 사용:

```text
cpf-docs/guides/docker/CPF_도커_연동및사용가이드.md
```

오류 대응과 데이터 초기화:

```text
cpf-docs/guides/docker/CPF_도커_문제해결및초기화가이드.md
```

구성요소와 버전:

```text
cpf-docs/architecture/CPF_도커_개발테스트환경_구성명세.md
```

## 제공 범위

- Oracle, PostgreSQL, MariaDB
- Redis, Kafka
- Java 25, Node.js 22, PowerShell 7.6.4
- Playwright Chromium·Firefox·WebKit
- Python 3, Git
- MariaDB Client, PostgreSQL Client, SQL*Plus
- Docker CLI와 Compose
- Toxiproxy
- OpenTelemetry Collector
- Trivy
- OSS Review Toolkit
- `curl`, `jq`, `openssl`, `zip`, `unzip`

CPF 업무 Database·Schema·User·Table·Seed·Kafka Topic은 Docker 설치 단계에서 만들지 않는다. Repository Source와 DB Script가 필요한 시점에 생성한다.

## 자동화 도구에 전달할 시작 문장

```text
먼저 cpf-docs/guides/docker/README.md와 cpf-docs/guides/docker/CPF_도커_개발테스트환경_안내.md를 읽어라. 새 PC이면 CPF_도커_개발테스트환경_전체구축가이드.md에 따라 환경을 구성하고, 이미 준비된 PC이면 CPF_도커_연동및사용가이드.md에 따라 필요한 Service와 Tool만 사용해라. CPF 업무 DB·Schema·Seed는 Docker 설치 단계에서 임의 생성하지 말고 Repository Source를 기준으로 생성해라. 오류가 발생하면 CPF_도커_문제해결및초기화가이드.md를 따르고 Docker Image·Runner·Script·Secret·Repository Source와 기존 Working Tree 변경을 삭제하거나 덮어쓰지 마라.
```
