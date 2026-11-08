import { afterEach, expect, it, vi } from 'vitest'
import apiClient, { searchMaterials, searchServices } from '../services/api'

vi.mock('../services/keycloak', () => ({
  default: { updateToken: vi.fn().mockResolvedValue(false), login: vi.fn() },
  getToken: () => null,
}))

const originalAdapter = apiClient.defaults.adapter

afterEach(() => {
  apiClient.defaults.adapter = originalAdapter
})

it('sends literal service and material picker queries as one query parameter', async () => {
  const requested: string[] = []
  apiClient.defaults.adapter = async config => {
    requested.push(apiClient.getUri(config))
    return { data: { content: [] }, status: 200, statusText: 'OK', headers: {}, config }
  }

  const query = 'Łódź & (a+b) #1'
  await searchServices(query)
  await searchMaterials(query)

  expect(requested).toHaveLength(2)
  for (const [index, path] of requested.entries()) {
    const url = new URL(path, 'http://localhost')
    expect(url.pathname.endsWith(index === 0 ? '/services' : '/materials')).toBe(true)
    expect([...url.searchParams.keys()]).toEqual(['query'])
    expect(url.searchParams.get('query')).toBe(query)
  }
})
