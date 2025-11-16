<template>
  <div class="container my-4">
    <div v-if="loading" class="text-center">
      <div class="spinner-border" role="status">
        <span class="visually-hidden">{{ t('calculations.print.loading') }}</span>
      </div>
      <p>{{ t('calculations.print.loadingCalculation') }}</p>
    </div>

    <div v-else-if="calculation">
      <!-- Calculation header -->
      <div class="calculation-header">
        <h1>{{ calculation.name }}</h1>
        <p class="text-muted">{{ calculation.description }}</p>
      </div>

      <!-- Stages -->
      <div v-for="(stage, index) in calculation.stages" :key="index" class="stage">
        <h2 class="stage-title">{{ stage.name }}</h2>

        <!-- Services -->
        <div v-if="stage.services && stage.services.length" class="mb-4">
          <h3>{{ t('calculations.print.services') }}</h3>
          <table class="table table-bordered">
            <thead>
              <tr>
                <th>{{ t('calculations.print.item') }}</th>
                <th>{{ t('calculations.print.quantity') }}</th>
                <th>{{ t('calculations.print.unit') }}</th>
                <th>{{ t('calculations.print.unit_price') }}</th>
                <th>{{ t('calculations.print.total') }}</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(service, index) in stage.services" :key="index">
                <td>{{ service.name }}</td>
                <td>{{ service.quantity }}</td>
                <td>{{ service.unit }}</td>
                <td>{{ formatCurrency(service.unitPrice) }}</td>
                <td>{{ formatCurrency(service.quantity * service.unitPrice) }}</td>
              </tr>
              <tr class="stage-total">
                <td colspan="4">{{ t('calculations.print.servicesTotal') }}</td>
                <td>{{ formatCurrency(stage.servicesTotalAmount) }}</td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Materials -->
        <div v-if="stage.materials && stage.materials.length" class="mb-4">
          <h3>{{ t('calculations.print.materials') }}</h3>
          <table class="table table-bordered">
            <thead>
              <tr>
                <th>{{ t('calculations.print.item') }}</th>
                <th>{{ t('calculations.print.quantity') }}</th>
                <th>{{ t('calculations.print.unit') }}</th>
                <th>{{ t('calculations.print.unit_price') }}</th>
                <th>{{ t('calculations.print.total') }}</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(material, index) in stage.materials" :key="index">
                <td>{{ material.name }}</td>
                <td>{{ material.quantity }}</td>
                <td>{{ material.unit }}</td>
                <td>{{ formatCurrency(material.unitPrice) }}</td>
                <td>{{ formatCurrency(material.quantity * material.unitPrice) }}</td>
              </tr>
              <tr class="stage-total">
                <td colspan="4">{{ t('calculations.print.materialsTotal') }}</td>
                <td>{{ formatCurrency(stage.materialsTotalAmount) }}</td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Stage total -->
        <div class="stage-total p-2 mb-4">
          <strong>{{ t('calculations.print.stage_total') }}</strong>:
          <span>{{ formatCurrency(stage.totalAmount) }}</span>
        </div>
      </div>

      <!-- Calculation total -->
      <div class="total">
        <span>{{ t('calculations.print.total') }}</span>:
        <span>{{ formatCurrency(calculation.totalAmount) }}</span>
      </div>

      <!-- Footer -->
      <div v-if="userSettings && userSettings.footerText" class="mt-5 pt-4 border-top">
        <div class="text-muted small">
          <pre>{{ userSettings.footerText }}</pre>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import api from '@/services/api'
import type { Calculation } from '@/models/calculation'
import type { UserSettings } from '@/models/userSettings'

import { useI18n } from 'vue-i18n'
const { t } = useI18n()

const route = useRoute()
const calculation = ref<Calculation | null>(null)
const userSettings = ref<UserSettings | null>(null)
const loading = ref(true)

const formatCurrency = (value: number) => {
  if (value == null) {
    return ''
  }
  if (!calculation.value) return ''
  return `${value.toFixed(2)} ${calculation.value.currency}`
}

onMounted(async () => {
  const id = route.params.id as string
  try {
    calculation.value = (await api.get<Calculation>(`/calculations/${id}`)).data
    userSettings.value = (await api.get<UserSettings>('/user-settings')).data
  } catch (error) {
    console.error('Error fetching calculation data:', error)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
@media print {
  .no-print {
    display: none;
  }
  body {
    padding: 0;
    margin: 0;
  }
  .container {
    max-width: 100%;
    width: 100%;
    padding: 0;
    margin: 0;
  }
  .table {
    font-size: 12px;
  }
  .stage-total {
    font-weight: bold;
    background-color: #f8f9fa !important;
    -webkit-print-color-adjust: exact;
    print-color-adjust: exact;
  }
}
.logo {
  max-width: 200px;
  height: auto;
  margin-bottom: 1rem;
}
.calculation-header {
  margin-bottom: 2rem;
}
.stage {
  margin-bottom: 2rem;
}
.stage-title {
  color: #198754;
  border-bottom: 2px solid #198754;
  margin-bottom: 1rem;
  padding-bottom: 0.5rem;
}
.table th {
  background-color: #e9ecef;
  -webkit-print-color-adjust: exact;
  print-color-adjust: exact;
}
.total {
  font-size: 1.2em;
  font-weight: bold;
  margin-top: 2rem;
  text-align: right;
}
</style>
