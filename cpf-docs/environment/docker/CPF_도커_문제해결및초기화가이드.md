# CPF Docker 문제 해결 및 초기화 가이드

> **주 독자**: 개발·QA·플랫폼 운영자
> **완료 결과**: Port·Image·Container·Volume·Secret·DB·Broker·Network 문제를 보호 범위 안에서 진단·복구한다.
> **기준 Repository**: `freeangelsun/202412_01_CPF` / `master` / `54bcc10887a83b933685bff462c0b0d7df824923` (`20260802_10`)

<!-- CPF-TOC:START -->
## 전체 목차

- [1. 진단 순서](#1-진단-순서)
- [2. 자주 발생하는 문제](#2-자주-발생하는-문제)
- [3. 상태 확인 한 줄](#3-상태-확인-한-줄)
- [4. 서비스 재생성 원칙](#4-서비스-재생성-원칙)
- [5. Test Data 초기화](#5-test-data-초기화)
- [6. Cleanup 체크리스트](#6-cleanup-체크리스트)
- [7. 장애별 상세 Runbook](#7-장애별-상세-runbook)
  - [7.1 Container 반복 종료](#71-container-반복-종료)
  - [7.2 Port 점유](#72-port-점유)
  - [7.3 Volume Data 불일치](#73-volume-data-불일치)
  - [7.4 Secret 권한/값 오류](#74-secret-권한값-오류)
  - [7.5 Network Fault 종료 후 장애 지속](#75-network-fault-종료-후-장애-지속)

<!-- CPF-TOC:END -->

## 1. 진단 순서

1. Docker Version·Backend·Architecture를 확인한다.
2. Compose Config와 Env/Secret 파일 존재를 확인한다.
3. Container 상태·Health·Exit Code·최근 Log를 확인한다.
4. Port·Network·DNS·Certificate·Disk·Memory를 확인한다.
5. DB/Broker/Fixture 자체 Health와 CPF Readiness를 분리한다.
6. 최근 Image/Config/Source SHA와 Lock을 비교한다.
7. 결과가 생겼을 수 있는 업무 Operation을 재실행 전에 대사한다.
8. 정확한 서비스만 재시작·재생성하고 정상 결과를 확인한다.

## 2. 자주 발생하는 문제

| 증상 | 원인 후보 | 안전한 조치 |
|---|---|---|
| Container Created/Stopped | 설치 정상 상태 | 필요 서비스만 `up -d` |
| Port 충돌 | 다른 Process/Project | 소유 Process 확인·Port 계획 수정 |
| Unhealthy | 의존/Secret/Schema/Memory | Health Log와 의존 순서 수정 |
| Image Pull 실패 | Network/Registry/Disk | 원인 수정 후 같은 설치 재실행 |
| Compose 변수 없음 | env/tool-images 누락 | 전체 설치/증분 설치로 재생성 |
| DB Migration Lock | 다른 Migration/중단 | Owner 확인·History/Lock 대사 |
| Kafka/Rabbit Lag | Consumer Down/Poison/용량 | Consumer·DLQ·Backpressure 확인 |
| SFTP Partial | Network/Disk/Checksum | Ledger·Offset·.part 확인 후 Resume |
| Vault/Keycloak 인증 실패 | Token/Role/Audience/Clock | Secret·Policy·시간 동기화 |
| Toxiproxy 후 미복구 | Proxy Toxic 잔존 | 시나리오 Toxic만 제거 |
| Disk Full | Image/Volume/Log/Test Output | 소유 경로별 보존 확인 후 정확히 정리 |

## 3. 상태 확인 한 줄

```powershell
$dockerRoot='C:\dev\Docker'; $cpf=Join-Path $dockerRoot 'CPF'; docker ps -a --filter 'name=cpf-' --format 'table {{.Names}}\t{{.Status}}\t{{.Ports}}'; if(Test-Path (Join-Path $cpf 'image-lock-complete.json')){Get-Content -LiteralPath (Join-Path $cpf 'image-lock-complete.json') -Raw}
```

## 4. 서비스 재생성 원칙

- Stateless Fixture는 Config/Secret/Fixture Backup을 확인하고 해당 서비스만 `rm` 후 `create`한다.
- DB/Broker/Volume Service는 Backup·Migration/Offset·업무 결과를 확인하기 전 Volume을 삭제하지 않는다.
- Image 교체는 Lock과 Version을 갱신하고 동일 시나리오를 재검증한다.
- `docker system prune`, `docker volume prune`, 다른 Project Network 삭제를 금지한다.

## 5. Test Data 초기화

`reset-test-data.ps1` 또는 시나리오 전용 초기화 Script가 소유하는 Schema/Topic/Directory만 초기화한다. 전체 DB Drop·Volume 삭제 대신 업무 Prefix·Fixture Manifest·Operation ID 범위를 사용한다. 초기화 전후 건수·Hash를 기록한다.

## 6. Cleanup 체크리스트

- [ ] 실행 중 CPF Application·Worker가 없다.
- [ ] Backup/Restore가 필요한 DB·Broker·File Data를 보존했다.
- [ ] 삭제 대상 Container·Volume·Network·Output의 소유자를 확인했다.
- [ ] Secret 폐기/보존 정책을 확인했다.
- [ ] 다른 Docker Project·Repository Source·전체 Cache를 건드리지 않는다.
- [ ] Cleanup 후 `verify-complete-environment.ps1 -RequireStopped`를 실행한다.

## 7. 장애별 상세 Runbook

### 7.1 Container 반복 종료
1. Exit Code·OOMKilled·Health·최근 Log를 확인한다.
2. Image/Config/Secret/Volume 권한을 비교한다.
3. 의존 서비스 Readiness와 Startup 순서를 확인한다.
4. 같은 오류로 무제한 Restart하지 않는다.
5. 수정 후 해당 Container만 `create/up`하고 Smoke를 실행한다.

### 7.2 Port 점유
1. Host Port 소유 Process/Container를 확인한다.
2. 다른 프로젝트를 중지하지 않고 CPF Port 계획 또는 해당 소유자와 조정한다.
3. Compose와 Application Config를 함께 변경한다.
4. Loopback Binding과 Firewall을 확인한다.

### 7.3 Volume Data 불일치
1. Image Version·Schema Version·Volume 생성 시점을 확인한다.
2. Migration/Backup/Restore 가능 여부를 확인한다.
3. Volume 삭제로 우회하지 않고 Upgrade 또는 격리 Restore를 수행한다.
4. Test Data만 정확한 Reset Script로 초기화한다.

### 7.4 Secret 권한/값 오류
1. 파일 존재·빈 값·ACL·줄바꿈을 Secret 원문 출력 없이 확인한다.
2. Consumer 계정 Read 권한과 Container Mount를 확인한다.
3. Rotation 중이면 신규/이전 Version 공존을 확인한다.
4. 유출 가능성이 있으면 즉시 Version 폐기·재발급한다.

### 7.5 Network Fault 종료 후 장애 지속
1. Toxiproxy Proxy와 Toxic 목록을 확인한다.
2. 시나리오가 만든 Toxic만 제거한다.
3. DNS/Connection Pool/Circuit Half-open을 확인한다.
4. Application Operation·Broker/DB 상태를 대사한다.
