import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAuthStore = defineStore('auth', () => {
  const user = ref(null)
  const token = ref(localStorage.getItem('token') || '')

  const login = async (email, password) => {
    try {
      // TODO: 連接後端 API
      // 暫時使用假資料
      const mockUser = { id: 1, email, name: '測試用戶' }
      const mockToken = 'mock-jwt-token-' + Date.now()

      user.value = mockUser
      token.value = mockToken
      localStorage.setItem('token', mockToken)
      localStorage.setItem('user', JSON.stringify(mockUser))

      return { success: true }
    } catch (error) {
      return { success: false, message: error.message }
    }
  }

  const register = async (email, password, name) => {
    try {
      // TODO: 連接後端 API
      // 暫時使用假資料
      const mockUser = { id: 1, email, name }
      const mockToken = 'mock-jwt-token-' + Date.now()

      user.value = mockUser
      token.value = mockToken
      localStorage.setItem('token', mockToken)
      localStorage.setItem('user', JSON.stringify(mockUser))

      return { success: true }
    } catch (error) {
      return { success: false, message: error.message }
    }
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
    logout,
    checkAuth
  }
})
