SELECT runtime_role, actual_state, COUNT(*) count
FROM bat_runtime_instance
GROUP BY runtime_role, actual_state
ORDER BY runtime_role, actual_state
