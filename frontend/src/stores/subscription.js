import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import dayjs from 'dayjs'

export const useSubscriptionStore = defineStore('subscription', () => {
  const subscriptions = ref([
    // 預設的範例資料
    {
      id: 1,
      name: 'Netflix',
      amount: 390,
      cycle: 'monthly',
      nextPaymentDate: '2025-12-15',
      categoryId: 1,
      createdAt: '2025-01-01'
    },
    {
      id: 2,
      name: 'Spotify',
      amount: 149,
      cycle: 'monthly',
      nextPaymentDate: '2025-12-10',
      categoryId: 1,
      createdAt: '2025-01-15'
    },
    {
      id: 3,
      name: 'Adobe Creative Cloud',
      amount: 1680,
      cycle: 'monthly',
      nextPaymentDate: '2025-12-20',
      categoryId: 2,
      createdAt: '2025-02-01'
    }
  ])

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
  const addSubscription = (subscription) => {
    const newId = Math.max(...subscriptions.value.map(s => s.id), 0) + 1
    subscriptions.value.push({
      ...subscription,
      id: newId,
      createdAt: dayjs().format('YYYY-MM-DD')
    })
  }

  // 更新訂閱
  const updateSubscription = (id, updatedData) => {
    const index = subscriptions.value.findIndex(s => s.id === id)
    if (index !== -1) {
      subscriptions.value[index] = {
        ...subscriptions.value[index],
        ...updatedData
      }
    }
  }

  // 刪除訂閱
  const deleteSubscription = (id) => {
    const index = subscriptions.value.findIndex(s => s.id === id)
    if (index !== -1) {
      subscriptions.value.splice(index, 1)
    }
  }

  // 根據週期計算下次付款日
  const calculateNextPaymentDate = (currentDate, cycle) => {
    const date = dayjs(currentDate)
    switch (cycle) {
      case 'monthly':
        return date.add(1, 'month').format('YYYY-MM-DD')
      case 'quarterly':
        return date.add(3, 'month').format('YYYY-MM-DD')
      case 'yearly':
        return date.add(1, 'year').format('YYYY-MM-DD')
      default:
        return date.format('YYYY-MM-DD')
    }
  }

  return {
    subscriptions,
    next30DaysTotal,
    monthlyAverage,
    upcomingSubscriptions,
    addSubscription,
    updateSubscription,
    deleteSubscription,
    calculateNextPaymentDate
  }
})
