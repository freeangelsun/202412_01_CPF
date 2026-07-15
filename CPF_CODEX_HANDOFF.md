# CPF Codex 세션 인수인계

## 1. 현재 기준

| 항목 | 값 |
|---|---|
| 저장소 | `D:/WORK_CPF/202412_01_CPF` |
| 브랜치 | `master` |
| 시작 HEAD | `d16cd7a40062a1e77bd8cd3c6f6f7125cdc0708d` |
| 요청서 | `CPF_NEW_REQUEST.md` |
| 요청서 SHA-256 | `BCCF457FABD14C9BE6D37563CC3A1E14AAA65525C9CD2C595CE23CEE3719A84C` |
| 최종 목표 | `CPF_FINAL_TARGET_REQUIREMENTS.md` |
| 결과 보고서 | `CPF_STABILIZATION_REPORT.md` |
| 상태 정본 | `specs/기능_구현_매트릭스.json` |
| 증적 인덱스 | `CPF_EVIDENCE_INDEX.md` |
| 현재 증적 | `specs/evidence/20260714_02` |

Codex는 commit, push, branch 생성, rebase를 하지 않았다. `CPF_NEW_REQUEST.md`는 사용자 입력 정본이므로 수정하지 않는다.

## 2. 절대 아키텍처 원칙

- PFW: 프레임워크 기술 core, engine, port, adapter, transaction, logging, reliability, broker, file transfer.
- CMN: 프레임워크를 사용하는 프로젝트의 공통 업무 규칙과 helper.
- ADM: 프레임워크 운영 콘솔.
- BIZADM: 업무 운영 콘솔.
- XYZ: 온라인·일반 프레임워크 EDU.
- BAT: batch·scheduler·center-cut EDU와 실행 모듈.
- ACC/MBR/EXS: 내부 서비스, 진입/복합거래, 외부연계 reference domain.
- 타 주제영역 Repository/Mapper 직접 접근을 금지한다.
- 주제영역 간 호출은 공개 port/facade와 Local Facade/Remote Proxy를 사용한다.
- 핵심 업무 흐름을 임의 Spring Event로 연결하지 않는다.

대표 흐름은 `MBR -> ACC -> EXS`다. 전 구간에서 `transactionGlobalId`, `transactionId`, `traceId`, `segmentId`, parent 식별자를 유지한다.

## 3. Java와 빌드

| 항목 | 기준 |
|---|---|
| Java | 25 단일 기준 |
| Gradle | wrapper 9.1.0 |
| compile release | 25 |
| class major | 69 |
| 실행 JAR | ACC, MBR, EXS, ADM, BAT, BIZADM, XYZ |

검증 명령:

```powershell
.\gradlew.bat clean test --no-daemon
.\gradlew.bat :acc:bootJar :mbr:bootJar :exs:bootJar :adm:bootJar :bat:bootJar :bizadm:bootJar :xyz:bootJar --no-daemon
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-java25-standard.ps1
.\gradlew.bat qualityGate --no-daemon
```

최근 전체 quality gate는 저장소 JUnit 기준 130 suites, 290 tests, 실패 0, 오류 0, 건너뜀 4다. 주제영역 생성기 smoke의 임시 모듈 test 1건은 별도 증적으로 관리하며, 건너뜀은 DB 또는 환경형 테스트이므로 완료로 승격하지 않는다.

## 4. 이번 구현 핵심

- 모든 모듈 Java 25 및 생성기 Java 25 전환.
- PFW broker bridge port/adapter와 PFW file exchange gateway 추가.
- CMN 메시지·파일교환은 PFW를 호출하는 호환 facade로 전환.
- PFW center-cut 기본 auto-configuration 추가.
- 고정 ADM 관리자 seed 제거와 안전한 bootstrap 추가.
- 최초 로그인 강제 비밀번호 변경, 현재 비밀번호 검증, 확인값, 정책·이력 재사용 차단, 세션 폐기 구현.
- ADM UI 강제 변경 화면과 제한 세션 적용.
- 7개 실행 모듈 runtime map, health/OpenAPI 검증, 표준 wasId 적용.
- 표준 헤더 현재 일자 거래 ID와 절대 경로 mock capture 적용.
- 주제영역 생성기 test·bootJar·major 69 검증.
- 공식 가이드 DOCX 9개, specs HTML 0개.
- report/matrix/evidence/gap을 `scripts/sync-verification-ledger.ps1`로 동기화.

## 5. 현재 검증 상태

### 완료

- Java 25 class major 69.
- 7개 bootJar.
- 7개 앱 프로세스·포트·HTTP probe.
- 7개 OpenAPI JSON.
- 표준 헤더 수신·차단·하위 호출 전파.
- PFW 파일 로그 runtime.
- architecture ownership와 Spring Event gate.
- 샘플 capability 47/47 매핑.
- 주제영역 생성기 test·bootJar.

### 부분 구현 또는 미검증

- MariaDB full install/Flyway/FK/index/seed/권한: 필수 비밀번호 환경변수 없음.
- 복합거래 runtime: ACC 호출은 수행했으나 DB 인증 실패로 HTTP 500.
- ADM 로그인·권한·브라우저: bootstrap/DB 선행조건 없음.
- Redis/Kafka/RabbitMQ: 실 broker 없음.
- SFTP/FTP/FTPS/SCP/SSH: 실 protocol test server 없음.
- BAT JobRepository·공유 스토리지 다중 프로세스: DB/공유 환경 없음.
- 전체 테스트 skip 4건.

상세 상태는 `CPF_GAP_MATRIX.md`와 `CPF_EVIDENCE_INDEX.md`를 우선 확인한다.

## 6. 환경변수

값은 문서·로그·명령행에 남기지 않는다.

| 변수 | 용도 |
|---|---|
| `JAVA_HOME` | Java 25 |
| `CPF_DB_HOST`, `CPF_DB_PORT` | MariaDB endpoint |
| `CPF_DB_ROOT_USERNAME`, `CPF_DB_ROOT_PASSWORD` | 설치 검증 |
| `CPF_DB_MIGRATION_PASSWORD` | migration 계정 생성/검증 |
| `CPF_DB_APP_PASSWORD` | app 계정 생성/검증 |
| `CPF_ADM_BOOTSTRAP_ENABLED` | ADM bootstrap 명시 활성화 |
| `CPF_ADM_BOOTSTRAP_OPERATOR_ID` | 초기 운영자 ID |
| `CPF_ADM_BOOTSTRAP_PASSWORD` | 초기 운영자 비밀번호 |
| `CPF_ADM_BOOTSTRAP_PROD_APPROVED` | 운영 bootstrap 별도 승인 |
| `CPF_LOG_ROOT` | 절대 로그 root |
| `CPF_ENV` | local/dev/stg/prod |
| `CPF_INSTANCE_ID` | 인스턴스 고유 ID |

## 7. 런타임 검증

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/runtime-start-services.ps1 -ResultDir build/runtime-smoke
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/runtime-status.ps1 -ResultDir build/runtime-smoke
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-openapi.ps1 -ResultDir build/runtime-smoke -RequireRuntime
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-standard-header-e2e.ps1 -ResultDir build/runtime-smoke -SkipLogLookup
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/runtime-stop-services.ps1 -ResultDir build/runtime-smoke
```

DB 변수가 준비되면 `-SkipLogLookup`을 제거하고 표준 헤더 DB log/detail까지 검증한다.

## 8. MariaDB 후속 검증

MariaDB를 설치·삭제·업그레이드하지 않는다. 승인된 검증 DB와 세 비밀번호가 환경변수로 제공된 경우에만 실행한다.

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-mariadb-full-install.ps1 -RequireRun
```

성공 후 CMN 채번 동시성, XYZ center-cut DB adapter, XYZ mapper slice를 다시 실행한다. Mapper fixture는 `xyz/src/test/resources/sql/xyz_edu_query_fixture.sql`이며 username 표준 변수는 `CPF_XYZ_EDU_MAPPER_DB_USERNAME`이다.

## 9. ADM bootstrap 순서

1. 운영자 ID와 초기 비밀번호를 보안 환경변수로 주입한다.
2. bootstrap을 명시적으로 활성화한다.
3. 운영 profile이면 별도 승인 변수도 주입한다.
4. 첫 로그인 후 강제 비밀번호 변경 화면만 사용할 수 있는지 확인한다.
5. 변경 후 기존 token과 모든 운영자 세션이 폐기되는지 확인한다.
6. 새 비밀번호로 재로그인하고 역할별 API 200/403과 감사 로그를 확인한다.
7. bootstrap을 다시 비활성화한다.

## 10. 공식 문서

README는 진입점이고 상세 정본은 `specs/CPF_*.docx` 9개다. 상태와 검증은 다음 기계 문서에서 확인한다.

- `specs/기능_구현_매트릭스.json`
- `specs/기능_구현_매트릭스.md`
- `CPF_STABILIZATION_REPORT.md`
- `CPF_GAP_MATRIX.md`
- `CPF_EVIDENCE_INDEX.md`
- `specs/evidence/20260714_02/final-worktree-manifest.sanitized.log`

문서나 상태를 바꾼 뒤에는 다음을 실행한다.

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/sync-verification-ledger.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-docx-standard.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-report-matrix-evidence-consistency.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/export-final-worktree-manifest.ps1
```

## 11. 다음 세션 첫 확인

```powershell
git status --short
git branch --show-current
git rev-parse HEAD
Get-FileHash .\CPF_NEW_REQUEST.md -Algorithm SHA256
git hash-object .\CPF_NEW_REQUEST.md
java -version
.\gradlew.bat --version
```

그다음 `CPF_STABILIZATION_REPORT.md`의 미검증 항목만 현재 환경에서 재현한다. 과거 증적만으로 상태를 올리지 않는다.
