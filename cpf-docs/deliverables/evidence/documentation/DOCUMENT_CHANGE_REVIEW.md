# CPF 공식 문서 전/후 품질 리뷰

기준 Source SHA: `92169d9918dd176e8322ac2f9dfc29ebe1d2ea12`

페이지 수 증가는 목표가 아니라 지침에서 빠졌던 Tutorial, 선택·설정 흐름, 실패/복구, 운영 절차, 정확한 Reference를 실제로 넣은 결과다. 반복 설명이나 내부 구현 나열로 분량을 부풀리지 않았다.

## 문서별 전/후 정량 비교

| 문서 | 기존 페이지 | 최종 페이지 | 증감 | DOCX 기존→최종 | PDF 기존→최종 |
|---|---:|---:|---:|---:|---:|
| 프레임워크 개발자 가이드 | 13 | 33 | +20 | 563.2 KiB → 369.6 KiB | 823.5 KiB → 1366.4 KiB |
| 배치 개발자 가이드 | 11 | 24 | +13 | 434.9 KiB → 276.3 KiB | 696.3 KiB → 980.6 KiB |
| 운영자 매뉴얼 | 12 | 17 | +5 | 633.8 KiB → 391.9 KiB | 920.6 KiB → 923.7 KiB |
| 배치 운영 가이드 | 13 | 18 | +5 | 534.8 KiB → 369.7 KiB | 756.6 KiB → 864.8 KiB |
| Gateway 개발·사용 가이드 | 10 | 16 | +6 | 333.0 KiB → 212.7 KiB | 482.2 KiB → 739.5 KiB |
| Specification / 기술 명세 | 15 | 28 | +13 | 1750.4 KiB → 1029.9 KiB | 738.4 KiB → 1102.5 KiB |
| **합계** | **74** | **136** | **+62** |  |  |

## 문서별 질적 변경

### README
- 잘못된 생성형/Batch 표현을 바로잡아 생성형은 Member/External/추가 업무 Domain, Batch는 선택 Capability/Runtime으로 분리했다.
- 전체 Architecture와 구성 방식 그림을 실제 CPF 구조에 맞추고 07과 동일 Architecture 자산을 사용한다.
- 하단 License를 추가하고 공식 7종 Navigation만 유지한다.

### 02 프레임워크 개발자 가이드
- 기존 API·내부 계약 표 중심 구조를 “무엇을 만들 것인가 → 어떤 Public Profile/Provider를 선택할 것인가 → 실제 build.gradle → 공통 기능 → 첫 API Tutorial” 흐름으로 재설계했다.
- Code/Parameter/Message/Calendar/Template, Transaction, Persistence, Cache, Messaging, Security, Integration, File/Object Storage, Observability를 실제 선택 기준과 함께 설명한다.
- 전체 Tutorial, 실패 예, Test, Local Debugging, SPI/Extension, Async/Outbox, DTO 경계, 기존 프로젝트 도입, Cookbook/Quick Reference를 보강했다.
- `com.customer`와 비공식 DB Vendor 혼선을 제거했다.

### 03 배치 개발자 가이드
- Tasklet/Chunk/Partition 선택부터 실제 Job/Step 구현, Reader/Processor/Writer, Parameter, Metadata, Checkpoint, Restart/Reprocess/Reconcile까지 한 개발 흐름으로 연결했다.
- Local/Remote Partition, Runner/Worker/Agent, Lease/Claim/Fencing, Scheduler/Misfire, Center-Cut, Dry Run, Fault Injection, Artifact/Job Pack, 운영 인계를 보강했다.
- 동일 단순명 `CpfBatchJob` 공개 Annotation 2종을 FQCN/사용 시나리오로 구분했다.

### 04 운영자 매뉴얼
- 증상/목적 Quick Navigation, Runtime/Health, 검색→목록→상세→Action, Permission/Data Scope, 위험조치, UNKNOWN_RESULT, Config Partial Apply를 운영 행동 순서로 정리했다.
- ADM 실제 화면, Gateway Runbook, 배포/DB/Backup/Restore/DR, Log/Metric/Trace/Audit, Alert, 증상별 Runbook, 정기점검, FAQ, 신규 운영자 실습, Escalation을 보강했다.

### 05 배치 운영 가이드
- Job/Execution/Step/Parameter 조회부터 실패 범위 판정, Stop, Restart/Rerun, Reprocess, Reconcile, Partition/Worker/Lease/Fencing을 운영자가 바로 판단하도록 재구성했다.
- Scheduler/Misfire, Center-Cut, Agent/Job Pack/Deployment, 권한·승인·감사, 일일 운영, “실행하면 안 되는 상황”, EDU 운영 실습을 추가했다.

### 06 Gateway 개발·사용 가이드
- Gateway가 필수 진입점이 아님을 먼저 확정하고 Direct/L4/Gateway/L4+Gateway 경계를 설명한다.
- Route 입력→CpfGatewayRoute→Server Group/Health/Canary→Auth/Authz→CORS/TLS→Rate/Concurrency→Retry/Circuit Breaker→Stored/Effective/Runtime→ADM 검증 흐름으로 확장했다.
- 실제 등록/검증 Tutorial, HTTP 상태 판정, Test Matrix, Rollback Checklist, 운영 API/FAQ, API/Command Reference를 보강했다.

### 07 Specification
- README와 동일한 최신 Architecture를 실제 임베드하고 Public Profile/Starter, Base API, @CpfTx, Persistence/Common/Domain Client, Batch/Gateway, Config/DB/HTTP/DTO/State/SPI 계약을 Reference 구조로 확장했다.
- Exact Public API, @CpfTx Exact Contract, Gateway Route Exact Contract, Batch Runtime Config, DTO/State/Enum, 잘못된 사용/Verification, ADM HTTP 계약까지 Source 근거와 함께 보강했다.
- 내부 DEV-DOC 식별자는 사용자 Specification에서 제거하고 실제 사용 시 확인할 구현 경계만 남겼다.

## 시각 품질 변경
- 표 Header 반복/정렬, 내용 기반 열 폭, 페이지 경계에서 표 고립 방지를 적용했다.
- 깨진 SVG filter/한글 glyph로 박스가 사라지던 Module/Operator/Gateway/Reconciliation 그림을 정상 PNG로 재생성했다.
- 136페이지 전체를 최종 DOCX render로 직접 검수했고 빈 페이지·잘림·고아 제목/표·깨진 글리프를 모두 수정 후 재렌더했다.
