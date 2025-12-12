import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import dayjs from 'dayjs'
import { subscriptionAPI } from '../api/index'

export const useSubscriptionStore = defineStore('subscription', () => {
  const subscriptions = ref([])
  const loading = ref(false)
  const error = ref(null)

  const fetchSubscriptions = async () => {
    try {
      loading.value = true
      const { data } = await subscriptionAPI.getAll()
      subscriptions.value = data
      error.value = null
    } catch (err) {
      error.value = err.response?.data?.message || '無法取得訂閱資料'
      throw err
    } finally {
      loading.value = false
    }
  }

  // 計算未來 30 天應付總額
  const next30DaysTotal = computed(() => {
    const today = dayjs()
    const next30Days = today.add(30, 'day')

    return subscriptions.value
      .filter(sub => {
        const paymentDate = dayjs(sub.nextPaymentDate)
        return paymentDate.isAfter(today) && paymentDate.isBefore(next30Days)
      })
      .reduce((total, sub) => total + sub.amount, 0)
  })

  // 計算總月均支出
  const monthlyAverage = computed(() => {
    return subscriptions.value.reduce((total, sub) => {
      let monthlyAmount = sub.amount
      if (sub.cycle === 'quarterly') {
        monthlyAmount = sub.amount / 3
      } else if (sub.cycle === 'yearly') {
        monthlyAmount = sub.amount / 12
      }
      return total + monthlyAmount
    }, 0)
  })

  // 取得 7 天內即將到期的項目
  const upcomingSubscriptions = computed(() => {
    const today = dayjs()
    const next7Days = today.add(7, 'day')

    return subscriptions.value
      .filter(sub => {
        const paymentDate = dayjs(sub.nextPaymentDate)
        return paymentDate.isAfter(today) && paymentDate.isBefore(next7Days)
      })
      .sort((a, b) => dayjs(a.nextPaymentDate).diff(dayjs(b.nextPaymentDate)))
  })

  // 新增訂閱
  const addSubscription = async (subscription) => {
    const payload = {
      name: subscription.name,
      amount: Number(subscription.amount),
      cycle: subscription.cycle,
      nextPaymentDate: subscription.nextPaymentDate,
      categoryId: subscription.categoryId,
      status: 'active',
      description: subscription.description || ''
    }
    const { data } = await subscriptionAPI.create(payload)
    subscriptions.value.push(data)
    return data
  }

  // 更新訂閱
  const updateSubscription = async (id, updatedData) => {
    const payload = {
      name: updatedData.name,
      amount: Number(updatedData.amount),
      cycle: updatedData.cycle,
      nextPaymentDate: updatedData.nextPaymentDate,
      categoryId: updatedData.categoryId,
      status: updatedData.status || 'active',
      description: updatedData.description || ''
    }
    const { data } = await subscriptionAPI.update(id, payload)
    const index = subscriptions.value.findIndex(s => s.id === id)
    if (index !== -1) {
      subscriptions.value[index] = data
    }
    return data
  }

  // 刪除訂閱
  const deleteSubscription = async (id) => {
    await subscriptionAPI.remove(id)
    const index = subscriptions.value.findIndex(s => s.id === id)
    if (index !== -1) {
      subscriptions.value.splice(index, 1)
    }
  }

  const clear = () => {
    subscriptions.value = []
    error.value = null
  }

  // 載入模擬數據（用於前端預覽）
  const loadMockData = () => {
    const today = dayjs()
    subscriptions.value = [
      // 串流影音
      { id: 1, name: 'Netflix', amount: 390, cycle: 'monthly', nextPaymentDate: today.add(3, 'day').format('YYYY-MM-DD'), categoryId: 1, status: 'active', description: 'Premium 4K 方案' },
      { id: 2, name: 'Disney+', amount: 270, cycle: 'monthly', nextPaymentDate: today.add(8, 'day').format('YYYY-MM-DD'), categoryId: 1, status: 'active', description: '迪士尼全系列' },
      { id: 3, name: 'HBO GO', amount: 190, cycle: 'monthly', nextPaymentDate: today.add(13, 'day').format('YYYY-MM-DD'), categoryId: 1, status: 'active', description: 'HBO 原創影集' },
      { id: 4, name: 'friDay影音', amount: 199, cycle: 'monthly', nextPaymentDate: today.add(6, 'day').format('YYYY-MM-DD'), categoryId: 1, status: 'active', description: '台灣本地平台' },

      // 音樂
      { id: 5, name: 'Spotify', amount: 149, cycle: 'monthly', nextPaymentDate: today.add(2, 'day').format('YYYY-MM-DD'), categoryId: 2, status: 'active', description: 'Premium 個人方案' },
      { id: 6, name: 'YouTube Premium', amount: 179, cycle: 'monthly', nextPaymentDate: today.add(10, 'day').format('YYYY-MM-DD'), categoryId: 2, status: 'active', description: '無廣告 + Music' },
      { id: 7, name: 'Apple Music', amount: 150, cycle: 'monthly', nextPaymentDate: today.add(24, 'day').format('YYYY-MM-DD'), categoryId: 2, status: 'active', description: '無損音質' },

      // 雲端儲存
      { id: 8, name: 'Google One', amount: 65, cycle: 'monthly', nextPaymentDate: today.add(16, 'day').format('YYYY-MM-DD'), categoryId: 3, status: 'active', description: '100GB 空間' },
      { id: 9, name: 'Dropbox', amount: 330, cycle: 'monthly', nextPaymentDate: today.add(29, 'day').format('YYYY-MM-DD'), categoryId: 3, status: 'active', description: '2TB 專業方案' },
      { id: 10, name: 'iCloud+', amount: 900, cycle: 'yearly', nextPaymentDate: today.add(93, 'day').format('YYYY-MM-DD'), categoryId: 3, status: 'active', description: '200GB 年付' },

      // 生產力工具
      { id: 11, name: 'ChatGPT Plus', amount: 600, cycle: 'monthly', nextPaymentDate: today.add(4, 'day').format('YYYY-MM-DD'), categoryId: 4, status: 'active', description: 'GPT-4 存取' },
      { id: 12, name: 'Notion', amount: 150, cycle: 'monthly', nextPaymentDate: today.add(7, 'day').format('YYYY-MM-DD'), categoryId: 4, status: 'active', description: 'Personal Pro' },
      { id: 13, name: 'Microsoft 365', amount: 2190, cycle: 'yearly', nextPaymentDate: today.add(51, 'day').format('YYYY-MM-DD'), categoryId: 4, status: 'active', description: '含 1TB OneDrive' },
      { id: 14, name: 'Evernote', amount: 169, cycle: 'monthly', nextPaymentDate: today.add(27, 'day').format('YYYY-MM-DD'), categoryId: 4, status: 'active', description: 'Professional' },

      // 遊戲娛樂
      { id: 15, name: 'Xbox Game Pass', amount: 490, cycle: 'monthly', nextPaymentDate: today.add(5, 'day').format('YYYY-MM-DD'), categoryId: 5, status: 'active', description: 'Ultimate 方案' },
      { id: 16, name: 'PlayStation Plus', amount: 1350, cycle: 'quarterly', nextPaymentDate: today.add(66, 'day').format('YYYY-MM-DD'), categoryId: 5, status: 'active', description: 'Extra 級別' },
      { id: 17, name: 'Nintendo Switch Online', amount: 290, cycle: 'yearly', nextPaymentDate: today.add(130, 'day').format('YYYY-MM-DD'), categoryId: 5, status: 'active', description: '擴充包' },
      { id: 18, name: 'Steam 錢包', amount: 500, cycle: 'monthly', nextPaymentDate: today.add(18, 'day').format('YYYY-MM-DD'), categoryId: 5, status: 'active', description: '每月預算' },

      // 健康運動
      { id: 19, name: 'Nike Training Club', amount: 0, cycle: 'yearly', nextPaymentDate: today.add(140, 'day').format('YYYY-MM-DD'), categoryId: 6, status: 'active', description: '免費健身' },
      { id: 20, name: 'MyFitnessPal', amount: 320, cycle: 'yearly', nextPaymentDate: today.add(88, 'day').format('YYYY-MM-DD'), categoryId: 6, status: 'active', description: '營養追蹤' },
      { id: 21, name: 'Headspace', amount: 399, cycle: 'yearly', nextPaymentDate: today.add(44, 'day').format('YYYY-MM-DD'), categoryId: 6, status: 'active', description: '冥想練習' },

      // 新聞雜誌
      { id: 22, name: 'The New York Times', amount: 140, cycle: 'monthly', nextPaymentDate: today.add(1, 'day').format('YYYY-MM-DD'), categoryId: 7, status: 'active', description: '數位訂閱' },
      { id: 23, name: 'Readmoo 讀墨', amount: 199, cycle: 'monthly', nextPaymentDate: today.add(31, 'day').format('YYYY-MM-DD'), categoryId: 7, status: 'active', description: '電子書吃到飽' },
      { id: 24, name: '天下雜誌', amount: 99, cycle: 'monthly', nextPaymentDate: today.add(9, 'day').format('YYYY-MM-DD'), categoryId: 7, status: 'active', description: '數位版' },

      // 學習教育
      { id: 25, name: 'Hahow 好學校', amount: 490, cycle: 'monthly', nextPaymentDate: today.add(12, 'day').format('YYYY-MM-DD'), categoryId: 8, status: 'active', description: '無限會員' },
      { id: 26, name: 'Coursera Plus', amount: 1780, cycle: 'yearly', nextPaymentDate: today.add(171, 'day').format('YYYY-MM-DD'), categoryId: 8, status: 'active', description: '7000+ 課程' },
      { id: 27, name: 'Duolingo Super', amount: 420, cycle: 'yearly', nextPaymentDate: today.add(70, 'day').format('YYYY-MM-DD'), categoryId: 8, status: 'active', description: '語言學習' },

      // 設計開發
      { id: 28, name: 'GitHub Copilot', amount: 300, cycle: 'monthly', nextPaymentDate: today.add(14, 'day').format('YYYY-MM-DD'), categoryId: 9, status: 'active', description: 'AI 程式助手' },
      { id: 29, name: 'Figma', amount: 450, cycle: 'monthly', nextPaymentDate: today.add(22, 'day').format('YYYY-MM-DD'), categoryId: 9, status: 'active', description: 'Professional' },
      { id: 30, name: 'Adobe Creative Cloud', amount: 1785, cycle: 'monthly', nextPaymentDate: today.add(15, 'day').format('YYYY-MM-DD'), categoryId: 9, status: 'active', description: '攝影方案' },

      // 生活購物
      { id: 31, name: 'Amazon Prime', amount: 170, cycle: 'monthly', nextPaymentDate: today.add(26, 'day').format('YYYY-MM-DD'), categoryId: 10, status: 'active', description: '免運 + Video' },
      { id: 32, name: 'Costco 會員', amount: 1350, cycle: 'yearly', nextPaymentDate: today.add(245, 'day').format('YYYY-MM-DD'), categoryId: 10, status: 'active', description: '金星會員' },
      { id: 33, name: 'Line 貼圖小舖', amount: 150, cycle: 'monthly', nextPaymentDate: today.add(11, 'day').format('YYYY-MM-DD'), categoryId: 10, status: 'active', description: '每月預算' }
    ]
  }

  return {
    subscriptions,
    loading,
    error,
    next30DaysTotal,
    monthlyAverage,
    upcomingSubscriptions,
    fetchSubscriptions,
    addSubscription,
    updateSubscription,
    deleteSubscription,
    clear,
    loadMockData
  }
})
