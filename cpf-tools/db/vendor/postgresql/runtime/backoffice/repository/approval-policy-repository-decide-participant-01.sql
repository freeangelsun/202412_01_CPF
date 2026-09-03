UPDATE MBW_APPROVAL_PARTICIPANT
   SET decision_status=:decisionStatus, idempotency_key=:idempotencyKey,
       decision_comment=:comment, decided_at=CURRENT_TIMESTAMP(3), updated_by=:operatorId
 WHERE approval_participant_id=:participantId AND decision_status='WAITING'
