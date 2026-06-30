import { defineStore } from 'pinia'
import http from '../api/http'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('ticketflow_token') || '',
    user: null,
    routes: []
  }),
  actions: {
    async login(form) {
      const result = await http.post('/auth/login', form)
      this.token = result.token
      localStorage.setItem('ticketflow_token', result.token)
      await this.loadCurrentUser()
    },
    async loadCurrentUser() {
      this.user = await http.get('/auth/me')
      this.routes = await http.get('/auth/routes')
    },
    async logout() {
      try {
        await http.post('/auth/logout')
      } finally {
        this.token = ''
        this.user = null
        this.routes = []
        localStorage.removeItem('ticketflow_token')
      }
    }
  }
})
