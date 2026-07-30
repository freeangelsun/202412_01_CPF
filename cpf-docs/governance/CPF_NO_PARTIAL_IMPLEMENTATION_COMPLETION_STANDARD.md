# CPF 부분 구현 금지 및 완료 판정 표준

## 1. 목적

CPF의 완료 상태는 파일·클래스·테이블·화면·테스트 이름의 존재가 아니라, 실제 사용자·운영자·Runtime이 소비하는 수직 흐름과 실패·복구 증적으로 결정한다. 이 표준은 QA32뿐 아니라 이후 모든 개발 요청에 적용한다.

## 2. 상태 정의

| 상태 | 정의 | 완료 집계 |
|---|---|---|
| `미구현` | 요구 기능의 Product Source 또는 필수 연결이 없음 | 제외 |
| `부분 구현` | 일부 계층만 구현됐거나 Legacy가 Primary로 남음 | 제외 |
| `미검증` | 구현은 있으나 필수 실제 환경 Scenario가 실행되지 않음 | 제외 |
| `실패` | 필수 Gate 또는 Scenario 실패 | 제외 |
| `재확인 필요` | Source·Evidence·SHA·환경이 불명확하거나 상충 | 제외 |
| `완료` | 본 표준의 모든 조건을 최신 exact SHA에서 충족 | 포함 |

`부분 구현`, `미검증`, `재확인 필요`를 완료와 같은 의미로 사용하지 않는다.

## 3. 공통 완료 조건

각 Requirement와 Defect는 적용 가능한 다음 계층을 모두 연결해야 한다.

1. Public API/SPI 또는 사용자 계약
2. Application Service와 정책
3. 실제 Adapter/Provider
4. Database·File·Broker·Session 등 상태 정본
5. Runtime Consumer와 Lifecycle
6. ADM/BZA 또는 운영 API·화면
7. 권한·승인·사유·감사
8. 오류 분류·Timeout·Cancellation·Retry·Unknown Result
9. 재시작·중복·응답 유실·부분 실패 복구
10. 정적 Gate·Unit·Integration·Runtime·Browser Evidence
11. Generated Source와 Distribution Artifact
12. Legacy 제거와 재유입 차단

적용되지 않는 계층은 `N/A` 사유와 검토자를 Evidence에 기록한다.

## 4. OSS Migration 완료 조건

OSS 전환은 다음 10단계를 모두 통과해야 한다.

1. Current Source와 Consumer Inventory
2. 정확한 Version·License·전이 의존성 확인
3. ADR와 Owner·Module 경계 확정
4. OSS Wrapper/Adapter와 실제 Vertical Slice 구현
5. 기능 Parity
6. 보안·성능·장애·복구 Parity
7. 전체 Consumer 이관
8. Legacy Source·Bean·Route·Dependency·Artifact 제거
9. POM/BOM/Lock/SBOM/Final Artifact 검증
10. latest exact SHA Evidence

Dual-run은 이관 기간에만 허용한다. 반드시 Change ID, Owner, 종료 조건, Reconciliation, Rollback과 제거 예정일을 기록한다.

## 5. 금지되는 부분 구현 패턴

- Interface·DTO·Entity·Migration만 만들고 Consumer가 없음
- 화면·메뉴만 만들고 API가 Mock 또는 고정값
- API만 만들고 실제 DB/Broker/Process가 연결되지 않음
- OSS Dependency만 추가하고 기존 자체 구현을 호출
- Feature Flag 기본값이 Legacy를 계속 선택
- 새 모듈을 만들었지만 Published POM이 기존 대형 의존성을 전파
- Error를 `catch(Exception)` 후 일반 메시지로 축약
- Timeout 값을 설정 객체에만 두고 실제 Client에 적용하지 않음
- In-memory Test를 실제 Kafka/DB/Session 증적으로 사용
- Port Open 또는 문자열 포함만으로 Readiness PASS
- Static Scanner가 찾은 Anchor 문자열만으로 기능 완료
- 과거 SHA, 다른 Branch, Dirty Worktree Evidence 승계
- 실패한 검증 로그를 숨기고 후속 재생성 성공으로 덮음
- Manual Step을 자동 검증 완료로 표시

## 6. 필수 Negative Test

각 공통 개발 패턴에는 최소 한 개 이상의 의도적 위반 Fixture가 있어야 한다.

- 금지 Dependency/License/Repository 유입
- Legacy Import/Bean/Route 재도입
- Missing Consumer 또는 Missing DB Migration
- Timeout·Cancellation·Response Loss
- Duplicate·Reorder·Replay·Concurrent Update
- Symlink·Traversal·Zip Bomb·Oversized Input
- SSRF·Header Injection·CORS/TLS 오류
- Secret·Token·Private Key 누출
- Git/Source Identity 부재·불일치
- Artifact Hash/Signature/Version 변조

Gate가 Fixture를 정확한 Failure Code로 차단해야 한다.

## 7. Evidence 최소 요건

- Repository·Branch/Ref·exact SHA·clean/dirty
- 실행 환경과 Tool Version
- 명령 전체 또는 재현 가능한 Invocation
- 시작/종료 시각·Exit Code
- stdout/stderr 또는 Sanitized Log 경로와 SHA-256
- Requirement/Scenario/Defect ID
- Expected·Actual·판정
- Runtime State·DB Row·Broker Offset·Browser Trace 등 결과 정본
- 실패 시 Root Cause와 남은 Blocker
- Artifact/SBOM/NOTICE/Manifest Hash

## 8. 전체 완료 선언

다음을 모두 만족해야 `QA32 전체 완료`를 선언할 수 있다.

- Requirement 62/62 완료
- Mandatory Scenario 202/202 PASS
- Defect 60/60 Closed 또는 정확한 오판 기각 Evidence
- OSS Migration 23/23의 결정 상태 준수
- `부분 구현`, `미검증`, `실패`, `재확인 필요` 0
- 최신 exact SHA CI와 Local/Reproducible Evidence 존재
- 최종 Root-relative Result ZIP 생성·검증
