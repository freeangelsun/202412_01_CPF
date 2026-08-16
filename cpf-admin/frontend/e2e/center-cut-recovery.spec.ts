import { expect, test } from '@playwright/test'

test('Center-Cut exposes only execution-scoped approved recovery actions @a11y', async ({ page }) => {
  const mutations: string[] = []
  await page.route('**/adm/api/center-cut/jobs', route => route.fulfill({ json: [{ centerCutJobId: 'CC-JOB-1', jobName: 'Center Cut', status: 'ACTIVE' }] }))
  await page.route('**/adm/api/center-cut/jobs/CC-JOB-1', route => route.fulfill({ json: { centerCutJobId: 'CC-JOB-1' } }))
  await page.route('**/adm/api/center-cut/jobs/CC-JOB-1/summary', route => route.fulfill({ json: { failed: 1, unknown: 1 } }))
  await page.route('**/adm/api/center-cut/jobs/CC-JOB-1/targets', route => route.fulfill({ json: [] }))
  await page.route('**/adm/api/center-cut/jobs/CC-JOB-1/parameters', route => route.fulfill({ json: [] }))
  await page.route('**/adm/api/center-cut/jobs/CC-JOB-1/results', route => route.fulfill({ json: [
    { resultId: 'R1', executionId: 'E-100', resultStatus: 'FAILED', resultMessage: 'failed' },
    { resultId: 'R2', executionId: 'E-101', resultStatus: 'UNKNOWN', resultMessage: 'unknown' },
  ] }))
  await page.route('**/adm/api/center-cut/results/R1', route => route.fulfill({ json: { resultId: 'R1' } }))
  await page.route('**/adm/api/center-cut/executions/**', async route => {
    mutations.push(route.request().url())
    await route.fulfill({ status: 202, json: { status: 'COMPLETED' } })
  })

  await page.goto('/batch/center-cut')
  await page.getByRole('button', { name: '열기' }).click()
  await expect(page.getByRole('button', { name: '실패 재처리' })).toHaveCount(1)
  await expect(page.getByRole('button', { name: 'UNKNOWN 대사' })).toHaveCount(1)
  await expect(page.getByRole('button', { name: /Job.*재처리|일괄 재처리/ })).toHaveCount(0)
  await expect(page.getByRole('heading', { name: 'Center-Cut Execution Recovery' })).toBeVisible()
  expect(mutations.some(url => /\/jobs\/.*\/(reprocess|reconcile)/.test(url))).toBeFalsy()
})
