# CPF 도커 문제 해결 및 초기화 가이드

상위 메뉴: [CPF Docker 가이드](README.md)

## 1. 우선 확인

```powershell
pwsh -NoProfile -File "C:\dev\Docker\CPF\cpf-env.ps1" -Action status
```

```powershell
pwsh -NoProfile -File "C:\dev\Docker\CPF\verify-complete-environment.ps1"
```

문제가 있는 Service만 로그를 확인한다.

```powershell
pwsh -NoProfile -File "C:\dev\Docker\CPF\cpf-env.ps1" -Action logs -Target external
```

## 2. 광역 정리 금지

다음 명령은 사용하지 않는다.

```text
docker system prune
docker volume prune
Docker Desktop Factory Reset
전체 Image·Container·Volume 삭제
```

문제 대상 하나를 식별하고 해당 Service·Volume·Fixture만 명시적으로 처리한다. Repository Source, Dockerfile, Compose, Script, Secret과 다른 작업자의 Working Tree는 삭제하지 않는다.

## 3. Port 충돌

| Service | Port |
|---|---:|
| MariaDB | 3306 |
| PostgreSQL | 5432 |
| Oracle | 1521 |
| Redis | 6379 |
| Kafka | 9092 |
| WireMock | 18080 |
| SFTP | 2222 |
| Vault | 8200 |
| Keycloak | 18081 |
| Toxiproxy | 8474 |
| OTel gRPC·HTTP | 4317·4318 |

Windows에서 대상 Port 소유 Process를 확인한 뒤 충돌 원인을 해결한다. 다른 프로그램을 근거 없이 종료하지 않는다.

## 4. Secret 오류

값을 화면에 출력하지 않고 파일 존재·길이만 확인한다.

```powershell
Get-ChildItem "C:\dev\Docker\Secrets" | Select-Object Name,Length,LastWriteTime
```

필수 파일:

```text
cpf-runtime.env
redis-password.txt
sftp-password.txt
vault-token.txt
keycloak-admin-password.txt
keycloak-test-password.txt
keycloak-service-client-secret.txt
```

Secret 파일이 없으면 증분 설치 Script를 다시 실행해 누락 파일만 생성한다. 기존 Secret은 덮어쓰지 않는다.

## 5. Container가 자동 시작되는 경우

```powershell
docker inspect --format "{{.Name}} {{.HostConfig.RestartPolicy.Name}}" cpf-mariadb cpf-postgresql cpf-oracle cpf-redis cpf-kafka cpf-wiremock cpf-sftp cpf-vault cpf-keycloak cpf-toxiproxy cpf-otel-collector
```

모두 `no`여야 한다. 상태 확인 Script가 불일치를 실패로 처리한다.

## 6. WireMock

Health:

```powershell
Invoke-RestMethod -Uri "http://127.0.0.1:18080/cpf-test/health"
```

Mapping 오류는 다음 Runtime 경로를 확인한다.

```text
C:\dev\Docker\CPF\fixtures\wiremock\mappings
```

Repository Fixture를 수정한 뒤 증분 설치 Script를 실행하면 WireMock·Keycloak 정본 Fixture만 갱신한다. SFTP 교환 파일은 덮어쓰지 않는다.

## 7. SFTP

Container Log:

```powershell
docker logs --tail 200 cpf-sftp
```

Chroot Root는 root 소유여야 하며 실제 쓰기 디렉터리는 `/exchange` 아래다. 비밀번호를 로그에 전달하지 않는다. 실제 연결은 `initialize-integration-fixtures.ps1`로 확인한다.

## 8. Vault

Health:

```powershell
Invoke-WebRequest -UseBasicParsing -Uri "http://127.0.0.1:8200/v1/sys/health"
```

Vault는 Dev Fixture이므로 Container 중지·재생성 시 KV Data가 사라질 수 있다. 운영 영속성 결함으로 오판하지 않는다. 연결·인증·Rotation·Failure 분류용이다.

## 9. Keycloak

OIDC Discovery:

```powershell
Invoke-RestMethod -Uri "http://127.0.0.1:18081/realms/cpf-test/.well-known/openid-configuration"
```

Realm import 이후 테스트 User·Service Client가 없으면 초기화 Script를 실행한다.

```powershell
pwsh -NoProfile -File "C:\dev\Docker\CPF\initialize-integration-fixtures.ps1"
```

Keycloak 내부 개발 저장소를 CPF의 H2 지원 근거로 사용하지 않는다.

## 10. Toxiproxy 장애가 남은 경우

```powershell
pwsh -NoProfile -File "C:\dev\Docker\CPF\cpf-tooling.ps1" -Action reset-faults
```

Toxiproxy 자체가 정지된 경우 먼저 `tools` 또는 대상 `fault-*` Group을 시작한다.

## 11. 제한된 Base 데이터 초기화

Base DB·Redis·Kafka 테스트 데이터를 초기화할 때만 정확한 Script를 사용한다.

```powershell
pwsh -NoProfile -File "C:\dev\Docker\CPF\reset-test-data.ps1" -ConfirmReset
```

이 Script는 명시된 Base Container·Volume만 대상으로 하며 Image·Secret·Repository Source를 보존한다. Keycloak Volume과 SFTP Fixture Data는 포함하지 않는다.

## 12. Keycloak Fixture 초기화

Keycloak Fixture만 완전히 다시 만들 필요가 있으면 다음 대상을 먼저 확인하고 별도 승인을 거쳐 처리한다.

```text
Container: cpf-keycloak
Volume: cpf-keycloak-data
```

다른 DB·Volume과 함께 삭제하지 않는다. 삭제 후 `cpf-env.ps1 -Action prepare`와 초기화 Script를 실행한다.

## 13. SFTP 교환 파일 정리

SFTP 교환 데이터는 `cpf-sftp-data` Docker Volume에 저장한다. Windows Bind Directory를 사용하지 않아 Chroot 소유권과 권한을 안정적으로 유지한다.

정리 전 확인 대상:

```text
Container: cpf-sftp
Volume: cpf-sftp-data
경로: /home/cpf-sftp/exchange/{inbound,outbound,ack,error,archive}
```

Container를 시작한 뒤 다음 명령으로 파일명과 크기만 확인한다.

```powershell
docker exec cpf-sftp sh -ec "ls -laR /home/cpf-sftp/exchange"
```

정리가 필요해도 Volume 전체나 다른 Service Data를 함께 삭제하지 않는다. 제거할 생성 파일 목록을 먼저 확정하고 승인받은 뒤 해당 경로만 처리한다. Repository Source·Script·Dockerfile과 Secret은 삭제 대상이 아니다.

## 14. 설치 재실행 기준

- 전체 설치: 새 PC 또는 Base 환경이 없는 경우만
- 증분 설치: 확장 Service 누락, Runtime 파일 갱신, Secret 파일 누락 시
- 특정 Image Pull 실패: 해당 Image만 재시도
- Build 실패: 최초 실패 Root Cause를 수정하고 해당 Image만 다시 Build

성공한 전체 설치를 반복하거나 정상 Image·Volume을 삭제해서 재시작하지 않는다.
