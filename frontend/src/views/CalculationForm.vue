<template>
  <div>
    <h2>{{ calculation.id ? $t('calculations.edit.title') : $t('calculations.new.title') }}</h2>

    <form @submit.prevent="saveCalculation">
      <div
        v-if="calculationSaved"
        class="alert alert-success alert-dismissible fade show"
        role="alert"
      >
        <span>{{ $t('calculations.form.saved') }}</span>
      </div>

      <!-- Calculation Details will go here -->
      <div class="row mb-3">
        <div class="col-md-6">
          <label for="calc-name" class="form-label">{{ $t('calculations.form.name') }}</label>
          <input type="text" id="calc-name" class="form-control" v-model="calculation.name" />

          <label for="calc-currency" class="form-label">{{
            $t('calculations.form.currency')
          }}</label>
          <input
            type="text"
            id="calc-currency"
            class="form-control"
            v-model="calculation.currency"
          />
        </div>
        <div class="col-md-6">
          <label for="calc-description" class="form-label">{{
            $t('calculations.form.description')
          }}</label>
          <textarea
            id="calc-description"
            class="form-control"
            v-model="calculation.description"
          ></textarea>
        </div>
      </div>

      <!-- Calculation Stages will go here -->
      <div v-for="(stage, stageIndex) in calculation.stages" :key="stageIndex" class="card mb-3">
        <div class="card-header">
          <h5 class="card-title">{{ $t('calculations.form.stage.index') }} {{ stageIndex + 1 }}</h5>
          <button type="button" class="btn btn-danger btn-sm" @click="removeStage(stageIndex)">
            {{ $t('calculations.form.remove.stage') }}
          </button>
        </div>
        <div class="card-body">
          <div class="mb-3">
            <label :for="`stage-name-${stageIndex}`" class="form-label">{{
              $t('calculations.form.stage.name')
            }}</label>
            <input
              type="text"
              :id="`stage-name-${stageIndex}`"
              class="form-control"
              v-model="stage.name"
            />
          </div>

          <!-- Items will go here -->
          <div class="row">
            <div class="col-md-6">
              <div class="mb-3">
                <label class="form-label">{{ $t('calculations.form.add.service') }}</label>
                <div class="input-group">
                  <input
                    type="text"
                    class="form-control"
                    :placeholder="$t('common.search')"
                    v-model="stage.serviceSearchQuery"
                    @input="handleServiceSearch(stageIndex)"
                  />
                </div>
                <ul class="list-group" v-if="stage.serviceSearchResults.length">
                  <li
                    class="list-group-item"
                    v-for="service in stage.serviceSearchResults"
                    :key="service.id"
                    @click="selectService(stageIndex, service)"
                  >
                    {{ service.name }}
                  </li>
                </ul>
              </div>
            </div>
            <div class="col-md-6">
              <div class="mb-3">
                <label class="form-label">{{ $t('calculations.form.add.material') }}</label>
                <div class="input-group">
                  <input
                    type="text"
                    class="form-control"
                    :placeholder="$t('common.search')"
                    v-model="stage.materialSearchQuery"
                    @input="handleMaterialSearch(stageIndex)"
                  />
                </div>
                <ul class="list-group" v-if="stage.materialSearchResults.length">
                  <li
                    class="list-group-item"
                    v-for="material in stage.materialSearchResults"
                    :key="material.id"
                    @click="selectMaterial(stageIndex, material)"
                  >
                    {{ material.name }}
                  </li>
                </ul>
              </div>
            </div>
          </div>

          <h5>{{ $t('calculations.form.services') }}</h5>
          <table class="table">
            <thead>
              <tr>
                <th>{{ $t('calculations.form.name') }}</th>
                <th>{{ $t('calculations.form.quantity') }}</th>
                <th>{{ $t('calculations.form.unit') }}</th>
                <th>{{ $t('calculations.form.price') }}</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="(item, itemIndex) in stage.items.filter((i) => i.type === 'service')"
                :key="itemIndex"
              >
                <td><input type="text" class="form-control" v-model="item.name" /></td>
                <td><input type="number" class="form-control" v-model="item.quantity" /></td>
                <td><input type="text" class="form-control" v-model="item.unit" /></td>
                <td><input type="number" class="form-control" v-model="item.unitPrice" /></td>
                <td>
                  <button
                    type="button"
                    class="btn btn-danger btn-sm"
                    @click="removeItem(stageIndex, item)"
                  >
                    {{ $t('common.remove') }}
                  </button>
                </td>
              </tr>
            </tbody>
          </table>

          <h5>{{ $t('calculations.form.materials') }}</h5>
          <table class="table">
            <thead>
              <tr>
                <th>{{ $t('calculations.form.name') }}</th>
                <th>{{ $t('calculations.form.quantity') }}</th>
                <th>{{ $t('calculations.form.unit') }}</th>
                <th>{{ $t('calculations.form.price') }}</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="(item, itemIndex) in stage.items.filter((i) => i.type === 'material')"
                :key="itemIndex"
              >
                <td><input type="text" class="form-control" v-model="item.name" /></td>
                <td><input type="number" class="form-control" v-model="item.quantity" /></td>
                <td><input type="text" class="form-control" v-model="item.unit" /></td>
                <td><input type="number" class="form-control" v-model="item.unitPrice" /></td>
                <td>
                  <button
                    type="button"
                    class="btn btn-danger btn-sm"
                    @click="removeItem(stageIndex, item)"
                  >
                    {{ $t('common.remove') }}
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <button type="button" class="btn btn-secondary mb-3" @click="addStage">
        {{ $t('calculations.form.add.stage') }}
      </button>

      <div class="mb-3">
        <button type="submit" class="btn btn-primary">{{ $t('calculations.form.submit') }}</button>
        <router-link to="/calculations" class="btn btn-secondary">{{
          $t('common.cancel')
        }}</router-link>
      </div>
    </form>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import type { Calculation, CalculationStage, CalculationItem } from '../models/calculation'
import {
  searchServices,
  searchMaterials,
  createCalculation,
  updateCalculation,
  getCalculationById,
} from '../services/api'
import type { Service } from '../models/service'
import type { Material } from '../models/material'

interface Stage extends CalculationStage {
  serviceSearchQuery: string
  materialSearchQuery: string
  serviceSearchResults: Service[]
  materialSearchResults: Material[]
  selectedService: Service | null
  selectedMaterial: Material | null
}

const calculation = ref<Calculation>({
  id: undefined,
  name: '',
  description: '',
  stages: [],
  currency: 'USD',
})

const calculationSaved = ref(false)

const route = useRoute()

onMounted(async () => {
  if (route.params.id) {
    try {
      const response = await getCalculationById(route.params.id as string)
      calculation.value = {
        ...response.data,
        stages: response.data.stages.map((stage: any) => ({
          ...stage,
          items: [
            ...stage.services.map((s: any) => ({ ...s, type: 'service' })),
            ...stage.materials.map((m: any) => ({ ...m, type: 'material' })),
          ],
          serviceSearchQuery: '',
          materialSearchQuery: '',
          serviceSearchResults: [],
          materialSearchResults: [],
        })),
      };
    } catch (error) {
      console.error('Error fetching calculation:', error)
    }
  }
})

const addStage = () => {
  calculation.value.stages.push({
    name: `Stage ${calculation.value.stages.length + 1}`,
    items: [],
    serviceSearchQuery: '',
    materialSearchQuery: '',
    serviceSearchResults: [],
    materialSearchResults: [],
    selectedService: null,
    selectedMaterial: null,
  } as Stage)
}

const removeStage = (index: number) => {
  calculation.value.stages.splice(index, 1)
}

const removeItem = (stageIndex: number, item: CalculationItem) => {
  const stage = calculation.value.stages[stageIndex]
  const itemIndex = stage.items.indexOf(item)
  if (itemIndex > -1) {
    stage.items.splice(itemIndex, 1)
  }
}

const handleServiceSearch = async (stageIndex: number) => {
  const stage = calculation.value.stages[stageIndex] as Stage
  if (stage.serviceSearchQuery.length > 2) {
    const response = await searchServices(stage.serviceSearchQuery)
    stage.serviceSearchResults = response.data.content
  } else {
    stage.serviceSearchResults = []
  }
}

const handleMaterialSearch = async (stageIndex: number) => {
  const stage = calculation.value.stages[stageIndex] as Stage
  if (stage.materialSearchQuery.length > 2) {
    const response = await searchMaterials(stage.materialSearchQuery)
    stage.materialSearchResults = response.data.content
  } else {
    stage.materialSearchResults = []
  }
}

const selectService = (stageIndex: number, service: Service) => {
  const stage = calculation.value.stages[stageIndex] as Stage
  stage.items.push({
    name: service.name,
    quantity: 1,
    unit: service.workUnit,
    unitPrice: service.price,
    type: 'service',
  })
  stage.serviceSearchQuery = ''
  stage.serviceSearchResults = []
}

const selectMaterial = (stageIndex: number, material: Material) => {
  const stage = calculation.value.stages[stageIndex] as Stage
  stage.items.push({
    name: material.name,
    quantity: 1,
    unit: material.unit,
    unitPrice: material.price,
    type: 'material',
  })
  stage.materialSearchQuery = ''
  stage.materialSearchResults = []
}

const saveCalculation = async () => {
  try {
    const payload = {
      ...calculation.value,
      stages: calculation.value.stages.map(stage => ({
        ...stage,
        services: stage.items.filter(item => item.type === 'service'),
        materials: stage.items.filter(item => item.type === 'material'),
      })),
    };

    if (calculation.value.id) {
      await updateCalculation(calculation.value.id, payload);
    } else {
      await createCalculation(payload);
    }
    calculationSaved.value = true;
    setTimeout(() => {
      calculationSaved.value = false;
    }, 3000);
  } catch (error) {
    console.error("Error saving calculation:", error);
    // Optionally, handle error display to the user
  }
};
</script>

<style scoped></style>
