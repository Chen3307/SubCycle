<template>
  <div class="home-container">
    <nav class="navbar">
      <div class="nav-brand">
        <img src="/logo.svg" alt="Logo" class="nav-logo" v-if="hasLogo" />
        <span class="brand-text">SubCycle</span>
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
          不再忘記付款，不再浪費金錢。SubCycle 幫助您追蹤、分析並優化您的定期支出。
        </p>
        <div class="hero-actions">
          <router-link to="/register" class="cta-button">立即開始使用</router-link>
          <a href="#features" class="secondary-button">了解更多</a>
        </div>
      </div>
      <div class="hero-image-container">
        <img 
          src="https://images.unsplash.com/photo-1554224155-8d04cb21cd6c?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=1000&q=80" 
          alt="Financial Dashboard" 
          class="hero-image"
        />
      </div>
    </header>

    <section id="features" class="features-section">
      <h2 class="section-title">為什麼選擇 SubCycle？</h2>
      <div class="features-grid">
        <div class="feature-card">
          <div class="feature-icon">📊</div>
          <h3>視覺化儀表板</h3>
          <p>一目了然地查看您的每月支出和訂閱分佈。</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon">🔔</div>
          <h3>付款提醒</h3>
          <p>在付款日前收到通知，避免意外扣款。</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon">💰</div>
          <h3>支出分析</h3>
          <p>深入了解您的消費習慣，找出省錢的機會。</p>
        </div>
      </div>
    </section>

    <footer class="landing-footer">
      <p>&copy; 2025 訂閱追蹤器 SubCycle. All rights reserved.</p>
    </footer>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { useAuthStore } from '../stores/auth'

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
  background-color: #f8f9fa;
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
  color: #2c3e50;
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
  color: #606266;
}

.nav-btn.text:hover {
  color: #409eff;
}

.nav-btn.primary {
  background-color: #409eff;
  color: white;
  padding: 8px 20px;
  border-radius: 20px;
}

.nav-btn.primary:hover {
  background-color: #66b1ff;
  transform: translateY(-1px);
}

.hero-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 80px 10%;
  gap: 60px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
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
  color: #2c3e50;
  font-weight: 800;
}

.hero-subtitle {
  font-size: 18px;
  line-height: 1.6;
  color: #5e6d82;
  margin-bottom: 40px;
}

.hero-actions {
  display: flex;
  gap: 20px;
}

.cta-button {
  background-color: #409eff;
  color: white;
  padding: 12px 30px;
  border-radius: 30px;
  text-decoration: none;
  font-weight: 600;
  font-size: 18px;
  transition: all 0.3s;
  box-shadow: 0 4px 15px rgba(64, 158, 255, 0.3);
}

.cta-button:hover {
  background-color: #66b1ff;
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(64, 158, 255, 0.4);
}

.secondary-button {
  padding: 12px 30px;
  border-radius: 30px;
  text-decoration: none;
  font-weight: 600;
  font-size: 18px;
  color: #606266;
  background-color: white;
  border: 1px solid #dcdfe6;
  transition: all 0.3s;
}

.secondary-button:hover {
  border-color: #409eff;
  color: #409eff;
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
  background-color: #f8f9fa;
  text-align: center;
  transition: all 0.3s;
}

.feature-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 30px rgba(0,0,0,0.05);
}

.feature-icon {
  font-size: 48px;
  margin-bottom: 20px;
}

.feature-card h3 {
  margin-bottom: 15px;
  color: #2c3e50;
}

.feature-card p {
  color: #5e6d82;
  line-height: 1.6;
}

.landing-footer {
  padding: 40px;
  text-align: center;
  background-color: #2c3e50;
  color: #909399;
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
