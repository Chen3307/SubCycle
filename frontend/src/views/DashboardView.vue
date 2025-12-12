<template>
  <div class="dashboard">
    <h1 class="page-title">儀表板</h1>

    <!-- 統計卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: var(--accent-blue)">
              <el-icon size="32"><TrendCharts /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">總月均支出</div>
              <div class="stat-value">NT$ {{ monthlyAverage.toFixed(0) }}</div>
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
              <div class="stat-label">下 30 天應付總額</div>
              <div class="stat-value">NT$ {{ next30DaysTotal.toFixed(0) }}</div>
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
            <Pie v-if="chartData.labels.length > 0" :key="chartKey" :data="chartData" :options="chartOptions" />
            <div v-else class="no-data">
              <el-empty description="暫無資料" />
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
              <el-empty description="無即將到期的訂閱" />
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
                <el-tag color="#52C9A6" style="color: white; border: none;">歷史數據</el-tag>
                <el-tag color="#6495ED" style="color: white; border: none; margin-left: 8px">預估數據</el-tag>
              </div>
            </div>
          </template>
          <div class="line-chart-container">
            <Line :key="chartKey" :data="lineChartData" :options="lineChartOptions" />
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
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
import { TrendCharts, Calendar, List } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import isoWeek from 'dayjs/plugin/isoWeek'

dayjs.extend(isoWeek)

// 用于强制图表重新渲染
const chartKey = ref(0)

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

const subscriptionStore = useSubscriptionStore()
const categoryStore = useCategoryStore()

// 從 store 獲取數據
const subscriptions = computed(() => subscriptionStore.subscriptions)
const monthlyAverage = computed(() => subscriptionStore.monthlyAverage)
const next30DaysTotal = computed(() => subscriptionStore.next30DaysTotal)
const upcomingSubscriptions = computed(() => subscriptionStore.upcomingSubscriptions)
const categoryDistribution = computed(() => categoryStore.categoryDistribution)

// 圓餅圖數據
const chartData = computed(() => {
  const distribution = categoryDistribution.value
  return {
    labels: distribution.map(d => d.name),
    datasets: [{
      data: distribution.map(d => d.amount),
      backgroundColor: distribution.map(d => d.color),
      borderWidth: 2,
      borderColor: '#fff'
    }]
  }
})

const chartOptions = {
  responsive: true,
  maintainAspectRatio: true,
  plugins: {
    legend: {
      position: 'bottom'
    },
    tooltip: {
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
}

const formatDate = (date) => {
  const d = dayjs(date)
  const today = dayjs()
  const diff = d.diff(today, 'day')

  if (diff === 0) return '今天'
  if (diff === 1) return '明天'
  if (diff === 2) return '後天'

  return d.format('MM/DD (ddd)')
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
      let currentDate = dayjs(sub.nextPaymentDate)
      const endDate = monthEnd.add(2, 'year') // 查看 2 年內的重複扣款

      // 根據週期生成重複扣款
      while (currentDate.isBefore(endDate)) {
        if (currentDate.isSameOrAfter(monthStart) && currentDate.isSameOrBefore(monthEnd)) {
          monthTotal += sub.amount
        }

        // 計算下次扣款日期
        if (sub.cycle === 'daily') {
          currentDate = currentDate.add(1, 'day')
        } else if (sub.cycle === 'weekly') {
          currentDate = currentDate.add(1, 'week')
        } else if (sub.cycle === 'monthly') {
          currentDate = currentDate.add(1, 'month')
        } else if (sub.cycle === 'quarterly') {
          currentDate = currentDate.add(3, 'month')
        } else if (sub.cycle === 'yearly') {
          currentDate = currentDate.add(1, 'year')
        } else {
          break
        }
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
        borderColor: '#52C9A6',
        backgroundColor: 'rgba(82, 201, 166, 0.1)',
        borderWidth: 2,
        tension: 0.4,
        fill: true,
        pointRadius: 4,
        pointHoverRadius: 6,
        pointBackgroundColor: '#52C9A6',
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
const lineChartOptions = {
  responsive: true,
  maintainAspectRatio: true,
  aspectRatio: 3,
  animation: {
    duration: 1500,
    easing: 'easeInOutQuart',
    delay: (context) => {
      let delay = 0
      if (context.type === 'data' && context.mode === 'default') {
        delay = context.dataIndex * 100
      }
      return delay
    }
  },
  interaction: {
    mode: 'index',
    intersect: false
  },
  plugins: {
    legend: {
      display: true,
      position: 'top'
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
        callback: function(value) {
          return 'NT$ ' + value
        }
      },
      grid: {
        color: 'rgba(0, 0, 0, 0.05)'
      }
    },
    x: {
      grid: {
        display: false
      }
    }
  }
}

// 处理窗口大小变化
const handleResize = () => {
  chartKey.value += 1
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.dashboard {
  max-width: 1400px;
  margin: 0 auto;
}

.page-title {
  margin: 0 0 24px 0;
  font-size: 28px;
  color: var(--text-primary);
  transition: color 0.3s ease;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  height: 120px;
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
  min-height: 400px;
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

html.dark .upcoming-amount {
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

.line-chart-container {
  padding: 20px;
}
</style>
