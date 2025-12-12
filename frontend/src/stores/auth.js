import { defineStore } from 'pinia'
import { ref } from 'vue'
import { authAPI } from '../api/index.js'

export const useAuthStore = defineStore('auth', () => {
  const user = ref(null)
  const token = ref(localStorage.getItem('token') || '')

  const login = async (email, password) => {
    try {
      const response = await authAPI.login(email, password)
      const { token: jwtToken, userId, email: userEmail, name } = response.data

      const userData = { id: userId, email: userEmail, name }
      user.value = userData
      token.value = jwtToken
      localStorage.setItem('token', jwtToken)
      localStorage.setItem('user', JSON.stringify(userData))

      return { success: true }
    } catch (error) {
      const message = error.response?.data?.message || '登入失敗，請檢查您的電子郵件和密碼'
      return { success: false, message }
    }
  }

  const register = async (email, password, name) => {
    try {
      const response = await authAPI.register(email, password, name)
      const { token: jwtToken, userId, email: userEmail, name: userName } = response.data

      const userData = { id: userId, email: userEmail, name: userName }
      user.value = userData
      token.value = jwtToken
      localStorage.setItem('token', jwtToken)
      localStorage.setItem('user', JSON.stringify(userData))

      return { success: true }
    } catch (error) {
      const message = error.response?.data?.message || '註冊失敗，請稍後再試'
      return { success: false, message }
    }
  }

  const mockLogin = (payload = {}) => {
    const timestamp = Date.now()
    const mockUser = {
      id: payload.id || `mock-${timestamp}`,
      email: payload.email || 'preview@subcycle.app',
      name: payload.name || '前端預覽用戶'
    }
    const mockToken = payload.token || `mock-token-${timestamp}`

    user.value = mockUser
    token.value = mockToken
    localStorage.setItem('token', mockToken)
    localStorage.setItem('user', JSON.stringify(mockUser))

    return Promise.resolve({ success: true, mock: true, user: mockUser })
  }

  const logout = () => {
    // 立即清除内存中的状态（同步）
    user.value = null
    token.value = ''

    // 异步清理 localStorage，避免阻塞 UI
    requestIdleCallback(() => {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    }, { timeout: 100 })
  }

  const checkAuth = () => {
    const savedToken = localStorage.getItem('token')
    const savedUser = localStorage.getItem('user')

    if (savedToken && savedUser) {
      token.value = savedToken
      user.value = JSON.parse(savedUser)
    }
  }

  return {
    user,
    token,
    login,
    register,
    mockLogin,
    logout,
    checkAuth
  }
})
