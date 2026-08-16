# CPF Local Runtime Compose

`docker-compose.local.yml`은 개발/검증용 선택형 Infrastructure 자산이다. Repository Root가 아니라 `deploy/local`이 소유한다.

사용 예:

```powershell
$env:CPF_MARIADB_ROOT_PASSWORD="<local-secret>"
docker compose -f .\deploy\local\docker-compose.local.yml --profile db up -d
```

- Secret은 환경변수로 전달하며 파일에 하드코딩하지 않는다.
- 운영 배포 정의로 사용하지 않는다.
- MariaDB/Kafka/Redis의 실제 운영 보안·HA 설정은 환경별 배포 자산에서 별도 관리한다.
