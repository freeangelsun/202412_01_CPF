# CPF CoreFlow Platform Framework

CPF는 Java 21, Spring Boot 3.4 기반의 업무 개발 표준 프레임워크입니다. 프레임워크 코어, 프로젝트 공통, 운영 콘솔, 배치 worker, 외부 연계, 업무 샘플, EDU 샘플을 같은 규칙으로 묶어 신규 업무 개발의 기준을 제공하는 것을 목표로 합니다.

상세 목표와 완료 기준은 `CPF_FINAL_TARGET_REQUIREMENTS.md`가 기준입니다. 최근 안정화 결과와 미검증 항목은 `CPF_STABILIZATION_REPORT.md`, `CPF_GAP_MATRIX.md`, `CPF_EVIDENCE_INDEX.md`에서 확인합니다.

## 모듈 책임

| 모듈 | 책임 |
| --- | --- |
| `pfw` | CPF 프레임워크 코어입니다. 표준 헤더, 거래 ID, 거래 로그, 파일 로그, 공통 응답/예외, OpenAPI, HTTP client, Batch 공통 API, 로그 정책, Trace Boost, CPF 자체 메시지/코드/설정 기능을 제공합니다. |
| `cmn` | 프로젝트 공통 영역입니다. CPF를 적용하는 프로젝트에서 업무 공통 채번, 업무 알림, 업무 로그, 전문/MQ 보조 기능처럼 프로젝트별 공통 기능을 확장하는 공간입니다. |
| `adm` | 운영 콘솔입니다. 운영자 권한, 메뉴, 버튼, API 권한, 회원 운영, 로그, 배치, 캐시, 메시지, 코드, 설정, 보안 운영 기능을 조회하고 조치합니다. |
| `bat` | Spring Batch worker 모듈입니다. `BatApplication`으로 standalone bootJar를 만들고 PFW Batch 공통 API와 CPF 배치 운영 메타를 함께 사용합니다. |
| `xyz` | 개발자 교육용 EDU 샘플 모듈입니다. 운영 코드와 분리된 Controller, Service, Repository, Mapper, Test 예제를 제공합니다. |
| `acc`, `mbr`, `bizadm`, `exs` | 계정, 회원, 업무 관리자, 외부 연계 업무 샘플 모듈입니다. |

## 주요 문서

| 문서 | 용도 |
| --- | --- |
| `specs/index.html` | 전체 문서 진입점 |
| `specs/개발_가이드.html` | 신규 업무 API, CRUD, validation, paging, transaction, EDU 샘플 사용법 |
| `specs/관리자_가이드.html` | ADM 운영 기능, 권한, 로그, 배치, 캐시, 보안 운영 절차 |
| `specs/아키텍처_가이드.html` | PFW/CMN/ADM/BAT/업무 모듈 경계와 MSA-first 구조 |
| `specs/표준_헤더_가이드.html` | 온라인 거래 표준 헤더와 전파 규칙 |
| `specs/배치_개발_가이드.html` | `batch-development-guide`, Spring Batch, BAT worker, center-cut 기준 |
| `specs/운영_매뉴얼.html` | `operation-runbook`, runtime, 로그, 장애 대응, smoke 절차 |
| `specs/SQL_가이드.html` | 설치 SQL, Flyway, MariaDB 검증 기준 |
| `specs/기능_구현_매트릭스.html` | 기능별 구현 상태와 검증 상태 |

## 기본 검증

```powershell
.\gradlew.bat clean :acc:bootJar :mbr:bootJar :adm:bootJar :exs:bootJar :bat:bootJar --offline --no-daemon --console=plain --rerun-tasks
.\gradlew.bat qualityGate --offline --no-daemon --console=plain

powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-utf8.ps1 -CheckMojibake
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-html-docs.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-feature-evidence.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-report-matrix-evidence-consistency.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-sql-standard.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-packaged-runtime-resources.ps1
```

## 주요 smoke

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/runtime-start-services.ps1 -Modules ACC,MBR,EXS,ADM,BAT -BuildBeforeRun -NoExitOnFailure
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-runtime-closure.ps1 -Modules ACC,MBR,EXS,ADM,BAT
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/export-sanitized-evidence.ps1 -EvidenceDir specs/evidence/20260707_01

powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-mariadb-full-install.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-standard-header-e2e.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-file-log-standard-runtime.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-trace-boost-runtime.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-bat-trace-boost-runtime.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-bat-runtime.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-log-policy-runtime.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-composite-transaction-runtime.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-adm-transaction-group-runtime.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-create-domain.ps1
```

실행하지 않은 검증은 완료로 기록하지 않습니다. 기능을 추가하거나 변경하면 코드, SQL, Swagger/OpenAPI, EDU 샘플, README, 관련 가이드, 기능 구현 매트릭스, 안정화 리포트를 함께 갱신합니다.

<!-- evidence: batch-development-guide operation-runbook scripts/smoke-log-policy-runtime.ps1 scripts/smoke-bat-runtime.ps1 scripts/smoke-file-log-standard-runtime.ps1 scripts/smoke-trace-boost-runtime.ps1 scripts/smoke-create-domain.ps1 scripts/smoke-composite-transaction-runtime.ps1 scripts/smoke-adm-transaction-group-runtime.ps1 BatApplication -->
