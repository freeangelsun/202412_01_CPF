# R4 Open Issues

현재 환경에서 수정 가능한 Source·SQL·Frontend·Test·Config·Script와 Java 21·Node·Python 대체검증은 실행했다. 아래 항목은 외부 실행 환경이 없어서 개발 GPT 완료로 기록하지 않았다.

## 외부 검증 잔여

1. **전체 Repository exact HEAD**: Overlay 적용·Commit 후 clean Working Tree, 전체 `settings.gradle`/Build Graph, 모든 Main Source Import, Manifest Hash를 새 HEAD에서 검증한다.
2. **Java 25·Gradle**: Root Configuration, 지정 Module Test, Spring ApplicationContextRunner, Publication을 실행한다.
3. **실제 3 Vendor DB**: Oracle·PostgreSQL·MariaDB Fresh Install, V97~V100 Upgrade, Verify, R100~R97 Rollback/Reapply, Reinstall, Runtime Repository Query를 실행한다.
4. **실제 Browser/BFF**: ADM/BZA 401·403·404·409·429·500·503, Paging, 접근성, 세션 만료, 부분 실패, UNKNOWN을 Chromium에서 실행한다.
5. **Audit 실제 Spring/DB**: 실제 ADM Spring 인스턴스 두 개와 ADM DB에서 동시 저장·Kill·지속·재기동·중복/유실·fail-closed를 실행한다.

전체 외부 검증 명령은 `cpf-tools/scripts/run-cpf-r4-exact-head-validation.ps1`과 `ENVIRONMENT_VALIDATION_HANDOFF.csv`에 기록했다.

## Requirement 집계

- 개별 Requirement Traceability 연결: **10,558건**
- 공통 구현·Java21/Node/Python 대체 Evidence 연결: **6,972건**
- Traceability-only·미검증: **3,586건**
- Work Package Source 연결: **291/291**
- Work Package Required Aspect 미연결: **0**
- Requirement별 전체 Acceptance 개발 완료: **0건**

`REQUIREMENT_STATUS.csv`는 QA 판정을 변경하지 않고 모든 행의 개발 GPT 상태를 `미완료`로 유지한다. 공통 대체 Evidence가 일부 기준만 증명하는 6,972건은 `부분 구현`, 나머지 3,586건은 `미검증`이다. 각 행의 `uncovered_acceptance`와 `개발GPT_미완료사유`에 정확한 잔여를 기록했다.
