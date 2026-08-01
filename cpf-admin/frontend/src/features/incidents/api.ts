import { admMutation, admQuery } from '../../shared/cpfApi'

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

export const findPolicies = (page=0,size=50) => admQuery<Page<IncidentPolicy>>('/adm/api/incidents/policies',{page,size})
export const savePolicy = (body: Record<string,unknown>, policyId?:number) => policyId
  ? admMutation<IncidentPolicy>(`/adm/api/incidents/policies/${policyId}`,'PUT',body)
  : admMutation<IncidentPolicy>('/adm/api/incidents/policies','POST',body)
export const findIncidents = (status:string,page=0,size=50) => admQuery<Page<Incident>>('/adm/api/incidents',{status,page,size})
export const findIncident = (id:number) => admQuery<Incident>(`/adm/api/incidents/${id}`)
export const findTimeline = (id:number) => admQuery<Timeline[]>(`/adm/api/incidents/${id}/timeline`)
export const transitionIncident = (id:number,action:'acknowledge'|'resolve'|'reopen'|'escalate',body:MutationProof) => {
  switch(action){
    case 'acknowledge': return admMutation<Incident>(`/adm/api/incidents/${id}/acknowledge`,'POST',body)
    case 'resolve': return admMutation<Incident>(`/adm/api/incidents/${id}/resolve`,'POST',body)
    case 'reopen': return admMutation<Incident>(`/adm/api/incidents/${id}/reopen`,'POST',body)
    case 'escalate': return admMutation<Incident>(`/adm/api/incidents/${id}/escalate`,'POST',body)
  }
}
export const findMaintenance = (page=0,size=50) => admQuery<Page<MaintenanceWindow>>('/adm/api/incidents/maintenance-windows',{page,size})
export const saveMaintenance = (body:Record<string,unknown>,id?:number) => id
  ? admMutation<MaintenanceWindow>(`/adm/api/incidents/maintenance-windows/${id}`,'PUT',body)
  : admMutation<MaintenanceWindow>('/adm/api/incidents/maintenance-windows','POST',body)
