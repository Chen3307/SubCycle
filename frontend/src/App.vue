<script setup>
import { computed, ref, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from './stores/auth'
import { useSubscriptionStore } from './stores/subscription'
import { useCategoryStore } from './stores/category'
import { useThemeStore } from './stores/theme'
import {
  SwitchButton
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import UserOutlineIcon from './components/icons/UserOutlineIcon.vue'
import BellIcon from './components/icons/BellIcon.vue'
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

const isLoggedIn = computed(() => !!authStore.token)
const isAdmin = computed(() => authStore.user?.role === 'ADMIN')
const showLayout = computed(() => {
  return route.path !== '/login' && route.path !== '/register'
})
const isDarkThemeActive = computed(() => themeStore.darkMode && isLoggedIn.value && showLayout.value)

// 通知相關
const notificationVisible = ref(false)
const upcomingSubscriptions = computed(() => subscriptionStore.unreadUpcomingSubscriptions)
const notificationCount = computed(() => upcomingSubscriptions.value.length)

const handleLogout = async () => {
  // 先清理状态
  authStore.logout()
  subscriptionStore.clear()
  categoryStore.clear()

  // 使用 nextTick 确保 DOM 更新完成后再跳转
  await router.push('/')
}

const handleNotificationClick = (subscription) => {
  subscriptionStore.markNotificationAsRead(subscription.id)
  router.push({
    path: '/calendar',
    query: { subscriptionId: String(subscription.id) }
  })
}

const formatDate = (date) => {
  const d = dayjs(date).startOf('day')
  const today = dayjs().startOf('day')
  const diff = d.diff(today, 'day')

  if (diff === 0) return '今天'
  if (diff === 1) return '明天'
  if (diff === 2) return '後天'

  return dayjs(date).format('MM/DD (ddd)')
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

watch(
  isDarkThemeActive,
  (active) => {
    document.body.classList.toggle('dark-theme', active)
  },
  { immediate: true }
)
</script>

<template>
  <transition name="fade" mode="out-in">
    <el-container v-if="isLoggedIn && showLayout" class="layout-container" :class="{ 'dark-theme': isDarkThemeActive }" key="authenticated">
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
        <el-sub-menu index="/account/settings">
          <template #title>
            <UserOutlineIcon />
            <span>帳戶設定</span>
          </template>
          <el-menu-item index="/account/settings/profile">
            <span>個人資訊</span>
          </el-menu-item>
          <el-menu-item index="/account/settings/password">
            <span>修改密碼</span>
          </el-menu-item>
          <el-menu-item index="/account/settings/notification">
            <span>通知設定</span>
          </el-menu-item>
          <el-menu-item index="/account/settings/preferences">
            <span>偏好設定</span>
          </el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="/admin" v-if="isAdmin">
          <template #title>
            <UserOutlineIcon />
            <span>管理者</span>
          </template>
          <el-menu-item index="/admin/users">
            <span>用戶管理</span>
          </el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-content">
          <div class="header-title"></div>
          <div class="header-actions">
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
                    <el-empty description="暫無扣款通知" :image-size="80" />
                  </div>
                </div>
              </div>
            </el-popover>

            <!-- 用戶資訊 -->
            <div class="user-info">
              <UserOutlineIcon class="user-info-icon" />
              <span class="user-name">{{ authStore.user?.name || '用戶' }}</span>
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

.el-menu-vertical :deep(.el-sub-menu__title) {
  display: flex;
  align-items: center;
}

.el-menu-vertical :deep(.el-sub-menu__title svg) {
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
  height: 63px;
}

.header-content {
  width: 100%;
  height: 100%;
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
  height: 100%;
}

.header-actions > * {
  display: flex;
  align-items: center;
}

.notification-badge {
  cursor: pointer;
  display: inline-flex;
  align-items: center;
}

.notification-badge .el-button {
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0;
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

:global(.dark-theme) .notification-item-amount {
  color: var(--accent-mint);
}

:global(.dark-theme) .notification-badge .el-button {
  background-color: #0f171f;
  border-color: #2f3d48;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.35);
  color: #1f2a33;
}

:global(.notification-badge .el-button) {
  background-color: var(--bg-secondary);
  border-color: var(--border-color);
  color: var(--text-primary);
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
  height: 100%;
}

.user-name {
  color: var(--accent-mint);
  font-weight: 600;
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
