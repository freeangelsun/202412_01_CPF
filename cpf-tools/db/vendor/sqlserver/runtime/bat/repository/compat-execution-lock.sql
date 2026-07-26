SELECT *
FROM bat_execution WITH (UPDLOCK, ROWLOCK)
WHERE execution_id = ?
