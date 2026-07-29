SELECT policy_code AS policyCode, policy_version AS policyVersion,
       policy_name AS policyName, business_domain AS businessDomain,
       approval_type AS approvalType, effective_from AS effectiveFrom,
       effective_to AS effectiveTo, enabled_yn AS enabledYn,
       self_approval_allowed_yn AS selfApprovalAllowedYn, description,
       created_at AS createdAt, updated_at AS updatedAt
  FROM bza_approval_policy
 WHERE (:businessDomain IS NULL OR business_domain = :businessDomain)
   AND (:approvalType IS NULL OR approval_type = :approvalType)
 ORDER BY policy_code, policy_version DESC
