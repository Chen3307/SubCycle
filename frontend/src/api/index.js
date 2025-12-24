import axios from 'axios'

// 使用環境變量或相對路徑，在 Docker 環境下會通過 Nginx 代理到後端
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api'

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 請求攔截器 - 自動添加 JWT token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 回應攔截器 - 處理錯誤
api.interceptors.response.use(
  (response) => {
    return response
  },
  (error) => {
    if (error.response?.status === 401) {
      // Token 過期或無效，清除本地存儲並跳轉到登入頁
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

// 認證 API
export const authAPI = {
  login: (email, password) => {
    return api.post('/auth/login', { email, password })
  },
  register: (email, password, name) => {
    return api.post('/auth/register', { email, password, name })
  },
  resendVerification: (email) => {
    return api.post('/auth/resend-verification', { email })
  }
}

// 類別 API
export const categoryAPI = {
  getAll: () => api.get('/categories'),
  create: (payload) => api.post('/categories', payload),
  update: (id, payload) => api.put(`/categories/${id}`, payload),
  remove: (id) => api.delete(`/categories/${id}`)
}

// 訂閱 API
export const subscriptionAPI = {
  getAll: () => api.get('/subscriptions'),
  create: (payload) => api.post('/subscriptions', payload),
  update: (id, payload) => api.put(`/subscriptions/${id}`, payload),
  remove: (id) => api.delete(`/subscriptions/${id}`),
  rollover: () => api.post('/subscriptions/rollover'),
  updateNotifications: (enabled) => api.put('/subscriptions/notifications', { enabled })
}

// 訂閱模板 API
export const subscriptionTemplateAPI = {
  getAll: () => api.get('/subscription-templates'),
  getByCategory: (categoryName) => api.get(`/subscription-templates/category/${categoryName}`),
  search: (keyword) => api.get('/subscription-templates/search', { params: { keyword } })
}

// 儀表板 API
export const dashboardAPI = {
  getStatistics: () => api.get('/dashboard/statistics')
}

// 使用者 API
export const userAPI = {
  getProfile: () => api.get('/user/profile'),
  updateProfile: (payload) => api.put('/user/profile', payload),
  changePassword: (payload) => api.put('/user/password', payload)
}

// 管理者 API
export const adminAPI = {
  // 用戶管理
  getUsers: (page = 0, size = 10, search = '') => {
    const params = { page, size }
    if (search) params.search = search
    return api.get('/admin/users', { params })
  },
  createUser: (payload) => api.post('/admin/users', payload),
  updateUser: (id, payload) => api.put(`/admin/users/${id}`, payload),
  updateUserStatus: (id, isActive) => api.put(`/admin/users/${id}/status`, { isActive }),
  resetUserPassword: (id, newPassword) => api.post(`/admin/users/${id}/reset-password`, { newPassword }),
  deleteUser: (id) => api.delete(`/admin/users/${id}`)
}

export default api
