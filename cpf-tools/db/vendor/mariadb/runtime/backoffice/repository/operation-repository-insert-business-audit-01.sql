INSERT INTO mbw_business_audit(transaction_id,actor_id,action_type,target_type,target_id,reason,before_data,after_data,created_by,updated_by)
VALUES(:transactionId,:actorId,:actionType,:targetType,:targetId,:reason,:beforeData,:afterData,:actorId,:actorId)
