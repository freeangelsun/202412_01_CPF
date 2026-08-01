# CPF QA37 Codex Review Index
## Docker 통합 검수·보완 개발·완료판

## 기준

- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- 문서 작성 시 원격 SHA: `866b2ff8bbc2a7aaecf91a617b58d79e9a1308a2`
- 실제 검수 SHA: 시작 시 `HEAD == origin/master` exact SHA
- 상태: `미검증`
- Docker Runtime: `C:\dev\Docker\CPF`
- Secret: `C:\dev\Docker\Secrets`

## 최초 읽기

1. Docker README
2. Docker 환경 안내
3. Docker 연동 및 사용 가이드
4. Docker 구성 명세
5. CPF 최상위 정본
6. 현재 작업 요청서
7. QA37 Source/Verification Readiness
8. QA37 Result/Coverage Matrix
9. Codex 독립 검수·보완 개발·완료 요청서

## 단일 실행 순서

1. Git Read-only Baseline
2. Docker Read-only Snapshot
3. 저비용 Gate
4. Java 25 Fresh Lifecycle
5. Optional Pack 제거 Compile
6. MariaDB Fresh Lifecycle
7. PostgreSQL Fresh Lifecycle
8. Oracle Fresh Lifecycle
9. Kafka·Redis·Batch Runtime
10. Toxiproxy Fault·Recovery
11. OpenTelemetry
12. ADM/BZA
13. Playwright 3 Browser
14. Trivy·ORT
15. exact-SHA Evidence
16. 이번 실행에서 시작한 Service만 중지

Stage 실패 시 후속 대형 Stage를 실행하지 않는다.
최초 Root Cause를 수정하고 영향 범위 검증 후 상위 Lifecycle을 마지막에 한 번만 실행한다.

## 절대 금지

- 전체 설치 Script 재실행
- 모든 DB와 Service 동시 상시 기동
- Container Restart Policy 변경
- Image·Container·Volume·Runner·Secret 삭제
- Docker prune·초기화
- Git 추적 파일 자동 삭제·복구
- Commit·Push
