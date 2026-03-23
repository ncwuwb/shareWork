import { API_BASE } from './config.js'
import { getToken, getUnreadCount, setUnreadCount } from './app.js'

let socketTask = null
let reconnectTimer = null
let closedByUser = false

function getSocketUrl() {
  return `${API_BASE.replace(/^http/i, 'ws')}/ws/notify`
}

function scheduleReconnect() {
  if (closedByUser || reconnectTimer || !getToken()) {
    return
  }
  reconnectTimer = setTimeout(() => {
    reconnectTimer = null
    connectNotifySocket()
  }, 3000)
}

export function connectNotifySocket() {
  const token = getToken()
  if (!token || socketTask) {
    return
  }

  closedByUser = false
  const task = uni.connectSocket({
    url: `${getSocketUrl()}?token=${encodeURIComponent(token)}`,
  })
  socketTask = task

  task.onOpen(() => {
    uni.$emit('socket:state', { connected: true })
    refreshUnreadBadge()
  })

  task.onMessage((event) => {
    let payload = {}
    try {
      payload = JSON.parse(event.data)
    } catch (error) {
      payload = { title: '新通知', content: event.data }
    }
    setUnreadCount(getUnreadCount() + 1)
    uni.$emit('notify:message', payload)
  })

  const onSocketClosed = () => {
    socketTask = null
    uni.$emit('socket:state', { connected: false })
    scheduleReconnect()
  }

  task.onClose(onSocketClosed)
  task.onError(onSocketClosed)
}

export function disconnectNotifySocket() {
  closedByUser = true
  if (reconnectTimer) {
    clearTimeout(reconnectTimer)
    reconnectTimer = null
  }
  if (!socketTask) {
    return
  }
  const current = socketTask
  socketTask = null
  try {
    current.close({})
  } catch (error) {
    console.log('socket close ignored', error)
  }
}

export function refreshUnreadBadge() {
  const token = getToken()
  if (!token) {
    setUnreadCount(0)
    return Promise.resolve(0)
  }

  return new Promise((resolve) => {
    uni.request({
      url: `${API_BASE}/api/app/messages?page=1&size=100`,
      method: 'GET',
      header: {
        Authorization: `Bearer ${token}`,
      },
      success: (response) => {
        const body = response.data || {}
        const records = (((body || {}).data || {}).records) || []
        const unread = records.filter((item) => !item.isRead).length
        setUnreadCount(unread)
        resolve(unread)
      },
      fail: () => resolve(getUnreadCount()),
    })
  })
}
