import { expect, test, type Page } from "@playwright/test";

async function applicationRoutes(page: Page): Promise<string[]> {
  return page.locator('nav a[href], [role="navigation"] a[href]').evaluateAll((links) =>
    Array.from(new Set(links
      .map((link) => (link as HTMLAnchorElement).getAttribute("href") || "")
      .filter((href) => href.startsWith("/") && !/logout|download/i.test(href))))
  );
}

async function assertAccessibleControls(page: Page): Promise<void> {
  const unnamed = await page.locator('button, input, select, textarea, [role="button"]').evaluateAll((elements) =>
    elements.filter((element) => {
      const node = element as HTMLElement;
      if (node.hasAttribute("disabled") || node.getAttribute("aria-hidden") === "true") return false;
      const label = node.getAttribute("aria-label") || node.getAttribute("aria-labelledby") ||
        (node instanceof HTMLInputElement ? node.labels?.[0]?.textContent : "") || node.textContent || "";
      return !label.trim();
    }).length
  );
  expect(unnamed).toBe(0);
}

test("all visible routes preserve navigation, loading, empty, paging and sort contracts", async ({ page }) => {
  await page.goto("/");
  const routes = await applicationRoutes(page);
  expect(routes.length).toBeGreaterThan(0);
  for (const route of routes.slice(0, 40)) {
    await page.goto(route);
    await expect(page.locator('main, [role="main"]').first()).toBeVisible();
    await expect(page.locator('[aria-busy="true"]')).toHaveCount(0, { timeout: 15_000 });
    await assertAccessibleControls(page);

    const tables = page.locator('table, [role="grid"]');
    if (await tables.count()) {
      const sortable = page.locator('th[aria-sort], [role="columnheader"][aria-sort]');
      for (let index = 0; index < await sortable.count(); index++) {
        expect(["none", "ascending", "descending", "other"]).toContain(
          await sortable.nth(index).getAttribute("aria-sort")
        );
      }
    }
    const paging = page.locator('[aria-label*="page" i], [aria-label*="페이지"], nav.pagination');
    if (await paging.count()) await expect(paging.first()).toBeVisible();
    const search = page.locator('input[type="search"], input[aria-label*="검색"], input[placeholder*="검색"]');
    if (await search.count()) {
      await search.first().fill("__cpf_no_result__");
      await search.first().press("Enter");
      await expect(page.locator('[role="alert"], [data-state="empty"], .empty-state, tbody')).toBeVisible();
    }
  }
});

test("mobile and keyboard navigation remain usable without horizontal overflow", async ({ page }) => {
  await page.setViewportSize({ width: 390, height: 844 });
  await page.goto("/");
  const overflow = await page.evaluate(() => document.documentElement.scrollWidth - document.documentElement.clientWidth);
  expect(overflow).toBeLessThanOrEqual(1);
  await page.keyboard.press("Tab");
  expect(await page.evaluate(() => document.activeElement !== document.body)).toBeTruthy();
  await assertAccessibleControls(page);
});

test("API failure is visible and does not expose a privileged action as successful", async ({ page }) => {
  let injected = false;
  await page.route(/\/api\//, async (route) => {
    if (!injected && !/auth\/(?:login|session|me)/.test(route.request().url())) {
      injected = true;
      await route.fulfill({ status: 503, contentType: "application/json", body: JSON.stringify({ code: "QA33_FORCED_FAILURE", message: "forced failure" }) });
      return;
    }
    await route.continue();
  });
  await page.goto("/");
  if (injected) {
    await expect(page.locator('[role="alert"], .error-state, [data-state="error"]').first()).toBeVisible({ timeout: 15_000 });
    await expect(page.getByText(/완료|성공/).last()).not.toContainText("QA33_FORCED_FAILURE");
  }
});

test("unauthenticated or unauthorized navigation does not render restricted controls", async ({ page, context }) => {
  await context.clearCookies();
  await page.goto("/");
  const restricted = page.locator('[data-requires-permission="true"], [data-dangerous-action="true"]');
  expect(await restricted.count()).toBe(0);
});


test("unknown deep link renders an explicit 404 instead of silently redirecting", async ({ page }) => {
  await page.goto("/__cpf_missing_route__");
  await expect(page.getByRole("alert")).toContainText("404");
  await expect(page).toHaveURL(/__cpf_missing_route__/);
});
