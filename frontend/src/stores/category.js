import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { useSubscriptionStore } from './subscription'

export const useCategoryStore = defineStore('category', () => {
  const categories = ref([
    { id: 1, name: '影音娛樂', color: '#409EFF', icon: 'VideoPlay' },
    { id: 2, name: '工作軟體', color: '#67C23A', icon: 'Briefcase' },
    { id: 3, name: '健康運動', color: '#F56C6C', icon: 'TrophyBase' },
    { id: 4, name: '學習成長', color: '#E6A23C', icon: 'Reading' },
    { id: 5, name: '其他', color: '#909399', icon: 'More' }
  ])

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
  const addCategory = (category) => {
    const newId = Math.max(...categories.value.map(c => c.id), 0) + 1
    categories.value.push({
      ...category,
      id: newId
    })
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

  return {
    categories,
    categoryDistribution,
    addCategory,
    updateCategory,
    deleteCategory,
    getCategoryById
  }
})
