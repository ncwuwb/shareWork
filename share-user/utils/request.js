import { API_BASE } from './config.js'
import { clearSession } from './app.js'

export function request({ url, method = 'GET', data = {}, header = {} }) {
  const token = uni.getStorageSync('token')
  const hasToken = Boolean(token)
  return new Promise((resolve, reject) => {
    uni.request({
      url: API_BASE + url,
      method,
      data,
      header: {
        'Content-Type': 'application/json',
        ...header,
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
      },
      success: (response) => {
        const body = response.data
        if (response.statusCode === 401 || response.statusCode === 403) {
          if (hasToken) {
            clearSession()
            uni.reLaunch({ url: '/pages/login/login' })
            reject({ code: response.statusCode, message: '登录状态已失效，请重新登录' })
            return
          }
          reject({
            code: response.statusCode,
            message: (body && body.msg) || '请求被拒绝，请检查服务访问地址或跨域配置',
          })
          return
        }
        if (!body || typeof body.code !== 'number') {
          reject(new Error('网络错误，请稍后再试'))
          return
        }
        if (body.code !== 0) {
          if (hasToken && (body.code === 401 || body.code === 403)) {
            clearSession()
            uni.reLaunch({ url: '/pages/login/login' })
          }
          reject({ code: body.code, message: body.msg || '请求失败' })
          return
        }
        resolve(body)
      },
      fail: (error) => {
        reject({ message: error.errMsg || '请求失败，请检查网络连接' })
      },
    })
  })
}
