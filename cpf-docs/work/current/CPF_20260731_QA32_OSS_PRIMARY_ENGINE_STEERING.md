# QA32 즉시 스티어링 — OSS Primary Engine 전환 강화

- 문서 ID: `CPF-20260731-QA32-OSS-PRIMARY-ENGINE-STEERING`
- 기준 Source: `4f675c7f89998cdbba7202e6c83320a0a4421a1f`
- 우선순위: 즉시 반영
- 범위: 진행 중 QA32 개발 전체

## 1. 즉시 정정 사항

기존 문서에서 Spring Batch를 `ADOPT_SCOPED`, “Job/Step Metadata 일부만 사용”으로 해석한 방향을 폐기한다.

Spring Batch는 CPF 배치 전체의 **Primary Execution Engine**이다. 센터컷, 로컬 실행, 병렬 실행, Partition, 원격 Worker 실행, File·DB·API·Shell Job, Restart·Checkpoint·Stop·Abandon·실행 Metadata는 Spring Batch 기반으로 구현한다.

CPF는 승인·권한·배포·Topology·Agent 보안·Fencing·Audit·UNKNOWN_RESULT·Reconciliation·운영 UX를 Control Plane으로 유지한다. 이는 별도 실행 엔진을 유지한다는 뜻이 아니다.

## 2. Spring Batch 구현 지침

- 모든 실제 Batch Consumer를 Spring Batch `Job`·`Step`으로 이관한다.
- 센터컷 Runner는 Spring Batch `JobOperator` 또는 승인된 표준 실행 API를 사용한다.
- 단일 JVM 병렬 처리는 Parallel Step 또는 Local Partitioning을 사용한다.
- 분산 실행은 Remote Partitioning, Remote Chunking 또는 Spring Batch 6 Remote Step 중 Job 특성에 맞는 표준 방식을 사용한다.
- File/DB/API/Shell 작업은 Tasklet 또는 Chunk Model로 구현한다.
- JobRepository와 ExecutionContext를 실행 Metadata·Checkpoint·Restart의 정본으로 사용한다.
- CPF 업무 원장에는 JobInstanceId·JobExecutionId·StepExecutionId를 연결한다.
- Scheduler는 Trigger만 담당하고 Job 결과를 소유하지 않는다.
- 자체 Job/Step State Machine, Restart/Checkpoint, Partition Dispatcher, Center-cut Engine, Worker 완료 집계가 Spring Batch와 중복되면 제거한다.

## 3. 모든 ADOPT_NOW OSS 공통 정정

Spring Batch에만 적용되는 예외 지침이 아니다. QA32에서 `ADOPT_NOW`로 결정한 모든 OSS에 다음을 적용한다.

- Dependency·Wrapper·Skeleton·Sample만 추가하고 기존 자체 구현을 Primary로 유지하지 않는다.
- OSS에 맡긴 책임 범위는 OSS의 표준 Lifecycle·State·Extension Point를 실질적으로 사용한다.
- CPF Wrapper는 정책·보안·감사·계약 경계만 제공하며 OSS 기능을 다시 구현하지 않는다.
- 기존 Consumer를 전수 이관하고 동일 책임의 Legacy와 Fallback을 제거한다.
- 임시 Dual Path는 Owner·기한·제거 Gate가 있는 Migration 단계에서만 허용한다.
- 실제 Runtime·Failure·Recovery·Scale Evidence와 exact Source SHA가 없으면 완료로 판정하지 않는다.

현재 확정된 주요 적용 예시는 다음과 같다.

- UI: Element Plus·TanStack Table을 실제 화면 Primary로 사용하고 Custom 범용 Widget 제거
- Router/State/Form: Vue Router·Pinia·TanStack Vue Query·Zod로 실제 Consumer 이관
- API Client: Orval 생성 Client와 승인된 Mutator만 사용하고 Raw Fetch/수동 Endpoint 제거
- Browser Security: Spring Security·Spring Session JDBC 기반 BFF를 실제 Session 정본으로 사용
- Gateway: SCG Server Web MVC가 실제 요청 Data Plane을 처리하고 Custom Proxy Primary 제거
- Messaging: Kafka가 실제 Product Primary이며 AMQP 기본 경로 제거
- Resilience: Resilience4j 표준 정책 경계로 이관하고 자체 Retry/Circuit 중복 제거
- Batch: Spring Batch가 전체 실행 Primary
- DB Migration: Flyway OSS Core가 실제 History/Order/Checksum 정본
- Observability: Micrometer Observation과 OTel/OTLP로 이관하고 중복 Instrumentation 제거
- Cache: Caffeine을 실제 L1 Primary로 사용
- Supply Chain: CycloneDX·ORT·Syft·Grype 결과를 Release Gate에서 상호 대조
- Browser E2E: Playwright 실제 3-Browser Gate 사용

## 4. 금지되는 완료 형태

- Dependency만 추가
- 인터페이스·Adapter만 추가
- Sample 또는 일부 Consumer만 이관
- 기존 자체 엔진과 OSS가 이중 정본
- Legacy Fallback으로 테스트 통과
- OSS 표준 기능을 Wrapper 내부에서 다시 자체 구현
- Compile·Unit Test만 실행하고 Runtime·장애·복구 미검증
- exact SHA가 다른 Evidence 재사용

위 상태는 모두 `PARTIAL` 또는 `UNVERIFIED`다.

## 5. 문서·Matrix 반영

다음 문서에서 이번 결정을 정본으로 반영한다.

- `cpf-docs/architecture/ADR_OSS_FIRST_PLATFORM_DIRECTION.md`
- `cpf-docs/architecture/CPF_BUILD_VS_BUY_MATRIX.md`
- `cpf-docs/quality/CPF_20260730_QA32_OSS_MIGRATION_MATRIX.csv`
- `cpf-docs/quality/CPF_20260730_QA32_REQUIREMENT_MATRIX.csv`
- `cpf-docs/quality/CPF_20260730_QA32_SCENARIO_MATRIX.csv`
- `cpf-docs/work/current/CPF_20260730_QA32_DEVELOPMENT_REMEDIATION_REQUEST.md`
- `cpf-docs/work/current/CPF_20260730_QA32_GPT_DEVELOPMENT_INSTRUCTION.md`
- `cpf-docs/work/handover/CPF_20260730_QA32_DEVELOPMENT_HANDOVER.md`

## 6. 완료 Evidence

최소 다음을 결과물에 포함한다.

- OSS별 Consumer Inventory와 Migration Parity
- Legacy 제거 목록과 정적 Gate 결과
- 실제 Primary Runtime 경로 증적
- 정상·실패·중단·재시작·복구·Scale 실행 결과
- Dependency Tree·Lock·SBOM·License·CVE 결과
- 실행 명령·환경·시작/종료시각·Exit Code·Report Hash
- exact Source SHA와 Artifact Hash

이번 정정은 범위 확대를 위한 신규 OSS 추가가 아니다. 이미 채택한 OSS를 형식적으로 붙이지 않고, 정해진 책임 범위에서 제대로 Primary로 사용하여 자체 중복 구현과 장기 유지보수 부담을 제거하기 위한 방향 정정이다.
