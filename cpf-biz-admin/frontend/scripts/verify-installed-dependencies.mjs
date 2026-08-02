import fs from "node:fs";
import path from "node:path";
import crypto from "node:crypto";
import { spawnSync } from "node:child_process";

const readJson = (file) => JSON.parse(fs.readFileSync(file, "utf8"));
const lock = readJson("package-lock.json");
const failures = [];
const forbiddenLicense = /(?:^|[ (])(?:AGPL|GPL|SSPL|BUSL|Commons-Clause)(?:[- )]|$)/i;
const allowedMissingLicense = new Set();
const licenseText = value => {
  if (typeof value === "string") return value.trim();
  if (Array.isArray(value)) return value.map(item => licenseText(item?.type || item)).filter(Boolean).join(" OR ");
  if (value && typeof value === "object" && typeof value.type === "string") return value.type.trim();
  return "";
};

for (const [lockPath, entry] of Object.entries(lock.packages || {})) {
  if (!lockPath) continue;
  const installed = path.join(process.cwd(), lockPath, "package.json");
  if (!fs.existsSync(installed)) {
    if (!entry.optional) failures.push(`${lockPath}: package.json not installed`);
    continue;
  }
  const metadata = readJson(installed);
  if (metadata.version !== entry.version) failures.push(`${lockPath}: installed=${metadata.version} lock=${entry.version}`);
  const installedLicense = licenseText(metadata.license) || licenseText(metadata.licenses);
  const lockedLicense = licenseText(entry.license);
  const license = [installedLicense, lockedLicense].filter(Boolean).join(" OR ");
  if (!license && !allowedMissingLicense.has(metadata.name)) failures.push(`${lockPath}: license metadata missing`);
  if (forbiddenLicense.test(license)) failures.push(`${lockPath}: forbidden license ${license}`);
}

if (failures.length) {
  console.error(failures.join("\n"));
  process.exit(1);
}

fs.mkdirSync("build/reports", { recursive: true });
const npmCli = process.env.npm_execpath;
if (!npmCli || !fs.existsSync(npmCli)) {
  console.error("Pinned npm lifecycle is required. Run this check with `corepack npm run verify:installed`.");
  process.exit(1);
}
const result = spawnSync(process.execPath, [npmCli, "sbom", "--sbom-format", "cyclonedx", "--omit", "optional"], {
  cwd: process.cwd(), encoding: "utf8", maxBuffer: 64 * 1024 * 1024,
});
if (result.status !== 0 || !result.stdout.trim()) {
  console.error(result.stderr || "npm sbom failed");
  process.exit(result.status || 1);
}
const sbomPath = "build/reports/cpf-frontend-sbom.cdx.json";
fs.writeFileSync(sbomPath, result.stdout);
const hash = crypto.createHash("sha256").update(result.stdout).digest("hex");
fs.writeFileSync(`${sbomPath}.sha256`, `${hash}  ${path.basename(sbomPath)}\n`);
console.log(`[CPF][FRONTEND][PASS] installed dependency/license/SBOM contract sha256=${hash}`);
