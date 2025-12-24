<template>
  <div class="home-container">
    <nav class="navbar">
      <div class="nav-brand">
        <img src="/logo.svg" alt="Logo" class="nav-logo" v-if="hasLogo" />
        <span class="brand-text">Subcycle</span>
      </div>
      <div class="nav-links">
        <template v-if="isLoggedIn">
          <router-link to="/dashboard" class="nav-btn primary">進入儀表板</router-link>
        </template>
        <template v-else>
          <router-link to="/login" class="nav-btn text">登入</router-link>
          <router-link to="/register" class="nav-btn primary">免費註冊</router-link>
        </template>
      </div>
    </nav>

    <header class="hero-section">
      <div class="hero-content">
        <h1 class="hero-title">輕鬆管理您的所有訂閱服務</h1>
        <p class="hero-subtitle">
          不再忘記付款，不再浪費金錢。Subcycle 幫助您追蹤、分析並優化您的定期支出。
        </p>
        <div class="hero-actions">
          <router-link to="/register" class="cta-button">立即開始使用</router-link>
          <a href="#features" class="secondary-button">了解更多</a>
        </div>
      </div>
      <div class="hero-image-container">
        <img
          src="/hero.jpg"
          alt="Financial Dashboard"
          class="hero-image"
        />
      </div>
    </header>

    <section id="features" class="features-section">
      <h2 class="section-title">為什麼選擇 Subcycle？</h2>
      <div class="features-grid">
        <div class="feature-card">
          <div class="feature-icon">
            <ChartIcon />
          </div>
          <h3>視覺化儀表板</h3>
          <p>一目了然地查看您的每月支出和訂閱分佈。</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon">
            <BellIconFeature />
          </div>
          <h3>付款提醒</h3>
          <p>在付款日前收到通知，避免意外扣款。</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon">
            <MoneyIcon />
          </div>
          <h3>支出分析</h3>
          <p>深入了解您的消費習慣，找出省錢的機會。</p>
        </div>
      </div>
    </section>

    <footer class="landing-footer">
      <p>&copy; 2025 訂閱追蹤器 Subcycle</p>
    </footer>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { useAuthStore } from '../stores/auth'
import ChartIcon from '../components/icons/ChartIcon.vue'
import BellIconFeature from '../components/icons/BellIconFeature.vue'
import MoneyIcon from '../components/icons/MoneyIcon.vue'

const authStore = useAuthStore()
const isLoggedIn = computed(() => !!authStore.token)
const hasLogo = ref(false)

onMounted(() => {
  // Check if logo exists (optional, just to be safe since we deleted it previously)
  const img = new Image()
  img.onload = () => { hasLogo.value = true }
  img.onerror = () => { hasLogo.value = false }
  img.src = '/logo.svg'
})
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  background-color: var(--bg-secondary);
  font-family: 'Inter', sans-serif;
}

.navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 40px;
  background-color: white;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
  position: sticky;
  top: 0;
  z-index: 100;
}

.nav-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
}

.nav-logo {
  width: 50px;
  height: 50px;
}

.nav-links {
  display: flex;
  gap: 20px;
  align-items: center;
}

.nav-btn {
  text-decoration: none;
  font-weight: 500;
  transition: all 0.3s;
}

.nav-btn.text {
  color: var(--text-secondary);
}

.nav-btn.text:hover {
  color: var(--accent-blue);
}

.nav-btn.primary {
  background: linear-gradient(120deg, var(--accent-blue), var(--accent-mint));
  color: white;
  padding: 8px 20px;
  border-radius: 20px;
}

.nav-btn.primary:hover {
  background: linear-gradient(120deg, #79a4ff, var(--accent-mint));
  transform: translateY(-1px);
}

.hero-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 80px 10%;
  gap: 60px;
  background: linear-gradient(135deg, #e8f8f2 0%, #e4ecff 100%);
  min-height: 80vh;
}

.hero-content {
  flex: 1;
  max-width: 600px;
}

.hero-title {
  font-size: 48px;
  line-height: 1.2;
  margin-bottom: 20px;
  color: var(--text-primary);
  font-weight: 800;
}

.hero-subtitle {
  font-size: 18px;
  line-height: 1.6;
  color: var(--text-secondary);
  margin-bottom: 40px;
}

.hero-actions {
  display: flex;
  gap: 20px;
}

.cta-button {
  background: linear-gradient(120deg, var(--accent-blue), var(--accent-mint));
  color: white;
  padding: 12px 30px;
  border-radius: 30px;
  text-decoration: none;
  font-weight: 600;
  font-size: 18px;
  transition: all 0.3s;
  box-shadow: 0 4px 15px rgba(91, 141, 239, 0.3);
}

.cta-button:hover {
  background: linear-gradient(120deg, #79a4ff, var(--accent-mint));
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(91, 141, 239, 0.4);
}

.secondary-button {
  padding: 12px 30px;
  border-radius: 30px;
  text-decoration: none;
  font-weight: 600;
  font-size: 18px;
  color: var(--text-secondary);
  background-color: white;
  border: 1px solid var(--border-color);
  transition: all 0.3s;
}

.secondary-button:hover {
  border-color: var(--accent-blue);
  color: var(--accent-blue);
}

.hero-image-container {
  flex: 1;
  display: flex;
  justify-content: center;
}

.hero-image {
  max-width: 100%;
  border-radius: 20px;
  box-shadow: 0 20px 40px rgba(0,0,0,0.15);
  transform: perspective(1000px) rotateY(-5deg);
  transition: transform 0.5s;
}

.hero-image:hover {
  transform: perspective(1000px) rotateY(0deg);
}

.features-section {
  padding: 80px 10%;
  background-color: white;
}

.section-title {
  text-align: center;
  font-size: 32px;
  margin-bottom: 60px;
  color: #2c3e50;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 40px;
}

.feature-card {
  padding: 40px;
  border-radius: 16px;
  background-color: var(--bg-secondary);
  text-align: center;
  transition: all 0.3s;
}

.feature-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 30px rgba(0,0,0,0.05);
}

.feature-icon {
  margin-bottom: 20px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.feature-card h3 {
  margin-bottom: 15px;
  color: var(--text-primary);
}

.feature-card p {
  color: var(--text-secondary);
  line-height: 1.6;
}

.landing-footer {
  padding: 40px;
  text-align: center;
  background-color: var(--accent-charcoal);
  color: var(--text-tertiary);
}

@media (max-width: 768px) {
  .hero-section {
    flex-direction: column;
    padding: 40px 20px;
    text-align: center;
  }

  .hero-content {
    max-width: 100%;
  }

  .hero-actions {
    justify-content: center;
  }

  .hero-image {
    transform: none;
  }
  
  .navbar {
    padding: 20px;
  }
}
</style>
