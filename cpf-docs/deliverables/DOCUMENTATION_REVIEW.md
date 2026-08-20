# CPF 문서 현행화 작업 결과 리뷰

## 1. 작업 목적

이번 작업은 문서의 내용을 줄이거나 새 개발요건을 뒤에 붙이는 것이 아니라, **최종 구현 완료 상태의 CPF를 처음 접하는 사용자·개발자·운영자가 이해하고 실제로 사용할 수 있게 현재 문서를 재구성하는 것**을 목표로 했다.

## 2. 사용자 피드백에서 직접 보정한 문제

### 구조
- README `License` 아래에 Central Server Registry 기능 설명이 붙었던 잘못된 배치를 제거.
- 새 Channel/Subject/Central Registry Requirement를 각 Owner 문서의 적정 장/하위절로 재배치.
- 공식 사용자 문서 7종과 별도 설계 산출물 5종을 분리.

### 페이지 균형
- Heading 1/2에 기계적으로 걸린 Page Break를 제거.
- 실제 대장급만 새 페이지 시작을 유지.
- 표 마지막 1행·bullet 1개·목차 항목 몇 개만 다음 페이지에 고립된 사례 수정.
- 문단 간격을 없애지 않고 1~2줄 수준의 절 구분과 본문 호흡을 유지.
- 총 116페이지를 전수 시각검수.

### 탐색성
- 표지 다음 Static TOC를 실제 내부 Hyperlink/Bookmark 방식으로 유지.
- PDF 변환 후 `GoTo` 링크를 구조적으로 검사해 invalid destination 0 확인.

### Source Fact Check
- 문서에서 사용하는 `Cpf*` 타입 54종 Source 존재 확인, missing 0.
- `run-local`, `status`, `verify-fast` 현재 Source 명령 확인.
- 구 System Header 및 stale Public API/Annotation 10종 패턴 잔존 0.

## 3. 독자별 개선

### 처음 보는 사용자
README와 Architecture 흐름에서 CPF가 무엇인지, Spring Boot와 어떤 관계인지, 업무 Domain과 Public Starter/Runtime/ADM이 어떻게 나뉘는지, 개발에서 운영까지 무엇이 자동화되는지 순서대로 이해할 수 있도록 구성했다.

### 업무 개발자
개발자 가이드는 Golden Path → Public API/Annotation → Domain Call → Transaction/DB → Integration/Resilience/Security → Channel/Subject 자동처리 → Source Reference 흐름을 유지한다. Source에 없는 과거 API명을 예제로 사용하지 않는다.

### 배치 개발자/운영자
Job/Step/Chunk/Partition/Worker/Scheduler/Center-Cut, Restart/Reprocess/Reconcile 및 Runtime/Agent 관계를 분리해 설명하며 Central Registry를 별도 Batch Server Master로 오해하지 않게 정리했다.

### Gateway 개발자
Gateway는 선택형 외부 진입 Runtime으로 설명하고 Business Domain Header/Context와 Control Plane 경계를 분리했다. Server 정보는 Central Registry Consumer로 설명한다.

### 운영자
ADM의 Runtime/Health, Logging, Configuration, Transaction/Subject Timeline, Central Server Registry를 연결해 “고객 문제 → Transaction → Runtime Instance → Server Detail/Health/Log/Config”까지 운영 흐름을 이해할 수 있게 했다.

### Specification 독자
External required 5 vs internal Context 6, Channel 길이 16, Generated Domain systemCode 관계, operationId+callerChannel Policy, Subject/Server Identity 등의 정확한 기술 계약을 Reference 형태로 유지한다.

## 4. 최종 품질 Gate

| Gate | 결과 |
|---|---|
| DOCX | 11종 |
| PDF | 11종 |
| 총 페이지 | 116 |
| 전 페이지 시각 QA | PASS |
| DOCX a11y | 11/11, finding 0 |
| PDF Preflight | 11/11 PASS |
| PDF TOC 내부 링크 | 11/11 존재, invalid 0 |
| README local link | 22 / missing 0 |
| `Cpf*` Source Fact Check | 54 / missing 0 |
| Stale Header/API 패턴 | 0 |
| Unicode ZIP filename | Packaging Gate에서 검증 |
| Delete | 0 |

## 5. 남은 주의

문서는 사용자 지시에 따라 최신 개발요건을 **최종 구현 완료 상태**로 설명한다. 이후 Source가 다시 변경되면 실제 API/Class/Config/Consumer를 재Inventory해 문서와 Source를 다시 맞춘다. 과거 이번 QA 수치만 다음 Source에 자동 승계하지 않는다.
