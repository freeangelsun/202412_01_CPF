INSERT INTO bat_execution (
    job_id,
    schedule_id,
    job_parameters,
    execution_status,
    business_date,
    requested_by,
    created_by,
    definition_version,
    definition_checksum
) VALUES (
    ?, ?, '{}', 'READY', ?, 'SCHEDULER', 'SCHEDULER', ?, ?
)
