# CPF 고객사 README·매뉴얼·설계 산출물 작성 및 관리 표준

> Repository: `freeangelsun/202412_01_CPF`  
> Branch: `master`  
> Repository 기준 Commit: `f6d7080c5a14b7dd7595093f9497470169e18d80`  
> Product Source 기준: `f0aa49f29cba3cfd6ae12b0ddd4e118d05fff16c`  
> 최상위 요구사항 정본: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`

## 1. 목적
CPF 사용자 문서는 **제품 완료 상태에서 사용자가 실제 업무를 끝낼 수 있는 완성 기준 문서**로 작성한다. 현재 개발 진행률이나 QA 일지는 사용자 매뉴얼의 본문 목적이 아니다. 사용자는 어떤 표현으로 질문하더라도 Source 역분석 없이 `질문 의도 → 기능 → 정본 문서 → 정확한 절 → 실행 → 정상/실패 판정 → 복구 → Evidence`로 이동할 수 있어야 한다.

## 2. 사실 우선순위
1. 최신 master의 실제 Source·SQL·API·Config·Frontend·Script·Test
2. 최상위 제품 요구사항과 Architecture/Specification
3. 공식 사용자 문서

Source에 없는 API·Class·Property·화면·Permission·State를 만들지 않는다. 다만 사용자 문서는 구현 진행상태 보고서가 아니라 **완료된 제품이 반드시 만족해야 하는 최종 사용 계약**을 기술한다. 구현이 문서 계약을 충족하지 못하면 Source/Test/SQL/API를 보완해야 하며 문서 범위를 축소해 맞추지 않는다.

## 3. 공식 사용자 문서
README + Guide 8종 PDF/DOCX + 설계 산출물 5종 PDF/DOCX만 공식 사용자 문서로 유지한다. 별도 Quick Start/FAQ/Reference/Case/Troubleshooting/Runbook 문서를 추가하지 않는다. 필요한 내용은 Owner Document 내부에 포함한다.

## 4. 질문 라우팅 4축
### 4.1 Persona
의사결정자/제안자, PM, 아키텍트, 온라인 개발자, 배치 개발자, ADM 연동 개발자, ADM 운영자, 플랫폼/SRE/DBA, 보안/감사, BZA 관리자, Gateway/API 운영자, 장애 대응자, 인수/검수자.

### 4.2 Lifecycle
검토, 선택, 설치, Bootstrap, 설정, 개발, 시험, 배포, 관측, 운영, 복구, 감사, Upgrade, 인계/폐기.

### 4.3 Feature
제품 가치/범위/Topology/Module/Starter/Generator/Public API/Paging/Transaction/Idempotency/Remote/Messaging/HTTP/TCP/전문/File/Notification/Security/DB/Batch/ADM/BZA/Gateway/Observability/Runtime Control/Feature Flag/Approval/DR 등 실제 제품 Capability를 사용한다.

### 4.4 Intent
무엇/왜/언제/어디/선택/비선택/선행조건/입력/기본값/범위/순서/정상 결과/상태/오류/Timeout/동시성/중복/부분 실패/Retry/Restart/Reprocess/Reconcile/Compensation/Rollback/권한/보안/감사/로그/Metric/Trace/Test/Source/비교/인계/완료 판정.

## 5. Owner Document 단일 정본
- 제품 가치·범위·기능 Navigator·독자/업무단계 지도: `00_프레임워크안내`
- 온라인 개발·Starter/Profile·Generator·Public API·Paging·Transaction·Messaging·외부연계: `01_개발자매뉴얼`
- Batch 의미/개발/Restart/Reprocess/Reconcile: `02_배치개발매뉴얼`
- 고객 업무의 ADM 연동 개발: `03_ADM개발자매뉴얼`
- ADM 실제 Route 운영: `04_ADM운영자매뉴얼`
- 설치/Bootstrap/Config/DB/기동/배포/관측/Backup/Restore/DR/Runbook: `05_플랫폼운영매뉴얼`
- BZA: `90_BZA매뉴얼`
- Gateway: `91_Gateway매뉴얼`
- Architecture/Trade-off: `아키텍처설계서`
- 제품 계약/Capability Reference: `기술사양서`
- Coding/API/Transaction/Security/Test 규칙: `기술표준서`
- DB Ownership/Naming/Migration/Vendor lifecycle: `데이터베이스표준서`
- Deliverable/Artifact/Evidence/인수 지도: `산출물목록`

## 6. 중복 금지와 교차참조
동일 상세 설명은 한 정본만 소유한다. 다른 문서는 필요한 맥락과 `문서명 + 절 제목`을 제공하고 가능한 경우 PDF 상대링크를 건다. Transaction 코드/오류 Matrix, Batch Restart, Gateway LKG, FileLog Recovery 같은 상세 표를 여러 문서에 복사하지 않는다.

## 7. 기능별 완성 기준
각 기능은 다음을 모두 답해야 한다: 목적, 대상 역할, Owner Module, 실제 Consumer, Public API/SPI/Internal, Source/Config/SQL/화면, 선행조건, 입력/기본값/범위, 전체 흐름, 단계별 절차, 정상 결과, 상태 변화, Log/Metric/Trace, 오류/동시성/Timeout/응답 유실/부분 실패, Retry/Restart/Reprocess/Reconcile/Compensation/Rollback, Permission/Data Scope/Masking/Reason/Approval/Audit, Test, ADM 확인, 운영 인계, 완료 판정 Gate.

`지원한다/관리한다/처리한다/등록한다/확인한다`로 끝내지 않는다. 누가 어떤 권한으로 어디에 무엇을 입력하고 어떤 상태로 바뀌며 실패하면 무엇으로 정상화를 판정하는지 쓴다.

## 8. 완료 상태 문서 작성 규칙
사용자 문서는 특정 검수 회차의 결함 목록, 배포 차단 상태, 후속 검증 계획 같은 진행 보고를 중심에 두지 않는다. 대신 **완료된 제품에서 성립해야 하는 정상 계약과 Acceptance Gate**를 쓴다. 예:
- Timeline Source query 예외 → `FAILED`로 보존, `NOT_APPLICABLE`과 분리
- FileLog Replay → 실패 Target 격리, 다른 Entry Replay 지속, quarantine/terminalLoss 종료 기준
- Remote 응답 유실 → `UNKNOWN_RESULT` 후 Operation/Reconcile
- Multi-instance → Lease/Fencing으로 stale writer 차단
- DB Restore → row/count/amount/version/checksum/hash와 업무 원장 대사

이 계약과 Source가 불일치하면 문서를 약화시키지 말고 구현 보완 대상으로 처리한다.

## 9. 초기 설치/Bootstrap 문서 규칙
설치 순서는 Artifact → 계정/Directory → Secret/Certificate → DB Profile/Lifecycle → Runtime Config → Bootstrap → Start/Health → 첫 로그인/비밀번호 변경 → 운영 인계로 설명한다. 고정 초기 비밀번호를 만들지 않는다. Bootstrap Secret 주입과 첫 로그인 후 변경 절차를 기술한다.

## 10. Route/화면 문서 규칙
실제 Route Registry와 Component/Operation을 대조한다. Query-only 화면에 Command/Approval/Rollback 절차를 억지로 적용하지 않는다. Command 화면은 Permission/Reason/Approval/Expected Version/Idempotency/UNKNOWN/PARTIAL/Audit를 실제 Operation에 맞춰 설명한다.

## 11. Property 문서 규칙
Key, env, Type, Default, required, range, Consumer, Profile, restart, Secret, 오류, 확인 방법, 정상 결과, rollback을 포함한다. Prefix만 나열하지 않는다.

## 12. Reader Question + Completion Contract QA
FAQ를 수동으로 무한 확장하지 않는다. 최소 10만 개 이상의 `Persona × Lifecycle × Feature × Intent` 조합 Probe가 단일 Owner Document와 절로 라우팅되어야 하고, 각 Feature는 정상·오류·복구·보안·관측·인계·완료 판정 계약을 가져야 한다. 대표 자연어 질문은 별도 수동 검수한다.

## 13. 현행화 Trigger
Public API/SPI, Starter/Profile, Route/Menu/Permission/Operation, Property, SQL/Migration, State/Error, Script/Command가 변경되면 Owner Document를 같은 변경 단위에서 갱신한다. 소비 문서는 링크/요약만 최소 현행화한다.

## 14. PDF/DOCX 검수
DOCX는 Heading/Navigation/Hyperlink/Bookmark/반복 Table Header/Page number를 제공한다. PDF/DOCX 모두 전 페이지 렌더해 clipping/overlap/blank/orphan/table split/font/link를 검수한다.

## 15. 문서 완료 판정
문서 수/페이지 수/링크 수/자동 PASS만으로 완료 처리하지 않는다. 다음 전체 체인이 닫혀야 한다.

`질문 → 단일 정본 → 실제 API/Config/Route/SQL → 실행 절차 → 정상 결과 → 오류/부분 실패 → 복구/대사 → 보안/감사 → Test → 운영 인계 → 완료 Gate`

정본을 찾았지만 Source를 역분석해야 답이 나오거나, 실패/복구/판정 기준이 없거나, 같은 상세 내용이 여러 문서에 복제되어 있으면 사용자 문서 완료가 아니다.
