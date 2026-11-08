import type { CalculationItem, CalculationStage } from './calculation'

type Decimal = { coefficient: bigint; scale: number }

const isValidItem = (item: CalculationItem) =>
  typeof item.quantity === 'number' && Number.isFinite(item.quantity) && item.quantity > 0 &&
  typeof item.unitPrice === 'number' && Number.isFinite(item.unitPrice) && item.unitPrice > 0

const decimal = (value: number): Decimal => {
  const [mantissa = '0', exponentText = '0'] = value.toString().split('e')
  const [whole = '0', fractional = ''] = mantissa.split('.')
  const scale = fractional.length - Number(exponentText)
  const coefficient = BigInt(whole + fractional)
  return scale < 0
    ? { coefficient: coefficient * 10n ** BigInt(-scale), scale: 0 }
    : { coefficient, scale }
}

const add = (left: Decimal, right: Decimal): Decimal => {
  const scale = Math.max(left.scale, right.scale)
  return {
    coefficient: left.coefficient * 10n ** BigInt(scale - left.scale) +
      right.coefficient * 10n ** BigInt(scale - right.scale),
    scale,
  }
}

const asNumber = (value: Decimal): number | null => {
  const number = Number(`${value.coefficient}e-${value.scale}`)
  return Number.isFinite(number) ? number : null
}

export const itemAmount = (item: CalculationItem): number | null => {
  if (!isValidItem(item)) return null
  const amount = item.quantity * item.unitPrice
  return Number.isFinite(amount) ? amount : null
}

const stageDecimalAmount = (stage: CalculationStage): Decimal | null => {
  let total: Decimal = { coefficient: 0n, scale: 0 }
  for (const item of [...stage.services, ...stage.materials]) {
    if (itemAmount(item) === null) return null
    const quantity = decimal(item.quantity)
    const unitPrice = decimal(item.unitPrice)
    total = add(total, {
      coefficient: quantity.coefficient * unitPrice.coefficient,
      scale: quantity.scale + unitPrice.scale,
    })
  }
  return total
}

export const stageAmount = (stage: CalculationStage): number | null => {
  const total = stageDecimalAmount(stage)
  return total === null ? null : asNumber(total)
}

export const calculationAmount = (stages: CalculationStage[]): number | null => {
  let total: Decimal = { coefficient: 0n, scale: 0 }
  for (const stage of stages) {
    const amount = stageDecimalAmount(stage)
    if (amount === null) return null
    total = add(total, amount)
  }
  return asNumber(total)
}

export const formatAmount = (amount: number | null, currency: string): string =>
  amount === null ? '—' : `${amount.toFixed(2)} ${currency}`
