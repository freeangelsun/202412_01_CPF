# Open Issues

## 제품 차단

- P0 재개발 요청: 10건
- P0 QA 직접보완 교차검토 대기: 3건
- P1 재개발 요청: 2건
- P1 QA 직접보완 교차검토 대기: 2건
- 개발GPT 교차검토 대기: 5개 Finding
- Codex 교차검토 대기: 5개 Finding
- 독립 QA 재검수 대기: 5개 Finding

## 필수 환경

- Java 25 및 fresh Gradle cache
- Oracle/PostgreSQL/MariaDB empty/upgrade DB와 DDL/DML 권한
- ADM/BZA Browser E2E 환경
- ADM Spring 2-instance와 공용 ADM DB
- GitHub Actions required check/branch protection 관리

## 미검수 자동 은폐 방지

현재 71,321개 행을 모두 검수 완료했다고 기록하지 않았다. 대신 누락된 단일 Current 원장을 생성하는 Builder와, 한 행이라도 QA 판정/Evidence가 없으면 완료를 거부하는 Gate를 포함했다. 임의 일괄 통과·미통과 채우기는 금지된다.
