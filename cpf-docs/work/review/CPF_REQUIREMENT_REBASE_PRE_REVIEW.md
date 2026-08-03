# CPF Requirement Rebase R3 작업 전 리뷰

## R2 문제

- 상세도는 높았지만 실행 선행관계와 Phase Gate가 없어 후행 구현 후 기반 계약을 다시 고칠 위험이 있었다.
- 최신 Repository 전체 Inventory가 최종 완료조건으로 강제되지 않아 목록 밖 기능 누락 가능성이 남았다.
- 과거 QA 자료를 Source Coverage에 요약했으나 각 Work Package 순서와 변경 동결 규칙이 부족했다.
- R2만 완료하면 현재 범위가 닫힌다고 단정할 수 없는 상태였다.

## R3 시정 방향

- 16단계 순서와 Phase Gate 추가
- 모든 Requirement에 Phase·Order·Work Package·선행조건·변경 동결·재작업 방지 규칙 추가
- 실제 제품 기능 추가 요구와 Source Inventory Closure 추가
- 최종 SHA 재Inventory에서 미등록 0건을 QA 종료조건으로 강제

## R4 Packaging Review

- GitHub 100MB hard limit와 50MB 권고치를 확인했다.
- Requirement Master 149,795,386 bytes와 Scenario Master 55,137,925 bytes는 단일 파일 Push가 불가능하거나 권고치를 초과했다.
- 메인 경로는 삭제하지 않고 Part 인덱스로 유지한다.
- 실제 데이터는 45,000,000 bytes 미만 Part로 분할한다.

## R5 Packaging Review

- GPT·Codex·QA 조회 안정성을 위해 Requirement·Scenario·Execution Sequence를 모두 8MB 미만 Part로 분할한다.
- 세 메인 경로는 삭제하지 않고 인덱스로 유지한다.
