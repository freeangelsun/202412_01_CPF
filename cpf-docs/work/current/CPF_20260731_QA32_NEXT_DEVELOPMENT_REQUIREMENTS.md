# CPF QA32 다음 개발요건

## 1. 기준

- Branch: `master`
- 시작 SHA: `1536a0d59004ebade7dcb29383cbe2e758547f8e`
- README·Guide 및 관련 Asset은 작업·검수 대상에서 제외한다.
- 사용자 승인 전 Commit·Push·Branch·Tag·PR을 생성하지 않는다.
- 부분 구현·미구현을 남기지 않는다.
- 실행하지 않은 검증을 성공으로 기록하지 않는다.

## 2. P0-1 Gradle Build Graph 복구

다음 중 하나의 정본 구조로 완전히 정리한다.

### 권장 구조

`cpf-tools/build/platform-bom`과 `cpf-tools/build/gradle-plugin`을 공식 Tooling Source로 복구하고 다음을 완료한다.

- 독립 `settings.gradle`
- BOM `build.gradle`
- Convention Plugin `build.gradle`
- Plugin Implementation
- Plugin Marker·Publication
- Local/Remote/Offline Repository 경로
- TestKit
- `settings.gradle includeBuild` 정합성

복구하지 않을 경우 `includeBuild`와 모든 Consumer를 제거하고 Root Build가 동일 기능을 직접 소유하도록 완전 이관한다.
빈 Directory나 이름만 있는 Placeholder는 금지한다.

필수 검증:

```powershell
.\gradlew.bat help --no-daemon --stacktrace
.\gradlew.bat projects --no-daemon
.\gradlew.bat clean test --no-daemon --stacktrace
```

## 3. P0-2 QA32 Gate fail-closed 개편

`verify-cpf-qa32-all.ps1`을 정적 Gate와 Runtime Gate로 명확히 분리한다.

Release 모드는 반드시 다음을 직접 실행하고 실패를 전파한다.

- Gradle initialization
- Java 25 clean test
- Architecture Test
- ADM npm ci/typecheck/test/build
- BZA npm ci/typecheck/test/build
- Playwright Chromium·Firefox·WebKit
- 3개 DB Lifecycle
- Kafka Remote Runtime
- Gateway·Agent Runtime
- Supply-chain Final Artifact
- exact-SHA Evidence 검사

Matrix 상태 문자열이나 파일 존재만으로 PASS하지 않는다.

## 4. P0-3 Runtime Script 수정

`verify-cpf-qa32-runtime.ps1`을 다음 기준으로 교체한다.

- `$script:results` 또는 반환값 기반 결과 수집
- Native command 하나마다 즉시 Exit Code 검사
- 단계마다 stdout·stderr 별도 파일 저장
- `try/finally`로 Location·Evidence 종료 보장
- 실패 시에도 Evidence JSON 생성
- 실행 명령·시작/종료 시각·Java/Node/npm/DB/Kafka Profile 기록
- Source SHA가 실행 종료 시점에도 동일한지 재검증
- Evidence 파일 Hash와 민감정보 검사

## 5. P0-4 Completion Gate 강화

각 Requirement·Defect·Scenario 완료행에 대해 다음을 검증한다.

- Evidence 파일 실재
- Evidence Source SHA = 현재 HEAD
- 실행 명령과 Exit Code
- 관련 Source·Test·SQL 경로 실재
- Runtime Consumer 연결
- 실패·복구 결과
- 민감정보 제거 표시
- 최신 Commit 유효성

검증 상태가 `미검증`이면 Release Gate는 무조건 실패해야 한다.

## 6. P0-5 전체 Compile·Frontend 결함 제거

Build Graph 복구 후 발생하는 모든 컴파일·TypeScript·Dependency 결함을 수정한다.

- Spring Batch 6 API 실제 Compile
- Remote Partition/Chunk/Step Bean Wiring
- Scheduler→JobOperator 연결
- Gateway SCG MVC Bean Wiring
- ADM/BZA Spring Session JDBC AutoConfiguration
- Starter 자동설정 Metadata
- Frontend Router·Pinia·Orval Client Compile
- package-lock 정합성

## 7. P1 Runtime 검증

다음을 실제 Runtime으로 실행한다.

### Batch

- 단일 Job
- 순차·조건 Flow
- 병렬 Step
- Local Partition
- Remote Partition
- Remote Chunk
- Remote Step
- Worker Crash·Restart
- Manager Restart
- Checkpoint 재개
- 선택 재시작
- 중복 실행 차단
- Fencing stale 결과 차단
- UNKNOWN_RESULT 대사

### Gateway·Operations

- Streaming 완료
- Client Disconnect
- Retry·Failover Attempt
- Response Cap
- Selective Rollback
- Agent Crash
- STOP 확인
- Artifact anti-rollback

### Database

Oracle·PostgreSQL·MariaDB 각각:

- 신규 Install
- V82~V85 Upgrade
- Rollback
- 재적용
- 기존 데이터 보존
- Checksum·Drift
- Spring Batch JobRepository
- Spring Session JDBC
- Bootstrap 승인 원장

### Frontend

ADM/BZA 각각:

- npm ci
- typecheck
- unit test
- build
- Chromium
- Firefox
- WebKit
- 권한별 메뉴·버튼·검색
- HttpOnly Session
- CSRF
- 강제 로그아웃
- 다중 인스턴스 Session

## 8. 완료 조건

다음이 모두 0이어야 완료다.

- Result Matrix 미검증
- Unresolved Register
- Gradle/Frontend/Runtime 실패
- Legacy Primary Engine
- Stale Evidence
- Source SHA 불일치
- README·Guide 작업 범위 침범

최종 결과에는 다음을 포함한다.

- exact SHA
- Requirement 62/62 검증 완료
- Defect 60/60 검증 완료
- Scenario 222/222 검증 완료
- OSS Migration 23/23 Runtime 검증 완료
- 변경 파일 수
- 삭제 파일 수
- ZIP SHA-256
- 실행 명령과 Exit Code
- 실패·복구 Evidence
