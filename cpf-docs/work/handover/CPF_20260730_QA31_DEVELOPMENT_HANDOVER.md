# CPF QA31 개발 인수인계

## 기준

- Repository: `https://github.com/freeangelsun/202412_01_CPF`
- Branch: `master`
- 개발 Base: `9594c8d5d9b1127a4e2694d0ec2f4add9475fc7e` (`20260730_09`)
- 전달 Head: `WORKTREE-OVERLAY`, 사용자 승인 없는 Commit·Push 없음
- QA 원본 상태: Defect 23, Requirement 99, Scenario 66 — 원본 파일 미수정
- README/Guide 제외 정책 유지

## 현재 상태

- 통합 Matrix: Requirement 708, Scenario 218, 개발 완료 652/미검증 274, 검증 완료 82/미검증 844

- Result Matrix: 완료 0, 부분 구현 73, 미검증 92, 실패 0
- Defect Register 대응: 부분 구현 20, 재확인 필요 3
- QA31 Full Static Gate: Exit 0, 476 checks, failures 0
- Gate 자체검수: QA31 기본 Self-Test 5건 + EDU/BZA Coverage Gate Fixture 4건 PASS
- Java 21 격리 Compile/Harness: Gateway HMAC 및 Batch Signature PASS
- Java 25/DB/Redis/Runtime/Browser: 미검증

## 보호할 Architecture 결정

1. Gateway Control 서명은 Body Hash·Audience·Key ID를 포함한다.
2. Nonce와 Security Audit은 공유 DB 원장이며 JVM Local Map으로 되돌리지 않는다.
3. Gateway Route는 Ingress Pattern과 Target Path를 구분한다.
4. ACK 없는 Candidate를 Runtime 활성본으로 사용하지 않는다.
5. Batch FILE_PROCESS는 실제 Processor Consumer 없이는 실행하지 않는다.
6. 민감 Shell Parameter는 Command line/평문 임시파일로 전달하지 않는다.
7. Log Export는 다중 인스턴스 공용 DB Artifact 원장을 사용한다.
8. README/Guide 작업은 별도 AI 범위다.

## 적용 순서

1. `git fetch origin` 후 `origin/master` exact SHA 확인
2. Working Tree clean 확인
3. ZIP을 임시 폴더에 해제
4. `apply-cpf-qa31-development-result.ps1` 실행 — Overlay copy + Delete Manifest 적용
5. `git diff --check`, `git status --short`, 변경 목록 검토
6. QA31 Full Gate, Java 25, Frontend, DB·Runtime 검증
7. 사용자가 직접 Commit·Push
8. 새 exact SHA로 Evidence 재생성 후 QA/Codex 독립검수

## 미검증 및 재확인

- 실제 DB Vendor Lifecycle과 Multi-instance Replay/Nonce 원자성
- Gateway Streaming client disconnect와 Retry/Failover attempt ledger
- Batch 파일 중복 Claim, Worker crash, Unknown Result, 보상·재처리
- ADM/BZA 브라우저 권한·승인·SSE·대량 Paging
- Java 25와 실제 Spring/Gradle 전체 Compile

## 관련 정본

- `cpf-docs/work/review/CPF_20260730_QA31_PRE_DEVELOPMENT_REVIEW.md`
- `cpf-docs/work/review/CPF_20260730_QA31_DEVELOPMENT_COMPLETION_REPORT.md`
- `cpf-docs/quality/CPF_20260730_QA31_RESULT_MATRIX.csv`
- `cpf-docs/quality/CPF_20260730_QA31_UNRESOLVED_REGISTER.csv`
- `cpf-docs/work/current/CPF_CURRENT_WORK_REQUEST.md`
- `cpf-docs/work/current/CPF_20260730_QA31_CODEX_REVIEW_READY.md`
- `cpf-docs/evidence/current/*.json`
