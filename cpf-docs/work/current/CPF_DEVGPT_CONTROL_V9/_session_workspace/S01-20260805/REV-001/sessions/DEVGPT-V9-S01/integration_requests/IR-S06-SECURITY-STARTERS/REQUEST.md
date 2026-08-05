# IR-S06-SECURITY-STARTERS — Security Starter 제품 경로 적용

- Parent request: `DEVGPT-V9-S01`
- Integration owner: `DEVGPT-V9-S06`
- Baseline SHA: `fc207ac5560da59f352ee0c5f83199177f2987b4`
- Status: `미완료 / 재확인 필요`

## Proposed product changes included

- Resource Server: issuer/audience/timestamp validator와 configurable clock skew, fail-fast property validation.
- Secret Registry: empty/duplicate/unknown provider fail-fast, canonical provider ID.
- Service Identity: replay fingerprint consume, secure distributed default, local mode explicit opt-in, bounded in-memory guard, health visibility.
- Regression: standalone harnesses와 기존 JUnit test의 explicit SINGLE_INSTANCE fixture 보정.

제안 파일은 `proposed_overlay/`에 Root 상대경로로 포함되어 있다. S06은 기존 Starter/BOM/AutoConfiguration/metadata와 충돌 여부를 확인하고 제품 경로에 적용해야 한다.

## Required verification

1. 원래 Gradle/JUnit test를 Java 25 기준으로 실행한다.
2. Spring Boot ApplicationContext에서 default `DISTRIBUTED`가 local guard로 조용히 기동하지 않고 fail-fast인지 확인한다.
3. 실제 distributed replay Provider가 있을 때 정상 기동·재생 차단을 검증한다.
4. Resource Server issuer/JWK mode, audience case sensitivity, clock skew boundary를 검증한다.
5. Secret Provider 0개/중복/미등록 경로가 시작 또는 요청 시 fail-closed인지 검증한다.
6. exact SHA와 원 Consumer 회귀 Evidence를 `impacted_ids.csv`에 연결한다.

제안 Harness는 Java 21 대체환경에서 Exit 0이지만, 타 Owner 제품 적용·Push 전에는 완료가 아니다.
