export class CpfApiError extends Error {
  status: number; payload: any;
  constructor(status:number,message:string,payload:any){super(message);this.name="CpfApiError";this.status=status;this.payload=payload;}
}
export async function admApi<T=any>(url:string,options:RequestInit={}):Promise<T>{
  const token=localStorage.getItem("admAccessToken")||"";const headers=new Headers(options.headers||{});
  if(!headers.has("Content-Type")&&options.body)headers.set("Content-Type","application/json");if(token)headers.set("Authorization",`Bearer ${token}`);
  if(!headers.has("X-Transaction-Id"))headers.set("X-Transaction-Id",`OADM-UI-${Date.now()}`);
  const response=await fetch(url,{...options,headers});const text=await response.text();let payload:any=text;
  try{payload=text?JSON.parse(text):undefined;}catch{/* plain response */}
  if(!response.ok)throw new CpfApiError(response.status,payload?.message||text||`HTTP ${response.status}`,payload);
  return payload as T;
}
export const cpfApi=admApi;
