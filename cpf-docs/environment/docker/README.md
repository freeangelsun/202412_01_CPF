# CPF Docker 개발·시험 환경 문서 지도

> **주 독자**: 개발자, DBA, 메시징·연계·플랫폼 운영자, QA
> **완료 결과**: 현재 설치 Script와 신규 Module 검증 환경을 선택하고 설치·기동·시험·중지·정리 문서를 찾는다.
> **기준 Repository**: `freeangelsun/202412_01_CPF` / `master` / `54bcc10887a83b933685bff462c0b0d7df824923` (`20260802_10`)



## 1. 문서 순서

| 순서 | 문서 | 완료 결과 |
|---|---|---|
| 1 | CPF_도커_개발테스트환경_안내.md | 환경 목표·현재/확장 서비스·보호 범위 결정 |
| 2 | CPF_도커_개발테스트환경_전체구축가이드.md | Base·Toolchain·확장 Fixture Created/Stopped 설치 |
| 3 | CPF_도커_연동및사용가이드.md | 서비스별 선택 기동·초기화·CPF 연결·시험 |
| 4 | CPF_도커_확장연동서비스_사용가이드.md | WireMock·SFTP·Vault·Keycloak과 신규 Provider Fixture |
| 5 | CPF_메시징Provider_도커사용가이드.md | Kafka·RabbitMQ·JMS·IBM MQ 검증 구성 |
| 6 | CPF_도커_문제해결및초기화가이드.md | 부분 실패·Port·Volume·Secret·정확한 Cleanup |

## 2. 실제 설치 Script

```text
cpf-tools/environment/docker-development-test/CPF_도커_개발테스트환경_전체설치.ps1
cpf-tools/environment/docker-development-test/CPF_도커_확장연동환경_증분설치.ps1
cpf-tools/environment/docker-development-test/verify-complete-environment.ps1
```

현재 전체 설치는 MariaDB·PostgreSQL·Oracle·Redis·Kafka, Toxiproxy·OTel Collector·Trivy·ORT와 Java25/Node22/PowerShell/Playwright Toolchain을 준비하고 확장 설치를 호출해 WireMock·SFTP·Vault·Keycloak을 추가한다. Container는 Created/Stopped 상태이며 업무 Schema·Seed·Topic을 임의 생성하지 않는다.

확장 메시징·연계 구성은 RabbitMQ·JMS·IBM MQ·TCP·Notification·Object Storage Provider를 각 Starter의 실제 Runtime Consumer, Compose·Fixture·Probe·Fault Scenario와 함께 구성한다. 문서의 목표 구성은 아래 문서에 명령·정상 결과·실패 처리와 함께 설명한다.

## 3. 공통 안전 원칙

- Docker Root는 `C:\dev\Docker`, Secret은 Repository 밖 `C:\dev\Docker\Secrets`를 기본으로 한다.
- 설치 중 기존 CPF Container는 정지 상태여야 한다.
- Port·Image·Container·Volume 충돌을 먼저 검사한다.
- `docker system prune`, 전체 Volume 삭제, 다른 프로젝트 Container 삭제를 사용하지 않는다.
- 설치 성공과 서비스 Runtime 성공을 분리한다.
