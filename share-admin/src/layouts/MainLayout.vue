<script setup>
import { computed, ref, watch } from 'vue'
import { storeToRefs } from 'pinia'
import { useRoute, useRouter } from 'vue-router'
import { useTabsStore } from '@/stores/tabs'
import {
  Calendar,
  DataAnalysis,
  Expand,
  Fold,
  FullScreen,
  OfficeBuilding,
  Place,
  Setting,
  User,
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const tabsStore = useTabsStore()
const { activePath, list: tabList } = storeToRefs(tabsStore)

const collapsed = ref(false)
const asideWidth = computed(() => (collapsed.value ? '64px' : '220px'))

const activeMenu = computed(() => (route.path === '/' ? '/dashboard' : route.path))

watch(
  () => route.fullPath,
  () => {
    tabsStore.syncRoute(route)
  },
  { immediate: true },
)

function onTabClick(tab) {
  const p = tab.paneName
  if (p && p !== route.path) router.push(p)
}

function onTabRemove(name) {
  const next = tabsStore.remove(name)
  if (next) router.push(next)
}

function toggleCollapse() {
  collapsed.value = !collapsed.value
}

function toggleFullscreen() {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen?.()
  } else {
    document.exitFullscreen?.()
  }
}

function logout() {
  localStorage.removeItem('adminToken')
  localStorage.removeItem('adminUser')
  router.push('/login')
}

const adminName = computed(() => {
  try {
    const u = JSON.parse(localStorage.getItem('adminUser') || '{}')
    return u.realName || u.phone || '管理员'
  } catch {
    return '管理员'
  }
})
</script>

<template>
  <el-container class="layout-root">
    <el-aside :width="asideWidth" class="aside">
      <div class="logo">
        <span v-show="!collapsed" class="logo-text">共享工位系统</span>
        <span v-show="collapsed" class="logo-mini">工</span>
      </div>
      <el-scrollbar class="menu-scroll">
        <el-menu
          :default-active="activeMenu"
          :collapse="collapsed"
          :collapse-transition="false"
          router
          background-color="transparent"
          text-color="#94a3b8"
          active-text-color="#ffffff"
        >
          <el-sub-menu index="base">
            <template #title>
              <el-icon><OfficeBuilding /></el-icon>
              <span>基础数据</span>
            </template>
            <el-menu-item index="/spaces">空间层级</el-menu-item>
            <el-menu-item index="/facilities">设施标签</el-menu-item>
            <el-menu-item index="/depts">部门</el-menu-item>
          </el-sub-menu>
          <el-menu-item index="/workstations">
            <el-icon><Place /></el-icon>
            <template #title>工位管理</template>
          </el-menu-item>
          <el-sub-menu index="order">
            <template #title>
              <el-icon><Calendar /></el-icon>
              <span>预约与订单</span>
            </template>
            <el-menu-item index="/orders">预约看板</el-menu-item>
            <el-menu-item index="/breaches">异常订单</el-menu-item>
          </el-sub-menu>
          <el-menu-item index="/users">
            <el-icon><User /></el-icon>
            <template #title>用户管理</template>
          </el-menu-item>
          <el-menu-item index="/dashboard">
            <el-icon><DataAnalysis /></el-icon>
            <template #title>数据看板</template>
          </el-menu-item>
          <el-menu-item index="/config">
            <el-icon><Setting /></el-icon>
            <template #title>系统设置</template>
          </el-menu-item>
        </el-menu>
      </el-scrollbar>
    </el-aside>
    <el-container class="main-wrap">
      <el-header class="header">
        <div class="header-left">
          <el-button class="icon-btn" text @click="toggleCollapse">
            <el-icon :size="20"><Fold v-if="!collapsed" /><Expand v-else /></el-icon>
          </el-button>
          <span class="sys-title">基于 Java 的共享工位管理系统</span>
        </div>
        <div class="header-right">
          <el-button class="icon-btn" text @click="toggleFullscreen">
            <el-icon :size="20"><FullScreen /></el-icon>
          </el-button>
          <el-dropdown trigger="click" @command="(c) => c === 'out' && logout()">
            <span class="user-trigger">
              <el-icon><User /></el-icon>
              {{ adminName }}
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="out">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <div class="tabs-bar">
        <el-tabs
          v-model="activePath"
          type="card"
          class="route-tabs"
          @tab-click="onTabClick"
          @tab-remove="onTabRemove"
        >
          <el-tab-pane
            v-for="t in tabList"
            :key="t.path"
            :label="t.title"
            :name="t.path"
            :closable="t.closable"
          />
        </el-tabs>
      </div>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.layout-root {
  height: 100%;
  background: var(--admin-bg-deep, #0f172a);
}
.aside {
  background: linear-gradient(180deg, #0f172a 0%, #111c33 100%);
  border-right: 1px solid var(--admin-border, #334155);
  display: flex;
  flex-direction: column;
  transition: width 0.2s ease;
}
.logo {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid var(--admin-border, #334155);
  flex-shrink: 0;
}
.logo-text {
  font-weight: 700;
  font-size: 15px;
  color: #f1f5f9;
  letter-spacing: 0.5px;
}
.logo-mini {
  font-weight: 800;
  font-size: 18px;
  color: #38bdf8;
}
.menu-scroll {
  flex: 1;
  min-height: 0;
}
.aside :deep(.el-menu) {
  border-right: none;
}
.aside :deep(.el-sub-menu__title:hover),
.aside :deep(.el-menu-item:hover) {
  background: rgba(56, 189, 248, 0.08) !important;
}
.aside :deep(.el-menu-item.is-active) {
  background: linear-gradient(90deg, rgba(56, 189, 248, 0.25), transparent) !important;
  border-left: 3px solid #38bdf8;
}
.main-wrap {
  flex-direction: column;
  background: var(--admin-bg-deep, #0f172a);
}
.header {
  height: 56px !important;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  background: var(--admin-bg-panel, #1e293b);
  border-bottom: 1px solid var(--admin-border, #334155);
}
.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}
.icon-btn {
  color: #94a3b8;
}
.sys-title {
  color: #e2e8f0;
  font-size: 15px;
  font-weight: 600;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 4px;
}
.user-trigger {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin-left: 12px;
  padding: 6px 12px;
  border-radius: 8px;
  cursor: pointer;
  color: #cbd5e1;
  font-size: 14px;
}
.user-trigger:hover {
  background: rgba(255, 255, 255, 0.06);
}
.tabs-bar {
  background: var(--admin-bg-panel, #1e293b);
  padding: 0 12px;
  border-bottom: 1px solid var(--admin-border, #334155);
}
.route-tabs :deep(.el-tabs__header) {
  margin: 0;
  border: none;
}
.route-tabs :deep(.el-tabs__nav) {
  border: none !important;
}
.route-tabs :deep(.el-tabs__item) {
  border: 1px solid var(--admin-border, #334155) !important;
  background: var(--admin-bg-elevated, #243047);
  color: #94a3b8;
  margin-right: 4px;
  border-radius: 6px 6px 0 0;
  height: 36px;
}
.route-tabs :deep(.el-tabs__item.is-active) {
  background: var(--admin-bg-deep, #0f172a);
  color: #38bdf8;
  border-bottom-color: transparent !important;
}
.main {
  background: var(--admin-bg-deep, #0f172a);
  padding: 16px;
  overflow: auto;
}
</style>
