# QA Codex 재검수 계획

## 기준

- Repository: `freeangelsun/202412_01_CPF`
- 개발 GPT Push SHA: `2903de14eb9cd6cfcccf8e4d2a3489ee2e4193ca`
- QA 회차: `QA-DEV-R1`
- 현재 결과: S4-001~009 모두 미통과
- Codex 후보가 생성되면 `QA-CODEX-R1`로 전체 재검수한다.

## Codex 우선순위

1. `CRITICAL` Source 결함
   - Transaction Annotation 0건 PASS와 Header 검증 우회
   - HTTP Client DNS Rebinding 방어 누락
   - PostgreSQL/Oracle LONGBLOB·MEDIUMTEXT
   - BZA Public API 호환성 회귀
2. Gate False-positive
   - Split exact SHA/Execution order
   - Traceability 0행 PASS
   - Owner 전체 Graph 미검사
   - DB path-only
   - Starter Source/Publication 미검사
3. 전체 Consumer/Test 연결
4. Java 21 대체검증
5. Java 25/3DB/Browser/Signing 외부 검증 분리

## Codex 산출물 필수

- `REVIEW_INDEX.md`
- `CHANGE_MANIFEST.csv`
- `REQUIREMENT_STATUS.csv`
- `TEST_AND_EVIDENCE.md`
- `OPEN_ISSUES.md`
- `QA_REWORK_REQUEST.md`
- `PACKAGE_MANIFEST.json`

## 재검수 규칙

- `2903de14eb9cd6cfcccf8e4d2a3489ee2e4193ca..CODEX_HEAD` 전체 Diff와 기존 개발분을 함께 검수
- 모든 Evidence에 exact HEAD, Clean Tree, 명령, Exit Code, 환경 Version, File Hash 기록
- Source 결함을 환경 문제로 분류하지 않음
- Java 21 PASS는 개발 검증으로 인정
- Java 25 전용 차이만 별도 `미검증`
- QA 통과 전 전체 상태 `완료` 금지
