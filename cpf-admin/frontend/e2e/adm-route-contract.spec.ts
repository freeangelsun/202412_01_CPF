import { expect, test, type Page, type Request } from "@playwright/test";
import fs from "node:fs";
import path from "node:path";

interface RouteContract {
  route_id: string; path: string; menu_id: string; label: string;
  query_operation_ids: string; mutation_operation_ids: string;
  required_error_statuses: string; requires_detail: string; requires_server_paging: string;
}

function parseCsv(text: string): RouteContract[] {
  const rows: string[][] = []; let row: string[] = []; let cell = ""; let quoted = false;
  for (let i = 0; i < text.length; i++) {
    const ch = text[i];
    if (quoted) {
      if (ch === '"' && text[i + 1] === '"') { cell += '"'; i++; }
      else if (ch === '"') quoted = false;
      else cell += ch;
    } else if (ch === '"') quoted = true;
    else if (ch === ',') { row.push(cell); cell = ""; }
    else if (ch === '\n') { row.push(cell.replace(/\r$/, "")); rows.push(row); row = []; cell = ""; }
    else cell += ch;
  }
  if (cell || row.length) { row.push(cell); rows.push(row); }
  const header = rows.shift()!.map((value, index) => index === 0 ? value.replace(/^\uFEFF/, "") : value);
  return rows.filter(values => values.some(Boolean)).map(values => Object.fromEntries(header.map((key, index) => [key, values[index] ?? ""])) as unknown as RouteContract);
}

const matrixPath = path.resolve(process.cwd(), "../../cpf-docs/quality/CPF_20260801_ADM_ROUTE_INTERACTION_MATRIX.csv");
if (!fs.existsSync(matrixPath)) throw new Error(`ADM route matrix missing: ${matrixPath}`);
const routes = parseCsv(fs.readFileSync(matrixPath, "utf8"));
if (routes.length !== 59) throw new Error(`ADM route baseline drift: expected=59 actual=${routes.length}`);

function expectedOperations(route: RouteContract): string[] {
  return `${route.query_operation_ids};${route.mutation_operation_ids}`.split(";").map(v => v.trim()).filter(Boolean);
}
function observedOperation(request: Request): string | undefined {
  return request.headers()["x-cpf-operation-id"];
}
async function waitForStableRoute(page: Page, routeId: string): Promise<void> {
  await expect(page.locator('.adm-commercial-page-boundary')).toHaveAttribute('data-route-id', routeId);
  await expect(page.locator('.route-loading')).toHaveCount(0, { timeout: 15_000 });
}

for (const contract of routes) {
  test(`${contract.route_id} deep-link/menu/component/operation contract`, async ({ page }) => {
    const operations = new Set<string>();
    page.on("request", request => { const operation = observedOperation(request); if (operation) operations.add(operation); });
    await page.goto(contract.path, { waitUntil: "domcontentloaded" });
    await expect(page).toHaveURL(new RegExp(`${contract.path === "/" ? "/$" : `${contract.path}(?:\\?|$)`}`));
    await waitForStableRoute(page, contract.route_id);
    await expect(page.locator('.adm-workspace-header h1')).toContainText(contract.label);
    await expect(page.locator('.adm-sidebar button.active')).toContainText(contract.label);
    const expected = expectedOperations(contract);
    if (expected.length) {
      await expect.poll(() => expected.some(id => operations.has(id)), {
        message: `No expected operation observed. expected=${expected.join(',')} observed=${[...operations].join(',')}`,
        timeout: 15_000
      }).toBeTruthy();
    }
    if (contract.requires_detail === "true") await expect(page.locator('main,section,article,table').first()).toBeVisible();
  });
}
