# CPF QA35 Handover

## 기준
- 시작 exact SHA: `e1f8bef7b7193522f2cd8e36cc6857dd1ff6694a`
- QA35 Defect: 46
- QA35 Requirement: 55
- ADM Route: 59
- EDU Feature Baseline: 32

## 다음 작업자가 가장 먼저 할 일
1. 최신 origin/master exact SHA와 clean tree를 확인한다.
2. QA35 Post-Push Review와 Defect Register를 읽는다.
3. QA35-REQ-001 Truth Reset부터 수행한다.
4. 현재 ADM/BZA OpenAPI·marker·generated files의 모순을 재확인한다.
5. deterministic frontend preflight를 먼저 고친다.
6. ADM 59 route matrix와 EDU 162 requirement coverage를 동시에 기준선으로 고정한다.

## 절대 금지
- Static Gate만 통과하고 development 완료 처리
- Runtime을 다음 회차로 단순 이월
- 메뉴/예제 이름만으로 기능 완료 처리
- 사용자 승인 없는 Commit/Push/Delete


## ADM 추가 인수인계
- Screenshot Evidence Index부터 읽고 임의로 menu 이름만 비교하지 않는다.
- 87 Capability Matrix의 current_assessment는 시작점이며 Source/Runtime으로 재판정한다.
- 고급 대체는 기능을 숨기는 근거가 아니다. 최소 기능 outcome과 고급 안전계약을 모두 충족해야 한다.
- Target Menu Architecture는 중복 대형 Component를 정리하고 운영자 task flow를 만드는 기준이다.
