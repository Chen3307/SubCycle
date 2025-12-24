<template>
  <div class="login-container">
    <div class="glow mint"></div>
    <div class="glow blue"></div>
    <el-card class="login-card">
      <template #header>
        <div class="card-header">
          <h2>Subcycle</h2>
          <p>登入帳號</p>
        </div>
      </template>

      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="rules"
        label-width="0"
        class="login-form"
      >
        <el-form-item prop="email">
          <el-input
            v-model="loginForm.email"
            placeholder="電子郵件"
            size="large"
          >
            <template #prefix>
              <el-icon class="custom-icon"><UserIcon /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="密碼"
            size="large"
            show-password
            @keyup.enter="handleLogin"
          >
            <template #prefix>
              <el-icon class="custom-icon"><Key1 /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="login-button"
            :loading="loading"
            @click="handleLogin"
          >
            登入
          </el-button>
        </el-form-item>

        <div v-if="showResendVerification" class="resend-verification">
          尚未驗證 Email？
          <el-link type="primary" @click="handleResendVerification">
            重新寄送驗證信
          </el-link>
        </div>

        <div class="register-link">
          還沒有帳號？
          <router-link to="/register">立即註冊</router-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { authAPI } from '../api'
import { ElMessage } from 'element-plus'
import UserIcon from '../components/icons/UserIcon.vue'
import Key1 from '../components/icons/Key1.vue'

const router = useRouter()
const authStore = useAuthStore()

const loginFormRef = ref(null)
const loading = ref(false)
const loginError = ref('')

const loginForm = reactive({
  email: '',
  password: ''
})

const rules = {
  email: [
    { required: true, message: '請輸入電子郵件', trigger: 'blur' },
    { type: 'email', message: '請輸入有效的電子郵件', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '請輸入密碼', trigger: 'blur' },
    { min: 6, message: '密碼至少需要 6 個字元', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return

  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      loginError.value = ''
      try {
        const result = await authStore.login(loginForm.email, loginForm.password)

        if (result.success) {
          ElMessage.success('登入成功！')
          router.push('/dashboard')
        } else {
          loginError.value = result.message || '登入失敗'
          ElMessage.error(loginError.value)
        }
      } catch (error) {
        loginError.value = '登入時發生錯誤'
        ElMessage.error(loginError.value)
      } finally {
        loading.value = false
      }
    }
  })
}

const showResendVerification = computed(() => loginError.value === '請先完成 Email 驗證')

const handleResendVerification = async () => {
  if (!loginForm.email) {
    ElMessage.error('請先輸入 Email')
    return
  }

  try {
    await authAPI.resendVerification(loginForm.email)
    ElMessage.success('驗證信已重新發送，請檢查信箱')
  } catch (error) {
    const message = error.response?.data?.message || '重新發送驗證信失敗'
    ElMessage.error(message)
  }
}
</script>

<style scoped>
.login-container {
  --mint: #8deac3;
  --charcoal: #1f2a33;
  --cornflower: #5b8def;
  --soft-white: #f7fbff;
  width: 100%;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  background: radial-gradient(1200px circle at 15% 20%, rgba(141, 234, 195, 0.28), transparent 45%),
    radial-gradient(900px circle at 85% 0%, rgba(91, 141, 239, 0.22), transparent 40%),
    linear-gradient(135deg, #f6fff9 0%, #eef2ff 100%);
}

.login-card {
  width: 420px;
  box-shadow: 0 14px 40px rgba(31, 42, 51, 0.14);
  background: #fff;
  border-radius: 18px;
  border: 1px solid rgba(91, 141, 239, 0.12);
  backdrop-filter: blur(8px);
  overflow: hidden;
}

.login-card :deep(.el-card__header) {
  border: none;
  background: linear-gradient(120deg, rgba(141, 234, 195, 0.24), rgba(91, 141, 239, 0.2));
  padding: 22px 24px;
}

.card-header {
  text-align: center;
  color: var(--charcoal);
}

.card-header h2 {
  margin: 0 0 10px 0;
  color: var(--charcoal);
  font-size: 26px;
  letter-spacing: 0.5px;
  font-weight: 700;
}

.card-header p {
  margin: 0;
  color: rgba(31, 42, 51, 0.65);
  font-size: 13px;
  letter-spacing: 0.2px;
}

.login-form {
  padding: 16px 0 6px;
}

.login-button {
  width: 100%;
  background: linear-gradient(120deg, var(--cornflower), #70d5b4);
  border: none;
  box-shadow: 0 12px 30px rgba(91, 141, 239, 0.25);
}

.login-button:hover {
  background: linear-gradient(120deg, #4a7ae0, #6ecfae);
}

.register-link {
  text-align: center;
  color: var(--charcoal);
  font-size: 13px;
  margin-top: 10px;
}

.resend-verification {
  text-align: center;
  color: rgba(31, 42, 51, 0.7);
  font-size: 12px;
  margin-top: -6px;
  margin-bottom: 6px;
}

.register-link a {
  color: var(--cornflower);
  text-decoration: none;
  font-weight: 600;
}

.register-link a:hover {
  text-decoration: underline;
}

:deep(.el-form-item) {
  margin-bottom: 16px;
}

:deep(.el-input__wrapper) {
  border-radius: 12px;
  border: 1px solid rgba(31, 42, 51, 0.12);
  box-shadow: none;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
  background-color: #fff;
}

:deep(.el-input__wrapper.is-focus) {
  border-color: var(--cornflower);
  box-shadow: 0 0 0 4px rgba(91, 141, 239, 0.12);
}

:deep(.el-input__inner) {
  color: var(--charcoal);
}

.custom-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}

.custom-icon svg {
  width: 20px;
  height: 20px;
}

.glow {
  position: absolute;
  width: 520px;
  height: 520px;
  border-radius: 50%;
  filter: blur(90px);
  opacity: 0.6;
  z-index: 0;
}

.glow.mint {
  background: rgba(141, 234, 195, 0.38);
  top: -120px;
  left: -60px;
}

.glow.blue {
  background: rgba(91, 141, 239, 0.3);
  bottom: -140px;
  right: -120px;
}

.login-card,
.login-form {
  position: relative;
  z-index: 1;
}
</style>
