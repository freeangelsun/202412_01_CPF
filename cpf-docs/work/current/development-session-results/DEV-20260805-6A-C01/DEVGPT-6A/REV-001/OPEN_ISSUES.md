# DEVGPT-6A Open Issues

## 외부 환경 차이

1. **Full Frontend Runtime**: Node 22.18+ 및 정상 npm Registry에서 `npm ci`, `npm run verify`, Vitest, Vite production build, Playwright, accessibility를 재실행해야 한다.
2. **Java25/Gradle Runtime**: Java25와 전체 Repository에서 OpenAPI Web MVC Starter 및 ADM/BZA Backend compile/test/runtime를 재실행해야 한다.
3. **Official DB Vendors**: Oracle/PostgreSQL/MariaDB install/upgrade/rollback/query parity는 DEVGPT-6E 통합 Evidence가 필요하다.
4. **Root Integration**: `cpf-starters/openapi-webmvc`의 settings/catalog/BOM/publication 등록은 DEVGPT-6F 통합 검토가 필요하다.
5. **Runtime-control Ownership**: 1,581개 6C-primary Requirement는 `EXCLUDED_PRIMARY_OWNER_REQUIREMENTS.csv` 기준으로 6C 검수와 6A Consumer 통합 확인이 필요하다.

## 비결함 환경 차이

- GitHub direct clone DNS 실패는 제품 결함으로 판정하지 않았다.
- `npm ci` Registry 404와 Node engine mismatch는 제품 Source Gate 성공으로 대체했지만 전체 Runtime PASS로 기록하지 않았다.
- Java21 Harness 결과는 Java25/full Spring Runtime 성공으로 확대하지 않았다.
