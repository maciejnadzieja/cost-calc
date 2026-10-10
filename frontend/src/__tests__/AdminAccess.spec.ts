import { beforeEach, describe, expect, it, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createI18n } from 'vue-i18n'
import { type RouteLocationNormalized } from 'vue-router'
import { isRealmAdmin } from '../services/adminAccess'
import router, { navigationGuard } from '../router'
import Sidebar from '../components/Sidebar.vue'
import en from '../en.json'

const auth = vi.hoisted(() => ({
  authenticated: true, realmRoles: [] as string[], clientRoles: ['ADMIN'] as string[], login: vi.fn(),
}))
vi.mock('../services/keycloak', () => ({
  default: {
    get authenticated() { return auth.authenticated },
    hasRealmRole: (role: string) => auth.realmRoles.includes(role),
    hasResourceRole: (role: string) => auth.clientRoles.includes(role),
    login: auth.login,
  },
}))

describe('Admin access', () => {
  beforeEach(() => {
    auth.authenticated = true
    auth.realmRoles = []
    vi.clearAllMocks()
  })

  it('admits realm ADMIN and rejects client-only ADMIN', () => {
    expect(isRealmAdmin()).toBe(false)
    auth.realmRoles = ['ADMIN']
    expect(isRealmAdmin()).toBe(true)
    auth.authenticated = false
    expect(isRealmAdmin()).toBe(false)
  })

  it('guards the real direct admin route with realm-role metadata', async () => {
    await router.push('/admin/users')
    expect(router.currentRoute.value.name).toBe('access-denied')
    auth.realmRoles = ['ADMIN']
    await router.push('/admin/users')
    expect(router.currentRoute.value.name).toBe('admin-users')
  })

  it('initiates login and cancels an unauthenticated direct route', () => {
    auth.authenticated = false
    const target = router.resolve('/admin/users') as RouteLocationNormalized
    expect(navigationGuard(target)).toBe(false)
    expect(auth.login).toHaveBeenCalledOnce()
  })

  it.each([false, true])('admin navigation visibility for realm ADMIN=%s', (admin) => {
    auth.realmRoles = admin ? ['ADMIN'] : []
    const wrapper = mount(Sidebar, { global: {
      plugins: [router, createI18n({ legacy: false, locale: 'en', messages: { en } })],
    } })
    expect(wrapper.find('a[href="/admin/users"]').exists()).toBe(admin)
    wrapper.unmount()
  })
})
