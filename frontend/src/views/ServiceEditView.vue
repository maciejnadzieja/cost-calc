<template>
  <div>
    <h1>{{ isEdit ? t('services.edit.title') : t('services.create.title') }}</h1>
    <form @submit.prevent="save">
      <div class="mb-3">
        <label for="name" class="form-label">{{ t('services.form.name') }}</label>
        <input type="text" class="form-control" id="name" v-model="service.name" required>
      </div>
      <div class="mb-3">
        <label for="description" class="form-label">{{ t('services.form.description') }}</label>
        <textarea class="form-control" id="description" v-model="service.description"></textarea>
      </div>
      <div class="mb-3">
        <label for="price" class="form-label">{{ t('services.price') }}</label>
        <input type="number" step="0.01" class="form-control" id="price" v-model="service.price" required>
      </div>
      <div class="mb-3">
        <label for="workUnit" class="form-label">{{ t('services.workUnit') }}</label>
        <select class="form-select" id="workUnit" v-model="service.workUnit" required>
          <option v-for="unit in workUnits" :key="unit" :value="unit">{{ t('units.service.' + unit.toLowerCase() + '.label') }}</option>
        </select>
      </div>
      <button type="submit" class="btn btn-primary">{{ t('common.save') }}</button>
      <router-link to="/services" class="btn btn-secondary ms-2">{{ t('common.cancel') }}</router-link>
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

const service = ref({
  id: null,
  name: '',
  description: '',
  price: 0,
  workUnit: 'HOUR',
});

const workUnits = ref<string[]>([]);

const isEdit = ref(false);

onMounted(async () => {
  try {
    const response = await apiClient.get('/services/units');
    workUnits.value = response.data;
  } catch (error) {
    console.error(error);
  }

  const id = route.params.id;
  if (id) {
    isEdit.value = true;
    try {
      const response = await apiClient.get(`/services/${id}`);
      service.value = response.data;
    } catch (error) {
      console.error(error);
    }
  }
});

const save = async () => {
  try {
    if (isEdit.value) {
      await apiClient.put(`/services/${service.value.id}`, service.value);
    } else {
      await apiClient.post('/services', service.value);
    }
    router.push('/services');
  } catch (error) {
    console.error(error);
  }
};
</script>

<style scoped>
</style>
