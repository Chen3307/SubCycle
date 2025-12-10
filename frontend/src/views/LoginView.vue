<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <div class="card-header">
          <img src="/logo.svg" alt="SubMinder Logo" class="logo-img" />
          <h2>SubCycle</h2>
          <p>登入您的帳號</p>
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

        <el-form-item>
          <el-button
            size="large"
            class="login-button"
            :loading="loading"
            @click="handleTestLogin"
          >
            測試登入（跳過表單驗證）
          </el-button>
        </el-form-item>

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
import { ElMessage } from 'element-plus'
import UserIcon from '../components/icons/UserIcon.vue'
import Key1 from '../components/icons/Key1.vue'
import loginBg from '../assets/login-bg.jpg'

const router = useRouter()
const authStore = useAuthStore()

// 背景圖片樣式
const bgStyle = computed(() => `url(${loginBg})`)

const loginFormRef = ref(null)
const loading = ref(false)

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
      try {
        const result = await authStore.login(loginForm.email, loginForm.password)

        if (result.success) {
          ElMessage.success('登入成功！')
          router.push('/dashboard')
        } else {
          ElMessage.error(result.message || '登入失敗')
        }
      } catch (error) {
        ElMessage.error('登入時發生錯誤')
      } finally {
        loading.value = false
      }
    }
  })
}

const handleTestLogin = async () => {
  loading.value = true
  try {
    // 直接使用測試帳號登入，跳過表單驗證
    const result = await authStore.login('test@example.com', 'test123')

    if (result.success) {
      ElMessage.success('測試登入成功！')
      router.push('/dashboard')
    } else {
      ElMessage.error(result.message || '登入失敗')
    }
  } catch (error) {
    ElMessage.error('登入時發生錯誤')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  width: 100%;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-image: v-bind(bgStyle);
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
}

.login-card {
  width: 420px;
  min-width: 420px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  backdrop-filter: blur(10px);
  background-color: rgba(255, 255, 255, 0.9);
  border-radius: 16px;
  border: none;
  overflow: hidden;
}

.card-header {
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  background: transparent;
  overflow: hidden;
  padding: 0 20px;
}

.login-card :deep(.el-card__header) {
  background: transparent;
  border: none;
}

.logo-img {
  width: 80px;
  height: 80px;
  max-width: 100%;
  margin-bottom: 16px;
  object-fit: contain;
}

.card-header h2 {
  margin: 0 0 10px 0;
  color: #303133;
  font-size: 28px;
  font-weight: 600;
}

.card-header p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.login-form {
  padding: 20px 0;
}

.login-button {
  width: 100%;
  font-weight: 500;
}

.register-link {
  text-align: center;
  color: #606266;
  font-size: 14px;
  margin-top: 10px;
}

.register-link a {
  color: #409eff;
  text-decoration: none;
  font-weight: 500;
}

.register-link a:hover {
  text-decoration: underline;
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
</style>
