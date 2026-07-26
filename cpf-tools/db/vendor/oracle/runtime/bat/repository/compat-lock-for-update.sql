SELECT *
FROM bat_lock
WHERE lock_key = ?
FOR UPDATE
