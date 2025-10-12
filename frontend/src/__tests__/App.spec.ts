import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import App from '../App.vue'

describe('App', () => {
  it('renders router-view', () => {
    const router = createRouter({
      history: createWebHistory(),
      routes: [
        {
          path: '/',
          name: 'home',
          component: {
            template: '<div></div>'
          }
        }
      ]
    })

    const wrapper = mount(App, {
      global: {
        plugins: [router]
      }
    })

    expect(wrapper.findComponent({ name: 'RouterView' }).exists()).toBe(true)
  })
})
