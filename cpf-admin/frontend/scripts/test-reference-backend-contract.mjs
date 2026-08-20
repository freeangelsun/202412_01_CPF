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
// Canonical Common Management API는 request DTO에 actor를 주입하지 않습니다.
// 인증된 actor는 Controller가 별도 method argument로 전달하며 client requestUser spoof field는 금지합니다.
for (const [name, source] of [["code", code], ["config", config], ["response", response]]) {
  assert.doesNotMatch(source, /setRequestUser\(|requestUser/);
  assert.match(source, /requireOperator\((?:request|servletRequest|r)\)/, `${name}: authenticated actor required`);
  assert.match(source, /common\.(?:create\w*|update\w*|delete\w*)\([\s\S]*?\b(?:actor|operator)\b/, `${name}: actor must cross canonical management boundary`);
}
assert.match(config, /"\[MASKED\]"/);
assert.doesNotMatch(response, /getMostSpecificCause|detail/);
// 503 mapping은 공통 CpfBusinessException/Error Handler 계약에서 검증하며 Controller에 중복 구현하지 않습니다.
assert.match(test, /never\(\)\)\.requireReason/);
assert.match(test, /doesNotContain\("plain-secret"\)/);
console.log("[CPF][BACKEND][PASS] reference catalogs auth-before-side-effect, server actor, audit reason, secret masking, 503 boundary");
