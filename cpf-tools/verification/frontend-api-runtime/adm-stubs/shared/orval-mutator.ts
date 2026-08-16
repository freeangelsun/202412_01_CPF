export class CpfOrvalError extends Error { constructor(public status:number,message:string,public payload:unknown){super(message);} }
export async function cpfOrvalRequest<T>(config:any):Promise<T>{ return ({ok:true,config} as T); }
