<template>
  <div>
    <h1>{{ t('services.title') }}</h1>
    <div class="d-flex justify-content-between mb-3">
      <div class="input-group" style="width: auto;">
        <input type="text" class="form-control" v-model="query" @keyup.enter="search">
        <button class="btn btn-primary" type="button" :disabled="loading" @click="search">{{ t('common.search') }}</button>
      </div>
      <router-link to="/services/create" class="btn btn-primary">{{ t('services.new') }}</router-link>
    </div>
    <div class="border rounded" style="overflow:hidden" :aria-busy="loading">
      <p v-if="loading" class="p-4 mb-0 text-center" role="status"><span class="spinner-border spinner-border-sm me-2" aria-hidden="true"></span>{{ t('services.list.loading') }}</p>
      <div v-else-if="failed" class="alert alert-danger m-3" role="alert">{{ t('services.list.failure') }} <button class="btn btn-outline-danger ms-2" type="button" @click="retry">{{ t('services.list.retry') }}</button></div>
      <p v-else-if="services.length === 0" class="p-4 mb-0 text-center">{{ t('services.list.empty') }}</p>
      <table v-else class="table table-striped table-hover">
      <thead>
        <tr>
          <th>{{ t('services.form.name') }}</th>
          <th>{{ t('services.form.description') }}</th>
          <th>{{ t('services.price') }}</th>
          <th>{{ t('services.workUnit') }}</th>
          <th>{{ t('common.actions') }}</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="service in services" :key="service.id || service.name">
          <td>{{ service.name }}</td>
          <td>{{ service.description }}</td>
          <td>{{ service.price }} / {{ t('units.service.' + service.workUnit.toLowerCase() + '.symbol') }}</td>
          <td>{{ t('units.service.' + service.workUnit.toLowerCase() + '.label') }}</td>
          <td>
            <router-link :to="`/services/edit/${service.id}`" class="btn btn-sm btn-primary me-2">{{ t('common.edit') }}</router-link>
            <button v-if="service.id" class="btn btn-sm btn-danger" @click="deleteService(service.id)">{{ t('common.delete') }}</button>
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
import type { Service } from '@/models/service';
import { catalogPageFromQuery, type CatalogPage } from '@/models/catalogPage';

const { t } = useI18n();

const router = useRouter();
const route = useRoute();

const query = ref(typeof route.query.query === 'string' ? route.query.query : '');
const services = ref<Service[]>([]);
const currentPage = ref(catalogPageFromQuery(route.query.page));
const totalPages = ref(0);
const loading = ref(false);
const failed = ref(false);
const requested = ref({ page: currentPage.value, query: query.value });

const fetchServices = async (page: number, queryText: string) => {
  if (loading.value) return;
  requested.value = { page, query: queryText };
  loading.value = true;
  failed.value = false;
  try {
    const response = await apiClient.get<CatalogPage<Service>>('/services', {
      params: {
        query: queryText,
        page: page,
        size: 10,
      },
    });
    services.value = response.data.content;
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

const retry = () => fetchServices(requested.value.page, requested.value.query);

const goToPage = (page: number) => {
  if (!Number.isInteger(page) || page < 0 || page >= Math.max(1, totalPages.value) || page === currentPage.value) return;
  return fetchServices(page, requested.value.query);
};

const search = () => {
  fetchServices(0, query.value);
};

const deleteService = async (id: string) => {
  if (confirm(t('services.delete.confirm'))) {
    try {
      await apiClient.delete(`/services/${id}`);
      await fetchServices(currentPage.value, requested.value.query);
    } catch (error) {
      console.error(error);
    }
  }
};

onMounted(() => {
  fetchServices(currentPage.value, query.value);
});
</script>

<style scoped>
</style>
