SELECT CASE WHEN control.job_id = ?
                  AND control.fencing_token = ?
                  AND epoch.current_fencing_token = control.fencing_token
                  AND control.control_status NOT IN ('ABANDONED', 'REJECTED')
                 THEN 1 ELSE 0 END
  FROM cpf_batch_execution_control control
  JOIN cpf_batch_execution_epoch epoch ON epoch.job_id = control.job_id
 WHERE control.cpf_execution_id = ?
