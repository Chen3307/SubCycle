<template>
  <div class="verify-email-view">
    <div class="verify-container">
      <el-card>
        <div class="verify-content">
          <!-- Loading 狀態 -->
          <div v-if="verifying" class="status-section">
            <el-icon class="loading-icon" :size="60">
              <Loading />
            </el-icon>
            <h2>正在驗證您的電子郵件...</h2>
            <p>請稍候</p>
          </div>

          <!-- 成功狀態 -->
          <div v-else-if="success" class="status-section success">
            <el-icon class="success-icon" :size="60">
              <CircleCheck />
            </el-icon>
            <h2>Email 驗證成功！</h2>
            <p>您的電子郵件已成功驗證，現在可以使用所有功能了。</p>
            <el-button type="primary" @click="goToLogin" class="action-btn">
              前往登入
            </el-button>
          </div>

          <!-- 失敗狀態 -->
          <div v-else-if="error" class="status-section error">
            <el-icon class="error-icon" :size="60">
              <CircleClose />
            </el-icon>
            <h2>驗證失敗</h2>
            <p class="error-message">{{ errorMessage }}</p>

            <div class="action-buttons">
              <el-button type="primary" @click="resendEmail" :loading="resending">
                重新發送驗證郵件
              </el-button>
              <el-button @click="goToLogin">
                返回登入
              </el-button>
            </div>

            <!-- 重新發送郵件表單 -->
            <el-form v-if="showResendForm" :model="resendForm" class="resend-form">
              <el-form-item>
                <el-input
                  v-model="resendForm.email"
                  placeholder="請輸入您的 Email"
                  type="email"
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleResend" :loading="resending" style="width: 100%">
                  發送驗證郵件
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { CircleCheck, CircleClose, Loading } from '@element-plus/icons-vue'
import axios from 'axios'

const route = useRoute()
const router = useRouter()

const verifying = ref(true)
const success = ref(false)
const error = ref(false)
const errorMessage = ref('')
const showResendForm = ref(false)
const resending = ref(false)

const resendForm = ref({
  email: ''
})

const verifyEmail = async (token) => {
  try {
    const response = await axios.get(`http://localhost:8080/api/auth/verify-email/${token}`)

    if (response.data.success) {
      success.value = true
      ElMessage.success('Email 驗證成功！')
    }
  } catch (err) {
    error.value = true
    errorMessage.value = err.response?.data?.message || '驗證連結無效或已過期'
    ElMessage.error(errorMessage.value)
  } finally {
    verifying.value = false
  }
}

const resendEmail = () => {
  showResendForm.value = true
}

const handleResend = async () => {
  if (!resendForm.value.email) {
    ElMessage.warning('請輸入 Email 地址')
    return
  }

  resending.value = true
  try {
    const response = await axios.post('http://localhost:8080/api/auth/resend-verification', {
      email: resendForm.value.email
    })

    if (response.data.success) {
      ElMessage.success('驗證郵件已重新發送，請檢查您的信箱')
      showResendForm.value = false
    }
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '發送失敗，請稍後再試')
  } finally {
    resending.value = false
  }
}

const goToLogin = () => {
  router.push('/login')
}

onMounted(() => {
  const token = route.query.token

  if (!token) {
    error.value = true
    errorMessage.value = '缺少驗證'
    verifying.value = false
    return
  }

  verifyEmail(token)
})
</script>

<style scoped>
.verify-email-view {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.verify-container {
  width: 100%;
  max-width: 500px;
}

.verify-content {
  padding: 40px 20px;
}

.status-section {
  text-align: center;
}

.loading-icon {
  color: #667eea;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.success-icon {
  color: #67c23a;
}

.error-icon {
  color: #f56c6c;
}

.status-section h2 {
  margin: 20px 0 10px;
  font-size: 24px;
  color: var(--text-primary);
}

.status-section p {
  color: var(--text-secondary);
  margin-bottom: 30px;
}

.error-message {
  color: #f56c6c;
  font-weight: 500;
}

.action-btn {
  min-width: 150px;
}

.action-buttons {
  display: flex;
  gap: 10px;
  justify-content: center;
  flex-wrap: wrap;
}

.resend-form {
  margin-top: 30px;
  max-width: 350px;
  margin-left: auto;
  margin-right: auto;
}
</style>
