# ICR-V9-S02-MGMT-0002

- requester_session: `DEVGPT-V9-S02`
- target_integration_owner: `V9 Management/Assignment Owner`
- priority: `P1`
- baseline_sha: `fc207ac5560da59f352ee0c5f83199177f2987b4`
- exact_path: `cpf-docs/work/current/CPF_DEVGPT_CONTROL_V9/` central assignment/state only
- related_work_items: all 141 S02 Work Items
- related_requirements: all 3136 S02 CPF-FR
- related_scenarios: all 4564 S02 CPF-SC
- root_cause: Campaign Assignment/development request_id was not present and FULL_ASSIGNMENT validation remained NOT_EXECUTED at baseline.
- current_behavior: local request id and deterministic canonical scope reconstruction are required.
- expected_behavior: official request_id, unique primary Work Item assignment and resolution of 80 semantic ties.
- required_tests: `validate-development-management.ps1 -RequireFullAssignment` and equality check against this submission.
- success_criteria: counts remain 141/28/3136/4564/19; missing/duplicate/unassigned/orphan zero.
- failure_criteria: scope shrink, representative-only IDs or duplicate Primary assignment.
- evidence: `results/SCOPE_*.csv`, `evidence/REQUIREMENT_MAPPING_MANUAL_REVIEW.csv`.
