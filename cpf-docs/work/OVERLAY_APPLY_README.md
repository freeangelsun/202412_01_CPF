# CPF 문서 Overlay 적용 안내

## 압축 구조

압축을 풀면 `202412_01_CPF/` 폴더가 나온다. 해당 폴더 안의 내용을 실제 Repository Root에 덮어쓰면 된다.

```text
202412_01_CPF/
├─ CPF_CURRENT_WORK_REQUEST.md
├─ CPF_FINAL_TARGET_REQUIREMENTS.md
├─ CPF_GAP_MATRIX.md
├─ CPF_REVIEW_PROGRESS_COMPLETION_GUIDE.md
├─ CPF_STABILIZATION_REPORT.md
├─ CPF_EVIDENCE_INDEX.md
└─ cpf-docs/
```

## 적용 전 확인

- 현재 작업이 Git에 Commit·Push된 상태인지 확인한다.
- 압축파일에는 Source·SQL 실행 결과나 Credential이 포함되지 않는다.
- 이 Overlay는 문서·요청서 갱신용이며 Code·DB를 직접 수정하지 않는다.
- 적용 후 Codex는 `CPF_CURRENT_WORK_REQUEST.md`를 정본으로 작업한다.

## DB 상태

회사 PC의 CPF Schema·Table은 이미 삭제되었고 집 PC도 Empty DB다. 요청서에는 Schema, Table, Constraint, Index, Service User, Product Meta Seed, EDU/Test Data 분리와 Source 전체 정합성을 처음부터 재구축하도록 명시했다.
