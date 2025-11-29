<template>
  <div class="calculation-list">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h1 class="h3 mb-0 text-gray-800">{{ t('calculations.list.title') }}</h1>
      <router-link to="/calculations/new" class="btn btn-primary shadow-sm">
        <i class="bi bi-plus-lg me-2"></i>{{ t('calculations.list.create_new') }}
      </router-link>
    </div>

    <div class="card border-0 shadow-sm mb-4">
      <div class="card-body p-3">
        <div class="input-group">
          <span class="input-group-text bg-light border-end-0">
            <i class="bi bi-search text-muted"></i>
          </span>
          <input 
            type="text" 
            class="form-control bg-light border-start-0 ps-0" 
            :placeholder="t('common.search')" 
            v-model="query" 
            @keyup.enter="search"
          >
          <button class="btn btn-primary" type="button" @click="search">{{ t('common.search') }}</button>
        </div>
      </div>
    </div>

    <div class="card border-0 shadow-sm overflow-hidden">
      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
          <thead class="bg-light">
            <tr>
              <th class="border-0 py-3 ps-4">{{ t('calculations.list.name') }}</th>
              <th class="border-0 py-3">{{ t('calculations.list.creationDate') }}</th>
              <th class="border-0 py-3">{{ t('calculations.list.total_price') }}</th>
              <th class="border-0 py-3 text-end pe-4">{{ t('common.actions') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="calculation in calculations" :key="calculation.id">
              <td class="ps-4">
                <div class="fw-medium">{{ calculation.name }}</div>
              </td>
              <td class="text-muted">
                <i class="bi bi-calendar3 me-2 text-secondary"></i>
                {{ calculation.createdAt ? new Date(calculation.createdAt).toLocaleDateString() : '' }}
              </td>
              <td>
                <span class="badge bg-success bg-opacity-10 text-success px-3 py-2 rounded-pill">
                  {{ calculation.totalAmount }} {{ calculation.currency }}
                </span>
              </td>
              <td class="text-end pe-4">
                <div class="btn-group">
                  <router-link :to="`/calculations/edit/${calculation.id}`" class="btn btn-sm btn-outline-primary" :title="t('common.edit')">
                    <i class="bi bi-pencil"></i>
                  </router-link>
                  <button v-if="calculation.id" class="btn btn-sm btn-outline-secondary" @click="duplicateCalculation(calculation.id)" :title="t('common.duplicate')">
                    <i class="bi bi-files"></i>
                  </button>
                  <router-link :to="`/calculations/print/${calculation.id}`" class="btn btn-sm btn-outline-info" :title="t('common.print')">
                    <i class="bi bi-printer"></i>
                  </router-link>
                  <button v-if="calculation.id" class="btn btn-sm btn-outline-danger" @click="deleteCalculation(calculation.id)" :title="t('common.delete')">
                    <i class="bi bi-trash"></i>
                  </button>
                </div>
              </td>
            </tr>
            <tr v-if="calculations.length === 0">
              <td colspan="4" class="text-center py-5 text-muted">
                <i class="bi bi-inbox fs-1 d-block mb-3"></i>
                {{ t('common.no_data') }}
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      
      <div class="card-footer bg-white border-top-0 py-3" v-if="totalPages > 1">
        <nav aria-label="Page navigation">
          <ul class="pagination justify-content-center mb-0">
            <li class="page-item" :class="{ disabled: currentPage === 0 }">
              <a class="page-link border-0 rounded-circle mx-1" href="#" @click.prevent="fetchCalculations(currentPage - 1)">
                <i class="bi bi-chevron-left"></i>
              </a>
            </li>
            <li class="page-item" v-for="page in totalPages" :key="page" :class="{ active: currentPage === page - 1 }">
              <a class="page-link border-0 rounded-circle mx-1" href="#" @click.prevent="fetchCalculations(page - 1)">{{ page }}</a>
            </li>
            <li class="page-item" :class="{ disabled: currentPage === totalPages - 1 }">
              <a class="page-link border-0 rounded-circle mx-1" href="#" @click.prevent="fetchCalculations(currentPage + 1)">
                <i class="bi bi-chevron-right"></i>
              </a>
            </li>
          </ul>
        </nav>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import {onMounted, ref} from 'vue';
import apiClient from '@/services/api';
import {useRoute, useRouter} from "vue-router";
import type {Calculation} from "@/models/calculation";
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

const router = useRouter();
const route = useRoute();

const query = ref(route.query.query || '');
const calculations = ref<Calculation[]>([]);
const currentPage = ref(parseInt(route.query.page as string) || 0);
const totalPages = ref(0);

const fetchCalculations = async (page = 0) => {
  try {
    const response = await apiClient.get('/calculations', {
      params: {
        query: query.value,
        page: page,
        size: 10,
      },
    });
    calculations.value = response.data.content;
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
  fetchCalculations(0);
};

const duplicateCalculation = async (id: string) => {
  try {
    await apiClient.post(`/calculations/${id}/duplicate`);
    await fetchCalculations(currentPage.value);
  } catch (error) {
    console.error(error);
  }
};

const deleteCalculation = async (id: string) => {
  if (confirm(t('calculations.delete.confirm'))) {
    try {
      await apiClient.delete(`/calculations/${id}`);
      await fetchCalculations(currentPage.value);
    } catch (error) {
      console.error(error);
    }
  }
};

onMounted(() => {
  fetchCalculations(currentPage.value);
});
</script>

<style scoped>
.table th {
  font-weight: 600;
  color: var(--text-muted);
  font-size: 0.875rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.page-link {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-muted);
}

.page-link:hover {
  background-color: var(--bg-body);
  color: var(--primary-color);
}

.page-item.active .page-link {
  background-color: var(--primary-color);
  color: white;
}
</style>
