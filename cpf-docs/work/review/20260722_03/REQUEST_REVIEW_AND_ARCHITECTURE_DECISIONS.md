# CPF 요청 리뷰와 확정 Architecture Decision

## 1. 결정 요약

1. CPF 정식 명칭은 Core Platform Framework다.
2. 공식 Module은 `cpf-*`, Java Package는 `com.cpf.*`다.
3. `cpf-core`의 공식 SystemCode는 `CPF`다.
4. `cpf-common`은 고객 업무 공통 Extension이며 기술 Engine 저장소가 아니다.
5. `cmnDB`는 Sample Table 1개만 제공한다.
6. Fixed-Length 범용 Engine은 Core, 기관별 정의와 Adapter는 External이 소유한다.
7. 업무 채번은 Core/Common 기본 기능이 아니며 BZA는 선택형 Customization Sample만 제공한다.
8. Batch, Scheduler, Agent, Runner, Worker와 Center-Cut은 `cpf-batch`가 소유한다.
9. ADM은 Platform Control Plane, BZA는 Customer Business Admin이다.
10. ADM/BZA Frontend는 Java WAS와 독립 Build/Deploy/Rollback을 지원한다.
11. Empty Install, Upgrade, Rollback과 Recovery는 제품 완료 묶음이다.
12. Markdown은 구현 중 정본이며 DOCX/PDF는 최종 안정화 후 생성한다.

## 2. DB 결정

회사·집 PC 모두 CPF DB가 비어 있으므로 정본 설치 Script가 Schema, User, Table, Constraint, Index와 Product Meta를 전부 재생성해야 한다. 초기화 상태를 이유로 Historical Flyway를 무단 수정하지 않는다.

## 3. 구현 대안 규칙

Codex는 더 나은 대안을 선택할 수 있지만 공식 명칭, Ownership, cmnDB 최소화, Fixed-Length Owner, 업무 채번 정책, Root 정리와 완료 판정 정책을 생략할 수 없다. 대안은 근거, 의존성, Migration, Compatibility와 Evidence를 보고한다.

## 4. 완료 금지

Interface만 만들고 기본 구현·복구·운영을 생략하거나, Sample을 제품으로 간주하거나, 정적 Scan만으로 완료 처리하지 않는다.
