SELECT job_pack_id, owner_domain, artifact_coordinate, artifact_version, artifact_checksum,
       signature_present_yn, platform_range, last_registered_at
FROM bat_job_pack
ORDER BY owner_domain, job_pack_id
