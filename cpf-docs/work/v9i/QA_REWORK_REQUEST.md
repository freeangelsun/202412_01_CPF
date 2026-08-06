# QA Rework Request

검토 기준 Commit: `1b35d84801e256e3e6d7e4482918817ec82865dd`

## QA-RW-01 — Spring Context wiring

- 대상: `cpf-admin` 및 신규 Crypto·Time·Data Quality·Webhook 구현
- 결함 근거: `AdmIntegrationClosureService`와 신규 SPI 구현의 Runtime Bean 등록이 확인되지 않음
- 기존 검증 한계: framework stub 기반 `javac` 성공만 존재
- 재실행:
  - `gradlew :cpf-admin:test`
  - 실제 ApplicationContext 시작 Test
  - Controller Bean 생성과 세 SPI 주입 확인
- 성공 기대: Missing Bean 없이 Context 시작, 운영 API 호출 성공
- 실패 기준: `NoSuchBeanDefinitionException`, Controller 생성 실패, 기능 Bean 미선택
- 요구 Evidence: 명령, Exit Code, Context Test 결과, Bean 목록

## QA-RW-02 — 서버 측 승인

- 대상: `/adm/api/integration-closure/data-quality/quarantine/{id}/correct`
- 결함 근거: 요청 Boolean `approved`를 승인 근거로 사용
- 수정 요구: 서버에서 검증한 승인 ID·승인 상태·권한·감사 Event를 사용
- 성공 기대: 클라이언트가 `approved=true`만 보내서는 조치 불가
- 실패 기준: 승인 기록 없이 정정 성공
- 요구 Evidence: 권한·승인 정상/거부/만료/중복 요청 Test

## QA-RW-03 — Target Runtime

- Java 25 / Gradle 9.1 전체 Build·Test·Publication
- 실제 3개 DB Vendor Runtime
- Browser·Playwright Release
- Broker·Multi-process·Process Kill
- PASS로 기록하기 전에 실제 실행 Evidence 필요

## QA-RW-04 — 원장 관리 정합성

- Integration Request Closure 완료율을 `30/32 = 93.75%`로 수정
- 헤더 전용 `REQUIREMENT_STATUS.csv`와 실제 분할 Dataset 관계를 명시
- QA 기준 Commit `1b35d84801e256e3e6d7e4482918817ec82865dd`를 문서에 추가
- 긴 중복 Workspace 삭제 후 짧은 Root만 정본으로 유지
