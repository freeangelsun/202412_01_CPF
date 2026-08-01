import { expect, test } from "@playwright/test";
import fs from "node:fs";
import path from "node:path";

const matrix = fs.readFileSync(path.resolve(process.cwd(), "../../cpf-docs/quality/CPF_20260801_ADM_ROUTE_INTERACTION_MATRIX.csv"), "utf8");
const paths = [...matrix.matchAll(/(?:^|\n)([^,]+),([^,]+),/g)].slice(1).map(match => ({ routeId: match[1].replace(/^\uFEFF/, ""), path: match[2] }));
const statuses = [401, 403, 404, 409, 429, 500, 503] as const;

for (const route of paths) {
  test(`${route.routeId} exposes all mandatory error states`, async ({ page }) => {
    for (const status of statuses) {
      await page.route("**/adm/api/**", async request => {
        await request.fulfill({ status, contentType: "application/json", body: JSON.stringify({ status, code: `E2E_${status}`, message: `state ${status}`, timestamp: new Date().toISOString() }) });
      });
      await page.goto(route.path, { waitUntil: "domcontentloaded" });
      const visibleState = page.locator(`.route-contract-error[data-status="${status}"], .state.error, [role="alert"]`);
      await expect(visibleState.first()).toBeVisible({ timeout: 10_000 });
      await page.unroute("**/adm/api/**");
    }
  });
}
