# Generated Business Domain / DB Artifact Canonicalization

## Fixed business domain policy

EXS is a Generated Business Domain. CPF does not ship a fixed `cpf-external` module or `exsDB`.

When an external business domain is required:

```powershell
pwsh -NoProfile -File .\cpf-tools\scripts\create-domain.ps1 `
  -DomainName external -SystemCode EXS -Database Y -DatabaseVendor mariadb -Apply
```

The result must have exactly the same generated structure as PAY/INS/CRM created from the same Golden Generated Domain template.

`cpf-reference` owns EDU/reference external simulator examples. It is not an EXS production business domain.

## DB artifact change rule

Whenever Schema / Column / Index / FK / Seed / Migration / Vendor SQL / MyBatis SQL metadata changes:

```powershell
pwsh -NoProfile -File .\cpf-tools\scripts\sync-database-artifacts.ps1
```

This regenerates installation bundles and `database-schema-manifest.json`, then runs drift/integrity gates.

Never hand-edit only a generated vendor bundle.

## Fresh install versus migration

- no expected tables: fresh DDL
- all expected tables and exact columns: DDL skipped
- partial tables: fail closed
- full tables but column drift: fail closed and require migration
- destructive reset/drop: separate explicit operation only
