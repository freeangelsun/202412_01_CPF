const assert=require('assert');
global.window={location:{origin:'https://cpf.local'}};
let cookie=''; global.document={get cookie(){return cookie},set cookie(v){cookie=v}};
let calls=[];
global.fetch=async (url,options)=>{calls.push([String(url),options||{}]);return new Response('{}',{status:200,headers:{'Content-Type':'application/json'}})};
async function rejects(fn,part){let e;try{await fn()}catch(x){e=x}assert(e,`expected reject ${part}`);assert(String(e.message).includes(part),`message ${e.message} missing ${part}`)}
const CANONICAL=['X-Transaction-Id','X-Original-System-Code','X-System-Code','X-Caller-System-Code','X-Target-System-Code','X-Target-Operation-Id'];

async function testAdm(apiPath,mutatorPath){
  const api=require(apiPath); const create=api.createAdmHeaders; const raw=api.admRawResponse;
  let h=create({}); assert.equal(h.get('X-Client-Id'),'cpf-adm-ui'); assert.equal(h.get('X-Client-Version'),'1.0.0'); assert(!h.has('Authorization'));
  for(const name of CANONICAL){assert(!h.has(name),`browser must not author ${name}`);assert.throws(()=>create({[name]:'forged'}),new RegExp(name));}
  assert.throws(()=>create({Authorization:'Bearer x'}),/Browser Bearer Token/);
  await rejects(()=>raw('https://evil.example/adm/api/runtime-control/status'),'same-origin');
  await rejects(()=>raw('/adm/api/runtime-control/status','POST',{nested:{operatorId:'evil'}}),'operatorId');
  cookie='XSRF-TOKEN=csrf%20token';calls=[];await raw('/adm/api/runtime-control/status','POST',{safe:'ok'});assert.equal(calls.length,1);
  let [,o]=calls[0];let fh=new Headers(o.headers);assert.equal(fh.get('X-XSRF-TOKEN'),'csrf token');assert.equal(o.credentials,'include');
  const mut=require(mutatorPath);calls=[];await mut.cpfOrvalRequest({url:'/adm/api/runtime-control/status',method:'POST',data:{reason:'safe'}});assert.equal(calls.length,1);
  return 6+CANONICAL.length*2;
}

async function testBackoffice(clientPath){
  const client=require(clientPath); calls=[]; cookie='';
  await client.invokeBackoffice('GET','/api/v1/backoffice/directory',{query:{q:'alpha'}});
  assert.equal(calls.length,1); let [url,o]=calls[0]; assert(url.startsWith('https://cpf.local/')); assert.equal(o.credentials,'include');
  let h=new Headers(o.headers); for(const name of CANONICAL) assert(!h.has(name),`Backoffice browser must not author ${name}`);
  assert(!h.has('Authorization'),'Backoffice browser must not author Authorization');
  calls=[]; cookie='XSRF-TOKEN=csrf%20token'; await client.invokeBackoffice('POST','/api/v1/backoffice/approvals',{body:{reason:'safe'}});
  assert.equal(calls.length,1); [,o]=calls[0]; h=new Headers(o.headers); assert.equal(h.get('X-XSRF-TOKEN'),'csrf token'); assert.equal(o.credentials,'include');
  for(const name of CANONICAL) assert(!h.has(name),`Backoffice browser must not author ${name}`);
  return 8+CANONICAL.length*2;
}
(async()=>{let n=0;n+=await testAdm(process.argv[2],process.argv[3]);n+=await testBackoffice(process.argv[4]);console.log(`PASS surfaces=2 canonicalSystem6=protected checks>=${n}`)})().catch(e=>{console.error(e);process.exit(1)});
