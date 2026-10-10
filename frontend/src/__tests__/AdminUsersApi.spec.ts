import { expect, it, vi } from 'vitest'
import { getAdminUsers } from '../services/adminUsers'
import apiClient from '../services/api'

vi.mock('../services/api', () => ({ default: { get: vi.fn() } }))

it('reads the application backend with only the requested page', async () => {
  vi.mocked(apiClient.get).mockResolvedValue({ data: { content: [], size: 20, number: 2, hasNext: false } })
  await getAdminUsers(2)
  expect(apiClient.get).toHaveBeenCalledWith('/admin/users', { params: { page: 2 } })
})
