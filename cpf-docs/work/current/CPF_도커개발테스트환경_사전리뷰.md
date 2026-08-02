# CPF 도커 개발·테스트 환경 확장 보완 사전 리뷰

- 기준 SHA: `1eda8e12fe123281748a4388938c62f11819da1e`
- 기준 정본: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
- 기존 Docker 상태: DB 3종, Redis, Kafka, Toxiproxy, OTel Collector 준비 완료
- Git 쓰기 작업: 없음
- 기존 Image·Container·Volume·Secret 삭제: 없음

## 1. 해결할 Requirement와 Gap

| Gap | 근거 | 처리 방향 |
|---|---|---|
| 외부 REST Mock Runtime 없음 | Gateway·외부연계 실제 Mock Evidence 요구 | WireMock 추가 |
| SFTP Runtime 없음 | 외부 파일 연계 Requirement | 제한된 SFTP Fixture 추가 |
| 외부 Secret Provider Runtime 없음 | Vault/file/env 연계 Requirement | Vault Dev Fixture 추가 |
| OIDC/OAuth2 Identity Runtime 없음 | 운영자·Service 인증 Requirement | Keycloak Local Fixture 추가 |
| 외부연계 장애 Proxy 없음 | timeout·disconnect·unknown·recovery | Toxiproxy Proxy 4개 추가 |
| 계정·Secret 사용 기준 부족 | Credential 원문 금지 | Repository 밖 Secret 파일과 계정 식별자 문서화 |
| MQ 오해 가능성 | Kafka Primary 정책 | Kafka가 공식 MQ임을 명시하고 비정본 Broker 제외 |
| External WAS 오판 가능성 | WAR 목표는 있으나 현재 Packaging Consumer 없음 | 빈 Tomcat 설치 대신 Source Gap으로 분리 |

## 2. Owner와 경계

- 환경 Source Owner: `cpf-tools/environment/docker-development-test`
- 사용자 Guide Owner: `cpf-docs/guides/docker`
- Architecture 명세: `cpf-docs/architecture`
- CPF 업무 Schema·User·Seed·Kafka Topic: 기존 Source Owner가 관리
- 확장 Service는 테스트 Fixture이며 `cpf-core` 또는 업무 Module에 Dependency를 강제하지 않음

## 3. Consumer

- WireMock: Gateway·Generated External REST Adapter·Resilience Starter
- SFTP: Generated External File Adapter·Batch File Executor
- Vault: Secret Starter와 Security Adapter
- Keycloak: Security Starter·ADM/BZA·Gateway 인증 경계
- Kafka: Event·Batch Remote Runtime의 공식 Broker

## 4. 회귀 위험과 보호 대상

- 기존 7개 Container와 5개 Volume 보존
- 기존 Secret 값 보존
- 기존 Guide·Asset 사용자 변경 보호
- `restart: no` 유지
- `cpf-env.ps1`의 광역 stop 동작을 제거하고 선택 Group만 중지
- RabbitMQ 등 대체 Broker 임의 추가 금지
- Source 연결 없는 빈 Runtime 제품으로 False Green 생성 금지

## 5. 구현 순서

1. Requirement·Source·Docker 구성 대조
2. 확장 Compose·Fixture·Secret 설계
3. 기존 PC 증분 설치 Script
4. 새 PC 전체 설치 연계
5. 선택 기동·장애주입·초기화 Script
6. 사용자·설치·연동·문제해결 Guide
7. JSON·YAML·Shell·Secret·Manifest 정적 Gate
8. 사용자 장비 Runtime 실행은 별도 Evidence로 수집

## 6. 완료 조건

- Source·Compose·Script·Guide·Matrix가 동일 서비스 목록을 사용
- 설치는 기존 자산을 삭제하지 않음
- Secret 원문 0건
- RabbitMQ·ActiveMQ·IBM MQ 제외 근거 명시
- Runtime 미실행 항목을 완료로 기록하지 않음
- Root 상대경로 ZIP Overlay 제공
