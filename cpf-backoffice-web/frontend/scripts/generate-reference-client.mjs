import fs from 'node:fs'
const spec=JSON.parse(fs.readFileSync(new URL('../openapi/cpf-openapi.json',import.meta.url),'utf8'))
const wanted=[
 'MBW_SUPPORT_DASHBOARD','MBW_BACKOFFICE_FIND_EMPLOYEES_PAGE','MBW_BACKOFFICE_SAVE_EMPLOYEE',
 'MBW_APPROVAL_INBOX','MBW_APPROVAL_SUBMISSION_DETAIL','MBW_APPROVAL_PARTICIPANT_DECISION',
 'MBW_OPERATION_FIND_ROLES_PAGE','MBW_OPERATION_FIND_PERMISSIONS_PAGE'
]
const rows=[]
for(const [path,item] of Object.entries(spec.paths??{})) for(const [method,op] of Object.entries(item??{})) {
 if(op && typeof op==='object' && wanted.includes(op.operationId)) rows.push({operationId:op.operationId,method:method.toUpperCase(),path})
}
const missing=wanted.filter(id=>!rows.some(r=>r.operationId===id)); if(missing.length) throw new Error(`OpenAPI operations missing: ${missing.join(',')}`)
function name(id){const parts=id.replace(/^MBW_/,'').toLowerCase().split('_'); return parts[0]+parts.slice(1).map(p=>p.charAt(0).toUpperCase()+p.slice(1)).join('')}
const lines=[`/* AUTO-GENERATED from openapi/cpf-openapi.json. Do not edit. */`,`import { invokeBackoffice } from '../shared/api/channelHttpClient'`]
for(const r of rows.sort((a,b)=>a.operationId.localeCompare(b.operationId))){
 const params=[...r.path.matchAll(/\{([^}]+)\}/g)].map(x=>x[1])
 const sig=params.map(p=>`${p}: string`).concat(['options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}']).join(', ')
 let path='`'+r.path.replace(/\{([^}]+)\}/g,'${encodeURIComponent($1)}')+'`'
 lines.push(`export async function ${name(r.operationId)}(${sig}) { return invokeBackoffice(${JSON.stringify(r.method)}, ${path}, options) }`)
}
fs.writeFileSync(new URL('../src/generated/backoffice-api.ts',import.meta.url),lines.join('\n')+'\n')
console.log(`GENERATED_MBW_REFERENCE_CLIENT=PASS operations=${rows.length}`)
