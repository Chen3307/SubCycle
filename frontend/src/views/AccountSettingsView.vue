<template>
  <div class="account-settings">
    <h1 class="page-title">帳戶設定</h1>

    <el-row :gutter="20">
      <!-- 左側設定選單 -->
      <el-col :span="6">
        <el-card class="settings-menu">
          <el-menu :default-active="activeMenu" @select="handleMenuSelect">
            <el-menu-item index="profile">
              <UserIcon />
              <span>個人資訊</span>
            </el-menu-item>
            <el-menu-item index="password">
              <el-icon><Lock /></el-icon>
              <span>修改密碼</span>
            </el-menu-item>
            <el-menu-item index="notification">
              <el-icon><Bell /></el-icon>
              <span>通知設定</span>
            </el-menu-item>
            <el-menu-item index="preferences">
              <el-icon><Setting /></el-icon>
              <span>偏好設定</span>
            </el-menu-item>
          </el-menu>
        </el-card>
      </el-col>

      <!-- 右側設定內容 -->
      <el-col :span="18">
        <!-- 個人資訊 -->
        <el-card v-show="activeMenu === 'profile'" class="settings-card">
          <template #header>
            <h3>個人資訊</h3>
          </template>
          <el-form
            ref="profileFormRef"
            :model="profileForm"
            :rules="profileRules"
            label-width="100px"
          >
            <el-form-item label="姓名" prop="name">
              <el-input v-model="profileForm.name" placeholder="請輸入姓名" />
            </el-form-item>

            <el-form-item label="電子郵件" prop="email">
              <el-input v-model="profileForm.email" placeholder="請輸入電子郵件" disabled />
              <el-text type="info" size="small">電子郵件無法修改</el-text>
            </el-form-item>

            <el-form-item label="電話" prop="phone">
              <el-input v-model="profileForm.phone" placeholder="請輸入電話號碼" />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="handleUpdateProfile">儲存變更</el-button>
              <el-button @click="resetProfileForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 修改密碼 -->
        <el-card v-show="activeMenu === 'password'" class="settings-card">
          <template #header>
            <h3>修改密碼</h3>
          </template>
          <el-form
            ref="passwordFormRef"
            :model="passwordForm"
            :rules="passwordRules"
            label-width="120px"
          >
            <el-form-item label="目前密碼" prop="currentPassword">
              <el-input
                v-model="passwordForm.currentPassword"
                type="password"
                placeholder="請輸入目前密碼"
                show-password
              />
            </el-form-item>

            <el-form-item label="新密碼" prop="newPassword">
              <el-input
                v-model="passwordForm.newPassword"
                type="password"
                placeholder="請輸入新密碼"
                show-password
              />
            </el-form-item>

            <el-form-item label="確認新密碼" prop="confirmPassword">
              <el-input
                v-model="passwordForm.confirmPassword"
                type="password"
                placeholder="請再次輸入新密碼"
                show-password
              />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="handleUpdatePassword">更新密碼</el-button>
              <el-button @click="resetPasswordForm">清除</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 通知設定 -->
        <el-card v-show="activeMenu === 'notification'" class="settings-card">
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

            <div class="setting-item">
              <div class="setting-info">
                <div class="setting-label">每日摘要</div>
                <div class="setting-desc">每天發送訂閱摘要報告</div>
              </div>
              <el-switch v-model="notificationSettings.dailyDigest" />
            </div>

            <div class="setting-item">
              <div class="setting-info">
                <div class="setting-label">每月報告</div>
                <div class="setting-desc">每月發送支出統計報告</div>
              </div>
              <el-switch v-model="notificationSettings.monthlyReport" />
            </div>

            <div class="setting-actions">
              <el-button type="primary" @click="handleSaveNotificationSettings">
                儲存設定
              </el-button>
            </div>
          </div>
        </el-card>

        <!-- 偏好設定 -->
        <el-card v-show="activeMenu === 'preferences'" class="settings-card">
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
                <el-option label="人民幣 (¥)" value="CNY" />
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
                <div class="setting-label">語言</div>
                <div class="setting-desc">介面顯示語言</div>
              </div>
              <el-select v-model="preferences.language" style="width: 150px">
                <el-option label="繁體中文" value="zh-TW" />
                <el-option label="English" value="en" />
                <el-option label="简体中文" value="zh-CN" />
              </el-select>
            </div>

            <div class="setting-item">
              <div class="setting-info">
                <div class="setting-label">深色模式</div>
                <div class="setting-desc">啟用深色介面主題</div>
              </div>
              <el-switch v-model="darkMode" />
            </div>

            <div class="setting-actions">
              <el-button type="primary" @click="handleSavePreferences">
                儲存設定
              </el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Lock, Bell, Setting } from '@element-plus/icons-vue'
import { useThemeStore } from '../stores/theme'
import UserIcon from '../components/UserIcon.vue'

const themeStore = useThemeStore()
const activeMenu = ref('profile')

// 個人資訊表單
const profileFormRef = ref(null)
const profileForm = reactive({
  name: '用戶',
  email: 'user@example.com',
  phone: ''
})

const profileRules = {
  name: [
    { required: true, message: '請輸入姓名', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '請輸入電子郵件', trigger: 'blur' },
    { type: 'email', message: '請輸入有效的電子郵件', trigger: 'blur' }
  ]
}

// 密碼表單
const passwordFormRef = ref(null)
const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('兩次輸入的密碼不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  currentPassword: [
    { required: true, message: '請輸入目前密碼', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '請輸入新密碼', trigger: 'blur' },
    { min: 6, message: '密碼長度至少 6 個字元', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '請再次輸入新密碼', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 通知設定
const notificationSettings = reactive({
  email: true,
  reminderDays: 7,
  dailyDigest: false,
  monthlyReport: true
})

// 偏好設定 - 使用 computed 連接 themeStore
const preferences = reactive({
  currency: 'TWD',
  dateFormat: 'YYYY-MM-DD',
  language: 'zh-TW'
})

// 深色模式使用 computed，直接連接到 themeStore
const darkMode = computed({
  get: () => themeStore.darkMode,
  set: (value) => themeStore.setDarkMode(value)
})

const handleMenuSelect = (index) => {
  activeMenu.value = index
}

const handleUpdateProfile = async () => {
  if (!profileFormRef.value) return

  await profileFormRef.value.validate((valid) => {
    if (valid) {
      // TODO: 實際 API 呼叫
      ElMessage.success('個人資訊已更新')
    }
  })
}

const resetProfileForm = () => {
  if (profileFormRef.value) {
    profileFormRef.value.resetFields()
  }
}

const handleUpdatePassword = async () => {
  if (!passwordFormRef.value) return

  await passwordFormRef.value.validate((valid) => {
    if (valid) {
      // TODO: 實際 API 呼叫
      ElMessage.success('密碼已更新')
      resetPasswordForm()
    }
  })
}

const resetPasswordForm = () => {
  Object.assign(passwordForm, {
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  })
  if (passwordFormRef.value) {
    passwordFormRef.value.clearValidate()
  }
}

const handleSaveNotificationSettings = () => {
  // TODO: 實際 API 呼叫
  ElMessage.success('通知設定已儲存')
}

const handleSavePreferences = () => {
  // TODO: 實際 API 呼叫
  ElMessage.success('偏好設定已儲存')
}
</script>

<style scoped>
.account-settings {
  max-width: 1400px;
  margin: 0 auto;
}

.page-title {
  margin: 0 0 24px 0;
  font-size: 28px;
  color: var(--text-primary);
  transition: color 0.3s ease;
}

.settings-menu {
  position: sticky;
  top: 20px;
}

.settings-menu .el-menu {
  border: none;
}

.settings-card {
  min-height: 400px;
}

.settings-card h3 {
  margin: 0;
  font-size: 18px;
  color: var(--text-primary);
  transition: color 0.3s ease;
}

.notification-settings,
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
  border-top: 1px solid #ebeef5;
}
</style>
