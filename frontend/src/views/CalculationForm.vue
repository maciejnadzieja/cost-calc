<template>
  <div>
    <h2>{{ calculation.id ? t('calculations.edit.title') : t('calculations.new.title') }}</h2>

    <form @submit.prevent="saveCalculation">
      <div
        v-if="calculationSaved"
        class="alert alert-success alert-dismissible fade show"
        role="alert"
      >
        <span>{{ t('calculations.form.saved') }}</span>
      </div>

      <!-- Calculation Details will go here -->
      <div class="row mb-3">
        <div class="col-md-6">
          <label for="calc-name" class="form-label">{{ t('calculations.form.name') }}</label>
          <input type="text" id="calc-name" class="form-control" v-model="calculation.name" />

          <label for="calc-currency" class="form-label">{{
            t('calculations.form.currency')
          }}</label>
          <select
            id="calc-currency"
            class="form-select"
            v-model="calculation.currency"
          >
            <option v-for="curr in currencies" :key="curr" :value="curr">{{ curr }}</option>
          </select>
        </div>
        <div class="col-md-6">
          <label for="calc-description" class="form-label">{{
            t('calculations.form.description')
          }}</label>
          <textarea
            id="calc-description"
            class="form-control"
            v-model="calculation.description"
          ></textarea>
        </div>
      </div>

      <!-- Calculation Stages will go here -->
      <calculation-stage
        v-for="(stage, stageIndex) in calculation.stages"
        :key="stageIndex"
        :stage="stage"
        :stage-index="stageIndex"
        @update:stage="updateStage(stageIndex, $event)"
        @remove-stage="removeStage(stageIndex)"
      ></calculation-stage>

      <button type="button" class="btn btn-secondary mb-3" @click="addStage">
        {{ t('calculations.form.add.stage') }}
      </button>

      <div class="mb-3">
        <button type="submit" class="btn btn-primary me-2">{{ t('calculations.form.submit') }}</button>
        <router-link to="/calculations" class="btn btn-secondary">{{
          t('common.cancel')
        }}</router-link>
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
}

const removeStage = (index: number) => {
  calculation.value.stages.splice(index, 1)
}

const updateStage = (index: number, updatedStage: CalculationStageType) => {
  calculation.value.stages[index] = updatedStage
}

const saveCalculation = async () => {
  try {
    const payload = calculation.value

    if (calculation.value.id) {
      await updateCalculation(calculation.value.id, payload)
    } else {
      await createCalculation(payload)
    }
    calculationSaved.value = true
    setTimeout(() => {
      calculationSaved.value = false
    }, 3000)
  } catch (error) {
    console.error('Error saving calculation:', error)
    // Optionally, handle error display to the user
  }
}
</script>

<style scoped></style>
