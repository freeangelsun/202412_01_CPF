-- bounded runtime checks; bind values at application layer.
SELECT consumer_identity,message_id,inbox_status,lease_version,updated_at FROM cpf_broker_inbox WHERE consumer_identity = ? AND message_id = ?;
SELECT inbox_id,consumer_identity,message_id,inbox_status,updated_at FROM cpf_broker_inbox WHERE inbox_status <> 'RECEIVED' AND updated_at < ? ORDER BY inbox_id FETCH FIRST ? ROWS ONLY;
