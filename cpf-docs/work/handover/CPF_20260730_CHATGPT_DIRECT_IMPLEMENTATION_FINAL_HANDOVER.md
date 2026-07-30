# CPF ChatGPT Direct Implementation Final Handover

## 1. 기준

- Repository: `https://github.com/freeangelsun/202412_01_CPF`
- Branch: `master`
- Overlay 기준 SHA: `fae7aa9643f646db4bcbcf665d13b8f3b809e8c8`
- 작업 범위: Requirement 405건 + Scenario 90건 통합 요청의 확인 결함 및 공통 기반 보완
- 산출물 상태: **개발 Overlay 작성 완료 / Repository exact-SHA 통합 검증 미검증**

## 2. 구현된 주요 변경

### Gateway

- Versioned Server Group, Member, Binding, Apply ACK, Connection Test 계약
- HTTP/HTTPS/gRPC/TCP/LOCAL 호출 표현과 Route 호환 계약
- Round Robin, Weighted Round Robin, Rendezvous Hash, Priority Failover, Least Load
- Active/Passive Health, Hysteresis, STALE, RECOVERING, Drain, Maintenance
- Transaction/Attempt 원장과 ADM 구조화 운영 화면

### Runtime Policy와 Logging

- Durable Policy Event, Consumer Claim, Lease/Fencing, ACK, Retry, Error 상태
- ADM과 Gateway Adapter의 Publish·Claim·ACK·Status 전체 구현
- 로그 정책 적용 상태와 Gateway Instance Drift 조회
- Raw JSON 중심 화면을 구조화된 조회·입력 화면으로 교체

### Batch

- `CpfParameterSchema` 기반 Typed Parameter, Secret Reference, Code Reference, Validation
- Job Definition Version, Parameter, Dependency, Approval, Publish, Audit
- File Stability Window, Marker, Checksum, Claim, Lease/Fencing, Restart Scan
- Approved Shell Hash/Signature, Parameter File, 출력 제한·마스킹, Process Tree 종료
- Timeout·Retryable Exit·Unknown Result 분류

### ADM/BZA

- ADM 5개 운영 정보구조와 Gateway·Batch·Log 운영 화면
- BZA Recursive Menu Tree, Parent 이동, Cycle 방지, 영향 분석, Optimistic Lock, 감사
- Service Registry Typed DTO/API/UI

### DB·Generator·Gate

- Canonical Table 173개 정본과 MariaDB/PostgreSQL/Oracle Source DDL 동기화
- V74 Gateway, V75 Runtime Policy, V76 Batch Job Definition Migration/Rollback
- Clean Install Bundle 3 Vendor 동기화
- Generated Domain Manifest에 Registry 후보·Endpoint·Health·Gateway 기본 비공개 정책 추가
- Runtime Query Inline SQL Gate, Raw JSON UI Gate, Exact-SHA Work Context/Evidence Gate 강화

## 3. 실제 수행한 검증

다음은 현재 작업 환경에서 직접 실행했다.

- Core API·Contract·Gateway·Batch Worker·Batch Control·ADM·BZA 변경 Java 구문/계약 컴파일
- Target Selection과 Health Hysteresis 동작 검증
- File Restart Scan, Claim fencing 증가, Shell Process Tree Fail-closed 검증
- TypeScript/Vue Script 구문 검증
- JSON 파싱과 UTF-8 기본 검증
- 신규 13개 Table의 3 Vendor Column/FK/Rollback Parity
- Clean Install Bundle에 173개 Table 및 신규 13개 Table 포함 여부

## 4. 실행하지 않은 검증

현재 실행 환경에는 전체 Repository와 `pwsh`, 공식 DB Runtime, Redis, Browser가 없어 다음은 성공으로 기록하지 않았다.

- Root Gradle `clean test assemble`
- ADM/BZA Typecheck·Lint·Vitest·Production Build
- Root `qualityGate`와 `verify-cpf-final-completion.ps1`
- MariaDB 실제 Upgrade/Rollback/Reapply/Clean Install
- PostgreSQL·Oracle 실제 Lifecycle
- Redis 장애·복구·다중 인스턴스
- Gateway/Batch Runtime·Browser E2E
- 495개 Matrix와 2,715개 Ledger exact-SHA 폐쇄

## 5. 다음 작업자 필수 절차

1. `cpf-tools/scripts/apply-cpf-20260730-final-overlay.ps1` 실행
2. `git diff --check`, 변경 목록과 삭제 목록 검토
3. 사용자 직접 Source Commit
4. Clean Source SHA에서 전체 검증과 Evidence 생성
5. 실패 항목은 Source·SQL·Test·문서를 함께 수정
6. Matrix/Ledger는 실제 Evidence가 있는 항목만 `완료`로 변경
7. 사용자 직접 Push
8. Codex 독립 검수

## 6. 보호할 결정

- 공식 DB Vendor는 Oracle, PostgreSQL, MariaDB만 유지한다.
- Generated Domain은 하나의 Golden Template으로 생성한다.
- EXS·ACC 등 업무 Domain을 고정 공식 Module로 되돌리지 않는다.
- Gateway는 선택 제품이지만 사용하는 경우 인증·권한·감사·Fail-closed를 적용한다.
- Batch Runtime Ownership은 `cpf-batch`에 유지한다.
