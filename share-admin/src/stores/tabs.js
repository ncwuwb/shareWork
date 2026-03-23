import { defineStore } from 'pinia'

export const useTabsStore = defineStore('tabs', {
  state: () => ({
    list: [{ path: '/dashboard', title: '首页', closable: false }],
    activePath: '/dashboard',
  }),
  actions: {
    syncRoute(route) {
      if (route.meta?.public) return
      const path = route.path === '/' ? '/dashboard' : route.path
      const title = route.meta?.title || route.name || path
      const exists = this.list.find((t) => t.path === path)
      if (!exists) {
        this.list.push({ path, title, closable: true })
      } else {
        exists.title = title
      }
      this.activePath = path
    },
    remove(path) {
      const i = this.list.findIndex((t) => t.path === path)
      if (i === -1 || !this.list[i].closable) return
      this.list.splice(i, 1)
      return this.list[Math.max(0, i - 1)]?.path || '/dashboard'
    },
    setActive(path) {
      this.activePath = path
    },
  },
})
