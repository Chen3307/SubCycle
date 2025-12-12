import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { useSubscriptionStore } from './subscription'
import { categoryAPI } from '../api/index'

export const useCategoryStore = defineStore('category', () => {
  const categories = ref([])
  const loading = ref(false)
  const error = ref(null)

  const fetchCategories = async () => {
    try {
      loading.value = true
      const { data } = await categoryAPI.getAll()
      categories.value = data
      error.value = null
    } catch (err) {
      error.value = err.response?.data?.message || '無法取得類別資料'
      throw err
    } finally {
      loading.value = false
    }
  }

  // 計算各類別的支出分佈（用於圓餅圖）
  const categoryDistribution = computed(() => {
    const subscriptionStore = useSubscriptionStore()
    const distribution = {}

    categories.value.forEach(cat => {
      distribution[cat.id] = {
        name: cat.name,
        color: cat.color,
        amount: 0
      }
    })

    subscriptionStore.subscriptions.forEach(sub => {
      if (distribution[sub.categoryId]) {
        let monthlyAmount = sub.amount
        if (sub.cycle === 'quarterly') {
          monthlyAmount = sub.amount / 3
        } else if (sub.cycle === 'yearly') {
          monthlyAmount = sub.amount / 12
        }
        distribution[sub.categoryId].amount += monthlyAmount
      }
    })

    return Object.values(distribution).filter(d => d.amount > 0)
  })

  // 新增類別
  const addCategory = async (category) => {
    const payload = {
      name: category.name,
      color: category.color,
      icon: category.icon,
      sortOrder: category.sortOrder
    }
    const { data } = await categoryAPI.create(payload)
    categories.value.push(data)
    return data
  }

  // 更新類別
  const updateCategory = (id, updatedData) => {
    const index = categories.value.findIndex(c => c.id === id)
    if (index !== -1) {
      categories.value[index] = {
        ...categories.value[index],
        ...updatedData
      }
    }
  }

  // 刪除類別
  const deleteCategory = (id) => {
    const index = categories.value.findIndex(c => c.id === id)
    if (index !== -1) {
      categories.value.splice(index, 1)
    }
  }

  // 根據 ID 取得類別
  const getCategoryById = (id) => {
    return categories.value.find(c => c.id === id)
  }

  const clear = () => {
    categories.value = []
    error.value = null
  }

  // 載入模擬數據（用於前端預覽）
  const loadMockData = () => {
    categories.value = [
      { id: 1, name: '串流影音', color: '#EF4444', icon: 'play-circle' },
      { id: 2, name: '音樂', color: '#F59E0B', icon: 'music' },
      { id: 3, name: '雲端儲存', color: '#10B981', icon: 'cloud' },
      { id: 4, name: '生產力工具', color: '#3B82F6', icon: 'briefcase' },
      { id: 5, name: '遊戲娛樂', color: '#8B5CF6', icon: 'gamepad' },
      { id: 6, name: '健康運動', color: '#EC4899', icon: 'heart' },
      { id: 7, name: '新聞雜誌', color: '#6366F1', icon: 'newspaper' },
      { id: 8, name: '學習教育', color: '#14B8A6', icon: 'book' },
      { id: 9, name: '設計開發', color: '#F97316', icon: 'code' },
      { id: 10, name: '生活購物', color: '#84CC16', icon: 'shopping-cart' }
    ]
  }

  return {
    categories,
    loading,
    error,
    categoryDistribution,
    fetchCategories,
    addCategory,
    updateCategory,
    deleteCategory,
    getCategoryById,
    clear,
    loadMockData
  }
})
