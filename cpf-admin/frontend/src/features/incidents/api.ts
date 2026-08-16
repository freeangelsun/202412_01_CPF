import {
  admIncidentAcknowledge,
  admIncidentCreateMaintenance,
  admIncidentCreatePolicy,
  admIncidentEscalate,
  admIncidentFindIncident,
  admIncidentFindIncidents,
  admIncidentFindMaintenance,
  admIncidentFindPolicies,
  admIncidentFindTimeline,
  admIncidentReopen,
  admIncidentResolve,
  admIncidentUpdateMaintenance,
  admIncidentUpdatePolicy,
  type AdmIncidentCreateMaintenanceBody,
  type AdmIncidentCreatePolicyBody,
  type AdmIncidentUpdateMaintenanceBody,
  type AdmIncidentUpdatePolicyBody,
} from '../../generated/cpf-api'

export interface Page<T> { content: T[]; page: number; size: number; totalElements: number; totalPages: number }
export interface IncidentPolicy {
  policyId: number; policyCode: string; eventType: string; eventSubType?: string; severity: string;
  thresholdCount: number; windowSeconds: number; escalationMinutes: number; receiverGroup: string;
  useYn: 'Y'|'N'; version: number; updatedBy: string; updatedAt: string
}
export interface Incident {
  incidentId: number; policyId: number; policyCode: string; severity: string; status: string;
  title: string; summary?: string; sourceType: string; sourceId: string; correlationId?: string;
  transactionId?: string; occurrenceCount: number; escalationLevel: number; firstOccurredAt: string;
  lastOccurredAt: string; acknowledgedAt?: string; resolvedAt?: string; ownerId?: string; version: number
}
export interface Timeline { timelineId:number; incidentId:number; actionType:string; beforeStatus?:string; afterStatus:string; reason:string; approvalRequestId?:string; actorId:string; createdAt:string }
export interface MaintenanceWindow { maintenanceId:number; maintenanceCode:string; targetType:string; targetId:string; startsAt:string; endsAt:string; useYn:'Y'|'N'; version:number; updatedBy:string; updatedAt:string }
export interface MutationProof { expectedVersion:number; reason:string; approvalRequestId:string; idempotencyKey:string }

export const findPolicies = (page=0,size=50) => admIncidentFindPolicies<Page<IncidentPolicy>>({ query: { page, size } })
export const savePolicy = (body: AdmIncidentCreatePolicyBody | AdmIncidentUpdatePolicyBody, policyId?:number) => policyId
  ? admIncidentUpdatePolicy<IncidentPolicy>({ path: { policyId }, data: body as AdmIncidentUpdatePolicyBody })
  : admIncidentCreatePolicy<IncidentPolicy>({ data: body as AdmIncidentCreatePolicyBody })
export const findIncidents = (status:string,page=0,size=50) => admIncidentFindIncidents<Page<Incident>>({ query: { status, page, size } })
export const findIncident = (id:number) => admIncidentFindIncident<Incident>({ path: { incidentId: id } })
export const findTimeline = (id:number) => admIncidentFindTimeline<Timeline[]>({ path: { incidentId: id } })
export const transitionIncident = (id:number,action:'acknowledge'|'resolve'|'reopen'|'escalate',body:MutationProof) => {
  const options = { path: { incidentId: id }, data: body }
  switch(action){
    case 'acknowledge': return admIncidentAcknowledge<Incident>(options)
    case 'resolve': return admIncidentResolve<Incident>(options)
    case 'reopen': return admIncidentReopen<Incident>(options)
    case 'escalate': return admIncidentEscalate<Incident>(options)
  }
}
export const findMaintenance = (page=0,size=50) => admIncidentFindMaintenance<Page<MaintenanceWindow>>({ query: { page, size } })
export const saveMaintenance = (body:AdmIncidentCreateMaintenanceBody | AdmIncidentUpdateMaintenanceBody,id?:number) => id
  ? admIncidentUpdateMaintenance<MaintenanceWindow>({ path: { maintenanceId: id }, data: body as AdmIncidentUpdateMaintenanceBody })
  : admIncidentCreateMaintenance<MaintenanceWindow>({ data: body as AdmIncidentCreateMaintenanceBody })
