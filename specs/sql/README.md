# CPF initial database SQL

This folder contains MariaDB/MySQL scripts for a clean CPF local or test
installation. Scripts are intentionally separated by purpose so a DBA can run
only the required area.

## Recommended local execution

For a local developer install, run the all-in-one script from the repository
root:

```powershell
mysql -h localhost -P 3306 -u root -p < specs/sql/00_all_install.sql
mysql -h localhost -P 3306 -u root -p < specs/sql/99_smoke_check.sql
```

Or run install and smoke check in one command:

```powershell
mysql -h localhost -P 3306 -u root -p < specs/sql/00_all_install_and_smoke.sql
```

The `00_*` files use MySQL client `SOURCE` commands, so run them from the
repository root. If a DBA runs scripts from another working directory, use the
split files below.

## Split execution order

1. `01_create_databases.sql`
2. `10_pfw_schema.sql`
3. `20_cmn_schema.sql`
4. `30_adm_schema.sql`
5. `40_business_sample_schema.sql`
6. `50_framework_seed_data.sql`
7. `60_adm_seed_data.sql`
8. `70_test_data.sql`
9. `99_smoke_check.sql` for verification only

## Target databases

| Database | Owner | Main tables |
| --- | --- | --- |
| `pfwDB` | PFW | `TRAN_LOG`, `TRAN_LOG_DTL` |
| `cmnDB` | CMN and current MBR local sample | `code_table`, `message_table`, `response_code_table`, `config_table`, `cache_refresh_event`, `cmn_member`, `member` |
| `admDB` | ADM | `operator_*`, `dynamic_log_level_rule` |
| `accDB` | ACC | `acc_member` |
| `mbrDB` | Optional separated MBR DB | `member` |

## Notes

- The scripts use `CREATE TABLE IF NOT EXISTS` and idempotent seed inserts.
- Response codes use `{S|E}{MODULE}{GROUP}{SEQ}`. Examples: `SACC000000`,
  `EACC010001`, `EPFW900001`.
- Message codes use `M{MODULE}{GROUP}{SEQ}`. One `message_table` row stores both
  `external_message` and `internal_message`.
- `message_format_type` is `FIXED` or `INDEXED`; indexed messages use common
  placeholders such as `{0}`, `{1}`.
- `response_code_table` links each response code to one `message_code`. Runtime
  exception handling and logging should resolve this table from
  `responseCodeCache`, then resolve `message_table` from `messageCache`.
- `TRAN_LOG` keeps response metadata separately: `HTTP_STATUS` is the numeric
  HTTP status, `RESPONSE_CODE` is the standard response code, and
  `MESSAGE_CODE` is the standard message code. `EXTERNAL_MESSAGE` and
  `INTERNAL_MESSAGE` are populated for success and failure. `ERROR_CODE` and
  `ERROR_MESSAGE` are populated only for failed transactions.
- ADM operator and authorization APIs are DB-first against `operator_user`,
  `operator_user_role`, `operator_role`, `operator_menu`, and
  `operator_role_menu`. Runtime code keeps a local education fallback only when
  the ADM database is unavailable.
- Dynamic log-level rules are applied in WAS memory for immediate runtime
  effect and persisted to `dynamic_log_level_rule` for ADM tracking and later
  multi-WAS propagation.
- The ADM seed account is for local/test only: `admin / Adm!n12345`.
- Production secrets, JWT secrets, token values, and raw passwords must not be
  stored in these scripts.
- If each service uses a physically separate DB account, grant only the target
  database privileges required by that service.
- Current local MBR configuration points to `cmnDB`; `mbrDB.member` is included
  for a later separated MBR datasource test.

## Local Smoke Test

If a MariaDB/MySQL client is installed and you want to run split files manually,
run the scripts in order:

```powershell
mysql -h localhost -P 3306 -u root -p < specs/sql/01_create_databases.sql
mysql -h localhost -P 3306 -u root -p < specs/sql/10_pfw_schema.sql
mysql -h localhost -P 3306 -u root -p < specs/sql/20_cmn_schema.sql
mysql -h localhost -P 3306 -u root -p < specs/sql/30_adm_schema.sql
mysql -h localhost -P 3306 -u root -p < specs/sql/40_business_sample_schema.sql
mysql -h localhost -P 3306 -u root -p < specs/sql/50_framework_seed_data.sql
mysql -h localhost -P 3306 -u root -p < specs/sql/60_adm_seed_data.sql
mysql -h localhost -P 3306 -u root -p < specs/sql/70_test_data.sql
mysql -h localhost -P 3306 -u root -p < specs/sql/99_smoke_check.sql
```

`99_smoke_check.sql` does not modify data. It checks core table row counts and
representative response-code/message links.
