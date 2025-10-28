<template>
  <div>
    <h1>{{ $t('calculations.list.title') }}</h1>
    <router-link to="/calculations/new" class="btn btn-primary mb-3">{{
      $t('calculations.list.create_new')
    }}</router-link>

    <div v-if="calculations.length === 0" class="alert alert-info">
      {{ $t('calculations.list.no_calculations') }}
    </div>

    <ul class="list-group">
      <li
        v-for="calculation in calculations"
        :key="calculation.id"
        class="list-group-item d-flex justify-content-between align-items-center"
      >
        <router-link :to="`/calculations/${calculation.id}`">
          {{ calculation.name }} - {{ calculation.currency }}
        </router-link>
        <div>
          <router-link
            :to="`/calculations/edit/${calculation.id}`"
            class="btn btn-secondary btn-sm me-2"
          >
            {{ $t('common.edit') }}
          </router-link>
          <button
            class="btn btn-danger btn-sm"
            @click="deleteCalculation(calculation.id)"
          >
            {{ $t('common.delete') }}
          </button>
        </div>
      </li>
    </ul>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { getAllCalculations, deleteCalculation } from '../services/api'
import type { Calculation } from '../models/calculation'

const calculations = ref<Calculation[]>([])

onMounted(async () => {
  try {
    const response = await getAllCalculations()
    calculations.value = response.data
  } catch (error) {
    console.error('Error fetching calculations:', error)
  }
})

const deleteCalculation = async (id: string) => {
  try {
    await deleteCalculation(id)
    calculations.value = calculations.value.filter((calc) => calc.id !== id)
  } catch (error) {
    console.error('Error deleting calculation:', error)
  }
}</script>

<style scoped></style>
