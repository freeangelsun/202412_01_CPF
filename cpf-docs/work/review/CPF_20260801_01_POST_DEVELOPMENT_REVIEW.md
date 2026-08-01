# CPF 20260801_01 사후 개발 리뷰

## 1. Requirement와 실제 구현 대조

- 최초 개발 SHA: `23babb9140b90e501d6ac715e7b77f55b66198a5`
- 최종 Overlay 적용 기준 SHA: `19dd72b5978f2a3c630943c0fff05bee2d2fed34`
- 작업 중 추가된 `20260801_04`는 README 연결 Guide·Manual·도커 개발환경 범위이며 Overlay 경로 충돌 0건
- 통합 Result Matrix: 115건
- 개발 완료: 113건
- 재확인 필요: 2건 (`QA35-REQ-003`, `QA36-GAP-015`)
- 재확인 2건은 README·README 연결 Manual/Guide 영역으로 사용자가 별도 관리하므로 Source 개발 완료 수치에 포함하지 않았다.
- 검증 완료: 0건
- 미검증: 113건

검증 상태가 0건인 이유는 Source가 미구현이어서가 아니라, 최종 Commit SHA에서 Java 25·Frontend clean build·3DB Runtime·분산 Fault 검증을 실행하지 않았기 때문이다. 정적 검증 결과를 Runtime 성공으로 승계하지 않았다.

## 2. 주요 구현 결과

### ADM/BZA

- ADM 59 Route와 BZA 26 Route의 Component·Menu·Operation 연결
- ADM Source OpenAPI 298 Operation, 인증 제외 Consumer 297건 연결
- BZA Source OpenAPI 84 Operation, 인증 제외 Consumer 76건 연결
- Route Operation Workbench, Path/Query/Body 입력, 위험조치 확인, 오류상태 표시
- Controller Operation ID·Mutation Permission·중복 HTTP 계약 fail-closed 검증

### 운영·보안

- Browser Actor fallback 제거와 서버 소유 Operator 강제
- Mandatory Audit 예약·상세 보강 실패 전파
- Calendar Actor·CAS·실제 before/after Audit·생성 Race 409·writable/권한 UI
- Network Endpoint/CIDR·Private/Metadata Address 차단과 실제 Consumer 연결
- DB-less Product fail-closed, EDU/test Memory·Sample Profile 제한

### Batch·Recovery

- BAT Owner 장애 시 HTTP 200 빈 목록 제거
- Remote Adapter null 응답 fail-closed
- Ghost Lock/Execution Row Lock·만료·Heartbeat·상태·Owner/Fencing 재확인
- 정확히 1건 변경, Audit, 실패 시 Transaction Rollback

### Notification·Incident

- 전용 DLQ 상태·조회·재처리 Consumer
- Incident 상태전이 Guard, 미존재 404, CAS·Active-Key·멱등 충돌 409
- Oracle·PostgreSQL·MariaDB V92 Install·Rollback·Checksum·핵심 제약조건

### Generator·EDU·Continuity

- Generator Lifecycle 정본 경로, 3DB Vendor, 사용자 변경 보호, parity 계약
- EDU Canonical 162 Coverage와 Product Profile Isolation
- Legacy 원장 5종의 3,679개 고유 ID를 누락 없이 Canonical 162에 연결
- README·연결 Manual을 완료 근거로 사용하지 못하도록 보호 Gate 추가

## 3. 실제 실행 검증

- Python Gate Unit Test: 144건 통과
- Requirement Trace: 115행, 개발 완료 113행의 Source·Consumer·Test 파일 존재 확인
- Java Source Syntax: 98개, 오류 0
- Frontend TypeScript/Vue Source Syntax: 112개, 오류 0
- ADM/BZA OpenAPI Controller Coverage: 298/84 일치
- ADM/BZA Source Operation Consumer Closure: 297/76 연결
- DB Lifecycle Static Contract: 3 Vendor, 7 Stage
- Generator Lifecycle: 3 Vendor, 10 Stage Static Contract
- Supply-chain Static Contract: Included Project 25, Artifact 32, Approved OSS 23
- Source/Contract Gate: 47/47 PASS
- Overlay Hygiene: 373개 파일, 생성물·고신뢰 Secret·줄 끝 공백 0

## 4. 실행하지 못한 검증

- Java 25 Fresh Cache 전체 Build·Test·Publication
- ADM/BZA clean `npm ci`, lint, typecheck, unit, production build
- Playwright Chromium·Firefox·WebKit
- Oracle·PostgreSQL·MariaDB Fresh Install·Upgrade·Rollback·Reapply·Backup/Restore
- Kafka·Redis·Batch·Scheduler·Gateway·Agent Runtime
- 다중 인스턴스·Process Kill·Response Loss·Unknown Result·Recovery
- SBOM/Vulnerability/License/Artifact Signature Runtime
- 최종 적용 Commit exact-SHA·clean Working Tree Evidence

## 5. 새로 발견·수정한 Gap

- 항상 실패할 수 있던 CI OpenAPI Coverage 인자 누락
- ADM 존재하지 않는 승인 API 호출
- Incident 3DB Schema 누락과 상태전이·404/409 미흡
- Product Calendar Actor `SYSTEM` fallback
- Batch 빈 목록 False Success와 Ghost 종료 SQL 과소 조건
- ADM 기존 Mixin·직접 URL 우회와 BZA Public Operation 미연결
- Requirement 완료 행의 경로 문자열만 검사하던 False Green

## 6. Repository Hygiene

README·연결 Manual·Guide, `build`, `.gradle`, `node_modules`, `dist`, `coverage`, Playwright 결과, log/tmp, Python cache, 중간 ZIP은 Overlay에서 제외한다. 추적 파일 삭제는 수행하지 않았으며 Delete Manifest는 `NONE`이다.
