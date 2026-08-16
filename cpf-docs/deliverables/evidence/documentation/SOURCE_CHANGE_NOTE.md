# Source Change Note

Basis: `758757c3206079b990ad7bef2f16c25063540041` (`13_02`)

문서 검수 중 `cpf-education`의 두 예제 Consumer가 `CpfContexts`에 존재하지 않는 Observability 편의 메서드를 호출하는 결함을 확인했습니다. Core 계약을 확장하지 않고 기존 Public Owner API인 `CpfTransactionContext`를 사용하도록 Consumer만 보정했습니다.

## 변경 파일

- `cpf-education/src/main/java/com/cpf/education/data/query/controller/EducationQueryEducationController.java`
- `cpf-education/src/main/java/com/cpf/education/web/header/controller/EducationStandardHeaderEducationController.java`

## 영향 검토

- Core 계약: 변경 없음
- 개발 기능/Architecture 정본: 변경 없음
- QA 판정 원장: 변경 없음
- `cpf-education`의 `:cpf-platform-operations-observability` 기존 dependency 확인
- `CpfTransactionContext.currentTraceId/memberNo/customerNo/channelCode/outboundHeaders` Public 메서드 존재 확인
- 잘못된 `CpfContexts.currentTraceId/memberNo/customerNo/channelCode/outboundHeaders` 호출: 0건
- Compile/Test: Gradle 9.1.0 배포본이 컨테이너에 캐시되어 있지 않고 `services.gradle.org` DNS/download가 막혀 실행 완료하지 못함. **미검증(환경)** 으로 유지
