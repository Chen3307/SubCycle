<template>
  <div class="categories-view">
    <div class="header-section">
      <h1 class="page-title">類別管理</h1>
      <el-button type="primary" :icon="Plus" @click="openAddDialog">
        新增類別
      </el-button>
    </div>

    <el-row :gutter="20">
      <el-col :span="16">
        <el-card>
          <el-table :data="categories" stripe style="width: 100%">
            <el-table-column prop="name" label="類別名稱" width="200" />
            <el-table-column label="顏色" width="80">
              <template #default="{ row }">
                <div class="color-display">
                  <div class="color-box" :style="{ backgroundColor: row.color }"></div>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="月均支出" width="150">
              <template #default="{ row }">
                <span class="amount">NT$ {{ getCategoryAmount(row.id).toFixed(0) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="訂閱數量" width="120">
              <template #default="{ row }">
                {{ getCategorySubscriptionCount(row.id) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180">
              <template #default="{ row }">
                <el-button size="small" type="primary" :icon="Edit" @click="openEditDialog(row)">
                  編輯
                </el-button>
                <el-button
                  size="small"
                  type="danger"
                  :icon="Delete"
                  :disabled="getCategorySubscriptionCount(row.id) > 0"
                  @click="handleDelete(row)"
                >
                  刪除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card>
          <template #header>
            <h3>類別統計</h3>
          </template>
          <div class="stats-container">
            <div class="stat-item">
              <div class="stat-label">總類別數</div>
              <div class="stat-value">{{ categories.length }}</div>
            </div>
            <div class="stat-item">
              <div class="stat-label">使用中的類別</div>
              <div class="stat-value">{{ activeCategories }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 新增/編輯對話框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'add' ? '新增類別' : '編輯類別'"
      width="450px"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
      >
        <el-form-item label="名稱" prop="name">
          <el-input v-model="form.name" placeholder="例如：影音娛樂" />
        </el-form-item>

        <el-form-item label="顏色" prop="color">
          <div class="color-picker-container">
            <el-color-picker v-model="form.color" />
            <el-input v-model="form.color" placeholder="#409EFF" style="margin-left: 10px" />
          </div>
        </el-form-item>

        <el-form-item label="快速選色">
          <div class="preset-colors">
            <div
              v-for="color in presetColors"
              :key="color"
              class="preset-color-box"
              :class="{ active: form.color === color }"
              :style="{ backgroundColor: color }"
              @click="form.color = color"
            ></div>
          </div>
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
import { useCategoryStore } from '../stores/category'
import { useSubscriptionStore } from '../stores/subscription'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'

const categoryStore = useCategoryStore()
const subscriptionStore = useSubscriptionStore()

const categories = computed(() => categoryStore.categories)
const subscriptions = computed(() => subscriptionStore.subscriptions)

const dialogVisible = ref(false)
const dialogMode = ref('add')
const formRef = ref(null)
const currentEditId = ref(null)

const form = reactive({
  name: '',
  color: '#409EFF'
})

const presetColors = [
  '#409EFF',
  '#67C23A',
  '#E6A23C',
  '#F56C6C',
  '#909399',
  '#8B5CF6',
  '#EC4899',
  '#10B981',
  '#F59E0B',
  '#3B82F6'
]

const rules = {
  name: [
    { required: true, message: '請輸入類別名稱', trigger: 'blur' }
  ],
  color: [
    { required: true, message: '請選擇顏色', trigger: 'change' }
  ]
}

const activeCategories = computed(() => {
  return categories.value.filter(cat => {
    return subscriptions.value.some(sub => sub.categoryId === cat.id)
  }).length
})

const getCategoryAmount = (categoryId) => {
  const distribution = categoryStore.categoryDistribution
  const category = distribution.find(d => {
    const cat = categoryStore.getCategoryById(categoryId)
    return cat && d.name === cat.name
  })
  return category?.amount || 0
}

const getCategorySubscriptionCount = (categoryId) => {
  return subscriptions.value.filter(sub => sub.categoryId === categoryId).length
}

const openAddDialog = () => {
  dialogMode.value = 'add'
  resetForm()
  dialogVisible.value = true
}

const openEditDialog = (category) => {
  dialogMode.value = 'edit'
  currentEditId.value = category.id
  Object.assign(form, {
    name: category.name,
    color: category.color
  })
  dialogVisible.value = true
}

const resetForm = () => {
  Object.assign(form, {
    name: '',
    color: '#409EFF'
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
        categoryStore.addCategory({ ...form })
        ElMessage.success('新增成功！')
      } else {
        categoryStore.updateCategory(currentEditId.value, { ...form })
        ElMessage.success('更新成功！')
      }
      dialogVisible.value = false
      resetForm()
    }
  })
}

const handleDelete = async (category) => {
  const count = getCategorySubscriptionCount(category.id)
  if (count > 0) {
    ElMessage.warning('此類別下還有訂閱項目，無法刪除')
    return
  }

  try {
    await ElMessageBox.confirm(
      `確定要刪除「${category.name}」類別嗎？`,
      '確認刪除',
      {
        confirmButtonText: '確定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    categoryStore.deleteCategory(category.id)
    ElMessage.success('刪除成功！')
  } catch {
    // 用戶取消刪除
  }
}
</script>

<style scoped>
.categories-view {
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

.color-display {
  display: flex;
  align-items: center;
  gap: 10px;
}

.color-box {
  width: 24px;
  height: 24px;
  border-radius: 4px;
  border: 1px solid #dcdfe6;
  transition: border-color 0.3s ease;
}

html.dark .color-box {
  border-color: var(--border-color);
}

.amount {
  color: #409eff;
  font-weight: 600;
  transition: color 0.3s ease;
}

html.dark .amount {
  color: #66b1ff;
}

.stats-container {
  padding: 20px 0;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 0;
  border-bottom: 1px solid #ebeef5;
  transition: border-color 0.3s ease;
}

.stat-item:last-child {
  border-bottom: none;
}

html.dark .stat-item {
  border-bottom-color: var(--border-color);
}

.stat-label {
  color: var(--text-tertiary);
  font-size: 14px;
  transition: color 0.3s ease;
}

.stat-value {
  color: var(--text-primary);
  font-size: 24px;
  font-weight: bold;
  transition: color 0.3s ease;
}

.color-picker-container {
  display: flex;
  align-items: center;
  width: 100%;
}

.preset-colors {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.preset-color-box {
  width: 36px;
  height: 36px;
  border-radius: 4px;
  cursor: pointer;
  border: 2px solid transparent;
  transition: all 0.2s;
}

.preset-color-box:hover {
  transform: scale(1.1);
  box-shadow: 0 0 8px rgba(0, 0, 0, 0.3);
}

.preset-color-box.active {
  border-color: #303133;
  box-shadow: 0 0 0 2px #fff, 0 0 0 4px #303133;
}

/* 暗色模式下的颜色选择框 */
html.dark .preset-color-box {
  border-color: var(--border-color);
}

html.dark .preset-color-box:hover {
  box-shadow: 0 0 12px rgba(255, 255, 255, 0.3);
  border-color: var(--text-tertiary);
}

html.dark .preset-color-box.active {
  border-color: #e5e5e5;
  box-shadow: 0 0 0 2px var(--bg-primary), 0 0 0 4px #e5e5e5;
}
</style>
