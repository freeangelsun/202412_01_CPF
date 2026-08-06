# Codex Independent Review Request — REV-004 R4

## Immutable target

- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- Required pre-apply HEAD: `a8be27a34bdac0b7c075e06d6e86571244c96421`
- Overlay revision: `REV-004-R4`

## Review sequence

1. Confirm a clean working tree and exact HEAD before applying the Root Overlay. Do not inherit old PASS evidence.
2. Review source boundaries and actual consumers for the five FDEV rows.
3. Execute the low-cost gates before full build:

```text
python -B cpf-tools/verification/final-dev/verify-rev004-overlay.py .
python -B cpf-tools/verification/verify_starter_catalog.py --root .
python -B cpf-tools/scripts/verify-cpf-qa34-build-contract.py --root .
python -B cpf-tools/verification/qa38/verify-qa38-structure.py .
python -B cpf-tools/verification/qa39/verify-qa39-canonical-starter-closure.py .
```

4. With Java 25 and the project Gradle wrapper:

```text
./gradlew --no-daemon clean check
```

5. With Node >=22.18.0 in `cpf-admin/frontend`:

```text
npm ci
npm run verify
```

6. With PowerShell/Pester:

```text
Invoke-Pester cpf-tools/verification/final-dev/tests/run-db3-lifecycle.Tests.ps1
```

7. With non-production DB3 endpoints and credentials in environment variables:

```text
pwsh -NoProfile -File cpf-tools/verification/final-dev/run-db3-lifecycle.ps1 -ExpectedHead a8be27a34bdac0b7c075e06d6e86571244c96421 -EvidenceDir build/evidence/db3-lifecycle
```

8. Confirm `cpf-starters/openapi-webmvc` remains retained unless explicit user/QA deletion approval exists.
9. Scan logs/evidence for secrets and original corrected payloads.
10. Update Codex-owned columns/evidence only. Do not mark QA complete.

## Fail criteria

Any missing consumer, generated-source drift, duplicate operation ID, active/internal BOM mismatch, password in argv/log/evidence, automatic retry of UNKNOWN mutation, non-409 version conflict, raw corrected payload in audit/browser storage, or unapproved deletion is a failure.
