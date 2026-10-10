import apiClient from './api'

export interface AdminUser {
  username: string
  email: string | null
  enabled: boolean
}

export interface AdminUserPage {
  content: AdminUser[]
  number: number
  size: number
  hasNext: boolean
}

export const getAdminUsers = (page: number) =>
  apiClient.get<AdminUserPage>('/admin/users', { params: { page } })
