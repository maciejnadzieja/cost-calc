<template>
  <div class="row justify-content-center">
    <div class="col-md-8">
      <div class="card">
        <div class="card-body">
          <h2 class="card-title mb-4">{{ t('settings.title') }}</h2>

          <form @submit.prevent="saveSettings">
            <div class="mb-3">
              <label for="currency" class="form-label">{{ t('settings.currency') }}</label>
              <select class="form-select" id="currency" v-model="settings.currency">
                <option v-for="curr in availableCurrencies" :key="curr" :value="curr">{{ curr }}</option>
              </select>
            </div>

            <div class="mb-3">
              <label for="footerText" class="form-label">{{ t('settings.footer_text.label') }}</label>
              <textarea class="form-control" id="footerText" v-model="settings.footerText" rows="3"
                        wrap="hard"></textarea>
              <small class="form-text text-muted">{{ t('settings.footer_text.help') }}</small>
            </div>

            <div class="d-flex justify-content-between">
              <router-link to="/dashboard" class="btn btn-secondary">{{ t('settings.cancel') }}</router-link>
              <button type="submit" class="btn btn-primary">{{ t('settings.save') }}</button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useI18n } from 'vue-i18n';
import apiClient from '@/services/api';

const { t } = useI18n();

const settings = ref({
  currency: 'USD',
  footerText: '',
  themeColor: '#007bff'
});

const availableCurrencies = ref<string[]>([]);

onMounted(async () => {
  try {
    const settingsResponse = await apiClient.get('/user-settings');
    settings.value = settingsResponse.data;

    const currenciesResponse = await apiClient.get('/calculations/currencies');
    availableCurrencies.value = currenciesResponse.data;
  } catch (error) {
    console.error('Error fetching data:', error);
  }
});

const saveSettings = async () => {
  try {
    await apiClient.put('/user-settings', settings.value);
    // Optionally, show a success message to the user
  } catch (error) {
    console.error('Error saving settings:', error);
    // Optionally, show an error message to the user
  }
};
</script>

<style scoped>
</style>
