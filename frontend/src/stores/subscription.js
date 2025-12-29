import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import dayjs from 'dayjs'
import { subscriptionAPI } from '../api/index'

export const useSubscriptionStore = defineStore('subscription', () => {
  const subscriptions = ref([])
  const loading = ref(false)
  const error = ref(null)
  const readNotifications = ref(JSON.parse(localStorage.getItem('readNotifications') || '[]'))

  // 後端欄位(price/billingCycle/category) 轉前端欄位(amount/cycle/categoryId)
  const normalizeSubscription = (sub) => ({
    id: sub.id,
    name: sub.name,
    amount: Number(sub.price ?? sub.amount ?? 0),
    cycle: sub.billingCycle ?? sub.cycle ?? 'monthly',
    nextPaymentDate: sub.nextPaymentDate,
    startDate: sub.startDate ?? sub.nextPaymentDate ?? null,
    endDate: sub.endDate ?? null,
    categoryId: sub.category?.id ?? sub.categoryId ?? null,
    status: sub.status ?? 'active',
    description: sub.description ?? '',
    currency: sub.currency ?? 'TWD',
    autoRenew: sub.autoRenew ?? true,
    reminderSent: sub.reminderSent ?? false,
    notificationEnabled: sub.notificationEnabled ?? true,
    includeHistoricalPayments: sub.includeHistoricalPayments ?? false,
    logoUrl: sub.logoUrl ?? '',
    websiteUrl: sub.websiteUrl ?? '',
    notes: sub.notes ?? ''
  })

  const fetchSubscriptions = async () => {
    try {
      loading.value = true
      const { data } = await subscriptionAPI.getAll()
      subscriptions.value = (data || []).map(normalizeSubscription)
      error.value = null
    } catch (err) {
      error.value = err.response?.data?.message || '無法取得訂閱資料'
      throw err
    } finally {
      loading.value = false
    }
  }

  const getCycleStep = (cycle) => {
    if (cycle === 'daily') return { amount: 1, unit: 'day' }
    if (cycle === 'weekly') return { amount: 1, unit: 'week' }
    if (cycle === 'monthly') return { amount: 1, unit: 'month' }
    if (cycle === 'quarterly') return { amount: 3, unit: 'month' }
    if (cycle === 'yearly') return { amount: 1, unit: 'year' }
    return { amount: 1, unit: 'month' }
  }

  const alignToRangeStart = (anchorDate, rangeStart, cycle) => {
    if (!anchorDate?.isValid()) return anchorDate
    const { amount, unit } = getCycleStep(cycle)
    let cursor = anchorDate

    if (cursor.isBefore(rangeStart, 'day')) {
      const diff = rangeStart.diff(cursor, unit, true)
      const steps = Math.floor(diff / amount)
      if (steps > 0) {
        cursor = cursor.add(steps * amount, unit)
      }
      while (cursor.isBefore(rangeStart, 'day')) {
        cursor = cursor.add(amount, unit)
      }
    } else if (cursor.isAfter(rangeStart, 'day')) {
      const diff = cursor.diff(rangeStart, unit, true)
      const steps = Math.floor(diff / amount)
      if (steps > 0) {
        cursor = cursor.subtract(steps * amount, unit)
      }
      while (cursor.isAfter(rangeStart, 'day')) {
        cursor = cursor.subtract(amount, unit)
      }
      if (cursor.isBefore(rangeStart, 'day')) {
        cursor = cursor.add(amount, unit)
      }
    }

    return cursor
  }

  const forEachPaymentInRange = (subscription, rangeStart, rangeEnd, onPayment) => {
    if (!subscription?.nextPaymentDate || !onPayment) return
    const startDate = subscription.startDate ? dayjs(subscription.startDate) : null
    const endDate = subscription.endDate ? dayjs(subscription.endDate) : null
    const includeHistorical = subscription.includeHistoricalPayments ?? false
    const anchorDate = includeHistorical && startDate
      ? startDate
      : dayjs(subscription.nextPaymentDate)
    let cursor = alignToRangeStart(anchorDate, rangeStart, subscription.cycle)
    if (!cursor?.isValid()) return

    const { amount, unit } = getCycleStep(subscription.cycle)
    const maxIterations = 1000
    let iterations = 0

    while (cursor.isSameOrBefore(rangeEnd, 'day') && iterations < maxIterations) {
      let paymentDate = cursor

      if (startDate && cursor.isBefore(startDate, 'day')) {
        const nextDate = cursor.add(amount, unit)
        if (nextDate.isAfter(startDate, 'day')) {
          paymentDate = startDate
        } else {
          cursor = nextDate
          iterations++
          continue
        }
      }

      const isAfterEnd = endDate && paymentDate.isAfter(endDate, 'day')
      if (!isAfterEnd
        && paymentDate.isSameOrAfter(rangeStart, 'day')
        && paymentDate.isSameOrBefore(rangeEnd, 'day')) {
        onPayment(paymentDate)
      }
      cursor = cursor.add(amount, unit)
      iterations++
    }
  }

  const calculateRangeTotal = (rangeStart, rangeEnd) => {
    let total = 0
    subscriptions.value.forEach(sub => {
      forEachPaymentInRange(sub, rangeStart, rangeEnd, (paymentDate) => {
        // 計算該時間範圍內的所有扣款，不受 includeHistoricalPayments 影響
        total += sub.amount
      })
    })
    return total
  }

  // 計算本月扣款總額（含重複扣款次數）
  const currentMonthTotal = computed(() => {
    const today = dayjs()
    const monthStart = today.startOf('month')
    const monthEnd = today.endOf('month')
    return calculateRangeTotal(monthStart, monthEnd)
  })

  // 計算下個月扣款總額（含重複扣款次數）
  const nextMonthTotal = computed(() => {
    const today = dayjs()
    const nextMonth = today.add(1, 'month')
    const monthStart = nextMonth.startOf('month')
    const monthEnd = nextMonth.endOf('month')

    return calculateRangeTotal(monthStart, monthEnd)
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

  // 取得 7 天內即將到期的項目（用於儀表板，顯示所有）
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

  // 取得未讀的即將到期項目（用於右上角通知）
  const unreadUpcomingSubscriptions = computed(() => {
    return upcomingSubscriptions.value.filter(sub => !readNotifications.value.includes(sub.id))
  })

  // 標記通知為已讀
  const markNotificationAsRead = (subscriptionId) => {
    if (!readNotifications.value.includes(subscriptionId)) {
      readNotifications.value.push(subscriptionId)
      localStorage.setItem('readNotifications', JSON.stringify(readNotifications.value))
    }
  }

  // 清除所有已讀通知
  const clearReadNotifications = () => {
    readNotifications.value = []
    localStorage.removeItem('readNotifications')
  }

  // 新增訂閱
  const addSubscription = async (subscription) => {
    const payload = {
      name: subscription.name,
      amount: Number(subscription.amount),
      cycle: subscription.cycle,
      nextPaymentDate: subscription.nextPaymentDate,
      startDate: subscription.startDate || subscription.nextPaymentDate,
      endDate: subscription.endDate || null,
      categoryId: subscription.categoryId,
      status: 'active',
      description: subscription.description || '',
      notificationEnabled: subscription.notificationEnabled ?? true,
      includeHistoricalPayments: subscription.includeHistoricalPayments ?? false
    }
    const { data } = await subscriptionAPI.create(payload)
    const normalized = normalizeSubscription(data)
    subscriptions.value.push(normalized)
    return normalized
  }

  // 更新訂閱
  const updateSubscription = async (id, updatedData) => {
    const payload = {
      name: updatedData.name,
      amount: Number(updatedData.amount),
      cycle: updatedData.cycle,
      nextPaymentDate: updatedData.nextPaymentDate,
      startDate: updatedData.startDate || updatedData.nextPaymentDate,
      endDate: updatedData.endDate || null,
      categoryId: updatedData.categoryId,
      status: updatedData.status || 'active',
      description: updatedData.description || '',
      notificationEnabled: updatedData.notificationEnabled ?? true,
      includeHistoricalPayments: updatedData.includeHistoricalPayments ?? false
    }
    const { data } = await subscriptionAPI.update(id, payload)
    const normalized = normalizeSubscription(data)
    const index = subscriptions.value.findIndex(s => s.id === id)
    if (index !== -1) {
      subscriptions.value[index] = normalized
    }
    return normalized
  }

  // 刪除訂閱
  const deleteSubscription = async (id) => {
    await subscriptionAPI.remove(id)
    const index = subscriptions.value.findIndex(s => s.id === id)
    if (index !== -1) {
      subscriptions.value.splice(index, 1)
    }
  }

  // 手動推進過期的扣款日（呼叫後端 rollover）
  const rollOverExpiredDates = async () => {
    const { data } = await subscriptionAPI.rollover()
    // 重新取得訂閱以反映最新日期
    await fetchSubscriptions()
    return data?.updated ?? 0
  }

  const clear = () => {
    subscriptions.value = []
    error.value = null
    clearReadNotifications()
  }

  return {
    subscriptions,
    loading,
    error,
    currentMonthTotal,
    nextMonthTotal,
    monthlyAverage,
    upcomingSubscriptions,
    unreadUpcomingSubscriptions,
    fetchSubscriptions,
    addSubscription,
    updateSubscription,
    deleteSubscription,
    rollOverExpiredDates,
    clear,
    markNotificationAsRead,
    clearReadNotifications
  }
})
