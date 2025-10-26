<template>
  <div>
    <nav class="navbar navbar-expand-lg navbar-dark bg-primary">
      <div class="container">
        <router-link class="navbar-brand" to="/">
          <span>{{ $t('app.name') }}</span>
        </router-link>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav"
                aria-controls="navbarNav" aria-expanded="false" aria-label="Toggle navigation">
          <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="navbarNav">
          <ul class="navbar-nav me-auto">
            <li class="nav-item">
              <router-link class="nav-link" to="/dashboard">
                <i class="bi bi-speedometer2"></i> <span>{{ $t('nav.dashboard') }}</span>
              </router-link>
            </li>
            <li class="nav-item">
              <router-link class="nav-link" to="/calculations">
                <i class="bi bi-calculator"></i> <span>{{ $t('nav.calculations') }}</span>
              </router-link>
            </li>
            <li class="nav-item">
              <router-link class="nav-link" to="/services">
                <i class="bi bi-gear"></i> <span>{{ $t('nav.services') }}</span>
              </router-link>
            </li>
            <li class="nav-item">
              <router-link class="nav-link" to="/materials">
                <i class="bi bi-box-seam"></i> <span>{{ $t('nav.materials') }}</span>
              </router-link>
            </li>
            <li class="nav-item">
              <router-link class="nav-link" to="/settings">
                <i class="bi bi-sliders"></i> <span>{{ $t('nav.settings') }}</span>
              </router-link>
            </li>
          </ul>
          <ul class="navbar-nav">
            <li class="nav-item dropdown">
              <a class="nav-link dropdown-toggle" href="#" id="languageDropdown" role="button"
                 data-bs-toggle="dropdown" aria-expanded="false">
                <i class="bi bi-globe me-2"></i>
                <span>{{ $i18n.locale.toUpperCase() }}</span>
              </a>
              <ul class="dropdown-menu dropdown-menu-end">
                <li><a class="dropdown-item" href="#" @click.prevent="setLocale('en')">{{ $t('lang.english') }}</a></li>
                <li><a class="dropdown-item" href="#" @click.prevent="setLocale('pl')">{{ $t('lang.polish') }}</a></li>
              </ul>
            </li>
            <li class="nav-item dropdown">
              <a class="nav-link dropdown-toggle" href="#" id="userDropdown" role="button" data-bs-toggle="dropdown" aria-expanded="false">
                <i class="bi bi-person-circle me-2"></i>
                <span>{{ username }}</span>
              </a>
              <ul class="dropdown-menu dropdown-menu-end">
                <li><a class="dropdown-item" href="#" @click.prevent="accountManagement">{{ $t('nav.account') }}</a></li>
                <li><hr class="dropdown-divider"></li>
                <li><a class="dropdown-item" href="#" @click.prevent="logout">{{ $t('nav.logout') }}</a></li>
              </ul>
            </li>
          </ul>
        </div>
      </div>
    </nav>
    <div class="container mt-4">
      <router-view />
    </div>
  </div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n';
import { ref } from 'vue';
import keycloak from '../services/keycloak';
import { useRouter } from 'vue-router';

const { locale } = useI18n();
const username = ref('');
const router = useRouter();

if (keycloak.tokenParsed?.preferred_username) {
  username.value = keycloak.tokenParsed.preferred_username;
}

keycloak.onAuthSuccess = () => {
  if (keycloak.tokenParsed?.preferred_username) {
    username.value = keycloak.tokenParsed.preferred_username;
  }
};

const setLocale = (newLocale: string) => {
  locale.value = newLocale;
};

const logout = () => {
  keycloak.logout({ redirectUri: window.location.origin });
};

const accountManagement = () => {
  router.push('/settings');
}
</script>

<style scoped>
</style>
