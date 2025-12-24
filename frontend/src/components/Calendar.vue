<template>
  <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
    <!-- 极简日历 -->
    <rect x="4" y="5" width="16" height="16" rx="2" stroke="currentColor" stroke-width="1.5"/>
    <line x1="4" y1="9" x2="20" y2="9" stroke="currentColor" stroke-width="1.5"/>
    <line x1="8" y1="3" x2="8" y2="7" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
    <line x1="16" y1="3" x2="16" y2="7" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
  </svg>
</template>

<script>
export default {
  name: "CalendarDays",
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
