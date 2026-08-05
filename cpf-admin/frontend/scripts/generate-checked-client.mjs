import fs from "node:fs";
import path from "node:path";
import { spawnSync } from "node:child_process";
const root = process.cwd();
const orvalCli = path.join(root, "node_modules", "orval", "dist", "bin", "orval.mjs");
if (!fs.existsSync(orvalCli)) throw new Error("Orval local CLI가 없습니다. 먼저 clean npm ci를 실행하세요.");
const generatedDir = path.join(root, "src/generated");
if (fs.existsSync(generatedDir)) fs.rmSync(generatedDir, { recursive: true, force: true });
for (const [name, command, args] of [
  ["enrich-adm-openapi", process.execPath, ["scripts/enrich-adm-openapi-contract.mjs"]],
  ["verify-openapi-schema-refs", process.execPath, ["scripts/verify-openapi-schema-refs.mjs"]],
  ["orval", process.execPath, [orvalCli, "--config", "orval.config.ts"]],
  ["sync-pre-runtime-coverage", process.execPath, ["scripts/sync-pre-runtime-orval-coverage.mjs"]],
  ["sync-adm-cache", process.execPath, ["scripts/sync-adm-cache-generated-client.mjs"]],
  ["sync-adm-reference", process.execPath, ["scripts/sync-adm-reference-generated-client.mjs"]],
  ["sync-adm-message", process.execPath, ["scripts/sync-adm-message-generated-client.mjs"]],
  ["sync-adm-notification", process.execPath, ["scripts/sync-adm-notification-generated-client.mjs"]],
  ["normalize-mutation-types", process.execPath, ["scripts/normalize-orval-mutation-types.mjs"]],
  ["normalize-request-options", process.execPath, ["scripts/normalize-orval-request-options.mjs"]],
  ["normalize-generated-whitespace", process.execPath, ["scripts/normalize-generated-whitespace.mjs"]],
  ["operation-contract", process.execPath, ["scripts/write-operation-contract.mjs"]],
  ["route-operation-contract", process.execPath, ["scripts/write-route-operation-contract.mjs"]],
  ["marker", process.execPath, ["scripts/write-generated-marker.mjs"]],
  ["verify-operation-contract", process.execPath, ["scripts/verify-orval-operation-contract.mjs"]],
  ["verify-request-boundary", process.execPath, ["scripts/verify-orval-request-boundary.mjs"]],
  ["verify", process.execPath, ["scripts/verify-generated-client.mjs"]]
]) {
  const result = spawnSync(command, args, { cwd: root, stdio: "inherit", env: process.env });
  if (result.error) throw new Error(`${name} failed to start: ${result.error.message}`);
  if (result.status !== 0) throw new Error(`${name} failed: exit=${result.status}`);
}
