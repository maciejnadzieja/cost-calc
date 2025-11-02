export interface Calculation {
  id?: string
  name: string
  description: string
  stages: CalculationStage[]
  currency: string
  totalAmount: number
}

export interface CalculationStage {
  name: string
  services: CalculationItem[]
  materials: CalculationItem[]
  servicesTotalAmount: number
  materialsTotalAmount: number
  totalAmount: number
}

export enum ItemType {
  SERVICE = 'SERVICE',
  MATERIAL = 'MATERIAL',
}

export interface CalculationItem {
  name: string
  quantity: number
  unit: string
  unitPrice: number
  type: ItemType
}
