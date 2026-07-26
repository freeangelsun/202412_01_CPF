# CPF QA Closure — 2026-07-26

## 기준

- QA 원본 기준 SHA: `56b165513f73f0548d41d2d52197abcdf69a0d14`
- 본 작업 최신 기준 SHA: `9253097086322c0eacc00c005e944b132e31ae06` (`20260726_04`)
- QA 등록: 결함/부분 구현/미검증 후보 289건 + 별도 제품 Gap 12건

QA 301건은 파일별 301개 패치로 처리하지 않고 최신 master에서 이미 해결된 항목을 먼저 제외한 뒤 Root Cause 단위로 구조 개선했다. 특히 `9253097`에서 이미 반영된 ADM/BZA Frontend, Secret, Paging, Retention, Tenant 및 관련 R14 수정은 중복 개발하지 않았다.

## 이번 Overlay에서 닫은 주요 Root Cause

### BAT Runtime / Multi-instance

- 기존 단일 `cpf-batch/src` 실행 구조를 Library 3개 + Standalone Runtime 5개로 분리한다.
- Control Server / Scheduler / Worker / Center-Cut Runner / Host Agent가 독립 bootJar와 독립 설정·기동 스크립트·로그 디렉터리를 가진다.
- Scheduler leader lease와 monotonic fencing token을 적용한다.
- Worker claim/lease/fencing과 crash recovery를 구현한다. 실행 시작 전 lease 만료만 안전 재할당하고, 실행 시작 후 결과 불명은 `UNKNOWN_RESULT`로 격리한다.
- Worker `maxConcurrency`를 실제 동시 Lease/virtual-thread 실행 수에 반영한다.
- Center-Cut execution/parameter snapshot/target cursor/chunk/claim/fencing/TPS/concurrency/pause/drain/cancel/reconcile 모델을 분리한다.
- Center-Cut TPS/concurrency admission은 execution row lock으로 직렬화해 다중 Runner race를 방지한다.

### Runtime Command / Deployment / Remote Agent

- 위험 Runtime 명령은 approvalRequest/policy/requester-approver 분리/만료/멱등 command state를 요구한다.
- 동일 idempotency key의 중복 실행은 `REQUESTED|APPROVED|PLANNED -> EXECUTING` CAS로 1개 실행만 허용한다.
- Deployment Cell/Plan/Execution/Instance Result/Compatibility/Lock/Rollback을 BAT Owner가 소유한다.
- Host Agent는 자유 Shell/자유 Path를 제공하지 않고 승인 Service Catalog와 고정 명령만 수행한다.
- Artifact SHA-256 + Ed25519 signature, path sandbox, mTLS production guard, log archive size 제한을 적용한다.
- Linux systemd와 Windows PID 기반 START/STOP/RESTART/STATUS를 별도 제공한다.

### Domain Repository Federation

- 신규 고정 업무 Domain은 생성하지 않는다.
- 기존 Domain을 독립 Git 저장소 형태로 export하거나 신규 Domain Repository를 생성할 수 있다.
- Domain은 `cpf-core/common/batch` project dependency가 아니라 게시 Artifact/BOM/Convention Plugin을 소비한다.
- Generated Domain의 `com.cpf.core.common.*` 직접 의존을 Gate에서 차단한다.
- EXS 고정 Module과 standalone `cpf-tools/db/source` 복원을 차단한다.

### DB / Release / Operations

- MariaDB 정본 Source/Fresh Install/Flyway V55/V56/Rollback/Verify를 동기화한다.
- V55/V56 checksum을 실제 파일 SHA-256과 일치시킨다.
- BAT Control Plane 권한 Seed를 조회/운영/배포/복구/감사 기능별로 분리한다.
- Commercial Release Gate는 clean master, signature, license/CVE report를 fail-closed로 요구한다.

## 정적 검증 결과

본 작업 환경에서 직접 실행한 결과만 기록한다.

- 최신 master SHA 재확인: PASS (`9253097086322c0eacc00c005e944b132e31ae06`)
- BAT Contract Java compile (`javac --release 21`): PASS
  - 목적: 계약 소스 자체 문법/타입 자립성 검증. Java 25 전체 Gradle Build의 대체 증적은 아니다.
- 전체 Overlay Java parser-only syntax scan: PASS, syntax indicator 0
- Public Java type/file-name 정합성: PASS
- 신규 `com.cpf.core.common.*` 직접 의존 검색: PASS
- JSON/YAML/XML parse: PASS
- ADM 신규 TypeScript / Vue `<script setup lang="ts">` syntax scan: PASS
- Bash `bash -n`: PASS
- PowerShell 24개 gross delimiter/here-string structure scan: PASS
- Gradle/Settings gross brace structure scan: PASS
- Secret/private-key/token literal 정적 탐지: 0건
- Flyway V55 checksum: PASS
- Flyway V56 checksum: PASS
- V55 신규 Table이 canonical `35_bat_schema.sql`에 모두 존재: PASS

## 실행 환경에서 반드시 확정할 Evidence

다음은 소스 구현 미완료가 아니라 실제 환경 실행이 필요한 검증이다. 실행 전에는 PASS로 기록하지 않는다.

- Java 25 + Gradle 9.1 전체 `clean test`와 5개 bootJar/3개 Library
- MariaDB fresh/upgrade/rollback/reapply/drift
- Control 2 / Scheduler 2 / Worker N / Center-Cut N / Agent multi-process
- Leader kill/takeover/fencing, Worker crash/UNKNOWN, Center-Cut reconcile
- 실제 원격 Agent mTLS/install/start/upgrade/rollback/log collection
- ADM/BZA Browser E2E와 Approval/RBAC/UNKNOWN UX
- 독립 Domain Repository create/export/clean build/remove/regenerate
- Commercial SBOM/License/CVE/Signature/Provenance/Can-Deploy

실행하지 않은 항목을 성공으로 표시하지 않는다.
