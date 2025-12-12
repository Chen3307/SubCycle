<template>
  <div class="reset-password-page">
    <div class="reset-password-container">
      <!-- Logo -->
      <div class="logo-section">
        <LogoIcon class="logo" />
        <h1>Subcycle</h1>
      </div>

      <!-- 驗證中 -->
      <div v-if="verifying" class="loading-section">
        <el-icon class="is-loading" size="40"><Loading /></el-icon>
        <p>驗證重置連結中...</p>
      </div>

      <!-- Token 無效 -->
      <div v-else-if="!tokenValid" class="error-section">
        <el-result
          icon="error"
          title="連結無效或已過期"
          sub-title="此密碼重置連結已失效，請重新申請"
        >
          <template #extra>
            <el-button type="primary" @click="goToForgotPassword">
              重新申請
            </el-button>
          </template>
        </el-result>
      </div>

      <!-- 重置表單 -->
      <div v-else>
        <div class="header">
          <h2>設定新密碼</h2>
          <p class="subtitle">為 {{ userEmail }} 設定新密碼</p>
        </div>

        <!-- 成功訊息 -->
        <el-result
          v-if="resetSuccess"
          icon="success"
          title="密碼重置成功"
          sub-title="您的密碼已成功重置，請使用新密碼登入"
        >
          <template #extra>
            <el-button type="primary" @click="goToLogin">
              前往登入
            </el-button>
          </template>
        </el-result>

        <!-- 表單 -->
        <el-form
          v-else
          ref="formRef"
          :model="form"
          :rules="rules"
          @submit.prevent="handleSubmit"
          class="reset-form"
        >
          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="請輸入新密碼"
              size="large"
              :prefix-icon="Lock"
              show-password
              :disabled="loading"
            />
          </el-form-item>

          <el-form-item prop="confirmPassword">
            <el-input
              v-model="form.confirmPassword"
              type="password"
              placeholder="請再次輸入新密碼"
              size="large"
              :prefix-icon="Lock"
              show-password
              :disabled="loading"
            />
          </el-form-item>

          <!-- 密碼強度指示 -->
          <div class="password-strength">
            <div class="strength-bar">
              <div
                class="strength-bar-fill"
                :class="`strength-${passwordStrength}`"
                :style="{ width: passwordStrengthPercentage + '%' }"
              ></div>
            </div>
            <p class="strength-text" :class="`strength-${passwordStrength}`">
              {{ passwordStrengthText }}
            </p>
          </div>

          <!-- 密碼要求 -->
          <div class="password-requirements">
            <p class="requirements-title">密碼必須包含：</p>
            <ul class="requirements-list">
              <li :class="{ valid: hasMinLength }">
                <el-icon><Check v-if="hasMinLength" /><Close v-else /></el-icon>
                至少 8 個字元
              </li>
              <li :class="{ valid: hasUpperCase }">
                <el-icon><Check v-if="hasUpperCase" /><Close v-else /></el-icon>
                至少一個大寫字母
              </li>
              <li :class="{ valid: hasLowerCase }">
                <el-icon><Check v-if="hasLowerCase" /><Close v-else /></el-icon>
                至少一個小寫字母
              </li>
              <li :class="{ valid: hasNumber }">
                <el-icon><Check v-if="hasNumber" /><Close v-else /></el-icon>
                至少一個數字
              </li>
            </ul>
          </div>

          <el-form-item>
            <el-button
              type="primary"
              size="large"
              :loading="loading"
              @click="handleSubmit"
              class="submit-btn"
            >
              {{ loading ? '重置中...' : '重置密碼' }}
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Lock, Loading, Check, Close } from '@element-plus/icons-vue'
import LogoIcon from '@/components/icons/LogoIcon.vue'
import axios from 'axios'

const router = useRouter()
const route = useRoute()
const formRef = ref(null)
const loading = ref(false)
const verifying = ref(true)
const tokenValid = ref(false)
const resetSuccess = ref(false)
const userEmail = ref('')

const form = reactive({
  password: '',
  confirmPassword: ''
})

// 密碼驗證規則
const validatePassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('請輸入密碼'))
  } else if (!isPasswordValid.value) {
    callback(new Error('密碼不符合要求'))
  } else {
    callback()
  }
}

const validateConfirmPassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('請再次輸入密碼'))
  } else if (value !== form.password) {
    callback(new Error('兩次輸入的密碼不一致'))
  } else {
    callback()
  }
}

const rules = {
  password: [{ validator: validatePassword, trigger: 'blur' }],
  confirmPassword: [{ validator: validateConfirmPassword, trigger: 'blur' }]
}

// 密碼強度檢查
const hasMinLength = computed(() => form.password.length >= 8)
const hasUpperCase = computed(() => /[A-Z]/.test(form.password))
const hasLowerCase = computed(() => /[a-z]/.test(form.password))
const hasNumber = computed(() => /\d/.test(form.password))

const isPasswordValid = computed(() => {
  return hasMinLength.value && hasUpperCase.value && hasLowerCase.value && hasNumber.value
})

const passwordStrength = computed(() => {
  if (!form.password) return 'none'

  let strength = 0
  if (hasMinLength.value) strength++
  if (hasUpperCase.value) strength++
  if (hasLowerCase.value) strength++
  if (hasNumber.value) strength++
  if (form.password.length >= 12) strength++
  if (/[!@#$%^&*(),.?":{}|<>]/.test(form.password)) strength++

  if (strength <= 2) return 'weak'
  if (strength <= 4) return 'medium'
  return 'strong'
})

const passwordStrengthPercentage = computed(() => {
  const strength = passwordStrength.value
  if (strength === 'weak') return 33
  if (strength === 'medium') return 66
  if (strength === 'strong') return 100
  return 0
})

const passwordStrengthText = computed(() => {
  const strength = passwordStrength.value
  if (strength === 'weak') return '密碼強度：弱'
  if (strength === 'medium') return '密碼強度：中等'
  if (strength === 'strong') return '密碼強度：強'
  return ''
})

// 驗證 token
const verifyToken = async () => {
  const token = route.query.token

  if (!token) {
    tokenValid.value = false
    verifying.value = false
    return
  }

  try {
    // TODO: 連接後端 API
    const response = await axios.get(`/api/auth/verify-reset-token/${token}`)

    if (response.data.success) {
      tokenValid.value = true
      userEmail.value = response.data.email
    }
  } catch (error) {
    tokenValid.value = false
    ElMessage.error(error.response?.data?.error || '驗證失敗')
  } finally {
    verifying.value = false
  }
}

// 提交重置
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true

    try {
      const token = route.query.token

      // TODO: 連接後端 API
      const response = await axios.post('/api/auth/reset-password', {
        token,
        newPassword: form.password
      })

      if (response.data.success) {
        resetSuccess.value = true
        ElMessage.success('密碼重置成功')
      }
    } catch (error) {
      ElMessage.error(error.response?.data?.error || '重置失敗，請稍後再試')
    } finally {
      loading.value = false
    }
  })
}

const goToLogin = () => {
  router.push('/login')
}

const goToForgotPassword = () => {
  router.push('/forgot-password')
}

onMounted(() => {
  verifyToken()
})
</script>

<style scoped>
.reset-password-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, rgba(141, 234, 195, 0.18) 0%, rgba(91, 141, 239, 0.2) 100%);
  padding: 20px;
}

.reset-password-container {
  background: white;
  border-radius: 16px;
  padding: 40px;
  width: 100%;
  max-width: 500px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.logo-section {
  text-align: center;
  margin-bottom: 30px;
}

.logo {
  width: 60px;
  height: 60px;
  margin-bottom: 10px;
}

.logo-section h1 {
  font-size: 28px;
  font-weight: bold;
  color: var(--accent-blue);
  margin: 0;
}

.loading-section {
  text-align: center;
  padding: 40px 0;
}

.loading-section p {
  margin-top: 16px;
  color: var(--text-secondary);
}

.error-section {
  margin-top: 20px;
}

.header {
  text-align: center;
  margin-bottom: 30px;
}

.header h2 {
  font-size: 24px;
  color: var(--text-primary);
  margin: 0 0 10px 0;
}

.subtitle {
  color: var(--text-secondary);
  font-size: 14px;
  margin: 0;
}

.reset-form {
  margin-top: 24px;
}

.password-strength {
  margin-bottom: 20px;
}

.strength-bar {
  height: 4px;
  background-color: var(--border-light);
  border-radius: 2px;
  overflow: hidden;
  margin-bottom: 8px;
}

.strength-bar-fill {
  height: 100%;
  transition: all 0.3s ease;
}

.strength-bar-fill.strength-weak {
  background-color: var(--danger-color);
}

.strength-bar-fill.strength-medium {
  background-color: var(--accent-blue);
}

.strength-bar-fill.strength-strong {
  background-color: var(--success-color);
}

.strength-text {
  font-size: 13px;
  margin: 0;
}

.strength-text.strength-weak {
  color: var(--danger-color);
}

.strength-text.strength-medium {
  color: var(--accent-blue);
}

.strength-text.strength-strong {
  color: var(--success-color);
}

.password-requirements {
  background-color: var(--bg-secondary);
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 20px;
}

.requirements-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 12px 0;
}

.requirements-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.requirements-list li {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--text-tertiary);
  margin-bottom: 8px;
  transition: color 0.3s ease;
}

.requirements-list li:last-child {
  margin-bottom: 0;
}

.requirements-list li.valid {
  color: var(--success-color);
}

.requirements-list li .el-icon {
  font-size: 16px;
}

.submit-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: 600;
}

@media (max-width: 576px) {
  .reset-password-container {
    padding: 30px 20px;
  }

  .header h2 {
    font-size: 20px;
  }

  .subtitle {
    font-size: 13px;
  }
}
</style>
