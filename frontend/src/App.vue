<script setup>
import { computed, ref, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from './stores/auth'
import { useSubscriptionStore } from './stores/subscription'
import { useCategoryStore } from './stores/category'
import { useThemeStore } from './stores/theme'
import {
  SwitchButton,
  Plus
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import UserIcon from './components/UserIcon.vue'
import BellIcon from './components/BellIcon.vue'
import dayjs from 'dayjs'
import Class from './components/Class.vue'
import Subscribe from './components/Subscribe.vue'
import Calendar from './components/Calendar.vue'
import Dashboard from './components/Dashboard.vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const subscriptionStore = useSubscriptionStore()
const categoryStore = useCategoryStore()
const themeStore = useThemeStore()

// 開發模式標記
const isDev = import.meta.env.DEV

const isLoggedIn = computed(() => !!authStore.token)
const showLayout = computed(() => {
  return route.path !== '/login' && route.path !== '/register'
})

// 通知相關
const notificationVisible = ref(false)
const upcomingSubscriptions = computed(() => subscriptionStore.unreadUpcomingSubscriptions)
const notificationCount = computed(() => upcomingSubscriptions.value.length)

// 快速新增訂閱對話框
const quickAddVisible = ref(false)

const handleLogout = async () => {
  // 先清理状态
  authStore.logout()
  subscriptionStore.clear()
  categoryStore.clear()

  // 使用 nextTick 确保 DOM 更新完成后再跳转
  await router.push('/')
}

const handleQuickAdd = () => {
  router.push('/subscriptions')
}

const handleNotificationClick = (subscription) => {
  subscriptionStore.markNotificationAsRead(subscription.id)
}

// 載入模擬數據（開發用）
const loadPreviewData = () => {
  categoryStore.loadMockData()
  subscriptionStore.loadMockData()
  ElMessage.success('已載入預覽模擬數據！')
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

// 初始化時檢查登入狀態
authStore.checkAuth()

const loadData = async () => {
  if (!authStore.token) return
  try {
    await Promise.all([
      categoryStore.fetchCategories(),
      subscriptionStore.fetchSubscriptions()
    ])
  } catch (error) {
    // 在 header 區域不打斷使用者流程，錯誤交由各頁面提示
    console.error('載入資料失敗', error)
  }
}

onMounted(() => {
  loadData()
})

watch(
  () => authStore.token,
  (token) => {
    if (token) {
      loadData()
    } else {
      subscriptionStore.clear()
      categoryStore.clear()
    }
  }
)
</script>

<template>
  <transition name="fade" mode="out-in">
    <el-container v-if="isLoggedIn && showLayout" class="layout-container" key="authenticated">
    <el-aside width="200px" class="sidebar">
      <div class="logo-container">
        <img src="/logo.svg" alt="Logo" class="sidebar-logo" />
        <h2>Subcycle</h2>
      </div>
      <el-menu
        :default-active="route.path"
        router
        class="el-menu-vertical"
        background-color="var(--bg-sidebar)"
        text-color="var(--text-inverse)"
        active-text-color="var(--menu-active-text)"
      >
        <el-menu-item index="/dashboard">
          <Dashboard/>
          <span>儀表板</span>
        </el-menu-item>
        <el-menu-item index="/calendar">
          <Calendar/>
          <span>付款行事曆</span>
        </el-menu-item>
        <el-menu-item index="/subscriptions">
          <Subscribe/>
          <span>訂閱管理</span>
        </el-menu-item>
        <el-menu-item index="/categories">
          <Class/>
          <span>類別管理</span>
        </el-menu-item>
        <el-menu-item index="/account/settings">
          <UserIcon /> 
          <span>帳戶設定</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-content">
          <div class="header-title">
            <h3>{{ route.meta.title || '' }}</h3>
          </div>
          <div class="header-actions">
            <!-- 預覽數據按鈕（開發用） -->
            <el-button
              v-if="isDev"
              type="success"
              size="small"
              @click="loadPreviewData"
            >
              載入預覽數據
            </el-button>
            <!-- 快速新增訂閱按鈕 -->
            <el-button
              type="primary"
              class="quick-add-btn"
              :icon="Plus"
              @click="handleQuickAdd"
              size="default"
            >
              快速新增
            </el-button>

            <!-- 通知提醒 -->
            <el-popover
              :visible="notificationVisible"
              placement="bottom"
              :width="320"
              trigger="click"
            >
              <template #reference>
                <el-badge :value="notificationCount" :hidden="notificationCount === 0" class="notification-badge">
                  <el-button circle @click="notificationVisible = !notificationVisible">
                    <BellIcon />
                  </el-button>
                </el-badge>
              </template>
              <div class="notification-content">
                <div class="notification-header">
                  <h4>即將扣款的項目</h4>
                  <el-tag size="small" type="warning">{{ notificationCount }} 項</el-tag>
                </div>
                <div class="notification-list">
                  <div v-if="upcomingSubscriptions.length > 0">
                    <div
                      v-for="sub in upcomingSubscriptions"
                      :key="sub.id"
                      class="notification-item"
                      @click="handleNotificationClick(sub)"
                    >
                      <div class="notification-item-info">
                        <div class="notification-item-name">{{ sub.name }}</div>
                        <div class="notification-item-date">{{ formatDate(sub.nextPaymentDate) }}</div>
                      </div>
                      <div class="notification-item-amount">NT$ {{ sub.amount }}</div>
                    </div>
                  </div>
                  <div v-else class="no-notifications">
                    <el-empty description="暫無即將扣款的項目" :image-size="80" />
                  </div>
                </div>
              </div>
            </el-popover>

            <!-- 用戶資訊 -->
            <div class="user-info">
              <UserIcon class="user-info-icon" />
              <span>{{ authStore.user?.name || '用戶' }}</span>
              <el-button @click="handleLogout" type="danger" size="small" :icon="SwitchButton">
                登出
              </el-button>
            </div>
          </div>
        </div>
      </el-header>

      <el-main class="main-content">
        <router-view />
      </el-main>

      <el-footer class="footer" height="60px">
        <div class="footer-content">
          <div class="footer-left">
            <span>&copy; 2025 訂閱追蹤器 Subcycle</span>
          </div>
          <div class="footer-right">
            <a href="#" class="footer-link">隱私政策</a>
            <span class="separator">|</span>
            <a href="#" class="footer-link">服務條款</a>
            <span class="separator">|</span>
            <a href="#" class="footer-link">聯絡我們</a>
          </div>
        </div>
      </el-footer>
    </el-container>
  </el-container>

    <div v-else class="auth-container" key="unauthenticated">
      <router-view />
    </div>
  </transition>
</template>

<style scoped>
/* 淡入淡出過渡動畫 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.layout-container {
  height: 100vh;
}

.sidebar {
  background-color: var(--bg-sidebar);
  color: var(--text-inverse);
  transition: background-color 0.3s ease;

}

.logo-container {
  padding: 15px;
  text-align: left;
  background-color: transparent;
  transition: background-color 0.3s ease;
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 10px;
}

.sidebar-logo {
  width: 32px;
  height: 32px;
  object-fit: contain;
}

.logo-container h2 {
  margin: 0;
  color: var(--text-inverse);
  font-size: 16px;
  font-weight: 600;
}

.el-menu-vertical {
  border: none;
  background-color: var(--bg-sidebar) !important;
}

.el-menu-item i[class^="lni"],
.el-menu-item svg {
  font-size: 18px;
  margin-right: 10px;
  width: 20px;
  text-align: center;
}

.header {
  background-color: var(--bg-primary);
  border-bottom: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  padding: 0 20px;
  transition: background-color 0.3s ease, border-color 0.3s ease;
}

.header-content {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 15px;
}

.header-content h3 {
  margin: 0;
  font-size: 20px;
  color: var(--text-primary);
  transition: color 0.3s ease;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 15px;
}

.notification-badge {
  cursor: pointer;
}

.notification-badge .el-button {
  display: flex;
  align-items: center;
  justify-content: center;
}

.quick-add-btn {
  background-color: #6495ED;
  border-color: #6495ED;
  color: #fff;
}

.quick-add-btn:hover {
  background-color: #4169E1;
  border-color: #4169E1;
}

.notification-content {
  padding: 0;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  border-bottom: 1px solid var(--border-color);
}

.notification-header h4 {
  margin: 0;
  font-size: 16px;
  color: var(--text-primary);
}

.notification-list {
  max-height: 400px;
  overflow-y: auto;
  /* 優化滾動性能 */
  -webkit-overflow-scrolling: touch;
  transform: translateZ(0);
  will-change: scroll-position;
}

.notification-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 15px;
  border-bottom: 1px solid var(--border-color);
  transition: background-color 0.15s ease;
  cursor: pointer;
  /* 啟用 GPU 加速避免殘影 */
  will-change: background-color;
  transform: translateZ(0);
  backface-visibility: hidden;
}

.notification-item:hover {
  background-color: var(--hover-bg);
}

.notification-item:last-child {
  border-bottom: none;
}

.notification-item-info {
  flex: 1;
}

.notification-item-name {
  font-size: 14px;
  color: var(--text-primary);
  margin-bottom: 4px;
  font-weight: 500;
  transition: color 0.3s ease;
}

.notification-item-date {
  font-size: 12px;
  color: var(--text-tertiary);
  transition: color 0.3s ease;
}

.notification-item-amount {
  font-size: 14px;
  font-weight: bold;
  color: var(--accent-blue);
  transition: color 0.3s ease;
}

html.dark .notification-item-amount {
  color: var(--accent-mint);
}

.no-notifications {
  padding: 20px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  padding-left: 15px;
  border-left: 1px solid var(--border-color);
}

.user-info-icon {
  color: var(--text-primary);
  width: 20px;
  height: 20px;
}

.main-content {
  background-color: var(--bg-secondary);
  padding: 20px;
  transition: background-color 0.3s ease;
}

.footer {
  background-color: var(--bg-primary);
  border-top: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  padding: 0 20px;
  transition: background-color 0.3s ease, border-color 0.3s ease;
}

.footer-content {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  color: var(--text-secondary);
  transition: color 0.3s ease;
}

.footer-left {
  display: flex;
  align-items: center;
}

.footer-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.footer-link {
  color: var(--accent-blue);
  text-decoration: none;
  transition: color 0.2s;
}

.footer-link:hover {
  color: var(--accent-mint);
  text-decoration: underline;
}

.separator {
  color: var(--border-light);
}

.auth-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, rgba(141, 234, 195, 0.18) 0%, rgba(91, 141, 239, 0.2) 100%);
}
</style>
