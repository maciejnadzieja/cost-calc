<template>
  <div></div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import keycloak from '../services/keycloak'

const router = useRouter()

onMounted(() => {
  if (keycloak.authenticated) {
    router.push('/dashboard')
  } else {
    keycloak.login()
  }
})
</script>
