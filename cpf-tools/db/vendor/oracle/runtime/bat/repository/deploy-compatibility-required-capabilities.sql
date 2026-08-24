SELECT required_capability
FROM BAT_VERSION_COMPATIBILITY
WHERE environment_id IN (?, '*')
  AND (provider_coordinate = ? OR provider_coordinate = '*')
  AND enabled_yn = 'Y'
