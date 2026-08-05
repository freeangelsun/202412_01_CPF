# 완료 제외·QA 재개방·격리자료 보존

완료 스킵은 다음 개발 요청에서 제외된다는 의미다. QA 통과 또는 전체 완료를 뜻하지 않는다.

QA Feed:

```text
REDEVELOP       → 재개발 대상
REREVIEW        → 재검수 대상
INVALIDATE_IMPACT → Evidence 무효화 후 재검수 대상
REOPEN_OWNER    → 소유권 검토
EXTERNAL_BLOCK  → 외부환경 차단
```

QA 재개방 시 과거 결과와 Evidence를 삭제하지 않고 이력으로 유지하되 현재 완료 근거에서 제외한다.

Campaign Workspace 정리는 중앙 병합과 보존 Evidence 이관이 끝난 뒤 수행한다. 완료 상태를 필터로 제외할 뿐 제품 파일이나 과거 Evidence를 새 요청이 덮어쓰지 않는다.
