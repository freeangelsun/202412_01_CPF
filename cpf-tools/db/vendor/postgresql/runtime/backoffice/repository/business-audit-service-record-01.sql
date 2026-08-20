INSERT INTO mbw_business_audit(transaction_id,actor_id,action_type,target_type,target_id,reason,before_data,after_data,previous_record_hash,record_hash,created_by,updated_by)
VALUES(:transactionId,:actor,:action,:targetType,:targetId,:reason,:beforeData,:afterData,:previous,:hash,:actor,:actor)
