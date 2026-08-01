# CPF QA37 Codex Docker 통합 요청서 정적 검증 결과

## 기준

- 원격 `master` 확인 SHA: `866b2ff8bbc2a7aaecf91a617b58d79e9a1308a2`
- Docker 문서 확인:
  - `cpf-docs/guides/docker/README.md`
  - `cpf-docs/guides/docker/CPF_도커_개발테스트환경_안내.md`
  - `cpf-docs/guides/docker/CPF_도커_연동및사용가이드.md`
  - `cpf-docs/architecture/CPF_도커_개발테스트환경_구성명세.md`

## 확인한 실제 환경 계약

- Container 7종 명칭
- Volume 5종 명칭
- `restart=no`
- `cpf_default` Network
- DB·Redis·Kafka 선택 기동
- Toxiproxy Proxy Port
- OTLP Endpoint와 Output
- 통합 Runner
- Trivy·ORT Script와 Output
- Repository DB Source Ownership
- 작업 종료 시 Volume 유지

## 수행한 정적 검증

- UTF-8 문서 생성
- PowerShell 경로 문자열 확인
- 비정상 제어문자 검사
- ZIP Root 상대경로 검사
- 파일별 SHA-256 생성
- Delete Manifest에 추적 파일 삭제 0건 확인

## 미실행

- 실제 Local Working Tree 확인
- Docker Container 상태 확인
- Java 25 Build
- Frontend
- DB 3 Vendor
- Kafka·Redis·Batch Runtime
- Toxiproxy
- OpenTelemetry
- Playwright
- Trivy·ORT

위 항목은 Codex가 사용자 PC에서 수행해야 하므로 현재 상태는 `미검증`이다.
