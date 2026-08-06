# Integration Change Request — Full Assignment Column Aliases

- request_id: `ICR-V9-S05-S06-FULL-ASSIGNMENT-ALIASES-001`
- requester: `DEVGPT-V9-S05`
- integration owner: `DEVGPT-V9-S06`
- baseline SHA: `af12a0c8851a2e8d20e9e42964d8dacc0266af03`
- affected central file: `cpf-tools/scripts/devgpt-control-v9/build_full_assignment.py`
- status: `REQUESTED_NOT_INTEGRATED`

## Reproduction
The canonical Requirement ledger uses `canonical_requirement_ids`, while the builder alias set accepts singular aliases only. The Scenario ledger uses `linked_requirement_id`, while the builder parent alias set does not include it. The builder therefore cannot produce FULL_ASSIGNMENT from the current canonical columns.

## Requested change
Add `canonical_requirement_ids` to the canonical requirement alias list and `linked_requirement_id` to the scenario parent requirement alias list. Add regression fixtures using the current master headers and assert 95/1,159/1,368/19 for S05.

## S05 continuity
S05 did not modify the central file. A read-only alternative parser extracted all assigned IDs and validation equations are recorded in `sessions/DEVGPT-V9-S05/evidence/ASSIGNMENT_VALIDATION.json`.

## Integration completion criteria
S06 implements and pushes the fix, reruns FULL_ASSIGNMENT on the then-latest master, and S05/Integration Owner reruns consumer/regression count comparison with zero missing/duplicate/unassigned IDs.
