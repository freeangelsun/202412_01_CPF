# CPF 도커 개발·테스트 환경 인수인계

- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- 기준 SHA: `1eda8e12fe123281748a4388938c62f11819da1e`

## 시작 문서

```text
cpf-docs/guides/docker/README.md
```

## 설치 Script

새 PC:

```text
cpf-tools/environment/docker-development-test/CPF_도커_개발테스트환경_전체설치.ps1
```

기존 Base 환경:

```text
cpf-tools/environment/docker-development-test/CPF_도커_확장연동환경_증분설치.ps1
```

Fixture 초기화:

```text
C:\dev\Docker\CPF\initialize-integration-fixtures.ps1
```

## Runtime Root

```text
C:\dev\Docker\CPF
C:\dev\Docker\Secrets
```

## 핵심 정책

- Kafka가 공식 MQ/Broker다.
- RabbitMQ·ActiveMQ·IBM MQ를 기본 설치하지 않는다.
- 공식 DB는 Oracle·PostgreSQL·MariaDB다.
- 모든 Container는 `restart: no`다.
- 계정명만 문서화하고 비밀번호·Token은 Secret 파일로 관리한다.
- 기존 Image·Container·Volume·Secret을 광역 삭제하지 않는다.
- 신규 확장 Runtime은 아직 사용자 장비 실행 Evidence가 필요하다.
- External WAS는 WAR Packaging·Servlet Initializer Source가 구현되기 전까지 Source Gap이며 빈 Tomcat 기동으로 완료 처리하지 않는다.
