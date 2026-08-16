INSERT INTO bat_job_parameter_definition (
  job_id,definition_version,parameter_name,parameter_type,label_text,description_text,
  required_yn,sensitive_yn,default_value,allowed_values,validation_pattern,min_value,max_value,
  min_length,max_length,reference_type,alias_required_yn,runtime_override_allowed_yn,sort_order
)
VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
