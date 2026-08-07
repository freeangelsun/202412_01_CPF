# QA B Deep Source Audit

## 원칙
이 재검수는 이전 QA 결과를 자동 승계하지 않았다. `file exists`, `interface exists`, `test exists`, `Swagger exists`는 PASS 근거로 사용하지 않았다. 실제 Consumer/route/owner/recovery/evidence provenance를 현재 SHA `3ed676061246c9db3e44f29e254c0393ecca3929`에서 역추적했다.

## 소스 열람 계량
- QA source probe manifest entries: **156**
- BZA page source direct-open: **26/26**
- ADM route matrix: **63/63** (direct component 또는 shared component owner 단위)
- EDU matrix: **135/135 ID accounted**
- EDU-ADM direct handler open: **17/17**
- Developer evidence logs probed: **14/14; 14 missing**
- Approval V105/R105 vendor lifecycle: **3/3 migration + 3/3 rollback source 확인**

## 중요한 제한
GitHub connector는 current exact-SHA file read를 제공하지만 repository archive를 local filesystem으로 내려주는 기능은 제공하지 않았다. sandbox의 direct GitHub DNS도 실패했다. 따라서 Java25/Gradle/npm/python gate를 current checkout에서 직접 실행할 수 없었다. 이 제한 때문에 실행하지 않은 항목은 `미검증`이며 PASS로 바꾸지 않았다.

EDU 135 중 ADM17은 파일 본문까지 개별 열람했다. 나머지 118은 QA37 verifier가 각 ID source/scenario/tests/consumer를 열어 검증하도록 구현된 Source 자체를 검수했지만 이번 환경에서 그 verifier를 current checkout으로 실행할 수 없고 handler 118개 본문을 개별 fetch하지 않았다. 따라서 135행 Matrix에서 이를 명시하고 모두 QA 미통과/미검증으로 유지한다.

## False-green 공격 결과
- Missing Evidence log인데 PASS 숫자만 문서에 남는 provenance false-green 발견.
- BZA retired 410 GET가 route metadata + generic GET workbench로 consumer count에 들어갈 수 있는 false-green 발견.
- QA37 claimed PASS와 current ADM17 canonical role 불일치 발견.
- Release workflow input 이름 불일치로 workflow 자체가 qualification 시작 전에 실패하는 결함 발견.
- Approval stale RUNNING recovery가 UNKNOWN 확정 recovery까지 의미하지 않음을 Owner SPI까지 내려가 확인.
- File-log fail-open이 canonical LOGFAIL durable recovery를 충족한다고 오인할 수 있는 semantic false-green 발견.
