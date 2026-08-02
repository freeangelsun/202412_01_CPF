# CPF 도커 개발·테스트 환경 확장 보완 완료 보고

- 기준 SHA: `1eda8e12fe123281748a4388938c62f11819da1e`
- 작업 성격: Docker 환경 Source·Script·Fixture·Guide 보완
- Git Commit·Push: 수행하지 않음
- Secret 원문: 포함하지 않음
- 기존 Docker 자산 삭제: 없음

## 주요 변경

- WireMock, SFTP, Vault, Keycloak 확장 Compose 추가
- 기존 PC 전용 증분 설치 Script 추가
- 새 PC 전체 설치 Script에 확장 설치 연결
- SFTP 제한 계정 Image와 실제 업로드·다운로드 확인 절차 추가
- Keycloak Realm·Test User·Service Client 초기화 절차 추가
- Vault KV 연결 확인 추가
- Toxiproxy 외부연계 Proxy 4개 추가
- 통합 Runner에 OpenSSH Client·`sshpass` 추가
- `cpf-env.ps1`을 선택 Group만 시작·중지하도록 보정
- Docker 사용자 문서와 Architecture 명세 전면 갱신
- Kafka가 공식 MQ이며 비정본 Broker를 설치하지 않는 근거 명시
- 전체 Runtime 재판정과 External WAS Source Packaging Gap 구분

## 정적 확인 결과

- Compose YAML Parse: 완료
- Fixture JSON Parse: 완료
- Toxiproxy JSON Parse: 완료
- Keycloak Realm JSON Parse: 완료
- SFTP Entrypoint `bash -n`: 완료
- Secret 원문 Pattern Scan: 완료
- 절대경로는 사용자 실행 경로만 사용, Repository ZIP은 Root 상대경로
- 광역 Docker 삭제 명령: 문서상 금지 예시 외 실행 Script에 없음

## Runtime 상태

기존 Base 환경은 사용자 장비에서 다음 상태가 확인된 이력이 있다.

```text
Required Images: 13/13
Prepared Containers: 7/7
Running Containers: 0
Prepared Volumes: 5/5
```

Overlay 적용 후 증분 설치의 목표 상태는 필수 Image 18개, Container 11개 Created/Stopped, Volume 7개, Secret File 7개다.

이번에 추가한 WireMock·SFTP·Vault·Keycloak의 실제 Pull·Build·Create·연결은 현재 실행 환경에서 수행하지 못했다. 사용자 장비에서 증분 설치와 Fixture 초기화 Script를 실행한 결과가 있어야 Runtime 완료로 판정한다.

## 완료 상태

| 영역 | 상태 |
|---|---|
| Source·Compose·Script | 완료 |
| Guide·Architecture·Matrix | 완료 |
| 정적 Syntax·Format Gate | 완료 |
| 기존 Base Runtime | 완료 이력 |
| 신규 확장 Runtime | 미검증 |
| CPF Application 통합 Runtime | 미검증 |
