<template>
  <svg viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg">
    <!-- 信用卡图标 -->
    <!-- 卡片外框 -->
    <rect x="8" y="14" width="32" height="20" rx="2" stroke="currentColor" stroke-width="2.5" fill="none"/>
    <!-- 磁条 -->
    <rect x="8" y="18" width="32" height="4" fill="currentColor"/>
    <!-- 芯片 -->
    <rect x="11" y="25" width="5" height="4" rx="0.5" stroke="currentColor" stroke-width="1.5" fill="none"/>
    <!-- 卡号线条 -->
    <line x1="18" y1="27" x2="28" y2="27" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
    <line x1="18" y1="30" x2="24" y2="30" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
  </svg>
</template>

<script>
export default {
  name: "MoneyIcon",
};
</script>

<style scoped>
svg {
  display: inline-block;
  vertical-align: middle;
  width: 64px;
  height: 64px;
  color: #FFB84D;
}
</style>
