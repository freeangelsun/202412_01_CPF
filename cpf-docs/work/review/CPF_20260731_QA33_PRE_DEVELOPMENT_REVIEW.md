# CPF QA33 착수 전 개발 리뷰

## 기준

- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- 확인한 최신 원격 SHA: `21eb93c7a7110f593e7d2db725046acb6635e7dd`
- QA33 검수 기준 SHA: `1536a0d59004ebade7dcb29383cbe2e758547f8e`
- README·Guide·관련 Asset: 별도 작업 범위이므로 변경·완료 판정에서 제외
- Commit·Push·Branch·Tag·PR: 수행하지 않음

## 통합 원장 판단

QA32 후속요건과 QA33의 113 Defect, 138 Requirement, 414 Scenario를 별도 작업으로 반복하지 않고 동일 Root Cause 묶음으로 처리한다. 문서의 완료 표시는 승계하지 않으며 Source, Consumer, Test, Runtime Evidence 순으로 판정한다.

## 우선 확정 결함

1. Root Build가 삭제된 `platform-bom`, `gradle-plugin` Included Build의 `check`와 publish Task를 계속 소비한다.
2. ADM/BZA/Gateway가 settings 정본과 다른 Starter project path를 참조한다.
3. Gateway에 중복 `bootJar` Task 선언이 있다.
4. Root가 모든 Subproject에 MariaDB Driver를 강제해 DB Driver Ownership을 훼손한다.
5. ADM Frontend가 삭제된 `clearAdmAccessToken`을 import한다.
6. BFF 응답 정제기가 `sessionId`를 다시 Body에 노출하며 중첩 Credential 형태를 fail-closed하지 않는다.
7. QA32 Gate가 CSV 상태 문자열과 마지막 native command exit code에 의존해 거짓 양성이 가능하다.
8. Archive 생성이 entry의 `byte[]` 전체 적재를 기본 계약으로 사용하고 atomic publish·부분 파일 정리가 불충분하다.
9. ADM `package.json`과 오래된 `package-lock.json`의 Dependency Set이 불일치한다. 네트워크 없는 환경에서는 lock 재생성 성공을 기록하지 않는다.

## 구현 순서

- Build Graph와 Artifact 공급 경로 복구
- Credential/Frontend 확정 Source 오류 수정
- Gate와 Evidence Contract fail-closed 전환
- Archive bounded streaming·atomic publish
- Runtime/DB/Kafka/Browser/Supply-chain은 실제 실행 환경에서만 완료 판정

## 완료 금지 조건

Java 25 전체 Build, ADM/BZA `npm ci`, 3 Browser Playwright, Kafka 장애 복구, Oracle/PostgreSQL/MariaDB Migration/복구, Agent 다중 인스턴스, Final Artifact Supply-chain 검증의 exact-SHA Evidence가 없으면 QA33 전체 완료로 기록하지 않는다.
