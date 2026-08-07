import { defineConfig, devices } from "@playwright/test";
import fs from "node:fs";
import path from "node:path";

const baseURL = process.env.CPF_ADM_FRONTEND_URL;
if (!baseURL) throw new Error("CPF_ADM_FRONTEND_URL is required. Browser validation cannot silently skip.");

const release = process.env.CPF_E2E_RELEASE === "true";
const storageState = process.env.CPF_E2E_AUTH_STATE;
const requiredReleaseInputs = [
  "CPF_E2E_AUTH_STATE",
  "CPF_E2E_AUTH_STATE_READONLY",
  "CPF_E2E_AUTH_STATE_OPERATOR",
  "CPF_E2E_PRIVILEGED_ENDPOINTS",
  "CPF_E2E_ROUTE_MATRIX",
  "CPF_E2E_FAILURE_MATRIX",
  "CPF_E2E_SECURITY_FIXTURE"
];
if (release) {
  for (const name of requiredReleaseInputs) {
    if (!process.env[name]) throw new Error(`${name} is required for release browser validation.`);
  }
  for (const name of ["CPF_E2E_AUTH_STATE", "CPF_E2E_AUTH_STATE_READONLY", "CPF_E2E_AUTH_STATE_OPERATOR"]) {
    const statePath = process.env[name];
    if (!statePath || !fs.existsSync(path.resolve(statePath))) {
      throw new Error(`${name} must reference an existing authenticated server session for release validation.`);
    }
  }
}

export default defineConfig({
  testDir: "./e2e",
  timeout: 60_000,
  expect: { timeout: 10_000 },
  fullyParallel: false,
  workers: release ? 1 : undefined,
  retries: release ? 0 : 1,
  forbidOnly: true,
  reporter: [
    ["list"],
    ["html", { open: "never" }],
    ["json", { outputFile: "test-results/adm-route-contract.json" }],
    ["junit", { outputFile: "test-results/junit.xml" }]
  ],
  use: {
    baseURL,
    storageState: storageState ? path.resolve(storageState) : undefined,
    trace: "retain-on-failure",
    screenshot: "only-on-failure",
    video: "retain-on-failure",
    actionTimeout: 15_000,
    navigationTimeout: 30_000,
    extraHTTPHeaders: { "X-CPF-E2E": "adm-route-contract" }
  },
  projects: [
    { name: "chromium", use: { ...devices["Desktop Chrome"] } },
    { name: "firefox", use: { ...devices["Desktop Firefox"] } },
    { name: "webkit", use: { ...devices["Desktop Safari"] } }
  ]
});
