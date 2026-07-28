# CPF 20260729 Stage 2 전체 QA 통합·보완 구현 보고서

## 1. 기준

- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- Overlay 작성 기준 SHA: `9e5d1676a9ccba55fedf4dfb633a9e710f487a02`
- 기존 QA: 요구사항 1,749건 + 실행 시나리오 369건 = 2,118건
- 신규 QA 병합 후: 요구사항 2,328건 + 실행 시나리오 387건 = 2,715건
- P0 Ledger 18건과 ADM Runtime Control 14개 Capability는 우선 추적 범위이며 전체 QA를 대체하지 않는다.

## 2. 실제 보완

1. Root Gradle `targetProject` Closure scope 오류를 수정했다.
2. Notification Portable SQL Gate가 `delivery_id` generated-key Owner를 잘못 검사하던 문제를 수정했다.
3. 만료된 Notification `PROCESSING` Lease를 자동 재발송하지 않고 `UNKNOWN_RESULT`로 격리한다.
4. Retry·Cancel에 `expectedVersion` CAS와 HTTP 409 충돌 응답을 추가했다.
5. Provider Attempt 불변 이력과 ADM 조회 API·화면을 추가했다.
6. MariaDB·PostgreSQL·Oracle V68 Migration과 R68 Rollback을 추가했다.
7. Provider 결과 메시지 저장·응답 경로에 민감정보 Redaction을 적용했다.
8. 기존 2,118건과 신규 QA를 병합한 2,715건 원장과 Dedup Trace를 작성했다.
9. Module·Package, Generated Domain, Menu·UI, Garbage Removal Matrix Exporter를 추가했다.
10. Architecture·UI·Hygiene 통합 Gate를 Enterprise Closure와 CI에 연결했다.
11. Generator parity가 공식 DB 3종만 사용하도록 수정했다.
12. `cpf-external`을 공식 Module로 취급하던 Stale Architecture inventory를 제거하고 EXS Generator 정책으로 교정했다.
13. 직전 Codex 검수 이후 누적 전체를 대상으로 하는 Codex 검수·보완 개발 요청서를 작성했다.
14. Lease 복구와 정상 결과 확정의 DB 잠금 순서를 Parent Outbox → Attempt로 통일해 교차 Deadlock 가능성을 줄였다.
15. 병합 원장의 ID 중복, 종류별 건수, 기존 2,118개 ID 전부 보존을 통합 Gate에서 강제했다.
16. Matrix Source 탐색이 `cpf-batch` 중첩 Runtime Module까지 포함하도록 보강했다.
17. ADM/BZA의 Result 객체 Raw JSON `<pre>` 화면을 자동 Gate에서 차단했다.

## 3. 이번 환경에서 실제 수행한 검증

- JSON parse
- YAML parse
- CSV row/column shape와 병합 원장 2,715행 대조
- 허용 상태값 및 전 행 `미검증` 유지 확인
- UTF-8 text control character 검사
- 변경 TypeScript `tsc --noEmit`
- Vue SFC 기본 tag 구조와 Raw JSON `<pre>` 부재 확인
- Java parser 수준 syntax diagnostic 부재 확인
- V68 Migration SHA-256과 4개 checksum manifest 일치 확인
- 절대 로컬 경로·대표 Secret pattern 정적 검색
- 기존 2,118개 QA ID가 병합 원장에 전부 보존됐는지 대조

## 4. 실행하지 못한 검증

현재 환경에는 PowerShell 7, Java 25, Gradle 9.1 Runtime, 공식 DB 3종과 Browser가 없어 다음은 실행하지 못했다.

- PowerShell Gate
- Java 25 전체 Build·Test·Assemble
- MariaDB·PostgreSQL·Oracle Lifecycle
- ADM/BZA Browser E2E
- Multi-process·Multi-instance·Offline·Kill/Recovery
- 실제 Email·SMS·Broker·SFTP Provider

따라서 이 Overlay는 구현·정적 검수 산출물이며 전체 2,715건 완료 Evidence가 아니다. 최신 Push SHA에서 Codex와 실제 실행 환경이 전수 검수·보완해야 한다.

## 신규 QA 계수 보정

- 신규 QA 원문 Bullet 590개 중 Section 15 판정 분류 4개와 Section 18 허용 상태 6개는 독립 Requirement가 아니므로 `EXCLUDED_METADATA`로 보존하고 병합 원장에서는 제외했다.
- 기존 Requirement와 확인된 중복 1개는 `MERGED` 처리했다.
- 따라서 신규 고유 Requirement는 579건, 신규 Scenario는 18건이며 최종 병합 원장은 Requirement 2,328건 + Scenario 387건 = 2,715건이다.
- 중복 후보 5건은 임의 삭제하지 않고 Codex가 Root Cause·실제 Consumer 기준으로 재확정한다.
