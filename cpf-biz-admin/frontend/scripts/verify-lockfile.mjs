import fs from "node:fs";
const pkg = JSON.parse(fs.readFileSync("package.json", "utf8"));
const lock = JSON.parse(fs.readFileSync("package-lock.json", "utf8"));
const root = lock.packages?.[""] || {};
const failures = [];
for (const section of ["dependencies", "devDependencies"]) {
  const expected = pkg[section] || {};
  const actual = root[section] || {};
  if (JSON.stringify(expected) !== JSON.stringify(actual)) failures.push(`${section} root mismatch`);
  for (const name of Object.keys(expected)) if (!lock.packages?.[`node_modules/${name}`]) failures.push(`missing package entry: ${name}`);
}
if (failures.length) { console.error(failures.join("\n")); process.exit(1); }
console.log("[CPF][FRONTEND][PASS] package-lock exact dependency contract");
