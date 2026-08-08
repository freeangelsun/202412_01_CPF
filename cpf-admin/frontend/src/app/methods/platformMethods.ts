import {
  admChannelExportPackage,
  admChannelFindSnapshot,
  admChannelImportPackage,
  admChannelRefreshSnapshot,
  admChannelSave,
} from "../../generated/orval/cpf-api";

function generatedData<T>(response: unknown): T {
  return ((response as { data?: unknown })?.data ?? response) as T;
}

export const platformMethods = {
  async loadChannelPolicy() {
        this.channelSnapshot = generatedData<any>(await admChannelFindSnapshot()) || { version: 0, channels: {}, policies: [] };
      },
  selectChannel(item) {
        this.channelForm = {
          ...this.channelForm,
          ...item,
          reason: "채널 레지스트리 변경",
        };
      },
  async saveChannel() {
        if (!this.requireReason(this.channelForm.reason) || !this.channelForm.channelCode) return;
        const { channelCode, ...body } = this.channelForm;
        this.channelSnapshot = generatedData<any>(await admChannelSave(channelCode, body));
        this.setMessage(`채널을 저장했습니다. code=${channelCode}`);
      },
  async refreshChannelPolicy() {
        if (!this.requireReason(this.channelPolicyForm.reason)) return;
        this.channelSnapshot = generatedData<any>(await admChannelRefreshSnapshot({ reason: this.channelPolicyForm.reason }));
        this.setMessage(`채널 정책 스냅샷을 갱신했습니다. version=${this.channelSnapshot.version}`);
      },
  async exportChannelPolicyPackage() {
        const policyPackage = generatedData<any>(await admChannelExportPackage());
        this.channelPackageText = this.pretty(policyPackage);
        this.setMessage("채널 정책 패키지를 반출했습니다.");
      },
  async importChannelPolicyPackage() {
        if (!this.requireReason(this.channelPolicyForm.reason)) return;
        let policyPackage;
        try {
          policyPackage = JSON.parse(this.channelPackageText);
        } catch (error) {
          this.setMessage("채널 정책 패키지 JSON 형식을 확인하세요.");
          return;
        }
        this.channelSnapshot = generatedData<any>(await admChannelImportPackage({
          policyPackage,
          dryRun: this.channelImportDryRun,
          reason: this.channelPolicyForm.reason,
        }));
        this.setMessage(this.channelImportDryRun ? "채널 정책 반입 사전 검증을 완료했습니다." : "채널 정책을 반입했습니다.");
      },
  async loadServiceRegistry() {
        const search = this.serviceRegistrySearch || {};
        const baseParams = this.buildParams({
          serviceId: search.serviceId,
          endpointCode: search.endpointCode,
          limit: search.limit || 50
        });
        const instanceParams = this.buildParams({
          serviceId: search.serviceId,
          endpointCode: search.endpointCode,
          status: search.instanceStatus,
          limit: search.limit || 50
        });
        const historyParams = this.buildParams({
          serviceId: search.serviceId,
          transactionId: search.transactionId,
          limit: search.limit || 50
        });
        const [services, endpoints, instances, health, routingPolicies, circuits, callHistory] = await Promise.allSettled([
          this.getJson(`/adm/api/service-registry/services?${baseParams.toString()}`),
          this.getJson(`/adm/api/service-registry/endpoints?${baseParams.toString()}`),
          this.getJson(`/adm/api/service-registry/instances?${instanceParams.toString()}`),
          this.getJson(`/adm/api/service-registry/health?${baseParams.toString()}`),
          this.getJson(`/adm/api/service-registry/routing-policies?${baseParams.toString()}`),
          this.getJson(`/adm/api/service-registry/circuit-states?${baseParams.toString()}`),
          this.getJson(`/adm/api/service-registry/call-history?${historyParams.toString()}`)
        ]);
        this.serviceRegistryResult = {
          services: this.settledValue(services),
          endpoints: this.settledValue(endpoints),
          instances: this.settledValue(instances),
          health: this.settledValue(health),
          routingPolicies: this.settledValue(routingPolicies),
          circuits: this.settledValue(circuits),
          callHistory: this.settledValue(callHistory)
        };
        this.setMessage("Service Registry status loaded.");
      },
  async loadReliability() {
        const search = this.reliabilitySearch;
        const [idempotency, outbox, inbox, dlq, fileTransfers, unknownResults, batchJobLogs] = await Promise.allSettled([
          this.getJson(`/adm/api/reliability/idempotency?${this.buildParams({ scope: search.scope, status: search.status, key: search.key, limit: search.limit }).toString()}`),
          this.getJson(`/adm/api/reliability/broker/outbox?${this.buildParams({ status: search.status, transactionId: search.transactionId, topic: search.topic, limit: search.limit }).toString()}`),
          this.getJson(`/adm/api/reliability/broker/inbox?${this.buildParams({ status: search.status, key: search.key, limit: search.limit }).toString()}`),
          this.getJson(`/adm/api/reliability/broker/dlq?${this.buildParams({ status: search.status, transactionId: search.transactionId, topic: search.topic, limit: search.limit }).toString()}`),
          this.getJson(`/adm/api/reliability/file-transfers?${this.buildParams({ status: search.status, transactionId: search.transactionId, endpointCode: search.endpointCode, limit: search.limit }).toString()}`),
          this.getJson(`/adm/api/reliability/unknown-results?${this.buildParams({ type: search.type, status: search.status, transactionId: search.transactionId, limit: search.limit }).toString()}`),
          this.getJson(`/adm/api/reliability/batch-job-logs?${this.buildParams({ businessDate: search.businessDate, jobName: search.jobName, jobInstanceId: search.jobInstanceId, serverInstanceId: search.serverInstanceId, limit: search.limit }).toString()}`)
        ]);
        this.reliabilityResult = {
          idempotency: this.settledValue(idempotency),
          outbox: this.settledValue(outbox),
          inbox: this.settledValue(inbox),
          dlq: this.settledValue(dlq),
          fileTransfers: this.settledValue(fileTransfers),
          unknownResults: this.settledValue(unknownResults),
          batchJobLogs: this.settledValue(batchJobLogs)
        };
        this.setMessage("Reliability 운영 데이터를 조회했습니다.");
      }
} satisfies Record<string, any>;
