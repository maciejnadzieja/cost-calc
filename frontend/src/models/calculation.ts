export interface Calculation {
  id?: string
  name: string
  description: string
  stages: CalculationStage[]
  currency: string
}

export interface CalculationStage {
  name: string
  items: CalculationItem[]
}

export interface CalculationItem {
  name: string
  quantity: number
  unit: string
  unitPrice: number
  type: 'service' | 'material'
}
