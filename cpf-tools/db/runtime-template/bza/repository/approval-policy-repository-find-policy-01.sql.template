SELECT policy_code AS policyCode, policy_version AS policyVersion,
       policy_name AS policyName, business_domain AS businessDomain,
       approval_type AS approvalType, effective_from AS effectiveFrom,
       effective_to AS effectiveTo, enabled_yn AS enabledYn,
       self_approval_allowed_yn AS selfApprovalAllowedYn, description
  FROM bza_approval_policy
 WHERE policy_code = :policyCode AND policy_version = :policyVersion
