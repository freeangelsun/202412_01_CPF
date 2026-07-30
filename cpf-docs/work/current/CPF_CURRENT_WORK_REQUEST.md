# CPF Current Work Request — QA31 적용 후 독립검증

## 기준

- Repository: `https://github.com/freeangelsun/202412_01_CPF`
- Branch: `master`
- QA31 개발 Base: `9594c8d5d9b1127a4e2694d0ec2f4add9475fc7e` (`20260730_09`)
- 전달 형태: `WORKTREE-OVERLAY`
- 사용자 승인 없는 Commit·Push·Branch·Tag·PR 없음
- README/Guide 범위는 별도 AI가 관리하므로 QA31 기능 검수에서 제외한다.

## 현재 판정

- 통합 Matrix: Requirement 708, Scenario 218, 개발 완료 652/미검증 274, 검증 완료 82/미검증 844

- Requirement + Scenario 165건: 부분 구현 73, 미검증 92
- Defect 23건: 부분 구현 20, 재확인 필요 3
- QA31 Full Static Gate: Exit Code 0, 476 checks, failures 0
- Java 21 격리 Harness: Gateway HMAC 및 Batch JCA/PKIX Signature PASS
- Java 25 전체 Gradle, Frontend 전체 Build/Test, 3 DB Lifecycle, Redis·Multi-instance, Gateway·Batch Runtime, Browser E2E: 미검증

## 다음 실행 순서

1. `origin/master`와 Working Tree를 확인하고 Base SHA가 다르면 적용하지 말고 Diff를 재검토한다.
2. QA31 ZIP을 Project Root에 적용하고 Delete Manifest를 실행한다.
3. `git diff --check`와 변경 목록을 검토한다.
4. QA31 Full Gate와 EDU/BZA Coverage Gate를 전체 Repository에서 실행한다.
5. Java 25 전체 Gradle과 ADM/BZA Frontend 검증을 실행한다.
6. Oracle·PostgreSQL·MariaDB Install→Upgrade→Rollback→재설치를 실제 실행한다.
7. Gateway·Batch·Redis·Multi-instance·Failure Injection·Browser E2E를 실행한다.
8. 사용자가 Push한 새 exact SHA로 Evidence를 다시 생성하고 QA/Codex가 독립검수한다.

## 완료 처리 금지

- Interface·DTO·Table·화면 파일 존재만으로 완료 처리
- 실행하지 않은 검증을 PASS로 기록
- 과거 SHA Evidence 승계
- README/Guide 변경을 QA31 기능 결과에 포함
- Consumer·DB·Runtime·실패·복구·Audit 연결이 없는 상태 승격
