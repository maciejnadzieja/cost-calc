<template>
  <div class="calculation-form">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <div class="d-flex align-items-center gap-3">
        <h2 class="h3 mb-0">{{ calculation.id ? t('calculations.edit.title') : t('calculations.new.title') }}</h2>
        <span class="badge bg-primary fs-6" v-if="calculation.totalAmount">
          {{ calculation.totalAmount.toFixed(2) }} {{ calculation.currency }}
        </span>
      </div>
      <div class="d-flex gap-2">
        <router-link to="/calculations" class="btn btn-outline-secondary">
          {{ t('common.cancel') }}
        </router-link>
        <button type="button" class="btn btn-primary" @click="saveCalculation">
          <i class="bi bi-save me-2"></i>{{ t('calculations.form.submit') }}
        </button>
      </div>
    </div>

    <form @submit.prevent="saveCalculation">
      <div
        v-if="calculationSaved"
        class="alert alert-success alert-dismissible fade show"
        role="alert"
      >
        <span>{{ t('calculations.form.saved') }}</span>
      </div>

      <!-- Calculation Details -->
      <div class="card border-0 shadow-sm mb-4">
        <div class="card-body p-4">
          <div class="row g-3">
            <div class="col-md-6">
              <label for="calc-name" class="form-label small text-muted">{{ t('calculations.form.name') }}</label>
              <input type="text" id="calc-name" class="form-control" v-model="calculation.name" />
            </div>
            <div class="col-md-6">
              <label for="calc-currency" class="form-label small text-muted">{{ t('calculations.form.currency') }}</label>
              <select id="calc-currency" class="form-select" v-model="calculation.currency">
                <option v-for="curr in currencies" :key="curr" :value="curr">{{ curr }}</option>
              </select>
            </div>
            <div class="col-12">
              <label for="calc-description" class="form-label small text-muted">{{ t('calculations.form.description') }}</label>
              <textarea
                id="calc-description"
                class="form-control"
                rows="2"
                v-model="calculation.description"
              ></textarea>
            </div>
          </div>
        </div>
      </div>

      <!-- Calculation Stages -->
      <div class="mb-4">
        <div class="d-flex justify-content-between align-items-center mb-3">
          <h5 class="mb-0 text-muted">{{ t('calculations.form.stage.stages') }}</h5>
          <button type="button" class="btn btn-outline-primary btn-sm" @click="addStage">
            <i class="bi bi-plus-lg me-2"></i>{{ t('calculations.form.add.stage') }}
          </button>
        </div>

        <calculation-stage
          v-for="(stage, stageIndex) in calculation.stages"
          :key="`${stageIndex}-${stage.totalAmount}`"
          :stage="stage"
          :stage-index="stageIndex"
          :is-expanded="expandedStages.includes(stageIndex)"
          @update:stage="updateStage(stageIndex, $event)"
          @remove-stage="removeStage(stageIndex)"
          @toggle="toggleStage(stageIndex)"
        ></calculation-stage>
        
        <div v-if="calculation.stages.length === 0" class="text-center py-5 text-muted border rounded-3 bg-light">
          <i class="bi bi-layers fs-1 d-block mb-2"></i>
          <p class="mb-0">No stages added yet. Click "Add Stage" to begin.</p>
        </div>
      </div>
    </form>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import {
  type Calculation,
  type CalculationStage as CalculationStageType
} from '../models/calculation'
import {
  createCalculation,
  getCalculationById,
  updateCalculation
} from '../services/api'
import CalculationStage from '@/components/CalculationStage.vue'
import { useI18n } from 'vue-i18n'
import apiClient from '@/services/api'

const { t } = useI18n()

const calculation = ref<Calculation>({
  id: undefined,
  name: '',
  description: '',
  stages: [],
  currency: 'USD',
  totalAmount: 0
})

const currencies = ref<string[]>([])
const calculationSaved = ref(false)
const expandedStages = ref<number[]>([0]) // Default expand first stage

const route = useRoute()

onMounted(async () => {
  try {
    const response = await apiClient.get('/calculations/currencies')
    currencies.value = response.data
  } catch (error) {
    console.error('Error fetching currencies:', error)
  }

  if (route.params.id) {
    try {
      const response = await getCalculationById(route.params.id as string)
      calculation.value = response.data
      // Expand all stages on edit for better visibility initially, or just the first one
      expandedStages.value = calculation.value.stages.map((_, index) => index)
    } catch (error) {
      console.error('Error fetching calculation:', error)
    }
  }
})

const addStage = () => {
  calculation.value.stages.push({
    name: `Stage ${calculation.value.stages.length + 1}`,
    services: [],
    materials: [],
    servicesTotalAmount: 0,
    materialsTotalAmount: 0,
    totalAmount: 0
  } as CalculationStageType)
  
  // Expand the new stage
  expandedStages.value = [calculation.value.stages.length - 1]
}

const removeStage = (index: number) => {
  calculation.value.stages.splice(index, 1)
  // Adjust expanded indices
  expandedStages.value = expandedStages.value
    .filter(i => i !== index)
    .map(i => i > index ? i - 1 : i)
}

const updateStage = (index: number, updatedStage: CalculationStageType) => {
  calculation.value.stages[index] = updatedStage
}

const toggleStage = (index: number) => {
  if (expandedStages.value.includes(index)) {
    expandedStages.value = expandedStages.value.filter(i => i !== index)
  } else {
    expandedStages.value.push(index)
  }
}

const saveCalculation = async () => {
  try {
    const payload = calculation.value
    let response

    if (calculation.value.id) {
      response = await updateCalculation(calculation.value.id, payload)
    } else {
      response = await createCalculation(payload)
    }
    
    calculation.value = response.data
    
    calculationSaved.value = true
    setTimeout(() => {
      calculationSaved.value = false
    }, 3000)
  } catch (error) {
    console.error('Error saving calculation:', error)
  }
}
</script>

<style scoped>
.calculation-form {
  max-width: 1000px;
  margin: 0 auto;
}
</style>
