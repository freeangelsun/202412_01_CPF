# CPF 도커 개발·테스트 환경 독립 점검 요청서

- 기준 SHA: `1eda8e12fe123281748a4388938c62f11819da1e`
- 정본: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
- 환경 Guide 시작점: `cpf-docs/guides/docker/README.md`
- 검수 범위: Docker 환경 Source·Fixture·Guide와 실제 CPF Runtime 연결
- 기존 성공 Gate와 전체 Build를 동일 SHA에서 반복하지 않음

## 1. 사전 확인

1. `HEAD`, `origin/master`, Working Tree
2. 사용자 소유 Guide·Asset 변경 보호
3. Docker Desktop Linux/amd64
4. `C:\dev\Docker\Secrets` 파일 존재만 확인하고 값 출력 금지
5. 기존 7개 Base Container·Image·Volume 삭제 금지

## 2. 설치 경로

기존 Base 환경이 있으면 전체 설치를 반복하지 말고 다음 증분 Script만 실행한다.

```powershell
pwsh -NoProfile -ExecutionPolicy Bypass -File ".\cpf-tools\environment\docker-development-test\CPF_도커_확장연동환경_증분설치.ps1" -RepoRoot (Get-Location).Path
```

설치 후 다음을 한 번 실행한다.

```powershell
pwsh -NoProfile -File "C:\dev\Docker\CPF\verify-complete-environment.ps1" -RequireStopped
```

## 3. 필수 Runtime 확인

다음은 Image·Container 목록 확인만으로 완료 처리하지 않는다.

- Oracle·PostgreSQL·MariaDB: Install·Seed·Runtime Query·Upgrade·Rollback·Reapply
- Redis: TTL·Eviction·Lock·Invalidation·Outage·Recovery
- Kafka: ACK·Transaction·Ordering·Duplicate·Rebalance·Retry/DLT·Broker Outage
- WireMock: 성공·503·지연·Connection Reset과 CPF Client 분류
- SFTP: 실제 업로드·다운로드·Checksum·ACK/NACK·재처리
- Vault: Provider 연결·원문 비노출·Rotation/Revocation 실패 분류
- Keycloak: OIDC Discovery·Login·Role·Service Client·Revocation
- Toxiproxy: DB·Kafka·외부연계 latency·disconnect·reset
- OTel: Trace·Metric·Log·식별자 연결·Masking
- Browser: ADM/BZA와 실제 Backend·Identity·DB 연동
- External WAS: 현재 WAR Packaging·Servlet Initializer가 없으면 환경 차단으로 돌리지 말고 Source Gap으로 기록

기본 Fixture 준비:

```powershell
pwsh -NoProfile -File "C:\dev\Docker\CPF\initialize-integration-fixtures.ps1" -StopAfter
```

## 4. Evidence

각 실행 결과에 다음을 기록한다.

- exact SHA
- 명령·Profile·환경
- 시작·종료 시각·Exit Code
- Container·Image Digest
- Requirement·Scenario
- 실제 요청·응답·DB Query·Topic·File 결과
- 오류 단계·재시도·복구 결과
- Artifact SHA-256
- Working Tree 상태
- Secret·Token·PII 제거 여부
- 종료 후 Running Container

## 5. 완료 금지 조건

- 신규 확장 Runtime을 실행하지 않음
- Kafka 대신 비정본 Broker를 사용함
- DB 3종 중 실제 SQL Lifecycle 누락
- 외부연계가 Port Open 확인으로 끝남
- Secret 원문이 로그·Evidence에 남음
- 실행 실패를 환경 문제로 단정하고 Source 결함을 구분하지 않음
- 성공한 Gate를 반복해 실행량만 늘림
- 빈 Tomcat·Web Server Container 기동만으로 External WAS·독립 Frontend 배포를 완료 처리함

실행하지 못한 항목은 `미검증`, `실패`, `환경 차단`, `재확인 필요` 중 하나로 기록한다.
