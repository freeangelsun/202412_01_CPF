# CPF Current Work Request

## 1. 기준

- Repository: `https://github.com/freeangelsun/202412_01_CPF`
- Branch: `master`
- Overlay 개발 기준 SHA: `fae7aa9643f646db4bcbcf665d13b8f3b809e8c8`
- 최우선 정본: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
- 구현 Overlay: `CPF_20260730_FULL_IMPLEMENTATION_FINAL_OVERLAY.zip`
- 추적 기준: Requirement 405건, Scenario 90건

## 2. 현재 해야 할 일

Source·SQL·Frontend·Test·Gate 개발 Overlay는 작성되었다. 현재 작업은 다음 순서로만 수행한다.

1. 최신 `master`가 Overlay 기준 SHA와 동일한지 확인한다.
2. Root Overlay를 적용하고 `cpf-tools/scripts/apply-cpf-20260730-final-overlay.ps1`을 실행한다.
3. 변경 Source를 검토한 뒤 사용자가 Commit한다.
4. Clean Working Tree의 Source SHA에서 `verify-cpf-20260730-full-implementation.ps1 -RecordEvidence`를 실행한다.
5. MariaDB 기존 DB Upgrade/Rollback/Reapply와 별도 Clean Install을 실행한다.
6. PostgreSQL·Oracle, Redis·다중 인스턴스, Browser E2E가 가능한 환경에서 남은 Runtime Scenario를 실행한다.
7. 495개 통합 Matrix와 2,715개 Verification Ledger를 실제 Evidence 경로로 폐쇄한다.
8. 최신 Push SHA에서 Codex 독립 검수를 수행한다.

## 3. 완료 판정 금지 조건

다음 중 하나라도 남으면 CPF 전체 제품 완료로 판정하지 않는다.

- 전체 Gradle `clean test assemble` 미실행 또는 실패
- ADM/BZA Typecheck·Lint·Vitest·Production Build 미실행 또는 실패
- 3개 공식 DB Vendor Lifecycle 미실행 또는 실패
- Redis·다중 인스턴스·Gateway·Batch·Browser 핵심 Scenario 미실행 또는 실패
- Evidence의 Source SHA 불일치
- Matrix 또는 Ledger에 `미검증`, `실패`, `재확인 필요`가 존재
- Working Tree가 Dirty이거나 Local/Remote SHA가 다름

## 4. 변경 금지 원칙

- 사용자 승인 없이 Commit, Push, Branch, Tag, PR을 생성하지 않는다.
- 실행하지 않은 검증을 성공으로 기록하지 않는다.
- 실패를 Skip 또는 정상 0건으로 바꾸지 않는다.
- Generated Domain을 고정 업무 Module로 되돌리지 않는다.
- Oracle, PostgreSQL, MariaDB 외 Vendor를 공식 지원에 다시 포함하지 않는다.
