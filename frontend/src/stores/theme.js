import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

export const useThemeStore = defineStore('theme', () => {
  // 從 localStorage 讀取保存的主題設定，預設為 false (淺色模式)
  const darkMode = ref(localStorage.getItem('darkMode') === 'true')
  const persistDarkMode = ref(true)

  // 切換深色模式
  const toggleDarkMode = () => {
    darkMode.value = !darkMode.value
  }

  // 設定深色模式（預設會持久化）
  const setDarkMode = (value) => {
    persistDarkMode.value = true
    darkMode.value = value
  }

  // 暫時設定深色模式（不寫入 localStorage）
  const setDarkModeTemporary = (value) => {
    persistDarkMode.value = false
    darkMode.value = value
  }

  // 將目前深色模式設定寫入 localStorage
  const persistDarkModeSetting = () => {
    localStorage.setItem('darkMode', darkMode.value)
    persistDarkMode.value = true
  }

  // 監聽 darkMode 變化，同步到 localStorage
  watch(darkMode, (newValue) => {
    if (persistDarkMode.value) {
      // 保存到 localStorage
      localStorage.setItem('darkMode', newValue)
    }
  }, { immediate: true })

  return {
    darkMode,
    toggleDarkMode,
    setDarkMode
    ,
    setDarkModeTemporary,
    persistDarkModeSetting
  }
})
