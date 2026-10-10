<template>
  <div class="sidebar d-flex flex-column flex-shrink-0 p-3 text-white bg-dark">
    <a href="/" class="d-flex align-items-center mb-3 mb-md-0 me-md-auto text-white text-decoration-none">
      <i class="bi bi-calculator-fill fs-4 me-2 text-primary"></i>
      <span class="fs-4 fw-bold">{{ t('app.name') }}</span>
    </a>
    <hr>
    <ul class="nav nav-pills flex-column mb-auto">
      <li v-if="isRealmAdmin()">
        <router-link to="/admin/users" class="nav-link text-white" active-class="active">
          <i class="bi bi-people me-2"></i>
          {{ t('adminUsers.title') }}
        </router-link>
      </li>
      <li class="nav-item">
        <router-link to="/dashboard" class="nav-link text-white" active-class="active">
          <i class="bi bi-speedometer2 me-2"></i>
          {{ t('nav.dashboard') || 'Dashboard' }}
        </router-link>
      </li>
      <li>
        <router-link to="/calculations" class="nav-link text-white" active-class="active">
          <i class="bi bi-calculator me-2"></i>
          {{ t('nav.calculations') }}
        </router-link>
      </li>
      <li>
        <router-link to="/services" class="nav-link text-white" active-class="active">
          <i class="bi bi-gear me-2"></i>
          {{ t('nav.services') }}
        </router-link>
      </li>
      <li>
        <router-link to="/materials" class="nav-link text-white" active-class="active">
          <i class="bi bi-box-seam me-2"></i>
          {{ t('nav.materials') }}
        </router-link>
      </li>
      <li>
        <router-link to="/settings" class="nav-link text-white" active-class="active">
          <i class="bi bi-sliders me-2"></i>
          {{ t('nav.settings') }}
        </router-link>
      </li>
    </ul>
    <hr>
    <div class="dropdown">
      <a href="#" class="d-flex align-items-center text-white text-decoration-none dropdown-toggle" id="dropdownUser1" data-bs-toggle="dropdown" aria-expanded="false">
        <div class="avatar me-2 bg-primary text-white d-flex align-items-center justify-content-center rounded-circle" style="width: 32px; height: 32px;">
          {{ username.charAt(0).toUpperCase() }}
        </div>
        <strong>{{ username }}</strong>
      </a>
      <ul class="dropdown-menu dropdown-menu-dark text-small shadow" aria-labelledby="dropdownUser1">
        <li><a class="dropdown-item" href="#" @click.prevent="accountManagement">{{ t('nav.account') }}</a></li>
        <li><hr class="dropdown-divider"></li>
        <li><a class="dropdown-item" href="#" @click.prevent="logout">{{ t('nav.logout') }}</a></li>
      </ul>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n';
import { ref } from 'vue';
import keycloak from '../services/keycloak';
import { useRouter } from 'vue-router';
import { isRealmAdmin } from '../services/adminAccess';

const { t } = useI18n();
const router = useRouter();
const username = ref('');

if (keycloak.tokenParsed?.preferred_username) {
  username.value = keycloak.tokenParsed.preferred_username;
}

const logout = () => {
  keycloak.logout({ redirectUri: window.location.origin });
};

const accountManagement = () => {
  router.push('/settings');
};
</script>

<style scoped>
.sidebar {
  width: 280px;
  height: 100vh;
  position: fixed;
  top: 0;
  left: 0;
  z-index: 1000;
  background-color: var(--bg-sidebar) !important;
  transition: all 0.3s;
}

.nav-link {
  color: var(--text-light);
  margin-bottom: 0.25rem;
  border-radius: var(--border-radius-md);
  transition: all 0.2s;
}

.nav-link:hover {
  background-color: rgba(255, 255, 255, 0.1);
  color: #fff;
}

.nav-link.active {
  background-color: var(--primary-color);
  color: #fff;
  box-shadow: var(--shadow-md);
}

.avatar {
  font-weight: 600;
}

@media (max-width: 768px) {
  .sidebar {
    transform: translateX(-100%);
  }
  
  .sidebar.show {
    transform: translateX(0);
  }
}
</style>
