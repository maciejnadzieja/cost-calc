<template>
  <div>
    <nav class="navbar navbar-expand-lg navbar-dark bg-primary">
      <div class="container">
        <router-link class="navbar-brand" to="/">{{ $t('app.name') }}</router-link>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav"
                aria-controls="navbarNav" aria-expanded="false" aria-label="Toggle navigation">
          <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="navbarNav">
          <ul class="navbar-nav me-auto">
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
          </ul>
          <ul class="navbar-nav">
            <li class="nav-item dropdown">
              <a class="nav-link dropdown-toggle" href="#" id="languageDropdown" role="button" 
                 data-bs-toggle="dropdown" aria-expanded="false">
                <i class="bi bi-globe"></i>
                <span>{{ $i18n.locale.toUpperCase() }}</span>
              </a>
              <ul class="dropdown-menu dropdown-menu-end">
                <li><a class="dropdown-item" href="#" @click.prevent="setLocale('en')">{{ $t('lang.english') }}</a></li>
                <li><a class="dropdown-item" href="#" @click.prevent="setLocale('pl')">{{ $t('lang.polish') }}</a></li>
              </ul>
            </li>
            <li class="nav-item">
              <router-link class="nav-link" to="/settings">
                <i class="bi bi-gear"></i> <span>{{ $t('nav.settings') }}</span>
              </router-link>
            </li>
            <li class="nav-item">
              <a class="nav-link" href="#" @click.prevent="logout">
                <i class="bi bi-box-arrow-right"></i> <span>{{ $t('nav.logout') }}</span>
              </a>
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

const { locale } = useI18n();

const setLocale = (newLocale: string) => {
  locale.value = newLocale;
};

const logout = () => {
  // Handle logout logic here
};
</script>

<style scoped>
</style>
