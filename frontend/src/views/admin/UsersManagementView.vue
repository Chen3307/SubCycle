<template>
  <div class="users-management">
    <el-config-provider :locale="zhTw">
      <el-card>
      <template #header>
        <div class="card-header">
          <h2>用戶管理</h2>
          <div class="card-actions">
            <el-button type="primary" @click="handleCreate">
              新增用戶
            </el-button>
            <el-input
              v-model="searchQuery"
              placeholder="搜尋用戶名稱或 Email"
              clearable
              style="width: 300px"
              @input="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </div>
        </div>
      </template>

      <!-- 用戶列表表格 -->
      <el-table
        :data="users"
        v-loading="loading"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="email" label="Email" min-width="200" />
        <el-table-column prop="name" label="名稱" min-width="120" />
        <el-table-column prop="role" label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'info'">
              {{ row.role === 'ADMIN' ? '管理者' : '一般用戶' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isActive" label="狀態" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isActive ? 'success' : 'warning'">
              {{ row.isActive ? '啟用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastLoginAt" label="最後登入" min-width="160">
          <template #default="{ row }">
            {{ row.lastLoginAt ? formatDate(row.lastLoginAt) : '從未登入' }}
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="註冊時間" min-width="160">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="340" fixed="right">
          <template #default="{ row }">
            <el-button
              size="small"
              @click="handleEdit(row)"
            >
              編輯
            </el-button>
            <el-button
              size="small"
              class="btn-reset-password"
              @click="handleResetPassword(row)"
            >
              重置密碼
            </el-button>
            <el-button
              size="small"
              :type="row.isActive ? 'danger' : 'success'"
              @click="handleToggleStatus(row)"
            >
              {{ row.isActive ? '停用' : '啟用' }}
            </el-button>
            <el-button
              size="small"
              type="danger"
              class="btn-delete-user"
              @click="handleDelete(row)"
            >
              刪除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分頁 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="totalItems"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="fetchUsers"
          @size-change="fetchUsers"
        />
      </div>
      </el-card>
    </el-config-provider>

    <!-- 編輯用戶對話框 -->
    <el-dialog
      v-model="editDialogVisible"
      title="編輯用戶"
      width="500px"
    >
      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="editRules"
        label-width="80px"
      >
        <el-form-item label="名稱" prop="name">
          <el-input v-model="editForm.name" />
        </el-form-item>
        <el-form-item label="Email" prop="email">
          <el-input v-model="editForm.email" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="editForm.role" style="width: 100%">
            <el-option label="一般用戶" value="USER" />
            <el-option label="管理者" value="ADMIN" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveEdit" :loading="saving">
          保存
        </el-button>
      </template>
    </el-dialog>

    <!-- 新增用戶對話框 -->
    <el-dialog
      v-model="createDialogVisible"
      title="新增用戶"
      width="500px"
    >
      <el-form
        ref="createFormRef"
        :model="createForm"
        :rules="createRules"
        label-width="80px"
      >
        <el-form-item label="名稱" prop="name">
          <el-input v-model="createForm.name" />
        </el-form-item>
        <el-form-item label="Email" prop="email">
          <el-input v-model="createForm.email" />
        </el-form-item>
        <el-form-item label="密碼" prop="password">
          <el-input v-model="createForm.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="createForm.role" style="width: 100%">
            <el-option label="一般用戶" value="USER" />
            <el-option label="管理者" value="ADMIN" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveCreate" :loading="saving">
          建立
        </el-button>
      </template>
    </el-dialog>

    <!-- 重置密碼對話框 -->
    <el-dialog
      v-model="resetPasswordDialogVisible"
      title="重置用戶密碼"
      width="500px"
    >
      <el-alert
        title="注意"
        type="warning"
        :closable="false"
        style="margin-bottom: 20px"
      >
        請為用戶 <strong>{{ currentUser?.name }}</strong> 設置新密碼
      </el-alert>
      <el-form
        ref="resetPasswordFormRef"
        :model="resetPasswordForm"
        :rules="resetPasswordRules"
        label-width="80px"
      >
        <el-form-item label="新密碼" prop="newPassword">
          <el-input
            v-model="resetPasswordForm.newPassword"
            type="password"
            show-password
            placeholder="至少 6 個字元"
          />
        </el-form-item>
        <el-form-item label="確認密碼" prop="confirmPassword">
          <el-input
            v-model="resetPasswordForm.confirmPassword"
            type="password"
            show-password
            placeholder="再次輸入新密碼"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resetPasswordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveResetPassword" :loading="saving">
          確認重置
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { adminAPI } from '@/api'
import dayjs from 'dayjs'
import zhTw from 'element-plus/es/locale/lang/zh-tw'

// 數據
const users = ref([])
const loading = ref(false)
const saving = ref(false)
const searchQuery = ref('')

// 分頁
const currentPage = ref(1)
const pageSize = ref(10)
const totalItems = ref(0)

// 編輯對話框
const editDialogVisible = ref(false)
const editFormRef = ref(null)
const editForm = reactive({
  id: null,
  name: '',
  email: '',
  role: 'USER'
})

const editRules = {
  name: [{ required: true, message: '請輸入名稱', trigger: 'blur' }],
  email: [
    { required: true, message: '請輸入 Email', trigger: 'blur' },
    { type: 'email', message: '請輸入正確的 Email 格式', trigger: 'blur' }
  ],
  role: [{ required: true, message: '請選擇角色', trigger: 'change' }]
}

// 新增對話框
const createDialogVisible = ref(false)
const createFormRef = ref(null)
const createForm = reactive({
  name: '',
  email: '',
  password: '',
  role: 'USER'
})

const createRules = {
  name: [{ required: true, message: '請輸入名稱', trigger: 'blur' }],
  email: [
    { required: true, message: '請輸入 Email', trigger: 'blur' },
    { type: 'email', message: '請輸入正確的 Email 格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '請輸入密碼', trigger: 'blur' },
    { min: 6, message: '密碼長度至少為 6 個字元', trigger: 'blur' }
  ],
  role: [{ required: true, message: '請選擇角色', trigger: 'change' }]
}

// 重置密碼對話框
const resetPasswordDialogVisible = ref(false)
const resetPasswordFormRef = ref(null)
const currentUser = ref(null)
const resetPasswordForm = reactive({
  newPassword: '',
  confirmPassword: ''
})

const validatePasswordMatch = (rule, value, callback) => {
  if (value !== resetPasswordForm.newPassword) {
    callback(new Error('兩次輸入的密碼不一致'))
  } else {
    callback()
  }
}

const resetPasswordRules = {
  newPassword: [
    { required: true, message: '請輸入新密碼', trigger: 'blur' },
    { min: 6, message: '密碼長度至少為 6 個字元', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '請再次輸入新密碼', trigger: 'blur' },
    { validator: validatePasswordMatch, trigger: 'blur' }
  ]
}

// 方法
const fetchUsers = async () => {
  loading.value = true
  try {
    const response = await adminAPI.getUsers(
      currentPage.value - 1, // 後端從 0 開始
      pageSize.value,
      searchQuery.value
    )
    users.value = response.data.users
    totalItems.value = response.data.totalItems
  } catch (error) {
    ElMessage.error('載入用戶列表失敗：' + (error.response?.data?.message || error.message))
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  fetchUsers()
}

const formatDate = (dateStr) => {
  return dayjs(dateStr).format('YYYY-MM-DD HH:mm')
}

// 編輯用戶
const handleEdit = (user) => {
  editForm.id = user.id
  editForm.name = user.name
  editForm.email = user.email
  editForm.role = user.role
  editDialogVisible.value = true
}

const handleSaveEdit = async () => {
  const valid = await editFormRef.value?.validate()
  if (!valid) return

  saving.value = true
  try {
    await adminAPI.updateUser(editForm.id, {
      name: editForm.name,
      email: editForm.email,
      role: editForm.role
    })
    ElMessage.success('用戶資訊已更新')
    editDialogVisible.value = false
    fetchUsers()
  } catch (error) {
    ElMessage.error('更新失敗：' + (error.response?.data?.message || error.message))
  } finally {
    saving.value = false
  }
}

// 新增用戶
const handleCreate = () => {
  createForm.name = ''
  createForm.email = ''
  createForm.password = ''
  createForm.role = 'USER'
  createDialogVisible.value = true
}

const handleSaveCreate = async () => {
  const valid = await createFormRef.value?.validate()
  if (!valid) return

  saving.value = true
  try {
    await adminAPI.createUser({
      name: createForm.name,
      email: createForm.email,
      password: createForm.password,
      role: createForm.role
    })
    ElMessage.success('用戶已建立')
    createDialogVisible.value = false
    fetchUsers()
  } catch (error) {
    ElMessage.error('建立失敗：' + (error.response?.data?.message || error.message))
  } finally {
    saving.value = false
  }
}

// 切換用戶狀態
const handleToggleStatus = async (user) => {
  const action = user.isActive ? '停用' : '啟用'
  try {
    await ElMessageBox.confirm(
      `確定要${action}用戶 "${user.name}" 嗎？`,
      '確認',
      {
        confirmButtonText: '確定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await adminAPI.updateUserStatus(user.id, !user.isActive)
    ElMessage.success(`用戶已${action}`)
    fetchUsers()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(`${action}失敗：` + (error.response?.data?.message || error.message))
    }
  }
}

// 刪除用戶
const handleDelete = async (user) => {
  try {
    await ElMessageBox.confirm(
      `確定要永久刪除用戶 "${user.name}" 嗎？此動作無法復原。`,
      '確認刪除',
      {
        confirmButtonText: '刪除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await adminAPI.deleteUser(user.id)
    ElMessage.success('用戶已刪除')
    fetchUsers()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('刪除失敗：' + (error.response?.data?.message || error.message))
    }
  }
}

// 重置密碼
const handleResetPassword = (user) => {
  currentUser.value = user
  resetPasswordForm.newPassword = ''
  resetPasswordForm.confirmPassword = ''
  resetPasswordDialogVisible.value = true
}

const handleSaveResetPassword = async () => {
  const valid = await resetPasswordFormRef.value?.validate()
  if (!valid) return

  saving.value = true
  try {
    await adminAPI.resetUserPassword(currentUser.value.id, resetPasswordForm.newPassword)
    ElMessage.success('密碼已重置成功')
    resetPasswordDialogVisible.value = false
  } catch (error) {
    ElMessage.error('重置密碼失敗：' + (error.response?.data?.message || error.message))
  } finally {
    saving.value = false
  }
}

// 初始化
onMounted(() => {
  fetchUsers()
})
</script>

<style scoped>
.users-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.card-header h2 {
  margin: 0;
  font-size: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

:deep(.btn-reset-password.el-button) {
  background-color: #5b8def;
  border-color: #5b8def;
  color: #ffffff;
}

:deep(.btn-reset-password.el-button:hover),
:deep(.btn-reset-password.el-button:focus) {
  background-color: #4b7fdf;
  border-color: #4b7fdf;
  color: #ffffff;
}

:deep(.btn-delete-user.el-button) {
  background-color: #a32626;
  border-color: #a32626;
  color: #ffffff;
}

:deep(.btn-delete-user.el-button:hover),
:deep(.btn-delete-user.el-button:focus) {
  background-color: #8a1f1f;
  border-color: #8a1f1f;
  color: #ffffff;
}

</style>
