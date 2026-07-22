# CPF 문서 복구·재시작 검수 보고

## 1. 기준

- 확인 Commit: `8da2e6f395d72ce032e52ea16fef0d1d5f490c5b`
- 직전 Commit: `a63380e6c736fa9c5ae7e425d0e301d21ef3b848`
- 정리 Commit 통계: 4,878 additions / 24,599 deletions

## 2. 의도된 삭제

- `.vscode`
- 하위 중복 README
- 과거 환경 Evidence
- Generated Matrix
- 조기 DOCX
- 조기 Release Notes
- SQL Archive 잔재

Source, Test, 활성 SQL, Flyway와 공식 Module은 정리 Commit에서 삭제되지 않았다.

## 3. 발견된 문서 회귀

- Root README 한글 Encoding 손상
- 삭제한 `cpf-docs/README.md`, Release Notes Link 잔존
- API/Security Guide 내용 중복
- Installation/Migration Guide 내용 중복
- Operator/Recovery Guide 내용 중복
- Deployment/ADM-BZA Frontend Deployment 내용 중복
- 기존 상세 Guide가 축약본으로 덮임
- Work Request 기준 Commit이 이전 값
- 삭제된 DOCX/Generated/Evidence를 참조할 Quality Gate 가능성

## 4. 이번 Overlay 보정

- Root README UTF-8 복구와 Link 정리
- 최상위 목표의 기존 상세 원칙과 신규 Ownership 정책 병합
- 현재 요청서 Baseline을 최신 정리 Commit 이후 HEAD 기준으로 갱신
- 역할별 Guide를 독립 내용으로 복구
- Fixed-Length Core, Sequence BZA Sample, cmnDB 1 Table, Batch Owner 정책 반영
- DB Empty Install 계약과 Codex 재시작 Prompt 추가

## 5. 남은 검증

문서 복구는 Source·DB·Runtime 완료를 의미하지 않는다. Codex는 최신 Git에서 Build, SQL, Runtime, Browser와 Evidence를 다시 검증하고 문서를 실제 구현에 맞춰 갱신해야 한다.
