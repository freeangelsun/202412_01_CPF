import { expect, test, type Page, type Request } from "@playwright/test";
import { admCapabilityRegistry } from "../src/app/routes";
import { admRouteOperationContract } from "../src/generated/adm-route-operation-contract";

const routes = Object.values(admCapabilityRegistry);
const expectedRouteCount = Number(process.env.CPF_EXPECTED_ADM_ROUTE_COUNT || "65");
if (!Number.isInteger(expectedRouteCount) || expectedRouteCount <= 0) throw new Error(`invalid expected route count=${expectedRouteCount}`);
if (routes.length !== expectedRouteCount) throw new Error(`ADM route registry drift: expected=${expectedRouteCount} actual=${routes.length}`);
if (Object.keys(admRouteOperationContract).length !== routes.length) throw new Error("ADM generated route-operation contract drift");

function expectedOperations(routeId: string): string[] {
  return [...(admRouteOperationContract[routeId as keyof typeof admRouteOperationContract] || [])];
}
function observedOperation(request: Request): string | undefined {
  return request.headers()["x-cpf-operation-id"];
}
async function waitForStableRoute(page: Page, routeId: string): Promise<void> {
  await expect(page.locator(".adm-commercial-page-boundary")).toHaveAttribute("data-route-id", routeId);
  await expect(page.locator(".route-loading")).toHaveCount(0, { timeout: 15_000 });
}

for (const contract of routes) {
  test(`${contract.routeId} deep-link/menu/component/operation contract`, async ({ page }) => {
    const operations = new Set<string>();
    page.on("request", request => { const operation = observedOperation(request); if (operation) operations.add(operation); });
    await page.goto(contract.path, { waitUntil: "domcontentloaded" });
    await expect(page).toHaveURL(new RegExp(`${contract.path === "/" ? "/$" : `${contract.path}(?:\\?|$)`}`));
    await waitForStableRoute(page, contract.routeId);
    await expect(page.locator(".adm-workspace-header h1")).toContainText(contract.label);
    await expect(page.locator(".adm-sidebar button.active")).toContainText(contract.label);
    const expected = expectedOperations(contract.routeId);
    if (expected.length) {
      await expect.poll(() => expected.some(id => operations.has(id)), {
        message: `No expected operation observed. expected=${expected.join(",")} observed=${[...operations].join(",")}`,
        timeout: 15_000
      }).toBeTruthy();
    }
  });
}
