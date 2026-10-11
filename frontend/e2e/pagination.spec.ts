import { test, expect } from '@playwright/test'
import { login, usersPage } from './support'
import { seedPagination } from './pagination-fixture'

test('25 humans paginate as 20 + 5, excluding service accounts', async ({ page, request }) => {
  const fixture = await seedPagination(request)
  try {
    const token = await login(page, 'admin')
    const first = await usersPage(request, token, 0)
    const second = await usersPage(request, token, 1)
    expect(first.content).toHaveLength(20)
    expect(first.hasNext).toBe(true)
    expect(second.content).toHaveLength(5)
    expect(second.hasNext).toBe(false)
    const all = [...first.content, ...second.content]
    expect(new Set(all.map(user => user.username)).size).toBe(25)
    expect(all.map(user => user.username).sort()).toEqual([
      ...fixture.usernames, 'admin@cost-calc.eu', 'tester@cost-calc.eu', 'testowy@cost-calc.eu',
    ].sort())
    expect(all.find(user => user.username === fixture.usernames[0])).toMatchObject({ enabled: false, email: null })

    await page.goto('/admin/users')
    await expect(page.locator('tbody tr')).toHaveCount(20)
    await expect(page.locator('tbody tr td:first-child')).toHaveText(first.content.map(user => user.username))
    await expect(page.getByRole('button', { name: 'Previous', exact: true })).toBeDisabled()
    await page.getByRole('button', { name: 'Next', exact: true }).click()
    await expect(page.locator('tbody tr')).toHaveCount(5)
    await expect(page.locator('tbody tr td:first-child')).toHaveText(second.content.map(user => user.username))
    await expect(page.getByRole('button', { name: 'Next', exact: true })).toBeDisabled()
    await page.getByRole('button', { name: 'Previous', exact: true }).click()
    await expect(page.locator('tbody tr')).toHaveCount(20)
    await expect(page.locator('tbody tr td:first-child')).toHaveText(first.content.map(user => user.username))
  } finally {
    await fixture.cleanup()
  }
})
