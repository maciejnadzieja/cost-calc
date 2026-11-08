import { expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import { createI18n } from 'vue-i18n'
import CalculationStage from '../components/CalculationStage.vue'
import type { CalculationStage as CalculationStageModel } from '../models/calculation'
import en from '../en.json'

const search = vi.hoisted(() => ({ services: vi.fn(), materials: vi.fn() }))
vi.mock('../services/api', () => ({
  searchServices: search.services,
  searchMaterials: search.materials,
}))

const stage: CalculationStageModel = {
  name: 'Preparation', services: [], materials: [],
  servicesTotalAmount: 0, materialsTotalAmount: 0, totalAmount: 0,
}

it('keeps the three-character picker threshold and displays matching names', async () => {
  search.services.mockResolvedValue({ data: { content: [{ id: 's1', name: 'Łódź', price: 10, workUnit: 'HOUR' }] } })
  search.materials.mockResolvedValue({ data: { content: [{ id: 'm1', name: 'Café', price: 5, unit: 'PIECE' }] } })
  const wrapper = mount(CalculationStage, {
    props: { stage, stageIndex: 0, currency: 'PLN', isExpanded: true },
    global: { plugins: [createI18n({ legacy: false, locale: 'en', messages: { en } })] },
  })
  const [serviceInput, materialInput] = wrapper.findAll('.input-group input')

  await serviceInput!.setValue('lo')
  await materialInput!.setValue('ca')
  expect(search.services).not.toHaveBeenCalled()
  expect(search.materials).not.toHaveBeenCalled()

  await serviceInput!.setValue('lodz')
  await materialInput!.setValue('cafe')
  await flushPromises()
  expect(search.services).toHaveBeenCalledWith('lodz')
  expect(search.materials).toHaveBeenCalledWith('cafe')
  expect(wrapper.text()).toContain('Łódź')
  expect(wrapper.text()).toContain('Café')
  wrapper.unmount()
})
