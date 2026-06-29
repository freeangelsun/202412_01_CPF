# CPF CoreFlow Platform Framework

CPF는 Java 21, Spring Boot 3.4 기반의 업무 개발 표준 프레임워크입니다. 신규 업무 모듈이 거래 추적, 표준 헤더, 표준 응답, 예외 처리, 거래 로그, 권한, 감사, 배치, 외부 연계, 운영 관제를 같은 방식으로 구현하도록 공통 코드와 운영 기준을 제공합니다.

이 파일은 짧은 진입점입니다. 상세 기준은 `specs` 아래 HTML 가이드에서 관리합니다.

## 핵심 경계

- `pfw`: 프레임워크 코어 영역입니다. 거래 ID, 표준 헤더, 표준 응답, 예외, 거래 로그, 로그 정책, OpenAPI 공통 설정, WebClient/RestClient 헤더 전파, Batch 공통 API, 메시지/코드/설정 같은 프레임워크 자체 기능을 제공합니다.
- `cmn`: 프로젝트 공통 영역입니다. CPF를 적용하는 프로젝트에서 프로젝트별 공통 채번, 업무 알림, 업무 로그, 파일/전문/MQ 보조 기능을 확장하는 영역입니다. 프레임워크 보편 기능을 담는 곳이 아닙니다.
- `adm`: 운영 콘솔입니다. 권한, 회원, 로그, 배치, 캐시, 메시지, 코드, 설정, 보안 운영을 조회하고 조치합니다.
- `bat`: 실제 Spring Batch Job을 실행하는 worker입니다. ADM은 관제와 명령을 담당하고 BAT가 Job/Step을 실행합니다.
- `xyz`: 개발자 교육용 EDU 샘플입니다. 운영 코드와 분리된 학습용 예제로 유지합니다.

## 주요 문서

| 문서 | 목적 |
| --- | --- |
| `specs/index.html` | 문서 읽는 순서와 모듈 책임 |
| `specs/아키텍처_가이드.html` | PFW/CMN/ADM/BAT/업무 모듈 경계 |
| `specs/표준_헤더_가이드.html` | 온라인 거래 헤더, 자동 검증, 전파, 로그/ADM 표시 기준 |
| `specs/DB_표준_가이드.html` | DB 소유권, 테이블/컬럼/인덱스/FK/COMMENT 표준 |
| `specs/프레임워크_구성_가이드.html` | profile, 모듈 구성, 품질 gate, 운영 구조 |
| `specs/개발_가이드.html` | 신규 업무 API, EDU 샘플 연결, 구현 절차 |
| `specs/관리자_가이드.html` | ADM 운영 기능과 권한/감사 기준 |
| `specs/배치_개발_가이드.html` | Spring Batch와 CPF 배치 운영 메타 연동 |
| `specs/SQL_가이드.html` | 설치 SQL, Flyway, MariaDB 검증 기준 |
| `specs/기능_구현_매트릭스.html` | 기능별 구현 상태, 증거, 남은 리스크 |

배치 runtime smoke는 `scripts/smoke-bat-runtime.ps1`로 확인합니다.

## 기본 검증

```powershell
.\gradlew.bat :pfw:test --offline
.\gradlew.bat :adm:test --offline
.\gradlew.bat test --offline
.\gradlew.bat qualityGate --offline

powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-utf8.ps1 -CheckMojibake
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-html-docs.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-feature-evidence.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-sql-standard.ps1
```

실행하지 않은 검증은 완료로 보지 않습니다. 기능을 추가하거나 바꾸면 소스, SQL, Swagger, EDU 샘플, README, HTML 가이드를 함께 현행화합니다.

<!-- evidence: batch-development-guide operation-runbook scripts/smoke-log-policy-runtime.ps1 BatApplication -->
