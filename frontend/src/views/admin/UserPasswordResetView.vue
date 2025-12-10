<template>
  <div class="user-password-reset">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <h2>使用者密碼管理</h2>
          <el-button type="primary" @click="showBatchResetDialog">
            批次重置密碼
          </el-button>
        </div>
      </template>

      <!-- 搜尋區域 -->
      <el-form :inline="true" class="search-form">
        <el-form-item label="搜尋使用者">
          <el-input
            v-model="searchQuery"
            placeholder="電子郵件或姓名"
            clearable
            @clear="handleSearch"
          >
            <template #append>
              <el-button :icon="Search" @click="handleSearch" />
            </template>
          </el-input>
        </el-form-item>
      </el-form>

      <!-- 使用者列表 -->
      <el-table
        :data="filteredUsers"
        style="width: 100%"
        v-loading="loading"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="email" label="電子郵件" min-width="200" />
        <el-table-column prop="password_changed_at" label="最後密碼變更" width="180">
          <template #default="{ row }">
            {{ row.password_changed_at ? formatDate(row.password_changed_at) : '從未變更' }}
          </template>
        </el-table-column>
        <el-table-column prop="force_password_change" label="強制變更" width="100">
          <template #default="{ row }">
            <el-tag :type="row.force_password_change ? 'warning' : 'success'" size="small">
              {{ row.force_password_change ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button
              size="small"
              type="primary"
              @click="showResetDialog(row)"
            >
              重置密碼
            </el-button>
            <el-button
              size="small"
              type="info"
              @click="viewResetHistory(row)"
            >
              重置記錄
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分頁 -->
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="totalUsers"
        layout="total, sizes, prev, pager, next, jumper"
        class="pagination"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </el-card>

    <!-- 單一使用者重置對話框 -->
    <el-dialog
      v-model="resetDialogVisible"
      title="重置使用者密碼"
      width="500px"
    >
      <el-form :model="resetForm" label-width="120px">
        <el-form-item label="使用者">
          <el-input
            :value="`${currentUser?.name} (${currentUser?.email})`"
            disabled
          />
        </el-form-item>

        <el-form-item label="重置方式">
          <el-radio-group v-model="resetForm.resetType">
            <el-radio value="temp_password">產生臨時密碼</el-radio>
            <el-radio value="reset_link">發送重置連結</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="通知使用者">
          <el-switch v-model="resetForm.notifyUser" />
          <p class="helper-text">是否透過電子郵件通知使用者</p>
        </el-form-item>

        <el-alert
          v-if="resetForm.resetType === 'temp_password'"
          type="warning"
          :closable="false"
          show-icon
        >
          使用者登入後將被強制要求修改密碼
        </el-alert>
      </el-form>

      <template #footer>
        <el-button @click="resetDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="resetting"
          @click="handleResetPassword"
        >
          確認重置
        </el-button>
      </template>
    </el-dialog>

    <!-- 批次重置對話框 -->
    <el-dialog
      v-model="batchResetDialogVisible"
      title="批次重置密碼"
      width="600px"
    >
      <el-alert
        type="warning"
        title="警告"
        :closable="false"
        show-icon
        class="mb-4"
      >
        批次重置將影響多位使用者，請謹慎操作
      </el-alert>

      <el-form :model="batchResetForm" label-width="120px">
        <el-form-item label="已選擇使用者">
          <el-tag
            v-for="user in selectedUsers"
            :key="user.id"
            class="user-tag"
            closable
            @close="removeUserFromBatch(user.id)"
          >
            {{ user.email }}
          </el-tag>
          <p v-if="selectedUsers.length === 0" class="no-selection">
            請先在列表中勾選要重置的使用者
          </p>
        </el-form-item>

        <el-form-item label="重置方式">
          <el-radio-group v-model="batchResetForm.resetType">
            <el-radio value="temp_password">產生臨時密碼</el-radio>
            <el-radio value="reset_link">發送重置連結</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="batchResetDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="resetting"
          :disabled="selectedUsers.length === 0"
          @click="handleBatchReset"
        >
          批次重置 ({{ selectedUsers.length }} 位使用者)
        </el-button>
      </template>
    </el-dialog>

    <!-- 重置記錄對話框 -->
    <el-dialog
      v-model="historyDialogVisible"
      title="密碼重置記錄"
      width="800px"
    >
      <el-table
        :data="resetHistory"
        v-loading="loadingHistory"
        max-height="400"
      >
        <el-table-column prop="created_at" label="重置時間" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.created_at) }}
          </template>
        </el-table-column>
        <el-table-column prop="type" label="重置方式" width="120">
          <template #default="{ row }">
            <el-tag :type="row.type === 'admin_reset' ? 'danger' : 'info'" size="small">
              {{ row.type === 'admin_reset' ? '管理員重置' : '使用者申請' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="admin_username" label="操作管理員" width="120">
          <template #default="{ row }">
            {{ row.admin_username || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="used_at" label="使用狀態" width="120">
          <template #default="{ row }">
            <el-tag :type="row.used_at ? 'success' : 'warning'" size="small">
              {{ row.used_at ? '已使用' : '未使用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ip_address" label="IP 位址" min-width="150" />
      </el-table>

      <template #footer>
        <el-button @click="historyDialogVisible = false">關閉</el-button>
      </template>
    </el-dialog>

    <!-- 重置結果對話框 -->
    <el-dialog
      v-model="resultDialogVisible"
      title="重置結果"
      width="500px"
    >
      <div v-if="resetResult">
        <el-result
          :icon="resetResult.success ? 'success' : 'error'"
          :title="resetResult.message"
        >
          <template #extra>
            <div v-if="resetResult.tempPassword && !resetForm.notifyUser" class="temp-password-section">
              <p class="temp-password-label">臨時密碼：</p>
              <div class="temp-password-box">
                <code class="temp-password">{{ resetResult.tempPassword }}</code>
                <el-button
                  :icon="CopyDocument"
                  size="small"
                  @click="copyToClipboard(resetResult.tempPassword)"
                >
                  複製
                </el-button>
              </div>
              <el-alert type="warning" :closable="false" class="mt-3">
                請將此臨時密碼安全地提供給使用者
              </el-alert>
            </div>
          </template>
        </el-result>
      </div>

      <template #footer>
        <el-button type="primary" @click="resultDialogVisible = false">
          確定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, CopyDocument } from '@element-plus/icons-vue'
import axios from 'axios'
import dayjs from 'dayjs'

const loading = ref(false)
const resetting = ref(false)
const loadingHistory = ref(false)
const searchQuery = ref('')
const currentPage = ref(1)
const pageSize = ref(20)
const totalUsers = ref(0)
const users = ref([])
const selectedUsers = ref([])

const resetDialogVisible = ref(false)
const batchResetDialogVisible = ref(false)
const historyDialogVisible = ref(false)
const resultDialogVisible = ref(false)

const currentUser = ref(null)
const resetHistory = ref([])
const resetResult = ref(null)

const resetForm = reactive({
  resetType: 'temp_password',
  notifyUser: true
})

const batchResetForm = reactive({
  resetType: 'temp_password'
})

const filteredUsers = computed(() => {
  if (!searchQuery.value) return users.value

  const query = searchQuery.value.toLowerCase()
  return users.value.filter(user =>
    user.email.toLowerCase().includes(query) ||
    user.name.toLowerCase().includes(query)
  )
})

// 載入使用者列表
const loadUsers = async () => {
  loading.value = true
  try {
    // TODO: 連接後端 API
    const response = await axios.get('/admin/api/users', {
      params: {
        page: currentPage.value,
        limit: pageSize.value
      }
    })

    users.value = response.data.users
    totalUsers.value = response.data.total
  } catch (error) {
    ElMessage.error('載入失敗')
  } finally {
    loading.value = false
  }
}

// 搜尋
const handleSearch = () => {
  currentPage.value = 1
  loadUsers()
}

// 分頁
const handleSizeChange = (size) => {
  pageSize.value = size
  loadUsers()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  loadUsers()
}

// 顯示重置對話框
const showResetDialog = (user) => {
  currentUser.value = user
  resetForm.resetType = 'temp_password'
  resetForm.notifyUser = true
  resetDialogVisible.value = true
}

// 執行重置
const handleResetPassword = async () => {
  resetting.value = true

  try {
    // TODO: 連接後端 API
    const response = await axios.post(
      `/admin/api/users/${currentUser.value.id}/reset-password`,
      {
        resetType: resetForm.resetType,
        notifyUser: resetForm.notifyUser
      }
    )

    resetResult.value = response.data
    resetDialogVisible.value = false
    resultDialogVisible.value = true

    ElMessage.success('密碼重置成功')
    loadUsers()
  } catch (error) {
    ElMessage.error(error.response?.data?.error || '重置失敗')
  } finally {
    resetting.value = false
  }
}

// 顯示批次重置對話框
const showBatchResetDialog = () => {
  if (selectedUsers.value.length === 0) {
    ElMessage.warning('請先選擇要重置密碼的使用者')
    return
  }
  batchResetDialogVisible.value = true
}

// 批次重置
const handleBatchReset = async () => {
  await ElMessageBox.confirm(
    `確定要批次重置 ${selectedUsers.value.length} 位使用者的密碼嗎？`,
    '確認操作',
    {
      type: 'warning',
      confirmButtonText: '確定',
      cancelButtonText: '取消'
    }
  )

  resetting.value = true

  try {
    const userIds = selectedUsers.value.map(u => u.id)

    // TODO: 連接後端 API
    const response = await axios.post('/admin/api/users/batch-reset-passwords', {
      userIds,
      resetType: batchResetForm.resetType
    })

    batchResetDialogVisible.value = false
    ElMessage.success(response.data.message)
    selectedUsers.value = []
    loadUsers()
  } catch (error) {
    ElMessage.error(error.response?.data?.error || '批次重置失敗')
  } finally {
    resetting.value = false
  }
}

// 查看重置記錄
const viewResetHistory = async (user) => {
  currentUser.value = user
  historyDialogVisible.value = true
  loadingHistory.value = true

  try {
    // TODO: 連接後端 API
    const response = await axios.get('/admin/api/password-reset-logs', {
      params: {
        userId: user.id
      }
    })

    resetHistory.value = response.data.logs
  } catch (error) {
    ElMessage.error('載入記錄失敗')
  } finally {
    loadingHistory.value = false
  }
}

// 移除批次選擇
const removeUserFromBatch = (userId) => {
  selectedUsers.value = selectedUsers.value.filter(u => u.id !== userId)
}

// 複製到剪貼簿
const copyToClipboard = async (text) => {
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success('已複製到剪貼簿')
  } catch (error) {
    ElMessage.error('複製失敗')
  }
}

// 格式化日期
const formatDate = (date) => {
  return dayjs(date).format('YYYY-MM-DD')
}

const formatDateTime = (date) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

onMounted(() => {
  loadUsers()
})
</script>

<style scoped>
.user-password-reset {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  font-size: 20px;
}

.search-form {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.helper-text {
  font-size: 12px;
  color: #999;
  margin: 5px 0 0 0;
}

.mb-4 {
  margin-bottom: 16px;
}

.user-tag {
  margin-right: 8px;
  margin-bottom: 8px;
}

.no-selection {
  color: #999;
  font-size: 14px;
  margin: 0;
}

.temp-password-section {
  text-align: center;
}

.temp-password-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 10px;
}

.temp-password-box {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin-bottom: 10px;
}

.temp-password {
  font-size: 24px;
  font-family: 'Courier New', monospace;
  color: #409EFF;
  background-color: #f5f5f5;
  padding: 10px 20px;
  border-radius: 5px;
  font-weight: bold;
}

.mt-3 {
  margin-top: 12px;
}
</style>
