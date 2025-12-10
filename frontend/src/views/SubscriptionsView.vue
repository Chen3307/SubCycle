<template>
  <div class="subscriptions-view">
    <div class="header-section">
      <h1 class="page-title">訂閱管理</h1>
      <el-button type="primary" :icon="Plus" @click="openAddDialog">
        新增訂閱
      </el-button>
    </div>

    <el-card>
      <el-table :data="subscriptions" stripe style="width: 100%">
        <el-table-column prop="name" label="名稱" width="200" />
        <el-table-column label="金額" width="150">
          <template #default="{ row }">
            <span class="amount">NT$ {{ row.amount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="週期" width="120">
          <template #default="{ row }">
            <el-tag :type="getCycleTagType(row.cycle)">
              {{ getCycleText(row.cycle) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="下次扣款日" width="150">
          <template #default="{ row }">
            {{ formatDate(row.nextPaymentDate) }}
          </template>
        </el-table-column>
        <el-table-column label="類別" width="150">
          <template #default="{ row }">
            <el-tag>{{ getCategoryName(row.categoryId) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="月均支出" width="150">
          <template #default="{ row }">
            NT$ {{ getMonthlyAmount(row).toFixed(0) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" :icon="Edit" @click="openEditDialog(row)">
              編輯
            </el-button>
            <el-button size="small" type="danger" :icon="Delete" @click="handleDelete(row)">
              刪除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/編輯對話框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'add' ? '新增訂閱' : '編輯訂閱'"
      width="500px"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="名稱" prop="name">
          <el-input v-model="form.name" placeholder="例如：Netflix" />
        </el-form-item>

        <el-form-item label="金額" prop="amount">
          <el-input-number
            v-model="form.amount"
            :min="0"
            :step="10"
            placeholder="0"
            controls-position="right"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="週期" prop="cycle">
          <el-select v-model="form.cycle" placeholder="請選擇週期" style="width: 100%">
            <el-option label="每月" value="monthly" />
            <el-option label="每季" value="quarterly" />
            <el-option label="每年" value="yearly" />
          </el-select>
        </el-form-item>

        <el-form-item label="下次扣款日" prop="nextPaymentDate">
          <el-date-picker
            v-model="form.nextPaymentDate"
            type="date"
            placeholder="選擇日期"
            style="width: 100%"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>

        <el-form-item label="類別" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="請選擇類別" style="width: 100%">
            <el-option
              v-for="category in categories"
              :key="category.id"
              :label="category.name"
              :value="category.id"
            />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">確定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, reactive } from 'vue'
import { useSubscriptionStore } from '../stores/subscription'
import { useCategoryStore } from '../stores/category'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import dayjs from 'dayjs'

const subscriptionStore = useSubscriptionStore()
const categoryStore = useCategoryStore()

const subscriptions = computed(() => subscriptionStore.subscriptions)
const categories = computed(() => categoryStore.categories)

const dialogVisible = ref(false)
const dialogMode = ref('add')
const formRef = ref(null)
const currentEditId = ref(null)

const form = reactive({
  name: '',
  amount: 0,
  cycle: 'monthly',
  nextPaymentDate: '',
  categoryId: 1
})

const rules = {
  name: [
    { required: true, message: '請輸入訂閱名稱', trigger: 'blur' }
  ],
  amount: [
    { required: true, message: '請輸入金額', trigger: 'blur' }
  ],
  cycle: [
    { required: true, message: '請選擇週期', trigger: 'change' }
  ],
  nextPaymentDate: [
    { required: true, message: '請選擇下次扣款日', trigger: 'change' }
  ],
  categoryId: [
    { required: true, message: '請選擇類別', trigger: 'change' }
  ]
}

const openAddDialog = () => {
  dialogMode.value = 'add'
  resetForm()
  dialogVisible.value = true
}

const openEditDialog = (subscription) => {
  dialogMode.value = 'edit'
  currentEditId.value = subscription.id
  Object.assign(form, {
    name: subscription.name,
    amount: subscription.amount,
    cycle: subscription.cycle,
    nextPaymentDate: subscription.nextPaymentDate,
    categoryId: subscription.categoryId
  })
  dialogVisible.value = true
}

const resetForm = () => {
  Object.assign(form, {
    name: '',
    amount: 0,
    cycle: 'monthly',
    nextPaymentDate: dayjs().format('YYYY-MM-DD'),
    categoryId: 1
  })
  currentEditId.value = null
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate((valid) => {
    if (valid) {
      if (dialogMode.value === 'add') {
        subscriptionStore.addSubscription({ ...form })
        ElMessage.success('新增成功！')
      } else {
        subscriptionStore.updateSubscription(currentEditId.value, { ...form })
        ElMessage.success('更新成功！')
      }
      dialogVisible.value = false
      resetForm()
    }
  })
}

const handleDelete = async (subscription) => {
  try {
    await ElMessageBox.confirm(
      `確定要刪除「${subscription.name}」嗎？`,
      '確認刪除',
      {
        confirmButtonText: '確定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    subscriptionStore.deleteSubscription(subscription.id)
    ElMessage.success('刪除成功！')
  } catch {
    // 用戶取消刪除
  }
}

const getCategoryName = (categoryId) => {
  const category = categoryStore.getCategoryById(categoryId)
  return category?.name || '未分類'
}

const getCycleText = (cycle) => {
  const cycleMap = {
    monthly: '每月',
    quarterly: '每季',
    yearly: '每年'
  }
  return cycleMap[cycle] || cycle
}

const getCycleTagType = (cycle) => {
  const typeMap = {
    monthly: 'success',
    quarterly: 'warning',
    yearly: 'danger'
  }
  return typeMap[cycle] || 'info'
}

const getMonthlyAmount = (subscription) => {
  let amount = subscription.amount
  if (subscription.cycle === 'quarterly') {
    amount = subscription.amount / 3
  } else if (subscription.cycle === 'yearly') {
    amount = subscription.amount / 12
  }
  return amount
}

const formatDate = (date) => {
  return dayjs(date).format('YYYY-MM-DD')
}
</script>

<style scoped>
.subscriptions-view {
  max-width: 1400px;
  margin: 0 auto;
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-title {
  margin: 0;
  font-size: 28px;
  color: var(--text-primary);
  transition: color 0.3s ease;
}

.amount {
  color: #409eff;
  font-weight: 600;
  transition: color 0.3s ease;
}

html.dark .amount {
  color: #66b1ff;
}
</style>
