import fs from 'node:fs';import path from 'node:path';
const root=process.cwd(), marker=path.join(root,'src/generated/.cpf-openapi-source.json');
if(!fs.existsSync(marker))throw new Error('Orval source marker가 없습니다. exact-SHA OpenAPI로 npm run generate:api를 실행하십시오.');
const m=JSON.parse(fs.readFileSync(marker,'utf8'));if(!/^[0-9a-f]{40}$/.test(m.sourceSha||''))throw new Error('OpenAPI sourceSha가 유효하지 않습니다.');
if(m.sourceSha!==process.env.CPF_SOURCE_SHA)throw new Error(`OpenAPI SHA 불일치: ${m.sourceSha} != ${process.env.CPF_SOURCE_SHA}`);
