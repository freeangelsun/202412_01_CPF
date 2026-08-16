import { integrationClosureApi as api } from "../../features/integration-closure/integrationClosureApi";
export const integrationClosureMethods = {
 async loadIntegrationTimeHealth(this:any,zone="Asia/Seoul"){this.integrationTimeHealth=await api.timeHealth(zone);return this.integrationTimeHealth;},
 async validateIntegrationDataQuality(this:any,recordId:string,record:Record<string,unknown>){this.integrationDataQualityResult=await api.validate(recordId,record);return this.integrationDataQualityResult;},
 async correctIntegrationQuarantine(this:any,id:string,expectedVersion:number,reason:string,approved:boolean,corrected:Record<string,unknown>){if(!reason.trim())throw new Error("reason is required");return api.requestCorrectionApproval(id,{expectedVersion,idempotencyKey:`adm-iq-${id}-${expectedVersion}`,reason,corrected});},
 async replayIntegrationDataQuality(this:any,id:string,reason:string){if(!reason.trim())throw new Error("reason is required");return api.replayQuality(id,{expectedVersion:0,idempotencyKey:`adm-iq-replay-${id}`,reason});},
 async loadIntegrationWebhookDlq(this:any,limit=100){this.integrationWebhookDlq=await api.webhookDlq(limit);return this.integrationWebhookDlq;},
 async replayIntegrationWebhook(this:any,id:string,expectedVersion:number,reason:string){if(!reason.trim())throw new Error("reason is required");return api.replayWebhook(id,expectedVersion,reason);}
};
