SELECT COUNT(*)
FROM OPS_SYSTEM_DOMAIN_ACCESS
WHERE caller_system_code = ?
  AND target_system_code = ?
