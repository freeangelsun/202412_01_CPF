UPDATE cpf_file_transfer_history
SET transfer_status = ?,
    result_detail = ?,
    completed_at = ?,
    updated_by = 'CPF_FILE_TRANSFER',
    updated_at = CURRENT_TIMESTAMP
WHERE transfer_id = ?
