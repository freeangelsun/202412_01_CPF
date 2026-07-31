import fs from 'node:fs';
import { expect, test, request as playwrightRequest, type APIRequestContext } from '@playwright/test';

const release = process.env.CPF_E2E_RELEASE === 'true';
const privileged = (process.env.CPF_E2E_PRIVILEGED_ENDPOINTS || '')
  .split(',')
  .map(value => value.trim())
  .filter(Boolean);

type Call = {
  method?: string;
  url: string;
  body?: unknown;
  headers?: Record<string, string>;
};

type Fixture = {
  sessionCookieName: string;
  protectedEndpoint: string;
  writeEndpoint: string;
  login: Call;
  logout: Call;
  untrustedOrigin: string;
  firstSessionAfterSecondLoginStatuses: number[];
};

function fixture(): Fixture {
  const file = process.env.CPF_E2E_SECURITY_FIXTURE;
  if (!file) throw new Error('CPF_E2E_SECURITY_FIXTURE is required');
  return JSON.parse(fs.readFileSync(file, 'utf8')) as Fixture;
}

async function invoke(context: APIRequestContext, call: Call) {
  return context.fetch(call.url, {
    method: call.method || 'POST',
    data: call.body,
    headers: call.headers,
    failOnStatusCode: false
  });
}

function cookieValue(
  state: Awaited<ReturnType<APIRequestContext['storageState']>>,
  name: string
) {
  return state.cookies.find(cookie => cookie.name === name)?.value;
}

test('unauthenticated privileged endpoints fail closed with 401/403', async ({ baseURL }) => {
  test.skip(!release, 'Actual BFF security is required in release mode.');
  expect(privileged.length, 'CPF_E2E_PRIVILEGED_ENDPOINTS is required').toBeGreaterThan(0);
  const anonymous = await playwrightRequest.newContext({ baseURL, storageState: undefined });
  for (const endpoint of privileged) {
    const response = await anonymous.get(endpoint, { failOnStatusCode: false });
    expect([401, 403], `${endpoint} exposed`).toContain(response.status());
  }
  await anonymous.dispose();
});

test('browser storage never exposes token credential or session id', async ({ page }) => {
  test.skip(!release, 'Actual browser session is required in release mode.');
  await page.goto('/');
  const values = await page.evaluate(() => ({
    local: { ...localStorage },
    session: { ...sessionStorage },
    cookie: document.cookie
  }));
  const serialized = JSON.stringify(values).toLowerCase();
  expect(serialized).not.toMatch(/access[_-]?token|refresh[_-]?token|authorization|bearer\s|session[_-]?id/);
});

test('csrf and untrusted origin are rejected on authenticated write', async ({ baseURL }) => {
  test.skip(!release, 'Actual BFF security is required in release mode.');
  const f = fixture();
  const authenticated = await playwrightRequest.newContext({
    baseURL,
    storageState: process.env.CPF_E2E_AUTH_STATE
  });
  const csrf = await authenticated.post(f.writeEndpoint, {
    data: { qa34: true },
    failOnStatusCode: false
  });
  expect(csrf.status(), 'write without CSRF token must fail').toBe(403);
  const origin = await authenticated.post(f.writeEndpoint, {
    data: { qa34: true },
    headers: { Origin: f.untrustedOrigin },
    failOnStatusCode: false
  });
  expect([401, 403], 'untrusted Origin must fail').toContain(origin.status());
  await authenticated.dispose();
});

test('login rotates session, logout revokes it, and concurrent login enforces policy', async ({ baseURL }) => {
  test.skip(!release, 'Actual BFF session lifecycle is required in release mode.');
  const f = fixture();
  const first = await playwrightRequest.newContext({ baseURL, storageState: undefined });
  await first.get('/', { failOnStatusCode: false });
  const before = cookieValue(await first.storageState(), f.sessionCookieName);
  const firstLogin = await invoke(first, f.login);
  expect(firstLogin.ok(), `first login failed status=${firstLogin.status()}`).toBeTruthy();
  const after = cookieValue(await first.storageState(), f.sessionCookieName);
  expect(after, 'session cookie must exist after login').toBeTruthy();
  if (before) expect(after, 'session fixation: cookie was not rotated').not.toBe(before);

  const second = await playwrightRequest.newContext({ baseURL, storageState: undefined });
  const secondLogin = await invoke(second, f.login);
  expect(secondLogin.ok(), `second login failed status=${secondLogin.status()}`).toBeTruthy();
  const firstAfterSecond = await first.get(f.protectedEndpoint, { failOnStatusCode: false });
  expect(f.firstSessionAfterSecondLoginStatuses).toContain(firstAfterSecond.status());

  const logout = await invoke(second, f.logout);
  expect(logout.ok() || logout.status() === 204, `logout failed status=${logout.status()}`).toBeTruthy();
  const afterLogout = await second.get(f.protectedEndpoint, { failOnStatusCode: false });
  expect([401, 403], 'logout did not revoke session').toContain(afterLogout.status());
  await first.dispose();
  await second.dispose();
});
