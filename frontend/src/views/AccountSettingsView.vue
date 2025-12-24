<template>
  <div class="account-settings">
    <h1 class="page-title">帳戶設定</h1>

    <div class="settings-layout">
      <section class="settings-content">
        <router-view />
      </section>
    </div>
  </div>
</template>

<script setup>
</script>

<style scoped>
.account-settings {
  max-width: 1400px;
  margin: 0 auto;
  padding-bottom: 20px;
}

.page-title {
  margin: 0 0 24px 0;
  font-size: 28px;
  color: var(--text-primary);
  transition: color 0.3s ease;
}

.settings-layout {
  display: flex;
  gap: 20px;
  align-items: flex-start;
  width: 100%;
}

.settings-content {
  min-width: 0;
  flex: 1 1 auto;
}

@media (max-width: 960px) {
  .settings-layout {
    flex-direction: column;
  }
}
</style>
