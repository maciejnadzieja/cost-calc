import { createRouter, createWebHistory } from 'vue-router'
import DefaultLayout from '../layouts/DefaultLayout.vue'
import keycloak from '../services/keycloak'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      component: DefaultLayout,
      meta: { requiresAuth: true },
      children: [
        {
          path: '',
          redirect: '/dashboard'
        },
        {
          path: 'dashboard',
          name: 'dashboard',
          component: () => import('../views/DashboardView.vue')
        },
        {
          path: 'settings',
          name: 'settings',
          component: () => import('../views/SettingsView.vue')
        },
        {
          path: 'materials',
          name: 'materials',
          component: () => import('../views/MaterialsView.vue')
        },
        {
          path: 'services',
          name: 'services',
          component: () => import('../views/ServicesView.vue')
        },
        {
          path: 'services/create',
          name: 'service-create',
          component: () => import('../views/ServiceEditView.vue')
        },
        {
          path: 'services/edit/:id',
          name: 'service-edit',
          component: () => import('../views/ServiceEditView.vue')
        },
        {
          path: 'materials/create',
          name: 'material-create',
          component: () => import('../views/MaterialEditView.vue')
        },
        {
          path: 'materials/edit/:id',
          name: 'material-edit',
          component: () => import('../views/MaterialEditView.vue')
        }
      ]
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue')
    }
  ]
})

router.beforeEach(async (to, from, next) => {
  if (to.meta.requiresAuth) {
    if (keycloak.authenticated) {
      next()
    } else {
      keycloak.login()
    }
  } else {
    next()
  }
})

export default router
