import assert from "node:assert/strict";
import fs from "node:fs";
import os from "node:os";
import path from "node:path";
import { pathToFileURL } from "node:url";
import { spawnSync } from "node:child_process";

const root = process.cwd();
const source = path.join(root, "src/shared/orval-mutator.ts");
if (!fs.existsSync(source)) throw new Error(`Mutator source missing: ${source}`);
const outDir = fs.mkdtempSync(path.join(os.tmpdir(), "cpf-orval-mutator-"));
const tsc = path.join(root, "node_modules", "typescript", "bin", "tsc");
if (!fs.existsSync(tsc)) throw new Error(`Local TypeScript compiler missing: ${tsc}`);
const compile = spawnSync(process.execPath, [tsc, source, "--target", "ES2022", "--module", "ES2022", "--moduleResolution", "Bundler", "--strict", "--lib", "ES2022,DOM,DOM.Iterable", "--skipLibCheck", "--outDir", outDir], {
  cwd: root,
  encoding: "utf8",
  maxBuffer: 16 * 1024 * 1024,
  env: process.env
});
if (compile.error) {
  throw new Error(`Mutator compile failed to start: ${compile.error.code || compile.error.name}: ${compile.error.message}`);
}
if (compile.status !== 0) {
  throw new Error(`Mutator compile failed: exit=${compile.status} signal=${compile.signal || "none"}\n${compile.stdout || ""}\n${compile.stderr || ""}`);
}
const modulePath = path.join(outDir, "orval-mutator.js");
const moduleSource = fs.readFileSync(modulePath, "utf8");
const esmPath = path.join(outDir, "orval-mutator.mjs");
fs.writeFileSync(esmPath, moduleSource);
const { CpfOrvalError, cpfOrvalRequest } = await import(`${pathToFileURL(esmPath).href}?v=${Date.now()}`);

globalThis.window = { location: { origin: "https://cpf.example" } };
globalThis.document = { cookie: "XSRF-TOKEN=csrf%20token" };
let fetchCalls = [];
globalThis.fetch = async (url, options) => {
  fetchCalls.push({ url: String(url), options });
  return new Response(JSON.stringify({ ok: true }), { status: 200, headers: { "Content-Type": "application/json", "X-Request-Id": "req-1" } });
};
const envelope = await cpfOrvalRequest({
  url: "/adm/api/example?existing=1",
  method: "post",
  params: { limit: 20, ignored: undefined },
  data: { value: 7 }
});
assert.deepEqual(envelope.data, { ok: true });
assert.equal(envelope.status, 200);
assert.equal(envelope.headers.get("X-Request-Id"), "req-1");
assert.equal(fetchCalls.length, 1);
assert.equal(fetchCalls[0].url, "https://cpf.example/adm/api/example?existing=1&limit=20");
const requestHeaders = new Headers(fetchCalls[0].options.headers);
assert.equal(requestHeaders.get("X-XSRF-TOKEN"), "csrf token");
assert.equal(requestHeaders.get("Content-Type"), "application/json");
assert.equal(fetchCalls[0].options.credentials, "include");
assert.equal(fetchCalls[0].options.cache, "no-store");
assert.equal(fetchCalls[0].options.redirect, "error");
assert.equal(fetchCalls[0].options.method, "POST");

for (const [name, config, pattern] of [
  ["cross-origin", { url: "https://evil.example/x", method: "GET" }, /same-origin/],
  ["bearer", { url: "/x", method: "GET", headers: { Authorization: "Bearer bad" } }, /Bearer Token/],
  ["actor-body", { url: "/x", method: "POST", data: { nested: { requestedBy: "spoof" } } }, /actor field/],
  ["actor-query", { url: "/x", method: "GET", params: { operatorId: "spoof" } }, /actor query/],
  ["raw-body", { url: "/x", method: "POST", data: "plain" }, /raw string body/],
  ["blob-body", { url: "/x", method: "POST", data: new Blob(["x"]) }, /Blob body/],
  ["get-body", { url: "/x", method: "GET", data: { bad: true } }, /GET request body/],
  ["unsupported-method", { url: "/x", method: "TRACE" }, /Unsupported CPF generated method/]
]) {
  const before = fetchCalls.length;
  await assert.rejects(() => cpfOrvalRequest(config), pattern, name);
  assert.equal(fetchCalls.length, before, `${name} must fail before fetch`);
}

for (const status of [401, 403, 404, 409, 429, 500, 503]) {
  globalThis.fetch = async () => new Response(JSON.stringify({ message: `status-${status}` }), {
    status,
    headers: { "Content-Type": "application/problem+json" }
  });
  await assert.rejects(
    () => cpfOrvalRequest({ url: "/adm/api/failure", method: "GET" }),
    error => error instanceof CpfOrvalError && error.status === status && error.message === `status-${status}`,
    `status=${status}`
  );
}

globalThis.fetch = async () => new Response(null, { status: 204 });
const noContent = await cpfOrvalRequest({ url: "/adm/api/no-content", method: "DELETE" });
assert.equal(noContent.data, undefined);
assert.equal(noContent.status, 204);

console.log(`[CPF][FRONTEND][PASS] Orval mutator runtime envelope/security/error-statuses=7`);
