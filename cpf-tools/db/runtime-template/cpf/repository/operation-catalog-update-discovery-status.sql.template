UPDATE OPS_OPERATION_CATALOG
SET discovery_status = ?,
    metadata_version = metadata_version + 1,
    updated_by = 'CPF_RUNTIME',
    updated_at = ?
WHERE operation_id = ?
  AND discovery_status <> ?
