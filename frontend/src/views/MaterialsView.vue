<template>
  <div>
    <h1>{{ $t('materials.title') }}</h1>
    <div class="input-group mb-3">
      <input type="text" class="form-control" v-model="query" @keyup.enter="searchMaterials">
      <button class="btn btn-primary" type="button" @click="searchMaterials">{{ $t('search') }}</button>
    </div>
    <ul class="list-group">
      <li class="list-group-item" v-for="material in materials" :key="material.id">
        {{ material.name }}
      </li>
    </ul>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import apiClient from '@/services/api';

const query = ref('');
const materials = ref([]);

const searchMaterials = async () => {
  try {
    const response = await apiClient.get('/materials/search', {
      params: {
        query: query.value,
      },
    });
    materials.value = response.data;
  } catch (error) {
    console.error(error);
  }
};
</script>

<style scoped>
</style>
