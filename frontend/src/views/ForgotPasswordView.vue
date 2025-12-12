<template>
  <div class="forgot-password-page">
    <div class="forgot-password-container">
      <!-- Logo -->
      <div class="logo-section">
        <LogoIcon class="logo" />
        <h1>Subcycle</h1>
      </div>

      <!-- 標題 -->
      <div class="header">
        <h2>忘記密碼</h2>
        <p class="subtitle">請輸入您的電子郵件地址，我們將發送密碼重置連結給您</p>
      </div>

      <!-- 成功訊息 -->
      <el-alert
        v-if="emailSent"
        title="郵件已發送"
        type="success"
        :closable="false"
        class="alert"
      >
        <template #default>
          <p>如果該電子郵件存在於我們的系統中，您將收到密碼重置連結。</p>
          <p class="mt-2">請檢查您的收件匣（包含垃圾郵件資料夾）。</p>
        </template>
      </el-alert>

      <!-- 表單 -->
      <el-form
        v-if="!emailSent"
        ref="formRef"
        :model="form"
        :rules="rules"
        @submit.prevent="handleSubmit"
        class="forgot-form"
      >
        <el-form-item prop="email">
          <el-input
            v-model="form.email"
            type="email"
            placeholder="請輸入電子郵件"
            size="large"
            :prefix-icon="Message"
            :disabled="loading"
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            @click="handleSubmit"
            class="submit-btn"
          >
            {{ loading ? '發送中...' : '發送重置連結' }}
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 返回登入 -->
      <div class="back-to-login">
        <el-link
          type="primary"
          :underline="false"
          @click="goToLogin"
        >
          <el-icon><ArrowLeft /></el-icon>
          返回登入
        </el-link>
      </div>

      <!-- 重新發送 -->
      <div v-if="emailSent" class="resend-section">
        <p class="resend-text">沒有收到郵件？</p>
        <el-button
          text
          type="primary"
          :disabled="resendCountdown > 0"
          @click="handleResend"
        >
          {{ resendCountdown > 0 ? `${resendCountdown}秒後可重新發送` : '重新發送' }}
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Message, ArrowLeft } from '@element-plus/icons-vue'
import LogoIcon from '@/components/icons/LogoIcon.vue'
import axios from 'axios'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const emailSent = ref(false)
const resendCountdown = ref(0)

const form = reactive({
  email: ''
})

const rules = {
  email: [
    { required: true, message: '請輸入電子郵件', trigger: 'blur' },
    { type: 'email', message: '請輸入有效的電子郵件格式', trigger: 'blur' }
  ]
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true

    try {
      // TODO: 連接後端 API
      const response = await axios.post('/api/auth/forgot-password', {
        email: form.email
      })

      if (response.data.success) {
        emailSent.value = true
        startResendCountdown()
        ElMessage.success('郵件發送成功')
      }
    } catch (error) {
      if (error.response?.status === 429) {
        ElMessage.warning('請稍後再試，您剛才已申請過密碼重置')
      } else {
        ElMessage.error(error.response?.data?.error || '發送失敗，請稍後再試')
      }
    } finally {
      loading.value = false
    }
  })
}

const handleResend = () => {
  emailSent.value = false
  form.email = ''
}

const startResendCountdown = () => {
  resendCountdown.value = 60
  const timer = setInterval(() => {
    resendCountdown.value--
    if (resendCountdown.value <= 0) {
      clearInterval(timer)
    }
  }, 1000)
}

const goToLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
.forgot-password-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, rgba(141, 234, 195, 0.18) 0%, rgba(91, 141, 239, 0.2) 100%);
  padding: 20px;
}

.forgot-password-container {
  background: white;
  border-radius: 16px;
  padding: 40px;
  width: 100%;
  max-width: 480px;
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
  line-height: 1.6;
  margin: 0;
}

.alert {
  margin-bottom: 24px;
}

.mt-2 {
  margin-top: 8px;
}

.forgot-form {
  margin-top: 24px;
}

.submit-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: 600;
}

.back-to-login {
  text-align: center;
  margin-top: 20px;
}

.back-to-login .el-link {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
}

.resend-section {
  text-align: center;
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid var(--border-light);
}

.resend-text {
  color: var(--text-secondary);
  font-size: 14px;
  margin: 0 0 8px 0;
}

@media (max-width: 576px) {
  .forgot-password-container {
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
