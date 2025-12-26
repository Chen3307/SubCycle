<template>
  <div class="dashboard">
    <!-- 快速操作 -->
    <el-row :gutter="16" class="action-row">
      <el-col :span="24">
        <el-card class="action-card">
          <div class="action-card-header">
            <div>
              <div class="action-title">快速操作</div>
              <div class="action-subtitle">常用入口一鍵直達，少走幾步更方便</div>
            </div>
          </div>
          <div class="action-grid">
            <div
              v-for="action in quickActions"
              :key="action.label"
              class="action-tile"
              @click="action.onClick"
            >
              <div class="action-icon" :style="{ backgroundColor: action.bg }">
                <el-icon><component :is="action.icon" /></el-icon>
              </div>
              <div class="action-info">
                <div class="action-label">{{ action.label }}</div>
                <div class="action-desc">{{ action.desc }}</div>
              </div>
              <el-icon class="action-arrow"><ArrowRight /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 統計卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: var(--accent-blue)">
              <el-icon size="32"><TrendCharts /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">本月支出總額</div>
              <div class="stat-value">NT$ {{ currentMonthTotal.toFixed(0) }}</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: var(--success-color)">
              <el-icon size="32"><Calendar /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">下月扣款總額</div>
              <div class="stat-value">NT$ {{ nextMonthTotal.toFixed(0) }}</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: var(--accent-charcoal)">
              <el-icon size="32"><List /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">訂閱項目總數</div>
              <div class="stat-value">{{ subscriptions.length }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 圓餅圖和即將到期列表 -->
    <el-row :gutter="20" class="charts-row">
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <h3>支出類別分佈</h3>
          </template>
          <div class="chart-container">
            <Pie
              v-if="chartData.labels.length > 0"
              ref="pieChartRef"
              :data="chartData"
              :options="chartOptions"
            />
            <div v-else class="no-data">
              <el-empty description="暫無資料" />
            </div>
          </div>
          <div class="chart-meta" :class="{ 'is-placeholder': chartMeta.empty }">
            <div class="meta-title">{{ chartMeta.name || ' ' }}</div>
            <div class="meta-amount">
              <span v-if="!chartMeta.empty">
                NT$ {{ chartMeta.amount.toFixed(0) }}
                <span class="meta-percent">({{ chartMeta.percentage.toFixed(1) }}%)</span>
              </span>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card class="upcoming-card">
          <template #header>
            <h3>7 天內即將扣款</h3>
          </template>
          <div class="upcoming-list">
            <div v-if="upcomingSubscriptions.length > 0">
              <div
                v-for="sub in upcomingSubscriptions"
                :key="sub.id"
                class="upcoming-item"
              >
                <div class="upcoming-info">
                  <div class="upcoming-name">{{ sub.name }}</div>
                  <div class="upcoming-date">
                    {{ formatDate(sub.nextPaymentDate) }}
                  </div>
                </div>
                <div class="upcoming-amount">
                  NT$ {{ sub.amount }}
                </div>
              </div>
            </div>
            <div v-else class="no-data">
              <el-empty description="暫無即將扣款的項目" />
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Top 5 支出排行榜 -->
    <el-row :gutter="20" class="top-row">
      <el-col :span="24">
        <el-card class="top-card" v-if="topSubscriptions && topSubscriptions.length">
          <template #header>
            <h3>本月花費 Top 5</h3>
          </template>
          <div class="top-list">
            <div
              v-for="(sub, index) in topSubscriptions"
              :key="sub.id"
              class="top-item"
            >
              <div class="top-rank">
                <el-tag
                  :type="index === 0 ? 'danger' : index === 1 ? 'warning' : index === 2 ? 'success' : 'info'"
                  size="large"
                  class="rank-badge"
                >
                  {{ index + 1 }}
                </el-tag>
              </div>
              <div class="top-info">
                <div class="top-name">{{ sub.name }}</div>
                <div class="top-cycle">
                  {{ getCycleText(sub.cycle) }} - NT$ {{ sub.amount.toFixed(0) }}
                </div>
              </div>
              <div class="top-amount">
                <div class="monthly-amount">NT$ {{ sub.monthlyAmount.toFixed(0) }}</div>
                <div class="amount-label">本月支出</div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 月支出折線圖 -->
    <el-row :gutter="20" class="line-chart-row">
      <el-col :span="24">
        <el-card class="line-chart-card">
          <template #header>
            <div class="chart-header">
              <h3>過去 6 個月 & 未來 6 個月支出趨勢</h3>
              <div>
                <el-tag color="#70d5b4" style="color: white; border: none;">歷史數據</el-tag>
                <el-tag color="#6495ED" style="color: white; border: none; margin-left: 8px">預估數據</el-tag>
              </div>
            </div>
          </template>
          <div class="line-chart-container">
            <Line ref="lineChartRef" :data="lineChartData" :options="lineChartOptions" />
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { Pie, Line } from 'vue-chartjs'
import {
  Chart as ChartJS,
  ArcElement,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  Filler
} from 'chart.js'
import { useSubscriptionStore } from '../stores/subscription'
import { useCategoryStore } from '../stores/category'
import { useThemeStore } from '../stores/theme'
import { TrendCharts, Calendar, List, Plus, Download, ArrowRight, CollectionTag } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import api, { dashboardAPI } from '../api/index.js'
import dayjs from 'dayjs'
import isoWeek from 'dayjs/plugin/isoWeek'

dayjs.extend(isoWeek)

const router = useRouter()
const activeSliceIndex = ref(null)
const dashboardStats = ref(null)
const exporting = ref(false)
const pieChartRef = ref(null)
const lineChartRef = ref(null)

// 註冊 Chart.js 元件
ChartJS.register(
  ArcElement,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  Filler
)

// 確保圓餅圖動畫啟用
ChartJS.overrides.doughnut = ChartJS.overrides.doughnut || {}
ChartJS.overrides.doughnut.animation = {
  animateRotate: true,
  animateScale: true,
  duration: 2000,
  easing: 'easeInOutQuart'
}

ChartJS.overrides.pie = ChartJS.overrides.pie || {}
ChartJS.overrides.pie.animation = {
  animateRotate: true,
  animateScale: true,
  duration: 2000,
  easing: 'easeInOutQuart'
}

const subscriptionStore = useSubscriptionStore()
const categoryStore = useCategoryStore()
const themeStore = useThemeStore()
const isDarkMode = computed(() => themeStore.darkMode)

const toNumber = (value) => {
  if (value === null || value === undefined) return 0
  const numeric = Number(value)
  return Number.isFinite(numeric) ? numeric : 0
}

// 從 store 獲取數據
const subscriptions = computed(() => subscriptionStore.subscriptions)
const currentMonthTotal = computed(() => subscriptionStore.currentMonthTotal)
const nextMonthTotal = computed(() => subscriptionStore.nextMonthTotal)
const upcomingSubscriptions = computed(() => subscriptionStore.upcomingSubscriptions)
const categoryDistribution = computed(() => categoryStore.categoryDistribution)

// Top 5 排行
const topSubscriptions = computed(() => {
  if (!dashboardStats.value?.topSubscriptions) return []
  return dashboardStats.value.topSubscriptions.map(item => ({
    ...item,
    amount: toNumber(item.amount),
    monthlyAmount: toNumber(item.monthlyAmount)
  }))
})

const totalDistributionAmount = computed(() => {
  return categoryDistribution.value.reduce((sum, d) => sum + d.amount, 0)
})

// 圓餅圖數據與互動
const chartData = computed(() => {
  const distribution = categoryDistribution.value
  return {
    labels: distribution.map(d => d.name),
    datasets: [{
      data: distribution.map(d => d.amount),
      backgroundColor: distribution.map(d => d.color),
      borderWidth: 2,
      borderColor: '#fff',
      hoverOffset: 10,
      clip: 8,
      offset: 0,
      spacing: 0
    }]
  }
})

const chartMeta = computed(() => {
  const distribution = categoryDistribution.value
  if (!distribution.length) {
    return {
      name: '',
      amount: 0,
      percentage: 0,
      empty: true
    }
  }
  const idx = activeSliceIndex.value ?? 0
  const item = distribution[idx]
  const total = totalDistributionAmount.value || 1
  return {
    ...item,
    percentage: (item.amount / total) * 100,
    empty: false
  }
})

const chartOptions = computed(() => ({
  responsive: true,
  maintainAspectRatio: false,
  animation: {
    duration: 1000,
    easing: 'easeOutQuart',
    animateRotate: false,
    animateScale: true
  },
  onHover: (event, elements, chart) => {
    chart.canvas.style.cursor = 'default'
    if (elements?.length) {
      activeSliceIndex.value = elements[0].index
    }
  },
  onClick: (_event, elements) => {
    if (elements?.length) {
      activeSliceIndex.value = elements[0].index
    }
  },
  plugins: {
    legend: {
      position: 'bottom',
      labels: {
        color: isDarkMode.value ? '#ffffff' : '#1f2a33',
        padding: 15,
        font: {
          size: 13
        },
        usePointStyle: true,
        pointStyle: 'circle'
      }
    },
    tooltip: {
      enabled: true,
      backgroundColor: 'rgba(0, 0, 0, 0.8)',
      padding: 12,
      titleFont: {
        size: 14,
        weight: 'bold'
      },
      bodyFont: {
        size: 13
      },
      callbacks: {
        label: function(context) {
          const label = context.label || ''
          const value = context.parsed || 0
          const total = context.dataset.data.reduce((a, b) => a + b, 0)
          const percentage = ((value / total) * 100).toFixed(1)
          return `${label}: NT$ ${value.toFixed(0)} (${percentage}%)`
        }
      }
    }
  }
}))

const formatDate = (date) => {
  const d = dayjs(date).startOf('day')
  const today = dayjs().startOf('day')
  const diff = d.diff(today, 'day')

  if (diff === 0) return '今天'
  if (diff === 1) return '明天'
  if (diff === 2) return '後天'

  return dayjs(date).format('MM/DD (ddd)')
}

// 獲取儀表板統計數據
const fetchDashboardStats = async () => {
  try {
    const { data } = await dashboardAPI.getStatistics()
    dashboardStats.value = data
  } catch (error) {
    console.error('獲取儀表板統計失敗:', error)
  }
}

// 週期文字轉換
const getCycleText = (cycle) => {
  const cycleMap = {
    'daily': '每日',
    'weekly': '每週',
    'monthly': '每月',
    'quarterly': '每季',
    'yearly': '每年'
  }
  return cycleMap[cycle] || cycle
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

const goToSubscriptions = () => router.push('/subscriptions')
const goToCategories = () => router.push('/categories')
const goToCalendar = () => router.push('/calendar')
const addSubscription = () => router.push('/subscriptions?action=add')
const addCategory = () => router.push('/categories?action=add')

// 快速操作 - 匯出報表
const handleExport = async () => {
  try {
    exporting.value = true
    const endpoint = '/export/subscriptions/excel'
    const { data, headers } = await api.get(endpoint, { responseType: 'blob' })
    const blob = new Blob([data])
    const url = window.URL.createObjectURL(blob)
    const disposition = headers['content-disposition'] || ''
    const matched = disposition.match(/filename\\*?=(UTF-8'')?([^;]+)/i)
    const fallbackName = `subscriptions_${dayjs().format('YYYYMMDD')}.xlsx`
    const fileName = matched ? decodeURIComponent(matched[2].replace(/\"/g, '')) : fallbackName

    const link = document.createElement('a')
    link.href = url
    link.download = fileName
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('報表匯出完成')
  } catch (error) {
    console.error('匯出失敗:', error)
    ElMessage.error('匯出失敗，請稍後再試')
  } finally {
    exporting.value = false
  }
}

const quickActions = [
  {
    label: '新增訂閱',
    desc: '馬上建立新的訂閱紀錄',
    icon: Plus,
    bg: '#E6F7FF',
    onClick: () => addSubscription()
  },
  {
    label: '新增類別',
    desc: '先分類再記帳',
    icon: CollectionTag,
    bg: '#F5E8FF',
    onClick: () => addCategory()
  },
  {
    label: '查看行事曆',
    desc: '確認扣款日期',
    icon: Calendar,
    bg: '#E9FBF2',
    onClick: () => goToCalendar()
  },
  {
    label: '匯出訂閱',
    desc: '下載 Excel 報表',
    icon: Download,
    bg: '#FFF3E6',
    onClick: () => handleExport()
  }
]

// 組件掛載時獲取數據
onMounted(async () => {
  // 獲取數據（後端會自動推進過期日期）
  await Promise.all([
    subscriptionStore.fetchSubscriptions(),
    categoryStore.fetchCategories(),
    fetchDashboardStats()
  ])
  window.addEventListener('resize', handleResize, { passive: true })
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
})

let resizeTimeout = null
const handleResize = () => {
  // 防抖處理，避免頻繁觸發
  if (resizeTimeout) {
    clearTimeout(resizeTimeout)
  }

  resizeTimeout = setTimeout(() => {
    requestAnimationFrame(() => {
      // 先更新再調整尺寸
      if (pieChartRef.value?.chart) {
        pieChartRef.value.chart.resize()
        pieChartRef.value.chart.update('none')
      }
      if (lineChartRef.value?.chart) {
        lineChartRef.value.chart.resize()
        lineChartRef.value.chart.update('none')
      }
    })
  }, 100)
}

// 計算過去 6 個月 + 未來 6 個月的花費數據
const monthlySpendingData = computed(() => {
  const months = []
  const today = dayjs()

  // 生成過去 6 個月到未來 6 個月的數據（共 13 個月，包含當月）
  for (let i = -6; i <= 6; i++) {
    const monthStart = today.add(i, 'month').startOf('month')
    const monthEnd = monthStart.endOf('month')
    const isPast = i < 0
    const isCurrent = i === 0

    let monthTotal = 0

    // 計算這個月內的所有扣款
    subscriptions.value.forEach(sub => {
      // 如果沒有下次扣款日期，跳過
      if (!sub.nextPaymentDate) {
        return
      }

      const subEndDate = sub.endDate ? dayjs(sub.endDate) : null
      const subStartDate = sub.startDate ? dayjs(sub.startDate) : null
      const includeHistorical = sub.includeHistoricalPayments

      // 如果訂閱開始日期在該月之後，跳過
      if (subStartDate && subStartDate.isAfter(monthEnd)) {
        return
      }

      // 如果訂閱結束日期在該月之前，跳過
      if (subEndDate && subEndDate.isBefore(monthStart)) {
        return
      }

      // 如果不包含歷史支出，且該月在今天之前，跳過
      if (!includeHistorical && monthEnd.isBefore(today)) {
        return
      }

      const anchorDate = includeHistorical && subStartDate
        ? subStartDate
        : dayjs(sub.nextPaymentDate)
      const { amount, unit } = getCycleStep(sub.cycle)
      let currentDate = alignToRangeStart(anchorDate, monthStart, sub.cycle)

      // 從月初往後推算該月的所有扣款
      while (currentDate.isSameOrBefore(monthEnd)) {
        // 如果在該月份內
        if (currentDate.isSameOrAfter(monthStart) && currentDate.isSameOrBefore(monthEnd)) {
          let paymentDate = currentDate

          // 如果這筆扣款在 startDate 之前，檢查是否需要調整到 startDate
          if (subStartDate && currentDate.isBefore(subStartDate)) {
            // 計算下一次扣款日期
            const nextDate = currentDate.add(amount, unit)

            if (nextDate.isAfter(subStartDate)) {
              // 這是跨越 startDate 的扣款，調整到 startDate
              paymentDate = subStartDate
            } else {
              // 還沒到 startDate，跳過
              currentDate = nextDate
              continue
            }
          }

          // 檢查是否超過到期日
          const isAfterEnd = subEndDate && paymentDate.isAfter(subEndDate)

          // 如果沒有超過到期日，且在該月份內，才計入
          if (!isAfterEnd && paymentDate.isSameOrAfter(monthStart) && paymentDate.isSameOrBefore(monthEnd)) {
            // 如果不包含歷史支出，只計算今天之後的扣款
            if (!includeHistorical && paymentDate.isBefore(today)) {
              // 跳過這筆，繼續下一筆
            } else {
              // 計算這筆扣款
              monthTotal += sub.amount
            }
          }
        }

        // 計算下次扣款日期
        currentDate = currentDate.add(amount, unit)
      }
    })

    months.push({
      label: monthStart.format('YYYY/MM'),
      shortLabel: monthStart.format('MM月'),
      fullLabel: `${monthStart.format('YYYY年MM月')}`,
      amount: monthTotal,
      isPast,
      isCurrent
    })
  }

  return months
})

// 折線圖數據
const lineChartData = computed(() => {
  const data = monthlySpendingData.value
  const currentIndex = 6 // 當前月份的索引（過去 6 個月後）

  // 分割成歷史數據和預估數據
  const historicalData = data.slice(0, currentIndex + 1).map(m => m.amount)
  const forecastData = new Array(currentIndex).fill(null).concat(data.slice(currentIndex).map(m => m.amount))

  return {
    labels: data.map(m => m.shortLabel),
    datasets: [
      {
        label: '歷史支出',
        data: historicalData,
        borderColor: '#70d5b4',
        backgroundColor: 'rgba(112, 213, 180, 0.12)',
        borderWidth: 2,
        tension: 0.4,
        fill: true,
        pointRadius: 4,
        pointHoverRadius: 6,
        pointBackgroundColor: '#70d5b4',
        pointBorderColor: '#fff',
        pointBorderWidth: 2
      },
      {
        label: '預估支出',
        data: forecastData,
        borderColor: '#6495ED',
        backgroundColor: 'rgba(100, 149, 237, 0.1)',
        borderWidth: 2,
        borderDash: [5, 5],
        tension: 0.4,
        fill: true,
        pointRadius: 4,
        pointHoverRadius: 6,
        pointBackgroundColor: '#6495ED',
        pointBorderColor: '#fff',
        pointBorderWidth: 2
      }
    ]
  }
})

// 折線圖配置
const lineChartOptions = computed(() => ({
  responsive: true,
  maintainAspectRatio: true,
  aspectRatio: 3,
  animation: {
    duration: 2500,
    easing: 'easeOutCubic',
    y: {
      type: 'number',
      easing: 'easeOutCubic',
      duration: 2500,
      from: (ctx) => {
        if (ctx.type === 'data') {
          return ctx.chart.scales.y.getPixelForValue(0)
        }
      }
    }
  },
  transitions: {
    active: {
      animation: {
        duration: 300
      }
    }
  },
  interaction: {
    mode: 'index',
    intersect: false
  },
  plugins: {
    legend: {
      display: true,
      position: 'top',
      labels: {
        color: isDarkMode.value ? '#ffffff' : '#1f2a33'
      }
    },
    tooltip: {
      enabled: true,
      mode: 'index',
      intersect: false,
      backgroundColor: 'rgba(0, 0, 0, 0.8)',
      padding: 12,
      titleFont: {
        size: 14,
        weight: 'bold'
      },
      bodyFont: {
        size: 13
      },
      callbacks: {
        title: function(context) {
          const index = context[0].dataIndex
          const monthData = monthlySpendingData.value[index]
          return monthData.fullLabel + (monthData.isCurrent ? ' (本月)' : '')
        },
        label: function(context) {
          if (context.parsed.y === null) return null
          const label = context.dataset.label || ''
          return `${label}: NT$ ${context.parsed.y.toFixed(0)}`
        }
      }
    }
  },
  scales: {
    y: {
      beginAtZero: true,
      ticks: {
        color: isDarkMode.value ? '#ffffff' : '#1f2a33',
        callback: function(value) {
          return 'NT$ ' + value
        }
      },
      grid: {
        color: isDarkMode.value ? 'rgba(255, 255, 255, 0.12)' : 'rgba(0, 0, 0, 0.05)'
      }
    },
    x: {
      ticks: {
        color: isDarkMode.value ? '#ffffff' : '#1f2a33'
      },
      grid: {
        display: false
      }
    }
  }
}))

</script>

<style scoped>
.dashboard {
  max-width: 1400px;
  margin: 0 auto;
}

.action-row {
  margin-bottom: 16px;
}

.action-card {
  padding: 8px 12px;
}

.action-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 4px 16px;
}

.action-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
}

.action-subtitle {
  font-size: 13px;
  color: var(--text-tertiary);
  margin-top: 2px;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 12px;
}

.action-tile {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  border: 1px solid var(--border-color);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
  background-color: var(--bg-primary);
}

.action-tile:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.08);
}

.action-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-primary);
  flex-shrink: 0;
}

:deep(.action-icon .el-icon) {
  color: #1f2a33;
}

:global(.dark-theme) .action-icon {
  color: #1f2a33;
}

:global(.dark-theme) :deep(.action-icon .el-icon) {
  color: #1f2a33;
}

.action-info {
  flex: 1;
}

.action-label {
  font-weight: 600;
  color: var(--text-primary);
}

.action-desc {
  font-size: 12px;
  color: var(--text-tertiary);
}

.action-arrow {
  color: var(--text-tertiary);
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  height: 120px;
  border: none !important;
}

.stat-card :deep(.el-card__body) {
  border: none !important;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 20px;
  height: 100%;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.stat-info {
  flex: 1;
}

.stat-label {
  color: var(--text-tertiary);
  font-size: 14px;
  margin-bottom: 8px;
  transition: color 0.3s ease;
}

.stat-value {
  color: var(--text-primary);
  font-size: 28px;
  font-weight: bold;
  transition: color 0.3s ease;
}

.charts-row {
  margin-top: 20px;
  display: flex;
}

.charts-row .el-col {
  display: flex;
}

.chart-card,
.upcoming-card {
  min-height: 400px;
  width: 100%;
  display: flex;
  flex-direction: column;
}

.chart-card {
  overflow: hidden;
}

.upcoming-card :deep(.el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 0 !important;
}

.chart-card h3,
.upcoming-card h3 {
  margin: 0;
  font-size: 18px;
  color: var(--text-primary);
  transition: color 0.3s ease;
}

.chart-container {
  padding: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 480px;
  max-height: 480px;
  min-height: 480px;
}

.chart-container :deep(canvas) {
  display: block;
  width: 100% !important;
  height: 100% !important;
}

@media (max-width: 768px) {
  .chart-container {
    height: 380px;
    max-height: 380px;
    min-height: 380px;
  }
}

.chart-meta {
  padding: 0 20px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px solid var(--border-color);
  color: var(--text-primary);
  min-height: 56px;
}

.chart-meta.is-placeholder {
  visibility: hidden;
}

.meta-title {
  font-weight: 600;
  font-size: 16px;
}

:global(.dark-theme) .meta-title {
  color: #ffffff;
}

.meta-amount {
  font-weight: 700;
  color: var(--accent-blue);
}

.meta-percent {
  margin-left: 6px;
  color: var(--text-secondary);
}

:global(.dark-theme) .meta-amount {
  color: var(--accent-mint);
}

.upcoming-list {
  overflow-y: auto;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.upcoming-list > div:first-child {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.upcoming-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid var(--border-color);
  transition: background-color 0.2s;
}

.upcoming-item:hover {
  background-color: var(--hover-bg);
}

.upcoming-item:last-child {
  border-bottom: none;
}

.upcoming-info {
  flex: 1;
}

.upcoming-name {
  font-size: 16px;
  color: var(--text-primary);
  margin-bottom: 4px;
  transition: color 0.3s ease;
}

.upcoming-date {
  font-size: 14px;
  color: var(--text-tertiary);
  transition: color 0.3s ease;
}

.upcoming-amount {
  font-size: 18px;
  font-weight: bold;
  color: var(--accent-blue);
  transition: color 0.3s ease;
}

:global(.dark-theme) .upcoming-amount {
  color: var(--accent-mint);
}

.no-data {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 200px;
}

.line-chart-row {
  margin-top: 20px;
}

.line-chart-card {
  min-height: 400px;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-header h3 {
  margin: 0;
  font-size: 18px;
  color: var(--text-primary);
  transition: color 0.3s ease;
}

:global(.dark-theme) .chart-header h3 {
  color: #ffffff;
}

.line-chart-container {
  padding: 20px;
}

/* Top 5 排行榜樣式 */
.top-row {
  margin-top: 20px;
}

.top-card h3 {
  margin: 0;
  font-size: 18px;
  color: var(--text-primary);
}

.top-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.top-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: var(--bg-secondary, #f5f7fa);
  border-radius: 12px;
  transition: all 0.3s ease;
}

.top-item:hover {
  transform: translateX(8px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.top-rank {
  flex-shrink: 0;
}

.rank-badge {
  min-width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: bold;
  border-radius: 50%;
}

.top-info {
  flex: 1;
}

.top-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.top-cycle {
  font-size: 13px;
  color: var(--text-tertiary);
}

.top-amount {
  text-align: right;
  flex-shrink: 0;
}

.monthly-amount {
  font-size: 20px;
  font-weight: bold;
  color: var(--accent-blue);
}

.amount-label {
  font-size: 12px;
  color: var(--text-tertiary);
  margin-top: 2px;
}

:global(.dark-theme) .monthly-amount {
  color: var(--accent-mint);
}

:global(.dark-theme) .top-item {
  background: var(--bg-secondary, #2c2c2c);
}
</style>
