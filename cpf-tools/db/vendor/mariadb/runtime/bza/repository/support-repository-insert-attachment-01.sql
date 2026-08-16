INSERT INTO bza_attachment (
    attachment_group_id, original_file_name, stored_file_name, storage_key,
    content_type, file_size, checksum_sha256, scan_status, data_classification, quarantine_yn, use_yn,
    created_by, updated_by
) VALUES (
    :attachmentGroupId, :originalFileName, :storedFileName, :storageKey,
    :contentType, :fileSize, :checksumSha256, :scanStatus, :dataClassification, :quarantineYn, 'Y',
    :requestUser, :requestUser
)
