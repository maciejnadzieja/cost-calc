<template>
  <div>
    <div class="row">
      <div class="col-12">
        <h2>{{ $t('dashboard.welcome') }}</h2>
        <p class="lead">{{ $t('dashboard.subtitle') }}</p>
      </div>
    </div>

    <div class="row mt-4">
      <div class="col-md-4">
        <div class="card">
          <div class="card-body">
            <h5 class="card-title">{{ $t('dashboard.quick_actions') }}</h5>
            <div class="list-group">
              <router-link to="/calculations/new" class="list-group-item list-group-item-action">{{ $t('dashboard.create_calculation') }}</router-link>
              <router-link to="/services/new" class="list-group-item list-group-item-action">{{ $t('dashboard.add_service') }}</router-link>
              <router-link to="/materials/new" class="list-group-item list-group-item-action">{{ $t('dashboard.add_material') }}</router-link>
            </div>
          </div>
        </div>
      </div>
      <div class="col-md-8">
        <div class="card">
          <div class="card-body">
            <h5 class="card-title">{{ $t('dashboard.recent_activity') }}</h5>
            <p>{{ $t('dashboard.recent_activity.empty') }}</p>
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
