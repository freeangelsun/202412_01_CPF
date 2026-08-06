import * as api from "../../generated/integrationClosureApi";
export const integrationClosureMethods = {
 async loadIntegrationTimeHealth(this:any,zone="Asia/Seoul"){this.integrationTimeHealth=await api.admIntegrationTimeHealth(zone);return this.integrationTimeHealth;},
 async validateIntegrationDataQuality(this:any,recordId:string,record:Record<string,unknown>){this.integrationDataQualityResult=await api.admIntegrationDataQualityValidate(recordId,record);return this.integrationDataQualityResult;},
 async correctIntegrationQuarantine(this:any,id:string,expectedVersion:number,reason:string,approved:boolean,corrected:Record<string,unknown>){if(!reason.trim())throw new Error("reason is required");return api.admIntegrationDataQualityCorrect(id,expectedVersion,reason,approved,corrected);},
 async replayIntegrationDataQuality(this:any,id:string,reason:string){if(!reason.trim())throw new Error("reason is required");return api.admIntegrationDataQualityReplay(id,reason);},
 async loadIntegrationWebhookDlq(this:any,limit=100){this.integrationWebhookDlq=await api.admIntegrationWebhookDlq(limit);return this.integrationWebhookDlq;},
 async replayIntegrationWebhook(this:any,id:string,expectedVersion:number,reason:string){if(!reason.trim())throw new Error("reason is required");return api.admIntegrationWebhookReplay(id,expectedVersion,reason);}
};
