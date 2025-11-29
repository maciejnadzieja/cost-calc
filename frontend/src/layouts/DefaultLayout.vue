<template>
  <div class="app-layout d-flex">
    <!-- Sidebar -->
    <Sidebar :class="{ 'show': isSidebarOpen }" />
    
    <!-- Main Content -->
    <div class="main-content flex-grow-1 d-flex flex-column" :class="{ 'sidebar-open': isSidebarOpen }">
      <Topbar @toggle-sidebar="toggleSidebar" />
      
      <main class="p-4 overflow-auto">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
    
    <!-- Overlay for mobile -->
    <div v-if="isSidebarOpen" class="sidebar-overlay d-md-none" @click="toggleSidebar"></div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import Sidebar from '../components/Sidebar.vue';
import Topbar from '../components/Topbar.vue';

const isSidebarOpen = ref(false);

const toggleSidebar = () => {
  isSidebarOpen.value = !isSidebarOpen.value;
};
</script>

<style scoped>
.app-layout {
  min-height: 100vh;
  background-color: var(--bg-body);
}

.main-content {
  margin-left: 280px; /* Sidebar width */
  transition: margin-left 0.3s;
  min-height: 100vh;
}

.sidebar-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 950;
}

/* Transitions */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

@media (max-width: 768px) {
  .main-content {
    margin-left: 0;
  }
}
</style>
