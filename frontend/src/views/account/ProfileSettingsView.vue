<template>
  <div class="settings-page">
    <el-card class="settings-card">
      <template #header>
        <h3>個人資訊</h3>
      </template>
      <el-form
        ref="profileFormRef"
        :model="profileForm"
        :rules="profileRules"
        label-width="120px"
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../../stores/auth'
import { userAPI } from '../../api/index'

const authStore = useAuthStore()

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

const handleUpdateProfile = async () => {
  if (!profileFormRef.value) return

  await profileFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const payload = {
          name: profileForm.name
        }
        const response = await userAPI.updateProfile(payload)
        if (response?.data?.name) {
          const updatedUser = {
            ...authStore.user,
            name: response.data.name
          }
          authStore.user = updatedUser
          localStorage.setItem('user', JSON.stringify(updatedUser))
        }
        ElMessage.success('個人資訊已更新')
      } catch (error) {
        const message = error.response?.data?.message || '個人資訊更新失敗'
        ElMessage.error(message)
      }
    }
  })
}

const resetProfileForm = () => {
  if (profileFormRef.value) {
    profileFormRef.value.resetFields()
  }
}

const syncProfile = async () => {
  try {
    const { data } = await userAPI.getProfile()
    if (!data) return
    profileForm.name = data.name || profileForm.name
    profileForm.email = data.email || profileForm.email
  } catch (error) {
    const message = error.response?.data?.message || '無法取得使用者資料'
    ElMessage.error(message)
  }
}

onMounted(() => {
  syncProfile()
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
</style>
