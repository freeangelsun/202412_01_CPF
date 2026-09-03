UPDATE MBW_APPROVAL_POLICY
   SET policy_name=:policyName, business_domain=:businessDomain,
       approval_type=:approvalType, effective_from=:effectiveFrom,
       effective_to=:effectiveTo, enabled_yn=:enabledYn,
       self_approval_allowed_yn=:selfApprovalAllowedYn,
       description=:description, updated_by=:operatorId
 WHERE policy_code=:policyCode AND policy_version=:policyVersion
