import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import { createI18n } from 'vue-i18n'
import { AxiosError, type AxiosResponse } from 'axios'
import AdminUsersView from '../views/AdminUsersView.vue'
import { getAdminUsers, type AdminUserPage } from '../services/adminUsers'
import en from '../en.json'

const auth = vi.hoisted(() => ({ authenticated: true, realmRoles: ['ADMIN'] as string[] }))
vi.mock('../services/keycloak', () => ({
  default: { get authenticated() { return auth.authenticated }, hasRealmRole: (role: string) => auth.realmRoles.includes(role) },
}))
vi.mock('../services/adminUsers', () => ({ getAdminUsers: vi.fn() }))

const page = (number = 0, hasNext = false, empty = false): AxiosResponse<AdminUserPage> => ({
  data: {
    content: empty ? [] : [{ username: `person-${number}`, email: null, enabled: number === 0 }],
    number, hasNext, size: 20,
  },
} as AxiosResponse<AdminUserPage>)

const render = () => mount(AdminUsersView, {
  global: { plugins: [createI18n({ legacy: false, locale: 'en', messages: { en } })] },
})
const api = vi.mocked(getAdminUsers)

describe('Admin users', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    auth.authenticated = true
    auth.realmRoles = ['ADMIN']
  })

  it('shows columns, missing-email placeholder, enabled status and human pagination', async () => {
    api.mockResolvedValueOnce(page(0, true)).mockResolvedValueOnce(page(1)).mockResolvedValueOnce(page(0, true))
    const wrapper = render()
    await flushPromises()
    expect(wrapper.findAll('th').map(cell => cell.text())).toEqual(['Username', 'Email', 'Enabled'])
    expect(wrapper.findAll('tbody td').map(cell => cell.text())).toEqual(['person-0', '—', 'Yes'])
    const [previous, next] = wrapper.findAll('nav button')
    expect(previous!.attributes('disabled')).toBeDefined()
    await next!.trigger('click')
    await flushPromises()
    expect(api).toHaveBeenLastCalledWith(1)
    expect(wrapper.find('tbody').text()).toContain('No')
    expect(next!.attributes('disabled')).toBeDefined()
    await previous!.trigger('click')
    await flushPromises()
    expect(api).toHaveBeenLastCalledWith(0)
    wrapper.unmount()
  })

  it('distinguishes loading from empty and disables navigation while reading', async () => {
    let resolve!: (value: AxiosResponse<AdminUserPage>) => void
    api.mockReturnValueOnce(new Promise(done => { resolve = done }))
    const wrapper = render()
    await wrapper.vm.$nextTick()
    expect(wrapper.find('[role="status"]').text()).toBe('Loading users…')
    expect(wrapper.findAll('nav button').every(button => button.attributes('disabled') !== undefined)).toBe(true)
    resolve(page(0, false, true))
    await flushPromises()
    expect(wrapper.find('[role="status"]').text()).toBe('No users found on this page.')
    expect(wrapper.find('table').exists()).toBe(false)
    wrapper.unmount()
  })

  it('retries the failed requested page and hides raw errors', async () => {
    api.mockResolvedValueOnce(page(0, true)).mockRejectedValueOnce(new Error('sensitive upstream details')).mockResolvedValueOnce(page(1))
    const wrapper = render()
    await flushPromises()
    await wrapper.findAll('nav button')[1]!.trigger('click')
    await flushPromises()
    expect(wrapper.find('[role="alert"]').text()).toContain('Users could not be loaded')
    expect(wrapper.text()).not.toContain('sensitive')
    await wrapper.find('[role="alert"] button').trigger('click')
    await flushPromises()
    expect(api).toHaveBeenLastCalledWith(1)
    expect(wrapper.text()).toContain('person-1')
    wrapper.unmount()
  })

  it.each([401, 403])('shows denied state for HTTP %s without retry', async (status) => {
    api.mockRejectedValueOnce(new AxiosError('sensitive', undefined, undefined, undefined, { status } as AxiosResponse))
    const wrapper = render()
    await flushPromises()
    expect(wrapper.find('[role="alert"]').text()).toBe('You do not have access to this page.')
    expect(wrapper.find('button').exists()).toBe(false)
    wrapper.unmount()
  })

  it('never reads for a user without realm ADMIN', async () => {
    auth.realmRoles = []
    const wrapper = render()
    await flushPromises()
    expect(api).not.toHaveBeenCalled()
    expect(wrapper.find('[role="alert"]').text()).toBe('You do not have access to this page.')
    wrapper.unmount()
  })
})
