import fs from "node:fs";
import os from "node:os";
import path from "node:path";
import crypto from "node:crypto";
import { spawnSync } from "node:child_process";

const root = process.cwd();
const files = [
  "openapi/cpf-openapi.json",
  "src/generated/orval/cpf-api.ts",
  "scripts/normalize-orval-mutation-types.mjs",
  "scripts/normalize-generated-whitespace.mjs",
  "scripts/sync-adm-message-generated-client.mjs",
  "scripts/sync-adm-notification-generated-client.mjs",
  "scripts/sync-pre-runtime-orval-coverage.mjs",
  "scripts/verify-orval-operation-contract.mjs",
  "src/generated/orval/model/admReliabilityActionRequest.ts"
];
for (const file of files) if (!fs.existsSync(path.join(root, file))) throw new Error(`fixture source missing: ${file}`);

function sha(value) {
  return crypto.createHash("sha256").update(fs.readFileSync(value)).digest("hex");
}
function fixture(name) {
  const temp = fs.mkdtempSync(path.join(os.tmpdir(), `cpf-orval-${name}-`));
  for (const file of files) {
    const source = path.join(root, file);
    const target = path.join(temp, file);
    fs.mkdirSync(path.dirname(target), { recursive: true });
    fs.copyFileSync(source, target);
  }
  return temp;
}
function run(temp, script, expectSuccess) {
  const result = spawnSync(process.execPath, [path.join(temp, script)], { cwd: temp, encoding: "utf8" });
  if ((result.status === 0) !== expectSuccess) {
    throw new Error(`${script} expectedSuccess=${expectSuccess} exit=${result.status}\nstdout=${result.stdout}\nstderr=${result.stderr}`);
  }
  return result;
}
function mutateClient(temp, mutate) {
  const file = path.join(temp, "src/generated/orval/cpf-api.ts");
  const original = fs.readFileSync(file, "utf8");
  const changed = mutate(original);
  if (changed === original) throw new Error("fixture mutation did not change generated client");
  fs.writeFileSync(file, changed);
}

{
  const temp = fixture("idempotent");
  run(temp, "scripts/sync-adm-message-generated-client.mjs", true);
  run(temp, "scripts/sync-adm-notification-generated-client.mjs", true);
  run(temp, "scripts/sync-pre-runtime-orval-coverage.mjs", true);
  run(temp, "scripts/normalize-orval-mutation-types.mjs", true);
  run(temp, "scripts/normalize-generated-whitespace.mjs", true);
  const first = sha(path.join(temp, "src/generated/orval/cpf-api.ts"));
  run(temp, "scripts/sync-adm-message-generated-client.mjs", true);
  run(temp, "scripts/sync-adm-notification-generated-client.mjs", true);
  run(temp, "scripts/sync-pre-runtime-orval-coverage.mjs", true);
  run(temp, "scripts/normalize-orval-mutation-types.mjs", true);
  run(temp, "scripts/normalize-generated-whitespace.mjs", true);
  const second = sha(path.join(temp, "src/generated/orval/cpf-api.ts"));
  if (first !== second) throw new Error(`generated client synchronization is not idempotent: ${first} != ${second}`);
  run(temp, "scripts/verify-orval-operation-contract.mjs", true);
  fs.rmSync(temp, { recursive: true, force: true });
}

{
  const temp = fixture("no-body-contamination");
  mutateClient(temp, value => value.replace(
    /MutationFunction<Awaited<ReturnType<typeof admMessageDeleteMessage>>, \{messageId: number; params: AdmMessageDeleteMessageParams\}>/,
    "MutationFunction<Awaited<ReturnType<typeof admMessageDeleteMessage>>, {messageId: number; params: AdmMessageDeleteMessageParams; data: AdmNotificationRuleRequest}>"
  ));
  run(temp, "scripts/verify-orval-operation-contract.mjs", false);
  run(temp, "scripts/normalize-orval-mutation-types.mjs", true);
  run(temp, "scripts/verify-orval-operation-contract.mjs", false);
  fs.rmSync(temp, { recursive: true, force: true });
}

{
  const temp = fixture("options-contamination-recovery");
  mutateClient(temp, value => value.replace(
    /UseMutationOptions<Awaited<ReturnType<typeof admMessageDeleteMessage>>, TError,\s*\{messageId: number; params: AdmMessageDeleteMessageParams\}, TContext>/,
    "UseMutationOptions<Awaited<ReturnType<typeof admMessageDeleteMessage>>, TError,{messageId: number; params: AdmMessageDeleteMessageParams; data: AdmNotificationRuleRequest}, TContext>"
  ));
  run(temp, "scripts/verify-orval-operation-contract.mjs", false);
  run(temp, "scripts/normalize-orval-mutation-types.mjs", true);
  run(temp, "scripts/normalize-generated-whitespace.mjs", true);
  run(temp, "scripts/verify-orval-operation-contract.mjs", true);
  fs.rmSync(temp, { recursive: true, force: true });
}

{
  const temp = fixture("body-options-recovery");
  mutateClient(temp, value => value.replace(
    /UseMutationOptions<Awaited<ReturnType<typeof requestAdmBrokerDlqReplay>>, TError,\s*\{messageId:\s*string;\s*data:\s*AdmReliabilityActionRequest\}, TContext>/,
    "UseMutationOptions<Awaited<ReturnType<typeof requestAdmBrokerDlqReplay>>, TError,{messageId: string}, TContext>"
  ));
  run(temp, "scripts/verify-orval-operation-contract.mjs", false);
  run(temp, "scripts/normalize-orval-mutation-types.mjs", true);
  run(temp, "scripts/normalize-generated-whitespace.mjs", true);
  run(temp, "scripts/verify-orval-operation-contract.mjs", true);
  fs.rmSync(temp, { recursive: true, force: true });
}

{
  const temp = fixture("body-function-loss");
  mutateClient(temp, value => value
    .replace(
      "requestAdmBrokerDlqReplay = async (messageId: string, data: AdmReliabilityActionRequest, options?:",
      "requestAdmBrokerDlqReplay = async (messageId: string, options?:"
    ));
  run(temp, "scripts/verify-orval-operation-contract.mjs", false);
  run(temp, "scripts/normalize-orval-mutation-types.mjs", true);
  run(temp, "scripts/verify-orval-operation-contract.mjs", false);
  fs.rmSync(temp, { recursive: true, force: true });
}


{
  const temp = fixture("semantic-body-type-loss");
  mutateClient(temp, value => value.replace(
    "data: AdmFeatureFlagEvaluateRequest, options?: CpfOrvalGeneratedRequestOptions",
    "data: Record<string, unknown>, options?: CpfOrvalGeneratedRequestOptions"
  ));
  run(temp, "scripts/verify-orval-operation-contract.mjs", false);
  fs.rmSync(temp, { recursive: true, force: true });
}

{
  const temp = fixture("risk-header-serialization-loss");
  mutateClient(temp, value => value.replace(
    '"X-CPF-Risk-Confirmed": params["X-CPF-Risk-Confirmed"]',
    '"X-CPF-Risk-Missing": params["X-CPF-Risk-Confirmed"]'
  ));
  run(temp, "scripts/verify-orval-operation-contract.mjs", false);
  fs.rmSync(temp, { recursive: true, force: true });
}

{
  const temp = fixture("required-params-made-optional");
  mutateClient(temp, value => value.replace(
    "data: AdmFeatureFlagDecisionRequest, params: AdmFeatureFlagApproveOverrideParams",
    "data: AdmFeatureFlagDecisionRequest, params?: AdmFeatureFlagApproveOverrideParams"
  ));
  run(temp, "scripts/verify-orval-operation-contract.mjs", false);
  fs.rmSync(temp, { recursive: true, force: true });
}

{
  const temp = fixture("path-type-loss");
  mutateClient(temp, value => value.replace(
    "export const admMessageFindMessage = async (messageId: number,",
    "export const admMessageFindMessage = async (messageId: string,"
  ));
  run(temp, "scripts/verify-orval-operation-contract.mjs", false);
  fs.rmSync(temp, { recursive: true, force: true });
}

{
  const temp = fixture("actor-spoof-model");
  const model = path.join(temp, "src/generated/orval/model/admReliabilityActionRequest.ts");
  fs.appendFileSync(model, "\nexport interface ActorSpoofFixture { requestUser?: string }\n");
  run(temp, "scripts/verify-orval-operation-contract.mjs", false);
  fs.rmSync(temp, { recursive: true, force: true });
}


{
  const temp = fixture("stale-url-recovery");
  mutateClient(temp, value => value.replace(
    "/adm/api/approvals/policies/${encodeURIComponent(String(policyCode))}/versions/${encodeURIComponent(String(version))}",
    "/adm/api/approvals/policies/${encodeURIComponent(String(policyCode))}/${encodeURIComponent(String(version))}"
  ));
  run(temp, "scripts/verify-orval-operation-contract.mjs", false);
  run(temp, "scripts/sync-pre-runtime-orval-coverage.mjs", true);
  run(temp, "scripts/normalize-generated-whitespace.mjs", true);
  run(temp, "scripts/verify-orval-operation-contract.mjs", true);
  fs.rmSync(temp, { recursive: true, force: true });
}

console.log("[CPF][FRONTEND][PASS] Orval operation contract negative, recovery, and idempotency fixtures");
