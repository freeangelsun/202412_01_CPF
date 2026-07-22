# CPF 검수·진행·완료 판정 Guide

## 1. 허용 상태

- 완료
- 부분 구현
- 미구현
- 미검증
- 실패
- 재확인 필요

## 2. 완료의 최소 근거

요구사항 하나가 완료되려면 적용 대상에 따라 다음이 연결돼야 한다.

```text
Requirement
→ Owner Module
→ Public API/SPI
→ Source
→ Consumer
→ SQL/Migration
→ Security/Audit
→ Operations
→ Unit/Integration
→ Runtime/Browser
→ Guide
→ Evidence
```

## 3. 완료 근거가 아닌 것

- Class 존재
- Interface 존재
- Table 존재
- Swagger 노출
- Static Scan PASS
- 단순 Heartbeat
- 설정 문자열
- Sample 등록
- 과거 Evidence
- Codex 성공 보고
- 문서의 완료 표시
- Quality Gate가 Runtime을 포함하지 않은 상태의 PASS

## 4. 검수 순서

1. 최신 master
2. 작업 요청
3. Codex 보고
4. Final Target
5. 전체 Diff
6. Source·Resource·SQL·Script·Frontend
7. Consumer
8. Runtime
9. Evidence
10. Regression
11. Garbage
12. 신규 Gap

## 5. 양방향 추적

### Requirement → Implementation

- Source
- API
- SQL
- Test
- Runtime
- Evidence

### Implementation → Requirement

- Requirement ID
- Owner
- Consumer
- Runtime Use
- Operations
- Security
- Retention

## 6. DB 검수

Table마다:

- Schema
- Owner
- DDL
- Index/Constraint
- Repository
- Service
- API
- UI
- Consumer
- Row Count
- PII
- Retention
- Migration
- Rollback
- Runtime

실 DB 미접속이면 삭제 완료 처리하지 않는다. 빈 DB 신규 정본에서는 Owner 없는 Object를 생성하지 않는다.

## 7. Evidence 등급

### E1 Static

Search·Inventory·AST·Architecture.

### E2 Build

Compile·Unit·Package.

### E3 Integration

DB·Container·Contract.

### E4 Runtime

실제 Process·HTTP·Broker·File.

### E5 Operations

Admin·Control·Recovery·Browser.

`완료`에는 기능 성격에 맞는 E3~E5가 필요하다.

## 8. Stale 판정

다음 중 하나면 Stale:

- Evidence Commit != Final Commit
- Source/API/SQL 변경 후 재실행 안 함
- 다른 Profile
- 다른 DB Schema
- 실행 명령 없음
- Exit Code 없음
- 환경 없음
- 원문 Log 없음

## 9. 회귀 검수

- 기존 성공 API
- Local/Remote
- Header
- Transaction ID
- Log
- Security
- DB Install
- Generator
- Batch
- ADM/BZA
- OpenAPI
- Deployment

## 10. 최종 Gate

- Worktree Clean
- Full Build
- Empty Install
- Reinstall
- Upgrade/Rollback
- Runtime
- Browser
- Security
- Docs
- Evidence
- No Garbage
- No Secret
- No Stale Name
