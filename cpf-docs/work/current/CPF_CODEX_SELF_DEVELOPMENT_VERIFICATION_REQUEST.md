# CPF 자체 개발 독립 검수 요청서

이 문서는 자체 개발 Requirement 구현과 Push가 완료된 뒤 사용한다.

## 범위

- `CPF-SELF-DEV-001`부터 `CPF-SELF-DEV-030`
- 외부 검수 정본은 포함하지 않는다.
- 검수자는 자체 개발 Requirement의 Source·Consumer·Test·Evidence만 독립 확인한다.

## 단일 검증 순서

1. 최신 Push SHA와 Clean Working Tree
2. Requirement·Source·Consumer·Test·Evidence 정합성
3. Repository Hygiene·Secret·Ownership Boundary
4. Java 25 Fresh Cache Build·Test·Publication
5. ADM/BZA 전체 OpenAPI·Generated Client·Clean Frontend
6. Chromium·Firefox·WebKit 전체 Route
7. Oracle·PostgreSQL·MariaDB Baseline Upgrade·Rollback·Reapply
8. Kafka·Batch·Scheduler·Gateway·Agent·Process Kill·Recovery
9. Supply-chain·Artifact Hash
10. Exact-SHA Evidence와 최종 상태 재판정

동일 전체 Build·Browser·DB 검증을 반복하지 않는다.
실패 시 Source Defect와 Environment Blocker를 구분하고 영향 범위의 최소 단위만 재검증한다.
Source 수정이 필요하면 독립 검수 완료로 판정하지 않는다.
