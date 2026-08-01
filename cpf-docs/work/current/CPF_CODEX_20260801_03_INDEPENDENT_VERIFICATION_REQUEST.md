# CPF Codex 20260801_03 독립 검수 요청서

## 기준

- 검수 대상 Branch: `master`
- 시작 SHA: `23a16f35a5633ce1317920468a69fef00c1a6a41`
- 정본: `CPF_FINAL_TARGET_REQUIREMENTS.md` 실제 최신 경로 재확인
- 개발 Backlog: `cpf-docs/quality/CPF_20260801_03_SELF_DEVELOPMENT_BACKLOG.csv`
- 사전 검수: `cpf-docs/work/review/CPF_20260801_03_POST_PUSH_INDEPENDENT_REVIEW.md`

## 검수 범위

Codex는 임의 재설계보다 아래 결함의 실제 해소 여부를 확인한다.

1. Root Build Owner 복구와 전체 Module Configuration
2. Gradle Plugin/BOM 삭제 정합
3. Source/Release OpenAPI Scope 분리
4. Clean Generated Client Lifecycle
5. ADM/BZA Lockfile·npm Policy·CI 정합
6. exact-SHA Evidence와 Manifest
7. EDU 32 Feature/162 Requirement 실제 Source·Test 연결
8. 3 Vendor DB Lifecycle
9. Runtime Fault/Recovery
10. README/Guide 비변경 경계

## 통합 검증 순서

1. Git SHA·Clean Working Tree·`git diff --check`
2. Repository Hygiene·Secret·Ownership·Dependency·Protected Doc Gate
3. Root Gradle Configuration 후 Java 25 `clean test assemble qualityGate`
4. ADM/BZA Clean npm ci → generate → consumer → lint → typecheck → unit → build
5. Playwright Chromium·Firefox·WebKit 각 1회
6. Oracle·PostgreSQL·MariaDB Install→Upgrade→Rollback→Reapply
7. Kafka·Redis·Batch·Gateway·Agent와 다중 인스턴스 Fault/Recovery
8. SBOM·Vulnerability·License·Artifact Hash
9. Matrix·Evidence·Manifest exact-SHA 정합

## 완료 처리 금지

- Root Build가 Module Build 내용인 경우
- Source OpenAPI만으로 Release 완료를 선언한 경우
- 실제 Browser/DB/Runtime 실행 없이 성공으로 기록한 경우
- EDU Source/Test Glob가 해석되지 않은 경우
- Evidence SHA가 검수 SHA와 다른 경우
- README/Guide를 개발 완료 증거로 사용한 경우

환경이 없으면 명령, Runtime/Tool Version, 오류, Exit Code를 기록하고 `미검증`으로 남긴다. Source Defect와 Environment Blocker를 구분한다.
