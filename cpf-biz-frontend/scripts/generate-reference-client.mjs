import fs from 'node:fs'
const spec=JSON.parse(fs.readFileSync(new URL('../openapi/cpf-openapi.json',import.meta.url),'utf8'))
const wanted=[
 'bzaSupportDashboard','bzaBackofficeFindEmployeesPage','bzaBackofficeSaveEmployee',
 'bzaApprovalInbox','bzaApprovalSubmissionDetail','bzaApprovalParticipantDecision',
 'bzaOperationFindRolesPage','bzaOperationFindPermissionsPage'
]
const rows=[]
for(const [path,item] of Object.entries(spec.paths??{})) for(const [method,op] of Object.entries(item??{})) {
 if(op && typeof op==='object' && wanted.includes(op.operationId)) rows.push({operationId:op.operationId,method:method.toUpperCase(),path})
}
const missing=wanted.filter(id=>!rows.some(r=>r.operationId===id)); if(missing.length) throw new Error(`OpenAPI operations missing: ${missing.join(',')}`)
function name(id){return id.replace(/^bza/,'').replace(/^./,x=>x.toLowerCase())}
const lines=[`/* AUTO-GENERATED from openapi/cpf-openapi.json. Do not edit. */`,`import { invokeBza } from '../shared/api/channelHttpClient'`]
for(const r of rows.sort((a,b)=>a.operationId.localeCompare(b.operationId))){
 const params=[...r.path.matchAll(/\{([^}]+)\}/g)].map(x=>x[1])
 const sig=params.map(p=>`${p}: string`).concat(['options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}']).join(', ')
 let path='`'+r.path.replace(/\{([^}]+)\}/g,'${encodeURIComponent($1)}')+'`'
 lines.push(`export async function ${name(r.operationId)}(${sig}) { return invokeBza(${JSON.stringify(r.method)}, ${path}, options) }`)
}
fs.writeFileSync(new URL('../src/generated/bza-api.ts',import.meta.url),lines.join('\n')+'\n')
console.log(`GENERATED_BZA_REFERENCE_CLIENT=PASS operations=${rows.length}`)
