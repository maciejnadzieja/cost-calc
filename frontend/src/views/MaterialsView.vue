<template>
  <div>
    <h1>{{ t('materials.title') }}</h1>
    <div class="d-flex justify-content-between mb-3">
      <div class="input-group" style="width: auto;">
        <input type="text" class="form-control" v-model="query" @keyup.enter="search">
        <button class="btn btn-primary" type="button" @click="search">{{ t('common.search') }}</button>
      </div>
      <router-link to="/materials/create" class="btn btn-primary">{{ t('materials.new') }}</router-link>
    </div>
    <div class="border rounded" style="overflow:hidden">
      <table class="table table-striped table-hover">
      <thead>
        <tr>
          <th>{{ t('materials.form.name') }}</th>
          <th>{{ t('materials.form.description') }}</th>
          <th>{{ t('materials.price') }}</th>
          <th>{{ t('materials.unit') }}</th>
          <th>{{ t('materials.category') }}</th>
          <th>{{ t('common.actions') }}</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="material in materials" :key="material.id">
          <td>{{ material.name }}</td>
          <td>{{ material.description }}</td>
          <td>{{ material.price }} / {{ t('units.material.' + material.unit.toLowerCase() + '.symbol') }}</td>
          <td>{{ t('units.material.' + material.unit.toLowerCase() + '.label') }}</td>
          <td>{{ material.category }}</td>
          <td>
            <router-link :to="`/materials/edit/${material.id}`" class="btn btn-sm btn-primary">{{ t('common.edit') }}</router-link>
          </td>
        </tr>
      </tbody>
      </table>
    </div>
    <nav aria-label="Page navigation" class="mt-3">
      <ul class="pagination">
        <li class="page-item" :class="{ disabled: currentPage === 0 }">
          <a class="page-link" href="#" @click.prevent="fetchMaterials(currentPage - 1)">{{ t('common.previous') }}</a>
        </li>
        <li class="page-item" v-for="page in totalPages" :key="page" :class="{ active: currentPage === page - 1 }">
          <a class="page-link" href="#" @click.prevent="fetchMaterials(currentPage - 1)">{{ page }}</a>
        </li>
        <li class="page-item" :class="{ disabled: currentPage === totalPages - 1 }">
          <a class="page-link" href="#" @click.prevent="fetchMaterials(currentPage + 1)">{{ t('common.next') }}</a>
        </li>
      </ul>
    </nav>
  </div>
</template>

<script setup lang="ts">
import {onMounted, ref} from 'vue';
import apiClient from '@/services/api';
import {useRoute, useRouter} from "vue-router";
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

const router = useRouter();
const route = useRoute();

const query = ref(route.query.query || '');
const materials = ref([]);
const currentPage = ref(parseInt(route.query.page as string) || 0);
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

    const queryParams: any = {};
    if (query.value) {
      queryParams.query = query.value;
    }
    if (currentPage.value > 0) {
      queryParams.page = currentPage.value;
    }
    router.push({ query: queryParams });
  } catch (error) {
    console.error(error);
  }
};

const search = () => {
  fetchMaterials(0);
};

onMounted(() => {
  fetchMaterials(currentPage.value);
});
</script>

<style scoped>
</style>
