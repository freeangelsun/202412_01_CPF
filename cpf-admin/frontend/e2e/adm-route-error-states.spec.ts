import { expect, test } from "@playwright/test";
import { admCapabilityRegistry } from "../src/app/routes";

const routes = Object.values(admCapabilityRegistry);
const statuses = [401, 403, 404, 409, 429, 500, 503] as const;

for (const route of routes) {
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
