# CPF_GAP_MATRIX

## 1. 기준

- 기준 Branch: `master`
- 기준 중간 Commit: `e35b7a0b4f2f7c94fb42f6767dc09c73ca2a3549`
- 직전 비교 Commit: `b32cc8ae5ccb798f3b1e6720c678e3838d01b460`
- 현재 상태: **중간 Push 재검수 필요**
- 중간 Commit의 작업자 완료 주장과 자동 생성 Evidence는 검수 확정이 아니다.

## 2. 최우선 Gap

| ID | 상태 | Gap | 완료 조건 |
|---|---|---|---|
| STRUCT-01 | 재확인 필요 | legacy Module명과 `cpf.*` Package 잔존 | 공식 `cpf-*`, `com.cpf.*` migration과 전체 build/runtime |
| STRUCT-02 | 재확인 필요 | Root project name과 Repository Root 비정본 | 목표 명칭·공식 문서 위치·경로 보정 |
| STRUCT-03 | 재확인 필요 | SystemCode와 DomainName 혼용 | Generator·Config·Route·SQL까지 분리 |
| GEN-01 | 재확인 필요 | Generator 변경의 실제 parity 미확정 | ACC lifecycle, 4 DB Vendor, capability absence |
| AI-01 | 재확인 필요 | AI 전용 PoC·Dependency 잔존 가능성 | 전체 검색·제거·범용 기능 재소유 |
| AUDIT-01 | 재확인 필요 | `e35b7a0` 문서 완료와 실제 Source 정합성 미검수 | 요구→구현·구현→요구 양방향 검수 |
| EVID-01 | 재확인 필요 | 대형 generated Evidence가 완료 근거로 혼재 | 파생 산출물 분리, Runtime Evidence 재확인 |
| ROOT-01 | 재확인 필요 | Root에 관리 문서와 legacy Module 산재 | 공식 Repository 구조 정리 |
| SQL-01 | 재확인 필요 | 중복 SQL 삭제와 canonical owner 영향 | 신규 설치·migration·rollback 실검증 |
| LOG-01 | 부분 구현 | 파일 로그 smoke가 최신 경로 보정 중 중단 | 실제 파일/DB 로그 parity 성공 |
| BAT-01 | 재확인 필요 | Worker 구현과 2-process 성공 주장 미검수 | lease/fencing/drain/takeover 실검증 |
| UI-01 | 재확인 필요 | ADM/BZA 현대화와 packaging 주장 미검수 | lint/typecheck/test/build/archive/browser |
| COMPAT-01 | 미구현 | Module·Package rename 호환·migration 계획 | consumer 영향·rollback·compat adapter |
| PROD-01 | 부분 구현 | 설치·배포·공개 package 제품화 미완결 | 독립 설치·upgrade·rollback·release 검증 |

## 3. 중단 작업 재검수 목록

- requirement/implementation traceability
- 전체 계층 taxonomy
- ACC lifecycle
- BAT Worker
- ADM/BZA frontend
- SQL canonicalization
- DB Vendor generator
- MariaDB full install
- OpenAPI/runtime
- file/DB logging
- 외부연계 failure/recovery
- EDU coverage
- security/supply-chain
- repository hygiene

모든 항목은 실제 최신 Source에서 재실행하기 전까지 `재확인 필요`다.

## 4. 133개 Requirement 상태 정책

기존 Matrix의 완료 표시는 자동 승계하지 않는다.

- 실제 Source·Consumer·Runtime·Evidence가 모두 확인된 항목만 `완료`
- 구현은 있으나 일부 경계·복구·운영·문서가 빠지면 `부분 구현`
- 실행 환경이 없어 직접 확인하지 못하면 `미검증`
- 기능이 없으면 `미구현`
- 실행 실패면 `실패`
- 중간 Commit의 주장만 있으면 `재확인 필요`

상세 133개 Catalog는 `CPF_FINAL_TARGET_REQUIREMENTS.md`를 따른다.
