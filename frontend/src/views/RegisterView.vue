<template>
  <div class="register-container">
    <div class="glow mint"></div>
    <div class="glow blue"></div>
    <el-card class="register-card">
      <template #header>
        <div class="card-header">
          <h2>Subcycle</h2>
          <p>註冊帳號</p>
        </div>
      </template>

      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="rules"
        label-width="0"
        class="register-form"
      >
        <el-form-item prop="name">
          <el-input
            v-model="registerForm.name"
            placeholder="姓名"
            size="large"
          >
            <template #prefix>
              <el-icon class="custom-icon"><UserIcon /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item prop="email">
          <el-input
            v-model="registerForm.email"
            placeholder="電子郵件"
            size="large"
          >
            <template #prefix>
              <el-icon class="custom-icon"><MailIcon /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="密碼"
            size="large"
            show-password
          >
            <template #prefix>
              <el-icon class="custom-icon"><Key1 /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="確認密碼"
            size="large"
            show-password
            @keyup.enter="handleRegister"
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
            class="register-button"
            :loading="loading"
            @click="handleRegister"
          >
            註冊
          </el-button>
        </el-form-item>

        <el-form-item>
          <el-button
            size="large"
            class="preview-button"
            :loading="loading"
            @click="handlePreviewLogin"
          >
            前端預覽
          </el-button>
        </el-form-item>

        <div class="login-link">
          已經有帳號了？
          <router-link to="/login">立即登入</router-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { ElMessage } from 'element-plus'
import UserIcon from '../components/icons/UserIcon.vue'
import MailIcon from '../components/icons/MailIcon.vue'
import Key1 from '../components/icons/Key1.vue'

const router = useRouter()
const authStore = useAuthStore()

const registerFormRef = ref(null)
const loading = ref(false)

const registerForm = reactive({
  name: '',
  email: '',
  password: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('請再次輸入密碼'))
  } else if (value !== registerForm.password) {
    callback(new Error('兩次輸入的密碼不一致'))
  } else {
    callback()
  }
}

const rules = {
  name: [
    { required: true, message: '請輸入姓名', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '請輸入電子郵件', trigger: 'blur' },
    { type: 'email', message: '請輸入有效的電子郵件', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '請輸入密碼', trigger: 'blur' },
    { min: 6, message: '密碼至少需要 6 個字元', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '請確認密碼', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  if (!registerFormRef.value) return

  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const result = await authStore.register(
          registerForm.email,
          registerForm.password,
          registerForm.name
        )

        if (result.success) {
          ElMessage.success('註冊成功！')
          router.push('/dashboard')
        } else {
          ElMessage.error(result.message || '註冊失敗')
        }
      } catch (error) {
        ElMessage.error('註冊時發生錯誤')
      } finally {
        loading.value = false
      }
    }
  })
}

const handlePreviewLogin = async () => {
  loading.value = true
  try {
    await authStore.mockLogin({
      email: registerForm.email || 'preview@subcycle.app',
      name: registerForm.name || '前端預覽用戶'
    })
    ElMessage.success('已開啟前端預覽模式')
    router.push('/dashboard')
  } catch (error) {
    ElMessage.error('啟用預覽模式時發生錯誤')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-container {
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

.register-card {
  width: 420px;
  box-shadow: 0 14px 40px rgba(31, 42, 51, 0.14);
  background: #fff;
  border-radius: 18px;
  border: 1px solid rgba(91, 141, 239, 0.12);
  backdrop-filter: blur(8px);
  overflow: hidden;
}

.register-card :deep(.el-card__header) {
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

.register-form {
  padding: 16px 0 6px;
}

.register-button {
  width: 100%;
  background: linear-gradient(120deg, var(--cornflower), #70d5b4);
  border: none;
  box-shadow: 0 12px 30px rgba(91, 141, 239, 0.25);
}

.register-button:hover {
  background: linear-gradient(120deg, #4a7ae0, #6ecfae);
}

.preview-button {
  width: 100%;
  border: 1px solid rgba(31, 42, 51, 0.2);
  color: var(--charcoal);
  background: #fff;
}

.preview-button:hover {
  border-color: var(--cornflower);
  color: var(--cornflower);
}

.login-link {
  text-align: center;
  color: var(--charcoal);
  font-size: 13px;
  margin-top: 10px;
}

.login-link a {
  color: var(--cornflower);
  text-decoration: none;
  font-weight: 600;
}

.login-link a:hover {
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

.register-card,
.register-form {
  position: relative;
  z-index: 1;
}
</style>
