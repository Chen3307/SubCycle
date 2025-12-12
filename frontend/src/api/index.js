import axios from 'axios'

const API_BASE_URL = 'http://localhost:8080/api'

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
  getCurrentUser: () => {
    return api.get('/auth/me')
  }
}

// 類別 API
export const categoryAPI = {
  getAll: () => api.get('/categories'),
  create: (payload) => api.post('/categories', payload)
}

// 訂閱 API
export const subscriptionAPI = {
  getAll: () => api.get('/subscriptions'),
  create: (payload) => api.post('/subscriptions', payload),
  update: (id, payload) => api.put(`/subscriptions/${id}`, payload),
  remove: (id) => api.delete(`/subscriptions/${id}`)
}

export default api
