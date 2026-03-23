<script setup>
import { onMounted, ref } from 'vue'
import router from '@/router'

const appReady = ref(false)

onMounted(async () => {
  await router.isReady()
  appReady.value = true
})
</script>

<template>
  <div v-if="!appReady" class="app-splash">
    <div class="app-splash__glow"></div>
    <div class="app-splash__card">
      <div class="app-splash__logo">工</div>
      <div class="app-splash__title">共享工位管理端</div>
      <div class="app-splash__desc">正在校验登录状态并加载工作台...</div>
    </div>
  </div>
  <router-view v-else v-slot="{ Component }">
    <Suspense>
      <component :is="Component" />
      <template #fallback>
        <div class="route-loading">
          <div class="route-loading__spinner"></div>
          <div class="route-loading__text">页面加载中...</div>
        </div>
      </template>
    </Suspense>
  </router-view>
</template>

<style>
html,
body,
#app {
  height: 100%;
  margin: 0;
}

body {
  background: #0f172a;
}

.app-splash,
.route-loading {
  min-height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background:
    radial-gradient(circle at top, rgba(56, 189, 248, 0.14), transparent 32%),
    linear-gradient(180deg, #0b1220 0%, #0f172a 100%);
  color: #e2e8f0;
}

.app-splash {
  position: relative;
  overflow: hidden;
}

.app-splash__glow {
  position: absolute;
  width: 420px;
  height: 420px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(56, 189, 248, 0.18), transparent 70%);
  filter: blur(4px);
}

.app-splash__card,
.route-loading {
  position: relative;
  z-index: 1;
  flex-direction: column;
  gap: 12px;
  padding: 32px 36px;
  border-radius: 18px;
  border: 1px solid rgba(148, 163, 184, 0.18);
  background: rgba(15, 23, 42, 0.82);
  box-shadow: 0 18px 60px rgba(2, 6, 23, 0.28);
}

.app-splash__logo {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #38bdf8, #0ea5e9);
  color: #04121f;
  font-size: 28px;
  font-weight: 800;
}

.app-splash__title {
  font-size: 18px;
  font-weight: 700;
}

.app-splash__desc,
.route-loading__text {
  font-size: 14px;
  color: #94a3b8;
}

.route-loading__spinner {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: 3px solid rgba(56, 189, 248, 0.18);
  border-top-color: #38bdf8;
  animation: spin 0.9s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
