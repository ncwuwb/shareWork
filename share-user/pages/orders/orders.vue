<template>
  <view class="sw-page">
    <view class="sw-shell">
      <view class="sw-hero">
        <view class="sw-metric-grid">
          <view class="sw-metric-card">
            <text class="sw-metric-value">{{ orderStats.pending }}</text>
            <text class="sw-metric-label">待签到</text>
          </view>
          <view class="sw-metric-card">
            <text class="sw-metric-value">{{ orderStats.active }}</text>
            <text class="sw-metric-label">使用中</text>
          </view>
          <view class="sw-metric-card">
            <text class="sw-metric-value">{{ orderStats.done }}</text>
            <text class="sw-metric-label">已完成</text>
          </view>
        </view>
      </view>

      <view v-if="focusOrder" class="sw-card">
        <view class="sw-section-head">
          <view>
            <text class="sw-section-title">当前最重要的预约</text>
            <text class="sw-section-subtitle">{{ formatRange(focusOrder.startTime, focusOrder.endTime) }}</text>
          </view>
          <text :class="['sw-pill', `sw-pill--${getOrderStatusMeta(focusOrder.status).tone}`]">
            {{ getOrderStatusMeta(focusOrder.status).label }}
          </text>
        </view>
        <text class="focus-order-text">{{ focusOrderHint }}</text>
      </view>

      <view class="sw-card">
        <view class="sw-section-head">
          <text class="sw-section-title">订单筛选</text>
          <button class="sw-secondary-button reset-button" @click="resetFilter">重置</button>
        </view>

        <view class="order-filter-row">
          <text class="order-filter-label">订单状态</text>
          <view class="order-filter-picker-wrap">
            <picker
              mode="selector"
              :range="ORDER_TABS"
              range-key="label"
              :value="tabIndex"
              @change="onTabPick"
            >
              <view class="sw-picker order-status-picker">{{ currentTabLabel }}</view>
            </picker>
          </view>
        </view>
      </view>

      <view v-if="loading" class="sw-empty">正在同步你的预约记录...</view>
      <view v-else-if="!filteredOrders.length" class="sw-empty">这个状态下暂时没有预约记录。</view>

      <view v-else class="order-list">
        <view v-for="order in filteredOrders" :key="order.id" class="order-card">
          <view class="order-top">
            <view>
              <text class="order-no">{{ order.orderNo || `订单 #${order.id}` }}</text>
              <text class="order-sub">{{ formatRange(order.startTime, order.endTime) }}</text>
            </view>
            <text :class="['sw-pill', `sw-pill--${getOrderStatusMeta(order.status).tone}`]">
              {{ getOrderStatusMeta(order.status).label }}
            </text>
          </view>

          <view class="meta-grid">
            <view class="meta-item">
              <text class="meta-label">工位编号</text>
              <text class="meta-value">{{ order.workstationCode || `#${order.workstationId}` }}</text>
            </view>
            <view class="meta-item">
              <text class="meta-label">签到时间</text>
              <text class="meta-value">{{ formatDateTime(order.signTime) }}</text>
            </view>
            <view class="meta-item">
              <text class="meta-label">创建时间</text>
              <text class="meta-value">{{ formatDateTime(order.createTime) }}</text>
            </view>
          </view>
          <view v-if="order.spacePath" class="space-path-row">
            <text class="space-path-label">区域路径</text>
            <text class="space-path-value">{{ order.spacePath }}</text>
          </view>

          <view class="timeline">
            <view class="timeline-node timeline-node--done"></view>
            <view class="timeline-line" :class="{ 'timeline-line--done': order.status >= 1 }"></view>
            <view :class="['timeline-node', order.status >= 1 ? 'timeline-node--done' : 'timeline-node--idle']"></view>
            <view class="timeline-line" :class="{ 'timeline-line--done': order.status >= 2 }"></view>
            <view :class="['timeline-node', order.status >= 2 ? 'timeline-node--done' : 'timeline-node--idle']"></view>
          </view>

          <view class="timeline-labels">
            <text>预约成功</text>
            <text>完成签到</text>
            <text>结束使用</text>
          </view>

          <text class="order-note">{{ getOrderStatusMeta(order.status).description }}</text>

          <view class="actions">
            <button
              v-if="order.status === 0"
              class="sw-secondary-button action-button"
              @click="cancelOrder(order)"
            >
              取消预约
            </button>
            <button
              v-if="order.status === 0"
              class="sw-primary-button action-button"
              @click="signOrder(order)"
            >
              立即签到
            </button>
            <button
              v-if="order.status === 1"
              class="sw-primary-button action-button"
              @click="finishOrder(order)"
            >
              结束使用
            </button>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import {
  ORDER_TABS,
  ensureLogin,
  formatDateTime,
  formatRange,
  getOrderStatusMeta,
  showError,
  toDate,
} from '@/utils/app.js'
import { SIGN_USE_AREA_CENTER } from '@/utils/config.js'
import { request } from '@/utils/request.js'

const loading = ref(false)
const currentTab = ref('all')
const orders = ref([])

const tabIndex = computed(() => {
  const i = ORDER_TABS.findIndex((t) => t.value === currentTab.value)
  return i >= 0 ? i : 0
})

const currentTabLabel = computed(() => {
  const tab = ORDER_TABS.find((t) => t.value === currentTab.value)
  return tab ? tab.label : '全部'
})

const filteredOrders = computed(() => {
  if (currentTab.value === 'all') {
    return orders.value
  }
  return orders.value.filter((item) => item.status === currentTab.value)
})

const orderStats = computed(() => {
  return orders.value.reduce(
    (summary, order) => {
      if (order.status === 0) summary.pending += 1
      if (order.status === 1) summary.active += 1
      if (order.status === 2) summary.done += 1
      return summary
    },
    { pending: 0, active: 0, done: 0 },
  )
})

const focusOrder = computed(() => {
  const sorted = [...orders.value].sort((first, second) => {
    const firstTime = toDate(first.startTime)
    const secondTime = toDate(second.startTime)
    return (firstTime ? firstTime.getTime() : 0) - (secondTime ? secondTime.getTime() : 0)
  })
  return sorted.find((item) => item.status === 0 || item.status === 1) || null
})

const focusOrderHint = computed(() => {
  if (!focusOrder.value) return ''
  if (focusOrder.value.status === 0) {
    return '到达办公区后记得完成签到；若行程有变，开始前 1 小时以外取消不会扣分。'
  }
  return '你已经处于使用中，离开时记得主动结束，以便尽快释放工位。'
})

onShow(() => {
  if (!ensureLogin()) {
    return
  }
  loadOrders()
})

async function loadOrders() {
  loading.value = true
  try {
    const response = await request({ url: '/api/app/orders?page=1&size=100' })
    orders.value = (response.data && response.data.records) || []
  } catch (error) {
    showError(error, '预约列表加载失败')
  } finally {
    loading.value = false
  }
}

function onTabPick(e) {
  const idx = Number(e.detail.value)
  const tab = ORDER_TABS[idx]
  if (tab) {
    currentTab.value = tab.value
  }
}

function resetFilter() {
  currentTab.value = 'all'
  loadOrders()
}

async function cancelOrder(order) {
  try {
    await request({
      url: `/api/app/orders/${order.id}/cancel?confirmLateCancel=false`,
      method: 'POST',
    })
    uni.showToast({ title: '预约已取消', icon: 'success' })
    loadOrders()
  } catch (error) {
    if (error && error.code === 1001) {
      uni.showModal({
        title: '取消提醒',
        content: error.message || '当前取消将扣分，是否继续？',
        success: async (result) => {
          if (result.confirm) {
            try {
              await request({
                url: `/api/app/orders/${order.id}/cancel?confirmLateCancel=true`,
                method: 'POST',
              })
              uni.showToast({ title: '预约已取消', icon: 'success' })
              loadOrders()
            } catch (confirmError) {
              showError(confirmError, '取消失败')
            }
          }
        },
      })
      return
    }
    showError(error, '取消失败')
  }
}

async function postSign(orderId, longitude, latitude) {
  await request({
    url: `/api/app/orders/${orderId}/sign`,
    method: 'POST',
    data: { longitude, latitude },
  })
  uni.showToast({ title: '签到成功', icon: 'success' })
  loadOrders()
}

function signOrder(order) {
  if (SIGN_USE_AREA_CENTER) {
    const lon = order.signCenterLongitude != null ? Number(order.signCenterLongitude) : NaN
    const lat = order.signCenterLatitude != null ? Number(order.signCenterLatitude) : NaN
    if (!Number.isFinite(lon) || !Number.isFinite(lat)) {
      showError({ message: '该区域未配置签到中心点，无法使用测试签到' }, '无法签到')
      return
    }
    postSign(order.id, lon, lat).catch((error) => showError(error, '签到失败'))
    return
  }

  uni.getLocation({
    type: 'gcj02',
    success: async (location) => {
      try {
        await postSign(order.id, location.longitude, location.latitude)
      } catch (error) {
        showError(error, '签到失败')
      }
    },
    fail: () => {
      showError({ message: '请先授权定位后再签到' }, '请先授权定位后再签到')
    },
  })
}

async function finishOrder(order) {
  try {
    await request({
      url: `/api/app/orders/${order.id}/finish`,
      method: 'POST',
    })
    uni.showToast({ title: '工位已释放', icon: 'success' })
    loadOrders()
  } catch (error) {
    showError(error, '结束使用失败')
  }
}
</script>

<style scoped>
.reset-button {
  width: 160rpx;
  min-height: 80rpx;
  font-size: 24rpx;
}

.order-filter-row {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.order-filter-label {
  flex-shrink: 0;
  font-size: 26rpx;
  color: var(--sw-text-muted);
}

.order-filter-picker-wrap {
  flex: 1;
  min-width: 0;
}

.order-status-picker {
  width: 100%;
  box-sizing: border-box;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.order-card {
  padding: 28rpx;
  border-radius: 28rpx;
  background: rgba(15, 23, 42, 0.86);
  border: 1px solid rgba(148, 163, 184, 0.14);
  box-shadow: 0 18rpx 50rpx rgba(2, 6, 23, 0.2);
}

.order-top {
  display: flex;
  justify-content: space-between;
  gap: 18rpx;
  align-items: flex-start;
}

.order-no,
.order-sub,
.order-note {
  display: block;
}

.order-no {
  font-size: 30rpx;
  font-weight: 700;
  color: #f8fafc;
}

.order-sub {
  margin-top: 8rpx;
  font-size: 24rpx;
  color: var(--sw-text-muted);
}

.meta-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14rpx;
  margin-top: 22rpx;
}

.meta-item {
  padding: 18rpx;
  border-radius: 20rpx;
  background: rgba(15, 23, 42, 0.4);
  border: 1px solid rgba(148, 163, 184, 0.1);
}

.meta-label,
.meta-value {
  display: block;
}

.meta-label {
  font-size: 22rpx;
  color: var(--sw-text-soft);
}

.meta-value {
  margin-top: 10rpx;
  font-size: 24rpx;
  color: #e2e8f0;
}

.timeline {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-top: 26rpx;
}

.timeline-node {
  width: 20rpx;
  height: 20rpx;
  border-radius: 50%;
}

.timeline-node--done {
  background: var(--sw-accent);
  box-shadow: 0 0 0 8rpx rgba(56, 189, 248, 0.12);
}

.timeline-node--idle {
  background: rgba(148, 163, 184, 0.3);
}

.timeline-line {
  flex: 1;
  height: 4rpx;
  border-radius: 999rpx;
  background: rgba(148, 163, 184, 0.18);
}

.timeline-line--done {
  background: linear-gradient(90deg, rgba(56, 189, 248, 0.7), rgba(14, 165, 233, 0.9));
}

.timeline-labels {
  display: flex;
  justify-content: space-between;
  gap: 16rpx;
  margin-top: 14rpx;
  font-size: 22rpx;
  color: var(--sw-text-soft);
}

.order-note {
  margin-top: 18rpx;
  font-size: 24rpx;
  line-height: 1.7;
  color: var(--sw-text-muted);
}

.actions {
  display: flex;
  gap: 16rpx;
  margin-top: 24rpx;
}

.action-button {
  flex: 1;
}

.focus-order-text {
  display: block;
  font-size: 25rpx;
  line-height: 1.7;
  color: var(--sw-text-muted);
}

.space-path-row {
  margin-top: 16rpx;
  padding: 18rpx 20rpx;
  border-radius: 20rpx;
  background: rgba(15, 23, 42, 0.4);
  border: 1px solid rgba(148, 163, 184, 0.1);
}

.space-path-label,
.space-path-value {
  display: block;
}

.space-path-label {
  font-size: 22rpx;
  color: var(--sw-text-soft);
}

.space-path-value {
  margin-top: 8rpx;
  font-size: 24rpx;
  line-height: 1.6;
  color: #e2e8f0;
}
</style>
