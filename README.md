# CPF CoreFlow Platform Framework

CPF는 Java 21, Spring Boot 3.4 기반의 업무 개발 표준 프레임워크입니다. 신규 업무 모듈이 표준 헤더, 거래 추적, 공통 응답, 예외 처리, 거래 로그, 권한, 감사, 배치, 외부 연계, 운영 콘솔을 같은 방식으로 구현하도록 공통 코드와 운영 기준을 제공합니다.

이 파일은 짧은 진입점입니다. CPF의 최상위 목표와 완료 기준은 `CPF_FINAL_TARGET_REQUIREMENTS.md`를 기준으로 삼고, 각 작업 요청서는 이 기준 아래에서 마일스톤 단위로 작성합니다.

상세 현행 가이드는 기존 `specs` 문서에서 확인합니다. 중간 개발/검수 산출물은 Markdown 원본을 우선하며, 기존 HTML 가이드는 최종 정본화 전까지 유지할 수 있습니다.

## 모듈 책임

- `pfw`: 프레임워크 코어입니다. 표준 헤더, 거래 ID, 거래 로그, 공통 응답/예외, OpenAPI, HTTP client, Batch 공통 API, 로그 정책, 메시지/코드/설정 같은 CPF 자체 기능을 담당합니다.
- `cmn`: 프로젝트 공통 영역입니다. CPF를 적용한 프로젝트에서 업무 공통 채번, 업무 알림, 업무 로그, 파일/전문/MQ 보조 기능을 확장하는 곳입니다.
- `adm`: 운영 콘솔입니다. 운영자, 역할, 메뉴, 버튼, API 권한, 회원 운영, 로그, 배치, 캐시, 메시지, 코드, 설정, 보안 운영 기능을 조회하고 조치합니다.
- `bat`: Spring Batch Job을 실제 실행하는 worker입니다. `BatApplication`으로 standalone bootJar를 만들고, PFW Batch 공통 API와 CPF 배치 운영 메타를 함께 사용합니다.
- `xyz`: 개발자 교육용 EDU 샘플입니다. 운영 코드와 분리된 학습용 Controller/Service/Repository/Mapper/Test 예제를 제공합니다.
- `acc`, `mbr`, `bizadm`, `exs`: 업무 예제 모듈입니다. 계정, 회원, 업무 관리자, 외부 연계 기준 구현을 제공합니다.

## 주요 문서

| 문서 | 용도 |
| --- | --- |
| `CPF_FINAL_TARGET_REQUIREMENTS.md` | CPF 최종 목표, 설계 원칙, 상태값, 완료/미완료 판단 기준 |
| `specs/index.html` | 문서를 읽는 순서와 모듈 책임 |
| `specs/로컬_개발환경_구성_가이드.html` | 로컬 개발 PC 구성, 빌드, smoke 실행 |
| `specs/아키텍처_가이드.html` | PFW/CMN/ADM/BAT/업무 모듈 경계 |
| `specs/표준_헤더_가이드.html` | 온라인 거래 헤더, 검증, 전파, 로그 저장 |
| `specs/개발_가이드.html` | 신규 업무 API와 EDU 샘플 연결 |
| `specs/관리자_가이드.html` | ADM 운영 기능과 권한/감사 기준 |
| `specs/배치_개발_가이드.html` | Spring Batch, BAT worker, heartbeat, ghost, center-cut 기준 |
| `specs/SQL_가이드.html` | 설치 SQL, Flyway, MariaDB 검증, DB 표준 |
| `specs/기능_구현_매트릭스.html` | 기능별 구현 상태와 검증 상태 |
| `specs/운영_매뉴얼.html` | 운영 절차와 장애 대응 |

## 기본 검증

```powershell
.\gradlew.bat :pfw:test --offline
.\gradlew.bat :adm:test --offline
.\gradlew.bat :bat:test --offline
.\gradlew.bat test --offline
.\gradlew.bat qualityGate --offline

powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-utf8.ps1 -CheckMojibake
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-html-docs.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-feature-evidence.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-sql-standard.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-composite-transaction-runtime.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-adm-transaction-group-runtime.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-bat-runtime.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-log-policy-runtime.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-mariadb-full-install.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-standard-header-e2e.ps1
```

실행하지 않은 검증은 완료로 기록하지 않습니다. 기능을 추가하거나 변경하면 코드, SQL, Swagger/OpenAPI, EDU 샘플, README, 관련 가이드, 기능 구현 매트릭스를 함께 현행화합니다.

중간 리포트는 `CPF_STABILIZATION_REPORT.md`에 Markdown으로 작성합니다. HTML/PDF/DOCX 배포본은 최종 정본화 단계에서 Markdown 원본 기준으로 생성합니다.

<!-- evidence: batch-development-guide operation-runbook scripts/smoke-log-policy-runtime.ps1 scripts/smoke-bat-runtime.ps1 scripts/smoke-composite-transaction-runtime.ps1 scripts/smoke-adm-transaction-group-runtime.ps1 BatApplication -->
