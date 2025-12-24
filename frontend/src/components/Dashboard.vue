<template>
  <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
    <!-- 极简风格：简单的方块网格 -->
    <rect x="3" y="3" width="8" height="8" rx="2" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
    <rect x="13" y="3" width="8" height="8" rx="2" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
    <rect x="3" y="13" width="8" height="8" rx="2" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
    <rect x="13" y="13" width="8" height="8" rx="2" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
  </svg>
</template>

<script>
export default {
  name: "DashboardSquare1",
};
</script>

<style scoped>
svg {
  display: inline-block;
  vertical-align: middle;
  /* 預設尺寸 (可自行調整) */
  width: 20px;
  height: 20px;
}
</style>
