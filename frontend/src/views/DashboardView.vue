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
            <Pie v-if="chartData.labels.length > 0" :data="chartData" :options="chartOptions" />
            <div v-else class="no-data">
              <el-empty description="暫無資料" />
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card class="upcoming-card">
          <template #header>
            <h3>7 天內即將到期</h3>
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

    <!-- 每週花費折線圖 -->
    <el-row :gutter="20" class="line-chart-row">
      <el-col :span="24">
        <el-card class="line-chart-card">
          <template #header>
            <div class="chart-header">
              <h3>未來 12 週花費趨勢</h3>
              <el-tag type="info">預估數據</el-tag>
            </div>
          </template>
          <div class="line-chart-container">
            <Line :data="lineChartData" :options="lineChartOptions" />
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed } from 'vue'
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

// 計算未來 12 週的花費數據
const weeklySpendingData = computed(() => {
  const weeks = []
  const today = dayjs()

  // 生成未來 12 週的數據
  for (let i = 0; i < 12; i++) {
    const weekStart = today.add(i, 'week').startOf('week')
    const weekEnd = weekStart.endOf('week')

    let weekTotal = 0

    // 計算這一週內的所有扣款
    subscriptions.value.forEach(sub => {
      let currentDate = dayjs(sub.nextPaymentDate)
      const endDate = weekEnd.add(1, 'year') // 查看一年內的重複扣款

      // 根據週期生成重複扣款
      while (currentDate.isBefore(endDate)) {
        if (currentDate.isAfter(weekStart) && currentDate.isBefore(weekEnd.add(1, 'day'))) {
          weekTotal += sub.amount
        }

        // 計算下次扣款日期
        if (sub.cycle === 'monthly') {
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

    weeks.push({
      label: `${weekStart.format('MM/DD')}`,
      fullLabel: `第 ${i + 1} 週 (${weekStart.format('MM/DD')} - ${weekEnd.format('MM/DD')})`,
      amount: weekTotal
    })
  }

  return weeks
})

// 折線圖數據
const lineChartData = computed(() => {
  const data = weeklySpendingData.value
  return {
    labels: data.map(w => w.label),
    datasets: [{
      label: '週支出',
      data: data.map(w => w.amount),
      borderColor: '#5b8def',
      backgroundColor: 'rgba(91, 141, 239, 0.12)',
      borderWidth: 2,
      tension: 0.4,
      fill: true,
      pointRadius: 4,
      pointHoverRadius: 6,
      pointBackgroundColor: '#5b8def',
      pointBorderColor: '#fff',
      pointBorderWidth: 2
    }]
  }
})

// 折線圖配置
const lineChartOptions = {
  responsive: true,
  maintainAspectRatio: true,
  aspectRatio: 3,
  plugins: {
    legend: {
      display: true,
      position: 'top'
    },
    tooltip: {
      callbacks: {
        title: function(context) {
          const index = context[0].dataIndex
          return weeklySpendingData.value[index].fullLabel
        },
        label: function(context) {
          return `支出: NT$ ${context.parsed.y.toFixed(0)}`
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
}

.chart-card,
.upcoming-card {
  min-height: 400px;
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
  min-height: 300px;
}

.upcoming-list {
  max-height: 340px;
  overflow-y: auto;
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
