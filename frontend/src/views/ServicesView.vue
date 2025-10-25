<template>
  <div>
    <h1>{{ $t('services.title') }}</h1>
    <div class="input-group mb-3">
      <input type="text" class="form-control" v-model="query" @keyup.enter="searchServices">
      <button class="btn btn-primary" type="button" @click="searchServices">{{ $t('search') }}</button>
    </div>
    <ul class="list-group">
      <li class="list-group-item" v-for="service in services" :key="service.id">
        {{ service.name }}
      </li>
    </ul>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import apiClient from '@/services/api';

const query = ref('');
const services = ref([]);

const searchServices = async () => {
  try {
    const response = await apiClient.get('/services/search', {
      params: {
        query: query.value,
      },
    });
    services.value = response.data;
  } catch (error) {
    console.error(error);
  }
};
</script>

<style scoped>
</style>
