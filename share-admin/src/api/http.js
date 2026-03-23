import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const http = axios.create({
  baseURL: '/api',
  timeout: 20000,
})

let authRedirecting = false

function redirectToLogin(message = '登录状态已失效，请重新登录') {
  localStorage.removeItem('adminToken')
  localStorage.removeItem('adminUser')

  if (!authRedirecting) {
    authRedirecting = true
    ElMessage.error(message)
    const currentPath = router.currentRoute.value.fullPath
    if (router.currentRoute.value.name !== 'login') {
      router.replace({ name: 'login', query: { redirect: currentPath, reason: message } })
    } else {
      router.replace({ name: 'login', query: { ...router.currentRoute.value.query, reason: message } })
    }
    setTimeout(() => {
      authRedirecting = false
    }, 300)
  }
}

http.interceptors.request.use((config) => {
  const t = localStorage.getItem('adminToken')
  if (t) {
    config.headers.Authorization = `Bearer ${t}`
  }
  return config
})

http.interceptors.response.use(
  (res) => {
    const body = res.data
    if (body && typeof body.code === 'number' && body.code !== 0) {
      if (body.code === 401 || body.code === 403) {
        redirectToLogin(body.msg || '登录状态已失效，请重新登录')
      }
      return Promise.reject(new Error(body.msg || '请求失败'))
    }
    return body
  },
  (err) => {
    const status = err?.response?.status
    if (status === 401 || status === 403) {
      redirectToLogin(status === 403 ? '没有权限访问，请重新登录' : '登录状态已失效，请重新登录')
      return Promise.reject(new Error(status === 403 ? '没有权限访问，请重新登录' : '登录状态已失效，请重新登录'))
    }
    return Promise.reject(err)
  },
)

export default http
