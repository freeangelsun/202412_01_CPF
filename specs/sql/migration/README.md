# DB Migration 표준

CPF는 Flyway 형식의 `V{버전}__{설명}.sql` migration을 표준으로 둡니다.

## 원칙

- 설치용 SQL은 `specs/sql/*.sql`에서 관리합니다.
- 운영 변경 이력은 `specs/sql/migration/flyway`에 누적합니다.
- migration은 `cpf_*_migration` 계정으로만 실행합니다.
- 애플리케이션은 `cpf_*_app` 계정만 사용하고 DDL 권한을 갖지 않습니다.
- rollback은 자동 역방향 SQL보다 백업/복구와 보정 migration을 우선합니다.

## 현재 기준

| 파일 | 설명 |
| --- | --- |
| `flyway/V1__cpf_baseline_install.sql` | CPF 1차 baseline 전체 설치 SQL |

운영 프로젝트에서 Flyway Gradle/Spring 의존성을 활성화할 때는 각 서비스의 datasource와 migration 계정을 분리하고, prod profile에는 비밀번호 기본값을 두지 않습니다.
