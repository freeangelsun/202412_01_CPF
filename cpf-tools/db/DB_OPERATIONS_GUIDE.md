# CPF DB Operations Provider Guide

## Purpose

This provider-side contract records database lifecycle execution without claiming that an ADM/BZA route already exists. The canonical contract is `cpf-tools/db/cpf-db-operability-contract.json`; the generated provider OpenAPI is `cpf-tools/db/generated/cpf-db-operations.openapi.json`.

## Invocation

1. Produce sanitized JSON evidence using the required fields in the canonical contract.
2. Calculate the evidence file SHA-256.
3. Invoke `cpf-tools/db/tools/invoke-cpf-db-operability-gate.ps1` with an independent operator and approver, approval reference, reason, and `-ConfirmSanitizedEvidence`.
4. Persist the normalized output as immutable evidence and retain its source SHA, trace identifiers, metrics, health transition, alert references, and runbook reference.

## Failure and recovery

- Invalid vendor, capability, identifier, timestamp, hash, metrics, trace, or health transition fails closed.
- `UNKNOWN` requires `reconcileRequired=true` and at least one alert.
- `RECONCILED` requires `reconcileRequired=false`.
- A successful operation cannot finish in `DOWN` or `UNKNOWN` health.
- Secret-bearing keys such as password, token, credential, or private key are rejected recursively.
- The wrapper verifies the evidence file hash and the operator/approver/reason/approval values before normalization.

## Consumer boundary

The DB provider contract and generated OpenAPI are implemented in the DB owner path. ADM backend routing and ADM/BZA frontend consumers remain explicit cross-session work; this guide does not represent them as implemented.


## Vendor pack provenance

Provider evidence에는 `canonicalBaselineId`, `vendor`, `vendorPackChecksum`, `migrationBaseline`, `overrideManifestChecksum`를 추적 가능하게 남긴다. 공식 Vendor는 `oracle|postgresql|mariadb`만 허용한다. MySQL/MSSQL/H2를 공식 Provider Evidence로 정규화하지 않는다.

Vendor Pack 실행기는 Canonical에서 생성된 Pack과 manifest-tracked override를 소비한다. 수동으로 변경된 Vendor SQL이 Canonical/Manifest checksum과 불일치하면 fail closed한다.

DB 영향 변경의 운영 검증은 Vendor별 별도 이름의 임의 시나리오가 아니라 같은 Scenario ID를 Oracle/PostgreSQL/MariaDB에 반복 실행해 parity를 판정한다.
