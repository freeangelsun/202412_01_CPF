import { readFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { dirname, resolve } from 'node:path';

const here = dirname(fileURLToPath(import.meta.url));
const page = readFileSync(resolve(here, '../src/features/approvals/ApprovalsPage.vue'), 'utf8');
const required = [
  '"admApprovalPolicies"',
  '"admApprovalPolicyDetail"',
  '"admApprovalPolicySave"',
  '"admApprovalRequest"',
  '"admApprovalRequestDetail"',
  '"admApprovalDecision"',
  '"admApprovalExecute"',
  'result.approvalRequestId'
];
for (const token of required) {
  if (!page.includes(token)) throw new Error(`ADM approval consumer contract token missing: ${token}`);
}
if (!/requestId=String\(result\.approvalRequestId\?\?result\.requestId\?\?result\.id/.test(page)) {
  throw new Error('ADM approval request consumer must prefer canonical approvalRequestId response field.');
}
console.log('ADM_APPROVAL_CONSUMER_CONTRACT_PASS');
