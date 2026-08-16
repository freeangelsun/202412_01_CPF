import { readFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { dirname, resolve } from 'node:path';

const here = dirname(fileURLToPath(import.meta.url));
const page = readFileSync(resolve(here, '../src/features/approvals/ApprovalsPage.vue'), 'utf8');

const requiredOperations = [
  'admApprovalPolicies',
  'admApprovalPolicyDetail',
  'admApprovalPolicySave',
  'admApprovalRequest',
  'admApprovalRequestDetail',
  'admApprovalDecision',
  'admApprovalExecute',
  'admApprovalReconcile'
];

const generatedSource = '} from \"../../generated/orval/cpf-api\";';
const generatedSourceIndex = page.indexOf(generatedSource);
if (generatedSourceIndex < 0) throw new Error('ADM approval consumer must import the canonical Orval generated client.');
const importStart = page.lastIndexOf('import {', generatedSourceIndex);
if (importStart < 0) throw new Error('ADM approval generated import block missing.');
const importBody = page.slice(importStart + 'import {'.length, generatedSourceIndex);
const imported = new Set(importBody.split(',').map((value) => value.trim()).filter(Boolean));
for (const operation of requiredOperations) {
  if (!imported.has(operation)) throw new Error(`ADM approval generated operation import missing: ${operation}`);
  if (!new RegExp(`\\b${operation}\\s*\\(`).test(page)) {
    throw new Error(`ADM approval generated operation consumer missing: ${operation}`);
  }
}
if (!page.includes('result.approvalRequestId')) {
  throw new Error('ADM approval request consumer must read canonical approvalRequestId.');
}
if (!/requestId=String\(result\.approvalRequestId\?\?result\.requestId\?\?result\.id/.test(page)) {
  throw new Error('ADM approval request consumer must prefer canonical approvalRequestId response field.');
}
if (!/admApprovalExecute\([\s\S]*?reason:this\.approvalEngine\.reason/.test(page)) {
  throw new Error('ADM approval execute consumer must carry an operator reason.');
}
if (!/admApprovalReconcile\([\s\S]*?reason:this\.approvalEngine\.reason/.test(page)) {
  throw new Error('ADM approval reconcile consumer must carry an operator reason.');
}
console.log('ADM_APPROVAL_CONSUMER_CONTRACT_PASS');
