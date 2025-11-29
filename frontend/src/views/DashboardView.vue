<template>
  <div class="dashboard-view">
    <div class="row mb-4">
      <div class="col-12">
        <div class="card glass-effect border-0 p-4">
          <div class="card-body">
            <h2 class="display-6 fw-bold text-primary mb-2">{{ t('dashboard.welcome') }}</h2>
            <p class="lead text-muted mb-0">{{ t('dashboard.subtitle') }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Quick Stats / Actions Placeholder -->
    <div class="row g-4">
      <div class="col-md-4">
        <div class="card h-100 border-0 shadow-sm">
          <div class="card-body text-center p-5">
            <div class="icon-circle bg-primary bg-opacity-10 text-primary mb-3 mx-auto">
              <i class="bi bi-calculator-fill fs-2"></i>
            </div>
            <h5 class="card-title">{{ t('nav.calculations') }}</h5>
            <p class="card-text text-muted">{{ t('dashboard.manage_calculations') }}</p>
            <router-link to="/calculations" class="btn btn-outline-primary stretched-link">{{ t('common.go') }}</router-link>
          </div>
        </div>
      </div>
      
      <div class="col-md-4">
        <div class="card h-100 border-0 shadow-sm">
          <div class="card-body text-center p-5">
            <div class="icon-circle bg-success bg-opacity-10 text-success mb-3 mx-auto">
              <i class="bi bi-box-seam fs-2"></i>
            </div>
            <h5 class="card-title">{{ t('nav.materials') }}</h5>
            <p class="card-text text-muted">{{ t('dashboard.manage_materials') }}</p>
            <router-link to="/materials" class="btn btn-outline-success stretched-link">{{ t('common.go') }}</router-link>
          </div>
        </div>
      </div>

      <div class="col-md-4">
        <div class="card h-100 border-0 shadow-sm">
          <div class="card-body text-center p-5">
            <div class="icon-circle bg-info bg-opacity-10 text-info mb-3 mx-auto">
              <i class="bi bi-gear-fill fs-2"></i>
            </div>
            <h5 class="card-title">{{ t('nav.services') }}</h5>
            <p class="card-text text-muted">{{ t('dashboard.manage_services') }}</p>
            <router-link to="/services" class="btn btn-outline-info stretched-link">{{ t('common.go') }}</router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n';

const { t } = useI18n();
</script>

<style scoped>
.icon-circle {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
