# CPF QA39 최종 시정 사후 리뷰

## 기준

- Base SHA: `4aea798c913787e86341809e2cef2b9495cbf7ba`
- QA 범위: QA39-045~QA39-064
- 보호 경로 변경: 0건
- Git 쓰기 작업: 수행하지 않음

## 최초 Requirement와 구현 대조

- Starter: 37개 중복·평면 구조에서 최종 38개, Root 9축, Profile 6, Internal 32로 정본화.
- Catalog/BOM: Canonical/Release 단일 생성원과 Public/Internal BOM 분리.
- API/SPI: Notification/Broker/Resilience/Feature Flag를 Core API/SPI로 이동, Starter 구현은 internal.
- Runtime: Resilience와 Feature Flag 상용 Source·JDBC·승인·감사·ADM Consumer 구현.
- Batch: Kafka Leaf 의존과 Source import 제거, Provider-neutral control port 및 composite identity 적용.
- DB: Canonical 200 Table, 공식 3 Vendor V97 lifecycle parity.
- Frontend: Backend Controller → Canonical OpenAPI 319 → Generated Client/Contracts → 실제 Vue Route Consumer.

## 구현 → Owner/Consumer 추적

- Resilience Owner `integration/resilience`; Consumer Gateway/HTTP/TCP.
- Feature Flag Owner `platform-operations/feature-flag-openfeature`; Consumer ADM/API and runtime callers.
- Notification Owner `notification/dispatch`; contract Owner `cpf-core`.
- Broker Provider Adapter Owner `messaging/kafka`; Batch는 Core control port만 참조.

## 회귀 검수에서 제거한 결함

1. Apply가 Overlay 정본 Gate 자체를 재치환하는 self-dirty.
2. self-nested capability가 sibling Module을 삼키는 재적용 결함.
3. Dynamic BOM을 literal Artifact로 검사한 False Negative.
4. Generated Route Contract 쉼표 누락.
5. OpenFeature Evaluation Context 미전달.
6. JDBC Insert와 DDL Column 불일치.
7. Resilience exception serial warning의 `-Werror` 실패.
8. Python 미설치 Windows 환경에서 적용기와 Gate가 9009로 실패하는 배포 결함. R3에서 JDK source launcher 기반 단일 도구로 제거.

## R3 배포 보정

- 적용·저비용 Gate: JDK source launcher만 사용.
- Java25 Build Contract, Browser Contract, DB Token Parity, Runtime Source Closure, Batch Control Plane, Supply-chain License 진입점의 Python 호출: 0건.
- 회사 노트북 사전점검: `cpf-tools/verification/qa39/test-qa39-company-laptop-environment.ps1`.
- R2 추출 실패 상태 위 R3 적용: 64 operations, 재적용: 0 operations.

## 실행 검증

PASS: 합성 적용/재적용, 38개 parity, 명칭, Public/Internal BOM, DB 정적 parity, OpenAPI/Generated/Route, Source Boundary, Java 21 합성 `-Werror` (Product 60, Spring/JDBC Adapter 78, Controller 39, Unit Test Source 14, Batch/Kafka 25 Class), TypeScript strict, JSON/CSV/Java-only Gate, diff check, Secret/Hygiene.

## 실행하지 못한 검증

Java25/Gradle, npm clean lifecycle, 3 Browser, 3 DB Live, 외부 Runtime fault, Generator Boot Matrix, Supply-chain, GitHub Required Checks. 상세 원인은 Environment Evidence에 기록했다. 실행하지 않은 검증을 성공으로 쓰지 않았다.

## 현재 판정

- Source 구현: 완료
- 정적·합성 검증: 완료
- 전체 Runtime/Release 검증: 미검증
- QA39 전체: 미검증

전체 완료는 `cpf-tools/verification/qa39/invoke-qa39-final-validation.ps1`이 깨끗한 exact-SHA 환경에서 exit 0을 반환하고 Runtime Evidence가 현재 SHA와 일치할 때만 가능하다.

## R4 배포 스크립트 사후 보정

R3 `test-qa39-company-laptop-environment.ps1`의 `$nodeVersion` 조건식에 닫는 괄호 위치 오류가 있었다. 제품 Source 이동 전 Parser 단계에서 중단된 결함이며 R4에서 스크립트를 명시적 문법으로 전면 재작성했다. 적용 래퍼와 사전점검을 분리하고, 배포 명령 자체에 ZIP 전체 PowerShell Parser 검사를 추가해 같은 종류의 오류가 Repository에 복사되기 전에 차단되도록 했다.
