# CPF_EVIDENCE_INDEX

## 1. Evidence 정책

현재 `e35b7a0`의 `specs/evidence/20260720_05` 자료는 작업자 중간 Evidence다. 기준 Commit·실행 명령·환경·종료 코드·원문 결과를 다시 확인하기 전에는 완료 근거가 아니다.

## 2. 우선 재검증 대상

| Evidence 영역 | 현재 판정 | 재검증 |
|---|---|---|
| 전체 Gradle test | 재확인 필요 | clean 상태 종료 코드·test report |
| MariaDB full install | 재확인 필요 | 설치·재실행·migration·DB 조회 |
| ACC lifecycle | 재확인 필요 | 삭제·생성·build·삭제·재생성 hash |
| Generator 4 DB Vendor | 재확인 필요 | dependency·config·package·build |
| BAT two-worker runtime | 재확인 필요 | DB lease·중복 0·drain·takeover |
| ADM/BZA frontend | 재확인 필요 | npm ci·lint·typecheck·test·build·archive |
| OpenAPI runtime | 재확인 필요 | 실제 프로세스·HTTP 200·contract |
| File/DB log parity | 실패 또는 중단 | 최신 경로·실제 거래·DB 대조 |
| Traceability | 도구 결과 | 기능 완료 근거가 아닌 보조 자료 |
| Architecture inventory | 도구 결과 | Owner·Consumer 확인용 보조 자료 |
| Browser E2E | 미검증 | 실제 브라우저 실행 필요 |
| Secret/License/SBOM | 재확인 필요 | 최신 diff·package 대상 검사 |

## 3. Evidence 필수 Metadata

- requirement ID
- baseline/final Commit
- command
- environment
- startedAt/endedAt
- exitCode
- result
- raw log
- sanitized 여부
- stale 여부
- 관련 Source/API/SQL/Test
