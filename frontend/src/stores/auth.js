import { defineStore } from 'pinia'
import { ref } from 'vue'
import { authAPI } from '../api/index.js'

export const useAuthStore = defineStore('auth', () => {
  const user = ref(null)
  const token = ref(localStorage.getItem('token') || '')

  const login = async (email, password) => {
    try {
      const response = await authAPI.login(email, password)
      const { token: jwtToken, userId, email: userEmail, name, role } = response.data

      const userData = { id: userId, email: userEmail, name, role }
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

      // 註冊成功後不再自動登入，需要先驗證 Email
      // 後端會返回訊息提示用戶去驗證 Email
      const message = response.data?.message || '註冊成功！請檢查您的信箱完成 Email 驗證'

      return { success: true, message }
    } catch (error) {
      const message = error.response?.data?.message || '註冊失敗，請稍後再試'
      return { success: false, message }
    }
  }

  const logout = () => {
    // 立即清除内存中的状态（同步）
    user.value = null
    token.value = ''

    localStorage.removeItem('token')
    localStorage.removeItem('user')
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
