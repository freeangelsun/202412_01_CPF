# CPF R11 Implementation Report

## 1. 기준

- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- 작업 기준 SHA: `b6db56f5ee745558a59ce511ad681216004b9672`
- 최상위 정본: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
- 선행 검토: R9 Implementation/Remaining Gap, R10 Implementation/Remaining Gap
- 판정 원칙: 실행하지 않은 Gradle/Spring/DB/Browser/다중 인스턴스 검증은 성공으로 기록하지 않는다.

## 2. 이번 작업의 핵심 결론

R10에서 완료 처리하지 않았던 두 핵심 Gap을 Source 기준으로 직접 보완했다.

1. **Generated Domain Golden Template Public Boundary**
   - Generator의 `com.cpf.core.common.*` 직접 참조를 제거했다.
   - Base / Execution / Error / HTTP / Logging / Database / Broker / FileTransfer를 `com.cpf.core.api.*` 중심으로 승격했다.
   - Center-Cut과 Service Call은 `api/spi` 경계로 분리했다.
   - Generator에 있던 중복 `CpfSortDirection direction` 선언을 제거했다.
   - Generated Domain이 생성 즉시 AOP Logging과 Execution Catalog에 합류하도록 공개 `CpfOnlineTransaction`/`CpfBatchJob`도 기존 scanner/aspect가 인식하도록 보완했다.

2. **`cpf-common.utils` 중복 Surface 제거 준비**
   - 확인된 실제 Consumer `AdmLogQueryService`를 `CpfStrings`로 이관했다.
   - `CpfStrings/Lists/Maps/Ids/Pages/Values/Json` 및 Fixed-Length 변환 helper를 보강했다.
   - `cleanup-r11-obsolete.ps1`는 전체 Java Source에서 `com.cpf.common.utils.*` 참조가 0건인 경우에만 legacy package를 삭제하는 fail-closed 방식이다.

## 3. 추가로 발견하여 보완한 실제 구조 오류

### 3.1 BAT Center-Cut 경계 누락

`BatRemoteCenterCutHandler`가 최신 Core에 존재하지 않는 `com.cpf.core.api.centercut.*` / `com.cpf.core.spi.centercut.*`를 import하고 있었다. Public Center-Cut API/SPI를 복구하고 BAT Runtime을 해당 계약으로 이관했다.

- `CpfCenterCutStatus`에 `UNKNOWN_RESULT`를 명시했다.
- BAT Runner/Service/Remote Handler가 `core.common` 구현을 직접 import하지 않도록 정리했다.
- `BatHttpCenterCutRemoteTransport`를 기본 HTTP transport로 제공한다.
- 결과불명을 `RETRY_REQUESTED` 같은 성공/실패 추정 상태로 숨기지 않는다.

### 3.2 REF Center-Cut / Batch EDU Ownership

REF EDU가 Core의 legacy Batch/Center-Cut Runtime을 직접 실행하던 구조를 제거했다.

- REF Center-Cut은 `TargetProvider/Handler + internal item endpoint`만 제공한다.
- Runner/lease/retry/UNKNOWN/재처리는 `cpf-batch` Owner가 담당한다.
- REF Batch EDU는 `CpfBatchOperationsPort`만 소비한다.
- Standalone REF에서 BAT Adapter가 없으면 기동 실패 대신 실행 API가 명확한 `503`으로 실패한다.
- REF local batch configuration은 명시 Property가 켜진 경우에만 구성한다.

### 3.3 ADM 메뉴 권한 fail-open

ADM Frontend는 `authorizedMenus`가 빈 배열이면 전체 메뉴를 노출하는 경로가 있었다. Permission loading 상태를 별도로 두고 인증 완료 후에는 명시적으로 허가된 메뉴만 노출하도록 fail-closed로 수정했다.

BZA는 현재 `hasBzaMenu()`로 Route를 필터링하고 `hasBzaPermission()`으로 버튼 권한을 판정하는 구조를 확인했다. `DASHBOARD`는 인증 사용자 공통 Landing으로 설계된 기존 정책을 유지했다.

### 3.4 ADM Fixed-Length Log Layout

ADM Log 상세가 고정길이 원문만 표시하던 부분을 보완했다.

- 명시된 `layoutId/version`이 있고 Registry/Parser가 존재할 때만 field/group를 해석한다.
- Layout metadata가 없거나 해석 실패하면 임의 길이 추론을 하지 않고 masked raw를 표시한다.
- 운영 화면에서 잘못된 전문 분해가 정상 데이터처럼 보이는 것을 방지한다.

## 4. 사용자 추가 요구사항 반영

| 항목 | Source 반영 | 현재 판정 |
|---|---|---|
| 메시지/코드/파라미터/공통오류/캐시 | CMN Code/Config/Message cache의 refresh/invalidation surface와 ADM owner DB direct pattern Gate 추가. Calendar change publisher SPI 추가 | **Source 보완 완료 / 다중 인스턴스 전파 Runtime 미검증** |
| 자료구조/형변환 Utility | JSON↔DTO/Map/List, 안전 형변환, Map/List helper, Paging helper, Fixed-Length↔Map/List/JSON 보강 | **완료** |
| REF/BAT EDU | Public API 경계로 Batch/Center-Cut/Utility/Fixed-Length 예제 보강 | **Source 보완 완료 / Runtime 미검증** |
| 선택/전체 Build, Jenkins | `build-module-set.ps1`, `deploy/ci/Jenkinsfile.cpf` 추가 | **완료 / 실제 Jenkins 미검증** |
| Batch Agent/Center-Cut | BAT Owner, Public SPI, HTTP remote transport, UNKNOWN_RESULT, ADM 기존 Operations 경계 유지 | **Source 보완 완료 / 장애주입·다중인스턴스 미검증** |
| cpf-gateway | 현재 Proxy/Route/Authorization 구조를 검토하고 Runtime entrypoint Gate 추가. 업무 Domain 구현 직접 의존은 신규로 추가하지 않음 | **정적 재확인 / E2E 미검증** |
| Offset/Keyset Paging | 기존 `CpfPage/CursorPage`에 `CpfPages` 편의 API 보강, EDU 사용 | **완료** |
| Generated Domain AOP 자동합류 | Public execution annotation을 LoggingAspect/ExecutionCatalogScanner가 인식 | **완료 / 실제 생성 Domain Runtime 미검증** |
| README/Guide | 외부 badge/깨질 수 있는 Mermaid 의존을 제거하고 text architecture, Public API/Utility/EDU/UI/Build Guide 추가 | **완료** |
| ADM/BZA 권한 | ADM fail-closed 보완, BZA menu/button route contract 확인, 정적 Gate 추가 | **Source 완료 / Browser 미검증** |
| Code Select/Date Range UI | ADM/BZA 공통 `CpfCodeSelect`, `CpfDateRange` 제공 및 UI Standard Guide 추가 | **완료 / 전체 화면 이관은 Gate/후속 기능 개발 시 사용** |
| 공통 SPI/Web Client/Property | Generated Domain이 Internal 구현 대신 Public HTTP/DB/Broker/File/ServiceCall/Center-Cut 계약만 소비하도록 Generator 변경 | **Source 완료 / Standalone Runtime 미검증** |

## 5. DB / Migration 영향

이번 Overlay는 새로운 제품 테이블이나 Migration을 임의 추가하지 않았다. 코드/메시지/설정/Calendar의 기존 Owner DB 구조를 유지하며, ADM이 다른 Owner DB를 직접 접근하는 신규 코드를 만들지 않았다.

Calendar mutation은 `CmnCalendarChangePublisher`를 호출하도록 보완했으나, 실제 Redis/Kafka/DB-notify 같은 다중 인스턴스 publisher adapter는 배포 토폴로지 선택 사항이다. Adapter가 없는 경우 local mutation 자체는 오염시키지 않는다.

## 6. 삭제/Repository Hygiene

ZIP은 파일 삭제를 표현하기 어려우므로 `cpf-tools/scripts/cleanup-r11-obsolete.ps1`를 제공한다.

- `cpf-common.utils`는 전체 Source 참조가 0건일 때만 삭제한다.
- 오래된 REF local Center-Cut runner artifact는 Consumer가 없을 때만 삭제한다.
- Core legacy Center-Cut package도 외부 Consumer가 0건일 때만 삭제한다. 남은 legacy auto-configuration consumer가 있으면 자동 유지한다.
- `.bak/.orig/.rej/.tmp/.swp/*~`만 정리하며 Evidence는 제외한다.
- `-IncludeBuildArtifacts` 옵션에서만 build/node_modules/dist를 제거한다.

## 7. 완료 판정

이번 작업은 **요청된 R9/R10 잔여 Source 구조와 추가 요구사항 중 현재 Source에서 확인·수정 가능한 범위를 구현 완료**했다. 다만 아래는 사용자가 계획한 통합 검수 전이므로 `미검증`이다.

- Full Gradle / Spring Boot Context
- npm lint/typecheck/test/build
- MariaDB empty install / migration / rollback
- 실제 Generator create → build → remove → regenerate parity
- ADM/BZA Browser 권한/UX
- Gateway Local/Remote E2E
- Batch/Center-Cut 다중 인스턴스, lease/fencing, target-down, timeout, 결과불명 복구
- Calendar 다중 인스턴스 invalidation

위 항목은 **구현 보류 목록이 아니라 통합 검증 목록**이며 `CPF_INTEGRATED_VERIFICATION_PLAN.md` 정책에 따라 적용 후 한 번에 검증한다.

## 8. Owner DB Boundary 정정 및 추가 보완

초기 R11 보고서 5절의 “ADM이 다른 Owner DB를 직접 접근하는 신규 코드를 만들지 않았다”는 표현은 신규 추가 여부만 설명했으며 실제 기존 Source 잔존을 충분히 반영하지 못했다. 사용자 적용 후 Common Capability Gate에서 다음 기존 구조가 확인되었다.

- `AdmJdbcConfig`: BAT/MBR/REF DataSource/JdbcTemplate 생성
- `AdmMemberOperationService`: MBR 회원/역할 DB 직접 SQL
- `AdmBatchRepositoryConfig`: ADM이 BAT Spring Batch Repository 소유
- `CpfBatchScheduleService/CpfBatchExecutionTargetService/CpfBatchScheduler`: ADM 내부 BAT Runtime 중복

이를 보정하여 ADM direct Owner DB 접근을 제거하고, MBR는 `CpfOwnerAdminOperationsPort`를 구현하며, ADM은 Local Port/Remote ServiceCall Adapter를 사용하도록 변경했다. BAT는 이미 존재하는 `BatBatchScheduler`, `BatBatchScheduleService`, `BatBatchExecutionTargetService`, `BatOperationFacade`를 정본 Owner 구현으로 유지한다.

따라서 초기 보고서의 해당 문장은 이 절로 정정한다. Source 보완은 수행했으나 사용자 Repository 전체 Gate 및 Full Runtime 검증 전에는 최종 완료로 승격하지 않는다.
