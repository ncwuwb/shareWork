<template>
  <view class="sw-page login-page">
    <view class="login-glow login-glow--top"></view>
    <view class="login-glow login-glow--bottom"></view>
    <view class="sw-shell">
      <view class="sw-hero compact-hero">
        <text class="sw-eyebrow">共享工位</text>
        <text class="sw-hero-title">手机号登录</text>
        <text class="sw-hero-desc">输入手机号和验证码后即可进入用户端。</text>
      </view>

      <view class="sw-card form-card">
        <view class="sw-field-grid">
          <view class="sw-field full">
            <text class="sw-label">手机号</text>
            <input
              v-model="phone"
              class="sw-input"
              type="number"
              maxlength="11"
              placeholder="请输入 11 位手机号"
              placeholder-class="placeholder"
            />
          </view>

          <view class="sw-field full">
            <text class="sw-label">验证码</text>
            <view class="code-row">
              <input
                v-model="code"
                class="sw-input code-input"
                type="number"
                maxlength="6"
                placeholder="请输入 6 位验证码"
                placeholder-class="placeholder"
              />
              <button
                class="sw-secondary-button code-button"
                :disabled="cooldown > 0 || sending"
                @click="sendCaptcha"
              >
                {{ cooldown > 0 ? `${cooldown}s` : sending ? '发送中...' : '获取验证码' }}
              </button>
            </view>
          </view>
        </view>

        <text class="login-tip">测试环境验证码由后端控制台输出。</text>

        <button class="sw-primary-button submit-button" :disabled="submitting" @click="doLogin">
          {{ submitting ? '登录中...' : '登录' }}
        </button>
      </view>
    </view>

    <!-- 未绑定部门：登录后必选 -->
    <view v-if="deptModalVisible" class="dept-mask">
      <view class="dept-dialog" @click.stop>
        <text class="dept-dialog-title">完善个人信息</text>
        <text class="dept-dialog-tip">请填写您的姓名并选择部门，便于统计与违约管理等。</text>
        <view v-if="deptLoadError" class="dept-empty">{{ deptLoadError }}</view>
        <template v-else>
          <view class="dept-field">
            <text class="dept-field-label">姓名</text>
            <input
              v-model="bindRealName"
              class="dept-name-input"
              type="text"
              maxlength="50"
              placeholder="请输入真实姓名"
              placeholder-class="placeholder"
            />
          </view>
        </template>
        <scroll-view v-if="!deptLoadError" scroll-y class="dept-scroll">
          <view
            v-for="d in deptList"
            :key="d.id"
            :class="['dept-item', selectedDeptId === d.id ? 'dept-item--on' : '']"
            @click="selectedDeptId = d.id"
          >
            <text class="dept-item-text">{{ d.name }}</text>
            <text class="dept-item-check">{{ selectedDeptId === d.id ? '✓' : '' }}</text>
          </view>
        </scroll-view>
        <button
          class="sw-primary-button dept-confirm"
          :disabled="deptBinding || (!deptList.length && !deptLoadError)"
          @click="confirmDept"
        >
          {{ deptBinding ? '提交中...' : deptList.length ? '确认' : '暂无部门，进入首页' }}
        </button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onUnload } from '@dcloudio/uni-app'
import { getToken, saveSession, setUnreadCount, showError } from '@/utils/app.js'
import { request } from '@/utils/request.js'
import { connectNotifySocket, refreshUnreadBadge } from '@/utils/socket.js'

const phone = ref('')
const code = ref('')
const cooldown = ref(0)
const sending = ref(false)
const submitting = ref(false)

const deptModalVisible = ref(false)
const deptList = ref([])
const selectedDeptId = ref(null)
const deptBinding = ref(false)
const deptLoadError = ref('')
const bindRealName = ref('')

let timer = null

onUnload(() => {
  clearTimer()
})

function clearTimer() {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
}

function startCooldown() {
  clearTimer()
  cooldown.value = 60
  timer = setInterval(() => {
    cooldown.value -= 1
    if (cooldown.value <= 0) {
      clearTimer()
    }
  }, 1000)
}

function validPhone() {
  return /^1\d{10}$/.test(phone.value)
}

async function sendCaptcha() {
  if (!validPhone()) {
    showError({ message: '请输入正确的 11 位手机号' }, '请输入正确的 11 位手机号')
    return
  }
  sending.value = true
  try {
    await request({
      url: `/api/app/send-captcha?phone=${encodeURIComponent(phone.value)}`,
      method: 'POST',
    })
    uni.showToast({ title: '验证码已发送', icon: 'none' })
    startCooldown()
  } catch (error) {
    showError(error, '验证码发送失败')
  } finally {
    sending.value = false
  }
}

async function doLogin() {
  if (!validPhone()) {
    showError({ message: '请输入正确的手机号' }, '请输入正确的手机号')
    return
  }
  if (!/^\d{6}$/.test(code.value)) {
    showError({ message: '请输入 6 位验证码' }, '请输入 6 位验证码')
    return
  }

  submitting.value = true
  try {
    const response = await request({
      url: '/api/app/login',
      method: 'POST',
      data: {
        phone: phone.value,
        code: code.value,
      },
    })
    saveSession(response.data.token, response.data.user)
    setUnreadCount(0)
    connectNotifySocket()
    refreshUnreadBadge()
    const user = response.data.user
    if (user && (user.deptId === null || user.deptId === undefined)) {
      await openDeptBindModal()
      return
    }
    uni.switchTab({ url: '/pages/hall/hall' })
  } catch (error) {
    showError(error, '登录失败，请重试')
  } finally {
    submitting.value = false
  }
}

async function openDeptBindModal() {
  deptLoadError.value = ''
  deptList.value = []
  selectedDeptId.value = null
  bindRealName.value = ''
  deptModalVisible.value = true
  try {
    const res = await request({ url: '/api/app/depts', method: 'GET' })
    const list = res.data || []
    deptList.value = list
    if (list.length) {
      selectedDeptId.value = list[0].id
    } else {
      deptLoadError.value = '暂无部门可选，请联系管理员在后台维护部门后再绑定。您仍可先进入首页。'
    }
  } catch (e) {
    deptLoadError.value = (e && e.message) || '部门列表加载失败'
    showError(e, '部门列表加载失败')
  }
}

async function confirmDept() {
  if (deptLoadError.value && !deptList.value.length) {
    deptModalVisible.value = false
    uni.switchTab({ url: '/pages/hall/hall' })
    return
  }
  if (!deptList.value.length) {
    return
  }
  if (selectedDeptId.value == null) {
    uni.showToast({ title: '请选择部门', icon: 'none' })
    return
  }
  const name = String(bindRealName.value || '').trim()
  if (!name) {
    uni.showToast({ title: '请输入姓名', icon: 'none' })
    return
  }
  deptBinding.value = true
  try {
    const res = await request({
      url: '/api/app/me/dept',
      method: 'POST',
      data: { deptId: selectedDeptId.value, realName: name },
    })
    saveSession(getToken(), res.data)
    deptModalVisible.value = false
    uni.switchTab({ url: '/pages/hall/hall' })
  } catch (e) {
    showError(e, '绑定部门失败')
  } finally {
    deptBinding.value = false
  }
}
</script>

<style scoped>
.login-page {
  position: relative;
  overflow: hidden;
  padding-top: 80rpx;
}

.login-glow {
  position: fixed;
  width: 360rpx;
  height: 360rpx;
  border-radius: 50%;
  pointer-events: none;
  filter: blur(8rpx);
}

.login-glow--top {
  top: -120rpx;
  right: -60rpx;
  background: radial-gradient(circle, rgba(56, 189, 248, 0.24), transparent 70%);
}

.login-glow--bottom {
  left: -120rpx;
  bottom: 80rpx;
  background: radial-gradient(circle, rgba(59, 130, 246, 0.16), transparent 70%);
}

.compact-hero {
  padding: 36rpx 32rpx;
}

.form-card {
  position: relative;
  z-index: 2;
}

.full {
  min-width: 100%;
}

.code-row {
  display: flex;
  gap: 16rpx;
  align-items: center;
}

.code-input {
  flex: 1;
}

.code-button {
  width: 220rpx;
  min-height: 88rpx;
  font-size: 24rpx;
}

.login-tip {
  display: block;
  margin-top: 20rpx;
  font-size: 23rpx;
  color: var(--sw-text-muted);
}

.submit-button {
  margin-top: 28rpx;
}

.placeholder {
  color: #64748b;
}

.dept-mask {
  position: fixed;
  left: 0;
  right: 0;
  top: 0;
  bottom: 0;
  z-index: 1000;
  background: rgba(15, 23, 42, 0.72);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48rpx 32rpx;
  box-sizing: border-box;
}

.dept-dialog {
  width: 100%;
  max-width: 640rpx;
  max-height: 80vh;
  background: #1e293b;
  border-radius: 20rpx;
  border: 1rpx solid rgba(148, 163, 184, 0.2);
  padding: 32rpx 28rpx 28rpx;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
}

.dept-dialog-title {
  font-size: 34rpx;
  font-weight: 700;
  color: #f1f5f9;
  text-align: center;
}

.dept-dialog-tip {
  display: block;
  margin-top: 16rpx;
  font-size: 24rpx;
  color: #94a3b8;
  line-height: 1.5;
}

.dept-field {
  margin-top: 24rpx;
}

.dept-field-label {
  display: block;
  font-size: 26rpx;
  color: #94a3b8;
  margin-bottom: 12rpx;
}

.dept-name-input {
  width: 100%;
  height: 88rpx;
  padding: 0 24rpx;
  box-sizing: border-box;
  font-size: 28rpx;
  color: #f1f5f9;
  background: rgba(15, 23, 42, 0.72);
  border-radius: 12rpx;
  border: 1rpx solid rgba(51, 65, 85, 0.9);
}

.dept-scroll {
  flex: 1;
  max-height: 360rpx;
  margin-top: 20rpx;
}

.dept-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 20rpx;
  margin-bottom: 12rpx;
  border-radius: 12rpx;
  background: rgba(15, 23, 42, 0.6);
  border: 1rpx solid rgba(51, 65, 85, 0.8);
}

.dept-item--on {
  border-color: #38bdf8;
  background: rgba(56, 189, 248, 0.12);
}

.dept-item-text {
  flex: 1;
  font-size: 28rpx;
  color: #e2e8f0;
  padding-right: 16rpx;
}

.dept-item-check {
  font-size: 28rpx;
  color: #38bdf8;
  width: 40rpx;
  text-align: right;
}

.dept-empty {
  margin-top: 24rpx;
  padding: 24rpx;
  font-size: 26rpx;
  color: #fbbf24;
  line-height: 1.5;
  background: rgba(251, 191, 36, 0.08);
  border-radius: 12rpx;
}

.dept-confirm {
  margin-top: 28rpx;
}
</style>
