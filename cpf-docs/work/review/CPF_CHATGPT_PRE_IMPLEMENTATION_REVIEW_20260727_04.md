# CPF ChatGPT 작업 전 통합 리뷰 — 2026-07-27 04

## 1. 기준

- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- 작업 시작 SHA: `702bf83580b9c4db2dbba6482ece233e00842f1b` (`20260727_03`)
- 최상위 정본: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
- QA 입력: `CPF 차기 통합 QA 요구사항` (`9e4edaef...` 기준 작성)
- QA 상세 보존: `cpf-docs/work/review/CPF_QA_INPUT_20260727_04.md`
- 현재 역할: ChatGPT 1차 개발. Codex는 몇 차례 추가 개발 후 누적 Diff를 최신 master에서 독립 검수한다.

## 2. 이전 Codex 작업 평가

`fb95e15f... (20260727_01)`의 대규모 구조 변경은 방향 자체가 잘못된 작업으로 보지 않는다.

잘된 점:

- BAT Runtime을 Control/Scheduler/Worker/Center-Cut/Agent/Contract/Runtime Common으로 분리
- Scheduler/Worker Lease·Fencing·Takeover 사고방식 도입
- ADM의 MBR 직접 Ownership 제거
- REF의 MBR 전용 EDU 의존 제거
- BAT Query Contract/Runtime SQL Template 중앙화
- Build Tooling을 `cpf-tools/build` Owner로 이동

보호할 것:

- BAT 158 Query Pack/V58 기존 Evidence
- ADM Generated Domain 중립화 방향
- REF self-contained 방향
- BAT standalone 역할 분리
- Lease/Fencing/Unknown Result/Reconciliation 구조

미완료/위험:

- MBR/ACC Generator Golden parity
- BAT 삭제 EDU 기능 parity
- Gateway Fault/Multi-instance
- 대규모 변경 후 최신 전체 회귀검증

결론: **Architecture는 대체로 합당하나 대규모 구조 전환이 최종 parity/통합검증 전에 중단된 상태**다.

## 3. 20260727_02 ChatGPT 1차 패치 평가

방향이 맞는 부분:

- ADM 연락처를 Identity가 아니라 Profile이 소유
- BZA 휴대폰/내부전화 분리
- BZA 신규 직원 `EMPLOYED` 기본값
- Local/Remote Artifact Federation 뼈대
- BOM/Convention Plugin 제품화 방향
- V59/V60 Canonical/Migration/Rollback 동시 변경

QA와 재검토로 확인된 보완점:

- Root `build` 성공만으로 Artifact 공개하는 기준은 부족
- Mutable Version의 여러 Artifact를 순차 공개하면 partial/mixed set 위험
- Remote와 Local Repository 경계를 더 명확히 분리해야 함
- Artifact 존재만이 아니라 POM/BOM/Plugin Marker/Hash/Source SHA 검증 필요
- Local 자동 Sync는 기본 opt-in이 적절
- Server/CI가 Local Repository로 fallback하면 안 됨

## 4. QA와 기존 자체 계획 병합 결과

QA는 기존 계획을 대체하지 않고 다음 위험을 더 구체화한다.

### 신규 P0로 승격

1. 공식 지원 가능한 Java/Gradle/Spring Boot Stack 결정
2. Artifact Publication의 Quality Gate/검증/Staging/Promotion
3. ADM 생성 Transaction + Product DB fail-closed
4. PII Masking/Audit Redaction/NULL 규칙
5. V59/V60 Lifecycle
6. BZA inline SQL/Core Internal Boundary

### 기존 P0 유지

1. Generated Domain Golden normalization
2. BAT Runtime/EDU parity
3. Gateway target-down/timeout/retry/UNKNOWN_RESULT
4. ADM/BZA 상용 운영 UX
5. DB Vendor/Lifecycle
6. Tool/Gate 정리와 Manual 제품화

## 5. 이번 Change Set 범위 결정

이번 작업은 `CHANGE-SET-A — Stack / Artifact / Baseline Safety`만 구현한다.

포함:

- Stack 단일 정본과 `TRANSITION` Release 차단
- Root/Module/Generator/Standalone Version source 통합
- `LOCAL_DEV / REMOTE / OFFLINE` 공급원 배타화
- Local 자동 Sync 기본 off
- `aggregateQualityBuild`
- isolated staging publication
- POM/module metadata/BOM/plugin marker/hash 검증
- manifest barrier + rollback local promotion
- Remote `cpfInternal` 전용 publish
- Offline Bundle
- 관련 Tool/CI/CD/Batch lifecycle Guide
- 최신 SHA 문서 재기준화

제외/다음 Change Set:

- Spring Boot 4 실제 Migration
- ADM DB Transaction/PII
- V59/V60 Runtime DB
- Generator Golden migration
- BAT EDU parity
- Gateway fault
- Browser/Multi-instance

## 6. Side Effect와 재검증 재개방

이번 변경은 Build/Plugin/BOM/Generator/Standalone Packaging에 직접 영향을 주므로 다음 과거 PASS를 재검증 상태로 다시 연다.

- Java 25 전체 configuration/compile/test
- Included BOM/Plugin build
- Generated Standalone Domain build
- bootJar/bootWar
- Artifact packaging
- Generator create/export

BAT 158 Query SQL 자체와 V58 SQL 내용은 직접 변경하지 않으므로 즉시 전수 반복하지 않는다.
다만 최종 clean aggregate build에서 회귀 여부는 확인한다.

## 7. 완료 금지

이번 Source 패치만으로 다음을 완료 처리하지 않는다.

- Java 25 Gradle 9.1 Runtime Build
- Local Artifact 실제 Promotion
- Windows 동시 Publisher/Consumer
- Remote Nexus/Artifactory
- Offline Standalone Build
- bootJar/bootWar exact hash
- GitHub Required CI

실행되지 않은 검증은 `미검증`이다.
