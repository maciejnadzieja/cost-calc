<template>
  <div>
    <h1>{{ t('calculations.list.title') }}</h1>
    <div class="d-flex justify-content-between mb-3">
      <div class="input-group" style="width: auto;">
        <input type="text" class="form-control" v-model="query" @keyup.enter="search">
        <button class="btn btn-primary" type="button" @click="search">{{ t('common.search') }}</button>
      </div>
      <router-link to="/calculations/new" class="btn btn-primary">{{ t('calculations.list.create_new') }}</router-link>
    </div>
    <div class="border rounded" style="overflow:hidden">
      <table class="table table-striped table-hover">
        <thead>
        <tr>
          <th>{{ t('calculations.list.name') }}</th>
          <th>{{ t('calculations.list.creationDate') }}</th>
          <th>{{ t('calculations.list.total_price') }}</th>
          <th>{{ t('common.actions') }}</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="calculation in calculations" :key="calculation.id">
          <td>{{ calculation.name }}</td>
          <td>{{ new Date(calculation.createdAt).toLocaleDateString() }}</td>
          <td>{{ calculation.totalPrice }} {{ calculation.currency }}</td>
          <td>
            <router-link :to="`/calculations/edit/${calculation.id}`" class="btn btn-sm btn-primary me-2">{{ t('common.edit') }}</router-link>
            <button class="btn btn-sm btn-secondary me-2" @click="duplicateCalculation(calculation.id)">{{ t('common.duplicate') }}</button>
            <button class="btn btn-sm btn-danger me-2" @click="deleteCalculation(calculation.id)">{{ t('common.delete') }}</button>
            <router-link :to="`/calculations/print/${calculation.id}`" class="btn btn-sm btn-info">
              <i class="bi bi-printer"></i>
            </router-link>
          </td>
        </tr>
        </tbody>
      </table>
    </div>
    <nav aria-label="Page navigation" class="mt-3">
      <ul class="pagination">
        <li class="page-item" :class="{ disabled: currentPage === 0 }">
          <a class="page-link" href="#" @click.prevent="fetchCalculations(currentPage - 1)">{{ t('common.previous') }}</a>
        </li>
        <li class="page-item" v-for="page in totalPages" :key="page" :class="{ active: currentPage === page - 1 }">
          <a class="page-link" href="#" @click.prevent="fetchCalculations(page - 1)">{{ page }}</a>
        </li>
        <li class="page-item" :class="{ disabled: currentPage === totalPages - 1 }">
          <a class="page-link" href="#" @click.prevent="fetchCalculations(currentPage + 1)">{{ t('common.next') }}</a>
        </li>
      </ul>
    </nav>
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
</style>
