import {
  approvalInbox,
  approvalParticipantDecision,
  approvalSubmissionDetail,
} from '../../../generated/backoffice-api'
import { validateApprovalDecision, type ApprovalDecisionRequest } from '../model/approvalModel'

export const approvalApi = {
  inbox: () => approvalInbox(),
  detail: (approvalId: string) => approvalSubmissionDetail(approvalId),
  decide: (approvalId: string, body: ApprovalDecisionRequest) =>
    approvalParticipantDecision(approvalId, { body: validateApprovalDecision(body) }),
}
