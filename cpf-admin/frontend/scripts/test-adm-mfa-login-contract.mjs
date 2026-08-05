import fs from "node:fs";
import path from "node:path";
const root = path.resolve(import.meta.dirname, "..");
const app = fs.readFileSync(path.join(root, "src/App.vue"), "utf8");
const state = fs.readFileSync(path.join(root, "src/state/createAdmState.ts"), "utf8");
const auth = fs.readFileSync(path.resolve(root, "../src/main/java/com/cpf/admin/opr/controller/AdmAuthController.java"), "utf8");
const service = fs.readFileSync(path.resolve(root, "../src/main/java/com/cpf/admin/opr/service/AdmSecurityOperationService.java"), "utf8");
for (const [name, ok] of [
  ["MFA input", app.includes('v-model="loginForm.otpCode"')],
  ["MFA state", state.includes('otpCode: ""')],
  ["login enforcement", auth.includes("requireMfaForLogin")],
  ["real secret resolution", service.includes("secretProvider.resolve(reference)")],
  ["TOTP verification precedes enable", service.indexOf("verifyReferencedTotp(string(state, \"SECRET_REF\"), otpCode)") < service.indexOf("SET ENABLED_YN = 'Y'")]
]) { if (!ok) throw new Error(`ADM MFA contract failed: ${name}`); }
console.log("PASS ADM MFA login contract");
