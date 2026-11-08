<template>
  <div class="mb-3">
    <button
      id="material-online-toggle" type="button" class="btn btn-outline-primary"
      :aria-expanded="open" aria-controls="material-online-panel"
      :disabled="disabled || busy !== null" @click="toggle"
    >
      {{ t('materials.online.open') }}
    </button>
    <section v-if="open" id="material-online-panel" class="border rounded p-3 mt-2" :aria-busy="busy !== null"
      aria-labelledby="material-online-title">
      <h2 id="material-online-title" class="h5">{{ t('materials.online.plants') }}</h2>
      <form v-if="sources.length" id="online-material-search" @submit.prevent="findResults">
        <label for="material-online-query" class="form-label">{{ t('materials.online.query') }}</label>
        <div class="input-group">
          <input id="material-online-query" v-model="query" type="search" class="form-control"
            maxlength="120" required :disabled="busy !== null || disabled">
          <button type="submit" class="btn btn-primary"
            :disabled="busy !== null || disabled || !query.trim() || !selectedSources.length">
            {{ t('common.search') }}
          </button>
        </div>
        <fieldset class="mt-2" :disabled="busy !== null || disabled">
          <legend class="fs-6 mb-1">{{ t('materials.online.sources') }}</legend>
          <div v-for="source in sources" :key="source.id" class="form-check form-check-inline">
            <label class="form-check-label">
              <input v-model="selectedSources" type="checkbox" class="form-check-input" :value="source.id">
              {{ source.label }}
            </label>
          </div>
        </fieldset>
      </form>
      <p v-if="busy" class="mt-2 mb-0" role="status">{{ t('materials.online.' + busy) }}</p>
      <div v-else-if="error" class="alert alert-warning mt-2 mb-0" role="alert">
        {{ t('materials.online.' + error) }}
        <button v-if="!sources.length" type="button" class="btn btn-sm btn-outline-secondary ms-2"
          :disabled="disabled" @click="loadSources">{{ t('materials.list.retry') }}</button>
      </div>
      <p v-else-if="results?.length === 0" class="mt-2 mb-0" role="status">{{ t('materials.online.empty') }}</p>
      <ul v-if="results?.length" id="material-online-results" class="list-group mt-3">
        <li v-for="result in results" :key="result.id" class="list-group-item">
          <div class="fw-semibold">{{ result.title }}</div>
          <a :href="safeMaterialUrl(result.sourceUrl)" target="_blank" rel="noopener noreferrer">
            {{ result.sourceLabel }}
          </a>
          <p v-if="result.excerpt" class="mb-2">{{ result.excerpt }}</p>
          <button type="button" class="btn btn-sm btn-outline-primary"
            :disabled="busy !== null || disabled" @click="useResult(result)">
            {{ t('materials.online.useResult') }}
          </button>
        </li>
      </ul>
    </section>
  </div>
</template>

<script setup lang="ts">
import { isAxiosError } from 'axios'
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import apiClient from '@/services/api'
import { safeMaterialUrl, type OnlineMaterialDraft } from '@/models/material'

interface SourceOption { id: string; label: string }
interface SearchResult {
  id: string;
  title: string;
  excerpt: string;
  sourceLabel: string;
  sourceUrl: string;
}

const props = defineProps<{ disabled: boolean }>()
const emit = defineEmits<{ draft: [OnlineMaterialDraft] }>()
const { t } = useI18n()
const endpoint = '/materials/external-search/plants'
const open = ref(false)
const busy = ref<'loadingSources' | 'searching' | 'fetching' | null>(null)
const error = ref('')
const query = ref('')
const sources = ref<SourceOption[]>([])
const selectedSources = ref<string[]>([])
const results = ref<SearchResult[] | null>(null)

async function toggle() {
  open.value = !open.value
  if (open.value && !sources.value.length) await loadSources()
}

async function loadSources() {
  busy.value = 'loadingSources'
  error.value = ''
  try {
    const { data } = await apiClient.get<SourceOption[]>(endpoint + '/sources')
    sources.value = data
    selectedSources.value = data.map(source => source.id)
    if (!data.length) error.value = 'noSources'
  } catch {
    error.value = 'searchFailed'
  } finally {
    busy.value = null
  }
}

async function findResults() {
  if (busy.value || props.disabled || !query.value.trim() || !selectedSources.value.length) return
  busy.value = 'searching'
  error.value = ''
  results.value = null
  try {
    const { data } = await apiClient.post<SearchResult[]>(endpoint + '/results', {
      query: query.value.trim(), sourceIds: selectedSources.value,
    })
    results.value = data
  } catch (cause) {
    error.value = isAxiosError(cause) && cause.response?.status === 429 ? 'searchBusy' : 'searchFailed'
  } finally {
    busy.value = null
  }
}

async function useResult(result: SearchResult) {
  if (busy.value || props.disabled) return
  busy.value = 'fetching'
  error.value = ''
  try {
    const { data } = await apiClient.post<OnlineMaterialDraft>(endpoint + '/results/' + result.id + '/details')
    if (!props.disabled) {
      emit('draft', data)
      open.value = false
    }
  } catch (cause) {
    const code = isAxiosError(cause) ? cause.response?.data?.error : undefined
    error.value = code === 'UNSUPPORTED_PAGE' ? 'unsupported' : code === 'RESULT_EXPIRED' ? 'expired' : 'fetchFailed'
  } finally {
    busy.value = null
  }
}
</script>
