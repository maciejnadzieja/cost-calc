<template>
  <div>
    <h1>{{ $t('materials.title') }}</h1>
    <div class="input-group mb-3">
      <input type="text" class="form-control" v-model="query" @keyup.enter="search">
      <button class="btn btn-primary" type="button" @click="search">{{ $t('search') }}</button>
    </div>
    <ul class="list-group">
      <li class="list-group-item" v-for="material in materials" :key="material.id">
        {{ material.name }}
      </li>
    </ul>
    <nav aria-label="Page navigation">
      <ul class="pagination">
        <li class="page-item" :class="{ disabled: currentPage === 0 }">
          <a class="page-link" href="#" @click.prevent="fetchMaterials(currentPage - 1)">{{ $t('previous') }}</a>
        </li>
        <li class="page-item" v-for="page in totalPages" :key="page" :class="{ active: currentPage === page - 1 }">
          <a class="page-link" href="#" @click.prevent="fetchMaterials(page - 1)">{{ page }}</a>
        </li>
        <li class="page-item" :class="{ disabled: currentPage === totalPages - 1 }">
          <a class="page-link" href="#" @click.prevent="fetchMaterials(currentPage + 1)">{{ $t('next') }}</a>
        </li>
      </ul>
    </nav>
  </div>
</template>

<script setup lang="ts">
import {onMounted, ref} from 'vue';
import apiClient from '@/services/api';

const query = ref('');
const materials = ref([]);
const currentPage = ref(0);
const totalPages = ref(0);

const fetchMaterials = async (page = 0) => {
  try {
    const response = await apiClient.get('/materials', {
      params: {
        query: query.value,
        page: page,
        size: 10,
      },
    });
    materials.value = response.data.content;
    currentPage.value = response.data.number;
    totalPages.value = response.data.totalPages;
  } catch (error) {
    console.error(error);
  }
};

const search = () => {
  fetchMaterials(0);
};

onMounted(() => {
  fetchMaterials();
});
</script>

<style scoped>
</style>
