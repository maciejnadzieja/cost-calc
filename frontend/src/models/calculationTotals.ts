import type { CalculationItem, CalculationStage } from './calculation'

export const itemAmount = (item: CalculationItem): number | null => {
  if (typeof item.quantity !== 'number' || !Number.isFinite(item.quantity) || item.quantity <= 0 ||
      typeof item.unitPrice !== 'number' || !Number.isFinite(item.unitPrice) || item.unitPrice <= 0) {
    return null
  }
  const amount = item.quantity * item.unitPrice
  return Number.isFinite(amount) ? amount : null
}

export const stageAmount = (stage: CalculationStage): number | null => {
  let total = 0
  for (const item of [...stage.services, ...stage.materials]) {
    const amount = itemAmount(item)
    if (amount === null) return null
    total += amount
    if (!Number.isFinite(total)) return null
  }
  return total
}

export const calculationAmount = (stages: CalculationStage[]): number | null => {
  let total = 0
  for (const stage of stages) {
    const amount = stageAmount(stage)
    if (amount === null) return null
    total += amount
    if (!Number.isFinite(total)) return null
  }
  return total
}

export const formatAmount = (amount: number | null, currency: string): string =>
  amount === null ? '—' : `${amount.toFixed(2)} ${currency}`
