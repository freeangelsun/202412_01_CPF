import { ref } from 'vue'
import { approvalApi } from '../api/approvalApi'
import type { ApprovalDecisionRequest, ApprovalDetailPayload, ApprovalInboxPayload } from '../model/approvalModel'

export function useApprovals() {
  const inbox = ref<ApprovalInboxPayload>()
  const detail = ref<ApprovalDetailPayload>()
  const error = ref('')
  const loading = ref(false)

  async function loadInbox() {
    await execute(async () => { inbox.value = await approvalApi.inbox() })
  }

  async function loadDetail(approvalId: string) {
    await execute(async () => { detail.value = await approvalApi.detail(approvalId) })
  }

  async function decide(approvalId: string, request: ApprovalDecisionRequest) {
    await execute(async () => {
      detail.value = await approvalApi.decide(approvalId, request) as ApprovalDetailPayload
      inbox.value = await approvalApi.inbox()
    })
  }

  async function execute(action: () => Promise<void>) {
    loading.value = true
    error.value = ''
    try { await action() }
    catch (cause) { error.value = cause instanceof Error ? cause.message : String(cause) }
    finally { loading.value = false }
  }

  return { inbox, detail, error, loading, loadInbox, loadDetail, decide }
}
