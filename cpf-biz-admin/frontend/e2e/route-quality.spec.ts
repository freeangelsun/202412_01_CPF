import fs from 'node:fs';
import { expect, test, type APIRequestContext, type Page, type Route } from '@playwright/test';
import { bzaRouterRecords } from '../src/app/routes';

const release = process.env.CPF_E2E_RELEASE === 'true';
const routes = bzaRouterRecords.map(record => String(record.path)).filter(path => !path.includes(':'));
const errors = [401,403,409,429,500,503];

type Interaction = {
  selector: string;
  action: 'fill'|'click'|'select'|'press';
  value?: string;
  key?: string;
  expectedUrl?: string;
  expectedText?: string;
};
type RouteCase = { path: string; interactions?: Interaction[]; riskConfirmationSelector?: string };
type FailureCase = {
  route: string;
  expectedStatus: number;
  expectedCode: string;
  setup?: { method?: string; url: string; body?: unknown };
  cleanup?: { method?: string; url: string; body?: unknown };
};

function loadJson<T>(name: string): T {
  const file = process.env[name];
  if (!file) throw new Error(`${name} is required`);
  return JSON.parse(fs.readFileSync(file, 'utf8')) as T;
}
const routeMatrix = release ? loadJson<RouteCase[]>('CPF_E2E_ROUTE_MATRIX') : [];
const failureMatrix = release ? loadJson<FailureCase[]>('CPF_E2E_FAILURE_MATRIX') : [];

async function assertPageQuality(page: Page, route: string) {
  await page.goto(route);
  await expect(page.locator('main, [role="main"]').first()).toBeVisible();
  await expect(page.locator('[aria-busy="true"]')).toHaveCount(0,{timeout:20_000});
  const unnamed=await page.locator('button,input,select,textarea,[role="button"]').evaluateAll(elements=>elements.filter(element=>{
    const node=element as HTMLElement;
    if(node.hasAttribute('disabled')||node.getAttribute('aria-hidden')==='true') return false;
    const label=node.getAttribute('aria-label')||node.getAttribute('aria-labelledby')||(node instanceof HTMLInputElement?node.labels?.[0]?.textContent:'')||node.textContent||'';
    return !label.trim();
  }).length);
  expect(unnamed,`unnamed control on ${route}`).toBe(0);
  const overflow=await page.evaluate(()=>document.documentElement.scrollWidth-document.documentElement.clientWidth);
  expect(overflow,`horizontal overflow on ${route}`).toBeLessThanOrEqual(1);
}

async function mockAuth(route: Route) {
  const url=route.request().url();
  if(/\/auth\/(?:me|session)/.test(url)) return route.fulfill({status:200,contentType:'application/json',body:JSON.stringify({authenticated:true,operatorId:'qa34',permissions:['*'],menus:routes})});
  if(/\/auth\/login/.test(url)) return route.fulfill({status:204,body:''});
  return route.fulfill({status:200,contentType:'application/json',body:JSON.stringify({content:[],items:[],data:[],page:0,size:20,totalElements:0,totalPages:0})});
}

async function callFixture(request: APIRequestContext, call?: {method?:string;url:string;body?:unknown}) {
  if (!call) return;
  const response=await request.fetch(call.url,{method:call.method||'POST',data:call.body,failOnStatusCode:false});
  expect(response.ok(),`fixture call failed: ${call.url} status=${response.status()}`).toBeTruthy();
}

async function applyInteractions(page: Page, item: RouteCase) {
  for (const interaction of item.interactions||[]) {
    const target=page.locator(interaction.selector).first();
    await expect(target,`missing interaction control ${interaction.selector} on ${item.path}`).toBeVisible();
    if(interaction.action==='fill') await target.fill(interaction.value||'');
    else if(interaction.action==='click') await target.click();
    else if(interaction.action==='select') await target.selectOption(interaction.value||'');
    else await target.press(interaction.key||'Enter');
    if(interaction.expectedUrl) await expect(page).toHaveURL(new RegExp(interaction.expectedUrl));
    if(interaction.expectedText) await expect(page.getByText(interaction.expectedText).first()).toBeVisible();
  }
  if(item.riskConfirmationSelector) {
    const confirmation=page.locator(item.riskConfirmationSelector).first();
    await expect(confirmation,'dangerous operation confirmation is missing').toBeVisible();
  }
}

test('release route matrix covers the complete router registry',()=>{
  test.skip(!release,'Release route matrix is evaluated only against the real backend.');
  expect(new Set(routeMatrix.map(item=>item.path))).toEqual(new Set(routes));
});

test('router registry entire route quality and interaction contract',async({page})=>{
  expect(routes.length).toBeGreaterThan(0);
  for(const path of routes){
    const calls:string[]=[];
    if(!release) await page.route(/\/api\//,async route=>{calls.push(route.request().url());await mockAuth(route);});
    await assertPageQuality(page,path);
    if(!release) expect(calls.length,`route ${path} made no API request`).toBeGreaterThan(0);
    if(release) {
      const item=routeMatrix.find(candidate=>candidate.path===path);
      expect(item,`release route fixture missing: ${path}`).toBeTruthy();
      await applyInteractions(page,item!);
    }
    if(!release) await page.unroute(/\/api\//);
  }
});

test('local deterministic API error injection never false-greens',async({page})=>{
  test.skip(release,'Release mode uses the required server-side failure matrix test.');
  for(let index=0;index<routes.length;index++){
    const path=routes[index];const status=errors[index%errors.length];let injected=0;
    await page.route(/\/api\//,async route=>{
      if(/\/auth\/(?:me|session|login)/.test(route.request().url())) return mockAuth(route);
      injected++;return route.fulfill({status,contentType:'application/problem+json',body:JSON.stringify({code:`QA34_${status}`,message:'forced failure'})});
    });
    await page.goto(path);
    expect(injected,`route ${path} did not exercise backend API`).toBeGreaterThan(0);
    await expect(page.locator('[role="alert"],.error-state,[data-state="error"]').first()).toBeVisible({timeout:15_000});
    await expect(page.getByText(/QA34_/).first()).toBeVisible();
    await page.unroute(/\/api\//);
  }
});

test('release server-side failure matrix exposes 401/403/409/429/500/503 visibly',async({page,request})=>{
  test.skip(!release,'Real backend failure matrix is required only in release mode.');
  expect(failureMatrix.length).toBeGreaterThan(0);
  expect(new Set(failureMatrix.map(item=>item.expectedStatus))).toEqual(new Set(errors));
  for(const item of failureMatrix){
    expect(routes).toContain(item.route);
    await callFixture(request,item.setup);
    await page.goto(item.route);
    await expect(page.locator('[role="alert"],.error-state,[data-state="error"]').first()).toBeVisible({timeout:15_000});
    await expect(page.getByText(new RegExp(item.expectedCode,'i')).first()).toBeVisible();
    await callFixture(request,item.cleanup);
  }
});

test('unknown deep link is explicit 404 and keyboard/mobile remain usable',async({page})=>{
  if(!release) await page.route(/\/api\//,mockAuth);
  await page.setViewportSize({width:390,height:844});
  await page.goto('/__cpf_missing_route__');
  await expect(page.getByRole('alert')).toContainText('404');
  await expect(page).toHaveURL(/__cpf_missing_route__/);
  await page.keyboard.press('Tab');
  expect(await page.evaluate(()=>document.activeElement!==document.body)).toBeTruthy();
});
