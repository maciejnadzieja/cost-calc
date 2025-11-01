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

            <div class="mb-3">
              <label for="themeColor" class="form-label">{{ t('settings.theme_color.label') }}</label>
              <div class="input-group">
                <input type="color" class="form-control form-control-color" id="colorPicker"
                       v-model="settings.themeColor">
                <input type="text" class="form-control" id="themeColor" v-model="settings.themeColor"
                       pattern="^#[0-9A-Fa-f]{6}$" title="Hex color code (e.g. #007bff)">
              </div>
              <small class="form-text text-muted">{{ t('settings.theme_color.help') }}</small>
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
import { ref } from 'vue';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

const settings = ref({
  currency: 'USD',
  footerText: '',
  themeColor: '#007bff'
});

const availableCurrencies = ref(['USD', 'EUR', 'PLN']);

const saveSettings = () => {
  // Mock implementation
  console.log('Settings saved:', settings.value);
};
</script>

<style scoped>
</style>