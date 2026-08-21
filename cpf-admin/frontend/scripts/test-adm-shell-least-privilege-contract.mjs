import fs from "node:fs";

const text = fs.readFileSync(new URL("../src/features/core/methods.ts", import.meta.url), "utf8");
const loadInitial = text.match(/async loadInitialData\(\)[\s\S]*?async loadRouteData/);
if (!loadInitial) throw new Error("ADM loadInitialData contract missing");
const body = loadInitial[0];
for (const forbidden of ["loadPermissions()", "loadServiceRegistry()", "/adm/api/permissions/"]) {
  if (body.includes(forbidden)) throw new Error(`ADM shell bootstrap requires privileged master API: ${forbidden}`);
}
if (!body.includes("await this.loadMe()")) throw new Error("ADM shell bootstrap must restore server session projection");
console.log("[CPF][FRONTEND][PASS] ADM shell bootstrap uses least-privilege /auth/me projection and lazy feature loading");
