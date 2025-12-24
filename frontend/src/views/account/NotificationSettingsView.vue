<template>
  <div class="settings-page">
    <el-card class="settings-card">
      <template #header>
        <h3>通知設定</h3>
      </template>
      <div class="notification-settings">
        <div class="setting-item">
          <div class="setting-info">
            <div class="setting-label">電子郵件通知</div>
            <div class="setting-desc">接收訂閱到期提醒郵件</div>
          </div>
          <el-switch v-model="notificationSettings.email" />
        </div>

        <div class="setting-item">
          <div class="setting-info">
            <div class="setting-label">提前提醒天數</div>
            <div class="setting-desc">在扣款日前幾天發送提醒</div>
          </div>
          <el-input-number
            v-model="notificationSettings.reminderDays"
            :min="1"
            :max="30"
            style="width: 150px"
          />
        </div>

        <div class="setting-actions">
          <el-button type="primary" @click="handleSaveNotificationSettings">
            儲存設定
          </el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useSubscriptionStore } from '../../stores/subscription'
import { subscriptionAPI, userAPI } from '../../api/index'

const subscriptionStore = useSubscriptionStore()

const notificationSettings = reactive({
  email: true,
  reminderDays: 7
})
const notificationToggleInitialized = ref(false)
const seenSubscriptionLoad = ref(false)

const handleSaveNotificationSettings = () => {
  Promise.all([
    userAPI.updateProfile({ notificationDays: notificationSettings.reminderDays }),
    subscriptionAPI.updateNotifications(notificationSettings.email)
  ])
    .then(() => {
      ElMessage.success('通知設定已儲存')
      notificationToggleInitialized.value = false
      subscriptionStore.fetchSubscriptions()
    })
    .catch((error) => {
      const message = error.response?.data?.message || '通知設定更新失敗'
      ElMessage.error(message)
    })
}

const syncProfile = async () => {
  try {
    const { data } = await userAPI.getProfile()
    if (!data) return
    if (typeof data.notificationDays === 'number') {
      notificationSettings.reminderDays = data.notificationDays
    }
  } catch (error) {
    const message = error.response?.data?.message || '無法取得使用者資料'
    ElMessage.error(message)
  }
}

const initNotificationToggle = () => {
  if (notificationToggleInitialized.value) return
  const subs = subscriptionStore.subscriptions
  if (subscriptionStore.loading) return
  if (!seenSubscriptionLoad.value && subs.length === 0) return
  if (subs.length === 0) {
    notificationSettings.email = true
  } else {
    notificationSettings.email = subs.every((sub) => sub.notificationEnabled !== false)
  }
  notificationToggleInitialized.value = true
}

watch(
  () => subscriptionStore.loading,
  (loading) => {
    if (loading) {
      seenSubscriptionLoad.value = true
    }
    if (!loading) {
      initNotificationToggle()
    }
  },
  { immediate: true }
)

onMounted(() => {
  syncProfile()
  if (subscriptionStore.subscriptions.length > 0) {
    seenSubscriptionLoad.value = true
    initNotificationToggle()
  }
})
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

.notification-settings {
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
