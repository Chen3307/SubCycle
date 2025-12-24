import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { useSubscriptionStore } from './subscription'
import { categoryAPI } from '../api/index'

export const useCategoryStore = defineStore('category', () => {
  const defaultCategories = [
    { id: 1, name: '影音娛樂', color: '#EF4444', icon: 'play-circle', sortOrder: 1 },
    { id: 2, name: '工作生產力', color: '#3B82F6', icon: 'briefcase', sortOrder: 2 },
    { id: 3, name: '生活與購物', color: '#10B981', icon: 'shopping-cart', sortOrder: 3 },
    { id: 4, name: '遊戲與社群', color: '#8B5CF6', icon: 'gamepad', sortOrder: 4 },
    { id: 5, name: '電信水費', color: '#F59E0B', icon: 'bolt', sortOrder: 5 }
  ]

  const categories = ref([])
  const loading = ref(false)
  const error = ref(null)

  const fetchCategories = async () => {
    try {
      loading.value = true
      const { data } = await categoryAPI.getAll()
      categories.value = Array.isArray(data) && data.length
        ? data
        : [...defaultCategories]
      error.value = null
    } catch (err) {
      // 若後端沒有資料，至少提供預設類別避免使用者需手動新增
      categories.value = [...defaultCategories]
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

    // 使用月均支出估算各類別分佈
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
  const updateCategory = async (id, updatedData) => {
    try {
      const payload = {
        name: updatedData.name,
        color: updatedData.color,
        icon: updatedData.icon,
        sortOrder: updatedData.sortOrder
      }
      const { data } = await categoryAPI.update(id, payload)
      const index = categories.value.findIndex(c => c.id === id)
      if (index !== -1) {
        categories.value[index] = data
      }
      return data
    } catch (err) {
      error.value = err.response?.data?.message || '更新類別失敗'
      throw err
    }
  }

  // 刪除類別
  const deleteCategory = async (id) => {
    try {
      await categoryAPI.remove(id)
      const index = categories.value.findIndex(c => c.id === id)
      if (index !== -1) {
        categories.value.splice(index, 1)
      }
    } catch (err) {
      error.value = err.response?.data?.message || '刪除類別失敗'
      throw err
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
    clear
  }
})
