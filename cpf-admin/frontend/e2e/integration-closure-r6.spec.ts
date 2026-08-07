import { expect, test } from "@playwright/test";

const route = "/integrationClosure";
async function grant(page: import("@playwright/test").Page, operations: string[]) {
  await page.addInitScript((value) => {
    document.addEventListener("DOMContentLoaded", () => {
      document.documentElement.dataset.admPermissions = value;
    }, { once: true });
  }, operations.join(","));
}

test("missing server permission metadata fails closed", async ({ page }) => {
  await grant(page, []);
  await page.goto(route);
  await expect(page.getByRole("heading", { name: "통합 운영 Closure" })).toBeVisible();
  await expect(page.getByRole("button", { name: "상태 조회" })).toBeDisabled();
  await expect(page.getByRole("button", { name: "승인 요청" })).toBeDisabled();
  await expect(page.getByRole("button", { name: "재검증 Replay" })).toBeDisabled();
});

test("operation grants are applied per button and 403 is announced", async ({ page }) => {
  await grant(page, ["admIntegrationCryptoStatus", "admIntegrationTimeHealth"]);
  await page.route("**/adm/api/integration-closure/**", async route => {
    await route.fulfill({ status: 403, contentType: "application/json", body: JSON.stringify({ message: "denied" }) });
  });
  await page.goto(route);
  await expect(page.getByRole("button", { name: "상태 조회" })).toBeEnabled();
  await expect(page.getByRole("button", { name: "승인 요청" })).toBeDisabled();
  await page.getByRole("button", { name: "상태 조회" }).click();
  await expect(page.getByRole("alert")).toContainText("403");
});

test("@a11y keyboard focus and responsive operation controls", async ({ page }) => {
  await grant(page, ["admIntegrationDataQualityValidate"]);
  await page.setViewportSize({ width: 390, height: 844 });
  await page.goto(route);
  await page.keyboard.press("Tab");
  await expect(page.locator(":focus")).toBeVisible();
  await expect(page.getByRole("main")).toHaveAttribute("aria-labelledby", "integration-closure-title");
  await expect(page.getByRole("button", { name: "검증" })).toBeDisabled();
  await page.getByLabel("Record ID").fill("R-1");
  await page.getByLabel("검증 JSON").fill('{"name":null}');
  await expect(page.getByRole("button", { name: "검증" })).toBeEnabled();
});
