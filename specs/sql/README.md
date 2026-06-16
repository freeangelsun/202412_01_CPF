# CPF Initial Database SQL

`specs/sql`은 CPF 로컬/테스트 MariaDB 설치 스크립트를 담고 있습니다. 현재 기준은 단순합니다.

- `pfwDB`: 프레임워크/운영 공통 메타데이터
- `admDB`: 관리자/운영자 권한과 감사
- `accDB`: 계좌 업무 샘플
- `mbrDB`: 회원 업무 샘플

`cmnDB`는 더 이상 만들지 않습니다. CMN 모듈은 공통 기능 라이브러리이고, 코드/메시지/응답코드/설정/캐시 이벤트 같은 프레임워크 참조 데이터는 `pfwDB`가 소유합니다.

## One File Execution

`00_all_install.sql`과 `00_all_install_and_smoke.sql`은 `SOURCE` 실행순서 파일이 아니라 실제 SQL 전체가 펼쳐진 실행 파일입니다.

```powershell
cmd.exe /d /c '"C:\Program Files\MariaDB 12.3\bin\mariadb.exe" --host=127.0.0.1 --port=3306 --user=root --password=비밀번호 --ssl=0 --default-character-set=utf8mb4 < "specs\sql\00_all_install_and_smoke.sql"'
```

PowerShell `Get-Content | mariadb` 파이프는 한글 seed가 깨질 수 있으니 사용하지 않습니다.

## Split Files

분리 파일은 검토와 유지보수용입니다.

1. `01_create_databases.sql`
2. `02_create_service_users.sql`
3. `03_cleanup_legacy_layout.sql`
4. `10_pfw_schema.sql`
5. `20_cmn_schema.sql`
6. `30_adm_schema.sql`
7. `40_business_sample_schema.sql`
8. `50_framework_seed_data.sql`
9. `60_adm_seed_data.sql`
10. `70_test_data.sql`
11. `99_smoke_check.sql`

## Database Ownership

| Database | Owner | Tables |
| --- | --- | --- |
| `pfwDB` | Framework | `TRAN_LOG`, `TRAN_LOG_DTL`, `code_table`, `message_table`, `response_code_table`, `config_table`, `cache_refresh_event`, `file_exchange_log`, `security_jwt_key`, `security_token_audit_log` |
| `admDB` | Admin | `operator_user`, `operator_role`, `operator_user_role`, `operator_menu`, `operator_role_menu`, `operator_session`, `operator_audit_log`, `dynamic_log_level_rule` |
| `accDB` | Account sample | `acc_account` |
| `mbrDB` | Member sample | `member` |

모든 base table은 `CREATED_BY`, `CREATED_AT`, `UPDATED_BY`, `UPDATED_AT` 공통 컬럼을 가집니다.

## Legacy Cleanup

`03_cleanup_legacy_layout.sql`은 이전 로컬 샘플 구조를 정리합니다.

- `cmnDB` 제거
- `admDB.member` 제거
- `accDB.acc_member` 제거

현재 구조에서 ACC는 `acc_account`, MBR은 `member`만 사용합니다.

## Service Users

`02_create_service_users.sql`는 local/test 전용 계정을 만듭니다.

| User | Database | Privileges |
| --- | --- | --- |
| `cpf_pfw` | `pfwDB` | `SELECT`, `INSERT`, `UPDATE`, `DELETE` |
| `cpf_adm` | `admDB` | `SELECT`, `INSERT`, `UPDATE`, `DELETE` |
| `cpf_acc` | `accDB` | `SELECT`, `INSERT`, `UPDATE`, `DELETE` |
| `cpf_mbr` | `mbrDB` | `SELECT`, `INSERT`, `UPDATE`, `DELETE` |

운영 환경에서는 local password를 사용하지 말고 환경변수, Vault, KMS, Secret Manager 등으로 교체해야 합니다.

## Cache Refresh Event

`cache_refresh_event`는 다중 WAS 캐시 동기화를 위한 프레임워크 이벤트 로그입니다. 코드/메시지/응답코드/설정 캐시가 한 WAS에서 바뀌었을 때 다른 WAS가 DB polling으로 변경을 감지해 자기 메모리 캐시를 refresh합니다.

브로커가 있는 운영 환경에서는 Kafka/RabbitMQ/Redis Pub/Sub을 실시간 경로로 쓰고, `cache_refresh_event`는 fallback 및 추적 로그로 유지하거나 보존 기간을 짧게 가져가면 됩니다.

## Verified Checks

실DB 검증 기준:

- `00_all_install_and_smoke.sql` 직접 실행 성공
- 같은 파일 재실행 시 row count 유지
- `cmnDB` 없음
- `accDB.acc_member` 없음
- 모든 base table 공통 등록/수정 컬럼 존재
- `cpf_acc`가 `pfwDB.code_table` 조회 시 권한 거부
