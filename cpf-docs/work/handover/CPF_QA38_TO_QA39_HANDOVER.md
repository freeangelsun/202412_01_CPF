# CPF QA38 → QA39 Handover

## 기준

- 최신 검토 SHA: `54bcc10887a83b933685bff462c0b0d7df824923`
- QA38 Push Commit: `20260802_10`
- Source 변경/삭제 경로 Audit: 612행
- QA38 Requirement 재판정:
  - 완료 11
  - 부분 구현 81
  - 미검증 29
  - 실패 22
  - 재확인 필요 13

## 핵심 결론

QA38 Source 이관 규모는 크지만 전체 완료가 아니다.  
가장 먼저 수정할 대상은 신규 Capability 세부 기능이 아니라 Build/Artifact/Ownership/Truth 기반이다.

## P0 시작 순서

1. Matrix 완료 상태 초기화와 exact-SHA Evidence
2. 미등록 Integration Module 7개 정식 등록
3. Artifact Registry/BOM/Platform 일원화
4. BOM literal version 51건 수정
5. Internal Classpath 7건 수정
6. HTTP/Runtime Control Cycle 제거
7. Runtime Control SPI 분해
8. Archive Bean 단절 복구
9. Batch subproject 의존 범위 교정
10. Generator Provider Binding 실제 Dependency 적용
11. False-Green Gate 교체
12. Java 25 full build와 publication

## 금지

- 현재 QA38 Matrix의 완료 값을 그대로 승계하지 않는다.
- 파일 존재, Interface, AutoConfiguration 이름, Marker, Sample만으로 완료 처리하지 않는다.
- Environment 미검증을 완료로 변경하지 않는다.
- Codex에게 개발 미완료를 넘기지 않는다.
- Protected path를 수정하지 않는다.
- Git write 작업을 사용자 승인 없이 수행하지 않는다.

## 다음 정본

- `cpf-docs/work/current/CPF_QA39_SELF_DEVELOPMENT_REQUIREMENTS.csv`
- `cpf-docs/work/current/CPF_QA39_INTEGRATED_DEVELOPMENT_REQUEST.md`
- `cpf-docs/work/review/CPF_QA38_POST_PUSH_DEFECT_REGISTER.csv`
- `cpf-docs/work/review/CPF_QA38_POST_PUSH_SOURCE_AUDIT_MATRIX.csv`
- `cpf-tools/verification/qa39/verify-qa39-post-push-closure.py`
