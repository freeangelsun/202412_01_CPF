export type CpfOperationId=string;
export const cpfOperationDescriptors=[{operationId:"bzaOp",method:"POST",template:"/bza/api/runtime-control/status"},{operationId:"bzaGet",method:"GET",template:"/bza/api/runtime-control/status"}];
export function resolveCpfOperation(method:string,path:string){return {operationId:"bza-"+method.toLowerCase(),method,template:path};}
