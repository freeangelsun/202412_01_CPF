# CPF R13 Static Validation Evidence

## 1. 기준

- Base master: `9b12ba025a0c6f2df59589681a862959232be16f`
- 작업일: 2026-07-26 (Asia/Seoul)
- 검증 종료 UTC: `2026-07-25T19:38:36Z`
- 환경: ChatGPT isolated Linux artifact workspace
- Java: OpenJDK 21 parser만 사용 가능
- 미제공: PowerShell, Gradle wrapper/full repository classpath, MariaDB, Browser
- 범위: R13 root overlay 자체 정적 정합성

## 2. 직접 실행한 결과

### V52 Migration

- canonical source/runtime byte compare: **일치**
- SHA-256: `ad418eb13630150dfd2d96d727d511ed603a55e7e259cf3088d1b80fe20e0af6`
- source/runtime `checksums.sha256` V52 entry: **일치**

### JSON/UTF-8/경로

- Overlay JSON 4개 Python stdlib parse: **성공**
  - Release Manifest schema
  - SBOM schema
  - Provenance schema
  - Contract compatibility schema
- Overlay text UTF-8 decode 검사: 오류 0
- Java package ↔ source path 정합성: 오류 0

### QA/R13 필수·금지 Pattern

직접 검사 결과:

- MBR `System.nanoTime()` 0
- ADM Log raw `response.put("details", details)` 0
- Response Code cache의 legacy `clearCache();` 경로 0
- Response Code explicit `ALL` + `CODE:` snapshot population 존재
- Cache refresh event 실제 `REQUIRES_NEW` store 존재
- Publisher bounded retry path 및 Listener 재처리 상태 존재
- MBR member-number issue history Owner query + ADM API 존재
- `cpf-admin`, `cpf-batch`, `cpf-common`, `cpf-member`의 R13 변경 Java에서 `com.cpf.core.common.*` 직접 import 0
- BAT logging diagnostics는 public `CpfLogPaths` facade 사용

### Java parser-level scan

- Overlay Java 파일: **50개**
- 명령 성격: `javac -proc:none -XDrawDiagnostics`
- Full repository dependency/classpath가 없어서 Spring/JUnit/CPF 기존 symbol resolution 오류는 발생함
- parser-level error pattern (`expected`, `illegal start`, `unclosed`, premature EOF 등): **0건**

따라서 이것은 **compile PASS가 아니라 parser-level syntax 확인**이다.

### Credential-like literal scan

- Java/Gradle/PowerShell/JSON/YAML의 password/secret/api-key/token 형태 literal 정적 검색
- 신규 실제 credential로 판단되는 hit: **0건**

## 3. 외부 버전 확인

R13에서 사용하는 OpenTelemetry stable BOM `1.64.0`이 Maven Central에 존재하는지 웹으로 확인했다. 이 확인은 dependency 존재 여부 확인일 뿐 CPF Build 성공 Evidence가 아니다.

## 4. 실행하지 못한 검증 — 상태 `미검증`

- PowerShell `check-r13-product-hardening.ps1`, Contract SelfTest, Generator Gate 실제 실행
- Gradle compile/test/assemble/qualityGate/validateReleaseMetadata
- MariaDB fresh install/V51→V52/R52 rollback/reapply
- ADM/BAT Runtime 503/LB
- MBR multi-instance concurrency
- Cache multi-instance/event DB-down/process-restart
- Browser/Excel/LibreOffice
- OTel Collector/exporter-down
- external Feature Flag Provider
- real Contract Registry/can-deploy
- detailed Fault Injection scenarios

위 항목은 성공으로 기록하지 않는다. 사용자 적용 환경에서 `verify-r13-overlay.ps1`과 `CPF_NEXT_WORK_REQUEST.md` 절차로 현재 Commit Evidence를 새로 생성해야 한다.
