<template>
  <div class="container">
    <div class="row justify-content-center mt-5">
      <div class="col-md-6 col-lg-4">
        <div class="card shadow">
          <div class="card-body">
            <h3 class="card-title text-center mb-4">{{ $t('app.name') }}</h3>
            
            <div v-if="error" class="alert alert-danger">{{ $t('login.error') }}</div>
            
            <form @submit.prevent="login">
              <div class="mb-3">
                <label for="username" class="form-label">{{ $t('login.email') }}</label>
                <input type="email" class="form-control" id="username" v-model="email" required autofocus>
              </div>
              <div class="mb-3">
                <label for="password" class="form-label">{{ $t('login.password') }}</label>
                <input type="password" class="form-control" id="password" v-model="password" required>
              </div>
              <div class="mb-3 form-check">
                <input type="checkbox" class="form-check-input" id="remember-me" v-model="rememberMe">
                <label class="form-check-label" for="remember-me">{{ $t('login.remember_me') }}</label>
              </div>
              <div class="d-grid">
                <button type="submit" class="btn btn-primary">{{ $t('login.submit') }}</button>
              </div>
            </form>
            
            <div class="text-center mt-3">
              <div class="mb-3">
                <span>{{ $t('login.or') }}</span>
              </div>
              <a href="/oauth2/authorization/google" class="btn btn-outline-dark w-100">
                <img src="https://www.google.com/favicon.ico" alt="Google" width="20" height="20" class="me-2">
                <span>{{ $t('login.google') }}</span>
              </a>
            </div>
            
            <div class="text-center mt-3">
              <router-link to="/register" class="text-decoration-none">{{ $t('login.register') }}</router-link>
              <span class="mx-2">|</span>
              <router-link to="/forgot-password" class="text-decoration-none">{{ $t('login.forgot_password') }}</router-link>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';

const email = ref('');
const password = ref('');
const rememberMe = ref(false);
const error = ref(false);

const login = () => {
  // Handle login logic here
};
</script>

<style scoped>
</style>
