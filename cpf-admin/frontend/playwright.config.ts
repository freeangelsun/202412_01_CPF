import { defineConfig, devices } from "@playwright/test";
import fs from "node:fs";
import path from "node:path";

const baseURL = process.env.CPF_ADM_E2E_BASE_URL || "http://127.0.0.1:4173";
const storageState = process.env.CPF_ADM_E2E_STORAGE_STATE;
if (process.env.CPF_ADM_E2E_RELEASE === "true" && (!storageState || !fs.existsSync(path.resolve(storageState)))) {
  throw new Error("Release E2E requires CPF_ADM_E2E_STORAGE_STATE with an authenticated server session.");
}

export default defineConfig({
  testDir: "./e2e",
  timeout: 60_000,
  expect: { timeout: 10_000 },
  fullyParallel: false,
  retries: 0,
  reporter: [["list"], ["json", { outputFile: "test-results/adm-route-contract.json" }]],
  use: {
    baseURL,
    storageState: storageState ? path.resolve(storageState) : undefined,
    trace: "retain-on-failure",
    screenshot: "only-on-failure",
    video: "off",
    extraHTTPHeaders: { "X-CPF-E2E": "adm-route-contract" }
  },
  projects: [
    { name: "chromium", use: { ...devices["Desktop Chrome"] } },
    { name: "firefox", use: { ...devices["Desktop Firefox"] } },
    { name: "webkit", use: { ...devices["Desktop Safari"] } }
  ]
});
