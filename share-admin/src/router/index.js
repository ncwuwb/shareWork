import { createRouter, createWebHistory } from 'vue-router'
import { useTabsStore } from '@/stores/tabs'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/Login.vue'),
    meta: { public: true },
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: 'dashboard', component: () => import('@/views/Dashboard.vue'), meta: { title: '数据看板' } },
      { path: 'spaces', name: 'spaces', component: () => import('@/views/Spaces.vue'), meta: { title: '空间层级' } },
      { path: 'facilities', name: 'facilities', component: () => import('@/views/Facilities.vue'), meta: { title: '设施标签' } },
      { path: 'workstations', name: 'workstations', component: () => import('@/views/Workstations.vue'), meta: { title: '工位管理' } },
      { path: 'orders', name: 'orders', component: () => import('@/views/OrdersBoard.vue'), meta: { title: '预约看板' } },
      { path: 'breaches', name: 'breaches', component: () => import('@/views/Breaches.vue'), meta: { title: '异常订单' } },
      { path: 'users', name: 'users', component: () => import('@/views/Users.vue'), meta: { title: '员工管理' } },
      { path: 'depts', name: 'depts', component: () => import('@/views/Depts.vue'), meta: { title: '部门' } },
      { path: 'config', name: 'config', component: () => import('@/views/Config.vue'), meta: { title: '系统参数' } },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

function updatePageTitle(to) {
  const title = to.meta?.title ? `${to.meta.title} - 共享工位管理端` : '共享工位管理端'
  document.title = title
}

router.afterEach((to) => {
  updatePageTitle(to)
  try {
    useTabsStore().syncRoute(to)
  } catch {
    /* pinia 未就绪时忽略 */
  }
})

router.beforeEach((to) => {
  const t = localStorage.getItem('adminToken')

  if (to.meta.public) {
    if (t) {
      const redirect = typeof to.query.redirect === 'string' && to.query.redirect ? to.query.redirect : '/dashboard'
      return redirect
    }
    return true
  }

  if (!t) {
    return {
      name: 'login',
      query: {
        redirect: to.fullPath,
        reason: '请先登录管理端账号',
      },
    }
  }
  return true
})

export default router
