<template>
  <view class="sw-page">
    <view class="sw-shell">
      <view class="sw-hero">
        <view class="sw-metric-grid">
          <view class="sw-metric-card">
            <text class="sw-metric-value">{{ messages.length }}</text>
            <text class="sw-metric-label">全部消息</text>
          </view>
          <view class="sw-metric-card">
            <text class="sw-metric-value">{{ unreadCount }}</text>
            <text class="sw-metric-label">未读</text>
          </view>
          <view class="sw-metric-card">
            <text class="sw-metric-value">{{ liveState }}</text>
            <text class="sw-metric-label">连接状态</text>
          </view>
        </view>
      </view>

      <view class="sw-card">
        <view class="sw-section-head">
          <text class="sw-section-title">消息筛选</text>
          <button class="sw-secondary-button action-mini" @click="markAllRead">全部已读</button>
        </view>

        <view class="sw-field">
          <text class="sw-label">关键词</text>
          <input
            v-model="keyword"
            class="sw-input"
            placeholder="搜索标题或内容"
            placeholder-class="placeholder"
          />
        </view>

        <view class="sw-chip-row filter-row">
          <view
            :class="['sw-chip', filterMode === 'all' ? 'sw-chip--active' : '']"
            @click="filterMode = 'all'"
          >
            全部
          </view>
          <view
            :class="['sw-chip', filterMode === 'unread' ? 'sw-chip--active' : '']"
            @click="filterMode = 'unread'"
          >
            仅未读
          </view>
        </view>
      </view>

      <view v-if="loading" class="sw-empty">正在拉取消息列表...</view>
      <view v-else-if="!filteredMessages.length" class="sw-empty">消息箱暂时很安静，新的提醒会实时出现在这里。</view>

      <view v-else class="message-list">
        <view
          v-for="message in filteredMessages"
          :key="message.id"
          :class="['message-card', !message.isRead ? 'message-card--unread' : '']"
          @click="readMessage(message)"
        >
          <view class="message-top">
            <view class="message-title-wrap">
              <view v-if="!message.isRead" class="message-dot"></view>
              <text class="message-title">{{ message.title || '系统通知' }}</text>
            </view>
            <text class="message-time">{{ formatRelativeTime(message.createTime) }}</text>
          </view>
          <text class="message-content">{{ message.content }}</text>
          <view class="message-bottom">
            <text class="message-state">{{ message.isRead ? '已读' : '未读' }}</text>
            <text class="message-time">{{ formatDateTime(message.createTime) }}</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onShow, onUnload } from '@dcloudio/uni-app'
import {
  ensureLogin,
  formatDateTime,
  formatRelativeTime,
  setUnreadCount,
  showError,
} from '@/utils/app.js'
import { request } from '@/utils/request.js'
import { refreshUnreadBadge } from '@/utils/socket.js'

const loading = ref(false)
const messages = ref([])
const keyword = ref('')
const filterMode = ref('all')
const liveState = ref('在线')
const listenerReady = ref(false)

const unreadCount = computed(() => messages.value.filter((item) => !item.isRead).length)

const filteredMessages = computed(() => {
  const keywordText = keyword.value.trim()
  return messages.value.filter((item) => {
    const matchesFilter = filterMode.value !== 'unread' || !item.isRead
    const matchesKeyword =
      !keywordText ||
      (item.title || '').includes(keywordText) ||
      (item.content || '').includes(keywordText)
    return matchesFilter && matchesKeyword
  })
})

onShow(() => {
  if (!ensureLogin()) {
    return
  }
  if (!listenerReady.value) {
    listenerReady.value = true
    uni.$on('notify:message', prependMessage)
    uni.$on('socket:state', updateSocketState)
  }
  loadMessages()
})

onUnload(() => {
  if (listenerReady.value) {
    uni.$off('notify:message', prependMessage)
    uni.$off('socket:state', updateSocketState)
  }
})

async function loadMessages() {
  loading.value = true
  try {
    const response = await request({ url: '/api/app/messages?page=1&size=100' })
    messages.value = (response.data && response.data.records) || []
    setUnreadCount(unreadCount.value)
  } catch (error) {
    showError(error, '消息加载失败')
  } finally {
    loading.value = false
  }
}

async function readMessage(message) {
  if (message.isRead) {
    return
  }
  try {
    await request({
      url: `/api/app/messages/${message.id}/read`,
      method: 'POST',
    })
    message.isRead = 1
    setUnreadCount(unreadCount.value)
  } catch (error) {
    showError(error, '消息状态更新失败')
  }
}

async function markAllRead() {
  const unreadList = messages.value.filter((item) => !item.isRead)
  if (!unreadList.length) {
    uni.showToast({ title: '已经全部已读', icon: 'none' })
    return
  }
  try {
    await Promise.all(
      unreadList.map((message) =>
        request({
          url: `/api/app/messages/${message.id}/read`,
          method: 'POST',
        }),
      ),
    )
    unreadList.forEach((item) => {
      item.isRead = 1
    })
    setUnreadCount(0)
    refreshUnreadBadge()
    uni.showToast({ title: '已全部标记', icon: 'success' })
  } catch (error) {
    showError(error, '批量已读失败')
  }
}

function prependMessage(payload) {
  if (!payload || !payload.title) {
    loadMessages()
    return
  }
  messages.value = [
    {
      id: `live-${Date.now()}`,
      title: payload.title,
      content: payload.content,
      isRead: 0,
      createTime: new Date(),
    },
    ...messages.value,
  ]
  setUnreadCount(unreadCount.value)
}

function updateSocketState(event) {
  liveState.value = event && event.connected ? '在线' : '重连中'
}
</script>

<style scoped>
.action-mini {
  width: 176rpx;
  min-height: 80rpx;
  font-size: 24rpx;
  margin-left: 300rpx;
}

.filter-row {
  margin-top: 20rpx;
}

.message-list {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
}

.message-card {
  padding: 26rpx;
  border-radius: 26rpx;
  background: rgba(15, 23, 42, 0.84);
  border: 1px solid rgba(148, 163, 184, 0.12);
}

.message-card--unread {
  border-color: rgba(56, 189, 248, 0.32);
  box-shadow: 0 0 0 2rpx rgba(56, 189, 248, 0.08);
}

.message-top,
.message-bottom {
  display: flex;
  justify-content: space-between;
  gap: 18rpx;
  align-items: center;
}

.message-title-wrap {
  display: flex;
  align-items: center;
  gap: 12rpx;
  min-width: 0;
}

.message-dot {
  width: 14rpx;
  height: 14rpx;
  border-radius: 50%;
  background: var(--sw-accent);
  box-shadow: 0 0 0 8rpx rgba(56, 189, 248, 0.12);
}

.message-title,
.message-time,
.message-content,
.message-state {
  display: block;
}

.message-title {
  font-size: 28rpx;
  font-weight: 700;
  color: #f8fafc;
}

.message-time {
  font-size: 22rpx;
  color: var(--sw-text-soft);
  flex-shrink: 0;
}

.message-content {
  margin-top: 14rpx;
  font-size: 25rpx;
  line-height: 1.7;
  color: var(--sw-text-muted);
}

.message-bottom {
  margin-top: 18rpx;
}

.message-state {
  font-size: 22rpx;
  color: var(--sw-accent);
}

.placeholder {
  color: #64748b;
}
</style>
