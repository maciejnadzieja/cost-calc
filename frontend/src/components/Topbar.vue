<template>
  <header class="topbar d-flex align-items-center justify-content-between px-4 py-3 bg-white border-bottom">
    <button class="btn btn-link d-md-none text-dark p-0 me-3" @click="$emit('toggle-sidebar')">
      <i class="bi bi-list fs-4"></i>
    </button>
    
    <h4 class="m-0 fw-semibold text-dark">{{ pageTitle }}</h4>
    
    <div class="d-flex align-items-center">
      <div class="dropdown">
        <button class="btn btn-light dropdown-toggle d-flex align-items-center" type="button" id="languageDropdown" data-bs-toggle="dropdown" aria-expanded="false">
          <i class="bi bi-globe me-2"></i>
          <span>{{ locale.toUpperCase() }}</span>
        </button>
        <ul class="dropdown-menu dropdown-menu-end shadow-sm" aria-labelledby="languageDropdown">
          <li><a class="dropdown-item" href="#" @click.prevent="setLocale('en')">{{ t('lang.english') }}</a></li>
          <li><a class="dropdown-item" href="#" @click.prevent="setLocale('pl')">{{ t('lang.polish') }}</a></li>
        </ul>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n';
import { computed } from 'vue';
import { useRoute } from 'vue-router';

const { t, locale } = useI18n();
const route = useRoute();

const pageTitle = computed(() => {
  // Map route names to titles or use a default
  const titleKey = `nav.${String(route.name)}`;
  const translated = t(titleKey);
  return translated !== titleKey ? translated : t('app.name');
});

const setLocale = (newLocale: string) => {
  locale.value = newLocale;
  localStorage.setItem('locale', newLocale);
};
</script>

<style scoped>
.topbar {
  height: 70px;
  position: sticky;
  top: 0;
  z-index: 900;
  background-color: rgba(255, 255, 255, 0.8) !important;
  backdrop-filter: blur(10px);
}
</style>
