<template>
  <div class="card mb-3 shadow-sm">
    <div 
      class="card-header d-flex justify-content-between align-items-center cursor-pointer bg-light"
      @click="$emit('toggle')"
    >
      <div class="d-flex align-items-center gap-3">
        <i 
          class="bi transition-transform" 
          :class="isExpanded ? 'bi-chevron-down' : 'bi-chevron-right'"
        ></i>
        <h6 class="m-0 fw-bold">{{ stage.name || `${t('calculations.form.stage.index')} ${stageIndex + 1}` }}</h6>
        <span class="badge bg-secondary bg-opacity-10 text-secondary rounded-pill">
          {{ stage.totalAmount?.toFixed(2) }}
        </span>
      </div>
      <button type="button" class="btn btn-outline-danger btn-sm py-0 px-2" @click.stop="$emit('remove-stage')">
        <i class="bi bi-trash"></i>
      </button>
    </div>
    
    <div v-show="isExpanded" class="card-body p-3">
      <div class="mb-3">
        <label :for="`stage-name-${stageIndex}`" class="form-label small text-muted mb-1">{{
          t('calculations.form.stage.name')
        }}</label>
        <input
          type="text"
          :id="`stage-name-${stageIndex}`"
          class="form-control form-control-sm"
          :value="stage.name"
          @input="
            $emit('update:stage', { ...stage, name: ($event.target as HTMLInputElement).value })
          "
        />
      </div>

      <!-- Add Items Section -->
      <div class="row g-2 mb-3">
        <div class="col-md-6">
          <div class="input-group input-group-sm">
            <span class="input-group-text bg-light"><i class="bi bi-search"></i></span>
            <input
              type="text"
              class="form-control"
              :placeholder="t('calculations.form.add.service')"
              v-model="serviceSearchQuery"
              @input="handleServiceSearch"
            />
          </div>
          <ul class="list-group list-group-flush shadow-sm position-absolute w-100 start-0" style="z-index: 1000;" v-if="serviceSearchResults.length">
            <li
              class="list-group-item list-group-item-action cursor-pointer small"
              v-for="service in serviceSearchResults"
              :key="service.id || service.name"
              @click="selectService(service)"
            >
              <div class="d-flex justify-content-between">
                <span>{{ service.name }}</span>
                <span class="text-muted">{{ service.price }}</span>
              </div>
            </li>
          </ul>
        </div>
        <div class="col-md-6">
          <div class="input-group input-group-sm">
            <span class="input-group-text bg-light"><i class="bi bi-search"></i></span>
            <input
              type="text"
              class="form-control"
              :placeholder="t('calculations.form.add.material')"
              v-model="materialSearchQuery"
              @input="handleMaterialSearch"
            />
          </div>
          <ul class="list-group list-group-flush shadow-sm position-absolute w-100 start-0" style="z-index: 1000;" v-if="materialSearchResults.length">
            <li
              class="list-group-item list-group-item-action cursor-pointer small"
              v-for="material in materialSearchResults"
              :key="material.id || material.name"
              @click="selectMaterial(material)"
            >
              <div class="d-flex justify-content-between">
                <span>{{ material.name }}</span>
                <span class="text-muted">{{ material.price }}</span>
              </div>
            </li>
          </ul>
        </div>
      </div>

      <!-- Services Table -->
      <div v-if="stage.services.length > 0" class="mb-3">
        <h6 class="small text-uppercase text-muted fw-bold mb-2">{{ t('calculations.form.services') }}</h6>
        <div class="table-responsive">
          <table class="table table-sm table-hover align-middle mb-0">
            <thead class="table-light">
              <tr>
                <th style="width: 40%">{{ t('calculations.form.name') }}</th>
                <th style="width: 15%">{{ t('calculations.form.quantity') }}</th>
                <th style="width: 15%">{{ t('calculations.form.unit') }}</th>
                <th style="width: 20%">{{ t('calculations.form.price') }}</th>
                <th style="width: 10%"></th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(item, itemIndex) in stage.services" :key="itemIndex">
                <td>
                  <input
                    type="text"
                    class="form-control form-control-sm border-0 bg-transparent p-0"
                    v-model="item.name"
                  />
                </td>
                <td>
                  <input
                    type="number"
                    step="0.01"
                    class="form-control form-control-sm"
                    v-model="item.quantity"
                  />
                </td>
                <td>
                  <input
                    type="text"
                    class="form-control form-control-sm border-0 bg-transparent p-0"
                    v-model="item.unit"
                  />
                </td>
                <td>
                  <input
                    type="number"
                    step="0.01"
                    class="form-control form-control-sm"
                    v-model="item.unitPrice"
                  />
                </td>
                <td class="text-end">
                  <button type="button" class="btn btn-link text-danger p-0" @click="removeService(item)">
                    <i class="bi bi-x-lg"></i>
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Materials Table -->
      <div v-if="stage.materials.length > 0">
        <h6 class="small text-uppercase text-muted fw-bold mb-2">{{ t('calculations.form.materials') }}</h6>
        <div class="table-responsive">
          <table class="table table-sm table-hover align-middle mb-0">
            <thead class="table-light">
              <tr>
                <th style="width: 40%">{{ t('calculations.form.name') }}</th>
                <th style="width: 15%">{{ t('calculations.form.quantity') }}</th>
                <th style="width: 15%">{{ t('calculations.form.unit') }}</th>
                <th style="width: 20%">{{ t('calculations.form.price') }}</th>
                <th style="width: 10%"></th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(item, itemIndex) in stage.materials" :key="itemIndex">
                <td>
                  <input
                    type="text"
                    class="form-control form-control-sm border-0 bg-transparent p-0"
                    v-model="item.name"
                  />
                </td>
                <td>
                  <input
                    type="number"
                    step="0.01"
                    class="form-control form-control-sm"
                    v-model="item.quantity"
                  />
                </td>
                <td>
                  <input
                    type="text"
                    class="form-control form-control-sm border-0 bg-transparent p-0"
                    v-model="item.unit"
                  />
                </td>
                <td>
                  <input
                    type="number"
                    step="0.01"
                    class="form-control form-control-sm"
                    v-model="item.unitPrice"
                  />
                </td>
                <td class="text-end">
                  <button type="button" class="btn btn-link text-danger p-0" @click="removeMaterial(item)">
                    <i class="bi bi-x-lg"></i>
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ItemType, type CalculationStage, type CalculationItem } from '../models/calculation'
import { searchServices, searchMaterials } from '../services/api'
import type { Service } from '../models/service'
import type { Material } from '../models/material'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const props = defineProps<{ 
  stage: CalculationStage; 
  stageIndex: number;
  isExpanded: boolean;
}>()

const emit = defineEmits(['update:stage', 'remove-stage', 'toggle'])

const serviceSearchQuery = ref('')
const materialSearchQuery = ref('')
const serviceSearchResults = ref<Service[]>([])
const materialSearchResults = ref<Material[]>([])

const handleServiceSearch = async () => {
  if (serviceSearchQuery.value.length > 2) {
    const response = await searchServices(serviceSearchQuery.value)
    serviceSearchResults.value = response.data.content
  } else {
    serviceSearchResults.value = []
  }
}

const handleMaterialSearch = async () => {
  if (materialSearchQuery.value.length > 2) {
    const response = await searchMaterials(materialSearchQuery.value)
    materialSearchResults.value = response.data.content
  } else {
    materialSearchResults.value = []
  }
}

const selectService = (service: Service) => {
  const newServices = [
    ...props.stage.services,
    {
      name: service.name,
      quantity: 1,
      unit: service.workUnit,
      unitPrice: service.price,
      type: ItemType.SERVICE
    }
  ]
  emit('update:stage', { ...props.stage, services: newServices })
  serviceSearchQuery.value = ''
  serviceSearchResults.value = []
}

const selectMaterial = (material: Material) => {
  const newMaterials = [
    ...props.stage.materials,
    {
      name: material.name,
      quantity: 1,
      unit: material.unit,
      unitPrice: material.price,
      type: ItemType.MATERIAL
    }
  ]
  emit('update:stage', { ...props.stage, materials: newMaterials })
  materialSearchQuery.value = ''
  materialSearchResults.value = []
}

const removeService = (item: CalculationItem) => {
  const newServices = props.stage.services.filter((i) => i !== item)
  emit('update:stage', { ...props.stage, services: newServices })
}

const removeMaterial = (item: CalculationItem) => {
  const newMaterials = props.stage.materials.filter((i) => i !== item)
  emit('update:stage', { ...props.stage, materials: newMaterials })
}
</script>

<style scoped>
.cursor-pointer {
  cursor: pointer;
}

.transition-transform {
  transition: transform 0.2s ease;
}

.list-group-item:hover {
  background-color: var(--bg-body);
}
</style>
