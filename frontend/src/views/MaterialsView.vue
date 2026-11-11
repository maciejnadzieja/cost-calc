<template>
  <div>
    <h1>{{ t('materials.title') }}</h1>
    <div class="d-flex justify-content-between mb-3">
      <div class="input-group" style="width: auto;">
        <input type="text" class="form-control" id="material-search-query" name="material-search-query" v-model="query" @keyup.enter="search">
        <button class="btn btn-primary" type="button" :disabled="loading" @click="search">{{ t('common.search') }}</button>
      </div>
      <router-link to="/materials/create" class="btn btn-primary">{{ t('materials.new') }}</router-link>
    </div>
    <div class="border rounded" style="overflow:hidden" :aria-busy="loading">
      <p v-if="loading" class="p-4 mb-0 text-center" role="status"><span class="spinner-border spinner-border-sm me-2" aria-hidden="true"></span>{{ t('materials.list.loading') }}</p>
      <div v-else-if="failed" class="alert alert-danger m-3" role="alert">{{ t('materials.list.failure') }} <button class="btn btn-outline-danger ms-2" type="button" @click="retry">{{ t('materials.list.retry') }}</button></div>
      <p v-else-if="materials.length === 0" class="p-4 mb-0 text-center">{{ t('materials.list.empty') }}</p>
      <table v-else class="table table-striped table-hover">
      <thead>
        <tr>
          <th>{{ t('materials.form.name') }}</th>
          <th>{{ t('materials.price') }}</th>
          <th>{{ t('materials.unit') }}</th>
          <th>{{ t('materials.category') }}</th>
          <th>{{ t('common.actions') }}</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="material in materials" :key="material.id || material.name">
          <td>{{ material.name }}</td>
          <td>{{ material.price }} / {{ t('units.material.' + material.unit.toLowerCase() + '.symbol') }}</td>
          <td>{{ t('units.material.' + material.unit.toLowerCase() + '.label') }}</td>
          <td>{{ material.category }}</td>
          <td>
            <router-link :to="`/materials/edit/${material.id}`" class="btn btn-sm btn-primary me-2">{{ t('common.edit') }}</router-link>
            <button v-if="material.id" class="btn btn-sm btn-danger" @click="deleteMaterial(material.id)">{{ t('common.delete') }}</button>
          </td>
        </tr>
      </tbody>
      </table>
    </div>
    <nav v-if="!loading && !failed" aria-label="Page navigation" class="mt-3">
      <ul class="pagination">
        <li class="page-item" :class="{ disabled: currentPage === 0 }">
          <button type="button" class="page-link" :disabled="currentPage === 0"
            @click="goToPage(Math.min(currentPage - 1, Math.max(0, totalPages - 1)))">{{ t('common.previous') }}</button>
        </li>
        <li class="page-item" v-for="page in totalPages" :key="page" :class="{ active: currentPage === page - 1 }">
          <button type="button" class="page-link" :aria-current="currentPage === page - 1 ? 'page' : undefined"
            @click="goToPage(page - 1)">{{ page }}</button>
        </li>
        <li class="page-item" :class="{ disabled: currentPage >= totalPages - 1 }">
          <button type="button" class="page-link" :disabled="currentPage >= totalPages - 1"
            @click="goToPage(currentPage + 1)">{{ t('common.next') }}</button>
        </li>
      </ul>
    </nav>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import apiClient from '@/services/api';
import { useRoute, useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import type { Material } from '@/models/material';
import { catalogPageFromQuery, type CatalogPage } from '@/models/catalogPage';

const { t } = useI18n();

const router = useRouter();
const route = useRoute();

const query = ref(typeof route.query.query === 'string' ? route.query.query : '');
const materials = ref<Material[]>([]);
const currentPage = ref(catalogPageFromQuery(route.query.page));
const totalPages = ref(0);
const loading = ref(false);
const failed = ref(false);
const requested = ref({ page: currentPage.value, query: query.value });

const fetchMaterials = async (page: number, queryText: string) => {
  if (loading.value) return;
  requested.value = { page, query: queryText };
  loading.value = true;
  failed.value = false;
  try {
    const response = await apiClient.get<CatalogPage<Material>>('/materials', {
      params: {
        query: queryText,
        page: page,
        size: 10,
      },
    });
    materials.value = response.data.content;
    currentPage.value = response.data.page.number;
    totalPages.value = response.data.page.totalPages;

    const queryParams: { query?: string; page?: number } = {};
    if (queryText) {
      queryParams.query = queryText;
    }
    if (currentPage.value > 0) {
      queryParams.page = currentPage.value;
    }
    router.push({ query: queryParams });
  } catch {
    failed.value = true;
  } finally {
    loading.value = false;
  }
};

const retry = () => fetchMaterials(requested.value.page, requested.value.query);

const goToPage = (page: number) => {
  if (!Number.isInteger(page) || page < 0 || page >= Math.max(1, totalPages.value) || page === currentPage.value) return;
  return fetchMaterials(page, requested.value.query);
};

const search = () => {
  fetchMaterials(0, query.value);
};

const deleteMaterial = async (id: string) => {
  if (confirm(t('materials.delete.confirm'))) {
    try {
      await apiClient.delete(`/materials/${id}`);
      await fetchMaterials(currentPage.value, requested.value.query);
    } catch (error) {
      console.error(error);
    }
  }
};

onMounted(() => {
  fetchMaterials(currentPage.value, query.value);
});
</script>
