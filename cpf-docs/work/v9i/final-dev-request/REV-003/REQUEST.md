# CPF 마지막 개발GPT 전수 보완 개발 요청서 — REV-003

## 1. 핵심 변경

이번 Revision은 **환경이 없는 개발GPT에게 Java 25, 실제 DB, Browser, Broker 실행을 반복 요구하지 않는다.**

총 Requirement는 25건으로 확장한다(FDEV-025 P0 Defect append).

- 개발GPT 직접 구현·검증: 21건
- 개발GPT 대체검증 후 Codex/QA Target Runtime 이관: 4건
- 현재 작업 기준 Commit: `2929163b3bb40159e22e1f57e79b6cd070abf7ad`
- 과거 SHA는 이력으로만 보존하고 현재 실행 기준과 구분


## 1-1. 통합 검증 정본 우선 원칙

`cpf-docs/work/v9i`의 통합 원장·Request·Provenance·Evidence가 이번 Campaign의 중심 정본이다.

`final-dev-request/REV-003`은 독립 원장이나 대체 정본이 아니라, 위 통합 정본을 최신 구현과 실행 결과로 갱신하기 위한 작업 지시서다.

모든 Requirement는 변경 전과 변경 후에 통합 무결성을 검증한다.

- 변경 전: 최신 SHA, 47,745 exact ID, 32 request_id, Index/Part, Provenance, Evidence, Consumer 확인
- 변경 후: Source/API/SQL/Test/Config/Frontend/Script Delta를 append/upsert하고 행 수·Hash·orphan·duplicate를 재검증
- 환경 의존 Runtime을 Codex로 이관하더라도 구현·대체검증·미검증 상태·실행 명령·Evidence 경로를 통합 정본에 반영
- 기존 완료 ID와 Cross-Session Request를 삭제·재번호화·덮어쓰기 금지

상세 절차는 `CANONICAL_INTEGRATION_CONTROL.md`를 따른다.

## 2. 역할 분리

### 개발GPT 완료 판정

개발GPT는 25건 모두 검토하되, 환경 의존 4건은 다음을 완료하면 자기 역할을 `완료`로 판정할 수 있다.

1. 제품 구현과 Config·Build Script·Migration·Test·Runtime Script 보완
2. 현재 환경에서 가능한 정적검증·로컬 Harness·대체 실행
3. 실제 Target Runtime용 Preflight와 정확한 실행 명령
4. 성공 기대 결과와 실패 판정 기준
5. 필요한 환경·권한·Secret·담당 역할
6. Evidence Template과 결과 저장 경로
7. 대체검증의 한계 명시
8. 개발GPT 독립 자체검수

이 경우 다음처럼 기록한다.

- `개발GPT_상태=완료`
- `개발GPT_자체검수상태=완료`
- `verification_status=미검증`
- `development_status=재확인 필요`
- QA 통과 전 전체 완료 주장 금지

### Codex 역할

Codex는 환경 의존 4건의 실제 Target Runtime 검증을 우선 수행한다.

- FDEV-004: Java 25·Gradle 9.1·Publication
- FDEV-005: Oracle·PostgreSQL·MariaDB 실제 Lifecycle
- FDEV-006: Broker·Multi-process·Split-WAS·Process Kill
- FDEV-017: 실제 Browser·Playwright Matrix

Codex 환경에도 해당 Runtime이 없으면 같은 요청을 개발GPT에게 되돌리지 않는다. QA·인프라·DBA·Frontend 실행 환경으로 이관하고 정확한 실행 Package를 유지한다.

## 3. No-Ping-Pong 정책

1. 환경 Capability 확인은 1회 수행한다.
2. 실제 실행 시도도 동일 환경·동일 원인에 대해 1회만 수행한다.
3. 동일 오류에 변경 없이 재시도하지 않는다.
4. 환경 미제공이 확인되면 즉시 대체검증 단계로 전환한다.
5. 대체검증 완료 후 개발GPT 역할을 닫고 Codex/QA로 넘긴다.
6. Codex가 환경 부족으로 실패해도 개발GPT 구현 결함이 아닌 한 개발GPT 재개발 요청으로 돌리지 않는다.
7. 실제 Runtime 실패가 Source·Config·Script 결함을 드러낼 때만 같은 Requirement ID로 개발GPT 재개발 요청한다.
8. 외부 환경 대기 때문에 다른 21건을 보류하지 않는다.

## 4. 대체검증 최소 기준

### Java 25·Gradle 9.1

- Toolchain, Wrapper, distribution URL/checksum, plugin compatibility Config 검토
- 현재 이용 가능한 JDK/Gradle에서 실행 가능한 Compile·Test·정적 Gate 수행
- Java 25 전용 문법·API 사용 지점 Source Review
- fresh clone 실행 Script와 Preflight 작성
- `java -version`, `gradlew --version`, clean/check/test/publication 명령과 Evidence Template 제출

### DB 3종

- 세 Vendor Schema·Migration·Rollback·Metadata·Seed 정적 parity
- SQL Parser·Query ID·Consumer·pack hash 검증
- 실제 DB 실행 Script, 접속 변수, Secret 이름, Version 수집, Schema diff, rollback/reupgrade 명령 준비
- 실제 PASS는 Codex/QA/DBA 환경에서 판정

### Browser

- TypeScript strict compile, production build, route/OpenAPI contract, accessibility static test
- Playwright Config·Browser Matrix·E2E Scenario 완성
- 실제 Browser binary 실행은 Codex/QA 환경에서 판정

### Multi-process

- 상태기계·fencing·lease·idempotency·retry·UNKNOWN·reconcile 단위/통합 Harness
- Process start/kill/restart Script와 대사 기준 완성
- 실제 다중 Process·Broker 실행은 Codex/QA 환경에서 판정

## 5. 직접 수행 21건

환경 의존 4건을 제외한 Architecture, Consumer, Security, ADM/BZA, OpenAPI, Generator, SQL, 문서, 원장, Evidence, Packaging 등 21건은 개발GPT가 실제 구현·실행·자체검수까지 수행한다.

환경 문제를 이유로 직접 수행 가능한 항목을 `미완료`로 이월하지 않는다.

## 6. 상태 판정 예시

### 대체검증 성공·Target Runtime 미실행

- 개발GPT_수행여부: 완료
- 개발GPT_상태: 완료
- 개발GPT_자체검수상태: 완료
- development_status: 재확인 필요
- verification_status: 미검증
- next_action: Codex Target Runtime 실행

### Target Runtime에서 제품 결함 발견

- Codex_검수보완상태: 재개발 요청
- 개발GPT_상태: 재개발 요청
- 같은 Requirement ID로 Source·Config·Script 수정
- 수정 후 대체검증과 Target Runtime 재실행

### Target Runtime 환경 자체 부재

- 개발GPT 상태를 다시 열지 않음
- Codex_상태: 미완료 또는 재검수 요청
- QA/인프라 실행 대기
- 환경·권한·명령·Evidence를 원장에 유지

## 7. 제출물

- `REQUEST.md`
- `REQUIREMENT_STATUS.csv`
- `ENVIRONMENT_DELEGATION_MATRIX.csv`
- `NO_PINGPONG_POLICY.md`
- `EXECUTION_GATE.md`
- `REVIEW_SUMMARY.md`
- `HANDOVER_TEMPLATE.md`
- `PACKAGE_MANIFEST.json`
- `SHA256SUMS.txt`

Commit·Push·삭제는 수행하지 않는다.


## REV-004 Steering Addendum

- FDEV-025 P0 Starter Catalog/BOM exact-equality defect를 append한다.
- 기존 47,745 exact ID 및 32 request_id는 변경·삭제·재번호화하지 않는다.
- openapi-webmvc 구현은 public web-api Profile 내부로 통합하고 기존 경로 삭제는 승인 전 수행하지 않는다.
