import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import { createI18n } from 'vue-i18n'
import ServicesView from '../views/ServicesView.vue'
import MaterialsView from '../views/MaterialsView.vue'
import CalculationListView from '../views/CalculationListView.vue'
import apiClient from '../services/api'
import en from '../en.json'
import pl from '../pl.json'

const route = vi.hoisted(() => ({ query: {} as Record<string, string> }))
const push = vi.hoisted(() => vi.fn())
vi.mock('vue-router', () => ({ useRoute: () => route, useRouter: () => ({ push }) }))
vi.mock('../services/api', () => ({ default: { get: vi.fn(), delete: vi.fn(), post: vi.fn() } }))

const get = vi.mocked(apiClient.get)
const remove = vi.mocked(apiClient.delete)
const post = vi.mocked(apiClient.post)
const deferred = () => {
  let resolve!: (value: unknown) => void
  let reject!: (reason: unknown) => void
  const promise = new Promise((yes, no) => { resolve = yes; reject = no })
  return { promise, resolve, reject }
}
const page = (content: unknown[], number = 0, totalPages = 1) => ({ data: { content, number, totalPages } })
const rows = {
  services: { id: '1', name: 'Paint walls', description: '', price: 20, workUnit: 'HOUR' },
  materials: { id: '1', name: 'Blue paint', description: '', price: 10, unit: 'LITER', category: 'Paint' },
  calculations: { id: '1', name: 'Room quote', description: '', stages: [], currency: 'PLN', totalAmount: 30 },
}
const cases = [
  { name: 'services', component: ServicesView, endpoint: '/services', row: rows.services, label: 'Paint walls' },
  { name: 'materials', component: MaterialsView, endpoint: '/materials', row: rows.materials, label: 'Blue paint' },
  { name: 'calculations', component: CalculationListView, endpoint: '/calculations', row: rows.calculations, label: 'Room quote' },
] as const

const render = (component: typeof ServicesView, locale = 'en') => mount(component, {
  global: {
    plugins: [createI18n({ legacy: false, locale, messages: { en, pl } })],
    stubs: { RouterLink: { template: '<a><slot /></a>' } },
  },
})

describe.each(cases)('$name list loading', ({ name, component, endpoint, row, label }) => {
  beforeEach(() => {
    vi.clearAllMocks()
    route.query = {}
    vi.stubGlobal('confirm', () => true)
  })

  it.each(['en', 'pl'])('shows localized pending, empty and failure states in %s', async locale => {
    const first = deferred()
    get.mockReturnValueOnce(first.promise as ReturnType<typeof get>)
    const wrapper = render(component, locale)
    await wrapper.vm.$nextTick()
    expect(wrapper.find('[aria-busy="true"]').exists()).toBe(true)
    expect(wrapper.find('[role="status"]').text()).toBe((locale === 'en' ? en : pl)[name].list.loading)
    expect(wrapper.text()).not.toContain((locale === 'en' ? en : pl)[name].list.empty)
    first.resolve(page([]))
    await flushPromises()
    expect(wrapper.find('[aria-busy="false"]').exists()).toBe(true)
    expect(wrapper.text()).toContain((locale === 'en' ? en : pl)[name].list.empty)

    const failed = deferred()
    get.mockReturnValueOnce(failed.promise as ReturnType<typeof get>)
    await wrapper.find('input[type="text"]').setValue('missing')
    await wrapper.find('button.btn-primary').trigger('click')
    expect(wrapper.find('[role="status"]').exists()).toBe(true)
    expect(wrapper.text()).not.toContain((locale === 'en' ? en : pl)[name].list.empty)
    failed.reject(new Error('private upstream detail'))
    await flushPromises()
    expect(wrapper.find('[role="alert"]').text()).toContain((locale === 'en' ? en : pl)[name].list.failure)
    expect(wrapper.text()).not.toContain('private upstream detail')
    expect(wrapper.find('[role="alert"] button').text()).toBe((locale === 'en' ? en : pl)[name].list.retry)
    wrapper.unmount()
  })

  it('hides old rows, guards overlapping requests and retries the failed search/page', async () => {
    get.mockResolvedValueOnce(page([row], 0, 2) as never)
    const wrapper = render(component)
    await flushPromises()
    expect(wrapper.text()).toContain(label)

    const failed = deferred()
    get.mockReturnValueOnce(failed.promise as ReturnType<typeof get>)
    await wrapper.find('input[type="text"]').setValue('wanted')
    await wrapper.find('button.btn-primary').trigger('click')
    expect(wrapper.text()).not.toContain(label)
    await wrapper.find('button.btn-primary').trigger('click')
    expect(get).toHaveBeenCalledTimes(2)
    failed.reject(new Error('private'))
    await flushPromises()
    await wrapper.find('input[type="text"]').setValue('different draft')
    const recovered = deferred()
    get.mockReturnValueOnce(recovered.promise as ReturnType<typeof get>)
    await wrapper.find('[role="alert"] button').trigger('click')
    expect(get).toHaveBeenLastCalledWith(endpoint, { params: { query: 'wanted', page: 0, size: 10 } })
    recovered.resolve(page([row]))
    await flushPromises()
    expect(wrapper.text()).toContain(label)
    wrapper.unmount()
  })

  it('retries a failed page with the original route search', async () => {
    route.query = { query: 'quoted', page: '2' }
    const failed = deferred()
    get.mockReturnValueOnce(failed.promise as ReturnType<typeof get>)
    const wrapper = render(component)
    expect(get).toHaveBeenLastCalledWith(endpoint, { params: { query: 'quoted', page: 2, size: 10 } })
    failed.reject(new Error('private'))
    await flushPromises()
    get.mockResolvedValueOnce(page([row], 2, 3) as never)
    await wrapper.find('[role="alert"] button').trigger('click')
    await flushPromises()
    expect(get).toHaveBeenLastCalledWith(endpoint, { params: { query: 'quoted', page: 2, size: 10 } })
    expect(wrapper.text()).toContain(label)
    wrapper.unmount()
  })

  it('shows loading for pagination and delete refresh', async () => {
    get.mockResolvedValueOnce(page([row], 0, 2) as never)
    const wrapper = render(component)
    await flushPromises()
    const next = deferred()
    get.mockReturnValueOnce(next.promise as ReturnType<typeof get>)
    await wrapper.findAll('nav a').at(-1)!.trigger('click')
    expect(wrapper.find('[role="status"]').exists()).toBe(true)
    next.resolve(page([row], 1, 2))
    await flushPromises()
    remove.mockResolvedValueOnce({} as never)
    const refresh = deferred()
    get.mockReturnValueOnce(refresh.promise as ReturnType<typeof get>)
    const deleteButton = wrapper.findAll('button').find(button => button.attributes('title') === en.common.delete || button.text() === en.common.delete)!
    await deleteButton.trigger('click')
    await flushPromises()
    expect(wrapper.find('[role="status"]').exists()).toBe(true)
    expect(get).toHaveBeenLastCalledWith(endpoint, { params: { query: '', page: 1, size: 10 } })
    refresh.resolve(page([], 1, 1))
    await flushPromises()
    if (name !== 'calculations') {
      expect(wrapper.find('nav a').exists()).toBe(true)
      get.mockResolvedValueOnce(page([row], 0, 1) as never)
      await wrapper.find('nav a').trigger('click')
      await flushPromises()
      expect(get).toHaveBeenLastCalledWith(endpoint, { params: { query: '', page: 0, size: 10 } })
      expect(wrapper.text()).toContain(label)
    }
    wrapper.unmount()
  })
})

it('shows loading while refreshing calculations after duplication', async () => {
  vi.clearAllMocks()
  get.mockResolvedValueOnce(page([rows.calculations]) as never)
  const wrapper = render(CalculationListView)
  await flushPromises()
  post.mockResolvedValueOnce({} as never)
  const refresh = deferred()
  get.mockReturnValueOnce(refresh.promise as ReturnType<typeof get>)
  await wrapper.find(`button[title="${en.common.duplicate}"]`).trigger('click')
  await flushPromises()
  expect(wrapper.find('[role="status"]').exists()).toBe(true)
  refresh.resolve(page([rows.calculations]))
  await flushPromises()
  wrapper.unmount()
})
