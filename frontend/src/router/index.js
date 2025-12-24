import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'Home',
      component: () => import('../views/HomeView.vue')
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import('../views/LoginView.vue')
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('../views/RegisterView.vue')
    },
    {
      path: '/verify-email',
      name: 'VerifyEmail',
      component: () => import('../views/VerifyEmailView.vue')
    },
    {
      path: '/dashboard',
      name: 'Dashboard',
      component: () => import('../views/DashboardView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/calendar',
      name: 'Calendar',
      component: () => import('../views/CalendarView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/subscriptions',
      name: 'Subscriptions',
      component: () => import('../views/SubscriptionsView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/categories',
      name: 'Categories',
      component: () => import('../views/CategoriesView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/account/settings',
      name: 'AccountSettings',
      component: () => import('../views/AccountSettingsView.vue'),
      meta: { requiresAuth: true, title: '帳戶設定' },
      redirect: '/account/settings/profile',
      children: [
        {
          path: 'profile',
          name: 'AccountSettingsProfile',
          component: () => import('../views/account/ProfileSettingsView.vue'),
          meta: { requiresAuth: true, title: '個人資訊' }
        },
        {
          path: 'password',
          name: 'AccountSettingsPassword',
          component: () => import('../views/account/PasswordSettingsView.vue'),
          meta: { requiresAuth: true, title: '修改密碼' }
        },
        {
          path: 'notification',
          name: 'AccountSettingsNotification',
          component: () => import('../views/account/NotificationSettingsView.vue'),
          meta: { requiresAuth: true, title: '通知設定' }
        },
        {
          path: 'preferences',
          name: 'AccountSettingsPreferences',
          component: () => import('../views/account/PreferencesSettingsView.vue'),
          meta: { requiresAuth: true, title: '偏好設定' }
        }
      ]
    },
    // 後台管理路由
    {
      path: '/admin/users',
      name: 'AdminUsers',
      component: () => import('../views/admin/UsersManagementView.vue'),
      meta: { requiresAuth: true, requiresAdmin: true, title: '用戶管理' }
    }
  ]
})

// 路由守衛 - 檢查登入狀態和管理者權限
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const userStr = localStorage.getItem('user')
  const user = userStr ? JSON.parse(userStr) : null

  // 需要登入但沒有 token
  if (to.meta.requiresAuth && !token) {
    return next('/login')
  }

  // 需要管理者權限
  if (to.meta.requiresAdmin) {
    if (!token) {
      return next('/login')
    }
    if (user?.role !== 'ADMIN') {
      // 非管理者重導向到儀表板
      return next('/dashboard')
    }
  }

  // 已登入用戶訪問登入/註冊頁，重導向到儀表板
  if ((to.path === '/login' || to.path === '/register') && token) {
    return next('/dashboard')
  }

  next()
})

export default router
