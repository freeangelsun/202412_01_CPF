# CPF 부분 구현 금지 및 완료 판정 표준

> Canonical path: `cpf-docs/governance/CPF_NO_PARTIAL_IMPLEMENTATION_COMPLETION_STANDARD.md`  
> Final Target synchronization: `2026-07-31`  
> Review baseline: `c1f273f1ea4fafac6fd5d23bd837adfc38a04497`

## 1. 목적

CPF의 완료 상태는 파일·Class·Interface·Table·화면·Dependency·Test 이름의 존재가 아니라, 실제 Product Consumer가 사용하는 수직 흐름과 정상·오류·부분 실패·복구·운영·보안·배포 증적으로 결정한다.

이 표준은 특정 QA 회차의 Count에 종속되지 않으며 모든 Requirement, Defect, OSS Migration, Release와 문서 작업에 영구 적용한다.

최상위 완료 기준:

`cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`

## 2. 상태 정의

| 상태 | 정의 | 완료 집계 |
|---|---|---|
| `미구현` | Product Source 또는 필수 연결이 없음 | 제외 |
| `부분 구현` | 일부 계층만 구현됐거나 Legacy/Dual Primary/Consumer 누락이 존재 | 제외 |
| `미검증` | 구현은 있으나 적용 가능한 필수 환경·Failure Scenario가 실행되지 않음 | 제외 |
| `실패` | Build, Gate, Scenario, Security 또는 Runtime 결과가 Acceptance를 충족하지 못함 | 제외 |
| `재확인 필요` | Source·Evidence·SHA·환경·Owner가 불명확하거나 상충 | 제외 |
| `완료` | 최신 exact SHA에서 본 표준과 Final Target의 모든 적용 조건을 충족 | 포함 |

`개발 상태`와 `검증 상태`를 별도 Column으로 관리한다. Source 수정이 끝났다는 이유로 Runtime 검증까지 완료로 승격하지 않는다.

## 3. 세 가지 완료 수준

### 3.1 개별 Requirement 완료

Final Target의 해당 Requirement 행에 정의된 최소 제품 목표와 필수 완료 증명, 공통 완료 축을 모두 충족해야 한다.

### 3.2 작업 패키지 완료

- 패키지의 Requirement·Defect·Scenario·Decision 행 전체가 유효한 상태를 가짐
- 적용 대상 행의 개발·검증 상태가 모두 `완료`
- ID 누락·중복·orphan이 없음
- 패키지 기준 exact SHA와 Evidence SHA가 일치
- Unresolved Register가 0

작업 패키지 Count는 Canonical Product Requirement Count와 별개다.

### 3.3 GA 완료

Final Target의 **Canonical 162개 Requirement**와 최종 제품화 Gate가 모두 완료되어야 한다. QA 패키지 하나가 끝났다는 이유로 GA를 선언하지 않는다.

## 4. 공통 완료 축

각 Requirement는 적용 가능한 다음 축을 모두 연결한다.

1. 단일 Owner Module과 의존성 방향
2. Public API/SPI/Internal 경계
3. 실제 Product Consumer와 Runtime Lifecycle
4. 정상 기능
5. 입력 오류·권한·경계·대용량
6. timeout·cancellation·interrupt
7. idempotency·duplicate·concurrency
8. multi-instance·lease·fencing·rebalance
9. side effect 전후 결과 불명과 reconciliation
10. retry·restart·reprocess·compensation·rollback/forward recovery
11. DB/File/Broker/Session/Artifact 상태 정본
12. Security·PII·Secret·Trust Boundary
13. 권한·사유·승인·Immutable Audit
14. 운영 조회·제어·metric·alert·runbook
15. resource budget·bounded streaming·cleanup
16. Local/Remote·mixed-version·Vendor compatibility
17. Unit·Contract·Integration·Runtime·Browser·Broker·Fault Evidence
18. OpenAPI·JavaDoc·Guide와 Generated Artifact
19. Legacy/Dual Primary/Dead Code/Stale Evidence 제거
20. exact Source/Artifact SHA와 재현성

`N/A`는 자동값이 아니다. 비적용 이유, 판정자와 대체 검증을 Evidence에 기록한다.

## 5. OSS Migration 완료

OSS 전환은 다음 전체 lifecycle을 통과한다.

```text
Inventory
→ 정확한 Coordinate/Version/License/전이 의존성
→ ADR와 Owner
→ Adapter/Configuration
→ 실제 Vertical Consumer
→ 기능·보안·성능·장애·복구 Parity
→ 전체 Consumer 이관
→ Legacy Source/Bean/Route/Dependency/Artifact 제거
→ POM/BOM/Lock/SBOM/Final Artifact 검증
→ exact-SHA Runtime Evidence
```

Dual-run은 제한된 Migration 기간에만 허용한다.

필수 기록:

- Change ID
- 두 경로의 Owner
- 데이터·상태 동기화
- 결과 불일치 Reconciliation
- Rollback
- 종료 조건과 제거 예정 Commit/Release
- 운영자가 Primary를 식별하는 방법

## 6. Build·Frontend 완료

### Build

- fresh clone
- clean Gradle cache
- settings/includeBuild/project graph
- Java Toolchain
- Plugin/BOM/Lock
- compile/test
- Published POM/source/javadoc
- bootJar/bootWar/static artifact
- LOCAL_DEV/REMOTE/OFFLINE
- final artifact dependency·hash·provenance

### Frontend

- package.json/package-lock exact 일치
- clean `npm ci`
- generated OpenAPI client와 input SHA
- typecheck/unit/build
- 실제 Orval/TanStack Query Consumer
- raw fetch/Legacy Store 제거
- server authorization
- Chromium/Firefox/WebKit
- deep link/session/permission/error/a11y/responsive

문자열 Marker나 dependency 존재만으로 완료 처리하지 않는다.

## 7. 상태기계와 결과 불명 완료

장기·분산 거래는 최소 다음을 구분한다.

- 요청 거부
- 실행 전 실패
- 실행 중 실패
- side effect 없음이 확인된 실패
- 성공
- 중단
- 재시도 가능 실패
- 결과 불명
- 대사 중
- 보상 중
- 수동 확정 필요
- terminal 완료

필수:

- canonical request hash
- idempotency scope
- optimistic/distributed lock
- latest fencing epoch
- attempt
- response loss
- reconciliation source
- retry/compensation eligibility
- 운영 API와 audit

`catch(Exception)` 후 일반 실패로 축약하거나 결과 불명을 실패/성공으로 임의 변환하면 완료가 아니다.

## 8. 필수 Negative·Fault Test

적용 범위에서 의도적 위반 Fixture를 제공한다.

- 금지 Dependency/License/Repository
- 잘못된 Project path·missing includeBuild
- stale/generated/lock drift
- Legacy Import/Bean/Route 재도입
- missing Consumer·Migration·permission
- duplicate·reorder·replay·concurrent update
- stale lease/fencing/late result
- timeout·interrupt·response loss
- DB commit/ACK 전후 process kill
- Kafka rebalance/broker outage/poison message
- stream disconnect·partial write
- symlink·traversal·archive bomb·oversize
- SSRF·header spoof·CSRF·session fixation
- Secret·Token·Session ID·PII 누출
- Artifact hash/signature/state/key 변조
- Git/Source/Artifact identity 불일치

Gate는 위반을 정확한 Failure Code와 안전 상태로 차단해야 한다.

## 9. Evidence 최소 요건

- Repository와 Branch
- exact Source SHA
- clean/dirty 상태
- 실행 시작·종료 시 SHA 재확인
- Artifact SHA-256
- 실행 명령
- 환경·Profile·Topology·Instance 수
- Tool/JDK/Node/DB/Kafka/Browser Version
- 시작·종료 시각
- Exit Code
- Requirement·Defect·Scenario ID
- Expected·Actual·판정
- Report·Log·Trace·DB/Broker 결과 hash
- 민감정보 정제 여부와 scan 결과
- 실패 Root Cause와 남은 Blocker
- Evidence가 현재 Commit에 유효한지

다른 Commit, 다른 Artifact 또는 다른 도구 설정의 결과를 조합해 현재 Evidence로 만들지 않는다.

## 10. 금지되는 완료 패턴

- Interface·DTO·Entity·Migration만 생성
- 화면·Menu만 있고 API가 Mock/고정값
- API는 있지만 실제 DB/Broker/Process 연결 없음
- OSS dependency만 추가하고 Legacy가 Primary
- Feature Flag 기본값이 Legacy 선택
- Starter가 있으나 Product Consumer 없음
- Published POM이 잘못된 대형 의존성을 전파
- 설정값이 실제 Client/Executor에 적용되지 않음
- in-memory test를 Kafka/DB/Session Runtime 증적으로 사용
- Port Open·문자열 Anchor만으로 readiness/기능 PASS
- Static Check 수를 제품 품질 수치처럼 사용
- 단일 Instance 정상 흐름만 검증
- 과거 SHA·Dirty Worktree Evidence 승계
- 실패 로그를 숨기고 재생성된 PASS만 보존
- 수동 절차를 자동 검증으로 기록
- final artifact가 아닌 Source directory만 Supply-chain scan
- 실행하지 않은 검증을 PASS
- README·Guide·보고서만으로 Source 완료 선언

## 11. Release 차단 조건

다음 중 하나라도 존재하면 전체 완료와 Release를 차단한다.

- Canonical Requirement의 비완료 상태
- 작업 패키지 Unresolved
- Build/Frontend/Runtime 실패
- 실제 DB/Kafka/Browser/다중 인스턴스 미검증
- Legacy 또는 Dual Primary
- Source/Artifact/Evidence SHA 불일치
- Security/PII/Credential 누출
- Migration/Rollback/Restore 미검증
- final Artifact signature/SBOM/License/CVE 미검증
- Generator/Generated Domain Drift
- Stale Current Request·Handover·Continuity
- 실행하지 않은 검증의 성공 기록

## 12. 전체 완료 선언 형식

전체 완료를 선언할 때 다음을 함께 제공한다.

- Canonical Requirement 전체/완료/비완료 수
- 작업 패키지별 전체/완료/비완료 수
- 실제 실행한 명령과 환경
- 미실행 항목 0 여부
- exact Source SHA
- final Artifact와 Evidence hash
- DB/Broker/Browser/Topology 범위
- Security·Supply-chain 결과
- Unresolved 0
- 독립 Review 결과
- Commit/Push 수행 주체

QA 회차별 고정 Count는 해당 Package Index/Matrix에서 관리하고 이 영구 표준에 하드코딩하지 않는다.
