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
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import FullCalendar from '@fullcalendar/vue3'
import dayGridPlugin from '@fullcalendar/daygrid'
import interactionPlugin from '@fullcalendar/interaction'
import { useSubscriptionStore } from '../stores/subscription'
import { useCategoryStore } from '../stores/category'
import dayjs from 'dayjs'

const router = useRouter()
const subscriptionStore = useSubscriptionStore()
const categoryStore = useCategoryStore()

const dialogVisible = ref(false)
const selectedEvent = ref(null)

// 生成未來 12 個月的付款事件
const calendarEvents = computed(() => {
  const events = []
  const today = dayjs()
  const endDate = today.add(12, 'month')

  subscriptionStore.subscriptions.forEach(sub => {
    let currentDate = dayjs(sub.nextPaymentDate)
    const category = categoryStore.getCategoryById(sub.categoryId)

    // 生成未來 12 個月的重複付款事件
    while (currentDate.isBefore(endDate)) {
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
      if (sub.cycle === 'monthly') {
        currentDate = currentDate.add(1, 'month')
      } else if (sub.cycle === 'quarterly') {
        currentDate = currentDate.add(3, 'month')
      } else if (sub.cycle === 'yearly') {
        currentDate = currentDate.add(1, 'year')
      }
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

const handleEventClick = (info) => {
  selectedEvent.value = info.event
  dialogVisible.value = true
}

const formatDate = (date) => {
  return dayjs(date).format('YYYY年MM月DD日')
}

const getCycleText = (cycle) => {
  const cycleMap = {
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

html.dark .detail-item {
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

html.dark .detail-value.amount {
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
html.dark :deep(.fc-theme-standard th) {
  background-color: var(--bg-secondary) !important;
}

html.dark :deep(.fc-col-header-cell) {
  background-color: var(--bg-secondary) !important;
}

html.dark :deep(.fc-scrollgrid) {
  border-color: var(--border-color);
}

html.dark :deep(.fc-col-header-cell-cushion) {
  color: var(--text-primary);
}

html.dark :deep(.fc-toolbar-title) {
  color: var(--text-primary);
}

html.dark :deep(.fc-daygrid-day-number) {
  color: var(--text-primary);
}

html.dark :deep(.fc-day-today) {
  background-color: rgba(91, 141, 239, 0.18) !important;
}

html.dark :deep(.fc) {
  border-color: var(--border-color);
}

html.dark :deep(.fc td),
html.dark :deep(.fc th) {
  border-color: var(--border-color);
}
</style>
