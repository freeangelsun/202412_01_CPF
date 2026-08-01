# CPF QA37 Codex Docker 통합 요청서 사전 리뷰

## 해결할 Requirement

- Codex가 준비된 Docker 환경으로 실제 DB·Runtime·Browser·Supply-chain을 검증해야 한다.
- CPF Schema가 없는 초기 상태에서 Repository DB Source로 Fresh Install해야 한다.
- 결함 발견 시 검수만 하고 넘기지 않고 영향 범위 보완 개발과 재검증까지 수행해야 한다.
- 반복 Build와 전체 탐색을 줄여 Codex 크레딧을 절감해야 한다.
- Docker 자산과 다른 작업자의 산출물을 보호해야 한다.

## Defect와 위험

- 기존 요청서에 Docker 서비스 선택 기동과 종료 규칙이 부족했다.
- DB Engine 준비 상태와 CPF 업무 Schema 준비 상태를 혼동할 수 있었다.
- 모든 DB를 동시에 시작하면 리소스를 불필요하게 사용한다.
- Fresh 검증을 위해 Volume을 삭제하거나 전체 환경을 초기화할 위험이 있다.
- 장애 검증 전에 Toxiproxy를 켜 정상 실패와 주입 실패를 혼동할 수 있다.
- 종료 시 기존 실행 Container까지 중지할 위험이 있다.
- Source 안정화 전에 Browser·Supply-chain을 반복 실행할 수 있다.

## Owner와 Consumer

- 요청서 Owner: `cpf-docs/work/codex/qa37`
- Consumer: Codex 독립 검수 세션
- Docker 환경 Owner: `C:\dev\Docker\CPF`의 운영 Script와 Repository Docker 문서
- 업무 DB Owner: Repository DB Source와 Vendor Pack
- QA37 EDU Owner: `cpf-reference`
- EDU DB Owner: 중앙 `refDB`

## 구현 순서

1. Docker 정본 확인
2. 최신 원격 SHA 확인
3. 기존 Codex 요청서와 Docker 사용 원칙 통합
4. 선택 기동·초기 Snapshot·종료 복구 설계
5. DB Fresh Lifecycle과 장애·관측성·Supply-chain 단계 연결
6. 최소 재검증 Matrix와 완료 조건 보강
7. Manifest·Hash·Overlay 생성

## 완료 조건

- 독립 실행 가능한 단일 요청서
- 전체 설치 재실행 금지
- CPF Schema는 Repository Source로 생성
- Vendor별 선택 기동
- Toxiproxy·OTel·Trivy·ORT 사용 조건 명확
- 이번 실행에서 시작한 Service만 중지
- Source Defect/Environment Blocker 분리
- Git/Docker 파괴적 명령 0건
