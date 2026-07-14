# CPF CoreFlow Platform

CPF는 Java 21 배포 기준과 Spring Boot 3.4 기반의 업무 개발 표준 프레임워크입니다. Gradle 9.1 wrapper와 Java 21 release 컴파일을 사용하며 Java 25에서도 compile/test/bootJar/qualityGate를 검증합니다. 목표는 신규 업무 모듈을 만들 때 PFW 코어, 프로젝트 공통, 운영 콘솔, 배치, 외부 연계, EDU 샘플을 같은 규칙으로 제공하는 것입니다.

## 먼저 볼 문서

| 문서 | 용도 |
| --- | --- |
| `CPF_FINAL_TARGET_REQUIREMENTS.md` | 최종 목표와 범위. 작업 중 직접 수정하지 않는 기준 파일입니다. |
| `CPF_NEW_REQUEST.md` | 현재 작업 요청서. 작업 대상이 아니라 확인용 입력입니다. |
| `CPF_STABILIZATION_REPORT.md` | 최근 작업 결과, 검증 결과, 미검증/보류 항목 리포트입니다. |
| `CPF_GAP_MATRIX.md` | 남은 gap과 우선순위 매트릭스입니다. |
| `CPF_EVIDENCE_INDEX.md` | 검증 증적 파일 목록입니다. |
| specs/sample-coverage-matrix.md | 범용 EDU 소유자인 XYZ/BAT 샘플의 source/test/evidence/status 매트릭스입니다. PFW는 engine/port 계약 테스트, CMN은 helper 단위 테스트를 소유합니다. |
| `specs/index.html` | 상세 가이드 진입점입니다. |
| `specs/개발_가이드.html` | 업무 개발, validation, transaction, service-call, EDU 샘플 안내입니다. |
| `specs/관리자_가이드.html` | ADM 운영, 권한, 로그, 배치, 캐시, 보안 운영 안내입니다. |
| `specs/프레임워크_구성_가이드.html` | profile, datasource, 배포, Docker/local, CI 기준 안내입니다. |
| `specs/배치_개발_가이드.html#batch-development-guide` | Spring Batch 개발/운영 표준입니다. |
| `specs/운영_매뉴얼.html#operation-runbook` | 운영 절차와 장애 대응 기준입니다. |

## 모듈 책임

| 모듈 | 책임 |
| --- | --- |
| `pfw` | 프레임워크 코어입니다. 표준 헤더, 거래 ID, 거래/파일 로그, 응답/예외, OpenAPI, HTTP client, service-call, broker/file-transfer/security/runtime port, 배치 공통 API, 메시지/코드/설정 코어를 제공합니다. |
| `cmn` | 프로젝트 공통 영역입니다. CPF를 사용하는 프로젝트에서 업무 공통 helper와 규칙을 확장합니다. 범용 EDU와 기술 engine을 소유하지 않고 PFW public port를 사용합니다. |
| `adm` | 운영 콘솔입니다. 운영자 권한, 메뉴/버튼/API 권한, 회원 운영, 로그, 배치, 캐시, 메시지, 코드, 설정, 보안 운영 기능을 제공합니다. |
| `bat` | Spring Batch worker 모듈입니다. `BatApplication` standalone bootJar로 동작하고 PFW batch 공통 API와 배치 운영 메타를 사용합니다. |
| `mbr`, `acc`, `exs` | 회원 진입, 계정 reference domain, 외부 연계 reference domain을 소유합니다. 기준 호출은 `MBR → ACC → EXS`이며 Local Facade와 Remote Proxy 모두 PFW Service Call Engine 경계를 따릅니다. |
| `bizadm` | 업무 운영자용 reference 모듈입니다. ADM 프레임워크 운영 권한과 분리합니다. |
| `xyz` | 개발자 교육용 EDU 샘플 모듈입니다. Controller, Service, Repository, Mapper, Test 예제를 제공합니다. |

## 로그 저장 기준

PFW가 로그 경로와 저장 엔진을 소유합니다. local은 저장소 위치를 기준으로 절대 `<repository>/logs`를 계산하고, dev/stg/prod는 절대 `CPF_LOG_ROOT`와 고유 `CPF_INSTANCE_ID`가 없으면 기동하지 않습니다. 실행 로그는 `${CPF_LOG_ROOT}/{environment}/{runtimeModuleCode}/{instanceId}` 아래에 두며 업무 모듈별 `*/logs` 폴더와 `*_LOG_BASE_PATH` 설정은 사용하지 않습니다. 상세 파일 규칙, DB 로그 rollback 격리와 durable recovery 절차는 [개발 가이드](specs/개발_가이드.html), [관리자 가이드](specs/관리자_가이드.html), [운영 매뉴얼](specs/운영_매뉴얼.html)을 따릅니다.

## 선택형 로컬 인프라

`docker-compose.local.yml`은 MariaDB/Redis/Kafka가 필요한 로컬 검증에만 쓰는 선택형 harness이며 운영 전제가 아닙니다. MariaDB profile은 고정 비밀번호가 없으므로 `CPF_MARIADB_ROOT_PASSWORD`를 직접 설정해야 합니다. Docker가 없으면 설치된 MariaDB와 in-memory/deterministic adapter를 사용하고, 실행하지 않은 broker·DB 검증은 미검증으로 기록합니다.

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
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-architecture-ownership.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-spring-event-usage.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-profile-loading.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-runtime-config-standard.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-sample-coverage.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-evidence-path-existence.ps1
```

## 배포 검증

배포 검증은 `scripts/deploy/*.ps1` 직접 호출이 아니라 Gradle 태스크를 표준으로 사용합니다. `remoteDeployDryRun`은 실제 원격 서버 접속 없이 env, inventory, bootJar, PFW/CMN 포함 여부, checksum, java -jar 실행 계획, 외부 WAS/JNDI 설정 계획을 증적으로 남깁니다.

```powershell
.\gradlew.bat checkDeployEnv -PcpfModule=ACC -PcpfEnv=dev -PcpfResultDir=specs/evidence/20260708_04 --offline --no-daemon --console=plain
.\gradlew.bat checkDeployInventory -PcpfModule=ACC -PcpfEnv=dev -PcpfResultDir=specs/evidence/20260708_04 --offline --no-daemon --console=plain
.\gradlew.bat checkPackagedDependencies -PcpfModule=ACC -PcpfResultDir=specs/evidence/20260708_04 --offline --no-daemon --console=plain
.\gradlew.bat remoteDeployDryRun -PcpfModule=ACC -PcpfEnv=dev -PcpfDeployMode=dryRun -PcpfRequireApproval=false -PcpfResultDir=specs/evidence/20260708_04 --offline --no-daemon --console=plain
```

## 주요 smoke

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/runtime-start-services.ps1 -Modules ACC,MBR,EXS,ADM,BAT -BuildBeforeRun -NoExitOnFailure
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-runtime-closure.ps1 -Modules ACC,MBR,EXS,ADM,BAT
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/export-sanitized-evidence.ps1 -EvidenceDir specs/evidence/20260708_04

powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-mariadb-full-install.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-standard-header-e2e.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-service-registry-runtime.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-service-call-engine-runtime.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-service-registry-health-runtime.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-service-call-engine-circuit-runtime.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-service-call-engine-failover-runtime.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-adm-service-registry-runtime.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-adm-service-registry-ui-static.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-bat-runtime.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-log-policy-runtime.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-file-log-standard-runtime.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-trace-boost-runtime.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-composite-transaction-runtime.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-adm-transaction-group-runtime.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-create-domain.ps1
```

운영 로그와 검증 증적 정책은 <code>./gradlew.bat checkLogManagementStandard</code>로 확인합니다. 상세 경로·파일명·BAT JobInstance 규칙은 [개발 가이드](specs/개발_가이드.html)와 [관리자 가이드](specs/관리자_가이드.html)를 참고합니다.

실행하지 않은 검증은 성공으로 기록하지 않습니다. 기능을 추가하거나 변경하면 코드, SQL, Swagger/OpenAPI, EDU 샘플, README, 상세 가이드, 기능 구현 매트릭스, 증적 인덱스, 안정화 리포트를 함께 현행화해야 합니다.
