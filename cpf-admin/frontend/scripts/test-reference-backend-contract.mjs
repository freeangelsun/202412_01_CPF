import assert from "node:assert/strict";
import fs from "node:fs";
import path from "node:path";

const repoRoot = path.resolve(process.cwd(), "../..");
const read = (name) => fs.readFileSync(path.join(repoRoot, `cpf-admin/src/main/java/com/cpf/admin/opr/controller/${name}.java`), "utf8");
const code = read("AdmCodeController");
const config = read("AdmConfigController");
const response = read("AdmResponseCodeController");
const test = fs.readFileSync(path.join(repoRoot, "cpf-admin/src/test/java/com/cpf/admin/opr/controller/AdmReferenceControllerAuthenticationTest.java"), "utf8");

for (const [name, source, servicePattern] of [
  ["code", code, /codeCacheService\.(createCode|updateCode|deleteCode)/],
  ["config", config, /configCacheService\.(createConfig|updateConfig|deleteConfig)/],
  ["response", response, /responseCodeCacheService\.(createResponseCode|updateResponseCode|deleteResponseCode)/]
]) {
  assert.doesNotMatch(source, /@RequestParam\([^\n]*requestUser|String requestUser/);
  for (const method of source.matchAll(/public ResponseEntity[\s\S]*?\n    }/g)) {
    const body = method[0];
    if (!servicePattern.test(body)) continue;
    const auth = body.indexOf("requireOperator(servletRequest)");
    const reason = body.indexOf("requireReason(");
    const sideEffect = body.search(servicePattern);
    assert.ok(auth >= 0 && auth < reason && reason < sideEffect, `${name}: auth/reason must precede side effect`);
  }
}
assert.match(code, /request\.setRequestUser\(operator\)/);
assert.match(config, /request\.setRequestUser\(operator\)/);
assert.match(response, /request\.setRequestUser\(operator\)/);
assert.match(config, /"\*\*\*\*\*\*\*\*"/);
assert.doesNotMatch(response, /getMostSpecificCause|detail/);
assert.match(response, /HttpStatus\.SERVICE_UNAVAILABLE/);
assert.match(test, /never\(\)\)\.requireReason/);
assert.match(test, /doesNotContain\("secret-host"\)/);
console.log("[CPF][BACKEND][PASS] reference catalogs auth-before-side-effect, server actor, audit reason, secret masking, 503 boundary");
