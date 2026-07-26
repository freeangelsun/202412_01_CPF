# CPF Final Completion Handover

## 기준

- Repository: `https://github.com/freeangelsun/202412_01_CPF`
- Branch: `master`
- 기준 SHA: `9253097086322c0eacc00c005e944b132e31ae06`
- Commit: `20260726_04`
- QA 원본 기준: `56b165513f73f0548d41d2d52197abcdf69a0d14`

## 이번 패키지 핵심

- BAT를 Library 3개 + Standalone Runtime 5개로 분리했다.
- Scheduler HA/Fencing, Worker multi-concurrency Lease/Fencing, Center-Cut 독립 실행/복구를 구현했다.
- Host Agent 원격 설치/기동/중지/재시작/Drain/Resume/Rollback/Log 수집을 승인 Catalog 경계로 제공한다.
- Runtime별 Shell/PowerShell/Properties/systemd/Logging 정책을 제공한다.
- ADM은 BAT DB를 직접 제어하지 않고 BAT Control Server Owner API와 기존 Approval Engine을 사용한다.
- Domain은 고정 신규 Module 생성이 아니라 독립 Repository Federation/BOM/Published Artifact 구조를 사용한다.
- MariaDB V55/V56/R55/R56과 Canonical Source/Fresh Install을 동기화한다.
- Commercial Release/Source Governance/Boundary Gate를 보강한다.

## 적용

압축을 Repository 밖에 푼 뒤:

```powershell
pwsh <package>\cpf-tools\scripts\apply-cpf-final-completion-package.ps1 `
  -PackageRoot <package> -RepositoryRoot <CPF repo>
```

Overlay 후 Legacy `cpf-batch/src`가 제거된다. 사용자 승인 전 Commit/Push/Branch 생성 금지.

## 이 환경에서 실제 확인한 결과

- 최신 master SHA: PASS (`9253097086322c0eacc00c005e944b132e31ae06`)
- Java public type/file-name: PASS
- BAT Contract + Release signer compile: PASS (`javac 21`, Java 25 전체 build 대체 아님)
- Overlay Java parser syntax: PASS, syntax indicator 0
- JSON/YAML/XML parse: PASS
- ADM Batch Control TS/Vue script syntax: PASS
- Bash syntax: PASS
- PowerShell gross structure: PASS
- Gradle/Settings gross structure: PASS
- 외부 `com.cpf.core.common.*` 직접 의존: 0
- 고정 `cpf-external`: 없음
- standalone `cpf-tools/db/source`: 없음
- Secret/private-key/token literal 정적 탐지: 0
- Flyway V55/V56 checksum: PASS
- V55 신규 Table → MariaDB canonical source 존재: PASS

원본: `cpf-docs/evidence/CPF_FINAL_STATIC_VALIDATION_20260726.txt`

## 적용 후 필수 검증

```powershell
pwsh .\cpf-tools\scripts\verify-cpf-final-completion.ps1
```

추가로 MariaDB fresh/upgrade/rollback/reapply, 실제 Remote Agent, Browser E2E,
다중 인스턴스 kill/takeover, Domain standalone build, Commercial Release Gate를 실제 환경에서 실행하고 Evidence를 보존한다.

## 보호할 Architecture

- EXS 고정 Module 복원 금지
- standalone `cpf-tools/db/source` 복원 금지
- Generated Domain 내부 core implementation 의존 금지
- ADM -> batDB 직접 위험 Update 금지
- UNKNOWN_RESULT를 실패/성공으로 임의 확정 금지
- Scheduler/Worker/Center-Cut/Deployment fencing 회귀 금지
- Host Agent 자유 Shell/임의 Path 실행 금지
- Secret 원문 Log/Evidence 금지
