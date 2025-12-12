<template>
  <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
    <!-- 第一行：圆点 + 横线 -->
    <circle cx="4" cy="6" r="1.5" fill="currentColor"/>
    <rect x="8" y="5" width="14" height="2" rx="1" fill="currentColor"/>

    <!-- 第二行：圆点 + 横线 -->
    <circle cx="4" cy="12" r="1.5" fill="currentColor"/>
    <rect x="8" y="11" width="14" height="2" rx="1" fill="currentColor"/>

    <!-- 第三行：圆点 + 横线 -->
    <circle cx="4" cy="18" r="1.5" fill="currentColor"/>
    <rect x="8" y="17" width="14" height="2" rx="1" fill="currentColor"/>
  </svg>
</template>

<script>
export default {
  name: "Layers1",
};
</script>

<style scoped>
svg {
  display: inline-block;
  vertical-align: middle;
  /* 你可以自由調整這裡的 width 和 height 
    來決定圖示的預設大小 
  */
  width: 20px;
  height: 20px;
}
</style>