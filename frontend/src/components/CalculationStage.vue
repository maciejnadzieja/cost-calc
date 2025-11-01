<template>
  <div class="card mb-3">
    <div class="card-header">
      <h5 class="card-title">{{ $t('calculations.form.stage.index') }} {{ stageIndex + 1 }}</h5>
      <button type="button" class="btn btn-danger btn-sm" @click="$emit('remove-stage')">
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
          :value="stage.name"
          @input="$emit('update:stage', { ...stage, name: $event.target.value })"
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
                v-model="serviceSearchQuery"
                @input="handleServiceSearch"
              />
            </div>
            <ul class="list-group" v-if="serviceSearchResults.length">
              <li
                class="list-group-item"
                v-for="service in serviceSearchResults"
                :key="service.id"
                @click="selectService(service)"
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
                v-model="materialSearchQuery"
                @input="handleMaterialSearch"
              />
            </div>
            <ul class="list-group" v-if="materialSearchResults.length">
              <li
                class="list-group-item"
                v-for="material in materialSearchResults"
                :key="material.id"
                @click="selectMaterial(material)"
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
            v-for="(item, itemIndex) in stage.items.filter((i) => i.type === ItemType.SERVICE)"
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
                @click="removeItem(item)"
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
            v-for="(item, itemIndex) in stage.items.filter((i) => i.type === ItemType.MATERIAL)"
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
                @click="removeItem(item)"
              >
                {{ $t('common.remove') }}
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { ItemType, type CalculationStage, type CalculationItem } from '../models/calculation';
import { searchServices, searchMaterials } from '../services/api';
import type { Service } from '../models/service';
import type { Material } from '../models/material';

const props = defineProps<{ stage: CalculationStage; stageIndex: number }>();
const emit = defineEmits(['update:stage', 'remove-stage']);

const serviceSearchQuery = ref('');
const materialSearchQuery = ref('');
const serviceSearchResults = ref<Service[]>([]);
const materialSearchResults = ref<Material[]>([]);

const handleServiceSearch = async () => {
  if (serviceSearchQuery.value.length > 2) {
    const response = await searchServices(serviceSearchQuery.value);
    serviceSearchResults.value = response.data.content;
  } else {
    serviceSearchResults.value = [];
  }
};

const handleMaterialSearch = async () => {
  if (materialSearchQuery.value.length > 2) {
    const response = await searchMaterials(materialSearchQuery.value);
    materialSearchResults.value = response.data.content;
  } else {
    materialSearchResults.value = [];
  }
};

const selectService = (service: Service) => {
  const newItems = [...props.stage.items, {
    name: service.name,
    quantity: 1,
    unit: service.workUnit,
    unitPrice: service.price,
    type: ItemType.SERVICE,
  }];
  emit('update:stage', { ...props.stage, items: newItems });
  serviceSearchQuery.value = '';
  serviceSearchResults.value = [];
};

const selectMaterial = (material: Material) => {
  const newItems = [...props.stage.items, {
    name: material.name,
    quantity: 1,
    unit: material.unit,
    unitPrice: material.price,
    type: ItemType.MATERIAL,
  }];
  emit('update:stage', { ...props.stage, items: newItems });
  materialSearchQuery.value = '';
  materialSearchResults.value = [];
};

const removeItem = (item: CalculationItem) => {
  const newItems = props.stage.items.filter(i => i !== item);
  emit('update:stage', { ...props.stage, items: newItems });
};
</script>

<style scoped></style>
