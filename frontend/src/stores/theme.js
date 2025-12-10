import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

export const useThemeStore = defineStore('theme', () => {
  // 從 localStorage 讀取保存的主題設定，預設為 false (淺色模式)
  const darkMode = ref(localStorage.getItem('darkMode') === 'true')

  // 切換深色模式
  const toggleDarkMode = () => {
    darkMode.value = !darkMode.value
  }

  // 設定深色模式
  const setDarkMode = (value) => {
    darkMode.value = value
  }

  // 監聽 darkMode 變化，同步到 localStorage 和 HTML 元素
  watch(darkMode, (newValue) => {
    // 保存到 localStorage
    localStorage.setItem('darkMode', newValue)

    // 在 HTML 根元素上添加/移除 dark 類
    if (newValue) {
      document.documentElement.classList.add('dark')
    } else {
      document.documentElement.classList.remove('dark')
    }
  }, { immediate: true })

  return {
    darkMode,
    toggleDarkMode,
    setDarkMode
  }
})
