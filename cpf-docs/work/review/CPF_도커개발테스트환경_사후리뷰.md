# CPF 도커 개발·테스트 환경 확장 보완 사후 리뷰

- 기준 SHA: `1eda8e12fe123281748a4388938c62f11819da1e`
- 검토 대상: Docker Source·Compose·Fixture·Script·Guide·Matrix·Evidence
- Git 쓰기 작업: 없음

## 1. 최초 요구와 구현 대조

| 요구 | 구현 | 상태 |
|---|---|---|
| 빠진 개발 Runtime 전수 판단 | 정본과 기존 Docker 구성 대조 | 완료 |
| MQ 부재 원인 확인 | Kafka가 공식 MQ임을 명시 | 완료 |
| 외부 REST Mock | WireMock 추가 | 완료 |
| 파일 연계 | SFTP Fixture 추가 | 완료 |
| Secret Provider | Vault Dev Fixture 추가 | 완료 |
| OIDC/OAuth2 | Keycloak Local Fixture 추가 | 완료 |
| 계정정보·사용법 | 계정 식별자·Secret 파일 위치 문서화 | 완료 |
| 기존 PC 안전 설치 | 증분 설치 Script | 완료 |
| 전체 사용자 매뉴얼 | Docker Guide 메뉴와 5개 Guide 갱신 | 완료 |
| 실제 신규 Runtime | 사용자 장비 실행 필요 | 미검증 |

## 2. 추적성

- Requirement → Compose: `compose.integration.yml`
- Requirement → Fixture: `fixtures/wiremock`, `fixtures/keycloak`, `cpf-sftp-data` Volume
- Requirement → 설치: `CPF_도커_확장연동환경_증분설치.ps1`
- Requirement → 실행: `cpf-env.ps1`, `cpf-tooling.ps1`, `initialize-integration-fixtures.ps1`
- Requirement → 상태: `verify-complete-environment.ps1`
- Requirement → Guide: `cpf-docs/guides/docker`
- Requirement → Matrix: `CPF_도커개발테스트환경_요건시나리오결과.csv`

## 3. 보호한 성공 기능

- 공식 DB 3종
- Redis·Kafka
- Toxiproxy·OTel Collector
- Trivy·ORT
- 기존 Docker Image·Volume·Secret
- 기존 사용자 Guide·Asset Working Tree
- Restart Policy `no`

## 4. 발견·보정한 결함

1. Kafka가 MQ임을 문서에서 충분히 강조하지 않아 별도 MQ 누락으로 오해할 수 있었음
2. 외부 REST Mock·SFTP·Vault·OIDC Runtime이 없어 최종 Requirement의 실제 연동 검수가 막힘
3. `cpf-env.ps1 -Action up`이 선택 Target 외 Service까지 광역 stop할 수 있었음
4. 계정·Secret 파일 계약과 Fixture 초기화 절차가 부족했음
5. Toxiproxy가 외부연계 Runtime을 Proxy하지 않았음

## 5. 실행한 확인

- YAML·JSON Parse
- SFTP Entrypoint Shell Syntax
- 경로·Manifest·SHA-256 정합성
- Secret 원문·특정 자동화 제품명 Scan
- Root 상대경로 확인

## 6. 실행하지 못한 확인

- PowerShell 실제 Parser
- Docker Compose config
- Image Pull·Build·Create
- WireMock·SFTP·Vault·Keycloak Runtime
- CPF Application 통합

위 항목은 완료로 표시하지 않았다.

## 7. 다음 판정

증분 설치, 상태 확인, Fixture 초기화가 사용자 장비에서 성공하고 실제 CPF Consumer가 연결되어야 신규 확장 Runtime을 `완료`로 변경할 수 있다.

## 전수 Runtime 재판정

- Kafka는 공식 MQ이므로 별도 RabbitMQ·ActiveMQ·IBM MQ를 추가하지 않았다.
- WireMock·SFTP·Vault·Keycloak은 정본 Scenario와 실제 실행 절차가 있어 추가했다.
- External WAS는 정본 목표가 있으나 현재 WAR Packaging Consumer가 없어 Source Gap으로 분리했다.
- 제품만 기동하고 Source 연결이 없는 False Green을 만들지 않았다.
