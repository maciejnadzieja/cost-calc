import { createRouter, createWebHistory, type RouteLocationNormalized } from 'vue-router'
import DefaultLayout from '../layouts/DefaultLayout.vue'
import keycloak from '../services/keycloak'
import { isRealmAdmin } from '../services/adminAccess'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      component: DefaultLayout,
      meta: { requiresAuth: true },
      children: [
        {
          path: 'admin/users',
          name: 'admin-users',
          meta: { requiresAdmin: true },
          component: () => import('../views/AdminUsersView.vue')
        },
        {
          path: 'access-denied',
          name: 'access-denied',
          component: () => import('../views/AccessDeniedView.vue')
        },
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
        },
        {
          path: 'calculations',
          name: 'calculations',
          component: () => import('../views/CalculationListView.vue')
        },
        {
          path: 'calculations/new',
          name: 'calculation-new',
          component: () => import('../views/CalculationForm.vue')
        },
        {
          path: 'calculations/edit/:id',
          name: 'calculation-edit',
          component: () => import('../views/CalculationForm.vue')
        }
      ]
    },
    {
      path: '/calculations/print/:id',
      name: 'calculation-print',
      component: () => import('../views/CalculationPrintView.vue')
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue')
    }
  ]
})

export const navigationGuard = (to: RouteLocationNormalized) => {
  if (to.meta.requiresAuth && !keycloak.authenticated) {
    keycloak.login()
    return false
  }
  if (to.meta.requiresAdmin && !isRealmAdmin()) return { name: 'access-denied' }
  return true
}

router.beforeEach(navigationGuard)

export default router
