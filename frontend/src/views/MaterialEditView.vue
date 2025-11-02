<template>
  <div>
    <h1>{{ isEdit ? t('materials.edit.title') : t('materials.create.title') }}</h1>
    <form @submit.prevent="save">
      <div class="mb-3">
        <label for="name" class="form-label">{{ t('materials.form.name') }}</label>
        <input type="text" class="form-control" id="name" v-model="material.name" required>
      </div>
      <div class="mb-3">
        <label for="description" class="form-label">{{ t('materials.form.description') }}</label>
        <textarea class="form-control" id="description" v-model="material.description"></textarea>
      </div>
      <div class="mb-3">
        <label for="price" class="form-label">{{ t('materials.price') }}</label>
        <input type="number" step="0.01" class="form-control" id="price" v-model="material.price" required>
      </div>
      <div class="mb-3">
        <label for="unit" class="form-label">{{ t('materials.unit') }}</label>
        <select class="form-select" id="unit" v-model="material.unit" required>
          <option v-for="unit in materialUnits" :key="unit" :value="unit">{{ t('units.material.' + unit.toLowerCase() + '.label') }}</option>
        </select>
      </div>
      <div class="mb-3">
        <label for="category" class="form-label">{{ t('materials.category') }}</label>
        <input type="text" class="form-control" id="category" v-model="material.category" required>
      </div>
      <button type="submit" class="btn btn-primary">{{ t('common.save') }}</button>
      <router-link to="/materials" class="btn btn-secondary ms-2">{{ t('common.cancel') }}</router-link>
    </form>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import apiClient from '@/services/api';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

const route = useRoute();
const router = useRouter();

const material = ref({
  id: null,
  name: '',
  description: '',
  price: 0,
  unit: 'PIECE',
  category: '',
});

const materialUnits = ref(['PIECE', 'KILOGRAM', 'GRAM', 'METER', 'CENTIMETER', 'SQUARE_METER', 'LITER', 'MILLILITER', 'PACK', 'ROLL', 'TON']); // TODO: Fetch from backend

const isEdit = ref(false);

onMounted(async () => {
  const id = route.params.id;
  if (id) {
    isEdit.value = true;
    try {
      const response = await apiClient.get(`/materials/${id}`);
      material.value = response.data;
    } catch (error) {
      console.error(error);
    }
  }
});

const save = async () => {
  try {
    if (isEdit.value) {
      await apiClient.put(`/materials/${material.value.id}`, material.value);
    } else {
      await apiClient.post('/materials', material.value);
    }
    router.push('/materials');
  } catch (error) {
    console.error(error);
  }
};
</script>

<style scoped>
</style>
