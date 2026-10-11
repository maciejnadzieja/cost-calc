import { test, expect } from '@playwright/test'
import { apiURL, login, usersPage } from './support'

test('admin sees Users and reads real realm accounts', async ({ page, request }) => {
  const token = await login(page, 'admin')
  await expect(page.locator('a[href="/admin/users"]')).toBeVisible()
  await page.locator('a[href="/admin/users"]').click()
  await expect(page.getByRole('heading', { name: 'Users', exact: true })).toBeVisible()
  await expect(page.locator('tbody tr')).not.toHaveCount(0)
  const result = await usersPage(request, token)
  expect(result.content.length).toBeGreaterThan(0)
  expect(result.content.length).toBeLessThanOrEqual(20)
  await expect(page.getByRole('columnheader')).toHaveText(['Username', 'Email', 'Enabled'])
})

test('tester is denied both direct navigation and API access', async ({ page, request }) => {
  const token = await login(page, 'tester')
  await expect(page.locator('a[href="/admin/users"]')).toHaveCount(0)
  let directoryRequests = 0
  page.on('request', req => { if (req.url().startsWith(`${apiURL}/admin/users`)) directoryRequests++ })
  await page.goto('/admin/users')
  await expect(page).toHaveURL(/\/access-denied$/)
  await expect(page.getByRole('alert')).toBeVisible()
  expect(directoryRequests).toBe(0)
  const response = await request.get(`${apiURL}/admin/users`, { headers: { Authorization: token } })
  expect(response.status()).toBe(403)
})

test('missing and invalid bearer tokens are rejected', async ({ request }) => {
  const attempts: Record<string, string>[] = [{}, { Authorization: 'Bearer invalid-regression-token' }]
  for (const headers of attempts) {
    const response = await request.get(`${apiURL}/admin/users`, { headers })
    expect(response.status()).toBe(401)
  }
})
