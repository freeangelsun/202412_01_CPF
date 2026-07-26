SELECT required_capability
FROM bat_version_compatibility
WHERE environment_id IN (?, '*')
  AND (provider_coordinate = ? OR provider_coordinate = '*')
  AND enabled_yn = 'Y'
