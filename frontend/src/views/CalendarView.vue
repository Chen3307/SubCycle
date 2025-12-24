<template>
  <div class="calendar-view">
    <h1 class="page-title">付款行事曆</h1>

    <el-card>
      <FullCalendar :options="calendarOptions" />
    </el-card>

    <!-- 事件詳情對話框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="selectedEvent?.title"
      width="400px"
      append-to-body
      :modal-append-to-body="true"
    >
      <div v-if="selectedEvent" class="event-details">
        <div class="detail-item">
          <span class="detail-label">訂閱名稱：</span>
          <span class="detail-value">{{ selectedEvent.title }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">金額：</span>
          <span class="detail-value amount">NT$ {{ selectedEvent.extendedProps.amount }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">付款日期：</span>
          <span class="detail-value">{{ formatDate(selectedEvent.start) }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">週期：</span>
          <span class="detail-value">{{ getCycleText(selectedEvent.extendedProps.cycle) }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">類別：</span>
          <span class="detail-value">{{ selectedEvent.extendedProps.categoryName }}</span>
        </div>
      </div>
      <template #footer>
        <el-button @click="dialogVisible = false">關閉</el-button>
        <el-button type="primary" @click="goToSubscriptions">查看詳情</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import FullCalendar from '@fullcalendar/vue3'
import dayGridPlugin from '@fullcalendar/daygrid'
import interactionPlugin from '@fullcalendar/interaction'
import { useSubscriptionStore } from '../stores/subscription'
import { useCategoryStore } from '../stores/category'
import dayjs from 'dayjs'

const router = useRouter()
const route = useRoute()
const subscriptionStore = useSubscriptionStore()
const categoryStore = useCategoryStore()

const dialogVisible = ref(false)
const selectedEvent = ref(null)
const lastOpenedId = ref(null)

// 生成未來 12 個月的付款事件
const calendarEvents = computed(() => {
  const events = []
  const today = dayjs()
  const endDate = today.add(12, 'month')

  subscriptionStore.subscriptions.forEach(sub => {
    // 檢查必要欄位，避免無限循環
    if (!sub.nextPaymentDate || !sub.cycle) {
      console.warn('跳過無效訂閱：', sub)
      return
    }

    let currentDate = dayjs(sub.nextPaymentDate)

    // 驗證日期是否有效
    if (!currentDate.isValid()) {
      console.warn('跳過無效日期的訂閱：', sub)
      return
    }

    const category = categoryStore.getCategoryById(sub.categoryId)

    // 生成未來 12 個月的重複付款事件
    let iterations = 0
    const maxIterations = 365 // 防止無限循環

    while (currentDate.isBefore(endDate) && iterations < maxIterations) {
      events.push({
        title: sub.name,
        start: currentDate.format('YYYY-MM-DD'),
        backgroundColor: category?.color || '#5b8def',
        borderColor: category?.color || '#5b8def',
        extendedProps: {
          amount: sub.amount,
          cycle: sub.cycle,
          categoryName: category?.name || '未分類',
          subscriptionId: sub.id
        }
      })

      // 根據週期計算下次付款日
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
        // 如果是未知的週期類型，預設為月付並警告
        console.warn('未知的週期類型：', sub.cycle, '訂閱：', sub.name)
        currentDate = currentDate.add(1, 'month')
      }

      iterations++
    }

    if (iterations >= maxIterations) {
      console.warn('訂閱事件生成達到最大次數限制：', sub.name)
    }
  })

  return events
})

const calendarOptions = computed(() => ({
  plugins: [dayGridPlugin, interactionPlugin],
  initialView: 'dayGridMonth',
  locale: 'zh-tw',
  headerToolbar: {
    left: 'prev,next today',
    center: 'title',
    right: 'dayGridMonth,dayGridWeek'
  },
  buttonText: {
    today: '今天',
    month: '月',
    week: '週'
  },
  events: calendarEvents.value,
  eventClick: handleEventClick,
  height: 'auto',
  firstDay: 0
}))

const openFromQuery = () => {
  const idStr = route.query.subscriptionId
  if (!idStr) return
  const id = Number(idStr)
  if (!Number.isFinite(id) || id === lastOpenedId.value) return
  const event = calendarEvents.value.find(item => item.extendedProps?.subscriptionId === id)
  if (!event) return

  lastOpenedId.value = id
  selectedEvent.value = event
  dialogVisible.value = true

  const { subscriptionId, ...rest } = route.query
  router.replace({ query: rest })
}

watch(
  () => [route.query.subscriptionId, calendarEvents.value.length],
  () => {
    openFromQuery()
  },
  { immediate: true }
)

const handleEventClick = (info) => {
  selectedEvent.value = info.event
  dialogVisible.value = true
}

const formatDate = (date) => {
  return dayjs(date).format('YYYY年MM月DD日')
}

const getCycleText = (cycle) => {
  const cycleMap = {
    daily: '每日',
    weekly: '每週',
    monthly: '每月',
    quarterly: '每季',
    yearly: '每年'
  }
  return cycleMap[cycle] || cycle
}

const goToSubscriptions = () => {
  dialogVisible.value = false
  router.push('/subscriptions')
}
</script>

<style scoped>
.calendar-view {
  max-width: 1400px;
  margin: 0 auto;
  position: relative;
}

.page-title {
  margin: 0 0 24px 0;
  font-size: 28px;
  color: var(--text-primary);
  transition: color 0.3s ease;
}

.event-details {
  padding: 10px 0;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid var(--border-color);
  transition: border-color 0.3s ease;
}

.detail-item:last-child {
  border-bottom: none;
}

:global(.dark-theme) .detail-item {
  border-bottom-color: var(--border-color);
}

.detail-label {
  color: var(--text-tertiary);
  font-size: 14px;
  transition: color 0.3s ease;
}

.detail-value {
  color: var(--text-primary);
  font-size: 14px;
  font-weight: 500;
  transition: color 0.3s ease;
}

.detail-value.amount {
  color: var(--accent-blue);
  font-size: 18px;
  font-weight: bold;
  transition: color 0.3s ease;
}

:global(.dark-theme) .detail-value.amount {
  color: var(--accent-mint);
}

/* FullCalendar 自定義樣式 */
:deep(.fc) {
  font-family: inherit;
  /* 啟用 GPU 加速以減少拖動殘影 */
  transform: translateZ(0);
  backface-visibility: hidden;
}

:deep(.fc-toolbar-title) {
  font-size: 20px;
  font-weight: bold;
}

:deep(.fc-button) {
  background-color: var(--primary-color);
  border-color: var(--primary-color);
  text-transform: none;
}

:deep(.fc-button:hover) {
  background-color: #79a4ff;
  border-color: #79a4ff;
}

:deep(.fc-button-active) {
  background-color: #3f6fd6 !important;
  border-color: #3f6fd6 !important;
}

:deep(.fc-event) {
  cursor: pointer;
  border-radius: 4px;
  padding: 2px 4px;
  /* 優化事件渲染性能 */
  transform: translateZ(0);
  will-change: transform;
}

:deep(.fc-daygrid-event) {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  /* 減少重繪 */
  backface-visibility: hidden;
}

/* 暗色模式 - FullCalendar 樣式調整 */
:global(.dark-theme) :deep(.fc-theme-standard th) {
  background-color: var(--bg-secondary) !important;
}

:global(.dark-theme) :deep(.fc-col-header-cell) {
  background-color: var(--bg-secondary) !important;
}

:global(.dark-theme) :deep(.fc-scrollgrid) {
  border-color: var(--border-color);
}

:global(.dark-theme) :deep(.fc-col-header-cell-cushion) {
  color: var(--text-primary);
}

:global(.dark-theme) :deep(.fc-toolbar-title) {
  color: var(--text-primary);
}

:global(.dark-theme) :deep(.fc-daygrid-day-number) {
  color: var(--text-primary);
}

:global(.dark-theme) :deep(.fc-day-today) {
  background-color: rgba(91, 141, 239, 0.18) !important;
}

:global(.dark-theme) :deep(.fc) {
  border-color: var(--border-color);
}

:global(.dark-theme) :deep(.fc td),
:global(.dark-theme) :deep(.fc th) {
  border-color: var(--border-color);
}
</style>
