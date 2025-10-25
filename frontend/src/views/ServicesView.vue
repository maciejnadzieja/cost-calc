<template>
  <div>
    <h1>{{ $t('services.title') }}</h1>
    <div class="input-group mb-3">
      <input type="text" class="form-control" v-model="query" @keyup.enter="search">
      <button class="btn btn-primary" type="button" @click="search">{{ $t('search') }}</button>
    </div>
    <table class="table">
      <thead>
        <tr>
          <th>{{ $t('services.form.name') }}</th>
          <th>{{ $t('services.form.description') }}</th>
          <th>{{ $t('services.price') }}</th>
          <th>{{ $t('services.workUnit') }}</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="service in services" :key="service.id">
          <td>{{ service.name }}</td>
          <td>{{ service.description }}</td>
          <td>{{ service.price }}</td>
          <td>{{ service.workUnit }}</td>
        </tr>
      </tbody>
    </table>
    <nav aria-label="Page navigation">
      <ul class="pagination">
        <li class="page-item" :class="{ disabled: currentPage === 0 }">
          <a class="page-link" href="#" @click.prevent="fetchServices(currentPage - 1)">{{ $t('previous') }}</a>
        </li>
        <li class="page-item" v-for="page in totalPages" :key="page" :class="{ active: currentPage === page - 1 }">
          <a class="page-link" href="#" @click.prevent="fetchServices(page - 1)">{{ page }}</a>
        </li>
        <li class="page-item" :class="{ disabled: currentPage === totalPages - 1 }">
          <a class="page-link" href="#" @click.prevent="fetchServices(currentPage + 1)">{{ $t('next') }}</a>
        </li>
      </ul>
    </nav>
  </div>
</template>

<script setup lang="ts">
import {onMounted, ref} from 'vue';
import apiClient from '@/services/api';
import {useRoute, useRouter} from "vue-router";

const router = useRouter();
const route = useRoute();

const query = ref(route.query.query || '');
const services = ref([]);
const currentPage = ref(parseInt(route.query.page as string) || 0);
const totalPages = ref(0);

const fetchServices = async (page = 0) => {
  try {
    const response = await apiClient.get('/services', {
      params: {
        query: query.value,
        page: page,
        size: 10,
      },
    });
    services.value = response.data.content;
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
  fetchServices(0);
};

onMounted(() => {
  fetchServices(currentPage.value);
});
</script>

<style scoped>
</style>
