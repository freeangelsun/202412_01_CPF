# CPF SQL 가이드

`specs/sql`은 로컬/검증용 MariaDB 설치 SQL과 운영 migration 기준을 함께 관리합니다. 분리 SQL은 유지보수 기준이고, `00_all_install.sql`, `00_all_install_and_smoke.sql`는 실제 SQL 본문을 모두 포함한 단일 실행 파일입니다.

## 실행 파일

| 파일 | 용도 |
| --- | --- |
| `00_all_install.sql` | DB, 계정, 스키마, seed, 테스트 데이터를 한 번에 설치 |
| `00_all_install_and_smoke.sql` | 전체 설치 후 smoke 조회까지 실행 |
| `migration/flyway/V1__cpf_baseline_install.sql` | 신규 환경 Flyway baseline SQL |
| `migration/flyway/V2__add_transaction_header_columns.sql` | 기존 설치 DB의 거래 헤더 컬럼 보강 |
| `migration/flyway/V3__adm_cmn_batch_security_operations.sql` | CMN 채번 확장, ADM 버튼 권한/보안, PFW 배치 메타 보강 |

## 분리 SQL 순서

1. `01_create_databases.sql`
2. `02_create_service_users.sql`
3. `10_pfw_schema.sql`
4. `20_cmn_schema.sql`
5. `30_adm_schema.sql`
6. `40_business_sample_schema.sql`
7. `50_framework_seed_data.sql`
8. `55_cmn_seed_data.sql`
9. `60_adm_seed_data.sql`
10. `70_test_data.sql`
11. `99_smoke_check.sql`

`archive/03_cleanup_legacy_layout.sql`은 초기 개발 중 사용했던 구형 명칭 정리 참고 파일입니다. 공식 신규 설치와 baseline에는 구형 테이블을 포함하지 않습니다.

## DB 소유권

| DB | 역할 | 주요 테이블 |
| --- | --- | --- |
| `pfwDB` | 프레임워크 엔진/메타 | `pfw_transaction_log`, `pfw_code`, `pfw_message`, `pfw_response_code`, `pfw_config`, `pfw_cache_refresh_event`, `pfw_batch_*`, `pfw_security_*` |
| `cmnDB` | 업무 공통 기능 | `cmn_sequence`, `cmn_sequence_issue_log`, `cmn_notification_log`, `cmn_business_log` |
| `admDB` | 관리자/운영 | `adm_operator`, `adm_role`, `adm_menu`, `adm_button`, `adm_role_menu`, `adm_role_button`, `adm_password_*`, `adm_audit_log`, `adm_operator_session`, `adm_ip_allowlist`, `adm_mfa_otp_secret` |
| `accDB` | 계정 샘플 | `acc_account` |
| `mbrDB` | 회원 샘플 | `mbr_member`, `mbr_member_role`, `mbr_member_role_history`, `mbr_member_login_history` |

## 테이블/컬럼 표준

- 테이블명은 `{주제영역}_{업무명}` lower snake case를 사용합니다.
- `*_table` suffix는 사용하지 않습니다.
- FK, UK, index는 `fk_`, `uk_`, `ix_` + 테이블명 + 의미 순서로 작성합니다.
- 모든 테이블은 `created_by`, `created_at`, `updated_by`, `updated_at`을 가집니다.
- 모든 테이블과 주요 컬럼은 DB에서 바로 읽을 수 있도록 한글 `COMMENT`를 작성합니다.

## CMN 채번 기준

`cmn_sequence`는 `sequence_key` 직접 호출과 `business_area + business_key + sequence_kind + channel_code` 조합 호출을 모두 지원합니다.

- `start_value`, `increment_by`, `min_value`, `max_value`로 번호 범위를 제어합니다.
- `reset_cycle`은 `NONE`, `DAY`, `MONTH`, `YEAR`, `PATTERN`을 기준으로 사용합니다.
- `log_enabled_yn='Y'`이면 `cmn_sequence_issue_log`에 발급 이력을 남깁니다.
- 발급번호는 gap을 허용합니다. 업무 처리 실패 후 번호 재사용은 감사 추적을 해치므로 금지합니다.

## Flyway 기준

Flyway를 표준 migration 도구로 채택합니다. 운영 적용 시 migration 계정만 DDL 권한을 가지고, 애플리케이션 계정은 DML 권한만 가집니다.

신규 변경 절차:

1. `V4__add_xxx.sql`처럼 증가하는 버전 파일을 추가합니다.
2. 같은 변경을 분리 설치 SQL에도 반영합니다.
3. `00_all_install.sql`, `00_all_install_and_smoke.sql`, Flyway baseline 또는 증분 migration을 함께 갱신합니다.
4. MariaDB에서 idempotent, FK/index, seed, 권한을 확인합니다.

## Smoke 실행 예시

운영에서는 비밀번호가 콘솔 기록에 남지 않도록 secret 주입 방식을 사용합니다.

```powershell
cmd.exe /d /c '"C:\Program Files\MariaDB 12.3\bin\mariadb.exe" --host=127.0.0.1 --port=3306 --user=root --password=<비밀번호> --ssl=0 --default-character-set=utf8mb4 < "specs\sql\00_all_install_and_smoke.sql"'
```

## 캐시 이벤트 테이블

`pfw_cache_refresh_event`는 Redis/Kafka가 없거나 장애가 있는 환경에서도 다중 WAS 캐시 갱신을 추적하고 재처리하기 위한 DB fallback 이벤트입니다. broker가 있으면 broker를 우선 사용하고, DB event는 추적과 fallback 용도로 유지합니다.
