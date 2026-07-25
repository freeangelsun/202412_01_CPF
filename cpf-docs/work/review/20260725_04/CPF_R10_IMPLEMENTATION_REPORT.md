# CPF R10 구현 리포트

기준: master `7dcccafe4445c10a148a7f45473de25c396aebd3` + R10 overlay  
판정 원칙: Source/SQL/API/Test/Guide를 만들었더라도 Runtime/DB/Browser를 직접 실행하지 않았으면 제품 완료로 올리지 않는다.

## 사용자 15개 항목 대조

| # | 확인한 문제 | R10 구현 | 현재 판정 |
|---|---|---|---|
| 1 | 작업자마다 이전 채팅/개별 요청만 보고 시작할 위험 | `check-work-context.ps1`로 Final Target→Continuity Ledger→Current Request→Decision/Continuity→최신 Handover→HEAD/worktree를 시작 Gate로 고정. Current Request/Final Target에도 의무화 | 부분 구현: 실제 여러 PC에서 Gate 실행은 미검증 |
| 2 | EXS를 baseline에 남기면 Generated Domain 정책과 충돌 | baseline `cpf-external` 금지, APPLY가 로컬 residue/settings 제거, `verify-exs-generated-domain-lifecycle.ps1`로 external/EXS 생성→verify→finally remove | 부분 구현: 실제 lifecycle 실행 미검증 |
| 3 | SQL/Metadata 변경이 Platform/Vendor/Generator/기존 Generated Domain에 따로 반영될 위험 | `sync-database-artifacts.ps1`에 migration checksum→bundle→manifest→drift→profile→Generated Domain parity 연결. `sync-generated-domain-artifacts.ps1`은 Database/AllGeneratorOwned, CREATE/UPDATE/DELETE, 사용자 수정 보호, ownership 누락 fail-closed 구현 | 부분 구현: 실제 기존 Generated Domain 적용 결과 미검증 |
| 4 | README/Guide/Request/Handover가 여러 PC에서 stale해질 위험 | README, Tools Guide, Foundation API Guide, Current Request, Final Target, R10 Handover, 통합 검증 계획 동시 갱신. 과거 checkpoint를 현재 작업지시로 사용하지 않는 원칙 강화 | 부분 구현: 실제 다음 세션 인수인계 사용 검증 필요 |
| 5 | DB/Browser/Generator 검증을 매 작업 반복해 비용/상태가 분산됨 | `CPF_INTEGRATED_VERIFICATION_PLAN.md` + `verify-full-product.ps1`; static gate는 상시, DB/Gradle/npm/Generator/Browser는 기준 commit에서 한 번에. sanitized Evidence와 `RequireAll` 제공 | 미검증: 최종 통합 실행은 의도적으로 보류 |
| 6 | 기존 CMN `*Utils`는 분산되어 있고 범용 기술 Utility Owner가 불명확 | 범용 기술 Utility를 `cpf-core` Public `Cpf*` Foundation API로 정리: String/Date/Time/Clock/Number/Decimal/List/Map/Attributes/ID/Hash/File/Validation/Header/Page. 단순 JDK rename이 아닌 null-safe/검증/정규화/보안 반복 오류 축소 기준 | 부분 구현: 기존 `cpf-common.utils` Consumer 이관/정리는 전체 compile과 함께 추가 확인 필요 |
| 7 | Generator가 Domain별 Slice/Paging DTO를 생성해 Framework 자료구조 표준을 우회 | `CpfPageRequest/CpfPage/CpfSlice/CpfCursorPage/CpfSort`, `CpfCursorCodec/CpfHmacCursorCodec` 구현. Generator/EDU를 CPF Page/Slice로 전환하고 obsolete generator-owned file 안전 삭제 동기화 | 부분 구현: 실제 arbitrary domain regeneration/build 미검증 |
| 8 | BAT가 자체 `bat_business_day_calendar`를 조회해 CMN/ADM 영업일과 이중 정본 | `cpf-common`의 `CmnBusinessCalendar` 단일 Owner, canonical `cmn_business_calendar_day`, JDBC Store+fallback, ADM 관리 API/UI, BAT Scheduler/Simulation 소비 경계로 교체. BAT legacy table/API 제거 | 부분 구현: DB migration 및 Calendar→Batch Runtime E2E 미검증 |
| 9 | transactionId/Header의 Public 진입 API와 34자리 규격 Drift 가능 | `CpfTransactionIdGenerator`, `CpfTransactionIds`, `CpfHeaders`; 기존 Generator는 Public contract 구현, 17+3+7+7=34 규격 고정. Security/Saga 기술 테이블의 runtime transactionId 길이 34로 정규화 migration 추가 | 부분 구현: inbound/local/remote/batch 전파 E2E 미검증 |
| 10 | 로그 운영성은 기반이 있으나 통합 조회축/Batch 파일 server instance/가짜 fixed-length 상세 문제가 존재 | 파일: 기존 Domain/Instance/transaction 경로 유지 + Batch Job 경로에 serverInstance 추가. DB: ADM filter에 module/WAS/server/host/transaction axes, 관련 index. Batch: JobInstance/Execution/Worker/Server/transaction 조회. 누락됐던 ADM LogsPage 복구. Fixed-Length layout 미연결 시 임의 필드분해 금지하고 RAW로 정직하게 표시. DB 원인문구는 UI에 직접 노출 금지 | 부분 구현: 실제 파일/DB/ADM/Batch Runtime 로그 E2E 미검증 |
| 11 | R7/R8 coarse frontend, Core Batch legacy, root log/source 등 가비지 잔존 가능 | APPLY/Cleanup Gate로 Core Batch Runtime/AutoConfig, old DB source, BZA console/coarse feature, ADM coarse feature, EXS/root garbage 제거. 삭제 후 Gate 실패 방식 | 부분 구현: 실제 Windows APPLY 후 Git status 검증 필요 |
| 12 | 사용자가 직접 지정하지 않은 제품 Gap을 놓칠 위험 | R9 전체 Gap을 재대조해 signed cursor, Generated Domain full sync, Calendar single owner, log security/traceability, migration checksum 자동화, fake log parsing 제거 등 추가 구현 | 부분 구현: Generator의 일부 `core.common.*` 직접 의존 등 P0 잔여는 별도 Remaining Gap에 명시 |
| 13 | 부분 구현/가짜 검증을 성공처럼 기록하는 관행 위험 | Final Target/Current Request에 no-fake/no-silent-skip/가능한 범위 한번에 closure 정책. Generated Domain ownership 누락, schema drift, route target, stale source는 fail-closed. 실제 실행하지 않은 Runtime은 계속 `미검증` | 부분 구현: 최종 Runtime 검증 전 완료 판정 금지 |
| 14 | 여러 PC에서 patch 적용 시 삭제/이동 누락 | `PROJECT_OVERLAY`가 프로젝트 Root와 같은 구조, APPLY에 삭제·이동·DB source migration·Generator sync 포함, META manifest/SHA 제공 | 완료(패키지 구조 기준), 실제 Windows 적용은 미검증 |
| 15 | 중요 Source 한글 설명/OpenAPI/EDU 품질 편차 | 새 Public API/Calendar/Controller에 한글 JavaDoc/의도·경계 설명, Controller `@Tag/@Operation`, Reference Foundation EDU 추가. `check-source-documentation-standard.ps1` Gate 추가 | 부분 구현: 저장소 전체 기존 Source 품질은 통합 Gate에서 재검수 필요 |

## 추가로 발견하고 교정한 사항

1. R9 보고와 달리 실제 master에는 Core Batch Runtime 물리 삭제가 반영되지 않았으므로 R10 APPLY에서 계약 이동→Consumer import 교정→Runtime/Test Owner 이동→legacy 삭제까지 수행하도록 설계했다.
2. ADM route가 `features/logs/LogsPage.vue`를 참조하지만 실제 파일이 누락된 회귀를 발견해 복구했다.
3. BAT Scheduler가 `bat_business_day_calendar`를 직접 소유하던 구조를 제거해 CMN 단일 Calendar 기준으로 바꿨다.
4. ADM 초기 로딩에 삭제된 Member/BAT Calendar method 호출 잔재가 있어 제거했다.
5. R9 fixed-length 로그 상세가 실제 Layout Metadata 없이 임의 필드 길이로 분해되던 가짜 표현을 제거했다.
6. Migration checksum을 사람이 수동 편집하지 않고 canonical Migration에서 자동 재생성하도록 변경했다.
7. Generator에서 사라진 이전 generator-owned 파일도 자동 Drift로 인식하고, 사용자 수정이 없을 때만 삭제하도록 보강했다.
8. Batch Job 파일로그 경로에 `serverInstanceId`를 포함해 DB의 실행 인스턴스 축과 파일 로그 분석 축을 맞췄다.

## 제품 완료로 아직 올리지 않은 범위

- Windows PowerShell APPLY 실제 실행
- Gradle clean/test/assemble와 Spring wiring
- ADM/BZA npm test/build/Browser
- MariaDB fresh/full/reset/upgrade/rollback
- EXS/arbitrary Generated Domain create→verify→remove
- local/remote/timeout/retry/failover/UNKNOWN Runtime
- Batch/Scheduler/Agent/Center-Cut multi-instance Runtime
- 파일/DB 로그와 Calendar→Batch E2E
- 지원 Vendor 전체 Platform DDL parity(현재 미지원 Vendor는 fail-closed)

위 항목은 `CPF_INTEGRATED_VERIFICATION_PLAN.md`에서 한 번에 수행하며, 실행 전에는 `완료`로 승격하지 않는다.
