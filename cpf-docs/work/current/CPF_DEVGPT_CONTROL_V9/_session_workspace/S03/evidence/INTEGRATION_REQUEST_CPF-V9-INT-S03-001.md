# CPF-V9-INT-S03-001

- Source session: `DEVGPT-V9-S03`
- Target: `V9-INTEGRATION-MANAGEMENT`
- Baseline: `fc207ac5560da59f352ee0c5f83199177f2987b4`
- Status: 요청서 작성 완료 / 통합 미처리

## Reproduction

- Command: `python cpf-tools/scripts/devgpt-control-v9/build_full_assignment.py`
- Exit code: `1`
- Actual error: master column aliases do not match script expectations (`canonical_requirement_ids`, `linked_requirement_id`).
- Additional risk: multi-Canonical gate rows cannot be safely assigned by taking only the first token; all candidates must be scored and duplicate-primary/unassigned checks rerun.

## Requested correction

1. Accept plural/singular requirement aliases explicitly.
2. Accept `linked_requirement_id` for scenario linkage.
3. Expand multi-Canonical candidates before mapping and score all candidates.
4. Re-run full 775 Work Item / 30,558 CPF-FR / 40,763 CPF-SC validation.
5. Publish implementation SHA and S03 regression result.

## S03 alternative verification

Independent reconstruction produced 111 Work Items, 3,523 CPF-FR, 5,479 CPF-SC and 18 Gates with missing=0, duplicate primary=0 and unassigned=0. This does not close the central script defect.
