<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import http from '@/api/http'

const page = ref({ records: [], total: 0 })
const q = ref({ page: 1, size: 10, phone: '' })

async function load() {
  try {
    const res = await http.get('/admin/users/page', { params: q.value })
    page.value = res.data
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
  }
}

onMounted(load)

async function toggleBan(row) {
  const st = row.status === 1 ? 0 : 1
  try {
    await http.post('/admin/users/' + row.id + '/ban?status=' + st)
    ElMessage.success('已更新')
    load()
  } catch (e) {
    ElMessage.error(e.message || '失败')
  }
}

function userStatusText(status) {
  if (status === 1) return '正常'
  if (status === 0) return '封禁'
  return '—'
}

async function exportUsers() {
  const res = await axios.get('/api/admin/users/export', {
    responseType: 'blob',
    headers: { Authorization: 'Bearer ' + localStorage.getItem('adminToken') },
  })
  const url = URL.createObjectURL(res.data)
  const a = document.createElement('a')
  a.href = url
  a.download = 'users.xlsx'
  a.click()
  URL.revokeObjectURL(url)
}
</script>

<template>
  <el-card>
    <el-space wrap>
      <el-input v-model="q.phone" placeholder="手机号" style="width: 160px" />
      <el-button type="primary" @click="load">查询</el-button>
      <el-button @click="exportUsers">导出 Excel</el-button>
    </el-space>
    <el-table :data="page.records" class="mt">
      <el-table-column prop="phone" label="手机号" />
      <el-table-column prop="realName" label="姓名" />
      <el-table-column prop="deptId" label="部门ID" width="90" />
      <el-table-column prop="creditScore" label="信用分" width="80" />
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small" effect="plain">
            {{ userStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button link @click="toggleBan(row)">{{ row.status === 1 ? '封禁' : '解封' }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      class="mt"
      background
      layout="prev, pager, next"
      :total="page.total"
      v-model:current-page="q.page"
      :page-size="q.size"
      @current-change="load"
    />
  </el-card>
</template>

<style scoped>
.mt {
  margin-top: 12px;
}
</style>
