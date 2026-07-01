# CPF 안정화 리포트

## 1. 기준 정보

| 항목 | 값 |
|---|---|
| 요청서 | `CPF_NEW_REQUEST.md` |
| 작업 범위 | CPF_M3_2_REQUEST_20260701_05, ADM 권한 기능 최소 Runtime 안정화 및 M4 진입 준비 |
| 작업 시작 branch | `master` |
| 작업 시작 HEAD | `3bcc79c87692f70b243b6d561ff2e9db5ca492dc` |
| 작업 시작 origin/master | `3bcc79c87692f70b243b6d561ff2e9db5ca492dc` |
| 작업 시작 status | `M CPF_NEW_REQUEST.md`, `M CPF_STABILIZATION_REPORT.html` |
| 작업 종료 HEAD | `3bcc79c87692f70b243b6d561ff2e9db5ca492dc` |
| 작업 종료 status | `CPF_NEW_REQUEST.md` 수정 상태는 사용자 요청 원본으로 제외, `CPF_STABILIZATION_REPORT.html` 삭제, `CPF_STABILIZATION_REPORT.md` 생성, smoke/게이트/기능 매트릭스 수정 |
| ADM 8090 포트 | 작업 종료 후 listener 없음 |
| 사용자 요청 원본 파일 | `CPF_NEW_REQUEST.md`는 검토 전용으로만 사용했고 작업 대상에서 제외 |
| 커밋/푸시/브랜치 | 수행하지 않음 |
| 별도 변경 파일 목록 산출물 | 생성하지 않음 |

## 2. 작업 전 리뷰

이번 요청은 M3에서 구현한 ADM 권한 관리 기능을 새 기능 확장보다 운영 위험 구간 중심으로 안정화하는 작업이다. 완료 판단은 소스 존재가 아니라 권한 쓰기 API, 감사 로그 적재, API 권한 필터 차단/허용을 실제 ADM runtime에서 확인했는지 여부로 보았다.

`CPF_STABILIZATION_REPORT.html`은 삭제하고, 내부 검수와 AI 검증에 맞게 `CPF_STABILIZATION_REPORT.md`를 새 증적 파일로 전환했다. 상세 가이드 문서는 기존 HTML 가이드 체계를 유지하고, 안정화 리포트만 Markdown으로 분리했다.

## 3. 구현 재확인

| 영역 | 상태 | 증적 |
|---|---|---|
| ADM 권한 Controller | 완료 | `adm/src/main/java/cpf/adm/opr/controller/AdmPermissionController.java` |
| ADM 권한 Service | 완료 | `adm/src/main/java/cpf/adm/opr/service/AdmPermissionService.java` |
| ADM API 권한 필터 | 완료 | `adm/src/main/java/cpf/adm/opr/filter/AdmApiAuthFilter.java` |
| ADM 권한 DTO | 완료 | `adm/src/main/java/cpf/adm/opr/dto/AdmApiPermission.java` 등 |
| V15 SQL/Flyway | 완료 | `specs/sql/migration/flyway/V15__adm_api_permission_management.sql` |
| 권한 단위 테스트 | 완료 | `adm/src/test/java/cpf/adm/opr/service/AdmPermissionServiceTest.java` |
| 권한 runtime smoke | 완료 | `scripts/smoke-adm-permission-runtime.ps1`, `build/runtime-smoke/adm-permission-runtime-result.json` |

## 4. 이번 변경 요약

| 구분 | 내용 |
|---|---|
| Runtime smoke | `scripts/smoke-adm-permission-runtime.ps1` 추가 |
| ADM runtime 확장 | `scripts/smoke-adm-runtime.ps1`에 `permissionWriteApi` 선택 검증 추가 |
| V15 적용 | `scripts/apply-v15-adm-api-permission-management.ps1` 추가, MariaDB CLI 없이 JDBC로 V15 idempotent 적용 |
| Smoke 계정 | `smoke_viewer_runtime` 고정 계정을 사용해 반복 실행 시 계정 증가를 줄임 |
| 리포트 전환 | `CPF_STABILIZATION_REPORT.html` 삭제, `CPF_STABILIZATION_REPORT.md` 생성 |
| 품질 게이트 | `check-html-docs.ps1`, `check-feature-evidence.ps1`를 Markdown 리포트 기준으로 갱신 |
| 기능 매트릭스 | `adm-permission-runtime` 검증 행 추가 |

## 5. 권한 쓰기 API runtime smoke

| 항목 | 결과 |
|---|---|
| 실행 스크립트 | `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\smoke-adm-permission-runtime.ps1` |
| 결과 파일 | `build/runtime-smoke/adm-permission-runtime-result.json` |
| smoke 계정 준비 | `POST /adm/api/operators`, `smoke_viewer_runtime`, HTTP 200 |
| 메뉴 권한 저장 | `PUT /adm/api/permissions/roles/ADM_VIEWER/menus/PERMISSION`, HTTP 200 |
| 역할별 API 권한 저장 | `PUT /adm/api/permissions/roles/ADM_VIEWER/api-permissions/API_PERMISSION_WRITE_PUT`, HTTP 200 |
| 변경 후 조회 | `/adm/api/permissions/api-matrix`, `ADM_VIEWER` + `API_PERMISSION_WRITE_PUT` + `ALLOW_YN=N` 확인 |
| 데이터 오염 관리 | 매 실행마다 새 계정을 만들지 않고 `smoke_viewer_runtime` 계정을 재사용 |

## 6. 감사 로그 검증

| 항목 | 결과 |
|---|---|
| 확인 방식 | `GET /adm/api/audit-logs?actionType=ROLE_API_PERMISSION_UPDATE&targetType=adm_role_api_permission&targetId=ADM_VIEWER%3AAPI_PERMISSION_WRITE_PUT&limit=5` |
| 감사 로그 적재 | 완료 |
| matchedRows | 2 |
| 감사 사유 | 존재 확인 |
| before/after/diff | 하나 이상 존재 확인 |
| 민감정보 원문 | 리포트와 result JSON에 비밀번호 원문을 기록하지 않음 |

## 7. API 권한 필터 검증

| 케이스 | 결과 |
|---|---|
| ADM_ADMIN 허용 | `GET /adm/api/permissions/api-permissions`, HTTP 200 |
| ADM_VIEWER 차단 | `PUT /adm/api/permissions/roles/ADM_VIEWER/api-permissions/API_PERMISSION_WRITE_PUT`, HTTP 403 |
| 검증 방식 | 실제 로그인 세션을 발급해 `AdmApiAuthFilter`가 runtime에서 차단/허용하는지 확인 |

## 8. V15/Flyway 확인

| 항목 | 결과 |
|---|---|
| mysql CLI | 미검증, PATH에서 찾지 못함 |
| mariadb CLI | 미검증, PATH에서 찾지 못함 |
| 대체 검증 | `scripts/apply-v15-adm-api-permission-management.ps1`로 JDBC 적용 |
| 적용 계정 | `cpf_adm_migration` |
| 결과 파일 | `build/runtime-smoke/v15-adm-api-permission-result.json` |
| 1차 적용 후 테이블 | `adm_api_permission=1`, `adm_role_api_permission=1` |
| idempotent | 완료, 1차/2차 적용 후 count 동일 |
| full install | 미검증, 신규 빈 MariaDB 전체 설치는 이번 범위가 아님 |

## 9. 검증 상태 매트릭스

| checkId | 상태 | 증적 | 비고 |
|---|---|---|---|
| adm-runtime | 완료 | `scripts/smoke-adm-runtime.ps1`, `build/runtime-smoke/adm-runtime-smoke-result.json` | ADM BOOT_JAR 기동, health, 조회 API, batch API, transaction meta, 정적 UI marker, cleanup 확인 |
| adm-permission-runtime | 완료 | `scripts/smoke-adm-permission-runtime.ps1`, `build/runtime-smoke/adm-permission-runtime-result.json` | 쓰기 API, 감사 로그, API 필터 403/200 확인 |
| openapi-runtime | 완료 | `scripts/smoke-adm-runtime.ps1` 내부 `scripts/smoke-openapi.ps1` 호출 | 앱 기동 상태에서 `/v3/api-docs` 확인 완료. 앱 미기동 상태의 단독 `smoke-openapi.ps1` 실행은 연결 실패 |
| adm-browser-click | 미검증 | `scripts/smoke-adm-ui.ps1 -BrowserClick` | 브라우저 자동화 런타임이 없어 실제 클릭은 실행하지 않음 |
| standard-header-e2e | 완료 | `CpfStandardHeaderE2eTest` | Source-Level 검증이며 Runtime E2E로 확대하지 않음 |
| edu-mapper-db-slice | 재확인 필요 | `XyzQueryEducationMapperSliceTest`, `xyz_edu_query_fixture.sql` | 다른 PC에서는 `CPF_XYZ_EDU_MAPPER_DB_USERNAME`와 `CPF_XYZ_EDU_MAPPER_DB_USER` 호환 설명에 따라 재실행 필요 |
| mariadb-full-install | 미검증 | `00_all_install.sql`, `00_all_install_and_smoke.sql` | 신규 빈 MariaDB 전체 설치 검증은 별도 마일스톤 |
| redis-kafka-mq-broker | 미검증 | 실 broker 환경 | local fallback 성공으로 대체하지 않음 |
| check-feature-evidence | 완료 | `scripts/check-feature-evidence.ps1` | 최종 quality gate 대상 |
| check-html-docs | 완료 | `scripts/check-html-docs.ps1` | 루트 안정화 리포트는 Markdown으로 검사 |
| check-utf8 | 완료 | `scripts/check-utf8.ps1 -CheckMojibake` | 최종 quality gate 대상 |
| quality-gate | 완료 | `.\gradlew.bat qualityGate --offline --no-daemon --console=plain` | 최종 실행 후 결과 기준 |

## 10. 실행 명령 결과

| 명령 | 결과 | 비고 |
|---|---|---|
| `.\gradlew.bat :adm:test --offline --no-daemon --console=plain` | 완료 | BUILD SUCCESSFUL |
| `.\gradlew.bat :pfw:test --offline --no-daemon --console=plain` | 완료 | BUILD SUCCESSFUL |
| `.\gradlew.bat :adm:bootJar --offline --no-daemon --console=plain` | 완료 | BUILD SUCCESSFUL |
| `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\smoke-adm-permission-runtime.ps1` | 완료 | V15 JDBC 적용, ADM runtime, 권한 쓰기, 감사, 필터 확인 |
| `.\gradlew.bat test --offline --no-daemon --console=plain` | 완료 | BUILD SUCCESSFUL |
| `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\smoke-openapi.ps1` | 실패 | 앱을 띄우지 않은 단독 실행이라 연결 실패. runtime smoke 내부에서는 완료 |
| `where.exe mysql` | 미검증 | CLI 없음 |
| `where.exe mariadb` | 미검증 | CLI 없음 |

## 11. 완료 불인정 항목 자체 점검

| 점검 항목 | 결과 |
|---|---|
| 실행하지 않은 검증을 완료로 기록했는가 | 아니오 |
| 브라우저 클릭을 완료로 기록했는가 | 아니오, 미검증 유지 |
| 신규 빈 MariaDB full install을 완료로 기록했는가 | 아니오, 미검증 유지 |
| 표준 헤더 Runtime E2E를 완료로 기록했는가 | 아니오, Source-Level 완료로만 기록 |
| 민감정보 원문을 리포트/result JSON에 기록했는가 | 아니오 |
| Git commit/push/branch를 수행했는가 | 아니오 |
| 별도 변경 파일 목록 산출물을 생성했는가 | 아니오 |

## 12. 남은 리스크와 다음 작업

| 우선순위 | 항목 | 상태 | 다음 조치 |
|---|---|---|---|
| 1 | MariaDB 신규 빈 DB full install | 미검증 | 안전한 빈 DB를 지정해 `00_all_install_and_smoke.sql` 직접 실행 |
| 2 | ADM browser click | 미검증 | Node/npm/npx/Playwright 또는 Chrome/Edge 준비 후 `smoke-adm-ui.ps1 -BrowserClick -RequireBrowserClick` 실행 |
| 3 | 기존 동적 smoke 데이터 | 재확인 필요 | 이전 실험 중 생성된 `smoke_viewer_yyyyMMdd...` 계정 정리 정책 결정 |
| 4 | M4 BAT | 다음 개발 | standalone worker, heartbeat, ghost 감지, center-cut 개발로 진입 |
