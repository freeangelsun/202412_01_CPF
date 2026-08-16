INSERT INTO bza_notification (
    recipient_login_id, notification_type, title, message_body,
    reference_type, reference_id, read_yn, use_yn, created_by, updated_by
) VALUES (
    :recipientLoginId, :notificationType, :title, :messageBody,
    :referenceType, :referenceId, 'N', 'Y', :requestUser, :requestUser
)
