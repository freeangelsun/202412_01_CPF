# CPF QA39 TEST_AND_EVIDENCE

## 기준

- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- 적용 기준 SHA: `4aea798c913787e86341809e2cef2b9495cbf7ba`
- 최종 전달 형식: Repository Root 상대경로 Overlay ZIP
- 최종 로컬 명령의 용도: 정확한 Delete Manifest 정리만 수행
- Build·Test·Lint·Gradle·Node·Docker·Git 검증 명령: 사용자에게 제공하지 않음

## 개발 작업환경에서 수행한 검증

| 검증 | 결과 | 근거 |
|---|---|---|
| `Qa39Tool.java` Java 17 호환 컴파일 및 `-Xlint:all -Werror` | PASS | 개발 작업환경 실행 |
| 기준 HEAD에 이미 존재한 `com.cpf.core.common.broker` 계약의 Naming Gate 오탐 제거 | PASS | `CPF_QA39_FINAL_NAMING_GATE_REPAIR.sanitized.json` |
| 신규 `common.notification` 및 `common.broker` 위반 동시 검출 | PASS, 2건 일괄 검출 | 동일 Evidence |
| Canonical 저비용 Gate | PASS, 7개 | 개발 Fixture 실행 |
| R8 부분 적용 상태의 Notification·Security `src/main`, `src/test` 복구 | PASS | R8/R9 Fixture |
| Public/Internal BOM의 Gradle `platform(...)` 함수 충돌 교정 | PASS | 사용자 R8 Gradle 설정 검증 결과 |
| Gradle Settings/Configuration | PASS | 사용자 제공 R8 통합 보고서 |
| Canonical Layout | PASS | 사용자 제공 R8 통합 보고서 |
| `git diff --check` | PASS | 사용자 제공 R8 통합 보고서 및 최종 Overlay staged check |
| PowerShell | 기존 QA39 PowerShell Parser PASS; 최종 Cleanup은 수동 정적 검토 | 개발 환경에 `pwsh`가 없어 최종 Cleanup 실행 검증은 미수행 |
| Delete Manifest 경로 안전성 | PASS | 와일드카드 0, `..` 0, 보호 경로 0 |
| ZIP 파일 수·SHA-256 Manifest | PASS | 최종 ZIP 재압축 후 전수 검사 |
| Python 전달 파일 | PASS, 0개 | 최종 ZIP Inventory |

## 수행하지 않은 검증

다음은 성공으로 기록하지 않는다.

- 전체 Java `clean test assemble check`
- ADM/BZA Frontend `npm ci`, Lint, Typecheck, Unit, Build
- Playwright Chromium·Firefox·WebKit
- Oracle·PostgreSQL·MariaDB 실제 Install·Upgrade·Rollback·Runtime Query
- 외부 연계 장애·복구 및 다중 인스턴스 Runtime
- Publication·SBOM·Artifact Repository
- GitHub Advanced Security Secret Scan

위 항목은 전체 개발 종료 후 Codex 독립 검수 직전에 한 번 수행한다.

## 판정

- 개발 구현 상태: **완료**
- 정적·구조·Gradle 설정 검증: **PASS**
- 전체 Runtime 검증 상태: **미검증**
- QA 최종 완료 상태: **QA 미통과 상태 유지**
