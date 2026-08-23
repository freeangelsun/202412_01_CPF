export type CpfOperationId=string;
export const cpfOperationDescriptors=[{operationId:"MBW_RUNTIME_CONTROL_UPDATE",method:"POST",template:"/api/v1/backoffice/runtime-control/status"},{operationId:"MBW_RUNTIME_CONTROL_STATUS",method:"GET",template:"/api/v1/backoffice/runtime-control/status"}];
export function resolveCpfOperation(method:string,path:string){return {operationId:"MBW_RUNTIME_CONTROL_"+method.toUpperCase(),method,template:path};}
