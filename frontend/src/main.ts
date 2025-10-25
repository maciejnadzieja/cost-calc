import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import i18n from './i18n'
import keycloak from './services/keycloak'
import './main.scss'
import 'bootstrap'
import 'bootstrap-icons/font/bootstrap-icons.css'

keycloak.init({ onLoad: 'check-sso' }).then(() => {
  const app = createApp(App)

  app.use(router)
  app.use(i18n)

  app.config.globalProperties.$keycloak = keycloak

  app.mount('#app')
}).catch((e) => {
    console.error('Failed to initialize Keycloak', e)
})
