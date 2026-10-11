import { randomUUID } from 'node:crypto'
import { expect, type APIRequestContext } from '@playwright/test'
import { assertLocal, keycloakURL, realm } from './support'

export async function seedPagination(request: APIRequestContext) {
  if (process.env.E2E_ALLOW_FIXTURES !== '1') {
    throw new Error('Pagination creates synthetic users. Set E2E_ALLOW_FIXTURES=1 to opt in on the local fixture realm.')
  }
  assertLocal(keycloakURL)
  const adminBase = `${keycloakURL}/admin/realms/${realm}`
  const prefix = `regression-${randomUUID()}-`
  const created: { id: string; username: string }[] = []
  async function headers() {
    const response = await request.post(`${keycloakURL}/realms/master/protocol/openid-connect/token`, {
      form: {
        grant_type: 'password', client_id: 'admin-cli',
        username: process.env.E2E_KEYCLOAK_ADMIN_USER || 'admin',
        password: process.env.E2E_KEYCLOAK_ADMIN_PASSWORD || 'admin',
      },
    })
    expect(response.status(), 'Local fixture administrator authentication').toBe(200)
    return { Authorization: `Bearer ${(await response.json()).access_token}` }
  }
  async function cleanup() {
    if (!created.length) return
    const authorization = await headers()
    const errors: string[] = []
    for (const account of created) {
      try {
        const existing = await request.get(`${adminBase}/users/${account.id}`, { headers: authorization })
        if (existing.status() === 404) continue
        if (existing.status() !== 200 || (await existing.json()).username !== account.username ||
            !account.username.startsWith(prefix)) throw new Error('Fixture ownership check failed')
        const result = await request.delete(`${adminBase}/users/${account.id}`, { headers: authorization })
        if (result.status() !== 204) throw new Error('Fixture deletion failed')
      } catch { errors.push(account.username) }
    }
    if (errors.length) throw new Error(`Could not clean up test-owned accounts: ${errors.join(', ')}`)
    const remaining = await request.get(`${adminBase}/users?search=${encodeURIComponent(prefix)}&max=100`, { headers: authorization })
    expect(remaining.status(), 'Verify fixture cleanup').toBe(200)
    expect((await remaining.json()).filter((user: { username: string }) => user.username.startsWith(prefix))).toHaveLength(0)
  }
  try {
    const authorization = await headers()
    const baseline = await request.get(`${adminBase}/users?first=0&max=100`, { headers: authorization })
    expect(baseline.status()).toBe(200)
    const existing = await baseline.json()
    expect(existing.map((user: { username: string }) => user.username).sort(),
      'Pagination requires the unmodified three-human fixture realm; no existing accounts are deleted').toEqual([
      'admin@cost-calc.eu', 'tester@cost-calc.eu', 'testowy@cost-calc.eu',
    ])
    for (let index = 0; index < 22; index++) {
      const username = `${prefix}${String(index).padStart(2, '0')}`
      const result = await request.post(`${adminBase}/users`, {
        headers: authorization,
        data: { username, enabled: index !== 0, ...(index === 0 ? {} : { email: `${username}@example.invalid` }) },
      })
      expect(result.status(), 'Synthetic account creation').toBe(201)
      const location = result.headers().location
      if (!location) throw new Error(`Missing ID for created fixture ${username}; manual cleanup may be required`)
      const id = new URL(location).pathname.split('/').pop()!
      created.push({ id, username })
    }
    return { usernames: created.map(user => user.username), cleanup }
  } catch (error) {
    await cleanup()
    throw error
  }
}
