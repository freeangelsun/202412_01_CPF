# README Visual Storyboard — v2.15.1

README는 **다크 브로셔형 제품 간판**이다. 목차를 두지 않고 제품 H1 하나로 시작한다. H2는 독자의 질문과 Source-backed Coverage가 필요한 만큼 사용하며 **상한을 두지 않는다.**

## 권장 흐름

1. Hero — CPF / Core Platform Framework, 한 줄 정의, 짧은 보조 설명, Hero Visual
2. Architecture — 전체 구조와 Module/Domain/Boundary
3. Development — Bootstrap → Generator → Starter/Provider → Build/Test/Runtime → DB3
4. Invocation & Failure — Same JVM/Remote, System6, Local/Remote Failure, UNKNOWN/Reconcile/Idempotency
5. Batch & Integration — Batch Roles, Restart/Rerun/Reprocess, 외부 연계
6. Operations & Security — Trace/Timeline/Health, Permission/Reason/Approval/Audit
7. Try it — 실제 Source-verified 시작 명령
8. Manuals — 역할별 PDF Navigation + License

Coverage가 더 필요하면 Section을 추가한다. 전체 길이/섹션 수 때문에 기능을 빼지 않는다.

## 브로셔 시각 규칙

- Hero와 주요 CPF-owned Panel은 dark brochure Surface를 유지한다.
- 핵심 Figure는 light/neutral canvas로 분리해 dark-on-dark를 방지한다.
- Figure 수 총 상한은 없다. 의미 없는 장식/중복 Figure만 금지한다.
- 5개 이상 Figure가 있으면 최소 4종의 시각 Grammar를 사용하고 같은 Grammar를 2회 넘게 반복하지 않는 것을 기본으로 한다.
- Table은 실제 비교 관계에만 사용하고 첫 화면을 Table로 시작하지 않는다.
- 긴 본문/표/Figure를 연속 배치하지 않고 시각적 호흡을 준다.

## AI/텍스트 설명

모든 의미 Figure는 다음을 갖는다.

- 의미 있는 Alt Text
- Figure 바로 아래 자연스러운 1~2문장의 한글 설명
- 파일명만 적은 Alt Text, 빈 Alt Text, `그림 설명/그림 해석` 라벨은 FAIL

한글 설명은 Figure를 못 보더라도 핵심 구조·관계·결과를 이해할 수 있어야 한다.

## Architecture Map Semantic Ownership

README Architecture Map은 Channel/Edge(`cpf-backoffice-web`, Gateway), Business Domain(Generated Domain, `cpf-backoffice`), Platform Operations/Runtime(`cpf-admin`, `cpf-batch`)을 서로 다른 영역으로 표현합니다. `cpf-backoffice`를 Operations/Edge에 배치하거나 `cpf-backoffice-web`과 합쳐 표현하지 않습니다.

