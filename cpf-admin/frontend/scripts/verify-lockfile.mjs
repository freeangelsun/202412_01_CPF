import fs from "node:fs";
import path from "node:path";
import crypto from "node:crypto";

const readJson = (file) => JSON.parse(fs.readFileSync(file, "utf8"));
const pkg = readJson("package.json");
const lock = readJson("package-lock.json");
const failures = [];

if (lock.lockfileVersion !== 3) failures.push(`lockfileVersion must be 3, got ${lock.lockfileVersion}`);
if (!lock.packages || typeof lock.packages !== "object") failures.push("package-lock packages map is missing");
const root = lock.packages?.[""] || {};

for (const section of ["dependencies", "devDependencies"]) {
  const expected = pkg[section] || {};
  const actual = root[section] || {};
  if (JSON.stringify(expected) !== JSON.stringify(actual)) failures.push(`${section} root mismatch`);
  for (const [name, declared] of Object.entries(expected)) {
    if (!/^\d+\.\d+\.\d+(?:[-+][0-9A-Za-z.-]+)?$/.test(String(declared))) {
      failures.push(`${section}.${name} must use an exact version, got ${declared}`);
    }
    const entry = lock.packages?.[`node_modules/${name}`];
    if (!entry) failures.push(`missing package entry: ${name}`);
    else if (entry.version !== declared) failures.push(`root version mismatch: ${name} lock=${entry.version} package=${declared}`);
  }
}

const forbiddenProtocols = /^(?:git\+|git:|ssh:|file:|link:|workspace:|https?:\/\/(?!registry\.npmjs\.org\/))/i;
for (const [key, entry] of Object.entries(lock.packages || {})) {
  if (!key) continue;
  const resolved = String(entry.resolved || "");
  if (!entry.version) failures.push(`${key}: missing version`);
  if (!resolved) failures.push(`${key}: missing resolved URL`);
  else if (forbiddenProtocols.test(resolved)) failures.push(`${key}: non-canonical resolved URL ${resolved}`);
  else if (!resolved.startsWith("https://registry.npmjs.org/")) failures.push(`${key}: registry must be registry.npmjs.org`);
  if (!entry.integrity || !/^sha(256|384|512)-/.test(String(entry.integrity))) failures.push(`${key}: missing/invalid integrity`);
}

const canonical = JSON.stringify(lock);
const sha256 = crypto.createHash("sha256").update(canonical).digest("hex");
if (failures.length) {
  console.error(failures.join("\n"));
  process.exit(1);
}
console.log(`[CPF][FRONTEND][PASS] package-lock canonical contract packages=${Object.keys(lock.packages).length - 1} sha256=${sha256}`);
