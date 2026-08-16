import { test, expect } from '@playwright/test';

test('deep-link, secure server session, no browser token persistence', async ({ page, context }) => {
  await page.goto('/');
  await expect(page.locator('body')).toBeVisible();
  const cookies = await context.cookies();
  const sessions = cookies.filter((cookie) => /SESSION/i.test(cookie.name));
  expect(sessions.length).toBeGreaterThan(0);
  expect(sessions.every((cookie) => cookie.httpOnly && cookie.secure && cookie.sameSite === 'Strict')).toBeTruthy();
  expect(await page.evaluate(() => Object.keys(sessionStorage).concat(Object.keys(localStorage)).filter((key) => /token/i.test(key)))).toEqual([]);
});

test('@a11y keyboard, landmark and accessible-name contract', async ({ page }) => {
  await page.goto('/');
  await expect(page.locator('main, [role=main]').first()).toBeVisible();
  const unnamedButtons = await page.locator('button:not([aria-label])').evaluateAll((buttons) =>
    buttons.filter((button) => !(button.textContent ?? '').trim()).length,
  );
  expect(unnamedButtons).toBe(0);
  await page.keyboard.press('Tab');
  expect(await page.evaluate(() => document.activeElement !== document.body)).toBeTruthy();
  const duplicateIds = await page.evaluate(() => {
    const ids = Array.from(document.querySelectorAll('[id]')).map((element) => element.id);
    return ids.filter((id, index) => ids.indexOf(id) !== index);
  });
  expect(duplicateIds).toEqual([]);
});
