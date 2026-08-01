# CPF 20260801_01 통합 개발 사전 리뷰

## 1. 기준과 범위

- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- 최초 개발 시작 SHA: `23babb9140b90e501d6ac715e7b77f55b66198a5`
- 최종 재기준 master SHA: `19dd72b5978f2a3c630943c0fff05bee2d2fed34` (`20260801_04`, 보호 문서·도커 환경 변경과 Overlay 경로 충돌 0건)
- 통합 개발 원장: SELF 30건 + QA36 Active Gap 85건
- 상위 추적축: Canonical Requirement 162건 + Mandatory Scenario 2,754건
- Legacy Continuity: 과거 Requirement·Scenario 원장의 모든 고유 ID를 보존하고 Canonical 162에 연결한다.

README와 README에서 연결되는 Manual·Guide는 별도 산출물 작업에서 미래 완성 상태를 가정해 작성되므로 이번 개발의 수정 대상과 완료 판단 근거에서 제외한다.

## 2. 해결할 Requirement와 공통 Defect

1. ADM/BZA Route가 실제 Component·Operation·Permission과 연결되지 않거나 Silent Fallback을 허용하는 결함
2. Controller Mutation Permission·Operation ID·Operator Trust·Audit 실패 전파 누락
3. Batch Owner 장애를 빈 목록으로 성공 처리하거나 Ghost Lock/Execution을 안전하지 않게 종료하는 결함
4. Calendar Actor·CAS·before/after Audit·중복 생성 충돌·Frontend 영업일 계산 누락
5. Notification DLQ·Incident 상태전이·404/409·3DB Schema/Migration 누락
6. DB-less Product fallback과 EDU/test Profile Isolation 누락
7. OpenAPI Source·Generated Client·실제 Consumer Drift 및 직접 URL 우회
8. Generator Lifecycle 경로·3DB Vendor parity·사용자 변경 보호·재생성 멱등성 누락
9. Network Endpoint/CIDR/Metadata·Private Address 차단과 실제 Consumer 연결 누락
10. Requirement→Source/API/SQL/Consumer/Test/Evidence 역추적과 exact-SHA 검증 분리 부족

## 3. Owner Module과 경계

- `cpf-core`: topology-independent Network·Archive 등 기술 핵심 계약만 소유
- `cpf-common`: Calendar와 고객 업무 공통 Public API·Persistence Adapter 소유
- `cpf-admin`: 플랫폼 운영 Controller·Service·Permission·Audit·ADM UI 소유
- `cpf-biz-admin`: 고객 업무 관리자 API Consumer와 BZA UI 소유
- `cpf-batch`: Batch·Worker·Scheduler·Control Server·Ghost Recovery Runtime 소유
- `cpf-tools`: Generator·3DB Canonical SQL·Migration·검증 Gate 소유

외부 Module이 Internal Package를 직접 참조하지 않고, Public API/SPI·실제 Consumer·Owner Runtime을 분리한다.

## 4. Consumer와 의존성 방향

- ADM/BZA 화면은 Generated Operation Catalog를 통해 실제 Backend Operation을 소비한다.
- ADM은 BAT Runtime을 직접 구현하지 않고 BAT Owner API를 fail-closed Proxy로 소비한다.
- Generator는 `cpf-common` Public API를 사용하고 `com.cpf.core.common.*` 직접 의존을 금지한다.
- Oracle·PostgreSQL·MariaDB Query Pack은 Canonical Schema와 동일 의미를 유지한다.
- Network 정책은 Gateway, Batch Outbound, Host Agent 실제 Consumer가 사용한다.

## 5. 회귀·운영 위험

- 동일 JVM과 분리 WAS 양쪽에서 Operator·Audit·Permission 계약이 유지돼야 한다.
- 다중 인스턴스에서는 CAS·Fencing·Owner·변경 건수 검증을 생략하지 않는다.
- 부분 실패·Response Loss·Unknown Result는 성공 또는 빈 결과로 축소하지 않는다.
- 위험 조치는 권한·사유·승인·감사·결과 추적을 요구한다.
- 민감정보·Secret·원문 개인정보는 Evidence와 화면 결과에 남기지 않는다.

## 6. DB·Migration·Generator 영향

- 공식 Vendor는 Oracle, PostgreSQL, MariaDB 3종만 사용한다.
- Notification/Incident V92 Install·Rollback·Checksum과 Runtime Query를 3종에 동일 의미로 반영한다.
- Ghost Lock/Execution SQL은 Row Lock·만료·상태·Owner/Fencing·정확히 1건 변경을 보장한다.
- Generator Create·Database·Remove·Lifecycle Script와 Golden Template parity를 함께 검증한다.

## 7. 구현 순서와 중복 방지

1. Requirement·Defect를 공통 원인 단위로 통합
2. Owner Backend·SQL 계약 수정
3. OpenAPI·Generated Operation Catalog 재생성
4. ADM/BZA 실제 Consumer·위험조치·오류상태 연결
5. Negative Test와 Source Gate 보강
6. Requirement·Scenario·Legacy Continuity 역추적
7. 저비용 Gate를 한 번 통합 실행
8. Java 25·Frontend·3DB·Runtime·Supply-chain은 환경이 있는 exact SHA에서 각 Stage를 한 번 실행

## 8. 완료 조건

- 개발 대상 113건에 실제 Source·Consumer·Test 경로가 존재한다.
- README·연결 Manual 2건은 개발 완료로 위장하지 않고 별도 작업으로 분리한다.
- ADM 59 Route, BZA 26 Route, Controller Permission, OpenAPI Source Coverage가 통과한다.
- 3DB Static Lifecycle, Generator, Network, Audit, Operator, Calendar, Batch Recovery Gate가 통과한다.
- 실행하지 않은 Runtime 검증은 `미검증`으로 유지하고 환경·명령·사유를 Evidence에 기록한다.
