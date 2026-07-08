# CPF CoreFlow Platform Framework

CPF는 Java 21, Spring Boot 3.4 기반의 업무 개발 표준 프레임워크입니다. 목표는 신규 업무 모듈을 만들 때 PFW 코어, 프로젝트 공통, 운영 콘솔, 배치 worker, 외부 연계, EDU 샘플을 같은 규칙으로 제공하는 것입니다.

## 기준 문서

| 문서 | 용도 |
| --- | --- |
| `CPF_FINAL_TARGET_REQUIREMENTS.md` | CPF 최종 목표와 장기 체크포인트 |
| `CPF_NEW_REQUEST.md` | 현재 작업 요청서. 작업 기준으로만 읽고 직접 수정하지 않습니다. |
| `CPF_REVIEW_PROGRESS_COMPLETION_GUIDE.md` | 리뷰와 완료 판정 기준 |
| `CPF_STABILIZATION_REPORT.md` | 최근 작업 결과와 실행/미실행 검증 기록 |
| `CPF_GAP_MATRIX.md` | 남은 gap과 우선순위 |
| `CPF_EVIDENCE_INDEX.md` | 검증 증적 파일 인덱스 |
| `specs/기능_구현_매트릭스.html` | 기능별 구현/검증 상태 매트릭스 |
| `specs/index.html` | 상세 가이드 진입점 |

상세 HTML 가이드는 내부 anchor 기준으로도 관리합니다. 배치 개발 표준은 `batch-development-guide`, 운영 절차는 `operation-runbook` 섹션을 기준으로 확장합니다.

## 모듈 책임

| 모듈 | 책임 |
| --- | --- |
| `pfw` | 프레임워크 코어입니다. 표준 헤더, 거래 ID, 거래/파일 로그, 공통 응답/예외, OpenAPI, HTTP client, 서비스 호출 엔진, 배치 공통 API, 로그 정책, Trace Boost, PFW 자체 메시지/코드/설정을 제공합니다. |
| `cmn` | 프로젝트 공통 영역입니다. CPF를 적용하는 프로젝트에서 업무 공통 기능과 EDU 샘플을 확장하는 공간입니다. |
| `adm` | 운영 콘솔입니다. 운영자 권한, 메뉴/버튼/API 권한, 회원 운영, 로그, 배치, 캐시, 메시지, 코드, 설정, 보안 운영 기능을 제공합니다. |
| `bat` | Spring Batch worker 모듈입니다. `BatApplication`으로 standalone bootJar를 만들고 PFW batch 공통 API와 배치 운영 메타를 사용합니다. |
| `acc`, `mbr`, `bizadm`, `exs` | 계정, 회원, 업무 관리자, 외부 연계 업무 샘플 모듈입니다. |
| `xyz` | 개발자 교육용 EDU 샘플 모듈입니다. Controller, Service, Repository, Mapper, Test 예제를 제공합니다. |

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
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/export-sanitized-evidence.ps1 -EvidenceDir specs/evidence/20260707_02

powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-mariadb-full-install.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-standard-header-e2e.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-service-registry-runtime.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-service-call-engine-runtime.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-service-registry-health-runtime.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-service-call-engine-circuit-runtime.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-service-call-engine-failover-runtime.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-adm-service-registry-runtime.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-adm-service-registry-ui-static.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-service-call-boundary.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-file-log-standard-runtime.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-trace-boost-runtime.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-bat-runtime.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-log-policy-runtime.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-composite-transaction-runtime.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-adm-transaction-group-runtime.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-create-domain.ps1
```

실행하지 않은 검증은 완료로 기록하지 않습니다. 기능을 추가하거나 변경하면 코드, SQL, Swagger/OpenAPI, EDU 샘플, README, 상세 가이드, 기능 구현 매트릭스, 증적 인덱스, 안정화 리포트를 함께 현행화합니다.
