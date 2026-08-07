import { expect, test, type Browser, type BrowserContext, type Page } from "@playwright/test";
import path from "node:path";

const route = "/integrationClosure";

type AdmSession = { buttonIds?: string[] };

async function readSession(page: Page): Promise<Set<string>> {
  const response = await page.request.get("/adm/api/auth/me");
  expect(response.ok(), `auth/me must succeed, actual=${response.status()}`).toBeTruthy();
  const session = await response.json() as AdmSession;
  expect(Array.isArray(session.buttonIds)).toBeTruthy();
  return new Set(session.buttonIds ?? []);
}

async function openRoleContext(browser: Browser, envName: string): Promise<BrowserContext> {
  const state = process.env[envName];
  if (!state) throw new Error(`${envName} is required for authenticated role-matrix validation.`);
  return browser.newContext({ storageState: path.resolve(state) });
}

async function assertGrantParity(page: Page, buttonName: string, operationIds: string[]): Promise<void> {
  const grants = await readSession(page);
  await page.goto(route);
  await expect(page.getByRole("heading", { name: "통합 운영 Closure" })).toBeVisible();
  const button = page.getByRole("button", { name: buttonName });
  const expected = operationIds.every(id => grants.has(id));
  if (expected) await expect(button).toHaveAttribute("title", "");
  else {
    await expect(button).toBeDisabled();
    await expect(button).toHaveAttribute("title", /권한 없음:/);
  }
}

test("actual auth/me buttonIds are the single source for integration closure actions", async ({ page }) => {
  await assertGrantParity(page, "상태 조회", ["admIntegrationCryptoStatus", "admIntegrationTimeHealth"]);
  await assertGrantParity(page, "승인 요청", ["admIntegrationDataQualityCorrectionApprovalRequest"]);
  await assertGrantParity(page, "재검증 Replay", ["admIntegrationDataQualityReplay"]);
});

test("read-only and operator server sessions preserve action-level role separation", async ({ browser }) => {
  const readonly = await openRoleContext(browser, "CPF_E2E_AUTH_STATE_READONLY");
  const operator = await openRoleContext(browser, "CPF_E2E_AUTH_STATE_OPERATOR");
  try {
    const readonlyPage = await readonly.newPage();
    const readonlyGrants = await readSession(readonlyPage);
    await readonlyPage.goto(route);
    expect(readonlyGrants.has("admIntegrationDataQualityCorrectionExecute")).toBeFalsy();
    const readonlyExecute = readonlyPage.getByRole("button", { name: "승인 검증 후 단회 실행" });
    await expect(readonlyExecute).toBeDisabled();
    await expect(readonlyExecute).toHaveAttribute("title", /권한 없음: admIntegrationDataQualityCorrectionExecute/);

    const operatorPage = await operator.newPage();
    const operatorGrants = await readSession(operatorPage);
    await operatorPage.goto(route);
    const execute = operatorPage.getByRole("button", { name: "승인 검증 후 단회 실행" });
    if (operatorGrants.has("admIntegrationDataQualityCorrectionExecute")) {
      await expect(execute).toHaveAttribute("title", "");
    } else {
      await expect(execute).toBeDisabled();
      await expect(execute).toHaveAttribute("title", /권한 없음: admIntegrationDataQualityCorrectionExecute/);
    }
  } finally {
    await readonly.close();
    await operator.close();
  }
});

test("@a11y keyboard focus and responsive controls use the real authenticated session", async ({ page }) => {
  const grants = await readSession(page);
  await page.setViewportSize({ width: 390, height: 844 });
  await page.goto(route);
  await page.keyboard.press("Tab");
  await expect(page.locator(":focus")).toBeVisible();
  await expect(page.getByRole("main")).toHaveAttribute("aria-labelledby", "integration-closure-title");
  const validate = page.getByRole("button", { name: "검증" });
  if (!grants.has("admIntegrationDataQualityValidate")) {
    await expect(validate).toBeDisabled();
    return;
  }
  await page.getByLabel("Record ID").fill("R-1");
  await page.getByLabel("검증 JSON").fill('{"name":null}');
  await expect(validate).toBeEnabled();
});
