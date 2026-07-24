INSERT INTO cpf_file_transfer_history (
    transfer_id, transaction_global_id, segment_id, endpoint_code, transfer_operation,
    local_path, remote_path, checksum, file_size, duplicate_key, transfer_status,
    result_detail, completed_at, created_by, updated_by
) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'CPF_FILE_TRANSFER', 'CPF_FILE_TRANSFER')
