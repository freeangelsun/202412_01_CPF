SELECT owner_instance_id,
       fencing_token,
       lease_until
  FROM bat_scheduler_lease
 WHERE scheduler_key = ?
