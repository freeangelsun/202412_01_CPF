# CPF 도커 문제 해결 및 초기화 가이드

## 1. 광역 삭제 금지

다음 명령은 사용하지 않는다.

```text
docker system prune -a
docker image prune -a
docker volume prune
docker builder prune -a
```

## 2. 읽기 전용 상태 확인

```powershell
docker ps -a --format "table {{.Names}}\t{{.Image}}\t{{.Status}}\t{{.Ports}}"; docker volume ls; docker image ls
```

## 3. Secret Access Denied

```powershell
[void][System.IO.File]::ReadAllBytes("C:\dev\Docker\Secrets\cpf-runtime.env"); [void][System.IO.File]::ReadAllBytes("C:\dev\Docker\Secrets\redis-password.txt")
```

ACL을 변경하기 전에 현재 사용자와 Docker Desktop이 실제로 읽을 수 있는지 확인한다. Secret 원문은 출력하지 않는다.

## 4. Port 충돌

```powershell
Get-NetTCPConnection -State Listen | Where-Object LocalPort -in 3306,5432,1521,6379,9092,8474,13306,15432,11521,16379,19093,4317,4318,8888 | Format-Table LocalAddress,LocalPort,OwningProcess
```

## 5. Tooling 로그

```powershell
pwsh -NoProfile -File "C:\dev\Docker\CPF\cpf-tooling.ps1" -Action logs -Target tools
```

## 6. Toxiproxy 상태

```powershell
Invoke-RestMethod "http://127.0.0.1:8474/proxies"
```

## 7. 데이터 초기화

Base DB·Redis·Kafka 데이터만 새로 만들 때:

```powershell
pwsh -NoProfile -File "C:\dev\Docker\CPF\reset-test-data.ps1" -ConfirmReset
```

그다음:

```powershell
pwsh -NoProfile -File "C:\dev\Docker\CPF\cpf-env.ps1" -Action prepare
```

Toxiproxy와 OpenTelemetry Collector는 데이터 Volume을 사용하지 않는다. Output 폴더는 자동 삭제하지 않는다.

## 8. 절대 보호 대상

```text
Docker Base Image
CPF Runner Image
Toxiproxy·OpenTelemetry·Trivy·ORT Image
C:\dev\Docker\CPF
C:\dev\Docker\Secrets
CPF Repository
Git Working Tree
CPF 외 Docker 자산
```

## 9. 다른 PC로 옮길 것

```text
Root Overlay
cpf-tools/environment/docker-development-test
CPF Repository
문서
```

옮기지 않을 것:

```text
Secret 원문
DB Volume
개인 Output
회사별 Token·인증서
```
