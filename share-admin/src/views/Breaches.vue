<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '@/api/http'

const list = ref([])

async function load() {
  try {
    const res = await http.get('/admin/orders/breaches')
    list.value = res.data || []
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
  }
}

async function revoke(id) {
  try {
    await http.post('/admin/orders/' + id + '/revoke-breach')
    ElMessage.success('已撤销违约')
    load()
  } catch (e) {
    ElMessage.error(e.message || '失败')
  }
}

onMounted(load)
</script>

<template>
  <el-card>
    <el-button @click="load">刷新</el-button>
    <el-table :data="list" class="mt">
      <el-table-column prop="id" label="订单ID" width="90" />
      <el-table-column prop="userId" label="用户" width="90" />
      <el-table-column prop="workstationId" label="工位" width="90" />
      <el-table-column prop="startTime" label="开始" />
      <el-table-column prop="endTime" label="结束" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button link type="primary" @click="revoke(row.id)">撤销违约</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<style scoped>
.mt {
  margin-top: 12px;
}
</style>
