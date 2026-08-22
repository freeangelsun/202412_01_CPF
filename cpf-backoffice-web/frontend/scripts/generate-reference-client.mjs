import fs from 'node:fs'

const spec = JSON.parse(fs.readFileSync(new URL('../openapi/cpf-openapi.json', import.meta.url), 'utf8'))
const rows = []
const operationIds = new Set()
const functionNames = new Set()

function functionName(operationId) {
  const parts = operationId.replace(/^MBW_/, '').toLowerCase().split('_').filter(Boolean)
  if (!parts.length) throw new Error(`Invalid operationId: ${operationId}`)
  return parts[0] + parts.slice(1).map(part => part.charAt(0).toUpperCase() + part.slice(1)).join('')
}

for (const [path, item] of Object.entries(spec.paths ?? {})) {
  for (const [method, op] of Object.entries(item ?? {})) {
    if (!op || typeof op !== 'object' || !op.operationId) continue
    const operationId = String(op.operationId)
    if (!operationId.startsWith('MBW_')) throw new Error(`Backoffice operationId must start with MBW_: ${operationId}`)
    if (operationIds.has(operationId)) throw new Error(`Duplicate OpenAPI operationId: ${operationId}`)
    const name = functionName(operationId)
    if (functionNames.has(name)) throw new Error(`Generated function-name collision: ${name}`)
    operationIds.add(operationId)
    functionNames.add(name)
    rows.push({ operationId, method: method.toUpperCase(), path, name })
  }
}

if (!rows.length) throw new Error('OpenAPI contains no Backoffice operations')
rows.sort((a, b) => a.operationId.localeCompare(b.operationId))

const lines = [
  `/* AUTO-GENERATED from openapi/cpf-openapi.json. Do not edit. */`,
  `import { invokeBackoffice } from '../shared/api/channelHttpClient'`,
  `export const cpfBackofficeGeneratedOperations = ${JSON.stringify(rows.map(({operationId,method,path,name}) => ({operationId,method,path,name})), null, 2)} as const`,
]

for (const r of rows) {
  const params = [...r.path.matchAll(/\{([^}]+)\}/g)].map(match => match[1])
  const sig = params.map(param => `${param}: string`).concat(['options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}']).join(', ')
  const runtimePath = '`' + r.path.replace(/\{([^}]+)\}/g, '${encodeURIComponent($1)}') + '`'
  lines.push(`export async function ${r.name}(${sig}) { return invokeBackoffice(${JSON.stringify(r.method)}, ${runtimePath}, options) }`)
}

fs.writeFileSync(new URL('../src/generated/backoffice-api.ts', import.meta.url), lines.join('\n') + '\n')
console.log(`GENERATED_MBW_REFERENCE_CLIENT=PASS operations=${rows.length}`)
