<template>
  <div class="mb-3">
    <button
      id="material-online-toggle" type="button" class="btn btn-outline-primary"
      aria-haspopup="dialog" aria-controls="material-online-dialog"
      :disabled="disabled" @click="openDialog"
    >
      {{ t('materials.online.open') }}
    </button>
    <dialog ref="dialog" id="material-online-dialog" class="online-dialog bg-body text-body border rounded shadow"
      aria-labelledby="material-online-title" @cancel.prevent="closeDialog">
      <header class="sticky-top bg-body border-bottom d-flex justify-content-between align-items-center gap-3 p-3">
        <h2 id="material-online-title" class="h5 mb-0">{{ t('materials.online.plants') }}</h2>
        <button type="button" class="btn-close" :aria-label="t('materials.online.close')" @click="closeDialog"></button>
      </header>
      <div class="p-3">
        <form v-if="sources.length" id="online-material-search" @submit.prevent="findResults">
          <label for="material-online-query" class="form-label">{{ t('materials.online.query') }}</label>
          <div class="input-group">
            <input ref="queryField" id="material-online-query" v-model="query" type="search" class="form-control"
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
      </div>
    </dialog>
  </div>
</template>

<script setup lang="ts">
import { isAxiosError } from 'axios'
import { nextTick, onBeforeUnmount, ref } from 'vue'
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
const dialog = ref<HTMLDialogElement | null>(null)
const queryField = ref<HTMLInputElement | null>(null)
let session: AbortController | undefined
const busy = ref<'loadingSources' | 'searching' | 'fetching' | null>(null)
const error = ref('')
const query = ref('')
const sources = ref<SourceOption[]>([])
const selectedSources = ref<string[]>([])
const results = ref<SearchResult[] | null>(null)

async function openDialog() {
  if (!dialog.value || dialog.value.open || props.disabled) return
  const controller = new AbortController()
  session = controller
  dialog.value.showModal()
  if (!sources.value.length) await loadSources()
  await nextTick()
  if (session === controller) queryField.value?.focus()
}

function closeDialog() {
  session?.abort()
  session = undefined
  busy.value = null
  dialog.value?.close()
}

onBeforeUnmount(() => session?.abort())

async function loadSources() {
  const signal = session?.signal
  if (!signal) return
  busy.value = 'loadingSources'
  error.value = ''
  try {
    const { data } = await apiClient.get<SourceOption[]>(endpoint + '/sources', { signal })
    if (signal.aborted) return
    sources.value = data
    selectedSources.value = data.map(source => source.id)
    if (!data.length) error.value = 'noSources'
  } catch {
    if (!signal.aborted) error.value = 'searchFailed'
  } finally {
    if (!signal.aborted) busy.value = null
  }
}

async function findResults() {
  const signal = session?.signal
  if (!signal || busy.value || props.disabled || !query.value.trim() || !selectedSources.value.length) return
  busy.value = 'searching'
  error.value = ''
  results.value = null
  try {
    const { data } = await apiClient.post<SearchResult[]>(endpoint + '/results', {
      query: query.value.trim(), sourceIds: selectedSources.value,
    }, { signal })
    if (!signal.aborted) results.value = data
  } catch (cause) {
    if (!signal.aborted) {
      error.value = isAxiosError(cause) && cause.response?.status === 429 ? 'searchBusy' : 'searchFailed'
    }
  } finally {
    if (!signal.aborted) busy.value = null
  }
}

async function useResult(result: SearchResult) {
  const signal = session?.signal
  if (!signal || busy.value || props.disabled) return
  busy.value = 'fetching'
  error.value = ''
  try {
    const { data } = await apiClient.post<OnlineMaterialDraft>(endpoint + '/results/' + result.id + '/details', undefined, { signal })
    if (!signal.aborted && !props.disabled) {
      emit('draft', data)
      closeDialog()
    }
  } catch (cause) {
    if (signal.aborted) return
    const code = isAxiosError(cause) ? cause.response?.data?.error : undefined
    error.value = code === 'UNSUPPORTED_PAGE' ? 'unsupported' : code === 'RESULT_EXPIRED' ? 'expired' : 'fetchFailed'
  } finally {
    if (!signal.aborted) busy.value = null
  }
}
</script>

<style scoped>
.online-dialog {
  width: min(44rem, calc(100% - 2rem));
  max-height: calc(100dvh - 2rem);
  padding: 0;
  overflow-y: auto;
}

.online-dialog::backdrop {
  background: rgb(0 0 0 / 45%);
}
</style>
