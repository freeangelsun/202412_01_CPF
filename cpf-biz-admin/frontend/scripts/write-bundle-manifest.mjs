import crypto from "node:crypto";
import fs from "node:fs";
import path from "node:path";
const root=process.cwd(), bundleRoot=path.resolve(root,"../build/generated/frontend/static/bza");
if (!fs.existsSync(path.join(bundleRoot,"index.html")) || !fs.existsSync(path.join(bundleRoot,".vite/manifest.json"))) {
  throw new Error(`BZA production bundle이 없습니다: ${bundleRoot}`);
}
const sha=v=>crypto.createHash("sha256").update(v).digest("hex");
const files=[];
(function walk(dir){ for(const e of fs.readdirSync(dir,{withFileTypes:true})){ const p=path.join(dir,e.name); if(e.isDirectory()) walk(p); else if(e.name!=='.cpf-bundle-manifest.json') files.push({path:path.relative(bundleRoot,p).replaceAll('\\','/'),sha256:sha(fs.readFileSync(p)),bytes:fs.statSync(p).size}); }})(bundleRoot);
files.sort((a,b)=>a.path.localeCompare(b.path));
if(!files.length) throw new Error("production bundle empty");
const manifest={schemaVersion:1,identityPolicy:"SOURCE_SHA_IN_RELEASE_EVIDENCE",files,bundleSha256:sha(files.map(f=>`${f.path}:${f.sha256}:${f.bytes}`).join('\n')),sanitized:true};
fs.writeFileSync(path.join(bundleRoot,'.cpf-bundle-manifest.json'),JSON.stringify(manifest,null,2)+'\n');
console.log(`[CPF][FRONTEND][PASS] bundle files=${files.length} sha=${manifest.bundleSha256}`);
