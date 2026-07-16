# CPF 최종 통합 안정화 보고서

## 0. 2026-07-16 요구사항 확장 기록 — 구현 판정 아님

`CPF_FINAL_TARGET_REQUIREMENTS.md`와 `CPF_NEW_REQUEST.md`에 채널·거래 테스트·정책 파일 승격·ADM 전체 공통 승인결재·ADM/BAM 반응형·로그 원문/포맷·거래/채널 통계 및 CPF 전체 횡단 상용화 요구사항을 추가했다.

이번 문서 갱신은 요구사항 정합성 보강이며 source, SQL, OpenAPI, browser, MariaDB, broker, multi-instance 실행을 새로 검증한 결과가 아니다.

따라서:

- 2026-07-15 evidence는 당시 범위의 역사적 근거다.
- 신규 또는 확장된 요구사항에는 그대로 사용할 수 없다.
- 기존 `완료` 항목 중 신규 정본과 직접 충돌하거나 문서 변경으로 stale가 된 항목은 `재확인 필요`로 재판정한다.
- 신규 요구사항은 Codex 구현 후 최신 master에서 source/test/SQL/UI/runtime/evidence를 직접 확인해야 한다.
- 아래 신규 ledger 행의 `없음`은 실패가 아니라 구현·직접 검증 전 상태를 의미한다.


## 1. 실제 변경 요약

이번 작업은 초기 배포 기준을 `pfw`, `cmn`, `mbr`, `adm`, `bza`, `xyz`와 선택 실행 `bat`로 재정립했다. `BIZADM`은 `BZA`로 전환했고 `ACC`, `EXS`는 baseline 소스·설정·배포·SQL에서 제거했다.

PFW에는 비밀번호 hash port, 온라인·배치 표준 실행 ID와 시작 시 카탈로그 등록, AI provider·embedding·vector port, 첨부 저장 port, registry 기반 원격 로그 라우팅과 비동기 ZIP 작업 capability를 추가했다. ADM에는 표준 실행·원격 로그 조회·진단·비동기 ZIP 생성·1회성 다운로드 token API/UI를 연결했고 BZA에는 DB 인증, access/refresh token 회전, 조직·직원·사용자·역할·메뉴·권한 write, 결재·감사, 알림·첨부·저장 검색·다운로드 감사·권한 분석을 구성했다. 주제영역 생성기는 온라인·배치·BZA 메뉴·레지스트리 후보까지 생성하도록 확장했다.

## 2. 완료

- BIZADM 소스·설정·환경파일을 BZA로 전환하고 `bzaDB`, `bza_*` SQL을 기준으로 통일했다.
- ACC·EXS 실행 모듈과 직접 참조를 초기 배포 baseline에서 제거했다.
- PFW PBKDF2-HMAC-SHA256 버전 hash와 legacy 검증·재hash 계약을 ADM/BZA에서 사용한다.
- BZA refresh token을 DB hash로 보관하고 조건부 폐기 후 회전해 동시 재사용을 차단한다.
- BZA 로그인 이력은 `USER:READ` 서버 권한을 요구하고 정지·잠금 계정은 현재 사용자 API에서도 거부한다.
- BZA 사용자·역할·메뉴·버튼/API 권한 등록·수정은 서버 `WRITE` 권한, PFW 비밀번호 hash, 감사 사유와 before/after 기록을 적용한다.
- PFW AI port와 XYZ deterministic AI EDU에서 구조화 출력, streaming, tool call, injection 방어, retry·fallback, token 사용량, RAG·출처와 사람 승인을 테스트한다.
- 온라인·배치 표준 실행 ID 322건의 형식과 전역 중복을 빌드 게이트에서 검사한다.
- MBR·ADM·BZA·XYZ를 최신 JAR로 실제 기동해 네 포트와 `/v3/api-docs` HTTP 200을 확인했다.
- OpenAPI 런타임 검증에서 ADM 154, MBR 11, BZA 35, XYZ 70개 path와 필수 tag/path를 확인했다.
- XYZ AI EDU 구조화 응답 API를 CPF 필수 헤더와 34자리 거래 ID로 호출해 HTTP 200, tool call과 token 사용량을 확인했다.
- 도메인 생성 결과를 별도 임시 경로에서 test, bootJar, Java class major 69까지 검증하고 임시 디렉터리를 정리했다.
- 분할 SQL에서 `00_all_install.sql`, `00_all_install_and_smoke.sql`, Flyway V1 baseline을 재생성하고 SQL 정적 표준 검사를 통과했다.
- 저장소 JUnit 137 suites, 316 tests는 failures 0, errors 0이며 환경형 4건은 skipped 상태를 유지한다.

## 3. 부분 구현

- BZA는 조직·직원 등록/수정, 사용자·역할·메뉴·권한·고객·상품·주문·설정·다운로드 정책 조회, 결재 상태 전이와 감사를 제공한다. 실제 DB 데이터 기반 브라우저 업무 흐름은 미검증이다.
- BZA UI는 실효 메뉴·버튼 권한을 반영하고 모든 조회 메뉴와 사용자·역할·메뉴·권한 등록·수정 dialog를 연결했다. 인증 후 browser 업무 흐름과 대량 작업 UX는 미검증이다.
- ADM 원격 로그는 allowlist root, 경로 이탈·symlink·확장자·크기 제한, 마스킹 preview, registry node 라우팅, timeout·부분 실패 진단, 비동기 checksum ZIP, 소유자 격리, 요청 한도와 1회성 다운로드 token을 제공한다. 실 mTLS 원격 HTTP client와 공유 queue/rate-limit adapter를 사용한 다중 서버 E2E는 미검증이다.
- 표준 실행 카탈로그는 DB 비가용 시 메모리로 기동하고 DB 가용 시 `pfw_standard_execution`에 등록한다. 실제 MariaDB 영속 등록은 미검증이다.
- Spring Batch, scheduler, 선후행·trigger, lease, ghost 조치 API/UI와 EDU는 소스·단위 테스트 기준으로 제공한다. JobRepository 실 DB 시나리오는 미검증이다.
- broker, 서비스 호출, 파일전송 capability와 deterministic adapter는 구현돼 있으나 실 외부 인프라 검증은 수행하지 않았다.

## 4. 미구현

- 원격 로그의 운영용 HTTP/mTLS node client와 Redis·DB 기반 공유 작업 queue·cluster rate-limit adapter.
- 외부 object storage·백신 검사 첨부 adapter와 BZA 대량 편집 UX. 현재 로컬 첨부 adapter와 개별 업무 처리 기능은 완료했다.

## 5. 미검증

- MariaDB full install, 반복 설치, FK/index/comment/seed, app DML·DDL 금지, migration DDL, Flyway upgrade와 BZA migration.
- ADM/BZA bootstrap, 로그인, 최초 비밀번호 변경, 잠금·해제, 200/403, 결재·감사·다운로드의 인증 후 브라우저 E2E.
- Redis, Kafka, RabbitMQ와 SFTP, FTP, FTPS, SCP, SSH 실제 서버 통합.
- 2개 이상 instance의 registry, failover, circuit, batch lease, worker claim, ghost 오탐, graceful shutdown.
- Microsoft Word 애플리케이션을 통한 DOCX 실제 열기. OpenXML 구조 검사는 통과했다.

## 6. 실패

최종 미해결 실패는 없다. 작업 중 발견한 실패와 조치는 다음과 같다.

| 발견 | 원인 | 조치 |
|---|---|---|
| ADM·XYZ 기동 실패 | 표준 실행 ID 7개 중복 | 전체 322개 ID를 고유화하고 빌드 중복 검사를 추가 |
| 런타임 하네스 중단 | 실패 결과의 `health` 미초기화 | 모든 모듈 결과 필드를 초기화하고 이후 모듈 검증을 계속 수행 |
| ADM 기동 지연 | DB 비가용 시 카탈로그 항목별 연결 재시도 | 첫 DB 실패에서 메모리 카탈로그로 장애 격리, 회귀 테스트 추가 |
| BZA OpenAPI 실패 | 검증 경로만 `/bza/api`로 작성 | 실제 정본 `/api/bza`로 스모크 계약 수정 |
| UTF-8 gate 실패 | OpenAPI 결과 JSON BOM | 결과 생성기를 UTF-8 no BOM으로 변경 |
| ADM UI static 실패 | 과거 `segment timeline` 단일 marker | 실제 `Timeline`, `Segments` 탭을 개별 검증 |
| 최종 quality gate 증적 검사 실패 | ADM 로그 정책 결과 파일이 정제 증적 확장자 규칙과 불일치 | 생성기·런타임 요약·내보내기·Gradle task를 `*.sanitized.json`으로 통일하고 전체 gate 재통과 |
| OpenAPI 한글 tag 오검출 | Windows PowerShell 5.1이 응답 본문을 시스템 코드페이지로 해석 | 응답 byte를 UTF-8로 직접 해석하도록 스모크를 수정하고 4개 서비스 재검증 |
| 첨부 EDU 첫 수동 호출 500 | 필수 표준 헤더가 누락된 검증 명령 | 재현 스크립트에 표준 헤더를 고정하고 저장·재조회·checksum 일치 런타임 스모크를 추가 |

## 7. 재확인 필요

- 운영 profile에서 BZA JWT secret, DB secret, bootstrap 승인 변수가 배포 secret manager를 통해 주입되는지 확인해야 한다.
- 운영 로그 root를 공유 스토리지로 둘 경우 원격 로그 root allowlist와 보존 정책을 환경별로 검토해야 한다.
- BZA 데이터 범위 권한이 조직 계층과 실제 조회 SQL에 적용되는 범위는 후속 업무 정책에 맞춰 확장해야 한다.

- 2026-07-16 신규 정본 범위: channel registry/policy, O/S/B transaction test, policy package promotion, ADM 전체 공통 승인결재, 반응형·통계·raw/format log, configuration/secret/observability/schema/retention/DR/supply-chain/performance 전수 재확인
- 기존 16자리 standard execution ID evidence와 신규 10자리 목표의 충돌 재확인
- 변경된 요청서 hash, qualityGate, sample coverage, README/스펙, report/matrix/evidence consistency 재생성 필요

## 8. 실행한 검증

| 검증 | 결과 | 증적 |
|---|---|---|
| 전체 Gradle test와 bootJar | 137 suites, 316 tests, failures 0, errors 0, skipped 4 | `specs/evidence/20260715_01/quality-gate.sanitized.log` |
| MBR·ADM·BZA·XYZ 기동/status/종료 | 4개 모두 HTTP 200, 종료 완료 | `specs/evidence/20260715_01/runtime-start-services.sanitized.log` |
| OpenAPI 런타임 | 4개 서비스 통과 | `specs/evidence/20260715_01/openapi-runtime.sanitized.log` |
| 도메인 생성기 | 생성·test·bootJar·major 69 통과 | `specs/evidence/20260715_01/create-domain.sanitized.log` |
| BZA UI/JS 정적 스모크 | 권한 메뉴, API route, session storage, Node 문법 통과 | `specs/evidence/20260715_01/bza-ui-static-result.sanitized.json` |
| AI EDU·원격 로그 라우팅 | deterministic AI와 node routing·부분 실패 ZIP 단위 테스트 통과 | `specs/evidence/20260715_01/quality-gate.sanitized.log` |
| AI EDU 실제 API | 필수 헤더·34자리 거래 ID로 구조화 응답 HTTP 200 확인 | `specs/evidence/20260715_01/ai-edu-runtime.sanitized.json` |
| 첨부 EDU 실제 API | 표준 헤더로 저장·재조회 HTTP 200, 파일 크기·SHA-256 일치 확인 | `specs/evidence/20260715_01/attachment-edu-runtime.sanitized.log` |
| SQL 표준·합본 생성 | 통과 | `specs/evidence/20260715_01/quality-gate.sanitized.log` |
| UTF-8·mojibake | 통과 | `specs/evidence/20260715_01/quality-gate.sanitized.log` |

## 9. 실행하지 못한 검증

MariaDB 인증 환경변수가 없어 DB 접속은 시도하지 않았다. 내장 브라우저가 현재 세션에 제공되지 않았고 ADM/BZA 인증정보도 없으므로 로그인 후 클릭 E2E를 실행하지 않았다. 외부 broker·파일 서버와 다중 instance 환경도 제공되지 않았다.

MariaDB 재현 명령:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-mariadb-full-install.ps1 -RequireRun
```

## 10. 주요 evidence

- 기준 디렉터리: `specs/evidence/20260715_01`
- 요청서 SHA-256: `c6d7ab01f45d2085dac270ff036d75d858e94d3feea674f9e1d2b6528557e454`
- 시작 commit: `0d206c683aab840dbab4639f2797dd7fd718cefd`
- quality gate: `quality-gate.sanitized.log`
- runtime: `runtime-start-services.sanitized.log`, `runtime-status.sanitized.log`, `runtime-stop-services.sanitized.log`
- OpenAPI: `openapi-runtime.sanitized.log`
- AI EDU runtime: `ai-edu-runtime.sanitized.json`
- MariaDB 미검증: `mariadb-full-install.sanitized.log`

## 11. 삭제·이동한 파일

- `bizadm/`을 제거하고 `bza/`로 전환했다.
- `acc/`, `exs/`와 관련 배포 환경 파일, 직접 호출 샘플을 baseline에서 제거했다.
- 구형 ACC·EXS·BIZADM runtime smoke와 복합 거래 wrapper 스크립트를 제거했다.
- 생성기 스모크의 임시 source·verification 디렉터리는 성공 후 자동 삭제한다.
- 사용자 변경인 `CPF_CODEX_HANDOFF.md` 삭제 상태와 `CPF_NEW_REQUEST.md` 수정은 되돌리지 않았다.

## 12. 남은 위험

가장 큰 위험은 소스·단위 테스트가 아니라 실제 운영 의존성 검증 공백이다. DB 인증, 실 broker·파일 서버, 2개 instance, 인증 후 브라우저를 준비하기 전까지 해당 기능을 운영 완료로 승격하면 안 된다. 원격 로그는 현재 로컬 adapter이므로 다중 서버 운영에서는 중앙 로그 수집 또는 secure remote adapter가 필요하다.

## 13. 작업트리 상태

- branch: `master`
- commit/push: 수행하지 않음
- 요청서: 읽기 전용으로 사용했으며 최종 SHA-256와 git blob이 작업 시작 기준과 일치함
- 사용자 선행 변경과 이번 변경이 함께 있는 dirty worktree이며 사용자 변경을 되돌리지 않음

## 기능별 상태 ledger

상태 정본은 `specs/기능_구현_매트릭스.json`이며 아래 구간은 동기화 스크립트가 생성한다.

<!-- CPF_LEDGER_BEGIN -->
| check id | 상태 | 핵심 증적 | 판정 |
|---|---|---|---|
| baseline-module-layout | 완료 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | 기본 배포 pfw·cmn·mbr·adm·bza·xyz와 선택 bat 구성으로 settings·배포 설정을 통일함 |
| bza-rename | 완료 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | BIZADM 소스·패키지·환경·DB 명칭을 BZA로 전환하고 legacy name gate를 통과함 |
| acc-exs-cleanup | 완료 | `specs/evidence/20260715_01/architecture-ownership-scan.sanitized.json` | ACC·EXS 모듈과 직접 참조를 baseline에서 제거함 |
| architecture-ownership | 완료 | `specs/evidence/20260715_01/architecture-ownership-scan.sanitized.json` | PFW 기술 코어, CMN 프로젝트 공통, ADM 프레임워크 운영, BZA 업무 운영 소유권 검사를 통과함 |
| password-hashing | 완료 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | PFW PBKDF2 버전 hash, legacy verify·rehash와 ADM/BZA 사용 테스트를 통과함 |
| bza-auth | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | 로그인·잠금·비밀번호 변경·access token은 구현·테스트 완료, bzaDB 실로그인은 미검증 |
| bza-refresh-rotation | 완료 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | refresh token hash 저장, 조건부 폐기, 회전과 동시 재사용 거부 테스트를 통과함 |
| bza-bootstrap | 미검증 | `specs/evidence/20260715_01/mariadb-full-install.sanitized.log` | 명시적 enable·승인·환경변수 기반 구현은 있으나 DB bootstrap 런타임은 미실행 |
| bza-permission-server | 완료 | `specs/evidence/20260715_01/bza-ui-static-result.sanitized.json`, `specs/evidence/20260715_01/quality-gate.sanitized.log` | 메뉴·행위 권한을 서버 필터에서 검사하고 로그인 이력 USER:READ 권한 테스트를 통과함 |
| bza-ui | 부분 구현 | `specs/evidence/20260715_01/bza-ui-static-result.sanitized.json` | 권한 기반 조회 메뉴와 사용자·역할·메뉴·권한 등록·수정 dialog를 연결했으나 인증 후 실제 browser E2E는 미검증 |
| bza-organization-employee | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | 조직·직원 조회 및 감사 사유 필수 등록·수정 API와 테스트는 완료, DB 런타임은 미검증 |
| bza-approval | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | 순차·병렬 결재선, 상태 전이, 낙관적 잠금, idempotency와 감사 테스트는 완료, DB E2E는 미검증 |
| bza-audit | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | BZA 업무 변경 before·after·reason 감사 적재와 조회를 구현했으나 실 DB 행은 미검증 |
| adm-framework-console | 부분 구현 | `specs/evidence/20260715_01/runtime-start-services.sanitized.log`, `specs/evidence/20260715_01/openapi-runtime.sanitized.log` | ADM 154개 OpenAPI path와 UI를 기동 확인, DB 기반 운영 데이터·인증 후 화면은 미검증 |
| adm-permission | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | 메뉴·버튼·API 권한 서비스 테스트는 통과했으나 계정별 200/403 런타임은 미검증 |
| adm-log-console | 부분 구현 | `specs/evidence/20260715_01/openapi-runtime.sanitized.log` | 거래·상세·감사·배치·운영 로그 API/UI는 제공하나 MariaDB 실데이터 조회는 미검증 |
| remote-log-local | 완료 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | allowlist root, 경로 이탈·symlink·확장자·크기 제한, SHA-256, 마스킹 preview와 다운로드 테스트를 통과함 |
| remote-log-multi-instance | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | registry·node client·service credential port, timeout·부분 실패, 라우팅 ID와 checksum ZIP 테스트는 완료했으나 실 mTLS HTTP adapter와 다중 서버 E2E는 미검증 |
| remote-log-bundle-jobs | 완료 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | 비동기 ZIP 작업 상태, 소유자 격리, 요청 한도, 부분 실패, 만료, 1회성 다운로드 token과 재발급을 구현하고 단위·ADM UI 정적 테스트를 통과함 |
| attachment-storage | 완료 | `specs/evidence/20260715_01/quality-gate.sanitized.log`, `specs/evidence/20260715_01/attachment-edu-runtime.sanitized.log` | PFW 첨부 저장 port와 로컬 adapter의 경로·symlink·확장자·크기·content type·SHA-256 검증, XYZ EDU 단위 테스트와 저장·재조회 HTTP 런타임을 통과함 |
| bza-operation-support | 부분 구현 | `specs/evidence/20260715_01/bza-ui-static-result.sanitized.json`, `specs/evidence/20260715_01/quality-gate.sanitized.log` | BZA 대시보드·알림·첨부·저장 검색·다운로드 감사·역할 비교·권한 시뮬레이션 API/UI와 테스트는 완료, 인증 후 DB browser E2E는 미검증 |
| standard-execution-id | 재확인 필요 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | 기존 16자리 검증은 역사적 근거이며 신규 정본의 O/S/B 10자리 ID와 alias/migration 전체를 다시 구현·검증해야 함 |
| standard-execution-catalog | 부분 구현 | `specs/evidence/20260715_01/runtime-start-services.sanitized.log`, `specs/evidence/20260715_01/quality-gate.sanitized.log` | 시작 스캔과 메모리 fallback 기동은 확인, pfw_standard_execution 실 DB 등록은 미검증 |
| execution-log-propagation | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | 표준 ID·transactionGlobalId의 로그 context 연계 테스트는 통과, 운영 DB·다중 instance는 미검증 |
| batch-standard | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | Spring Batch port, 실행·step·lock·운영 API와 EDU는 구현, JobRepository 실 DB는 미검증 |
| scheduler-dependency | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | 영업일·허용시간·시뮬레이션·선후행·trigger·실행대상 API/UI는 구현, DB 실행 시나리오는 미검증 |
| batch-ghost | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | heartbeat 기반 ghost 후보·조치·운영 로그를 구현, 다중 worker 오탐 검증은 미실행 |
| bat-runtime | 미검증 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | BAT compile·test·bootJar는 통과했으나 선택 실행 모듈 runtime과 DB job 실행은 이번에 미실행 |
| domain-generator | 완료 | `specs/evidence/20260715_01/create-domain.sanitized.log` | 온라인·배치·BZA 메뉴·서비스 레지스트리 후보 생성 후 test·bootJar·major 69를 통과함 |
| xyz-edu | 완료 | `specs/evidence/20260715_01/sample-coverage-result.sanitized.json` | XYZ/BAT 공개 capability 대비 EDU 샘플 카탈로그 49/49 매핑을 통과함 |
| bat-edu | 부분 구현 | `specs/evidence/20260715_01/sample-coverage-result.sanitized.json`, `specs/evidence/20260715_01/quality-gate.sanitized.log` | tasklet·chunk·retry·restart·idempotency·center-cut 샘플은 있으나 실 JobRepository 검증은 미실행 |
| ai-edu | 완료 | `specs/evidence/20260715_01/sample-coverage-result.sanitized.json`, `specs/evidence/20260715_01/quality-gate.sanitized.log`, `specs/evidence/20260715_01/ai-edu-runtime.sanitized.json` | PFW provider·embedding·vector port와 XYZ deterministic 구조화 출력·streaming·tool·RAG·fallback·token·사람 승인 테스트 및 표준 헤더를 포함한 HTTP 200 런타임 검증을 완료함. 실 provider는 외부 자격정보 항목으로 미검증 |
| standard-header | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | 헤더 검증·전파·로그 context 테스트와 EDU는 완료, 이번 세션 DB 로그·하위 E2E는 미실행 |
| service-call-engine | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | timeout·retry·failover·circuit 단위 테스트는 통과, 다중 instance 실 runtime은 미검증 |
| broker-capability | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | PFW broker port, outbox·inbox·DLQ와 adapter 테스트는 통과, 실 broker는 미검증 |
| broker-real-integration | 미검증 | `없음` | Redis·Kafka·RabbitMQ 서버가 제공되지 않아 실 장애·fallback·replay를 실행하지 않음 |
| file-transfer-capability | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | 파일 검증·checksum·임시파일·이동·이력·원격 명령 계획 테스트는 통과함 |
| file-server-real-integration | 미검증 | `없음` | SFTP·FTP·FTPS·SCP·SSH 실 서버가 없어 전송 runtime은 실행하지 않음 |
| mariadb-full-install | 미검증 | `specs/evidence/20260715_01/mariadb-full-install.sanitized.log` | CLI·서비스는 확인했으나 인증 환경변수가 없어 접속과 SQL 실행을 시도하지 않음 |
| flyway-static | 완료 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | V1 baseline과 V28~V31 변경 파일, naming·순서 정적 검사를 통과함 |
| sql-all-install | 완료 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | 분할 SQL에서 두 단일 실행 합본과 Flyway baseline을 재생성하고 mismatch 검사를 통과함 |
| runtime-baseline | 완료 | `specs/evidence/20260715_01/runtime-start-services.sanitized.log`, `specs/evidence/20260715_01/runtime-status.sanitized.log`, `specs/evidence/20260715_01/runtime-stop-services.sanitized.log` | MBR·ADM·BZA·XYZ 프로세스·포트·HTTP 200과 종료를 실제 확인함 |
| openapi-runtime | 완료 | `specs/evidence/20260715_01/openapi-runtime.sanitized.log` | ADM 154, MBR 11, BZA 35, XYZ 70개 path와 필수 tag/path를 검증함 |
| browser-public-http | 부분 구현 | `specs/evidence/20260715_01/runtime-start-services.sanitized.log` | ADM·BZA HTML HTTP 200은 확인했으나 내장 browser가 없어 실제 렌더링·console 검증은 미실행 |
| browser-auth-e2e | 미검증 | `없음` | DB·bootstrap 인증정보와 browser 연결이 없어 로그인 이후 E2E를 실행하지 않음 |
| multi-instance-runtime | 미검증 | `없음` | 2개 instance registry·failover·lease·worker claim·graceful shutdown 환경을 실행하지 않음 |
| security-static | 완료 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | 평문·raw SHA·기본 JWT secret·seed 보안과 secret scan을 통과함 |
| bza-session-storage | 완료 | `specs/evidence/20260715_01/bza-ui-static-result.sanitized.json` | BZA access·refresh token을 sessionStorage로 제한하고 localStorage 사용을 gate에서 차단함 |
| bza-login-history-auth | 완료 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | 로그인 이력 조회에 Bearer token과 USER:READ 서버 권한을 강제하는 테스트를 통과함 |
| service-call-boundary | 완료 | `specs/evidence/20260715_01/architecture-ownership-scan.sanitized.json` | 타 주제영역 Repository·Mapper·DB 직접 접근 금지 검사를 통과함 |
| spring-event-usage | 완료 | `specs/evidence/20260715_01/spring-event-usage-scan.sanitized.json` | 핵심 처리의 금지 Spring Event 사용 검사를 통과함 |
| profile-loading | 완료 | `specs/evidence/20260715_01/profile-loading-result.sanitized.json` | local·dev·stg·prod profile과 secret 기본값 금지 정적 계약을 통과함 |
| deploy-inventory | 완료 | `specs/evidence/20260715_01/runtime-config-inventory.sanitized.json` | BZA 기준 dev·stg·prod inventory와 환경 파일 정적 검사를 통과함 |
| sql-standard | 완료 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | prefix·공통 컬럼·COMMENT·FK·index·seed·합본 정적 검사를 통과함 |
| utf8-mojibake | 완료 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | 텍스트 UTF-8, PowerShell BOM/CRLF, mojibake와 생성 JSON no BOM 검사를 통과함 |
| ui-static | 완료 | `specs/evidence/20260715_01/bza-ui-static-result.sanitized.json`, `specs/evidence/20260715_01/adm-log-policy-ui-static-result.sanitized.json` | ADM 메뉴/API marker와 BZA 권한·전체 route·Node JS 문법 정적 스모크를 통과함 |
| sample-coverage | 재확인 필요 | `specs/evidence/20260715_01/sample-coverage-result.sanitized.json` | 기존 49개 capability 범위는 통과했으나 신규 channel·approval·promotion·횡단 capability의 실제 EDU/sample/test가 추가된 후 재생성해야 함 |
| generator-cleanup | 완료 | `specs/evidence/20260715_01/create-domain.sanitized.log` | 생성기 스모크 성공 후 임시 source·verification 디렉터리가 제거됨 |
| evidence-sanitization | 완료 | `specs/evidence/20260715_01/log-management-standard.sanitized.json` | 최종 근거 로그에 실행 메타데이터·secret 제거·본문 SHA-256을 적용함 |
| docx-openxml | 완료 | `specs/evidence/20260715_01/docx-standard.sanitized.json` | 공식 DOCX 9개의 OpenXML 구조 검사를 통과함. Word 애플리케이션 실제 열기는 미검증 |
| readme-docs | 재확인 필요 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | 기존 문서 검증은 신규 패턴·승인결재·채널·정책 승격·횡단 기능 문서화 이전이므로 구현 후 재검증 필요 |
| quality-gate | 재확인 필요 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | 기존 gate 결과는 신규 요구사항 이전 기준이며 approval 우회·snapshot·schema·retention·stale evidence 등 신규 규칙 추가 후 재실행 필요 |
| request-protection | 재확인 필요 | `specs/evidence/20260715_01/cpf-new-request-protection.sanitized.json` | CPF_NEW_REQUEST.md를 의도적으로 갱신했으므로 기존 hash evidence는 stale이며 새 baseline으로 재생성해야 함 |
| report-matrix-consistency | 재확인 필요 | `specs/evidence/20260715_01/report-matrix-evidence-consistency.sanitized.json` | 요구사항 확장과 신규 check ID 추가 후 report/gap/evidence/matrix를 다시 생성·검증해야 함 |
| channel-registry-policy | 재확인 필요 | 없음 | 통합 채널 마스터, 거래별 허용 채널, client/service binding, immutable snapshot, ADM UI와 runtime 차단 검증 필요 |
| transaction-test-console | 재확인 필요 | 없음 | O/S/B 테스트 콘솔, JUT, prod 강제 비활성, 권한·property·profile, 결과 포맷과 감사 검증 필요 |
| policy-package-promotion | 재확인 필요 | 없음 | 환경 독립 정책 파일 Export/Import, 원본 보관, diff, 사전 등록, source matching, rollback 검증 필요 |
| global-change-approval | 재확인 필요 | 없음 | ADM/BAM/BZA 전체 mutation inventory, 자동승인, 통합 결재함, 예약 적용, apply/rollback handler 검증 필요 |
| adm-bam-responsive-statistics | 재확인 필요 | 없음 | 전 화면 반응형, 거래별·채널별·성공/오류 통계, 교차 통계와 로그 drill-down 검증 필요 |
| log-raw-format | 재확인 필요 | 없음 | JSON/XML/text/fixed-length raw·formatted 조회, masking, 보안 원문 권한·감사·다운로드 검증 필요 |
| configuration-secret-lifecycle | 재확인 필요 | 없음 | 설정 catalog/version/drift/last-known-good와 secret·credential·certificate·key rotation 구현 및 runtime 검증 필요 |
| observability-alert-slo | 재확인 필요 | 없음 | metric·trace·health·SLI/SLO·alert·ack·runbook과 ADM/BAM 연계 검증 필요 |
| resource-protection | 재확인 필요 | 없음 | bulkhead·rate limit·quota·backpressure·retry budget·pool limit·overload 보호 검증 필요 |
| schema-versioning-migration | 재확인 필요 | 없음 | REST/event/file schema versioning, deprecation, compatibility, expand/contract, backfill·resume 검증 필요 |
| retention-privacy-dr | 재확인 필요 | 없음 | retention·archive·purge·privacy·backup·restore·RPO/RTO·DR 절차와 복구 검증 필요 |
| supply-chain-performance | 재확인 필요 | 없음 | SBOM·dependency/license/secret scan과 대표 경로 성능·용량 benchmark 필요 |
| full-capability-inventory | 재확인 필요 | 없음 | PFW/CMN/업무/BAT/BZA/ADM/BAM/Gateway/DB/broker/file/UI 전체 owner·상태·source·test·evidence inventory 필요 |
<!-- CPF_LEDGER_END -->

## 항상 지켜야 할 기준 점검

| 기준 | 판정 |
|---|---|
| 문서·소스·SQL·OpenAPI·EDU 정합성 | 정적 gate와 ledger 동기화로 확인 |
| README는 짧은 진입점 | 반영 |
| 관련 가이드 동시 현행화 | README·matrix·증적 인덱스·GAP·검증 리포트와 DOCX 재생성 완료 |
| 신규 주석·설명 한글 | 반영 |
| EDU와 실제 engine·test 연결 | sample coverage 49/49 통과 |
| 실행하지 않은 검증 성공 보고 금지 | DB·browser·multi-instance·external을 미검증 유지 |
| 민감정보 원문 금지 | 정제 evidence만 최종 근거로 사용 |
| 요청서 무수정 | hash·git blob 보호 gate로 재확인 |
