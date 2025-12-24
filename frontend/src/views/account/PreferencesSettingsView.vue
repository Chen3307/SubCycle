<template>
  <div class="settings-page">
    <el-card class="settings-card">
      <template #header>
        <h3>偏好設定</h3>
      </template>
      <div class="preference-settings">
        <div class="setting-item">
          <div class="setting-info">
            <div class="setting-label">貨幣單位</div>
            <div class="setting-desc">顯示金額的貨幣單位</div>
          </div>
          <el-select v-model="preferences.currency" style="width: 150px">
            <el-option label="新台幣 (NT$)" value="TWD" />
            <el-option label="美元 ($)" value="USD" />
          </el-select>
        </div>

        <div class="setting-item">
          <div class="setting-info">
            <div class="setting-label">日期格式</div>
            <div class="setting-desc">日期顯示格式</div>
          </div>
          <el-select v-model="preferences.dateFormat" style="width: 200px">
            <el-option label="YYYY-MM-DD" value="YYYY-MM-DD" />
            <el-option label="DD/MM/YYYY" value="DD/MM/YYYY" />
            <el-option label="MM/DD/YYYY" value="MM/DD/YYYY" />
          </el-select>
        </div>

        <div class="setting-item">
          <div class="setting-info">
            <div class="setting-label">深色模式</div>
            <div class="setting-desc">啟用深色介面主題</div>
          </div>
          <el-switch v-model="tempDarkMode" />
        </div>

        <div class="setting-actions">
          <el-button type="primary" @click="handleSavePreferences">
            儲存設定
          </el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, watch } from 'vue'
import { onBeforeRouteLeave } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useThemeStore } from '../../stores/theme'
import { userAPI } from '../../api/index'

const themeStore = useThemeStore()

const preferences = reactive({
  currency: 'TWD',
  dateFormat: 'YYYY-MM-DD'
})

const tempDarkMode = ref(themeStore.darkMode)
const initialDarkMode = ref(themeStore.darkMode)
const saved = ref(false)

const applyLocalPreferences = () => {
  const storedDateFormat = localStorage.getItem('preferences.dateFormat')
  if (storedDateFormat) {
    preferences.dateFormat = storedDateFormat
  }
}

const handleSavePreferences = () => {
  userAPI.updateProfile({ currency: preferences.currency })
    .then(() => {
      themeStore.persistDarkModeSetting()
      localStorage.setItem('preferences.dateFormat', preferences.dateFormat)
      ElMessage.success('偏好設定已儲存')
      saved.value = true
    })
    .catch((error) => {
      const message = error.response?.data?.message || '偏好設定更新失敗'
      ElMessage.error(message)
    })
}

const syncProfile = async () => {
  try {
    const { data } = await userAPI.getProfile()
    if (!data) return
    preferences.currency = data.currency || preferences.currency
  } catch (error) {
    const message = error.response?.data?.message || '無法取得使用者資料'
    ElMessage.error(message)
  }
}

onMounted(() => {
  applyLocalPreferences()
  tempDarkMode.value = themeStore.darkMode
  initialDarkMode.value = themeStore.darkMode
  syncProfile()
})

onBeforeRouteLeave(() => {
  if (!saved.value) {
    themeStore.setDarkModeTemporary(initialDarkMode.value)
  }
})

watch(
  () => tempDarkMode.value,
  (value) => {
    themeStore.setDarkModeTemporary(value)
  }
)
</script>

<style scoped>
.settings-page {
  width: 100%;
}

.settings-card h3 {
  margin: 0;
  font-size: 18px;
  color: var(--text-primary);
  transition: color 0.3s ease;
}

.preference-settings {
  padding: 20px 0;
}

.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 0;
  border-bottom: 1px solid #ebeef5;
}

.setting-item:last-child {
  border-bottom: none;
}

.setting-info {
  flex: 1;
}

.setting-label {
  font-size: 16px;
  color: var(--text-primary);
  font-weight: 500;
  margin-bottom: 4px;
  transition: color 0.3s ease;
}

.setting-desc {
  font-size: 14px;
  color: var(--text-tertiary);
  transition: color 0.3s ease;
}

.setting-actions {
  margin-top: 30px;
  padding-top: 20px;
  border-top: none;
}
</style>
