# CPF QA37 Codex Docker 통합 요청서 사후 리뷰

## Requirement 대조

- Docker 정본 선확인: 반영 완료
- 설치 완료 환경 재설치 금지: 반영 완료
- Vendor별 필요한 Service만 기동: 반영 완료
- CPF 업무 Schema를 Repository Source로 생성: 반영 완료
- Volume 삭제 없는 Fresh Lifecycle: 반영 완료
- Toxiproxy 장애·복구: 반영 완료
- OpenTelemetry 관측성: 반영 완료
- Playwright 3종: 반영 완료
- Trivy·ORT: 반영 완료
- 초기 Running Container 보호: 반영 완료
- Codex 크레딧 절감 실행 순서: 반영 완료
- Source Defect 보완 개발과 최소 재검증: 반영 완료
- Git·Docker 파괴적 작업 금지: 반영 완료

## 변경 영향

- 실제 CPF Source·SQL·Test 변경 없음
- Codex 작업 요청 문서와 검수 보조 문서만 변경
- Docker Compose·Script·Secret·Image·Volume 변경 없음
- Guide·Manual·Deliverable 변경 없음

## 남은 검증

실제 Java, Frontend, DB, Runtime, Browser, Supply-chain 검증은 Codex가 수행해야 한다.
현재 CPF 전체 상태는 `미검증`이다.
