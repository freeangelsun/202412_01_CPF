export async function cpfOrvalRequest<T>(config: {url:string;method:string;headers?:HeadersInit;data?:unknown;params?:Record<string,unknown>;signal?:AbortSignal}): Promise<T> {
  const url=new URL(config.url,location.origin);Object.entries(config.params||{}).forEach(([k,v])=>{if(v!==undefined&&v!==null)url.searchParams.set(k,String(v))});
  const headers=new Headers(config.headers);if(config.data!==undefined&&!headers.has('Content-Type'))headers.set('Content-Type','application/json');
  const csrf=document.cookie.split(';').map(v=>v.trim()).find(v=>v.startsWith('XSRF-TOKEN='));if(csrf)headers.set('X-XSRF-TOKEN',decodeURIComponent(csrf.split('=',2)[1]||''));
  if(headers.has('Authorization'))throw new Error('Browser Bearer Token 금지');
  const response=await fetch(url,{method:config.method,headers,body:config.data===undefined?undefined:JSON.stringify(config.data),signal:config.signal,credentials:'include',cache:'no-store'});
  if(!response.ok)throw Object.assign(new Error(`HTTP ${response.status}`),{status:response.status,payload:await response.text()});
  if(response.status===204)return undefined as T;return await response.json() as T;
}
export default cpfOrvalRequest;
