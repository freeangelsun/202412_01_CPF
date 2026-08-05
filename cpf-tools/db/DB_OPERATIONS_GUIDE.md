# CPF DB Operations Provider Guide

## Purpose

This provider-side contract records database lifecycle execution without claiming that an ADM/BZA route already exists. The canonical contract is `cpf-tools/db/cpf-db-operability-contract.json`; the generated provider OpenAPI is `cpf-tools/db/generated/cpf-db-operations.openapi.json`.

## Invocation

1. Produce sanitized JSON evidence using the required fields in the canonical contract.
2. Calculate the evidence file SHA-256.
3. Invoke `cpf-tools/scripts/invoke-cpf-db-operability-gate.ps1` with an independent operator and approver, approval reference, reason, and `-ConfirmSanitizedEvidence`.
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
