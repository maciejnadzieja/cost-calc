import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import { createI18n } from 'vue-i18n'
import CalculationStage from '../components/CalculationStage.vue'
import { ItemType, type CalculationStage as CalculationStageModel } from '../models/calculation'
import en from '../en.json'

const stage = (): CalculationStageModel => ({
  name: 'Preparation',
  services: [{ name: 'Design', quantity: 2, unit: 'HOUR', unitPrice: 12.5, type: ItemType.SERVICE }],
  materials: [{ name: 'Paint', quantity: 4, unit: 'LITER', unitPrice: 7.5, type: ItemType.MATERIAL }],
  servicesTotalAmount: 25,
  materialsTotalAmount: 30,
  totalAmount: 55,
})

const render = (currency = 'USD') => mount(CalculationStage, {
  props: { stage: stage(), stageIndex: 0, isExpanded: true, currency },
  global: { plugins: [createI18n({ legacy: false, locale: 'en', messages: { en } })] },
})

const rowTotal = (element: Element, tableIndex: number) => {
  const table = element.querySelectorAll('table')[tableIndex] as HTMLTableElement | undefined
  return table?.tBodies[0]?.rows[0]?.cells[4]?.textContent?.trim()
}

describe('calculation item totals', () => {
  it('shows a total for both service and material rows', () => {
    const wrapper = render()
    const tables = wrapper.element.querySelectorAll('table')

    expect(tables).toHaveLength(2)
    expect(rowTotal(wrapper.element, 0)).toBe('25.00 USD')
    expect(rowTotal(wrapper.element, 1)).toBe('30.00 USD')
    wrapper.unmount()
  })

  it('updates the affected row as values and currency change', async () => {
    const wrapper = render()
    const [quantity, price] = wrapper.findAll('table')[0]!.findAll('input[type="number"]')

    await quantity!.setValue('3')
    expect(rowTotal(wrapper.element, 0)).toBe('37.50 USD')
    expect(rowTotal(wrapper.element, 1)).toBe('30.00 USD')

    await price!.setValue('7.25')
    await wrapper.setProps({ currency: 'PLN' })
    expect(rowTotal(wrapper.element, 0)).toBe('21.75 PLN')
    expect(rowTotal(wrapper.element, 1)).toBe('30.00 PLN')

    await quantity!.setValue('')
    expect(rowTotal(wrapper.element, 0)).toBe('—')
    wrapper.unmount()
  })

  it('does not show an infinite amount when numeric inputs overflow', () => {
    const overflowing = stage()
    overflowing.services[0]!.quantity = Number.MAX_VALUE
    overflowing.services[0]!.unitPrice = Number.MAX_VALUE
    const wrapper = mount(CalculationStage, {
      props: { stage: overflowing, stageIndex: 0, isExpanded: true, currency: 'USD' },
      global: { plugins: [createI18n({ legacy: false, locale: 'en', messages: { en } })] },
    })

    expect(rowTotal(wrapper.element, 0)).toBe('—')
    wrapper.unmount()
  })
})
