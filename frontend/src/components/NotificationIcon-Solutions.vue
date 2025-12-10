<template>
  <div class="notification-examples">
    <h2>通知圖示 - 去除白邊解決方案</h2>

    <!-- 方案 1：使用 CSS 強制移除白邊 -->
    <div class="solution">
      <h3>✅ 方案 1：CSS 強制移除（最簡單）</h3>
      <div class="demo-row">
        <img :src="iconUrl" class="icon-fix-1" alt="通知">
        <span>白色背景</span>
      </div>
      <div class="demo-row bg-gray">
        <img :src="iconUrl" class="icon-fix-1" alt="通知">
        <span>灰色背景</span>
      </div>
      <div class="demo-row bg-dark">
        <img :src="iconUrl" class="icon-fix-1" alt="通知">
        <span>深色背景</span>
      </div>
    </div>

    <!-- 方案 2：使用 background-image -->
    <div class="solution">
      <h3>✅ 方案 2：background-image（推薦）</h3>
      <div class="demo-row">
        <div class="icon-bg" :style="{ backgroundImage: `url(${iconUrl})` }"></div>
        <span>使用背景圖</span>
      </div>
    </div>

    <!-- 方案 3：包在容器內 -->
    <div class="solution">
      <h3>✅ 方案 3：容器包裹</h3>
      <div class="demo-row">
        <div class="icon-wrapper">
          <img :src="iconUrl" alt="通知">
        </div>
        <span>使用容器包裹</span>
      </div>
    </div>

    <!-- 方案 4：使用 object-fit -->
    <div class="solution">
      <h3>✅ 方案 4：object-fit</h3>
      <div class="demo-row">
        <img :src="iconUrl" class="icon-fit" alt="通知">
        <span>使用 object-fit</span>
      </div>
    </div>

    <!-- 方案 5：Element Plus Icon（如果可用）-->
    <div class="solution">
      <h3>✅ 方案 5：使用 Element Plus 圖示</h3>
      <div class="demo-row">
        <el-icon :size="40" color="#d4a03a">
          <Bell />
        </el-icon>
        <span>使用內建圖示</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
// import { Bell } from '@element-plus/icons-vue'  // 如果使用 Element Plus

const iconUrl = ref('./assets/login-bg.png')  // 替換成你的圖片路徑

// 如果要從外部傳入
const props = defineProps({
  src: {
    type: String,
    default: ''
  },
  size: {
    type: Number,
    default: 50
  }
})
</script>

<style scoped>
.notification-examples {
  padding: 20px;
}

.solution {
  margin-bottom: 30px;
  padding: 20px;
  border: 1px solid #ddd;
  border-radius: 8px;
}

.demo-row {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px;
  margin: 10px 0;
  background: white;
  border-radius: 4px;
}

.demo-row.bg-gray {
  background: #f5f5f5;
}

.demo-row.bg-dark {
  background: #2c3e50;
  color: white;
}

/* ========================================
   方案 1：CSS 強制移除白邊
   ======================================== */
.icon-fix-1 {
  width: 50px;
  height: auto;
  display: block !important;
  margin: 0 !important;
  padding: 0 !important;
  border: none !important;
  vertical-align: middle !important;
  line-height: 0;
}

/* ========================================
   方案 2：使用 background-image
   ======================================== */
.icon-bg {
  width: 50px;
  height: 60px;
  background-size: contain;
  background-repeat: no-repeat;
  background-position: center;
  /* backgroundImage 會從 :style 綁定 */
}

/* ========================================
   方案 3：容器包裹
   ======================================== */
.icon-wrapper {
  display: inline-block;
  line-height: 0;
  font-size: 0;
  overflow: hidden;
}

.icon-wrapper img {
  display: block;
  width: 50px;
  height: auto;
}

/* ========================================
   方案 4：使用 object-fit
   ======================================== */
.icon-fit {
  width: 50px;
  height: 60px;
  display: block;
  object-fit: contain;
  object-position: center;
}
</style>
