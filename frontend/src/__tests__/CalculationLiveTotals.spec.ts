import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import { createI18n } from 'vue-i18n'
import CalculationForm from '../views/CalculationForm.vue'
import CalculationPrintView from '../views/CalculationPrintView.vue'
import { ItemType, type Calculation } from '../models/calculation'
import en from '../en.json'

const api = vi.hoisted(() => ({
  get: vi.fn(), getCalculationById: vi.fn(), updateCalculation: vi.fn(), searchMaterials: vi.fn(),
}))
vi.mock('../services/api', () => ({
  default: { get: api.get },
  getCalculationById: api.getCalculationById,
  createCalculation: vi.fn(),
  updateCalculation: api.updateCalculation,
  searchServices: vi.fn(),
  searchMaterials: api.searchMaterials,
}))
vi.mock('vue-router', () => ({ useRoute: () => ({ params: { id: 'calculation-1' } }) }))

const calculation = (): Calculation => ({
  id: 'calculation-1',
  name: 'Office quote',
  description: '',
  currency: 'USD',
  totalAmount: 65,
  stages: [
    {
      name: 'Preparation',
      services: [{ name: 'Design', quantity: 2, unit: 'HOUR', unitPrice: 12.5, type: ItemType.SERVICE }],
      materials: [{ name: 'Paint', quantity: 4, unit: 'LITER', unitPrice: 7.5, type: ItemType.MATERIAL }],
      servicesTotalAmount: 25,
      materialsTotalAmount: 30,
      totalAmount: 55,
    },
    {
      name: 'Finishing',
      services: [{ name: 'Cleanup', quantity: 1, unit: 'HOUR', unitPrice: 10, type: ItemType.SERVICE }],
      materials: [],
      servicesTotalAmount: 10,
      materialsTotalAmount: 0,
      totalAmount: 10,
    },
  ],
})

const render = async () => {
  const wrapper = mount(CalculationForm, {
    global: {
      plugins: [createI18n({ legacy: false, locale: 'en', messages: { en } })],
      stubs: { RouterLink: true },
    },
  })
  await flushPromises()
  return wrapper
}

const stageBadges = (element: Element) =>
  [...element.querySelectorAll('.card-header .badge')].map(badge => badge.textContent?.trim())

const calculationBadge = (element: Element) =>
  element.querySelector('.badge.bg-primary')?.textContent?.trim()

describe('live calculation totals', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    api.get.mockResolvedValue({ data: ['USD', 'PLN'] })
    api.getCalculationById.mockResolvedValue({ data: calculation() })
    api.searchMaterials.mockResolvedValue({ data: { content: [{ id: 'material-2', name: 'Tape', price: 2.5, unit: 'ROLL' }] } })
  })

  it('updates the overall badge from current stage values without changing other stages', async () => {
    const wrapper = await render()
    const firstQuantity = wrapper.findAll('table')[0]!.find('input[type="number"]')

    await firstQuantity.setValue('3')
    expect(stageBadges(wrapper.element)).toEqual(['67.50 USD', '10.00 USD'])
    expect(calculationBadge(wrapper.element)).toBe('77.50 USD')

    await wrapper.find('#calc-currency').setValue('PLN')
    expect(stageBadges(wrapper.element)).toEqual(['67.50 PLN', '10.00 PLN'])
    expect(calculationBadge(wrapper.element)).toBe('77.50 PLN')
    wrapper.unmount()
  })

  it('updates totals when items and stages are added or removed', async () => {
    const wrapper = await render()

    await wrapper.findAll('table')[0]!.find('tbody button').trigger('click')
    expect(stageBadges(wrapper.element)).toEqual(['30.00 USD', '10.00 USD'])
    expect(calculationBadge(wrapper.element)).toBe('40.00 USD')

    await wrapper.findAll('input[placeholder="Add Material"]')[0]!.setValue('Tape')
    await flushPromises()
    await wrapper.find('.list-group-item-action').trigger('click')
    expect(stageBadges(wrapper.element)).toEqual(['32.50 USD', '10.00 USD'])
    expect(calculationBadge(wrapper.element)).toBe('42.50 USD')

    await wrapper.find('button.btn-outline-primary').trigger('click')
    expect(stageBadges(wrapper.element)).toEqual(['32.50 USD', '10.00 USD', '0.00 USD'])
    expect(calculationBadge(wrapper.element)).toBe('42.50 USD')

    await wrapper.findAll('.card-header button')[1]!.trigger('click')
    expect(stageBadges(wrapper.element)).toEqual(['32.50 USD', '0.00 USD'])
    expect(calculationBadge(wrapper.element)).toBe('32.50 USD')

    await wrapper.findAll('.card-header button')[0]!.trigger('click')
    await wrapper.findAll('.card-header button')[0]!.trigger('click')
    expect(stageBadges(wrapper.element)).toEqual([])
    expect(calculationBadge(wrapper.element)).toBe('0.00 USD')
    wrapper.unmount()
  })

  it('shows a placeholder for the affected stage and calculation while an input is incomplete', async () => {
    const wrapper = await render()
    await wrapper.findAll('table')[0]!.find('input[type="number"]').setValue('')

    expect(stageBadges(wrapper.element)).toEqual(['—', '10.00 USD'])
    expect(calculationBadge(wrapper.element)).toBe('—')
    wrapper.unmount()
  })

  it('shows saved totals consistently after reopening the form and print view', async () => {
    const saved = calculation()
    saved.stages[0]!.services[0]!.quantity = 3
    saved.stages[0]!.servicesTotalAmount = 37.5
    saved.stages[0]!.totalAmount = 67.5
    saved.totalAmount = 77.5
    api.updateCalculation.mockResolvedValue({ data: saved })

    const editing = await render()
    await editing.findAll('table')[0]!.find('input[type="number"]').setValue('3')
    await editing.find('button.btn-primary').trigger('click')
    await flushPromises()
    expect(api.updateCalculation.mock.calls[0]?.[1].stages[0].services[0].quantity).toBe(3)
    editing.unmount()

    api.getCalculationById.mockResolvedValue({ data: saved })
    api.get.mockImplementation((path: string) => Promise.resolve({
      data: path === '/calculations/calculation-1' ? saved :
        path === '/user-settings' ? { footerText: '' } : ['USD', 'PLN'],
    }))
    const reopened = await render()
    const printed = mount(CalculationPrintView, {
      global: { plugins: [createI18n({ legacy: false, locale: 'en', messages: { en } })] },
    })
    await flushPromises()

    expect(stageBadges(reopened.element)).toEqual(['67.50 USD', '10.00 USD'])
    expect(calculationBadge(reopened.element)).toBe('77.50 USD')
    expect([...printed.element.querySelectorAll('.stage > .stage-total span')].map(span => span.textContent?.trim()))
      .toEqual(stageBadges(reopened.element))
    expect(printed.element.querySelector('.total')?.children[1]?.textContent?.trim())
      .toBe(calculationBadge(reopened.element))
    expect(printed.element.querySelector('table tbody tr')?.lastElementChild?.textContent?.trim())
      .toBe('37.50 USD')

    reopened.unmount()
    printed.unmount()
  })
})
