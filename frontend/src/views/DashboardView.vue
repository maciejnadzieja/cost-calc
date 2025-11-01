<template>
  <div>
    <div class="row">
      <div class="col-12">
        <h2>{{ t('dashboard.welcome') }}</h2>
        <p class="lead">{{ t('dashboard.subtitle') }}</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n';

const { t } = useI18n();
</script>

<style scoped>
</style>
