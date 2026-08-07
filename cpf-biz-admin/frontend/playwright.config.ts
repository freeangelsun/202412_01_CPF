import { defineConfig, devices } from '@playwright/test';
import fs from 'node:fs';
import path from 'node:path';
const baseURL = process.env.CPF_BZA_FRONTEND_URL || process.env.CPF_FRONTEND_URL;
if (!baseURL) throw new Error('CPF_BZA_FRONTEND_URL (or CPF_FRONTEND_URL fallback) is required. Browser validation cannot silently skip.');
const release = process.env.CPF_E2E_RELEASE === 'true';
const storageState = process.env.CPF_BZA_E2E_AUTH_STATE || process.env.CPF_E2E_AUTH_STATE;
const requiredReleaseInputs = [
  'CPF_BZA_E2E_AUTH_STATE',
  'CPF_BZA_E2E_PRIVILEGED_ENDPOINTS',
  'CPF_BZA_E2E_ROUTE_MATRIX',
  'CPF_BZA_E2E_FAILURE_MATRIX',
  'CPF_BZA_E2E_SECURITY_FIXTURE'
];
if (release) {
  if (!process.env.CPF_BZA_FRONTEND_URL) throw new Error('CPF_BZA_FRONTEND_URL is required for release browser validation.');
  for (const name of requiredReleaseInputs) {
    if (!process.env[name]) throw new Error(`${name} is required for release browser validation.`);
  }
  const statePath = process.env.CPF_BZA_E2E_AUTH_STATE;
  if (!statePath || !fs.existsSync(path.resolve(statePath))) {
    throw new Error('CPF_BZA_E2E_AUTH_STATE must reference an existing authenticated BZA server session for release validation.');
  }
}
export default defineConfig({
  testDir: './e2e',
  fullyParallel: false,
  workers: release ? 1 : undefined,
  retries: release ? 0 : 1,
  forbidOnly: true,
  reporter: [['html',{open:'never'}],['json',{outputFile:'test-results/results.json'}],['junit',{outputFile:'test-results/junit.xml'}]],
  use: {
    baseURL,
    storageState: storageState ? path.resolve(storageState) : undefined,
    trace:'retain-on-failure',
    screenshot:'only-on-failure',
    video:'retain-on-failure',
    actionTimeout:15_000,
    navigationTimeout:30_000
  },
  projects: [
    {name:'chromium',use:{...devices['Desktop Chrome']}},
    {name:'firefox',use:{...devices['Desktop Firefox']}},
    {name:'webkit',use:{...devices['Desktop Safari']}}
  ]
});
