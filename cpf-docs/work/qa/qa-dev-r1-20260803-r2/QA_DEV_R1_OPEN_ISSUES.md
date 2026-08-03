# QA Open Issues

## QA-DEV-R1 판정 완료

- `CPF-SELF-DEV-S4-001` ~ `S4-009`: 모두 `미통과`
- `CPF-SCOPE-000001 execution_order 1~10,027`: `미통과`
- 확정 Finding: 25건
- QA 최종 통과: 0건

## Repository Source 수정이 필요한 미해결 결함

1. Transaction 업무 Endpoint Annotation Coverage와 Interceptor 적용성
2. HTTP Client DNS Resolve·Address Validation·Pinned Connection
3. PostgreSQL/Oracle Fresh Install SQL의 MariaDB 전용 타입
4. Starter Catalog Package Ownership·baseline SHA·Publication 계약
5. Split/Traceability/Owner/DB/Starter Gate의 False-positive 구조
6. BZA API 호환성과 Actor Guard Body Type 누락

## Source 수정 후 외부 환경 검증

1. 28개 Split Part 실제 byte/hash/row continuity — 전체 Checkout 필요
2. Java 25 Full Gradle Build/Test/Publication/SBOM
3. 3 Vendor 실제 Provision/Install/Upgrade/Rollback/Runtime
4. ADM/BZA 전체 Build·Playwright
5. Audit Multi-instance·Process Kill·UNKNOWN Recovery
6. Registry/Signing/Release

## 안전

- QA는 Repository Source를 수정하지 않았다.
- Commit/Push/Branch/Tag/PR/삭제/Working Tree 정리를 수행하지 않았다.
- 사용자 `머지` 지시 전 활성 정본에 QA 결과를 반영하지 않는다.
