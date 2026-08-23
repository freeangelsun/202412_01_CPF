SELECT s.instance_id
FROM OPS_RUNTIME_INSTANCE_STATE s
JOIN OPS_SERVICE_INSTANCE i ON i.instance_id = s.instance_id
WHERE s.lease_until > ?
  AND i.active_yn = 'Y'
  AND i.service_id = ?
