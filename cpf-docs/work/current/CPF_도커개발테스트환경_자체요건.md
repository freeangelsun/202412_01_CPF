# CPF 도커 개발·테스트 환경 자체 요건

## 완료 대상으로 반영한 항목

- 공식 DB 3종
- Redis
- Kafka 공식 MQ/Broker
- WireMock 외부 REST Fixture
- SFTP 파일 연계 Fixture
- Vault Dev Secret Fixture
- Keycloak Local Identity Fixture
- DB·Cache·Broker·외부연계 Toxiproxy
- OpenTelemetry Collector
- Java·Node·PowerShell·Python·Git·DB Client·Playwright 통합 Runner
- OpenSSH Client·SFTP 실제 송수신 확인
- Trivy·OSS Review Toolkit
- 새 PC 전체 설치와 기존 PC 증분 설치 분리
- Repository 밖 Secret 파일 관리
- 사용·계정·장애·초기화 Guide
- Sanitized 실행 결과 계약

## 의도적으로 제외한 항목

- RabbitMQ·ActiveMQ·IBM MQ·JMS Broker: Kafka가 공식 Primary
- MySQL·MSSQL·CPF용 H2: 공식 DB 아님
- MinIO·S3·Ceph: 공식 Provider·Consumer 미확정
- Prometheus·Grafana: 운영 Stack 확정 전 강제하지 않음
- Nexus·Artifactory: 조직별 외부 설비
- ClamAV: 실제 Scanner Adapter·Consumer 미확정
- External WAS·Tomcat: 정본 목표는 있으나 WAR Packaging·Servlet Initializer Consumer가 없어 Source Gap
- Nginx 등 독립 Web Server: 공식 제품·배포 계약 미확정
- Kubernetes·kind·k3d·Helm: 실제 Manifest·실행 Consumer 미확정
- SMTP·LDAP: 실제 공식 Adapter·Consumer 미확정

## 환경 설치로 가릴 수 없는 Source Gap

- External WAS WAR 산출과 실제 Tomcat 배포 Consumer
- 독립 Frontend Artifact의 공식 Web Server 배포 계약
- 실제 Application Container/Image와 다중 인스턴스 실행 묶음

빈 Runtime 제품만 설치해 위 항목을 완료 처리하지 않는다. Source·Build·Deploy 연결이 구현된 뒤 같은 작업 단위에서 환경을 추가한다.

## 남은 환경 의존

- 사용자 Windows PC의 실제 Image Pull과 Container Create
- Registry·Proxy·사설 인증서 정책
- Oracle Registry 접근
- CPU·Memory·Disk 여유
- 실제 CPF Application·SQL·Migration·Client 연결
- 다중 인스턴스·Process Kill·Unknown Result·Recovery 실행

위 항목은 Runtime Evidence가 없으면 `미검증`으로 유지한다.
