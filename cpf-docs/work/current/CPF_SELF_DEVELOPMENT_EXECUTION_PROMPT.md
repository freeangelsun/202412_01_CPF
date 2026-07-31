# CPF 자체 개발 실행 프롬프트

최신 `master`를 기준으로 CPF 자체 개발을 진행해라.

Repository:
`https://github.com/freeangelsun/202412_01_CPF`

Branch:
`master`

문서의 Baseline SHA를 그대로 신뢰하지 말고 작업 시작 전에 실제 `HEAD`, `origin/master`, Working Tree를 다시 확인해라.

사용자의 명시적 승인 없이 Commit, Push, Branch, Tag, PR, Reset, Restore, Stash, Clean을 수행하지 마라.

## 정본 분리

이번 작업은 다음 자체 개발 정본만 대상으로 한다.

1. `cpf-docs/work/review/CPF_SELF_DEVELOPMENT_SOURCE_REVIEW.md`
2. `cpf-docs/work/current/CPF_SELF_DEVELOPMENT_REQUIREMENTS.md`
3. `cpf-docs/quality/CPF_SELF_DEVELOPMENT_REQUIREMENT_MATRIX.csv`
4. `cpf-docs/work/current/CPF_CURRENT_SELF_DEVELOPMENT_REQUEST.md`

외부 검수 조직의 Requirement·Defect·회차 문서를 생성하거나 수정하거나 재분류하지 마라.
외부 검수 목록이 이번 세션에 별도로 제공되지 않았다면 추정해서 만들지 마라.
자체 개발 ID는 `CPF-SELF-DEV-*`만 사용해라.

## 작업 시작 전

최상위 목표와 관련 Source·API·SQL·Test·Frontend·Script·Config·Migration·Evidence를 확인하고 사전 리뷰를 작성해라.

사전 리뷰에는 Owner Module, Public API/SPI/Internal 경계, 실제 Consumer, Batch·Online 통합 영향, MSA/동일 JVM, 다중 인스턴스, 부분 실패·재시도·복구, 보안·권한·승인·감사·마스킹, DB·Generator·Vendor 영향, 기존 성공 기능, 구현 순서, 완료 조건을 포함해라.

## 개발 목표

`CPF_SELF_DEVELOPMENT_REQUIREMENTS.md`의 30건을 문서 보완이 아니라 실제 Source 구현으로 닫아라.

File·Class·Menu·Route·Script·Marker·Matrix 존재만으로 완료 처리하지 마라.
각 기능을 Source·Backend API·Frontend Consumer·OpenAPI·Generated Client·Permission·Audit·정상/오류/복구 Test·Runtime Evidence까지 하나의 완료 단위로 처리해라.

특히 다음을 완결해라.

- ADM Canonical Menu·Route·Permission·Operation Registry
- Silent Dashboard Fallback 제거
- Gateway Menu·Tab 정합성
- Batch Execution·Scheduler HA·Worker Pool·Host Agent·Recovery Workbench
- Batch 위험조치 권한·승인·CAS·Audit
- Online Transaction Definition·Runtime Diagnostics·Error·Message·Deployment
- Batch·Online Causal Timeline과 통합 Dashboard
- 전체 OpenAPI·Generated Client·실제 Consumer
- 전체 ADM Route 상용 Page Contract
- 실제 Controller Permission 전수 검증
- Literal IP/CIDR 정책 공통화
- Oracle·PostgreSQL·MariaDB Baseline Upgrade Chain
- Exact-SHA Fresh Clone 독립 완료 Gate

## 검증과 완료 판정

Runner나 Matrix 존재를 Runtime 완료로 승격하지 마라.
하나의 Evidence에 다수 ID를 무검증 일괄 삽입하지 마라.
과거 SHA Evidence를 승계하지 마라.
Self-Dirty·False Green·Fail-Always 검증 구조를 남기지 마라.

저비용 Gate → Java 25 Build → Frontend → 3 Browser → 3 DB → Kafka/Batch/Scheduler/Gateway/Agent → Process Kill/Recovery → Supply-chain → Exact-SHA Evidence 순서로 각 대형 검증을 한 번씩 실행해라.

환경 부족으로 실행하지 못한 검증은 성공으로 기록하지 말고 명령·환경·오류·종료코드를 기록해라.

## 최종 산출물

프로젝트 Root 상대경로의 단일 ZIP Overlay를 제공해라.

ZIP에는 Source·API·Generated Client·SQL·Migration·Test·Script·Config·작업 전후 리뷰·자체 개발 Matrix·Sanitized Evidence·Handover·Continuity·독립 검수 요청서·Manifest·SHA-256·Delete Manifest를 포함해라.

최종 응답에는 ZIP 링크와 SHA-256, 기준 SHA, 변경 파일 수, 적용·저비용 검증·Git Status·가비지 정리 PowerShell 한 줄 명령, 수행/미수행 검증, 완료/미검증/실패 구분을 제공해라.

독립 검수자는 개발 완료 후 검증만 수행한다. 개발이 끝나기 전에 구현을 넘기지 마라.
