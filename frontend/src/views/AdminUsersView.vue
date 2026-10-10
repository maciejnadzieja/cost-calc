<template>
  <section :aria-busy="loading">
    <h1>{{ t('adminUsers.title') }}</h1>
    <p v-if="loading" role="status">{{ t('adminUsers.loading') }}</p>
    <div v-else-if="denied" class="alert alert-warning" role="alert">{{ t('adminUsers.denied') }}</div>
    <div v-else-if="failed" class="alert alert-danger" role="alert">
      <p>{{ t('adminUsers.failure') }}</p>
      <button class="btn btn-primary" type="button" @click="load(requestedPage)">{{ t('adminUsers.retry') }}</button>
    </div>
    <template v-else>
      <p v-if="users.length === 0" role="status">{{ t('adminUsers.empty') }}</p>
      <div v-else class="table-responsive border rounded">
        <table class="table table-striped table-hover mb-0">
          <thead>
            <tr>
              <th scope="col">{{ t('adminUsers.username') }}</th>
              <th scope="col">{{ t('adminUsers.email') }}</th>
              <th scope="col">{{ t('adminUsers.enabled') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="user in users" :key="user.username">
              <td>{{ user.username }}</td>
              <td>{{ user.email || t('adminUsers.missingEmail') }}</td>
              <td>{{ user.enabled ? t('adminUsers.yes') : t('adminUsers.no') }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </template>
    <nav v-if="!denied" :aria-label="t('adminUsers.pagination')" class="d-flex align-items-center gap-3 mt-3">
      <button class="btn btn-outline-primary" type="button" :disabled="loading || failed || currentPage === 0" @click="load(currentPage - 1)">
        {{ t('common.previous') }}
      </button>
      <span>{{ t('adminUsers.page', { number: currentPage + 1 }) }}</span>
      <button class="btn btn-outline-primary" type="button" :disabled="loading || failed || !hasNext" @click="load(currentPage + 1)">
        {{ t('common.next') }}
      </button>
    </nav>
  </section>
</template>

<script setup lang="ts">
import { onMounted, onBeforeUnmount, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import axios from 'axios'
import { isRealmAdmin } from '../services/adminAccess'
import { getAdminUsers, type AdminUser } from '../services/adminUsers'

const { t } = useI18n()
const users = ref<AdminUser[]>([])
const currentPage = ref(0)
const requestedPage = ref(0)
const hasNext = ref(false)
const loading = ref(false)
const failed = ref(false)
const denied = ref(false)
let active = true

const load = async (page: number) => {
  if (loading.value || page < 0) return
  requestedPage.value = page
  failed.value = false
  if (!isRealmAdmin()) {
    users.value = []
    denied.value = true
    return
  }
  loading.value = true
  try {
    const { data } = await getAdminUsers(page)
    if (!active) return
    users.value = data.content
    currentPage.value = data.number
    hasNext.value = data.hasNext
  } catch (error) {
    if (!active) return
    users.value = []
    const status = axios.isAxiosError(error) ? error.response?.status : undefined
    denied.value = status === 401 || status === 403
    failed.value = !denied.value
  } finally {
    if (active) loading.value = false
  }
}

onMounted(() => load(0))
onBeforeUnmount(() => { active = false })
</script>
