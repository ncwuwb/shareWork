export const ORDER_STATUS_META = {
  0: {
    label: '待签到',
    tone: 'amber',
    description: '请在开始前 15 分钟到开始后 30 分钟内完成签到',
  },
  1: {
    label: '使用中',
    tone: 'emerald',
    description: '已签到，可在离开时主动结束使用',
  },
  2: {
    label: '已完成',
    tone: 'sky',
    description: '本次预约已正常结束',
  },
  3: {
    label: '已取消',
    tone: 'slate',
    description: '该预约已取消，不再占用工位',
  },
  4: {
    label: '已违约',
    tone: 'rose',
    description: '超过签到时限未签到，系统已释放工位',
  },
}

export const ORDER_TABS = [
  { value: 'all', label: '全部' },
  { value: 0, label: '待签到' },
  { value: 1, label: '使用中' },
  { value: 2, label: '已完成' },
  { value: 3, label: '已取消' },
  { value: 4, label: '已违约' },
]

export const RUNTIME_STATUS_META = {
  0: {
    label: '可预约',
    tone: 'emerald',
    description: '当前时段可直接发起预约',
  },
  1: {
    label: '已预约',
    tone: 'rose',
    description: '当前时段已被其他人锁定',
  },
  2: {
    label: '使用中',
    tone: 'amber',
    description: '该工位当前有人使用',
  },
  3: {
    label: '不可用',
    tone: 'slate',
    description: '维护中或当前不对你开放',
  },
}

export const TIME_PRESETS = [
  { key: 'full', label: '全天', start: 9, end: 18 },
  { key: 'am', label: '上午', start: 9, end: 12 },
  { key: 'pm', label: '下午', start: 13, end: 18 },
  { key: 'focus', label: '深度工作', start: 10, end: 16 },
  { key: 'custom', label: '自定义' },
]

export const HOUR_OPTIONS = Array.from({ length: 16 }, (_, index) => {
  const hour = index + 7
  return {
    value: hour,
    label: `${String(hour).padStart(2, '0')}:00`,
  }
})

export function getToken() {
  return uni.getStorageSync('token') || ''
}

export function getStoredUser() {
  return uni.getStorageSync('user') || null
}

export function saveSession(token, user) {
  uni.setStorageSync('token', token)
  uni.setStorageSync('user', user || {})
}

export function clearSession() {
  uni.removeStorageSync('token')
  uni.removeStorageSync('user')
  setUnreadCount(0)
}

export function ensureLogin() {
  if (!getToken()) {
    uni.reLaunch({ url: '/pages/login/login' })
    return false
  }
  return true
}

export function getUnreadCount() {
  return Number(uni.getStorageSync('messageUnread') || 0) || 0
}

export function setUnreadCount(count) {
  const safeCount = Math.max(0, Number(count) || 0)
  uni.setStorageSync('messageUnread', safeCount)
  const options =
    safeCount > 0
      ? {
          index: 2,
          text: safeCount > 99 ? '99+' : String(safeCount),
          fail: () => {},
        }
      : {
          index: 2,
          fail: () => {},
        }
  const task = safeCount > 0 ? uni.setTabBarBadge(options) : uni.removeTabBarBadge(options)
  if (task && typeof task.catch === 'function') {
    task.catch(() => {})
  }
  return safeCount
}

export function formatDateKey(date = new Date()) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

export function buildDateTime(day, hour) {
  return `${day} ${String(hour).padStart(2, '0')}:00:00`
}

export function toDate(value) {
  if (!value) return null
  if (value instanceof Date) return value
  const text = String(value).trim().replace('T', ' ')
  const match = text.match(
    /^(\d{4})-(\d{2})-(\d{2})(?:\s+(\d{2}))?(?::(\d{2}))?(?::(\d{2}))?$/,
  )
  if (!match) return null
  return new Date(
    Number(match[1]),
    Number(match[2]) - 1,
    Number(match[3]),
    Number(match[4] || 0),
    Number(match[5] || 0),
    Number(match[6] || 0),
  )
}

export function formatDateTime(value) {
  const date = toDate(value)
  if (!date) return '--'
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hour = String(date.getHours()).padStart(2, '0')
  const minute = String(date.getMinutes()).padStart(2, '0')
  return `${month}-${day} ${hour}:${minute}`
}

export function formatFullDateTime(value) {
  const date = toDate(value)
  if (!date) return '--'
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hour = String(date.getHours()).padStart(2, '0')
  const minute = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hour}:${minute}`
}

export function formatRange(start, end) {
  const startDate = toDate(start)
  const endDate = toDate(end)
  if (!startDate || !endDate) return '--'
  const sameDay =
    startDate.getFullYear() === endDate.getFullYear() &&
    startDate.getMonth() === endDate.getMonth() &&
    startDate.getDate() === endDate.getDate()
  if (sameDay) {
    return `${formatDateTime(start)} - ${String(endDate.getHours()).padStart(2, '0')}:${String(
      endDate.getMinutes(),
    ).padStart(2, '0')}`
  }
  return `${formatDateTime(start)} - ${formatDateTime(end)}`
}

export function formatRelativeTime(value) {
  const date = toDate(value)
  if (!date) return '--'
  const diff = Date.now() - date.getTime()
  const minute = 60 * 1000
  const hour = 60 * minute
  const day = 24 * hour
  if (diff < minute) return '刚刚'
  if (diff < hour) return `${Math.max(1, Math.floor(diff / minute))} 分钟前`
  if (diff < day) return `${Math.max(1, Math.floor(diff / hour))} 小时前`
  if (diff < 7 * day) return `${Math.max(1, Math.floor(diff / day))} 天前`
  return formatDateTime(value)
}

export function formatPhone(phone) {
  const text = String(phone || '')
  if (text.length < 7) return text || '--'
  return `${text.slice(0, 3)} **** ${text.slice(-4)}`
}

export function getOrderStatusMeta(status) {
  return ORDER_STATUS_META[status] || ORDER_STATUS_META[3]
}

export function getRuntimeStatusMeta(status) {
  return RUNTIME_STATUS_META[status] || RUNTIME_STATUS_META[3]
}

export function formatScoreChange(log) {
  const value = Number(log && log.score) || 0
  return `${log && log.changeType === 1 ? '+' : '-'}${value}`
}

export function summarizeCreditLevel(score) {
  const safeScore = Number(score) || 0
  if (safeScore >= 90) return '信用优秀'
  if (safeScore >= 75) return '信用稳定'
  if (safeScore >= 60) return '建议注意'
  return '预约受限风险'
}

export function showError(error, fallback = '操作失败，请稍后重试') {
  const message = normalizeMessage(error && (error.message || error.errMsg), fallback)
  uni.showToast({
    title: message,
    icon: 'none',
    duration: 2200,
  })
}

function normalizeMessage(message, fallback) {
  const text = String(message || '').trim()
  if (!text) return fallback
  return text.length > 24 ? `${text.slice(0, 24)}...` : text
}
