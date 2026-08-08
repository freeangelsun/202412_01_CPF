import { admHealthInstanceDetail, admHealthInstanceList } from "../../generated/cpf-api";

export const healthMethods = {
  async loadHealthInstances(this: any) {
    const search = this.healthInstanceSearch || {};
    this.healthInstanceResult = await admHealthInstanceList({
      query: {
        systemId: search.systemId || undefined,
        readiness: search.readiness || undefined,
        includeStale: Boolean(search.includeStale),
        page: Math.max(0, Number(search.page || 0)),
        size: Math.min(200, Math.max(1, Number(search.size || 50))),
      },
    });
  },
  async selectHealthInstance(this: any, item: any) {
    if (!item?.systemId || !item?.instanceId) return;
    this.healthInstanceDetail = await admHealthInstanceDetail({
      path: { systemId: String(item.systemId), instanceId: String(item.instanceId) },
    });
  },
};
