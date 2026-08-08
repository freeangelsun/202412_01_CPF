# CPF 고객사 README·매뉴얼·설계 산출물 작성 및 관리 표준

> Repository: `freeangelsun/202412_01_CPF`  
> Branch: `master`  
> 기준 Commit: `b2da6bd720d1a8506db6bddf5d2e35feb9dca964`  
> Canonical Requirement Count: **180개**  
> 최상위 정본: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`

## 1. 목적
공식 사용자 문서는 CPF를 처음 접한 대상자가 다른 사람 설명이나 Source 역분석 없이 자기 업무를 끝낼 수 있도록 작성한다. 문서의 목적은 작업 일지, QA 결과 요약, 기능 목록 나열이 아니다.

## 2. 세 가지 사실 표기
- **CURRENT SOURCE**: 최신 master에서 직접 확인한 실제 Class/API/Route/Property/SQL/Test/경로.
- **PRODUCT CONTRACT**: 완료된 CPF 제품이 사용자에게 보장해야 하는 선택·실행·실패·복구 계약. 현재 구현량을 이유로 범위를 축소하지 않는다.
- **REFERENCE**: 사용자가 복사·응용 가능한 완성 기준 코드/설정/절차 예시. 실제 CPF 내부 Symbol로 오인시키지 않는다.

현행화 시 실제 Symbol이 달라지면 CURRENT SOURCE만 교체하고, PRODUCT CONTRACT와 교육 흐름을 임의로 얇게 만들지 않는다.

## 3. 공식 사용자 문서
README + Guide 8종 PDF/DOCX + 설계 산출물 5종 PDF/DOCX만 공식 사용자 문서로 유지한다. Quick Start, EDU, Reference, Case, Troubleshooting, Runbook은 Owner 매뉴얼 내부에 포함한다.

## 4. 내용 우선순위
`본문 완결성 → 실제 코드/설정/SQL/명령/화면 → 오류·복구 → Test/운영 인계 → 링크/북마크/조판` 순서로 만든다. 링크, 페이지 수, 표 수, 질문 라우팅 수는 본문 품질을 대체하지 못한다.

## 5. 기능별 필수 계약
각 기능은 적용 가능한 범위에서 목적, Persona, 선택/비선택 기준, Owner, Consumer, Public API/SPI/Internal, 파일 경로, 전체 Source 또는 충분한 구현 단위, Config, SQL, 입력/기본값/범위, 실행 명령, 정상 출력, 상태 변화, Log/Metric/Trace, Validation, 동시성, Timeout, 응답 유실, 부분 실패, Retry/Restart/Reprocess/Reconcile/Compensation/Rollback, Permission/Data Scope/Masking/Reason/Approval/Audit, Test, ADM 확인, 운영 인계를 제공한다.

## 6. 코드 표준
코드가 필요한 기능은 `파일 경로 → 코드 블록 → 설명 → 변경 가능 영역 → 실패 시 결과 → Test` 순서로 제시한다. 장식용 몇 줄짜리 코드만으로 기능을 완료 처리하지 않는다. CURRENT SOURCE를 인용할 때는 실제 Repository 경로를 정확히 적는다.

## 7. 매뉴얼별 완료 기준
- 00: 의사결정자가 범위/비범위/Architecture/Topology/Profile/Module/도입 순서를 결정 가능.
- 01: 개발자가 Generator부터 API/Domain/DB/Transaction/Integration/Security/Test/운영 인계까지 수행 가능.
- 02: 배치 개발자가 Job/Step/Chunk/Partition/Scheduler/Worker/Restart/Reprocess/Reconcile까지 수행 가능.
- 03: 고객 Owner Query/Command를 ADM Backend/OpenAPI/Generated Client/Vue 화면까지 연결 가능.
- 04: 모든 실제 ADM Route를 검색·판단·조치·승인·복구·감사까지 운영 가능.
- 05: 설치·Property·DB·Broker·기동·배포·Observability·Backup/Restore·Upgrade/Rollback·DR을 수행 가능.
- 90: 조직·직원·Role·Permission·Data Scope·결재·세션·Attachment·Audit를 운영 가능.
- 91: Gateway Route/Target/Security/Publish/ACK-NACK-PARTIAL/LKG/Rollback/Scale-out/Drift를 운영 가능.

## 8. 현행화
Public API/SPI, Starter/Profile, Route/Menu/Permission/Operation, Property, SQL/Migration, State/Error, Script/Command, Requirement가 변경되면 최신 master 전체 영향을 다시 확인한다. 부분 수정 전 Owner 문서 전체의 관련 절이 여전히 일관되는지 재검토한다.

## 9. 100점 제출 Gate
사용자가 지정한 필수 품질 규칙에 따라 다음 산출물 제출 전 평가 항목은 각각 내부 기준 100점이어야 한다: 문서 체계/정본, README, 사실성, 업무 수행 가능성, Source 역분석 불필요성, 책형 흐름, 실제 코드/설정/SQL/API/명령/Test/Fault/복구, 각 매뉴얼 실무 가치, 설계 산출물 깊이, 양적·질적 충분성, 양방향 일치, 고객 인수 가치, DOCX/PDF/링크/접근성/조판.

하나라도 미달하면 완료 ZIP으로 제출하지 않는다. 단, 이 Gate는 **문서 품질 Gate**이며 제품 Runtime Qualification PASS를 의미하지 않는다.

## 10. 렌더·인수
DOCX와 PDF를 동기화하고 전 페이지 렌더 QA를 수행한다. 최종 ZIP은 Repository Root 상대경로를 유지하며 wrapper folder를 만들지 않는다. Commit/Push는 사용자 승인 없이는 수행하지 않는다.
