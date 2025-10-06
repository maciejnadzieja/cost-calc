<template>
  <div class="container">
    <div class="row justify-content-center mt-5">
      <div class="col-md-8 col-lg-6">
        <div class="card shadow">
          <div class="card-body">
            <h1 class="card-title text-center mb-4">{{ $t('app.name') }}</h1>
            <p class="text-center mb-4">{{ $t('main.description') }}</p>
            <div class="d-grid">
              <router-link to="/login" class="btn btn-primary btn-lg">{{ $t('nav.login') }}</router-link>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
</script>

<style scoped>
</style>
