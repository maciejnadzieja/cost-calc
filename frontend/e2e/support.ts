import { expect, type Page, type APIRequestContext } from '@playwright/test'

export const apiURL = process.env.E2E_API_URL || 'http://localhost:8888/api'
export const keycloakURL = process.env.E2E_KEYCLOAK_URL || 'http://localhost:9090'
export const realm = 'cost-calc-realm'

export function assertLocal(url: string) {
  const parsed = new URL(url)
  if (!['localhost', '127.0.0.1', '[::1]', 'host.docker.internal'].includes(parsed.hostname)) {
    throw new Error('Regression tests require local endpoints.')
  }
}

export async function login(page: Page, role: 'admin' | 'tester') {
  assertLocal(apiURL)
  assertLocal(process.env.E2E_BASE_URL || 'http://localhost:5173')
  await page.addInitScript(() => localStorage.setItem('locale', 'en'))
  await page.goto('/materials')
  await expect(page.locator('#username')).toBeVisible()
  assertLocal(page.url())
  await page.locator('#username').fill(process.env[`E2E_${role.toUpperCase()}_USER`] || `${role}@cost-calc.eu`)
  await page.locator('#password').fill(process.env[`E2E_${role.toUpperCase()}_PASSWORD`] || `${role}-password`)
  const applicationRequest = page.waitForRequest(request =>
    request.url().startsWith(`${apiURL}/`) && !!request.headers()['authorization'])
  await page.locator('button[type="submit"], input[type="submit"]').click()
  await page.waitForURL('**/materials')
  const authorization = (await applicationRequest).headers()['authorization']!
  // Real browser-issued token stays in memory: no storageState, tracing or token logs.
  return authorization
}

export async function usersPage(request: APIRequestContext, authorization: string, page = 0) {
  const response = await request.get(`${apiURL}/admin/users?page=${page}`, { headers: { Authorization: authorization } })
  expect(response.status()).toBe(200)
  const result = await response.json()
  expect(result.size).toBe(20)
  expect(result.number).toBe(page)
  for (const user of result.content) {
    expect(Object.keys(user).sort()).toEqual(['email', 'enabled', 'username'])
  }
  return result as { content: { username: string; email: string | null; enabled: boolean }[]; hasNext: boolean }
}
