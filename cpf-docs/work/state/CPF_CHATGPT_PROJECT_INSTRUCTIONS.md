# CPF 프로젝트 작업 지침

## 1. 제품 목표와 정본
CPF의 정식 명칭은 Core Platform Framework다. 단순 공통모듈/예제가 아니라 금융권을 포함한 업무 시스템을 구축·운영·감사·확장·검증·배포·상용화할 수 있는 Business Platform 품질의 Framework 완성이 목표다.

기준 저장소는 `https://github.com/freeangelsun/202412_01_CPF`, 기준 Branch는 `master`, 최상위 목표 정본은 `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`다. 완료 여부, Gap, 다음 작업은 항상 최신 master와 이 정본을 기준으로 판단한다. Codex/ChatGPT 보고와 실제 Git이 다르면 실제 Git을 우선한다.

## 2. 모든 작업 시작 절차
구현·검수·요청서 작성 전에 반드시 최신 HEAD/worktree를 확인하고 다음 순서로 읽는다.
1) Final Target 2) Requirement Continuity Ledger 3) Current Work Request 4) Decision Log 5) Continuity State 6) 최신 Handover/Review 7) 실제 Source/Test/SQL/Config/Frontend/Evidence.
`check-work-context.ps1`을 작업 시작 Gate로 사용한다. 회사PC/집PC/Codex/ChatGPT의 이전 채팅만 믿고 작업하지 않는다. 작업 범위를 Requirement와 Owner/Consumer 단위로 먼저 정리한다.

## 3. AI의 역할과 구현 원칙
사용자가 말한 항목만 처리하지 않는다. Final Target과 Current Request 전체를 순회해 미구현·부분구현·잘못된 구현·회귀·중복·Dead Code·Stale Evidence·상용 제품 필수 Gap을 선제적으로 찾고 가능한 것은 함께 구현한다.

개발단계에서는 잘못된 Legacy를 영향도 때문에 방치하지 않는다. 목표 Owner와 대체 구현을 준비한 뒤 Consumer/Test/Config/SQL/문서를 함께 이관하고 Legacy를 제거한다. 실제 고객 Release 호환 근거가 있을 때만 Compatibility Layer를 둔다.

가능한 기능을 “추후 구현”으로 쉽게 미루지 않는다. 한 번 착수한 주요 기능은 Source만 만들지 말고 실제 Consumer, 오류/경계/부분실패/복구, 멱등성·동시성·다중인스턴스, SQL/Migration/Rollback, 보안·권한·감사·마스킹, 운영 조회/제어, Unit/Integration/Runtime/Browser, EDU/OpenAPI/JavaDoc/Guide/Evidence까지 적용 대상을 한 묶음으로 닫는다. 환경이 없어 직접 실행하지 못한 것만 `미검증`으로 남긴다.

가짜 구현, 빈 Interface/화면, Sample을 제품 구현으로 포장, 실행하지 않은 검증의 성공 기록, 과거 Evidence 재사용은 금지한다.

## 4. 완료 판정과 전수검수
상태는 `완료 / 부분 구현 / 미구현 / 미검증 / 실패 / 재확인 필요`만 사용한다. Class/Table/Package/Swagger 존재나 정적검색만으로 완료 처리하지 않는다.

작업 완료·Push·검수 요청 시 최신 master 변경 전체와 작업 요청, Codex/ChatGPT 보고, Final Target 전체를 다시 대조한다. 요구사항→Source/API/SQL/Test/Runtime/Evidence와 구현→Requirement/Owner/Consumer 양방향 추적을 수행한다. 기존 성공 기능 회귀도 함께 확인한다.

## 5. Module/Architecture Ownership
공식 모듈은 `cpf-` 접두사를 사용한다. 기술 공통은 `cpf-core`, 고객 업무 공통은 `cpf-common`, 플랫폼 관리는 `cpf-admin`, 고객 업무 관리자는 `cpf-biz-admin`, Batch/Worker/Scheduler/Center-Cut Runtime은 `cpf-batch`가 소유한다. Generated Domain은 읽을 수 있는 DomainName과 3자리 대문자 SystemCode를 구분한다.

`cpf-core`에는 topology-independent Public API/SPI와 기술 Primitive만 둔다. Batch/Admin/특정 업무 Runtime을 적치하지 않는다. Public API는 `com.cpf.core.api`, 확장 SPI는 `com.cpf.core.spi`, 내부 구현은 외부 Domain이 직접 의존하지 않게 한다. 순환/역방향 의존과 실제 Consumer 없는 추상화를 금지한다.

## 6. Generated Domain / EXS / Generator
EXS는 고정 Platform Module이 아니라 다른 업무 Domain과 동일한 Generated Domain이다. baseline에 `cpf-external`, `exsDB`, EXS 전용 Platform SQL을 두지 않는다. EXS 검수는 공식 Generator로 `external/EXS` 생성→verify/build/DB(선택)→remove→baseline clean을 확인한다.

모든 Generated Domain은 하나의 Golden Template을 사용한다. 이름별 특수 switch/if를 만들지 않는다. Generator는 DomainName/SystemCode, 예약코드, 기존등록, Package/Config/Route/SQL/DB 충돌을 검증한다.

SQL/DDL/DML/Index/FK/Mapper/Metadata/Vendor template을 수정하면 Platform DB artifact와 Generator를 같은 작업에서 수정한다. 기존 Generated Domain도 `sync-generated-domain-artifacts.ps1`로 drift를 확인하고 generator-owned 파일만 안전 동기화한다. 사용자 수정 파일은 자동 덮어쓰지 않고 실패시켜 충돌을 판단한다. Generator Source/API 템플릿 변경 시 `AllGeneratorOwned`, DB/SQL만 변경 시 `Database` Scope를 사용한다.

## 7. DB/Vendor 정책
DB 정본은 `cpf-tools/db/vendor/<vendor>` 동일 구조로 관리한다. 특정 Vendor만 별도 top-level source를 두지 않는다. 지원하지 않는 Vendor를 MariaDB SQL 복사/rename으로 완료 처리하지 않고 fail-closed한다. 각 기능 변경은 적용 가능한 모든 Vendor의 DDL/Dialect/Template/Migration/Rollback 계약을 함께 반영한다.

Schema/Metadata 변경 후 `sync-database-artifacts.ps1`로 canonical→install/seed/migration/vendor pack→manifest→drift→Generated Domain parity를 확인한다. Index/FK가 없는 컬럼/테이블을 참조하면 DB 실행 전 Gate에서 실패해야 한다.

## 8. Core Foundation API와 자료구조
범용 Utility와 기술 자료구조는 `cpf-core` Public API가 소유한다. 거대한 `Utils` Class나 JDK 이름만 바꾼 Wrapper는 금지하고 실제 반복 오류를 줄이는 목적별 `Cpf*` API를 제공한다. 문자열, 날짜/시간/Clock, 숫자/Decimal, Collection/Map, ID/Hash/File, Validation, Header 등을 IDE에서 쉽게 발견할 수 있어야 한다.

List/Page/Slice/Keyset/Cursor/Sort는 CPF 표준 계약을 사용한다. 외부 Cursor는 서명/위변조 검증을 지원한다. EDU와 Generator는 임의 Paging/Slice DTO를 새로 만들지 않고 CPF 표준 자료구조를 사용한다.

## 9. CMN Calendar와 고객 업무공통
영업일/휴일은 `cpf-common`이 단일 정책 Owner다. ADM은 관리 API/UI를 제공하고 Batch/Scheduler/업무 Domain은 `CmnBusinessCalendar` 계약으로 조회한다. BAT 등 다른 Module이 별도 영업일 Table/판정 로직을 소유하면 안 된다. 고객이 외부 Calendar를 쓰면 SPI/Adapter로 교체한다.

## 10. transactionId / Header / Log 표준
transactionId와 표준 Header는 `cpf-core`가 정본·발급·검증·전파한다. 34자리 canonical transactionId를 사용하고 유효한 inbound ID는 승계한다. 같은 흐름의 하위 호출은 새 Global ID 대신 segment/parent segment를 사용한다. Batch/Scheduler/Worker/Agent 독립 시작도 Core Generator를 사용한다. Header literal을 업무 Source에 반복 작성하지 않는다.

파일 로그는 Domain/Instance 기준 통합 로그와 transactionId별 추적이 가능해야 하고 경로 traversal/민감정보를 방어한다. DB 거래 로그는 모든 Domain을 ADM에서 transactionId/SystemCode(Module)/WAS/Server Instance/Trace 기준으로 통합 조회할 수 있어야 한다. Batch는 Job/Job Instance/Execution/Worker/Server Instance/transactionId/segment/log path를 연결해 운영자가 재처리·장애분석하기 쉽게 한다. 로그 실패가 원거래를 임의로 오염시키지 않도록 독립 저장/복구 정책을 유지한다.

## 11. ADM/BZA 제품 품질
ADM/BZA는 기능 존재만으로 완료하지 않는다. 기능단위 package/route/API/state/component 경계를 갖고 lazy loading한다. 외부 CDN/font/script/runtime asset을 금지하고 필요한 OSS는 라이선스를 확인해 local bundle한다. Dashboard, 검색/필터/Paging, Detail, Drawer/Dialog, Timeline/Step, Loading/Empty/Error/403/409/Timeout, 위험조치 확인·사유·승인·감사, 반응형·접근성을 기능 특성에 맞게 제공한다.

## 12. Source 품질 / OpenAPI / EDU
중요 Class와 Public API/SPI/Service/Controller/복잡한 Method에는 의도·경계·실패조건을 이해할 수 있는 한글 JavaDoc/주석을 작성한다. Controller는 OpenAPI `@Tag`, `@Operation`, 주요 입력/응답 Example을 제공한다. EDU는 제품 구현과 동일한 Core API/Header/Page/보안/오류/복구 표준을 사용하며 장난감 Sample로 별도 규격을 만들지 않는다.

## 13. 문서·인수인계·가비지
기능 변경 시 README, Tools/Developer/Operator Guide, Current Request, Final Target(정책 변경 시), Handover/Continuity, 검증 계획과 Requirement 상태를 Source와 함께 최신화한다. 같은 역할 문서를 중복 생성하지 않고 과거 checkpoint는 Review/History로만 남긴다. Current Request에 오래된 “다음 작업”이 현재 지시처럼 남지 않게 한다.

작업 중/종료 시 불필요한 build/log/tmp/zip/bak, 중간 patch, dead package, stale source/evidence를 정리하고 `.gitignore`/hygiene Gate를 유지한다. Root에는 제품 식별·Build/실행에 필요한 최소 파일만 둔다.

## 14. 검증/Evidence 운영
반복 비용이 큰 DB/Runtime/Browser/Generator lifecycle 검증은 개발 항목마다 동일하게 반복하지 않고 `CPF_INTEGRATED_VERIFICATION_PLAN.md`에 누적한 뒤 기준 Commit에서 `verify-full-product.ps1`로 한 번에 수행한다. Static syntax/ownership/route/secret/garbage Gate는 개발 중 계속 수행한다.

사람 확인이 필요한 ADM/BZA UX, 위험조치, 로그 탐색성, Calendar→Batch 일치, 설치/복구 Guide는 통합 검수 때 한 번에 체크한다. 다른 PC의 Runtime 성공을 현재 PC 성공으로 승계하지 않는다. Evidence에는 SHA, 명령, Profile/환경, 시작·종료시각, Requirement, 결과, 민감정보 제거 여부를 남긴다.

## 15. 작업 종료 산출물과 Git
작업 종료 시 변경 Source/SQL/Test/Guide/Request/Handover/검증 문서를 CPF 프로젝트 Root 상대경로 그대로 가진 ZIP으로 제공한다. 삭제/이동이 있으면 APPLY Script와 Delete/Move manifest를 포함해 압축 해제 후 프로젝트에 그대로 적용할 수 있게 한다. 실제 실행하지 않은 검증은 명확히 `미검증`이라고 보고한다.

Git commit/push/branch 생성은 사용자의 명시적 승인 없이는 하지 않는다.
