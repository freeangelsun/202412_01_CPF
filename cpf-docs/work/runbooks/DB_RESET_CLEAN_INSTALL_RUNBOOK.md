# 현재 환경 선행 조건

- 회사 PC에서는 CPF Schema와 Table이 이미 삭제되었다.
- 집 PC는 신규 MariaDB다.
- 첫 검증은 Reset을 실행하지 않고 Empty Install부터 시작한다.
- Reset 절차는 재설치·회귀 검증을 위한 두 번째 Cycle부터 사용한다.
- Install은 Schema·Table·Constraint·Index·Service User·Grant·Product Meta Seed를 모두 생성해야 하며 Source·Mapper·API·UI·Generator와 명칭이 일치해야 한다.

# CPF DB Reset·Clean Install Runbook

## 1. 목적

회사 PC와 집 PC의 MariaDB를 동일한 빈 CPF 환경으로 만들고 공식 Install Script의 재현성을 검증한다.

## 2. 사용자 준비

- MariaDB Server
- MariaDB CLI
- Admin 접속 가능
- 다른 Application Schema 존재 여부 확인
- Backup 필요 여부 확인

사용자가 CPF Schema/Table을 수동 생성하지 않는다.

## 3. 사전 조회

```sql
SELECT VERSION();

SELECT SCHEMA_NAME
FROM information_schema.SCHEMATA
ORDER BY SCHEMA_NAME;
```

현재 CPF 후보 Schema:

```text
cpfDB
cmnDB
admDB
bzaDB
batDB
mbrDB
accDB
refDB
exsDB
```

실제 Allowlist는 Source·Install 정본 검수 후 확정한다.

## 4. Reset 안전장치

Reset Script는 다음을 만족해야 한다.

- Default Dry Run
- `-Apply` 명시
- Host·Port·Database 목록 출력
- Exact Case-sensitive Allowlist
- System Schema 차단
- Allowlist 외 Schema 차단
- Wildcard 금지
- User 확인
- 결과 Log
- Secret Masking

## 5. 금지 예

```sql
DROP DATABASE LIKE 'cpf%';
```

```powershell
Get-Databases | Where-Object Name -Like '*DB' | Remove
```

## 6. 권장 실행 흐름

```text
Clone
→ Version 확인
→ Reset Dry Run
→ Allowlist 검토
→ Apply
→ Schema 0 확인
→ Provision
→ Install
→ Verify
→ Seed Re-run
→ Backend Build
→ Start
→ API
→ Frontend
→ Browser
→ Stop
→ Reset/Reinstall
→ Upgrade
→ Rollback
```

## 7. DB 확인 SQL

### Schema

```sql
SELECT SCHEMA_NAME
FROM information_schema.SCHEMATA
WHERE SCHEMA_NAME IN (
  'cpfDB','cmnDB','admDB','bzaDB','batDB',
  'mbrDB','accDB','refDB','exsDB'
)
ORDER BY SCHEMA_NAME;
```

### Table

```sql
SELECT TABLE_SCHEMA, TABLE_NAME, TABLE_ROWS
FROM information_schema.TABLES
WHERE TABLE_SCHEMA IN (...)
ORDER BY TABLE_SCHEMA, TABLE_NAME;
```

### FK

```sql
SELECT TABLE_SCHEMA, TABLE_NAME, CONSTRAINT_NAME,
       REFERENCED_TABLE_SCHEMA, REFERENCED_TABLE_NAME
FROM information_schema.KEY_COLUMN_USAGE
WHERE REFERENCED_TABLE_SCHEMA IN (...);
```

### Index

```sql
SELECT TABLE_SCHEMA, TABLE_NAME, INDEX_NAME, NON_UNIQUE
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA IN (...)
ORDER BY TABLE_SCHEMA, TABLE_NAME, INDEX_NAME;
```

### Grants

```sql
SHOW GRANTS FOR CURRENT_USER;
```

## 8. cmnDB 확인

최종 신규 설치에서 `cmnDB` Table 수는 1이어야 한다.

```sql
SELECT TABLE_NAME
FROM information_schema.TABLES
WHERE TABLE_SCHEMA='cmnDB';
```

## 9. 다른 Schema 보호

Reset 전후 전체 Schema 목록 Hash 또는 Snapshot을 비교해 Allowlist 외 Schema가 변하지 않았음을 증명한다.

## 10. Evidence

- Server Version
- Schema Before
- Reset Plan
- Apply Result
- Schema After Reset
- Install Result
- Object Count
- Seed Count
- Grants
- Other Schema Diff
- Reinstall
- Upgrade
- Rollback
