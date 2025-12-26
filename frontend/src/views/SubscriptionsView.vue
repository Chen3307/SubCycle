<template>
  <div class="subscriptions-view">
    <div class="header-section">
      <h1 class="page-title">訂閱管理</h1>
      <div class="header-actions">
        <el-button type="primary" class="add-subscription-btn" :icon="Plus" @click="openAddDialog">
          新增訂閱
        </el-button>
      </div>
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
            <el-tag
              :color="getCycleTagColor(row.cycle)"
              :style="{ color: 'white', border: 'none' }"
            >
              {{ getCycleText(row.cycle) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="起始日" width="150">
          <template #default="{ row }">
            <span v-if="row.startDate">{{ formatDate(row.startDate) }}</span>
            <span v-else class="text-muted">—</span>
          </template>
        </el-table-column>
        <el-table-column label="下次扣款日" width="150">
          <template #default="{ row }">
            <span>{{ formatDate(row.nextPaymentDate) }}</span>
            <el-tag
              v-if="isOverdue(row.nextPaymentDate)"
              type="danger"
              size="small"
              effect="plain"
              class="overdue-tag"
            >
              待續費
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="到期日" width="150">
          <template #default="{ row }">
            <span v-if="row.endDate">{{ formatDate(row.endDate) }}</span>
            <span v-else class="text-muted">—</span>
          </template>
        </el-table-column>
        <el-table-column label="類別" width="150">
          <template #default="{ row }">
            <el-tag
              class="category-tag"
              :color="getCategoryColor(row.categoryId)"
              :style="{ color: '#ffffff', border: 'none' }"
            >
              {{ getCategoryName(row.categoryId) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="本月支出" width="150">
          <template #default="{ row }">
            NT$ {{ getCurrentMonthAmount(row).toFixed(0) }}
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
    <el-config-provider :locale="zhTw">
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
        <el-form-item label="類別" prop="categoryId">
          <el-select
            v-model="form.categoryId"
            :placeholder="categoryPlaceholder"
            style="width: 100%"
            :loading="categoryLoading"
            :disabled="categoryLoading || categories.length === 0"
          >
            <el-option
              v-for="category in categories"
              :key="category.id"
              :label="category.name"
              :value="category.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="服務名稱" prop="name">
          <el-select
            v-model="form.name"
            filterable
            allow-create
            default-first-option
            :placeholder="servicePlaceholder"
            style="width: 100%"
            :disabled="!form.categoryId"
          >
            <el-option
              v-for="service in serviceOptions"
              :key="service"
              :label="service"
              :value="service"
            />
          </el-select>
          <template #extra>
            <span class="text-muted">可自訂輸入服務名稱</span>
          </template>
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
            <el-option label="每日" value="daily" />
            <el-option label="每週" value="weekly" />
            <el-option label="每月" value="monthly" />
            <el-option label="每季" value="quarterly" />
            <el-option label="每年" value="yearly" />
          </el-select>
        </el-form-item>

        <el-form-item label="訂閱起始日" prop="startDate">
          <el-date-picker
            v-model="form.startDate"
            type="date"
            placeholder="選擇起始日"
            style="width: 100%"
            :format="dateFormat"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>

        <el-form-item label="下次扣款日" prop="nextPaymentDate">
          <el-date-picker
            v-model="form.nextPaymentDate"
            type="date"
            placeholder="選擇日期"
            style="width: 100%"
            :format="dateFormat"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>

        <el-form-item label="合約到期日">
          <el-date-picker
            v-model="form.endDate"
            type="date"
            placeholder="選擇到期日（若無限期請留白）"
            style="width: 100%"
            :format="dateFormat"
            value-format="YYYY-MM-DD"
            clearable
          />
          <template #extra>
            <span class="text-muted">無限期續訂請留白；填寫後到期日後將不再推進扣款。（選填）</span>
          </template>
        </el-form-item>

        <el-form-item label="續費提醒">
          <el-switch v-model="form.notificationEnabled" />
        </el-form-item>

        <el-form-item label="推算歷史支出">
          <el-switch v-model="form.includeHistoricalPayments" />
          <template #extra>
            <span class="text-muted">開啟後，儀表板折線圖將從訂閱起始日開始推算過去的歷史支出（選填）</span>
          </template>
        </el-form-item>
        </el-form>

        <template #footer>
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">確定</el-button>
        </template>
      </el-dialog>
    </el-config-provider>
  </div>
</template>

<script setup>
import { ref, computed, reactive, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useSubscriptionStore } from '../stores/subscription'
import { useCategoryStore } from '../stores/category'
import { subscriptionTemplateAPI } from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'
import zhTw from 'element-plus/es/locale/lang/zh-tw'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import dayjs from 'dayjs'

const subscriptionStore = useSubscriptionStore()
const categoryStore = useCategoryStore()
const route = useRoute()
const router = useRouter()

const subscriptions = computed(() => subscriptionStore.subscriptions)
const categories = computed(() => categoryStore.categories)
const categoryLoading = computed(() => categoryStore.loading)
const categoryPlaceholder = computed(() => categories.value.length ? '請選擇類別' : '請先建立類別')
const servicePlaceholder = computed(() => {
  if (!form.categoryId) return '請先選擇類別'
  return serviceOptions.value.length ? '請選擇服務名稱' : '可自行輸入服務名稱'
})

const dialogVisible = ref(false)
const dialogMode = ref('add')
const formRef = ref(null)
const currentEditId = ref(null)
const subscriptionTemplates = ref([])
const templatesLoading = ref(false)
const lastOpenedId = ref(null)
const dateFormat = ref(localStorage.getItem('preferences.dateFormat') || 'YYYY-MM-DD')

const form = reactive({
  name: '',
  amount: 0,
  cycle: 'monthly',
  startDate: '',
  nextPaymentDate: '',
  endDate: '',
  categoryId: null,
  notificationEnabled: true,
  includeHistoricalPayments: false
})

const normalizeCategoryName = (name) => {
  return (name || '').replace(/\s+/g, ' ').trim()
}

const selectedCategoryName = computed(() => {
  const category = categories.value.find((item) => item.id === form.categoryId)
  return normalizeCategoryName(category?.name || '')
})

const serviceOptions = computed(() => {
  if (!selectedCategoryName.value) return []

  // 从模板中筛选当前类别的服务
  return subscriptionTemplates.value
    .filter(template => normalizeCategoryName(template.categoryName) === selectedCategoryName.value)
    .map(template => template.name)
})

const setDefaultCategory = () => {
  if (categories.value.length > 0) {
    const preferredOrder = [
      '影音娛樂',
      '工作生產力',
      '生活與購物',
      '遊戲與社群',
      '其他項目'
    ]
    const normalizedMap = new Map(
      categories.value.map((category) => [normalizeCategoryName(category.name), category.id])
    )
    const preferredId = preferredOrder
      .map((name) => normalizedMap.get(normalizeCategoryName(name)))
      .find((id) => id)
    form.categoryId = preferredId || categories.value[0].id
  } else {
    form.categoryId = null
  }
}

// 获取订阅模板
const fetchSubscriptionTemplates = async () => {
  try {
    templatesLoading.value = true
    const response = await subscriptionTemplateAPI.getAll()
    subscriptionTemplates.value = response.data
  } catch (error) {
    console.error('获取订阅模板失败:', error)
    ElMessage.warning('无法加载预设订阅项目，您仍可自行输入')
  } finally {
    templatesLoading.value = false
  }
}

onMounted(async () => {
  try {
    // 總是重新抓類別，以確保新增的預設類別會顯示
    await categoryStore.fetchCategories()
    // 获取订阅模板
    await fetchSubscriptionTemplates()
    setDefaultCategory()

    // 進入頁面時自動推進過期訂閱，避免需要手動操作
    await subscriptionStore.rollOverExpiredDates()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '載入資料失敗')
  }
})

watch(
  () => categories.value.length,
  () => {
    // 類別清單變動時，若尚未選取，套用第一個類別
    if (!form.categoryId) {
      setDefaultCategory()
    }
  }
)

watch(
  () => form.categoryId,
  () => {
    if (dialogMode.value !== 'add') return
    if (!form.name && serviceOptions.value.length > 0) {
      form.name = serviceOptions.value[0]
    }
  }
)


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
  startDate: [
    { required: true, message: '請選擇訂閱起始日', trigger: 'change' }
  ],
  categoryId: [
    { required: true, message: '請選擇類別', trigger: 'change' }
  ]
}

const openAddDialog = async () => {
  dialogMode.value = 'add'
  dateFormat.value = localStorage.getItem('preferences.dateFormat') || 'YYYY-MM-DD'

  // 確保類別已加載
  if (categories.value.length === 0) {
    try {
      await categoryStore.fetchCategories()
    } catch (error) {
      ElMessage.error('無法加載類別，請稍後再試')
      return
    }
  }

  resetForm()
  dialogVisible.value = true
}

const openEditDialog = async (subscription) => {
  dialogMode.value = 'edit'
  currentEditId.value = subscription.id
  dateFormat.value = localStorage.getItem('preferences.dateFormat') || 'YYYY-MM-DD'

  // 確保類別已加載
  if (categories.value.length === 0) {
    try {
      await categoryStore.fetchCategories()
    } catch (error) {
      ElMessage.error('無法加載類別，請稍後再試')
      return
    }
  }

  Object.assign(form, {
    name: subscription.name,
    amount: subscription.amount,
    cycle: subscription.cycle,
    startDate: subscription.startDate || subscription.nextPaymentDate,
    nextPaymentDate: subscription.nextPaymentDate,
    endDate: subscription.endDate || '',
    categoryId: subscription.categoryId || null,
    notificationEnabled: subscription.notificationEnabled ?? true,
    includeHistoricalPayments: subscription.includeHistoricalPayments ?? false
  })

  // 如果訂閱沒有類別，設置默認類別
  if (!form.categoryId && categories.value.length > 0) {
    setDefaultCategory()
  }

  dialogVisible.value = true
}

const openFromQuery = async () => {
  if (!subscriptions.value.length) return
  const idStr = route.query.subscriptionId
  if (!idStr) return
  const id = Number(idStr)
  if (!Number.isFinite(id) || id === lastOpenedId.value) return
  const subscription = subscriptions.value.find(item => item.id === id)
  if (!subscription) return

  lastOpenedId.value = id
  await openEditDialog(subscription)

  const { subscriptionId, ...rest } = route.query
  router.replace({ query: rest })
}

watch(
  () => [route.query.subscriptionId, subscriptions.value.length],
  () => {
    openFromQuery()
  },
  { immediate: true }
)

const resetForm = () => {
  Object.assign(form, {
    name: '',
    amount: 0,
    cycle: 'monthly',
    startDate: dayjs().format('YYYY-MM-DD'),
    nextPaymentDate: dayjs().format('YYYY-MM-DD'),
    endDate: '',
    categoryId: null,
    notificationEnabled: true,
    includeHistoricalPayments: false
  })
  setDefaultCategory()
  currentEditId.value = null
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      if (dialogMode.value === 'add') {
        await subscriptionStore.addSubscription({ ...form })
        ElMessage.success('新增成功！')
      } else {
        await subscriptionStore.updateSubscription(currentEditId.value, { ...form })
        ElMessage.success('更新成功！')
      }
      dialogVisible.value = false
      resetForm()
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '操作失敗，請稍後再試')
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

    await subscriptionStore.deleteSubscription(subscription.id)
    ElMessage.success('刪除成功！')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '刪除失敗')
    }
  }
}

const getCategoryName = (categoryId) => {
  const category = categoryStore.getCategoryById(categoryId)
  return category?.name || '未分類'
}

const getCategoryColor = (categoryId) => {
  const category = categoryStore.getCategoryById(categoryId)
  return category?.color || '#5b8def'
}

const getCycleText = (cycle) => {
  const cycleMap = {
    daily: '每日',
    weekly: '每週',
    monthly: '每月',
    quarterly: '每季',
    yearly: '每年'
  }
  return cycleMap[cycle] || cycle
}

const getCycleTagColor = (cycle) => {
  const colorMap = {
    daily: '#409EFF',
    weekly: '#67C23A',
    monthly: '#70d5b4',
    quarterly: '#E6A23C',
    yearly: '#F56C6C'
  }
  return colorMap[cycle] || '#909399'
}

const getCycleStep = (cycle) => {
  if (cycle === 'daily') return { amount: 1, unit: 'day' }
  if (cycle === 'weekly') return { amount: 1, unit: 'week' }
  if (cycle === 'monthly') return { amount: 1, unit: 'month' }
  if (cycle === 'quarterly') return { amount: 3, unit: 'month' }
  if (cycle === 'yearly') return { amount: 1, unit: 'year' }
  return { amount: 1, unit: 'month' }
}

const forEachPaymentInRange = (subscription, rangeStart, rangeEnd, onPayment) => {
  if (!subscription?.nextPaymentDate || !onPayment) return
  let cursor = dayjs(subscription.nextPaymentDate)
  if (!cursor.isValid()) return

  const startDate = subscription.startDate ? dayjs(subscription.startDate) : null
  const endDate = subscription.endDate ? dayjs(subscription.endDate) : null

  // 如果訂閱開始日在範圍結束之後，或結束日在範圍開始之前，直接返回
  if (startDate && startDate.isAfter(rangeEnd, 'day')) return
  if (endDate && endDate.isBefore(rangeStart, 'day')) return

  const { amount, unit } = getCycleStep(subscription.cycle)
  const maxIterations = 1000
  let iterations = 0

  // 先往前推到目標範圍之前
  while (cursor.isAfter(rangeStart) && iterations < maxIterations) {
    cursor = cursor.subtract(amount, unit)
    iterations++
  }

  // 從範圍開始往後遍歷所有扣款
  while (cursor.isSameOrBefore(rangeEnd) && iterations < maxIterations) {
    // 檢查是否在範圍內
    if (cursor.isSameOrAfter(rangeStart) && cursor.isSameOrBefore(rangeEnd)) {
      let paymentDate = cursor

      // 如果這筆扣款在 startDate 之前，且是第一次扣款，調整到 startDate
      if (startDate && cursor.isBefore(startDate)) {
        // 檢查下一次扣款是否在 startDate 之後
        const nextCursor = cursor.add(amount, unit)
        if (nextCursor.isAfter(startDate)) {
          // 這是跨越 startDate 的扣款，調整到 startDate
          paymentDate = startDate
        } else {
          // 還沒到 startDate，跳過
          cursor = cursor.add(amount, unit)
          iterations++
          continue
        }
      }

      // 檢查是否超過 endDate
      const isAfterEnd = endDate && paymentDate.isAfter(endDate)
      if (!isAfterEnd && paymentDate.isSameOrAfter(rangeStart) && paymentDate.isSameOrBefore(rangeEnd)) {
        onPayment(paymentDate)
      }
    }

    cursor = cursor.add(amount, unit)
    iterations++
  }
}

const getCurrentMonthAmount = (subscription) => {
  const today = dayjs()
  const monthStart = today.startOf('month')
  const monthEnd = today.endOf('month')

  let total = 0
  forEachPaymentInRange(subscription, monthStart, monthEnd, () => {
    total += subscription.amount
  })

  return total
}

const formatDate = (date) => {
  return dayjs(date).format(dateFormat.value)
}

const isOverdue = (date) => {
  if (!date) return false
  return dayjs(date).isBefore(dayjs(), 'day')
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

.header-actions {
  display: flex;
  gap: 10px;
}

.page-title {
  margin: 0;
  font-size: 28px;
  color: var(--text-primary);
  transition: color 0.3s ease;
}

.amount {
  color: var(--accent-blue);
  font-weight: 600;
  transition: color 0.3s ease;
}

:global(.dark-theme) .amount {
  color: var(--accent-mint);
}

.add-subscription-btn {
  background-color: #6495ED;
  border-color: #6495ED;
  color: #fff;
}

.add-subscription-btn:hover {
  background-color: #4169E1;
  border-color: #4169E1;
}

.overdue-tag {
  margin-left: 8px;
}

.text-muted {
  color: var(--text-tertiary);
}

:global(.dark-theme) :deep(.el-table__body tr:hover > td.el-table__cell),
:global(.dark-theme) :deep(.el-table__body tr:hover > td) {
  background-color: var(--hover-bg) !important;
  color: var(--text-primary) !important;
}

:global(.dark-theme) :deep(.el-table__row:hover) {
  background-color: var(--hover-bg) !important;
}

:global(.dark-theme) .subscriptions-view :deep(.el-table__body tr.hover-row > td.el-table__cell),
:global(.dark-theme) .subscriptions-view :deep(.el-table__body tr.hover-row > td) {
  background-color: var(--hover-bg) !important;
  color: var(--text-primary) !important;
}

:global(.dark-theme) .subscriptions-view :deep(.el-table__body tr.el-table__row--striped > td.el-table__cell),
:global(.dark-theme) .subscriptions-view :deep(.el-table__body tr.el-table__row--striped > td) {
  background-color: var(--bg-secondary) !important;
  color: var(--text-primary) !important;
}

</style>
