<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '@/api/http'

const router = useRouter()
const route = useRoute()

const phone = ref('13800001001')
const password = ref('admin123')
const loading = ref(false)

const loginReason = computed(() => {
  const reason = String(route.query.reason || '').trim()
  if (!reason) return ''
  if (reason.includes('权限')) return '当前登录态没有管理端访问权限，请使用管理员账号重新登录。'
  if (reason.includes('失效')) return '登录状态已失效，请重新登录后继续操作。'
  return reason
})

async function submit() {
  loading.value = true
  try {
    const res = await http.post('/admin/login', {
      phone: phone.value,
      password: password.value,
    })
    localStorage.setItem('adminToken', res.data.token)
    localStorage.setItem('adminUser', JSON.stringify(res.data.user))
    const r = route.query.redirect || '/'
    router.replace(r)
  } catch (e) {
    ElMessage.error(e.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="wrap">
    <div class="bg-glow"></div>
    <el-card class="login-card" shadow="always">
      <h2 class="title">共享工位 · 管理端</h2>
      <p class="sub">基于 Java 的共享工位管理系统</p>
      <el-alert
        v-if="loginReason"
        :title="loginReason"
        type="warning"
        :closable="false"
        show-icon
        class="login-alert"
      />
      <el-form class="form" @submit.prevent="submit" label-position="top">
        <el-form-item label="手机号">
          <el-input v-model="phone" size="large" placeholder="请输入管理员手机号" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="password" type="password" show-password size="large" placeholder="请输入管理端密码" />
        </el-form-item>
        <el-button type="primary" :loading="loading" class="btn" native-type="submit" size="large">登录</el-button>
      </el-form>
      <p class="hint">仅 `超级管理员` 与 `区域管理员` 可登录管理端。</p>
    </el-card>
  </div>
</template>

<style scoped>
.wrap {
  min-height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #0f172a;
  position: relative;
  overflow: hidden;
}
.bg-glow {
  position: absolute;
  width: 480px;
  height: 480px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(56, 189, 248, 0.18), transparent 70%);
  top: 10%;
  left: 50%;
  transform: translateX(-50%);
  pointer-events: none;
}
.login-card {
  width: 420px;
  position: relative;
  z-index: 1;
  border-radius: 12px;
  --el-card-bg-color: #1e293b;
  border: 1px solid #334155;
}
.title {
  margin: 0;
  text-align: center;
  color: #f1f5f9;
  font-size: 22px;
  font-weight: 700;
}
.sub {
  text-align: center;
  color: #64748b;
  font-size: 13px;
  margin: 8px 0 24px;
}
.form {
  margin-top: 8px;
}
.login-alert {
  margin-bottom: 18px;
  text-align: left;
}
.btn {
  width: 100%;
  margin-top: 8px;
}
.hint {
  margin: 14px 0 0;
  text-align: center;
  font-size: 12px;
  color: #94a3b8;
}
</style>
