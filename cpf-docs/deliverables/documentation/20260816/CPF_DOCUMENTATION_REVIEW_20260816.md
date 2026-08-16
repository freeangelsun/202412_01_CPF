# CPF 공식 사용자 문서 재작성 리뷰

## 1. 작업 기준

- 입력 정본: `CPF_FULL_SOURCE_FOR_NEXT_QA(9).zip`
- 입력 ZIP SHA-256: `7334ce55d1529cd5910f7b67ed97375ebcce63c0324b9668eb7bab38ccd0756e`
- Git exact SHA: **미확인** - 제공된 로컬 ZIP에는 `.git` metadata가 없음.
- 문서 정본: `cpf-docs/documentation-standards/**` 8종, SHA-256 원본 일치 확인.
- 공식 사용자 문서: README + 02~07, 총 7종만 사용자 Navigation에 노출.
- 최신 사용자 요구를 공통 편집 Gate로 적용: 처음 사용하는 사람 중심, 목적별 Quick Finder, README 제외 전체 목차 전용 페이지, 클릭 이동, 불필요한 표 제거, 내용 기반 가변 열 폭, 장문 셀 억제, 실제 Source 기반 API/명령/기능 설명.

## 2. README.md

### 기존 문제
구조 자체는 정리돼 있었지만 Quick Start가 Gradle 일반 명령 중심이어서 실제 CPF 일상 개발 도구를 바로 찾기 어려웠고, README 시각화는 축소 표시 시 글자 크기·밀도·외곽 규격이 서로 맞지 않는 문제가 있었다.

### 이번 변경
- Quick Start를 `cpf-tools/build/tools/cpf-dev.ps1` 중심으로 변경하고 `로컬 실행(1) / 전체 빌드(2) / 빠른 검증(4)`을 가장 먼저 보도록 정리.
- 8개 README 이미지를 같은 시각 체계로 전면 재작성. 본문 이미지는 1672x941로 규격 통일.
- 실제 CPF 영역과 운영/지원, 외부 자원을 분리해 Architecture를 재작성.
- 구성 방식, 오케스트레이션, Gateway, Batch Runtime, Starter 선택, 개발-운영 Lifecycle을 각각 한 그림 한 메시지로 축약.
- 마지막에 요청 문구 기준 `License` 섹션 유지.

### 사용자가 얻는 정보
README만 보고 CPF가 무엇인지, 어떤 방식으로 구성되는지, 개발을 어디서 시작하는지, 어떤 공식 문서를 다음에 열어야 하는지 판단할 수 있다.

## 3. 02 프레임워크 개발자 가이드

### 기존 문제
33페이지/105개 표로 정보가 분산돼 있었고, Module/내부 구조/세부 계약이 앞에 나와 기본 셋업이 끝난 업무 개발자가 Controller·Service·DB·TX 같은 일상 개발 방법을 빠르게 찾기 어려웠다.

### 새 구성
`업무 개발 Quick Finder → 자주 쓰는 명령 → Controller → Service → DB/Repository/Paging → Transaction → Cache/Lock → 공통코드/파라미터/메시지/영업일 → Logging/Context/Audit → 외부 연계 → Domain 호출 → Messaging → Security → Test → 기능 추가 → 빠른 Reference`

### 주요 개선
- 33p/105표 → 16p/29표. 표를 줄이되 실제 선택에 필요한 Summary는 유지.
- 기본 셋업 완료 후 업무 개발이라는 전제를 문서 첫 장에서 명확히 함.
- `@CpfController`, `@CpfService`, `@CpfRepository`, `@CpfTx`, `CpfCachePort`, `CpfCacheAsideService`, `@CpfLogging`, `@CpfAudit`, `@CpfClient`, `@CpfTimeout`, `@CpfRetry`, Generated Typed Client/`CpfResult`, `@CpfMessageListener`, `@CpfPermission`, `@CpfApprovalRequired`를 실제 Source와 대조.
- 명령은 `용도 / 명령어 / 세부 용도 / 주요 옵션 / 참고사항` 중심으로 목적별 그룹화.
- Provider/내부 구현 나열 대신 "내 업무 목적이면 무엇을 쓰는가"를 먼저 제시.
- Page/Slice/Cursor, Transaction propagation, Cache 옵션, 동시성/Idempotency 등은 선택 판단에 필요한 수준만 유지.

### 사용자가 얻는 정보
새 업무 Controller/Service/Repository를 어떤 Base/Annotation으로 작성하고, TX·Cache·로그·연계·권한·Test를 어떤 CPF 패턴으로 붙이는지 바로 복사·응용할 수 있다.

## 4. 03 배치 개발자 가이드

### 기존 문제
24페이지/71개 표에 Runtime 구조와 세부 기능이 넓게 퍼져 있었고 첫 Job 작성자가 Tasklet/Chunk, Parameter, Restart 경계를 빠르게 잡기 어려웠다.

### 새 구성
`배치 개발 Quick Finder → 자주 쓰는 명령/실행 구조 → 첫 Job → Job/Step Annotation → Tasklet/Chunk → Reader/Processor/Writer → Parameter/Instance/Execution → TX/Checkpoint/Restart → Retry/Skip/Reprocess → Partition/Worker → Scheduler/Misfire → Center-Cut/Preview → 외부 연계/UNKNOWN → Test/ADM → 운영 인계/Job Pack`

### 주요 개선
- 24p/71표 → 12p/21표.
- 일반 Spring Batch Job Bean용 `com.cpf.batch.api.CpfBatchJob(id,name,ownerDomain)`과 Annotation Step Handler용 `com.cpf.batch.api.annotation.CpfBatchJob + @CpfBatchStep`을 혼동하지 않도록 실제 Source 기준으로 분리.
- Education Sample과 동일한 JobBuilder/StepBuilder 및 Reader/Processor/Writer 패턴 반영.
- Restart/Reprocess/Abandon/UNKNOWN/Reconcile을 개발자 판단과 운영자 조치로 역할 분리.
- Job Pack/Artifact는 일반 Job 코딩 시 불필요한 내부 상세를 제거하고 배포/인계 단계에서만 알면 되는 계약으로 정리.

### 사용자가 얻는 정보
첫 Batch Job을 만들면서 처리 모델, commit/restart 경계, 실패 처리, 병렬화와 Scheduler를 어떤 순서로 판단해야 하는지 빠르게 찾을 수 있다.

## 5. 04 운영자 매뉴얼

### 기존 문제
17페이지/48개 표로 운영 개념이 넓게 퍼져 있었고, 실제 장애 시 "어디를 먼저 보고 무엇을 확인할지"보다 기능 설명이 앞섰다.

### 새 구성
`운영 Quick Finder → 처음 이상을 봤을 때 → ADM 위치 → 거래/실행 찾기 → Runtime/Topology → 오류/UNKNOWN → 설정/기준정보 → 변경/승인/위험 조치 → Gateway 일반 운영 → 로그/Audit/Incident → 권한/운영자 → 정기 점검/인수인계`

### 주요 개선
- 17p/48표 → 9p/12표.
- Transaction/Execution/Trace 식별자 기반으로 첫 진단 순서를 고정.
- 위험 조치는 Permission/Reason/Approval/Audit를 함께 확인하도록 정리.
- ADM 실제 화면 예시를 유지하고 메뉴/경로를 한눈에 찾게 함.
- 깨져 있던 `cpf-command-lifecycle.png` 한글 렌더링을 수정.

## 6. 05 배치 운영 가이드

### 기존 문제
18페이지/47개 표에 운영 기능이 분산돼 있었고 Restart/Reprocess/UNKNOWN 등 실제 운영자가 가장 자주 판단하는 차이가 빠르게 보이지 않았다.

### 새 구성
`배치 운영 Quick Finder → 실행 찾기 → 상태 읽기 → 수동 실행/Parameter → Stop → Restart → Reprocess → UNKNOWN/Reconcile → Partition/Worker → Scheduler/Misfire → Center-Cut → 장애 Runbook → 감사/인수인계`

### 주요 개선
- 18p/47표 → 8p/12표.
- 조치 이름보다 먼저 대상 Execution과 상태/Parameter를 확인하게 구성.
- Restart/Reprocess/UNKNOWN/Reconcile을 별도 절로 나눠 오조작 가능성을 낮춤.
- 장애 Runbook과 감사/인수인계를 마지막에 연결.

## 7. 06 Gateway 개발·사용 가이드

### 기존 문제
16페이지/44개 표에서 Route/Security/Runtime 세부가 분산돼 처음 사용하는 개발자가 Gateway가 필요한지, Route를 어떻게 만들고 검증할지 판단하기 어려웠다.

### 새 구성
`Gateway Quick Finder → Gateway 필요 여부 → Route 기본 → Route 적용 → Target/Health → Security/Header → Timeout/Retry/Circuit/Rate Limit → 추적/로그 → 변경/배포/Rollback → 오류 확인 → ADM/Reference`

### 주요 개선
- 16p/44표 → 8p/9표.
- Direct/L4/Gateway/L4+Gateway 선택을 먼저 보여주고 Gateway를 무조건 전제로 하지 않음.
- Route 생성부터 Health/연결시험, Security, Resilience, 오류 확인까지 실제 개발 순서로 정리.
- Workbench/Lifecycle 시각화를 사용해 긴 설명을 줄임.
- 최종 전 페이지 시각검수에서 Gateway Workbench/Publish 이미지의 한글 대체문자(□)를 발견해 원본 SVG 폰트 계약과 PNG를 함께 보정하고 DOCX/PDF를 재렌더함.

## 8. 07 Specification / 기술 명세

### 기존 문제
28페이지/74개 표로 계약이 과도하게 분산돼 있었고 개발 가이드와 중복되는 사용법과 Reference가 섞여 필요한 계약을 찾는 속도가 느렸다.

### 새 구성
`빠른 사용법 → Architecture/Ownership → Public Profile/Starter → Web/Controller → Service/Base → Persistence/TX → Cache/Lock → Common → Integration/Domain Result → Messaging → Security/Audit/Logging → Batch 공개 계약 → Runtime/ADM/Gateway → Config/DB → Error/State/UNKNOWN → Command/검증 → Source Index`

### 주요 개선
- 28p/74표 → 12p/20표.
- 사용법은 02/03/06으로 넘기고 정확한 Public API/Annotation/State/Config 계약 중심으로 역할을 정리.
- Batch의 두 `CpfBatchJob` 계약을 package까지 구분하고 `BusinessJobProvider`, `JobPackManifest`, `BatchExecutionControlPort`, `BatchControlState`, LaunchMode/Outcome/OperationsPort를 실제 Source에 맞춰 보강.
- README와 동일한 CPF Architecture 시각화를 사용.

## 9. 공통 시각/Navigation 결과

- README 제외 6종 모두 표지 다음 **전체 목차 전용 페이지** 제공.
- 전체 목차와 각 절의 `전체 목차로` 링크를 실제 내부 Bookmark/Hyperlink로 연결.
- DOCX 내부 링크 깨짐 0건, PDF 내부 링크 깨짐 0건.
- 다중 컬럼 표에서 모든 열이 동일 폭으로 남은 표 0건.
- 장문 셀(220자 초과) 0건.
- 표 Header 반복 속성과 이미지 대체텍스트를 최종 보정.
- PDFium 기반 PDF 재렌더와 DOCX 렌더를 모두 시각검수.
- 최종 시각검수에서 발견된 Gateway 이미지 한글 glyph 결함까지 수정 후 06 DOCX/PDF 전체 8페이지를 다시 검수.
