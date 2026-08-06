# Dataset Map

## 현재 상태 정본

| 목적 | 파일 |
|---|---|
| 최상위 검토 진입점 | `REVIEW_INDEX.md` |
| 구조화된 점검 결과 | `results/REVIEW_FINDINGS.csv` |
| exact ID Part 목록 | `results/REQUIREMENT_STATUS_INDEX.csv` |
| exact ID 실제 행 | `results/status/REQUIREMENT_STATUS_PART_001.csv` ~ `PART_004.csv` |
| 개발 세션 집계 | `results/DEVELOPMENT_SESSION_RESULT.csv` |
| Work Item 상세 | `results/DEVELOPMENT_WORK_ITEM_RESULT_INDEX.csv` |
| CPF-FR 상세 | `results/DEVELOPMENT_REQUIREMENT_RESULT_INDEX.csv` |
| CPF-SC 상세 | `results/DEVELOPMENT_SCENARIO_RESULT_INDEX.csv` |
| Gate 상세 | `results/ENGINEERING_GATE_RESULT_INDEX.csv` |
| Provenance | `results/PROVENANCE_INDEX.csv` |
| exact ID–파일 연결 | `results/FILE_CATALOG_INDEX.csv` |
| Evidence | `results/EVIDENCE_CATALOG_INDEX.csv` |
| 자체검수 | `results/SELF_REVIEW_CATALOG_INDEX.csv` |
| Integration Request 상태 | `results/INTEGRATION_REQUEST_CLOSURE.csv` |
| 실행 명령과 결과 | `results/TEST_EXECUTION_LEDGER.csv` |
| 환경 Gap | `results/ENVIRONMENT_CAPABILITY_MATRIX.csv` |
| 최종 무결성 | `evidence/FINAL_INTEGRITY.json` |

## 주의

- `results/REQUIREMENT_STATUS.csv`는 현재 Commit에서 헤더만 가진 Schema 파일이다.
- 진행률 집계는 `PROGRESS_STATUS.csv`, 실제 행은 분할 Part를 사용한다.
- `baseline_sha`는 구현 작성 기준이며, QA는 별도 `reviewed_commit_sha`를 사용한다.
- `개발GPT 완료`와 `QA 통과`는 서로 다른 상태다.

## Final Development GPT Campaign

| 목적 | 파일 |
|---|---|
| 25건 Campaign 상태 | `fdr/r1/REQUIREMENT_STATUS.csv` |
| 실행·Evidence | `fdr/r1/TEST_AND_EVIDENCE.md` |
| 변경 Manifest | `fdr/r1/CHANGE_MANIFEST.csv` |
| Codex 인계 | `fdr/r1/CODEX_REVIEW_REQUEST.md` |

현재 검토 기준 SHA는 `2929163b3bb40159e22e1f57e79b6cd070abf7ad`다. 기존 exact ID 47,745행과 integration request_id 32개는 변경하지 않았다.
