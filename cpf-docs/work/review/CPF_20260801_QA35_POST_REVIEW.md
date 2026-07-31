# CPF QA35 요청 작성 후 독립 리뷰

## 1. 산출물 검토 결과
- Defect: 46건
- Requirement: 55건
- ADM Route Baseline: 59건
- EDU Feature Baseline: 32건
- Source 변경: 없음
- Git Write: 없음
- 삭제: 없음

## 2. 작업 전 요청 대비 보강된 내용
- QA34의 20/20 개발 완료 판정을 그대로 승계하지 않고 Source 모순을 P0로 재분류했다.
- Frontend “생성 Script 존재”가 아니라 tracked OpenAPI/generated artifact/fresh clone Build를 하나의 완료 단위로 묶었다.
- ADM을 Route 존재가 아닌 59개 메뉴의 API·권한·상태·상호작용·감사로 전수 정의했다.
- EDU를 Sample 존재가 아닌 162 Requirement/Public API의 실제 Consumer·Fault·ADM·Evidence로 정의했다.
- 마지막 all-or-nothing 검증 전에 deterministic Source Gate와 CI를 필수화했다.
- Codex가 전체 탐색을 반복하지 않도록 검수 순서·핵심 파일·명령·미실행 범위를 별도 제공했다.

## 3. 재발 방지 핵심 규칙
1. `development_status=완료`는 fresh clone deterministic build와 실제 Consumer가 있어야 한다.
2. Runtime 미실행은 반드시 `verification_status=미검증`이다.
3. Public Contract 변경은 Generator·EDU·ADM·OpenAPI·Test Impact Row 없이는 완료할 수 없다.
4. Current Request는 P0 Source Gate가 모두 통과하기 전 “검증만 남음”으로 닫지 않는다.
5. Evidence Verifier가 Requirement별 수용조건을 확인해야 한다.
6. Push exact SHA에 CI Status가 없으면 Release 완료 금지다.

## 4. 미실행 검증
현재 환경 제한으로 다음은 실행하지 않았다.

- fresh clone Gradle
- Java 25 compile/test
- npm ci/generate/typecheck/build
- Browser 3종
- 3DB/Kafka/Multi-instance/Process Kill
- Supply-chain tools

Source-level로 확정된 모순은 실행 없이도 실패 판정이 가능하다. Runtime 상태는 미검증으로 남겼다.

## 5. 삭제 후보
이번 검토는 Source를 생성·수정하지 않았고 Repository 가비지/빈 폴더를 실제 filesystem clone에서 탐지하지 못했다. Delete Manifest는 비어 있으며 정리 대상 없음으로 기록한다.


## 6. ADM 최소 기능선 보강 결과

- Screenshot Evidence: 44건
- Minimum Capability: 87건
- Target Menu Architecture: 41건
- Screen Quality Acceptance: 20건
- 추가 Defect: 18건
- 추가 Requirement: 20건

초기 QA35가 Route/API/Permission 구조를 중심으로 작성됐다면 V2는 실제 운영 제품의
세부 Menu·업무 흐름·분석·화면 완성도까지 기준선으로 고정했다.
