<template>
  <view class="sw-page">
    <view class="sw-shell">
      <view class="sw-hero">
        <view class="profile-head">
          <view class="avatar">{{ avatarText }}</view>
          <view class="profile-meta">
            <text class="profile-name">{{ displayName }}</text>
            <text class="profile-phone">{{ formatPhone(user.phone) }}</text>
            <text class="profile-dept">{{ deptName || '暂未绑定部门' }}</text>
          </view>
        </view>
      </view>

      <view class="sw-card">
        <view class="sw-section-head">
          <view>
            <text class="sw-section-title">信用状态</text>
            <text class="sw-section-subtitle">信用分会影响是否可以继续提交新预约。</text>
          </view>
          <text class="credit-score">{{ user.creditScore || 0 }}</text>
        </view>

        <view class="score-track">
          <view class="score-bar" :style="scoreBarStyle"></view>
        </view>
        <text class="score-text">{{ creditLevel }}</text>

        <view v-if="user.restrictBookingUntil" class="warning-card">
          <text class="warning-title">预约限制提醒</text>
          <text class="warning-text">当前限制至 {{ formatFullDateTime(user.restrictBookingUntil) }}，请注意恢复信用分后再尝试预约。</text>
        </view>
      </view>

      <view class="sw-card">
        <view class="sw-section-head">
          <view>
            <text class="sw-section-title">快捷入口</text>
            <text class="sw-section-subtitle">高频动作直接从个人中心进入，少一层跳转。</text>
          </view>
        </view>

        <view class="quick-grid">
          <view class="quick-card" @click="goOrders">
            <text class="quick-title">我的预约</text>
            <text class="quick-desc">查看待签到和使用中的订单</text>
          </view>
          <view class="quick-card" @click="goMessages">
            <text class="quick-title">消息中心</text>
            <text class="quick-desc">处理未读提醒与系统通知</text>
          </view>
          <view class="quick-card" @click="openFeedback">
            <text class="quick-title">意见反馈</text>
            <text class="quick-desc">提交问题或改进建议</text>
          </view>
        </view>
      </view>

      <view class="sw-card">
        <view class="sw-section-head">
          <view>
            <text class="sw-section-title">信用记录</text>
            <text class="sw-section-subtitle">展示最近的加分和扣分流水，方便追溯规则触发原因。</text>
          </view>
        </view>

        <view v-if="!creditLogs.length" class="sw-empty">暂时没有信用记录，正常使用工位后会在这里累计轨迹。</view>
        <view v-else class="credit-list">
          <view v-for="log in creditLogs" :key="log.id" class="credit-card">
            <view class="credit-card-top">
              <text class="credit-reason">{{ log.reason || '信用变更' }}</text>
              <text :class="['sw-pill', log.changeType === 1 ? 'sw-pill--emerald' : 'sw-pill--rose']">
                {{ formatScoreChange(log) }}
              </text>
            </view>
            <view class="credit-meta">
             <!-- <text>关联订单：{{ log.orderId || '--' }}</text> -->
              <text>{{ formatDateTime(log.createTime) }}</text>
            </view>
          </view>
        </view>
      </view>

      <view class="sw-card">
        <view class="sw-section-head">
          <view>
            <text class="sw-section-title">常见问题</text>
          <!--  <text class="sw-section-subtitle">把高频规则翻成面向用户的说明，减少反复查找。</text> -->
          </view>
        </view>

        <view v-if="!faqList.length" class="sw-empty">暂未配置常见问题。</view>
        <view v-else class="faq-list">
          <view
            v-for="(item, index) in faqList"
            :key="item.q"
            class="faq-card"
            @click="toggleFaq(index)"
          >
            <view class="faq-head">
              <text class="faq-question">{{ item.q }}</text>
              <text class="faq-toggle">{{ openedFaq === index ? '收起' : '展开' }}</text>
            </view>
            <text v-if="openedFaq === index" class="faq-answer">{{ item.a }}</text>
          </view>
        </view>
      </view>

      <button class="sw-danger-button logout-button" @click="logout">退出登录</button>
    </view>
  </view>

  <view v-if="showFeedback" class="sw-overlay" @click="closeFeedback" @touchmove.stop.prevent>
    <view class="sw-sheet feedback-sheet" @click.stop @touchmove.stop>
      <view class="feedback-sheet-head">
        <view class="feedback-sheet-titles">
          <text class="sw-section-title">意见反馈</text>
          <text class="sw-section-subtitle">你的建议将帮助我们持续改进。</text>
        </view>
        <text class="feedback-close" @click.stop="closeFeedback">关闭</text>
      </view>
      <view class="sw-field feedback-field">
        <text class="sw-label">反馈内容</text>
        <textarea
          v-model="feedbackContent"
          class="sw-textarea feedback-textarea"
          placeholder="请描述遇到的问题或改进建议（最多 500 字）"
          placeholder-class="placeholder"
          maxlength="500"
          :adjust-position="true"
          :cursor-spacing="120"
          :show-confirm-bar="false"
        />
      </view>
      <text class="feedback-count">{{ feedbackLen }} / 500</text>
      <view class="sheet-actions">
        <button class="sw-secondary-button flex-button" :disabled="submittingFeedback" @click="closeFeedback">取消</button>
        <button
          class="sw-primary-button flex-button"
          :class="{ 'is-disabled': submittingFeedback }"
          :disabled="submittingFeedback"
          @click="submitFeedback"
        >
          {{ submittingFeedback ? '提交中...' : '提交' }}
        </button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import {
  clearSession,
  ensureLogin,
  formatDateTime,
  formatFullDateTime,
  formatPhone,
  formatScoreChange,
  getStoredUser,
  showError,
  summarizeCreditLevel,
} from '@/utils/app.js'
import { request } from '@/utils/request.js'

const user = ref(getStoredUser() || {})
const deptName = ref('')
const creditLogs = ref([])
const faqList = ref([])
const openedFaq = ref(0)
const showFeedback = ref(false)
const feedbackContent = ref('')
const submittingFeedback = ref(false)

const displayName = computed(() => user.value.realName || '共享用户')
const avatarText = computed(() => (displayName.value || 'U').slice(0, 1))
const creditLevel = computed(() => summarizeCreditLevel(user.value.creditScore))
const scoreBarStyle = computed(() => {
  const score = Math.max(0, Math.min(100, Number(user.value.creditScore) || 0))
  return `width:${score}%;`
})

const feedbackLen = computed(() => (feedbackContent.value || '').length)

onShow(() => {
  if (!ensureLogin()) {
    return
  }
  loadAll()
})

async function loadAll() {
  try {
    const [profileResponse, creditResponse, faqResponse] = await Promise.all([
      request({ url: '/api/app/me/profile' }),
      request({ url: '/api/app/me/credit-logs?page=1&size=20' }),
      request({ url: '/api/app/me/faq' }),
    ])
    user.value = profileResponse.data.user || {}
    deptName.value = profileResponse.data.deptName || ''
    creditLogs.value = (creditResponse.data && creditResponse.data.records) || []
    faqList.value = faqResponse.data || []
    uni.setStorageSync('user', user.value)
  } catch (error) {
    showError(error, '个人信息加载失败')
  }
}

function toggleFaq(index) {
  openedFaq.value = openedFaq.value === index ? -1 : index
}

function goOrders() {
  uni.switchTab({ url: '/pages/orders/orders' })
}

function goMessages() {
  uni.switchTab({ url: '/pages/messages/messages' })
}

function logout() {
  clearSession()
  uni.reLaunch({ url: '/pages/login/login' })
}

function openFeedback() {
  feedbackContent.value = ''
  showFeedback.value = true
}

function closeFeedback() {
  showFeedback.value = false
  feedbackContent.value = ''
}

async function submitFeedback() {
  const text = feedbackContent.value.trim()
  if (!text) {
    showError({ message: '请输入反馈内容' }, '请输入反馈内容')
    return
  }
  submittingFeedback.value = true
  try {
    await request({
      url: '/api/app/feedback',
      method: 'POST',
      data: { content: text },
    })
    uni.showToast({ title: '感谢你的反馈', icon: 'success' })
    feedbackContent.value = ''
    showFeedback.value = false
  } catch (error) {
    showError(error, '提交失败，请稍后重试')
  } finally {
    submittingFeedback.value = false
  }
}
</script>

<style scoped>
.profile-head {
  display: flex;
  align-items: center;
  gap: 20rpx;
  margin-top: 28rpx;
}

.avatar {
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, rgba(56, 189, 248, 0.9), rgba(14, 165, 233, 0.72));
  color: #04121f;
  font-size: 40rpx;
  font-weight: 800;
}

.profile-meta {
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.profile-name,
.profile-phone,
.profile-dept {
  display: block;
}

.profile-name {
  font-size: 32rpx;
  font-weight: 700;
  color: #f8fafc;
}

.profile-phone,
.profile-dept {
  font-size: 24rpx;
  color: var(--sw-text-muted);
}

.credit-score {
  font-size: 44rpx;
  font-weight: 800;
  color: var(--sw-accent);
}

.score-track {
  width: 100%;
  height: 18rpx;
  border-radius: 999rpx;
  background: rgba(148, 163, 184, 0.16);
  overflow: hidden;
}

.score-bar {
  height: 100%;
  border-radius: 999rpx;
  background: linear-gradient(90deg, #38bdf8, #34d399);
}

.score-text {
  display: block;
  margin-top: 16rpx;
  font-size: 24rpx;
  color: var(--sw-text-muted);
}

.warning-card {
  margin-top: 20rpx;
  padding: 22rpx 24rpx;
  border-radius: 24rpx;
  background: rgba(251, 113, 133, 0.1);
  border: 1px solid rgba(251, 113, 133, 0.18);
}

.warning-title,
.warning-text {
  display: block;
}

.warning-title {
  font-size: 24rpx;
  font-weight: 700;
  color: #fda4af;
}

.warning-text {
  margin-top: 10rpx;
  font-size: 24rpx;
  line-height: 1.7;
  color: #fecdd3;
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16rpx;
}

.quick-card {
  padding: 22rpx 20rpx;
  border-radius: 24rpx;
  background: rgba(15, 23, 42, 0.44);
  border: 1px solid rgba(148, 163, 184, 0.12);
}

.quick-title,
.quick-desc {
  display: block;
}

.quick-title {
  font-size: 26rpx;
  font-weight: 700;
  color: #f8fafc;
}

.quick-desc {
  margin-top: 10rpx;
  font-size: 22rpx;
  line-height: 1.6;
  color: var(--sw-text-muted);
}

.credit-list,
.faq-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.credit-card,
.faq-card {
  padding: 24rpx;
  border-radius: 24rpx;
  background: rgba(15, 23, 42, 0.44);
  border: 1px solid rgba(148, 163, 184, 0.12);
}

.credit-card-top,
.credit-meta,
.faq-head {
  display: flex;
  justify-content: space-between;
  gap: 18rpx;
  align-items: flex-start;
}

.credit-reason,
.faq-question,
.faq-toggle,
.faq-answer {
  display: block;
}

.credit-reason,
.faq-question {
  font-size: 26rpx;
  font-weight: 700;
  color: #f8fafc;
}

.credit-meta {
  margin-top: 14rpx;
  font-size: 22rpx;
  color: var(--sw-text-soft);
}

.faq-toggle {
  font-size: 22rpx;
  color: var(--sw-accent);
}

.faq-answer {
  margin-top: 14rpx;
  font-size: 24rpx;
  line-height: 1.7;
  color: var(--sw-text-muted);
}

.logout-button {
  margin-top: 8rpx;
}

.feedback-count {
  display: block;
  text-align: right;
  margin-top: 10rpx;
  font-size: 22rpx;
  color: var(--sw-text-soft);
}

.sheet-actions {
  display: flex;
  gap: 16rpx;
  margin-top: 24rpx;
}

.flex-button {
  flex: 1;
}

.placeholder {
  color: #64748b;
}

.feedback-sheet {
  max-height: 85vh;
  overflow-y: auto;
  padding-bottom: calc(36rpx + env(safe-area-inset-bottom));
}

.feedback-sheet-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20rpx;
  margin-bottom: 20rpx;
}

.feedback-sheet-titles {
  flex: 1;
  min-width: 0;
}

.feedback-sheet-titles .sw-section-title,
.feedback-sheet-titles .sw-section-subtitle {
  display: block;
}

.feedback-close {
  flex-shrink: 0;
  padding: 8rpx 0 8rpx 20rpx;
  font-size: 28rpx;
  color: var(--sw-accent);
  line-height: 1.4;
}

.feedback-field {
  width: 100%;
  flex: none;
  min-width: 0;
}

.feedback-textarea {
  width: 100%;
  min-height: 220rpx;
}

.flex-button.is-disabled,
.flex-button[disabled] {
  opacity: 0.55;
}
</style>
