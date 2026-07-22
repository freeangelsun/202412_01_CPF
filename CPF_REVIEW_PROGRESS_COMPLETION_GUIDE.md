# CPF 검수·진행·완료 판정 Guide

## 1. 목적

이 문서는 CPF 작업을 검수하고 진행률과 완료 여부를 판정하는 공통 기준이다. 작업자 보고나 문서 표기를 그대로 신뢰하지 않고 최신 Git과 실행 결과를 기준으로 한다.

## 2. 기준 자료 우선순위

1. 최신 `master` Source, Test, SQL, Script, Config와 Frontend
2. `CPF_FINAL_TARGET_REQUIREMENTS.md`
3. 현재 작업 요청서와 Architecture Decision
4. 실제 Runtime·DB·Browser Evidence
5. Gap Matrix와 Evidence Index
6. Codex 완료 보고

Codex 보고와 실제 Git이 다르면 실제 구현을 기준으로 판정한다.

## 3. 허용 상태

- **완료**: 적용 가능한 제품 완료 묶음과 최신 실행 Evidence 확인
- **부분 구현**: 일부 Source/연결/Test는 있으나 필수 묶음 누락
- **미구현**: 요구 기능 또는 책임 구조 없음
- **미검증**: 구현 가능성은 있으나 필요한 실행 확인 없음
- **실패**: 실행·정합성·보안·회귀 검증에서 실패
- **재확인 필요**: 접근 권한, 환경, 정책 결정 또는 근거 부족

그 외 상태 문자열과 임의 퍼센트 완료 표기는 사용하지 않는다.

## 4. 요구사항별 검수 축

각 Requirement를 다음과 대조한다.

- 실제 Source와 계층 연결
- Module/Package Owner와 의존성 방향
- Public API, SPI와 Internal 경계
- 실제 Consumer와 운영 Consumer
- 표준 Header, Error, Validation과 권한
- SQL, Migration, Empty Install, Upgrade와 Rollback
- 정상, 오류, 경계와 부분 실패
- 멱등성, 동시성, Retry, Unknown과 Recovery
- Multi-instance, Lease, Fencing와 Takeover
- Security, Approval, Audit와 Masking
- 운영 조회, 제어, 통계와 Alert
- OpenAPI, JavaDoc, EDU와 Guide
- Unit, Integration, Runtime와 Browser
- 최신 Commit과 실행 환경이 명시된 Evidence
- 기존 성공 기능 회귀
- Dead Code, Stale Evidence와 Garbage

비적용 항목은 생략하지 않고 이유와 대체 검증을 기록한다.

## 5. 완료로 인정하지 않는 근거

- File, Class, Table 또는 Package 존재
- Static Search 또는 Architecture Inventory만 통과
- Swagger/OpenAPI 노출
- 일부 Unit Test 통과
- 설정 문자열 또는 Sample 등록
- 과거 PC·과거 Commit Evidence
- Codex의 성공 보고
- 문서의 `완료` 표기
- DB Table 생성만 성공
- UI 메뉴가 보이는 것

## 6. 양방향 추적

### 요구사항 → 구현

```text
Requirement
→ Owner Module/Package
→ Source/API/SPI
→ SQL/Migration/Seed
→ Test/Runtime/Browser
→ Evidence
```

### 구현 → 요구사항

```text
Source/Table/API/UI
→ Requirement ID
→ Owner
→ Actual Consumer
→ Operation/Recovery
```

Owner, Consumer 또는 Requirement가 없는 구현은 Dead Code, 중복 또는 잘못된 배치 후보로 판정한다.

## 7. Evidence 등급

- E1 Static: Source Scan, Inventory
- E2 Build/Test: Compile, Unit, Integration
- E3 Runtime: Module Boot, API, DB
- E4 System: Multi-module, Browser, Broker, Multi-instance
- E5 Recovery: Fault Injection, Unknown, Failover, Rollback

상용 완료에는 요구 특성에 맞는 등급이 필요하다. E1만으로 Runtime 요구를 완료 처리하지 않는다.

## 8. 작업 완료 후 전수검수

1. 최신 HEAD와 전체 Commit Diff
2. 요청서와 완료 보고
3. 최상위 목표 전체
4. Source/Test/Resource/SQL/Migration/Script/Config/Frontend/OpenAPI/EDU/Deploy/Evidence
5. 요구사항 단위 판정
6. 기존 기능 회귀
7. 중복, Dead Code, Stale Evidence와 Garbage
8. 상용 제품 주변 Gap 선제 발굴
9. Gap Matrix, Evidence Index와 다음 요청 갱신

## 9. 완료 금지 조건

- Secret 또는 개인정보 원문 포함
- Historical Migration 무단 변경
- 다른 Schema를 Pattern으로 삭제
- 실행하지 않은 검증을 성공 기록
- Admin이 Owner DB를 직접 갱신
- Core 역방향 의존 또는 순환
- BZA가 온라인 Runtime의 필수 채번 의존점
- Stale Evidence를 최신 근거로 사용
- 실패한 Gate를 근거 없이 비활성화
- 사용자 승인 없는 Commit/Push/Branch
