# CPF Masking Policy JDBC Schema Contract

Owner: DEVGPT-6B runtime provider. Canonical Oracle/PostgreSQL/MariaDB DDL, migration,
rollback and installer integration remain DEVGPT-6E owned.

## Required objects

### `cpf_masking_policy_shard`

- `shard_id` integer primary key.
- Exactly one required seed row for the provider: `shard_id = 0`.
- The provider executes `SELECT ... FOR UPDATE` before every initialization or mutation.

### `cpf_masking_policy_head`

- `singleton_id` integer primary key; exactly one row with value `1`.
- `active_version` bigint/not-null and references an existing policy version.
- Update must support `WHERE singleton_id = 1 AND active_version = ?` optimistic CAS.

### `cpf_masking_policy_version`

- `policy_version` bigint primary key.
- `sensitive_keys_csv` supports at least 16,640 ASCII characters. Keys are canonical
  `[a-z0-9_.-]{2,64}`, sorted and comma-separated; commas are forbidden in keys.
- `max_length` integer, `mask_bearer_flag` boolean-equivalent.
- `updated_at` timestamp with at least millisecond precision.
- `updated_by` varchar(128), `update_reason` varchar sized for sanitized audit reason.
- Index policy versions for descending history and ascending bounded retention cleanup.

### `cpf_masking_policy_command`

- `command_id_hash` char/varchar(64) primary key. Raw command IDs are forbidden.
- `command_hash` char/varchar(64), both lowercase SHA-256.
- Complete result snapshot columns: version, sensitive-key CSV, max length, bearer flag,
  updated time, actor and sanitized reason. Replay must not depend on retained history rows.
- `recorded_at` timestamp, indexed for TTL deletion.

## Atomicity and failure semantics

A mutation transaction locks shard `0`, removes expired command rows, resolves command
replay/conflict, verifies the active version, inserts the next policy, CAS-updates the head,
inserts the hashed command result and trims old history. Any pre-commit error rolls back.
A JDBC commit exception is `UNKNOWN_RESULT`; callers must re-read the active version.

## Security and operations

- No raw command ID, approval token, secret value or bearer token may be stored.
- Runtime status exposes only counts, configured bounds, health and active version.
- Missing seed/schema fails application startup for `mode=jdbc`.
- DDL must be idempotent, upgradeable and rollbackable for Oracle, PostgreSQL and MariaDB.
