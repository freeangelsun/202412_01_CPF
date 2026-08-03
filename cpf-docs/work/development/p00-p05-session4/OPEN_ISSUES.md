# Open Issues and Validation Transfers

기준 SHA `d2adc89f344fa1f93a2f9291f6576ce69be05239`에서 Source 구현 결함과 환경 검증 이관을 구분한다.

## 개발 미완료

- Canonical 논리 행 1~10,027 각각의 Source·Consumer·SQL·Test·Runtime Evidence를 전부 완료한 상태는 아니다. 이번 Checkpoint가 직접 수정·검증한 자체 Requirement만 완료 처리했다.

## 구현·대체검증 완료 후 이관

1. Java 25 전용 Toolchain·Bytecode·API·JVM Option·Publication.
2. 전체 Gradle Build/Test/Publication/SBOM.
3. MariaDB·PostgreSQL·Oracle 실제 Provision/Install/Seed/Migration/Verify/Rollback/Runtime Query.
4. ADM/BZA 전체 App Dependency Install·Lint·Unit·Build·Generated Client Drift·Playwright Matrix.
5. 전체 적용 Repository의 P02 Owner Boundary와 P03 Transaction Annotation/Legacy ID 전수 Scan.
6. Docker·Registry·Signing·운영 Network·실제 WAS·다중 Instance.

Java 21 Compile/Unit/Harness와 ADM/BZA TypeScript/Node/Chromium Targeted 검증은 이미 PASS이므로 다시 환경 부재 사유로 개발 GPT에 반송하지 않는다. 상세 이관은 `ENVIRONMENT_VALIDATION_HANDOFF.csv`를 따른다.

Release Gate는 Canonical Requirement 전체가 아직 완료되지 않았음을 감지해 의도한 Exit Code 1로 차단한다.
