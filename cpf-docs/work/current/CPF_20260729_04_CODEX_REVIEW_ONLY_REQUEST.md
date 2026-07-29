# CPF 20260729_04 Codex 검수 전용 요청서

## 역할

Codex는 검수자다. 신규 기능 개발, Source 수정, SQL 작성, Frontend 구현, Architecture 변경을 하지 않는다. 결함은 재현 가능한 보고로 ChatGPT 개발 세션에 반환한다.

## 기준

- Repository: `https://github.com/freeangelsun/202412_01_CPF`
- Branch: `master`
- 체크포인트 작성 기준 SHA: `b8941577b99535ff3e64a4fad99b74bafa544227`
- 실제 검수 기준: 사용자가 Overlay 적용·Push한 최신 master exact SHA
- 200개 체크포인트: `cpf-docs/quality/qa-20260729/CPF_QA_SCENARIO_200_CHECKPOINT_MATRIX_20260729_04.csv`
- 전체 387개: `cpf-docs/quality/qa-20260729/CPF_QA_387_SCENARIO_VALIDATION_BACKLOG_20260729_04.csv`

## 필수 검수

1. Working Tree Clean과 exact SHA 기록
2. Java25/Gradle9.1 전체 clean test assemble qualityGate
3. ADM/BZA Frontend install/build/test 및 Browser E2E
4. Oracle/PostgreSQL/MariaDB Fresh/Upgrade/Rollback/Reapply
5. Redis Standalone/Sentinel/Cluster와 장애·복구·Reconcile
6. ADM/BZA Session/Action Permission 직접 API 우회 차단
7. Runtime Control 대상 Snapshot, Partial Failure, Retry/Cancel/Rollback, Offline Catch-up
8. File Job 대용량/악성파일/중단/다중 Worker/Retention/Rollback
9. Generator 임의 Domain 2개 Lifecycle
10. Batch/Gateway/외부연계/Notification Provider 정상·오류·경계·부분 실패
11. Evidence Contract와 민감정보 제거

## 선행 명령

```powershell
python .\cpf-tools\verification\20260729_04\check_checkpoint_overlay.py .
```

```powershell
.\gradlew.bat clean test assemble qualityGate --no-daemon --no-build-cache
```

## 결함 보고 형식

- Scenario/Requirement ID
- 기준 SHA
- 실행 명령·환경·Profile·시작/종료
- Expected / Actual
- 최초 실패 지점
- 관련 Source/API/SQL/UI/Test 경로
- Sanitized Evidence 경로
- 상태: 실패 / 미검증 / 재확인 필요

## 금지

- Codex가 기능을 대신 개발하지 않는다.
- Source 존재·Interface·Mock·화면 제목·Swagger 노출만으로 PASS 처리하지 않는다.
- 과거 Commit/다른 장비 Evidence를 현재 PASS로 승계하지 않는다.
- 사용자 승인 없이 Commit·Push·Branch·PR을 만들지 않는다.
