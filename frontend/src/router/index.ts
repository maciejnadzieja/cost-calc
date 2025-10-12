import { createRouter, createWebHistory } from 'vue-router'
import DefaultLayout from '../layouts/DefaultLayout.vue'
import keycloak from '../services/keycloak'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'main',
      component: () => import('../views/MainView.vue')
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue')
    },
    {
      path: '/dashboard',
      component: DefaultLayout,
      meta: { requiresAuth: true },
      children: [
        {
          path: '',
          name: 'dashboard',
          component: () => import('../views/DashboardView.vue')
        }
      ]
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
