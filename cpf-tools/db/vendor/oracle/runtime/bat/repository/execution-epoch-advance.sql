UPDATE cpf_batch_execution_epoch
   SET current_fencing_token = ?,
       epoch_version = epoch_version + 1,
       updated_at = CURRENT_TIMESTAMP(6)
 WHERE job_id = ?
   AND current_fencing_token < ?
