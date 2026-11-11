import { flushPromises, mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createI18n } from 'vue-i18n'
import MaterialEditView from '../views/MaterialEditView.vue'
import apiClient from '../services/api'
import en from '../en.json'
import pl from '../pl.json'

const route = vi.hoisted(() => ({ params: {} as Record<string, string> }))
const push = vi.hoisted(() => vi.fn())
vi.mock('vue-router', () => ({ useRoute: () => route, useRouter: () => ({ push }) }))
vi.mock('../services/api', () => ({ default: { get: vi.fn(), post: vi.fn(), put: vi.fn() } }))

const get = vi.mocked(apiClient.get)
const post = vi.mocked(apiClient.post)
const searchPath = '/materials/external-search/plants'
const sources = [{ id: 'ekatalog', label: 'e-Katalog Roślin' }, { id: 'atlas', label: 'Atlas Roślin' }]
const result = {
  id: 'result-1', title: 'Spiraea alba', excerpt: 'Białe kwiaty', sourceLabel: 'e-Katalog Roślin',
  sourceUrl: 'https://e-katalogroslin.pl/plants/1,tawula',
}
const draft = {
  name: 'Spiraea alba', description: 'x'.repeat(498) + '\nabc',
  sourceUrl: result.sourceUrl, imageUrl: 'https://e-katalogroslin.pl/photo.jpg',
}

function render(locale = 'en') {
  const view = mount(MaterialEditView, {
    global: {
      plugins: [createI18n({ legacy: false, locale, messages: { en, pl } })],
      stubs: { RouterLink: { template: '<a><slot /></a>' } },
    },
  })
  // jsdom does not implement the browser's modal methods.
  const dialog = view.get<HTMLDialogElement>('#material-online-dialog').element
  dialog.showModal = () => { dialog.open = true }
  dialog.close = () => { dialog.open = false }
  return view
}

describe('online material creation', () => {
  beforeEach(() => {
    route.params = {}
    vi.resetAllMocks()
    get.mockImplementation(async url => {
      if (url === '/materials/units') return { data: ['PIECE', 'KILOGRAM'] }
      if (url === searchPath + '/sources') return { data: sources }
      throw new Error('unexpected GET')
    })
  })

  it('prefills the create form, preserves overlength text for editing and confirms only once', async () => {
    post.mockResolvedValueOnce({ data: [result] }).mockResolvedValueOnce({ data: draft })
    const view = render()
    await flushPromises()
    await view.get('#material-online-toggle').trigger('click')
    await flushPromises()
    await view.get('#material-online-query').setValue('tawuła')
    await view.get('#online-material-search').trigger('submit')
    await flushPromises()
    expect(post).toHaveBeenCalledWith(searchPath + '/results', {
      query: 'tawuła', sourceIds: ['ekatalog', 'atlas'],
    }, { signal: expect.any(AbortSignal) })
    await view.get('dialog[open] #material-online-results button').trigger('click')
    await flushPromises()
    expect(view.find('dialog[open]').exists()).toBe(false)
    expect(view.get<HTMLInputElement>('#name').element.value).toBe('Spiraea alba')
    expect(view.get<HTMLTextAreaElement>('#description').element.value).toBe(draft.description)
    expect(view.get('#material-source a').attributes('href')).toBe(result.sourceUrl)
    expect(view.get('#material-source img').attributes('src')).toBe(draft.imageUrl)
    await view.get('#material-source img').trigger('error')
    expect(view.get('#material-source').text()).toContain(en.materials.online.imageMissing)
    await view.get('#price').setValue('12')
    await view.get('#category').setValue('Plants')
    await view.get('#material-form').trigger('submit')
    expect(post).toHaveBeenCalledTimes(2)

    const description = 'x'.repeat(498) + '\na'
    await view.get('#description').setValue(description)
    post.mockRejectedValueOnce(new Error('Save unavailable'))
    await view.get('#material-form').trigger('submit')
    await flushPromises()
    expect(view.get('[role=alert]').text()).toBe(en.materials.online.saveFailed)
    expect(view.get<HTMLTextAreaElement>('#description').element.value).toBe(description)
    expect(push).not.toHaveBeenCalled()

    let complete!: (value: unknown) => void
    post.mockReturnValueOnce(new Promise(resolve => { complete = resolve }) as ReturnType<typeof post>)
    await view.get('#material-form').trigger('submit')
    await view.get('#material-form').trigger('submit')
    expect(post).toHaveBeenCalledTimes(4)
    expect(post).toHaveBeenLastCalledWith('/materials', {
      name: 'Spiraea alba', description, price: 12, unit: 'PIECE', category: 'Plants',
      sourceUrl: draft.sourceUrl, imageUrl: draft.imageUrl,
    })
    complete({ data: { id: 'saved' } })
    await flushPromises()
    expect(push).toHaveBeenCalledWith('/materials')
  })

  it('keeps manual inputs when a selected page cannot be fetched', async () => {
    post.mockResolvedValueOnce({ data: [result] }).mockRejectedValueOnce({
      isAxiosError: true, response: { data: { error: 'UNSUPPORTED_PAGE' } },
    })
    const view = render('pl')
    await flushPromises()
    await view.get('#name').setValue('Mój materiał')
    await view.get('#material-online-toggle').trigger('click')
    await flushPromises()
    await view.get('#material-online-query').setValue('tawuła')
    await view.get('#online-material-search').trigger('submit')
    await flushPromises()
    await view.get('#material-online-results button').trigger('click')
    await flushPromises()
    expect(view.get('[role=alert]').text()).toBe(pl.materials.online.unsupported)
    expect(view.get<HTMLInputElement>('#name').element.value).toBe('Mój materiał')
    expect(post).toHaveBeenCalledTimes(2)
    expect(push).not.toHaveBeenCalled()
  })

  it('dismisses the modal during a fetch without applying a late result', async () => {
    post.mockResolvedValueOnce({ data: [result] })
    let complete!: (value: unknown) => void
    post.mockReturnValueOnce(new Promise(resolve => { complete = resolve }) as ReturnType<typeof post>)
    const view = render()
    await flushPromises()
    await view.get('#name').setValue('Manual material')
    await view.get('#material-online-toggle').trigger('click')
    await flushPromises()
    await view.get('#material-online-query').setValue('tawuła')
    await view.get('#online-material-search').trigger('submit')
    await flushPromises()
    await view.get('dialog[open] #material-online-results button').trigger('click')
    const signal = post.mock.calls[1]?.[2]?.signal
    await view.get('dialog[open]').trigger('cancel')
    expect(signal?.aborted).toBe(true)
    expect(view.find('dialog[open]').exists()).toBe(false)
    complete({ data: draft })
    await flushPromises()
    expect(view.get<HTMLInputElement>('#name').element.value).toBe('Manual material')
    expect(view.find('#material-source').exists()).toBe(false)

    await view.get('#material-online-toggle').trigger('click')
    await view.get('dialog[open] .btn-close').trigger('click')
    expect(view.find('dialog[open]').exists()).toBe(false)
  })
})
