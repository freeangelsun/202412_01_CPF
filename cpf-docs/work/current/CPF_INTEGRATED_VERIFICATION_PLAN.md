# CPF 통합 검증 실행 계획

## 목적
여러 개발 작업마다 동일 DB/Runtime/Browser 검증을 반복하지 않고, 구현을 누적한 뒤 기준 Commit에서 한 번에 재현한다. 다른 PC의 성공 결과는 현재 PC의 성공으로 승계하지 않는다.

## 실행 전
1. `check-work-context.ps1`
2. `git status`, `HEAD`, `origin/master`, 작업 Diff 확인
3. `check-r10-product-standard.ps1`
4. Generated Domain baseline에 `cpf-external`이 없는지 확인

## 통합 검증 순서
1. Repository/Architecture/Secret/JavaDoc/OpenAPI/Frontend route/UTF-8/SQL 정적 Gate
2. `sync-database-artifacts.ps1` + Existing Generated Domain parity
3. Gradle clean/test/assemble
4. ADM/BZA npm test/build
5. Empty DB Install → Schema/Index/FK/Seed verify
6. Reinstall/Upgrade/Rollback
7. EXS `external/EXS` create → verify → remove
8. 다른 arbitrary Generated Domain create/remove/regenerate parity
9. Local/Remote ServiceCall, transactionId/Header, File/DB Log
10. Batch/Scheduler/CMN Calendar, Worker/Lease/Ghost, Center-Cut
11. Unknown/DLQ/Compensation/Reconciliation
12. ADM/BZA Browser E2E
13. Multi-instance/target-down/retry/failover
14. Evidence sanitize/commit 대상 검토
15. Post-run repository hygiene

## 사람 확인을 한 번에 모을 항목
- ADM/BZA 반응형 화면과 권한별 메뉴
- 위험조치 승인/Break-glass UX
- 로그 Raw/Formatted/Timeline 탐색성
- Calendar 관리→Batch 수행 시뮬레이션 일치
- 설치/복구 Guide 실제 명령 가독성

`verify-full-product.ps1 -WithDatabase -WithGeneratorLifecycle -WithBrowser -RequireAll`이 최종 진입점이며 SKIPPED가 있으면 완료가 아니다.

## R11 추가 통합 검증 시나리오

### Public Boundary / Generator
- `verify-r11-source-product.ps1` 전 Gate PASS
- arbitrary Domain 생성 결과 `com.cpf.core.common.*` import 0건
- 생성 Domain의 Online API 호출 시 Public `CpfOnlineTransaction`이 AOP/Execution Catalog에 실제 등록
- HTTP/DB/Broker/FileTransfer/ServiceCall Public API가 동일 JVM과 분리 WAS 양쪽에서 동작

### Utility / Paging / Fixed-Length
- JSON ↔ DTO/Map/List 왕복
- Fixed-Length ↔ Map/List/JSON 왕복, 한글 byte 길이, 반복 Group, 민감필드 Masking
- Offset `CpfPage`와 Keyset `CpfCursorPage`의 정렬/경계/빈 페이지/다음 Cursor 검증
- ADM Log 상세에서 명시 Layout metadata가 있을 때만 Fixed-Length field/group 표시, metadata 없을 때 raw-only

### CMN Code/Config/Message/Calendar
- ADM 변경 직후 동일 인스턴스 조회 반영
- 다중 인스턴스에서 cache invalidation/change publisher adapter 반영
- 변경 전파 adapter 장애가 원 Calendar 원장 mutation을 실패처럼 오염시키지 않는지 확인
- Calendar 변경 → Batch schedule simulation/실행 결과 일치

### BAT / Center-Cut
- BAT standalone bootJar 기동
- 동일 JVM Handler + Remote Domain Handler
- target-down / timeout / retry / failover
- `UNKNOWN_RESULT`가 성공/실패로 변조되지 않고 ADM에서 재확인/수동확정/재처리 가능한지 확인
- lease/fencing/primary-secondary/ghost takeover/failed-only reprocess

### ADM / BZA / Gateway
- ADM 인증 후 권한 0건 사용자가 메뉴 전체를 볼 수 없는지 확인
- BZA menu/button permission과 unauthorized route 직접 접근 차단
- 공통 Code select/Date range의 keyboard/accessibility/validation/반응형 동작
- Gateway 외부 진입, 내부 Domain 간 direct local/remote 호출, S 거래 외부 차단
- Gateway target-down/failover/표준 Header/transactionId 전달

### Build / CI
- `build-module-set.ps1`로 단일 Domain, 복수 Domain, Platform+Domain 조합, `-Full` 검증
- Jenkins Pipeline에서 동일 Source Gate 후 선택 Module/전체 Build
- JDK 25/Gradle Wrapper/Frontend Node LTS 기준을 Evidence에 기록
