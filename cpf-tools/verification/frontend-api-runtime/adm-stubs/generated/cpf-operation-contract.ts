export type CpfOperationId=string;
export const cpfOperationDescriptors=[{operationId:"admOp",method:"POST",template:"/adm/api/runtime-control/status"},{operationId:"admGet",method:"GET",template:"/adm/api/runtime-control/status"}];
export function resolveCpfOperation(method:string,path:string){return {operationId:"adm-"+method.toLowerCase(),method,template:path};}
