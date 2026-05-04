<script>
import { getToken } from './utils/app.js'
import { connectNotifySocket, disconnectNotifySocket, refreshUnreadBadge } from './utils/socket.js'

let lastUnreadSyncAt = 0

export default {
  onLaunch() {
    this.notifyHandler = (payload = {}) => {
      if (payload.title) {
        uni.showToast({
          title: payload.title,
          icon: 'none',
          duration: 1800,
        })
      }
      if (typeof uni.vibrateShort === 'function') {
        uni.vibrateShort({})
      }
    }
    uni.$on('notify:message', this.notifyHandler)
  },
  onShow() {
    if (!getToken()) {
      return
    }
    const now = Date.now()
    if (now - lastUnreadSyncAt > 30000) {
      lastUnreadSyncAt = now
      refreshUnreadBadge()
    }
    connectNotifySocket()
  },
  onHide() {},
  onUnload() {
    if (this.notifyHandler) {
      uni.$off('notify:message', this.notifyHandler)
    }
    disconnectNotifySocket()
    lastUnreadSyncAt = 0
  },
}
</script>

<style>
:root {
  --sw-bg: #08111f;
  --sw-bg-deep: #0f172a;
  --sw-panel: rgba(15, 23, 42, 0.86);
  --sw-panel-soft: rgba(30, 41, 59, 0.92);
  --sw-panel-strong: #131d31;
  --sw-line: rgba(148, 163, 184, 0.18);
  --sw-text: #e2e8f0;
  --sw-text-muted: #94a3b8;
  --sw-text-soft: #64748b;
  --sw-accent: #38bdf8;
  --sw-accent-strong: #0ea5e9;
  --sw-success: #34d399;
  --sw-warning: #fbbf24;
  --sw-danger: #fb7185;
}

page {
  background:
    radial-gradient(circle at top right, rgba(14, 165, 233, 0.16), transparent 34%),
    radial-gradient(circle at left center, rgba(59, 130, 246, 0.12), transparent 28%),
    linear-gradient(180deg, #06101d 0%, #0b1220 48%, #0f172a 100%);
  color: var(--sw-text);
}

view,
text,
scroll-view,
button,
input,
textarea,
picker {
  box-sizing: border-box;
}

button {
  border-radius: 24rpx;
  font-size: 28rpx;
  font-weight: 600;
  line-height: 1.2;
}

button::after {
  border: none;
}

.sw-page {
  min-height: 100vh;
  padding: 28rpx 24rpx 40rpx;
}

.sw-shell {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.sw-hero {
  position: relative;
  overflow: hidden;
  padding: 32rpx;
  border-radius: 32rpx;
  background: linear-gradient(135deg, rgba(15, 23, 42, 0.92), rgba(17, 24, 39, 0.86));
  border: 1px solid var(--sw-line);
  box-shadow: 0 24rpx 80rpx rgba(2, 6, 23, 0.32);
}

.sw-hero::before {
  content: '';
  position: absolute;
  right: -120rpx;
  top: -120rpx;
  width: 320rpx;
  height: 320rpx;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(56, 189, 248, 0.26), transparent 68%);
}

.sw-eyebrow {
  display: block;
  color: var(--sw-accent);
  font-size: 22rpx;
  letter-spacing: 2rpx;
}

.sw-hero-title {
  display: block;
  margin-top: 14rpx;
  font-size: 44rpx;
  font-weight: 700;
  line-height: 1.28;
  color: #f8fafc;
}

.sw-hero-desc {
  display: block;
  margin-top: 12rpx;
  font-size: 26rpx;
  line-height: 1.7;
  color: var(--sw-text-muted);
}

.sw-card {
  background: var(--sw-panel);
  border: 1px solid var(--sw-line);
  border-radius: 28rpx;
  padding: 28rpx;
  box-shadow: 0 18rpx 60rpx rgba(15, 23, 42, 0.24);
}

.sw-section-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16rpx;
  margin-bottom: 20rpx;
}

.sw-section-title {
  display: block;
  font-size: 30rpx;
  font-weight: 700;
  color: #f8fafc;
}

.sw-section-subtitle {
  display: block;
  margin-top: 6rpx;
  font-size: 24rpx;
  line-height: 1.6;
  color: var(--sw-text-muted);
}

.sw-chip-row {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.sw-chip {
  padding: 14rpx 22rpx;
  border-radius: 999rpx;
  background: rgba(51, 65, 85, 0.34);
  color: #cbd5e1;
  border: 1px solid transparent;
  font-size: 24rpx;
  line-height: 1;
}

.sw-chip--active {
  color: #f8fafc;
  border-color: rgba(56, 189, 248, 0.38);
  background: rgba(56, 189, 248, 0.14);
  box-shadow: inset 0 0 0 2rpx rgba(56, 189, 248, 0.08);
}

.sw-field-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx;
}

.sw-field {
  flex: 1;
  min-width: 200rpx;
}

.sw-label {
  display: block;
  margin-bottom: 12rpx;
  font-size: 24rpx;
  color: var(--sw-text-muted);
}

.sw-picker,
.sw-input,
.sw-textarea {
  min-height: 88rpx;
  padding: 0 24rpx;
  display: flex;
  align-items: center;
  border-radius: 22rpx;
  background: var(--sw-panel-soft);
  border: 1px solid rgba(148, 163, 184, 0.12);
  color: #f8fafc;
}

.sw-input,
.sw-textarea {
  width: 100%;
}

.sw-textarea {
  min-height: 180rpx;
  padding: 24rpx;
  line-height: 1.6;
}

.sw-subtle {
  display: block;
  color: var(--sw-text-soft);
  font-size: 24rpx;
  line-height: 1.7;
}

.sw-primary-button {
  background: linear-gradient(135deg, var(--sw-accent), var(--sw-accent-strong));
  color: #04121f;
}

.sw-secondary-button {
  background: rgba(30, 41, 59, 0.9);
  color: var(--sw-text);
  border: 1px solid rgba(148, 163, 184, 0.18);
}

.sw-danger-button {
  background: linear-gradient(135deg, #fb7185, #f43f5e);
  color: #fff;
}

.sw-empty {
  padding: 48rpx 24rpx;
  border-radius: 24rpx;
  border: 1px dashed rgba(148, 163, 184, 0.18);
  background: rgba(15, 23, 42, 0.32);
  text-align: center;
  color: var(--sw-text-muted);
  line-height: 1.7;
}

.sw-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 10rpx 18rpx;
  border-radius: 999rpx;
  font-size: 22rpx;
  font-weight: 700;
}

.sw-pill--emerald {
  background: rgba(52, 211, 153, 0.16);
  color: var(--sw-success);
}

.sw-pill--amber {
  background: rgba(251, 191, 36, 0.14);
  color: var(--sw-warning);
}

.sw-pill--sky {
  background: rgba(56, 189, 248, 0.14);
  color: var(--sw-accent);
}

.sw-pill--slate {
  background: rgba(148, 163, 184, 0.14);
  color: #cbd5e1;
}

.sw-pill--rose {
  background: rgba(251, 113, 133, 0.14);
  color: var(--sw-danger);
}

.sw-overlay {
  position: fixed;
  inset: 0;
  background: rgba(2, 6, 23, 0.74);
  display: flex;
  align-items: flex-end;
  justify-content: center;
  padding: 24rpx;
  z-index: 99;
}

.sw-sheet {
  width: 100%;
  border-radius: 28rpx;
  padding: 32rpx 28rpx 36rpx;
  background: linear-gradient(180deg, rgba(15, 23, 42, 0.98), rgba(17, 24, 39, 0.98));
  border: 1px solid rgba(148, 163, 184, 0.18);
  box-shadow: 0 -12rpx 64rpx rgba(2, 6, 23, 0.42);
}

.sw-metric-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16rpx;
  margin-top: 24rpx;
}

.sw-metric-card {
  padding: 20rpx;
  border-radius: 22rpx;
  background: rgba(15, 23, 42, 0.52);
  border: 1px solid rgba(148, 163, 184, 0.12);
}

.sw-metric-value {
  display: block;
  font-size: 40rpx;
  font-weight: 700;
  color: #f8fafc;
}

.sw-metric-label {
  display: block;
  margin-top: 8rpx;
  font-size: 22rpx;
  color: var(--sw-text-muted);
}
</style>
