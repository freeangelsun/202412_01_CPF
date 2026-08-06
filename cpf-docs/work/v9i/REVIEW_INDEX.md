# CPF V9 통합 검수 문서 인덱스

## 기준

- 현재 `master` 검토 Commit: `1b35d84801e256e3e6d7e4482918817ec82865dd`
- 개발 Overlay 작성 기준 SHA: `2a013663090d4e430a15983ad7269f8e86c5ef58`
- 현재 논리 정본 Root: `cpf-docs/work/v9i`
- QA 최종 판정: **미수행**
- GitHub Commit status / workflow run: **등록 결과 없음**
- 이 문서는 개발GPT 완료 보고를 QA 통과로 변경하지 않는다.

## 현재 집계

| 구분 | 수량 | 현재 해석 |
|---|---:|---|
| Work Item | 735 | 개발GPT 완료·자체검수 완료 보고 |
| CPF-FR | 19,914 | 개발GPT 완료·자체검수 완료 보고 |
| CPF-SC | 27,075 | 개발GPT 완료·자체검수 완료 보고 |
| Engineering Gate | 21 | 개발GPT 완료·자체검수 완료 보고 |
| 전체 exact ID | 47,745 | 논리 Dataset의 unique ID |
| Integration Request | 32 | DevGPT 종결 30, 외부 실행·승인 대기 2 |
| Target-runtime 재실행 추적 | 2,618 | 실제 Target Runtime PASS가 아님 |

## 원장 탐색 순서

1. `REPOSITORY_PUSH_REVIEW.md`
2. `DATASET_MAP.md`
3. `results/REQUIREMENT_STATUS_INDEX.csv`
4. `results/status/REQUIREMENT_STATUS_PART_*.csv`
5. `results/INTEGRATION_REQUEST_CLOSURE.csv`
6. `results/TEST_EXECUTION_LEDGER.csv`
7. `evidence/FINAL_INTEGRITY.json`

`results/REQUIREMENT_STATUS.csv`는 현재 Commit에서 **헤더만 있는 Schema 파일**이다.
47,745건의 실제 상태 정본은 `REQUIREMENT_STATUS_INDEX.csv`와 4개 Part를 합친
**단일 논리 Dataset**으로 해석해야 한다.

## 이번 점검에서 확인한 정리 필요 항목

- 긴 Integration Workspace와 `cpf-docs/work/v9i`가 동시에 Commit되어 중복 보관됨
- 긴 Workspace는 Windows 경로 문제를 다시 유발할 수 있어 삭제 후보
- 기존 `REVIEW_INDEX.md`에 동일 문장이 반복 기록됨
- `PROGRESS_STATUS.csv`의 Request 진행률이 `30/32`인데 `100%`로 기록됨
- 기존 Handover가 이미 Push된 상태에서도 “Overlay 적용” 단계로 남아 있음
- 전체 Gradle/Spring Context, 실제 DB, Browser, Broker·Multi-process 검증은 미수행
- ADM 신규 Service와 SPI 구현의 Runtime Bean wiring은 실제 Context로 입증되지 않음
- 데이터 품질 정정 API가 요청 Boolean `approved`를 신뢰하므로 서버 측 승인 증명이 필요함

## 삭제 범위

이번 정리 명령에는 아래 **중복 세션 문서 Root만** 포함한다.

`cpf-docs/work/current/CPF_DEVGPT_CONTROL_V9/_session_workspace/CPF-V9-FINAL-INTEGRATION-CLOSURE-20260806/REV-001/sessions/DEVGPT-V9-INTEGRATION`

다음은 포함하지 않는다.

- Product Source·SQL·Test·Config·Frontend·Script
- `cpf-docs/work/v9i`
- S01~S06 원본 Workspace
- `cpf-starters/openapi-webmvc`
- 현재 QA Evidence

실제 삭제는 `CLEANUP_ONE_LINE.txt`의 검증형 명령을 사용자가 실행할 때만 수행된다.
