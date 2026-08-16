# CPF 문서 산출물 최종 재작업 리뷰

## 기준
- 최신 로컬 입력: `CPF_FULL_SOURCE_FOR_NEXT_QA(9).zip`
- 입력 ZIP SHA-256: `7334ce55d1529cd5910f7b67ed97375ebcce63c0324b9668eb7bab38ccd0756e`
- 입력 ZIP에는 `.git`이 없어 exact Git SHA/Working Tree는 확인하지 않았습니다.
- 개발팀 제공 `Public Function TOP 100`, `Batch TOP 50`, 개발자 도입/학습곡선 개선 방향을 참고하되 실제 Source/Public Surface와 대조해 문서 구조를 최적화했습니다.

## README
**이전 문제**: 구성 방식 용어가 추상적이고, Gateway/Batch/Starter 그림에서 선택 이유와 장점이 약했으며 단순 구조와 전체 Architecture 역할이 겹쳤습니다.

**이번 구성**: `CPF 정의/해결 문제/Spring Boot 관계/Golden Path/적합 범위/5분 실행 → CPF 한눈에 보기 → 전체 Architecture → 기본 제공/선택 기능/업무 Domain 생성 → 오케스트레이션 → Gateway 사용 사례 → Batch 확장 → Starter 선택 장점 → 개발~운영 → 최소 시작 → 공식 문서 → License`.

**주요 보완**
- `기본 제공 → 선택 기능 사용 → 필요 시 업무 Domain 생성`으로 용어를 직관화했습니다.
- 큰 전체 Architecture와 단순 Quick Overview를 둘 다 유지했습니다.
- Gateway는 `L4만 / Gateway만 / L4+Gateway` 사례와 단일 진입점/Route 정책 집중 장점을 그림으로 표시했습니다.
- Batch는 Worker/Agent 수평 확장과 Control Plane 분리 장점을 표시했습니다.
- Starter는 `업무 목적 → Profile → Starter/Provider → CPF 구성 → 업무 Domain` 흐름과 추가/교체/설정 일관성/업무 코드 영향 최소화 장점을 표시했습니다.
- README 앞부분에 CPF 정의·해결 문제·Spring Boot 관계·Golden Path·적합한 시스템 범위·5분 실행 경로를 명시해 Repository README만 읽어도 CPF 방향을 오해하지 않도록 보강했습니다.
- README 최하단 License 문구를 유지했습니다.

## 02 프레임워크 개발자 가이드
- 이전: 16p / 29 tables
- 현재: 28p / 52 tables

기본 셋업이 끝난 업무 개발자가 바로 찾아 쓰는 매뉴얼로 재구성했습니다. 앞부분은 Golden Path 중심 `핵심 기능 Quick Summary`, 실행/테스트 명령, Controller-Service-Repository 표준 흐름과 Transaction 표준 그림을 제공합니다. 내부 Application 호출, CPF Domain 호출, 외부 연계를 3분류로 분리했고 `domainClient.execute()`, `@CpfClient/@CpfTimeout/@CpfRetry`, 고급 `CpfServiceCaller.invoke()`를 서로 다른 수준으로 배치했습니다. Controller/Service/DAO Base helper, Cache, Messaging, Context, Code/Message/Parameter/Calendar, Logging/Audit/Security/Test를 기능군별로 정리했습니다. 뒤의 **TOP 100**은 10개 그룹 + `Golden/Capability/Advanced` 구분으로 유지해 기능 존재와 선택 기준을 빠르게 찾게 했습니다.

## 03 배치 개발자 가이드
- 이전: 12p / 21 tables
- 현재: 18p / 38 tables

처리 모델 선택부터 시작합니다. `Tasklet / Chunk / LOCAL_PARTITION / REMOTE_PARTITION / REMOTE_CHUNK / REMOTE_STEP`, Job/Step, JobParameter, TX/Checkpoint, 실행 요청 `run/scheduledRun/retry/restart/rerun/stop/onDemand`, Control Plane `start/stop/restart/abandon/reconcile`, Retry/Restart/Rerun/Reprocess/Reconcile 차이, Worker/Lease/Fencing, Scheduler/Misfire, Center-Cut, 외부 UNKNOWN, 실행 Property Default/Range까지 연결했습니다. **Batch TOP 50**은 Golden/Capability/Advanced로 구분했습니다.

## 04 운영자 매뉴얼
- 이전: 9p → 현재 7p
메뉴 설명보다 `증상/목적 → 거래·실행 찾기 → Runtime/Topology → UNKNOWN → 설정 → 위험 조치 → Gateway → Audit/Incident` 순서로 정리했습니다.

## 05 배치 운영 가이드
- 이전: 8p → 현재 7p
Stop/Restart/Reprocess/UNKNOWN-Reconcile을 독립 장으로 구분하고 Worker/Agent, Scheduler/Misfire, Center-Cut과 운영 인계 기준을 상황 중심으로 정리했습니다.

## 06 Gateway 개발·사용 가이드
- 이전: 8p → 현재 6p
Gateway 사용 여부부터 판단하고 `L4만 / Gateway만 / L4+Gateway` 구조, Route, Target/Health, Security/Header, Timeout/Retry/Circuit/Rate Limit, 적용/검증/Rollback, 오류/ADM 순으로 구성했습니다.

## 07 Specification / 기술 명세
- 이전: 12p → 현재 10p
사용법 중복을 줄이고 Public Profile/Starter, Web/Service/Persistence/TX, Cache, Domain/Integration, Messaging, Security/Logging/Audit, Common, Batch 공개 계약, Gateway/Runtime, Command/Config/DB, Error/UNKNOWN, Source Index를 찾는 Reference로 정리했습니다. README와 같은 상세 Architecture를 사용합니다.

## 공통 품질
- README 제외 6종 모두 표지 다음 `전체 목차` 전용 페이지 + 내부 링크
- 내용 기준 가변 열 폭, 균등폭 다중열 표 0
- 접근성 최신 Audit: 6종 high/medium/low 0
- DOCX/PDF 내부 이동 링크 broken 0
- 최종 DOCX/PDF 합계 76 pages, 표 146개
- 표는 Summary/선택/Reference에만 사용하고 장문 설명은 본문·코드·그림으로 분리
