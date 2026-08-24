SELECT owner_instance_id,
       fencing_token,
       lease_until
  FROM BAT_SCHEDULER_LEASE
 WHERE scheduler_key = ?
