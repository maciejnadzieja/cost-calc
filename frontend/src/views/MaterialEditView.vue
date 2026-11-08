<template>
  <div>
    <h1>{{ isEdit ? t('materials.edit.title') : t('materials.create.title') }}</h1>
    <MaterialOnlineSearch v-if="!isEdit" :disabled="saving" @draft="useDraft" />
    <div v-if="sourceUrl" id="material-source" class="border rounded p-3 mb-3">
      <a :href="sourceUrl" target="_blank" rel="noopener noreferrer">{{ t('materials.online.sourcePage') }}</a>
      <div v-if="imageUrl && !imageFailed" class="mt-2">
        <img :src="imageUrl" :alt="material.name" referrerpolicy="no-referrer" class="img-thumbnail material-photo"
          @error="imageFailed = true">
      </div>
      <p v-else class="text-body-secondary mb-0 mt-2">{{ t('materials.online.imageMissing') }}</p>
      <p v-if="draft && !draft.description" class="text-body-secondary mb-0 mt-2">
        {{ t('materials.online.descriptionMissing') }}
      </p>
    </div>
    <p v-if="saveError" class="alert alert-danger" role="alert">{{ t('materials.online.' + saveError) }}</p>
    <form id="material-form" @submit.prevent="save">
      <fieldset :disabled="saving || loading">
        <div class="mb-3">
          <label for="name" class="form-label">{{ t('materials.form.name') }}</label>
          <input type="text" class="form-control" id="name" v-model="material.name" required
            :aria-describedby="draft ? 'material-name-length' : undefined" :aria-invalid="nameTooLong">
          <div v-if="draft" id="material-name-length" class="form-text" :class="{ 'text-danger': nameTooLong }">
            {{ t('materials.online.length', { count: material.name.length, max: 100 }) }}
          </div>
        </div>
        <div class="mb-3">
          <label for="description" class="form-label">{{ t('materials.form.description') }}</label>
          <textarea class="form-control" id="description" v-model="material.description" rows="5"
            :aria-describedby="draft ? 'material-description-length' : undefined" :aria-invalid="descriptionTooLong"></textarea>
          <div v-if="draft" id="material-description-length" class="form-text" :class="{ 'text-danger': descriptionTooLong }">
            {{ t('materials.online.length', { count: material.description.length, max: 500 }) }}
          </div>
          <p v-if="nameTooLong || descriptionTooLong" class="text-danger mb-0">
            {{ t('materials.online.shorten') }}
          </p>
        </div>
        <div class="mb-3">
          <label for="price" class="form-label">{{ t('materials.price') }}</label>
          <input type="number" step="0.01" min="0.01" class="form-control" id="price" v-model="material.price" required>
        </div>
        <div class="mb-3">
          <label for="unit" class="form-label">{{ t('materials.unit') }}</label>
          <select class="form-select" id="unit" v-model="material.unit" required>
            <option v-for="unit in materialUnits" :key="unit" :value="unit">{{ t('units.material.' + unit.toLowerCase() + '.label') }}</option>
          </select>
        </div>
        <div class="mb-3">
          <label for="category" class="form-label">{{ t('materials.category') }}</label>
          <input type="text" class="form-control" id="category" v-model="material.category" required
            :maxlength="draft ? 50 : undefined">
        </div>
        <button type="submit" class="btn btn-primary" :disabled="!canSave">
          {{ t(saving ? 'materials.online.saving' : 'common.save') }}
        </button>
      </fieldset>
      <router-link v-if="!saving" to="/materials" class="btn btn-secondary mt-2">{{ t('common.cancel') }}</router-link>
    </form>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import apiClient from '@/services/api';
import { useI18n } from 'vue-i18n';
import MaterialOnlineSearch from '@/components/MaterialOnlineSearch.vue';
import { safeMaterialUrl, type Material, type OnlineMaterialDraft } from '@/models/material';

const { t } = useI18n();

const route = useRoute();
const router = useRouter();

const material = ref<Material>({
  id: null,
  name: '',
  description: '',
  price: 0,
  unit: 'PIECE',
  category: '',
});

const materialUnits = ref<string[]>([]);

const isEdit = computed(() => Boolean(route.params.id));
const draft = ref<OnlineMaterialDraft | null>(null);
const saving = ref(false);
const loading = ref(isEdit.value);
const saveError = ref('');
const imageFailed = ref(false);
const sourceUrl = computed(() => safeMaterialUrl(draft.value?.sourceUrl ?? material.value.sourceUrl));
const imageUrl = computed(() => safeMaterialUrl(draft.value?.imageUrl ?? material.value.imageUrl));
const nameTooLong = computed(() => Boolean(draft.value) && material.value.name.length > 100);
const descriptionTooLong = computed(() => Boolean(draft.value) && material.value.description.length > 500);
const canSave = computed(() => !loading.value && !saving.value && !nameTooLong.value && !descriptionTooLong.value &&
  Boolean(material.value.name.trim()) && Number(material.value.price) >= 0.01 &&
  Boolean(material.value.unit) && Boolean(material.value.category.trim()) &&
  (!draft.value || material.value.category.length <= 50));

function useDraft(value: OnlineMaterialDraft) {
  draft.value = value;
  material.value.name = value.name;
  material.value.description = value.description;
  imageFailed.value = false;
  saveError.value = '';
}

onMounted(async () => {
  try {
    const response = await apiClient.get('/materials/units');
    materialUnits.value = response.data;
  } catch (error) {
    console.error(error);
  }

  const id = route.params.id;
  if (id) {
    try {
      const response = await apiClient.get<Material>('/materials/' + id);
      material.value = { ...response.data, description: response.data.description ?? '' };
      loading.value = false;
    } catch {
      saveError.value = 'loadFailed';
    }
  }
});

const save = async () => {
  if (!canSave.value) return;
  saving.value = true;
  saveError.value = '';
  const { name, description, price, unit, category } = material.value;
  const fields = { name, description, price, unit, category };
  try {
    if (isEdit.value) {
      await apiClient.put('/materials/' + material.value.id, fields);
    } else {
      const links = draft.value ? { sourceUrl: draft.value.sourceUrl, imageUrl: draft.value.imageUrl } : {};
      await apiClient.post('/materials', { ...fields, ...links });
    }
    router.push('/materials');
  } catch {
    saveError.value = 'saveFailed';
  } finally {
    saving.value = false;
  }
};
</script>

<style scoped>
.material-photo { max-height: 200px; }
</style>
